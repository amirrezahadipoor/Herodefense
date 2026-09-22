#!/usr/bin/env python3
"""Promote the reviewed arena cover: twelve props, and not one pixel that was not audited.

The same shape as the other promotion tools in this directory: the candidate must be exactly the keys this batch
promises, the audit attached to the review document must hash the candidate manifest the promotion is reading, and
the review document must carry an ACCEPTED decision. What is different is that the cover arrives into a catalog
that does not carry it yet, so the promotion adds entries rather than replacing them -- and it refuses to run at all
if any of the twelve keys is already committed, because silently replacing reviewed art is the thing this gate is
for.

Usage:
    python3 tools/visual/promote_arena_cover_batch.py <candidate> <destination>
"""
from __future__ import annotations

import argparse
import hashlib
import json
import shutil
from pathlib import Path

from PIL import Image

REPOSITORY = Path(__file__).resolve().parents[2]
REVIEW_DOCUMENT = "docs/art_reviews/ARENA_COVER_REVIEW.md"
REVIEW_DIRECTORY = REPOSITORY / "docs/art_reviews/arena_cover"
AUDIT_PATH = REVIEW_DIRECTORY / "arena_cover_audit.json"
HASH_LEDGER = REPOSITORY / "docs/asset_hashes.json"
EXPECTED_KEYS = tuple(
    f"obstacle_{family}_{variant}"
    for family in ("standing_stone", "ruin_slab", "thorn_hedge", "mossy_boulder")
    for variant in range(3)
)
EXPECTED_SHEETS = {"arena_cover_families.png", "arena_cover_kinds.png"}
EXPECTED_SUMMARY = {
    "staticFrameCount": 12,
    "familyCount": 4,
    "coverPropCount": 12,
    "shelterPropCount": 6,
    "lowPropCount": 6,
    "decodedBytes": 12 * 384 * 384 * 4,
}


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("candidate", type=Path)
    parser.add_argument("destination", type=Path)
    args = parser.parse_args()
    source = args.candidate.resolve()
    destination = args.destination.resolve()
    if source == destination:
        raise ValueError("Candidate and destination must be different directories")

    candidate_manifest_path = resolve_inside(source, "asset_manifest.json")
    candidate_manifest = read_json(candidate_manifest_path)
    assets = candidate_manifest.get("assets", [])
    candidate_by_key = {asset["key"]: asset for asset in assets}
    if len(candidate_by_key) != len(assets) or tuple(sorted(candidate_by_key)) != tuple(sorted(EXPECTED_KEYS)):
        raise ValueError("The cover candidate must contain each of the twelve props exactly once")
    audit = validate_review_evidence(source, candidate_manifest_path)

    ledger_path = HASH_LEDGER
    ledger = read_json(ledger_path)
    catalog_path = resolve_inside(destination, "asset_manifest.json")
    catalog = read_json(catalog_path)
    by_key = {asset["key"]: asset for asset in catalog["assets"]}
    already = sorted(set(EXPECTED_KEYS) & set(by_key))
    if already:
        raise ValueError(
            f"the catalog already carries cover art ({already}); promoting over reviewed pixels is not this tool's job"
        )

    review = {
        "category": "arena_cover",
        "document": REVIEW_DOCUMENT,
        "status": "accepted",
        "auditSha256": sha256_file(AUDIT_PATH),
        "sourceManifestSha256": sha256_file(candidate_manifest_path),
    }
    for key in EXPECTED_KEYS:
        asset = json.loads(json.dumps(candidate_by_key[key]))
        sheet_relative = asset["sheet"]
        source_png = resolve_inside(source, sheet_relative)
        destination_png = resolve_inside(destination, sheet_relative)
        destination_png.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source_png, destination_png)
        asset["reviewDocument"] = REVIEW_DOCUMENT
        asset["categoryReview"] = review
        ledger.setdefault("sheets", {})[sheet_relative] = sha256_file(destination_png)
        by_key[key] = asset
        metadata_path = destination / "environment" / f"{key}.json"
        write_json(metadata_path, asset)

    catalog["pipelineVersion"] = max(
        int(catalog.get("pipelineVersion", 0)), int(candidate_manifest.get("pipelineVersion", 0))
    )
    catalog["generatedBatch"] = "arena-cover"
    catalog["assets"] = [by_key[key] for key in sorted(by_key)]
    # The catalog total is arithmetic over what ships: every sheet's pixels, plus the icons core resolves by
    # name. Recomputing it here rather than adding a number keeps `RuntimeResidencyTest`'s equality honest.
    catalog["decodedBytes"] = decoded_catalog_bytes(catalog, destination)
    write_json(catalog_path, catalog)
    write_json(ledger_path, ledger)
    print(
        f"Promoted {len(EXPECTED_KEYS)} reviewed cover props from audit {sha256_file(AUDIT_PATH)[:12]}; "
        f"catalog decodedBytes {catalog['decodedBytes']}"
    )


def validate_review_evidence(source: Path, candidate_manifest_path: Path) -> dict:
    if not AUDIT_PATH.is_file():
        raise ValueError(f"missing review audit at {AUDIT_PATH}")
    audit = read_json(AUDIT_PATH)
    review_text = (REPOSITORY / REVIEW_DOCUMENT).read_text(encoding="utf-8")
    if "**Decision:** ACCEPTED" not in review_text:
        raise ValueError(f"{REVIEW_DOCUMENT} does not carry an ACCEPTED decision")
    audit_sha = sha256_file(AUDIT_PATH)
    if audit_sha not in review_text:
        raise ValueError(f"{REVIEW_DOCUMENT} does not name the audit it accepted ({audit_sha[:12]})")
    manifest_sha = sha256_file(candidate_manifest_path)
    if manifest_sha not in review_text:
        raise ValueError(f"{REVIEW_DOCUMENT} does not name the candidate manifest it reviewed")
    if audit.get("candidateManifestSha256") != manifest_sha:
        raise ValueError("the audit describes a different candidate manifest than the one being promoted")
    if audit.get("expectedKeys") != list(EXPECTED_KEYS):
        raise ValueError("the audit does not cover exactly the twelve cover keys")
    if audit.get("batch") != "arena-cover-v1":
        raise ValueError(f"the audit is for batch {audit.get('batch')!r}, not the cover batch")
    summary = audit.get("summary", {})
    for field, value in EXPECTED_SUMMARY.items():
        if summary.get(field) != value:
            raise ValueError(f"audit summary {field} is {summary.get(field)!r}; expected {value!r}")
    sheets = {path.name for path in REVIEW_DIRECTORY.glob("*.png")}
    if sheets != EXPECTED_SHEETS:
        raise ValueError(f"review sheets are {sorted(sheets)}; expected {sorted(EXPECTED_SHEETS)}")
    recorded = audit.get("reviewSheets", {})
    if set(recorded) != EXPECTED_SHEETS or audit.get("reviewSheetCount") != len(EXPECTED_SHEETS):
        raise ValueError("the audit's sheet list disagrees with the sheets in the review directory")
    for name in sorted(EXPECTED_SHEETS):
        path = REVIEW_DIRECTORY / name
        if recorded[name]["sha256"] != sha256_file(path):
            raise ValueError(f"{name} does not hash to the audit")
    by_key = {record["key"]: record for record in audit.get("assets", [])}
    if set(by_key) != set(EXPECTED_KEYS):
        raise ValueError("the audit's records are not exactly the twelve cover keys")
    for key in EXPECTED_KEYS:
        record = by_key[key]
        relative = next(
            asset["sheet"]
            for asset in read_json(candidate_manifest_path)["assets"]
            if asset["key"] == key
        )
        if record["sheetSha256"] != sha256_file(resolve_inside(source, relative)):
            raise ValueError(f"{key} does not hash to the reviewed sheet")
        if record.get("runtimeGlow") is not False:
            raise ValueError(f"{key} was audited with runtimeGlow {record.get('runtimeGlow')!r}")
    return audit


def decoded_catalog_bytes(catalog: dict, destination: Path) -> int:
    total = 0
    for asset in catalog["assets"]:
        for sheet in asset.get("sheets", []):
            total += int(sheet["width"]) * int(sheet["height"]) * 4
        icon = asset.get("icon")
        if icon:
            path = resolve_inside(destination, icon)
            with Image.open(path) as image:
                total += image.width * image.height * 4
    return total


def resolve_inside(root: Path, relative: str | Path) -> Path:
    path = (root / relative).resolve()
    if not str(path).startswith(str(root.resolve())):
        raise ValueError(f"{relative} escapes {root}")
    return path


def read_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def write_json(path: Path, payload: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def sha256_file(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


if __name__ == "__main__":
    main()

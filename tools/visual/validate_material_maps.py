#!/usr/bin/env python3
"""Re-derive every committed material map and fail if it disagrees with its provenance (roadmap R5.5).

The maps under `docs/materials/` are generated artefacts, and a generated artefact that nothing re-derives is
just a PNG somebody once uploaded. This checker reads the provenance table written by
`generate_material_maps.py`, re-runs the derivation from each named master, and fails on any difference: a map
whose bytes changed, a master that changed underneath a map, a dimension that no longer matches the table.

It needs no network and no Blender: the masters ship in the repository, which is exactly why the maps name them
as their source.

Usage:
    python3 tools/visual/validate_material_maps.py
"""
from __future__ import annotations

import hashlib
import re
import sys
from pathlib import Path

from PIL import Image

TOOLS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS / "visual"))

import generate_material_maps as generator  # noqa: E402

ROOT = TOOLS.parent
ROW = re.compile(r"^\| `(?P<key>[^`]+)` \| (?P<family>[^|]+) \| (?P<kind>\w+) \| `(?P<path>[^`]+)` ")


def provenance_rows() -> list[dict[str, str]]:
    text = generator.PROVENANCE.read_text(encoding="utf-8")
    rows = []
    for line in text.splitlines():
        match = ROW.match(line)
        if match:
            rows.append(match.groupdict())
    return rows


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main() -> int:
    rows = provenance_rows()
    if not rows:
        print("the material-map provenance table has no rows")
        return 1

    masters = generator.master_files(generator.manifest())
    failures: list[str] = []
    for row in rows:
        path = ROOT / row["path"]
        if not path.is_file():
            failures.append(f"{row['path']} is listed in the provenance table but not committed")
            continue
        expected = generator.maps_for(generator.GENERATED / masters[row["key"]])[row["kind"]]
        with Image.open(path) as committed:
            actual = committed.convert("RGBA")
        if actual.size != expected.size:
            failures.append(f"{row['path']} is {actual.size}, the derivation gives {expected.size}")
            continue
        if actual.tobytes() != expected.tobytes():
            failures.append(f"{row['path']} does not match what its master re-derives to")
            continue
        if f"`{sha256(path)[:16]}...`" not in generator.PROVENANCE.read_text(encoding="utf-8"):
            failures.append(f"{row['path']} hash is not quoted in the provenance table")

    if failures:
        for failure in failures[:10]:
            print(failure)
        print(f"{len(failures)} material map(s) disagree with their provenance")
        return 1

    print(
        f"{len(rows)} material maps re-derived from their masters, "
        f"{len({row['key'] for row in rows})} entities, {len({row['kind'] for row in rows})} channels"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

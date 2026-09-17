#!/usr/bin/env python3
"""The render hash log: what a batch produced, what changed, and what is left to render.

Roadmap R5.3. The Blender batches render in CI (`generate-visual-assets.yml`) and a render is the most
expensive thing this repository does, so three questions have to be answerable from the artifacts alone:

* **What did this batch produce?** `write` records the sha256 of every sheet, atlas and descriptor the
  batch wrote, plus the asset keys its manifest declares. The log holds no timestamps, no absolute paths
  and no commit hashes, so two logs of the same bytes are byte-identical and can be compared.
* **What changed since the last one?** `diff` classifies every entry as added, removed or changed, and
  `--fail-on-change` turns that into a gate for promotion work ("this render is the reviewed one").
* **What is left to render?** `resume` answers it from a manifest and a log: a key whose files are
  missing, empty or no longer hash to what the log recorded is a key this render did not finish. The
  workflow feeds that list straight back into the generator's `--only`, which is what makes a batch
  resumable instead of all-or-nothing inside a 120-minute job.

`asset_manifest.json` is deliberately *not* hashed: it carries `generatedAt` and `generatedCommit`, so its
bytes change on every run for reasons that have nothing to do with the render. Its asset keys are recorded
instead, and they are what an interrupted batch has to complete.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import sys
from pathlib import Path

LOG_NAME = "render_hash_log.json"
LOG_VERSION = 1
ALGORITHM = "sha256"
MANIFEST_NAME = "asset_manifest.json"
#: Directories a render may keep scratch frames in; they are not output.
IGNORED_DIRECTORIES = ("_frames",)
#: Files hashed into the log, by suffix.
HASHED_SUFFIXES = (".png", ".atlas", ".json")


def digest(path: Path) -> str:
    """The sha256 of a file, read in chunks so a 384-pixel atlas never lands in memory whole."""
    accumulator = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1 << 20), b""):
            accumulator.update(chunk)
    return accumulator.hexdigest()


def files_to_log(root: Path) -> list[Path]:
    """Every output file under `root`, sorted, minus the scratch directories and the log itself."""
    found = []
    for path in sorted(root.rglob("*")):
        if not path.is_file() or path.name in (LOG_NAME, MANIFEST_NAME):
            continue
        if any(part in IGNORED_DIRECTORIES for part in path.relative_to(root).parts):
            continue
        if path.suffix.lower() not in HASHED_SUFFIXES:
            continue
        found.append(path)
    return found


def manifest_keys(root: Path) -> list[str]:
    """The asset keys the batch's manifest declares, sorted; empty when there is no manifest."""
    path = root / MANIFEST_NAME
    if not path.is_file():
        return []
    try:
        manifest = json.loads(path.read_text(encoding="utf-8"))
    except ValueError:
        return []
    return sorted(entry.get("key", "") for entry in manifest.get("assets", []) if entry.get("key"))


def build_log(root: Path) -> dict:
    """The log of a render directory: one entry per file, plus the keys the manifest declares."""
    root = root.resolve()
    entries = {}
    for path in files_to_log(root):
        relative = path.relative_to(root).as_posix()
        entries[relative] = {"sha256": digest(path), "bytes": path.stat().st_size}
    return {
        "version": LOG_VERSION,
        "algorithm": ALGORITHM,
        "files": entries,
        "assetKeys": manifest_keys(root),
    }


def write_log(root: Path, log_path: Path | None = None) -> tuple[Path, int]:
    """Writes the log for `root` and returns where it went and how many files it covers."""
    root = root.resolve()
    target = log_path.resolve() if log_path else root / LOG_NAME
    log = build_log(root)
    target.write_text(json.dumps(log, indent=1, sort_keys=True) + "\n", encoding="utf-8")
    return target, len(log["files"])


def read_log(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def diff_logs(expected: dict, actual: dict) -> dict:
    """Added, removed and changed files, plus the asset keys one log has and the other does not."""
    before = expected.get("files", {})
    after = actual.get("files", {})
    added = sorted(set(after) - set(before))
    removed = sorted(set(before) - set(after))
    changed = sorted(
        name for name in set(before) & set(after) if before[name].get("sha256") != after[name].get("sha256")
    )
    keys_added = sorted(set(actual.get("assetKeys", [])) - set(expected.get("assetKeys", [])))
    keys_removed = sorted(set(expected.get("assetKeys", [])) - set(actual.get("assetKeys", [])))
    return {
        "added": added,
        "removed": removed,
        "changed": changed,
        "keysAdded": keys_added,
        "keysRemoved": keys_removed,
    }


def has_differences(diff: dict) -> bool:
    return any(diff[name] for name in ("added", "removed", "changed", "keysAdded", "keysRemoved"))


def format_diff(diff: dict) -> str:
    """A human summary: counts first, then the names, so a log is readable in a CI step summary."""
    lines = [
        f"added {len(diff['added'])}, changed {len(diff['changed'])}, removed {len(diff['removed'])}, "
        f"keys added {len(diff['keysAdded'])}, keys removed {len(diff['keysRemoved'])}"
    ]
    for name in ("added", "removed", "changed"):
        for entry in diff[name]:
            lines.append(f"  {name}: {entry}")
    for name in ("keysAdded", "keysRemoved"):
        for entry in diff[name]:
            lines.append(f"  {name}: {entry}")
    return "\n".join(lines)


def files_for_key(manifest: dict, key: str) -> list[str]:
    """The output files a manifest entry claims, as relative posix paths."""
    entry = next((item for item in manifest.get("assets", []) if item.get("key") == key), None)
    if entry is None:
        return []
    names = [
        value for field, value in entry.items()
        if isinstance(value, str) and field.lower().endswith(("sheet", "atlas", "page", "icon", "image"))
    ]
    return sorted(set(names))


def keys_to_render(manifest: dict, log: dict, root: Path) -> dict:
    """Which asset keys this log says are unfinished, and why.

    "Missing" means the render never finished the key at all: a file is absent, empty, or absent from the
    log. "Changed" means every file is there but at least one of them no longer hashes to what the log
    recorded, which is a render that ran and produced different bytes. Both are keys a resumed batch has to
    render again; the split is for the human reading the report.
    """
    root = root.resolve()
    recorded = log.get("files", {})
    outstanding: dict[str, list[str]] = {"missing": [], "changed": []}
    for key in sorted(entry.get("key", "") for entry in manifest.get("assets", []) if entry.get("key")):
        wanted = files_for_key(manifest, key)
        if not wanted:
            outstanding["missing"].append(f"{key}: the manifest names no rendered file for it")
            continue
        reasons: list[str] = []
        unfinished = False
        for name in wanted:
            path = root / name
            entry = recorded.get(name)
            if not path.is_file():
                reasons.append(f"{name} missing")
                unfinished = True
            elif path.stat().st_size == 0:
                reasons.append(f"{name} is empty")
                unfinished = True
            elif entry is None:
                reasons.append(f"{name} was never rendered")
                unfinished = True
            elif entry.get("bytes") not in (None, path.stat().st_size) or entry.get("sha256") != digest(path):
                reasons.append(f"{name} does not hash to the log")
        if reasons:
            outstanding["missing" if unfinished else "changed"].append(f"{key}: " + "; ".join(reasons))
    return outstanding


def _keys_from_reasons(reasons: list[str]) -> list[str]:
    return sorted(reason.split(":", 1)[0] for reason in reasons)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    commands = parser.add_subparsers(dest="command", required=True)

    write = commands.add_parser("write", help="record the hash log of a render directory")
    write.add_argument("root", type=Path)
    write.add_argument("--log", type=Path, default=None)

    compare = commands.add_parser("diff", help="classify the differences between two hash logs")
    compare.add_argument("expected", type=Path)
    compare.add_argument("actual", type=Path)
    compare.add_argument("--fail-on-change", action="store_true",
                         help="exit 1 when anything differs, for promotion gates")

    resume = commands.add_parser("resume", help="list the asset keys an incomplete render still owes")
    resume.add_argument("manifest", type=Path)
    resume.add_argument("log", type=Path)
    resume.add_argument("--root", type=Path, required=True)
    resume.add_argument("--keys", action="store_true", help="print only the keys, space separated")

    args = parser.parse_args(argv)
    if args.command == "write":
        target, count = write_log(args.root, args.log)
        print(f"wrote {target} covering {count} file(s)")
        return 0
    if args.command == "diff":
        diff = diff_logs(read_log(args.expected), read_log(args.actual))
        print(format_diff(diff))
        if args.fail_on_change and has_differences(diff):
            print("the render differs from the log it was compared against", file=sys.stderr)
            return 1
        return 0
    if not args.manifest.is_file():
        # No manifest means nothing has ever finished here: the batch renders from the start, and an empty
        # list says exactly that rather than failing a step whose whole job is to be quiet when there is
        # nothing to resume.
        print("no previous manifest: this render starts from nothing")
        return 0
    manifest = json.loads(args.manifest.read_text(encoding="utf-8"))
    outstanding = keys_to_render(manifest, read_log(args.log), args.root)
    keys = _keys_from_reasons(outstanding["missing"]) + _keys_from_reasons(outstanding["changed"])
    if args.keys:
        print(" ".join(keys))
        return 0
    print(f"{len(keys)} asset key(s) still owed by this render")
    for label in ("missing", "changed"):
        for reason in outstanding[label]:
            print(f"  {label}: {reason}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

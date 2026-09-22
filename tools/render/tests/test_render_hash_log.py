"""Tests for the render hash log (roadmap R5.3).

The log is what makes a render reproducible, comparable and resumable, so the things worth testing are the
ones the workflow depends on: identical bytes produce identical logs, a difference is classified rather
than merely detected, and an interrupted render can be asked what it still owes.
"""
from __future__ import annotations

import json
import sys
import tempfile
import contextlib
import io
import unittest
from pathlib import Path

TOOLS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS))

import render_hash_log as log_tool  # noqa: E402


def write(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def manifest(*keys: str) -> dict:
    return {"assets": [
        {"key": key, "sheet": f"sprites/{key}.png", "atlas": f"sprites/{key}.atlas"} for key in keys
    ]}


class RenderHashLogTest(unittest.TestCase):
    def setUp(self) -> None:
        self.directory = tempfile.TemporaryDirectory()
        # The logs of a real run live outside the render directory: a log inside the tree it describes would
        # be a file the tree does not have.
        self.root = Path(self.directory.name) / "rendered"
        write(self.root / "sprites/rootling.png", "sheet bytes")
        write(self.root / "sprites/rootling.atlas", "atlas bytes")
        write(self.root / "sprites/notes.txt", "not an output file")
        write(self.root / "_frames/scratch.png", "scratch frame")
        (self.root / "asset_manifest.json").write_text(
            json.dumps({"generatedAt": "2026-09-17T00:00:00Z", **manifest("rootling")}), encoding="utf-8")
        self.log = log_tool.build_log(self.root)

    def tearDown(self) -> None:
        self.directory.cleanup()

    def testTheLogHoldsEveryOutputAndNothingElse(self) -> None:
        self.assertEqual(["sprites/rootling.atlas", "sprites/rootling.png"], sorted(self.log["files"]))
        self.assertEqual(["rootling"], self.log["assetKeys"])
        self.assertEqual("sha256", self.log["algorithm"])

    def testTheSameBytesAlwaysProduceTheSameLog(self) -> None:
        first, _ = log_tool.write_log(self.root)
        second = json.loads(first.read_text(encoding="utf-8"))
        self.assertEqual(self.log, second, "a log that changes between two reads of one tree is not a log")
        text = first.read_text(encoding="utf-8")
        self.assertNotIn("2026", text, "no timestamp may enter the log, or two logs could never be equal")
        self.assertNotIn(str(self.root), text, "no absolute path may enter the log")

    def testTheManifestIsRecordedAsKeysRatherThanBytes(self) -> None:
        write(self.root / "asset_manifest.json", json.dumps({"generatedAt": "later", **manifest("rootling")}))
        self.assertEqual(self.log, log_tool.build_log(self.root),
                         "the manifest carries a timestamp, so its keys are logged and its bytes are not")

    def testDiffClassifiesAddedRemovedAndChangedFiles(self) -> None:
        write(self.root / "sprites/stonekin.png", "new sheet")
        write(self.root / "sprites/rootling.png", "re-rendered sheet")
        (self.root / "sprites/rootling.atlas").unlink()
        after = log_tool.build_log(self.root)
        diff = log_tool.diff_logs(self.log, after)
        self.assertEqual(["sprites/stonekin.png"], diff["added"])
        self.assertEqual(["sprites/rootling.atlas"], diff["removed"])
        self.assertEqual(["sprites/rootling.png"], diff["changed"])
        self.assertTrue(log_tool.has_differences(diff))
        self.assertIn("changed: sprites/rootling.png", log_tool.format_diff(diff))

    def testAReRunOfTheSameBytesIsNotADifference(self) -> None:
        diff = log_tool.diff_logs(self.log, log_tool.build_log(self.root))
        self.assertFalse(log_tool.has_differences(diff), "a clean re-render must diff clean")
        self.assertIn("added 0, changed 0, removed 0", log_tool.format_diff(diff))

    def testKeysThatOnlyOneLogDeclaresAreDifferences(self) -> None:
        diff = log_tool.diff_logs(self.log, log_tool.build_log(self.root) | {"assetKeys": ["rootling", "wolf"]})
        self.assertEqual(["wolf"], diff["keysAdded"])
        self.assertTrue(log_tool.has_differences(diff))

    def testFailOnChangeExitsNonZeroOnlyWhenSomethingChanged(self) -> None:
        before = self.root.parent / "before.json"
        log_tool.write_log(self.root, before)
        after = self.root.parent / "after.json"
        write(self.root / "sprites/rootling.png", "re-rendered")
        log_tool.write_log(self.root, after)
        self.assertEqual(0, log_tool.main(["diff", str(before), str(after)]))
        self.assertEqual(1, log_tool.main(["diff", str(before), str(after), "--fail-on-change"]))

    def testResumeNamesTheKeysAnIncompleteRenderStillOwes(self) -> None:
        complete = manifest("rootling", "stonekin")
        log = log_tool.build_log(self.root)
        write(self.root / "sprites/stonekin.png", "half a sheet")
        (self.root / "sprites/stonekin.atlas").unlink(missing_ok=True)
        outstanding = log_tool.keys_to_render(complete, log, self.root)
        self.assertEqual(1, len(outstanding["missing"]))
        self.assertIn("stonekin", outstanding["missing"][0])
        self.assertEqual([], outstanding["changed"])

    def testResumeCatchesAnAlteredOrTruncatedSheet(self) -> None:
        write(self.root / "sprites/wolf.png", "the sheet the log recorded")
        write(self.root / "sprites/wolf.atlas", "the atlas the log recorded")
        log = log_tool.build_log(self.root)
        write(self.root / "sprites/wolf.png", "an altered sheet")
        write(self.root / "sprites/wolf.atlas", "")
        outstanding = log_tool.keys_to_render(manifest("wolf"), log, self.root)
        keys = log_tool._keys_from_reasons(outstanding["missing"]) + log_tool._keys_from_reasons(
            outstanding["changed"])
        self.assertEqual(["wolf"], keys, "an altered or emptied sheet means the key is owed again")

    def testKeysModeStaysEmptyWhenNoManifestHasEverBeenRendered(self) -> None:
        # The workflow consumes `--keys` as Blender's `--only` argument list. With no previous manifest there is
        # nothing to resume, and the note that says so belongs to the human-readable mode: printing it in keys
        # mode passed the words "no previous manifest: this render starts from nothing" to Blender, which
        # rendered nothing and left the step looking like a successful resume.
        missing = self.root / "never-rendered/asset_manifest.json"
        printed: list[str] = []
        with contextlib.redirect_stdout(io.StringIO()) as captured:
            self.assertEqual(0, log_tool.main([
                "resume", str(missing), str(self.root / "render_hash_log.json"),
                "--root", str(self.root), "--keys",
            ]))
        printed.append(captured.getvalue())
        self.assertEqual("", printed[0], "keys mode printed something that is not a key list")
        with contextlib.redirect_stdout(io.StringIO()) as captured:
            self.assertEqual(0, log_tool.main([
                "resume", str(missing), str(self.root / "render_hash_log.json"), "--root", str(self.root),
            ]))
        self.assertIn("no previous manifest", captured.getvalue())

    def testResumeIsEmptyWhenTheLogMatchesTheTree(self) -> None:
        log = log_tool.build_log(self.root)
        outstanding = log_tool.keys_to_render(manifest("rootling"), log, self.root)
        self.assertEqual({"missing": [], "changed": []}, outstanding)


if __name__ == "__main__":
    unittest.main()

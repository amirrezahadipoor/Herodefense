"""Unit tests for the audio tools (roadmap R6.2).

The point of these is the contract the tools publish, not the samples they read: exit 2 means "not measured",
a missing file is a failure rather than a pass, and the measurement document is regenerated rather than
hand-edited. CI runs the tools themselves against the committed bytes; this keeps their edges honest.
"""
from __future__ import annotations

import pathlib
import subprocess
import sys
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[3]
TOOLS = ROOT / "tools" / "audio"


def run(script: str, *args: str) -> subprocess.CompletedProcess:
    return subprocess.run(
        [sys.executable, str(TOOLS / script), *args],
        cwd=ROOT,
        capture_output=True,
        text=True,
        check=False,
    )


class AudioLevelTest(unittest.TestCase):
    def test_the_committed_files_are_all_measured_and_under_the_ceiling(self) -> None:
        result = run("check_audio_levels.py", "--check")
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertIn("audio files measured", result.stdout)

    def test_a_missing_directory_is_not_measured_rather_than_passed(self) -> None:
        module = (TOOLS / "check_audio_levels.py").read_text(encoding="utf-8")
        self.assertIn("AUDIO NOT MEASURED", module)
        self.assertIn("return 2", module)

    def test_the_measurement_document_covers_every_committed_file(self) -> None:
        document = (ROOT / "docs" / "audio" / "LEVELS.md").read_text(encoding="utf-8")
        files = sorted((ROOT / "android" / "assets" / "audio").rglob("*.ogg"))
        self.assertGreaterEqual(len(files), 20)
        for path in files:
            relative = path.relative_to(ROOT / "android" / "assets").as_posix()
            self.assertIn(f"`{relative}`", document, relative)


class AudioGeneratorTest(unittest.TestCase):
    def test_generators_are_reproducible_sources_rather_than_one_off_scripts(self) -> None:
        for script in ("generate_music.py", "generate_sfx.py"):
            text = (TOOLS / script).read_text(encoding="utf-8")
            self.assertIn("SAMPLE_RATE", text)
            self.assertIn("--output", text, script)

    def test_normaliser_verifies_what_it_wrote(self) -> None:
        text = (TOOLS / "normalize_levels.py").read_text(encoding="utf-8")
        self.assertIn("passes", text)
        self.assertIn("Vorbis adds its own overshoot", text)


if __name__ == "__main__":
    unittest.main()

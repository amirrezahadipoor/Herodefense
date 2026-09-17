#!/usr/bin/env python3
"""The containers in the bundle are the ones the gate passed, and they still are.

`encode_textures.py` decides what ships; this file re-derives that decision from the bundle itself, so a
container that was hand-copied in, left behind by an older encoder, or written by a run whose numbers have
since changed cannot sit in `android/assets/generated` quietly. Three things are checked for every container
found there, and nothing about them is taken from the tool's own report:

* it has the PNG it was encoded from, at the path the runtime looks beside;
* its header names a format the device path supports, and its dimensions are the PNG's;
* decoding it lands within the tool's own bar of the PNG over the pixels that show, and its one-bit alpha
  agrees with the PNG's mask exactly -- the two conditions the tool ships on.

The report is only used to cross-check: a container that shipped has to have a line in it, with the same
number of bytes. `docs/perf/texture-encoding-report.json` is the run that measured them, and the run it belongs to is
filed as `perf:2026-09-17-texture-encoding-sweep`.
"""
from __future__ import annotations

import json
import pathlib
import sys
import unittest

import numpy as np
from PIL import Image

TOOLS = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS))
sys.path.insert(0, str(TOOLS / "texture"))
sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))

import encode_textures  # noqa: E402
import etc2  # noqa: E402
import ktx  # noqa: E402

REPOSITORY = pathlib.Path(__file__).resolve().parents[3]
GENERATED = REPOSITORY / "android" / "assets" / "generated"
REPORT = REPOSITORY / "docs" / "perf" / "texture-encoding-report.json"


def containers() -> list[pathlib.Path]:
    return sorted(GENERATED.glob("**/compressed/etc2/*.ktx"))


class ShippedContainersTest(unittest.TestCase):
    def test_every_container_has_the_png_it_was_encoded_from(self) -> None:
        for container in containers():
            png = container.parents[2] / (container.stem + ".png")
            self.assertTrue(png.is_file(), f"{container.relative_to(GENERATED)} has no sheet beside it")

    def test_no_container_is_in_the_bundle_without_a_line_in_the_report(self) -> None:
        if not containers():
            self.skipTest("no container ships yet, so there is nothing to account for")
        document = json.loads(REPORT.read_text())
        shipped = {record["sheet"]: record for record in document["sheets"] if record.get("encoded")}
        for container in containers():
            sheet = str(container.parents[2].relative_to(GENERATED) / (container.stem + ".png"))
            self.assertIn(sheet, shipped, f"{sheet} ships a container the filed run does not account for")
            self.assertEqual(
                shipped[sheet]["encodedBytes"], len(ktx.read(container.read_bytes()).payload),
                f"{sheet}: the container in the bundle is not the one the run measured",
            )

    def test_every_container_decodes_to_what_the_tool_shipped_it_for(self) -> None:
        floor = encode_textures.DEFAULT_MINIMUM_PSNR
        for container in containers():
            image = ktx.read(container.read_bytes())
            png = container.parents[2] / (container.stem + ".png")
            with Image.open(png) as source:
                sheet = np.asarray(source.convert("RGBA"), dtype=np.uint8)
            self.assertTrue(
                image.gl_internal_format in (
                    etc2.ETC2_RGB8_PUNCHTHROUGH_ALPHA1, etc2.ETC2_RGB8
                ),
                f"{container.name}: 0x{image.gl_internal_format:x} is not a format the device path claims",
            )
            self.assertTrue(
                image.width == sheet.shape[1] and image.height == sheet.shape[0],
                f"{container.name}: {image.width}x{image.height} is not the sheet's "
                f"{sheet.shape[1]}x{sheet.shape[0]}",
            )
            punchthrough = image.gl_internal_format == etc2.ETC2_RGB8_PUNCHTHROUGH_ALPHA1
            decoded = etc2.decode_blocks(
                image.payload, image.width, image.height, punchthrough=punchthrough
            )
            opaque = sheet[..., 3] >= etc2.OPAQUE_THRESHOLD
            quality = encode_textures.peak_signal_to_noise(sheet[..., :3], decoded[..., :3], opaque)
            self.assertGreaterEqual(
                quality, floor,
                f"{container.name}: decodes at {quality:.2f} dB, under the {floor:.2f} dB this tool ships on",
            )
            if punchthrough:
                self.assertGreaterEqual(
                    etc2.binary_alpha_fraction(sheet), encode_textures.MINIMUM_BINARY_ALPHA,
                    f"{container.name}: the sheet's alpha stopped being a mask",
                )
                mask = decoded[..., 3] >= etc2.OPAQUE_THRESHOLD
                self.assertEqual(
                    int(np.count_nonzero(mask != opaque)), 0,
                    f"{container.name}: the container's mask disagrees with the sheet's",
                )

    def test_the_run_that_measured_them_is_filed(self) -> None:
        if not containers():
            self.skipTest("no container ships yet")
        self.assertTrue(REPORT.is_file(), f"{REPORT.name} is a filed run, not a number in a document")
        document = json.loads(REPORT.read_text())
        self.assertEqual(document["minimumPsnrDb"], encode_textures.DEFAULT_MINIMUM_PSNR)
        self.assertEqual(document["referenceEncoderLicence"], "Apache-2.0", "the reference is named and licensed")
        self.assertTrue(document["measuredReferenceOnThisRun"], "the run names its reference as measured")


if __name__ == "__main__":
    unittest.main()

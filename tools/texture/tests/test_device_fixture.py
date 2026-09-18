"""The device fixture R8.1's upload test reads (roadmap R8.1, the evidence half).

The instrumentation test `CompressedTextureDeviceTest` uploads `rootling-64.ktx` through GLES3 and compares what
the GPU decodes with `rootling-64.rgba`. That comparison only means something if those committed bytes are what
this repository's encoder produces from the source sheet *today* -- a fixture that quietly drifted would let the
device test pass against a picture of a payload nobody can rebuild. This test rebuilds it and compares, byte for
byte, against the three files in the instrumentation assets.
"""
from __future__ import annotations

import json
import sys
import unittest
from pathlib import Path

import numpy as np

TOOLS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS))
sys.path.insert(0, str(TOOLS / "texture"))

import etc2  # noqa: E402
import make_device_fixture  # noqa: E402


class DeviceFixtureTest(unittest.TestCase):
    def test_the_committed_fixture_is_rebuildable(self) -> None:
        rebuilt = make_device_fixture.build()
        for name, expected in rebuilt.items():
            path = make_device_fixture.FIXTURES / name
            self.assertTrue(path.exists(), f"{name} is missing from the instrumentation assets")
            self.assertEqual(
                expected,
                path.read_bytes(),
                f"{name} differs from a rebuild; run tools/texture/make_device_fixture.py --write",
            )

    def test_the_container_is_the_mode_the_device_test_uploads(self) -> None:
        document = json.loads(
            (make_device_fixture.FIXTURES / f"{make_device_fixture.NAME}.json").read_text())
        self.assertEqual(etc2.ETC2_RGB8_PUNCHTHROUGH_ALPHA1, document["glInternalFormat"])
        self.assertEqual(64, document["crop"]["width"])
        self.assertEqual(64, document["crop"]["height"])
        # The crop is the busiest square of the sheet: an empty one would exercise the upload without ever
        # asking a GPU to decode a colour.
        self.assertLess(document["roundTripPsnr"], 99.0)
        self.assertGreater(document["roundTripPsnr"], 20.0)


    def test_the_flag_bit_agrees_with_the_pixels_of_every_block(self) -> None:
        """The one rule the driver enforces that the encoder could get wrong on its own: the opaque bit.

        In the punchthrough format the bit that selects the colour layout in ETC2_RGB8 is the opaque bit, and it
        decides two things at once in the decoder: which intensity-modifier table the block is read with, and
        whether an index of `10` punches a hole. A block that carries a clear pixel and declares itself opaque
        therefore decodes with a hole missing on a device -- the defect the emulator caught, in the state it was
        caught in, where the flag was *never* set and every block was read as non-opaque under the wrong table.

        The fixture is the stream the device uploads, so this checks the actual bytes rather than a fresh encode.
        """
        container = (make_device_fixture.FIXTURES / f"{make_device_fixture.NAME}.ktx").read_bytes()
        payload = container[68:]
        self.assertEqual(0, len(payload) % 8)
        opaque_blocks = clear_blocks = 0
        for offset in range(0, len(payload), 8):
            high = int.from_bytes(payload[offset:offset + 4], "big")
            low = int.from_bytes(payload[offset + 4:offset + 8], "big")
            flag = (high >> 1) & 1
            bits = np.arange(16)
            indices = ((low >> bits) & 1) | (((low >> (bits + 16)) & 1) << 1)
            clears = int((indices == etc2.PUNCHTHROUGH_INDEX).sum())
            opaque_blocks += flag
            clear_blocks += 1 if clears else 0
            if clears:
                self.assertEqual(
                    0, flag,
                    f"block {offset // 8} declares itself opaque and still carries a clear pixel",
                )
        self.assertGreater(clear_blocks, 0, "the fixture carried no clear pixel to check the opaque bit on")
        self.assertGreater(
            opaque_blocks, 0,
            "the fixture has no opaque block, so the flag is never exercised the other way",
        )

    def test_the_expectation_is_the_decode_of_the_container(self) -> None:
        container = (make_device_fixture.FIXTURES / f"{make_device_fixture.NAME}.ktx").read_bytes()
        expectation = (make_device_fixture.FIXTURES / f"{make_device_fixture.NAME}.rgba").read_bytes()
        image = etc2.decode_blocks(container[68:], make_device_fixture.SIZE, make_device_fixture.SIZE,
                                   punchthrough=True)
        self.assertEqual(expectation, image.tobytes())


if __name__ == "__main__":
    unittest.main()

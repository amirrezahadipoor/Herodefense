"""The device evidence the log carries has to reassemble exactly, or not at all."""
import base64
import hashlib
import pathlib
import sys
import unittest

TOOLS = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS))

import decode_device_evidence  # noqa: E402


def log_for(name: str, payload: bytes, chunk: int = 700) -> str:
    digest = hashlib.sha256(payload).hexdigest()
    encoded = base64.b64encode(payload).decode("ascii")
    lines = [f"09-17 10:39:57.000  1234  1234 I HERODEFENSE_TEXTURE_PIXELS: {name} "
             f"bytes={len(payload)} sha256={digest}"]
    for offset in range(0, len(encoded), chunk):
        lines.append("09-17 10:39:57.001  1234  1234 I HERODEFENSE_TEXTURE_PIXELS: "
                     f"{name} {encoded[offset:offset + chunk]}")
    lines.append(f"09-17 10:39:57.002  1234  1234 I HERODEFENSE_TEXTURE_PIXELS: {name} end")
    return "\n".join(lines) + "\n"


class DeviceEvidenceTest(unittest.TestCase):
    def test_a_logged_buffer_comes_back_byte_for_byte(self) -> None:
        payload = bytes(range(256)) * 64
        buffers = decode_device_evidence.reassemble(log_for("texture-rendered", payload))
        self.assertEqual(payload, buffers["texture-rendered"])

    def test_a_buffer_whose_bytes_do_not_match_the_logged_hash_is_refused(self) -> None:
        payload = bytes(range(256)) * 64
        text = log_for("texture-rendered", payload)
        # One character of the payload flipped: the length still matches, the hash does not.
        lines = text.splitlines()
        for index, line in enumerate(lines):
            if "bytes=" in line:
                continue
            if " end" in line:
                continue
            marker = "texture-rendered "
            head, _, body = line.partition(marker)
            flipped = ("A" if body[0] != "A" else "B") + body[1:]
            lines[index] = head + marker + flipped
            break
        buffers = decode_device_evidence.reassemble("\n".join(lines))
        self.assertEqual({}, buffers)

    def test_a_log_without_the_tag_reassembles_nothing(self) -> None:
        self.assertEqual({}, decode_device_evidence.reassemble("nothing here\n"))


if __name__ == "__main__":
    unittest.main()

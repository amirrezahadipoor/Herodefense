#!/usr/bin/env python3
"""KTX v1 containers, written and read here rather than by a library.

Why KTX and why this file: libGDX ships `KTXTextureData`, which reads a KTX v1 header and hands the payload
straight to `glCompressedTexImage2D` with the internal format the header names -- so a container this tool writes
is a container the runtime can already load, with no new dependency on either side. The header is small and
fully specified (twelve bytes of magic, then thirteen little-endian 32-bit fields, then the mip levels), which is
why writing it here is cheaper than taking on a package for it.

Only what the tool needs is supported: 2D textures, no arrays, no cubemaps, one mip level, one face.
"""
from __future__ import annotations

import struct
from dataclasses import dataclass

#: KTX 1 identification, "«KTX 11»" as the specification writes it.
IDENTIFIER = b"\xABKTX 11\xBB\r\n\x1A\n"

#: The endianness marker that says the header is little-endian.
ENDIANNESS = 0x04030201

#: `glType`, `glFormat` and the base internal format of a compressed colour texture.
GL_UNSIGNED_BYTE = 0x1401
GL_RGBA = 0x1908
GL_RGB = 0x1907


@dataclass(frozen=True)
class Image:
    """One level of one texture, as the header describes it."""

    width: int
    height: int
    gl_internal_format: int
    gl_base_internal_format: int
    payload: bytes

    @property
    def bytes_per_pixel(self) -> float:
        return len(self.payload) / float(self.width * self.height)


def write(image: Image) -> bytes:
    """Serialise one compressed image as a KTX v1 file."""
    header = struct.pack(
        "<12s13I",
        IDENTIFIER,
        ENDIANNESS,
        0,                       # glType: compressed data has none
        1,                       # glTypeSize
        0,                       # glFormat
        image.gl_internal_format,
        image.gl_base_internal_format,
        image.width,
        image.height,
        0,                       # pixelDepth: 2D
        0,                       # numberOfArrayElements
        1,                       # numberOfFaces
        1,                       # numberOfMipmapLevels
        0,                       # bytesOfKeyValueData
    )
    level = struct.pack("<I", len(image.payload)) + image.payload
    # Every level starts on a four-byte boundary.
    padding = b"\x00" * (-len(level) % 4)
    return header + level + padding


def read(blob: bytes) -> Image:
    """Parse a KTX v1 file written by `write`, and refuse anything else."""
    if len(blob) < 64 or blob[:12] != IDENTIFIER:
        raise ValueError("not a KTX v1 file")
    fields = struct.unpack("<13I", blob[12:64])
    (
        endianness,
        gl_type,
        gl_type_size,
        gl_format,
        gl_internal_format,
        gl_base_internal_format,
        width,
        height,
        depth,
        arrays,
        faces,
        levels,
        key_value_bytes,
    ) = fields
    if endianness != ENDIANNESS:
        raise ValueError("KTX file is big-endian; this tool writes little-endian")
    if (gl_type, gl_format) != (0, 0):
        raise ValueError("this reader only handles compressed payloads")
    if (depth, arrays, faces, levels) != (0, 0, 1, 1):
        raise ValueError("this reader only handles 2D, single-level, single-face textures")
    start = 64 + key_value_bytes
    size = struct.unpack("<I", blob[start:start + 4])[0]
    payload = blob[start + 4:start + 4 + size]
    if len(payload) != size:
        raise ValueError("KTX file is truncated")
    return Image(width, height, gl_internal_format, gl_base_internal_format, payload)

package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;
import java.io.InputStream;

/**
 * The header of one sheet's encoded container, read and checked before anything is uploaded.
 *
 * <p>Roadmap R8.1's loading half. {@link TexturePayloadPolicy} answers <em>which</em> file to read; this answers
 * whether the file beside the sheet is the container it claims to be and whether this device may be handed it.
 * The header is sixty-four bytes of magic and thirteen little-endian fields, written by
 * {@code tools/texture/ktx.py}, and the checks here are what turn a wrong or incomplete container into a logged
 * fallback to the reviewed PNG instead of a wrongly sampled frame:
 *
 * <ul>
 *   <li>it is a KTX v1 file and says it is little-endian;
 *   <li>it holds compressed data with no key/value data, one face and at least one mip level, because those are
 *       the only shapes {@code KTXTextureData} will stream to a driver;
 *   <li>the length of its first level is positive and the header says how long it is;
 *   <li>its dimensions are the ones the caller expected: a container that is not the page's size would upload
 *       happily and then sample the wrong texels.
 * </ul>
 *
 * <p>The payload itself is deliberately not read here. {@code KTXTextureData} streams it straight to
 * {@code glCompressedTexImage2D}, and reading a sheet of several megabytes a second time just to look at it would
 * be work for nothing.
 */
public final class KtxContainer {
    /** KTX v1 identification, "«KTX 11»" as the specification writes it. */
    private static final byte[] IDENTIFIER = {
        (byte) 0xAB, 'K', 'T', 'X', ' ', '1', '1', (byte) 0xBB, '\r', '\n', (byte) 0x1A, '\n',
    };

    /** The little-endian marker, read as a little-endian integer. */
    private static final int ENDIANNESS = 0x04030201;

    /** The header alone, before the four-byte length of the first level's payload. */
    public static final int HEADER_BYTES = 64;

    /** The header plus that length: how much has to be read to know what the file holds. */
    public static final int PREFIX_BYTES = HEADER_BYTES + 4;

    private final int glInternalFormat;
    private final int glBaseInternalFormat;
    private final int width;
    private final int height;
    private final int mipLevels;
    private final int payloadLength;

    private KtxContainer(
        int glInternalFormat, int glBaseInternalFormat, int width, int height, int mipLevels, int payloadLength
    ) {
        this.glInternalFormat = glInternalFormat;
        this.glBaseInternalFormat = glBaseInternalFormat;
        this.width = width;
        this.height = height;
        this.mipLevels = mipLevels;
        this.payloadLength = payloadLength;
    }

    /** Reads and checks the header of a container, without reading its payload. */
    public static KtxContainer read(FileHandle file) {
        byte[] prefix = new byte[PREFIX_BYTES];
        try (InputStream stream = file.read()) {
            int filled = 0;
            while (filled < prefix.length) {
                int read = stream.read(prefix, filled, prefix.length - filled);
                if (read < 0) {
                    throw new IllegalArgumentException(
                        file.name() + " is shorter than a KTX v1 header: " + filled + " of " + prefix.length
                            + " bytes"
                    );
                }
                filled += read;
            }
        } catch (IOException failure) {
            throw new IllegalArgumentException("could not read " + file.path() + ": " + failure, failure);
        }
        return parse(prefix);
    }

    /**
     * Checks a header that has already been read: at least the first sixty-eight bytes of a container.
     *
     * @throws IllegalArgumentException with a plain-language reason when the bytes are not a container this
     *     loader may upload, which the caller reports as a fallback rather than a crash
     */
    public static KtxContainer parse(byte[] prefix) {
        if (prefix.length < PREFIX_BYTES) {
            throw new IllegalArgumentException(
                "only " + prefix.length + " bytes, and a KTX v1 header with its first level length is "
                    + PREFIX_BYTES
            );
        }
        for (int index = 0; index < IDENTIFIER.length; index++) {
            if (prefix[index] != IDENTIFIER[index]) {
                throw new IllegalArgumentException("not a KTX v1 file: the identification bytes do not match");
            }
        }
        if (littleEndian(prefix, 12) != ENDIANNESS) {
            throw new IllegalArgumentException("this container's header is not little-endian");
        }
        int glType = littleEndian(prefix, 16);
        int glTypeSize = littleEndian(prefix, 20);
        int glFormat = littleEndian(prefix, 24);
        int glInternalFormat = littleEndian(prefix, 28);
        int glBaseInternalFormat = littleEndian(prefix, 32);
        int width = littleEndian(prefix, 36);
        int height = littleEndian(prefix, 40);
        int pixelDepth = littleEndian(prefix, 44);
        int arrayElements = littleEndian(prefix, 48);
        int faces = littleEndian(prefix, 52);
        int mipLevels = littleEndian(prefix, 56);
        int keyValueBytes = littleEndian(prefix, 60);
        int payloadLength = littleEndian(prefix, 64);
        if (glType != 0 || glTypeSize != 1 || glFormat != 0) {
            throw new IllegalArgumentException(
                "this container holds uncompressed texels, which the compressed upload path cannot take"
            );
        }
        if (keyValueBytes != 0) {
            throw new IllegalArgumentException("this container carries " + keyValueBytes + " bytes of key/value data");
        }
        if (pixelDepth != 0 || arrayElements != 0) {
            throw new IllegalArgumentException("this container is not a two-dimensional, non-array texture");
        }
        if (faces != 1) {
            throw new IllegalArgumentException("this container has " + faces + " faces");
        }
        if (mipLevels < 1) {
            throw new IllegalArgumentException("this container has no mip levels");
        }
        if (payloadLength < 1) {
            throw new IllegalArgumentException("this container's first level is empty");
        }
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("this container is " + width + "x" + height);
        }
        return new KtxContainer(glInternalFormat, glBaseInternalFormat, width, height, mipLevels, payloadLength);
    }

    /** The GL internal format the driver will be asked for. */
    public int glInternalFormat() {
        return glInternalFormat;
    }

    /** The base format the container declares, which for a mask sheet is RGBA and for an opaque one is RGB. */
    public int glBaseInternalFormat() {
        return glBaseInternalFormat;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int mipLevels() {
        return mipLevels;
    }

    /** How many bytes the first level's payload takes. */
    public int payloadLength() {
        return payloadLength;
    }

    /** Whether this container is exactly the texels the caller was expecting to upload. */
    public boolean agreesWith(int expectedWidth, int expectedHeight) {
        return width == expectedWidth && height == expectedHeight;
    }

    /** One line for a log or a report, with the format written the way GL writes it. */
    public String describe() {
        return "0x" + Integer.toHexString(glInternalFormat) + " " + width + "x" + height + ", " + payloadLength
            + " bytes" + (mipLevels > 1 ? ", " + mipLevels + " levels" : "");
    }

    private static int littleEndian(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF)
            | ((bytes[offset + 1] & 0xFF) << 8)
            | ((bytes[offset + 2] & 0xFF) << 16)
            | ((bytes[offset + 3] & 0xFF) << 24);
    }
}

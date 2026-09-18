package com.amirrezahadipoor.herodefense.render;

import java.util.List;

/**
 * The GPU formats this game could ship a texture page in (roadmap R8.1).
 *
 * <p>The audit's memory finding was arithmetic: 1,277 MiB of decoded catalog, because every page is decoded to
 * RGBA8888 on the device. Compression is the first half of the answer and mipmaps the second, and they pull in
 * opposite directions: compression divides the bytes, a mip chain multiplies the base level by 4/3.
 *
 * <p>{@code bytesPerPixel} is the plain arithmetic for each format rather than a marketing number. ETC1 is half
 * a byte per pixel and has no alpha at all, which is why {@link #sheetCount(boolean)} exists: an alpha page in
 * ETC1 costs a companion sheet, and a plan that forgets that is a plan that halves the expected saving without
 * noticing. ETC2 RGBA8 is one byte per pixel with alpha. ASTC 6x6 is 16 bytes per 36 pixels. RGBA8888 is the
 * four bytes the device decodes to today.
 */
public enum TextureFormat {
    /** What ships today: the decoded form, four bytes per pixel, alpha included. */
    RGBA8888(4.0, true),
    /** ETC1: half a byte per pixel, no alpha, GLES 2.0 and up. Alpha needs a companion sheet. */
    ETC1(0.5, false),
    /** ETC2 RGBA8: one byte per pixel with alpha, part of GLES 3.0. The safe compressed target. */
    ETC2_RGBA8(1.0, true),
    /** ASTC 6x6: 16 bytes per 36 pixels, alpha included, GLES 3.0 with the extension. */
    ASTC_6X6(16.0 / 36.0, true);

    private final double bytesPerPixel;
    private final boolean alpha;

    TextureFormat(double bytesPerPixel, boolean alpha) {
        this.bytesPerPixel = bytesPerPixel;
        this.alpha = alpha;
    }

    public double bytesPerPixel() {
        return bytesPerPixel;
    }

    /** Whether the format carries an alpha channel; without one, alpha needs its own sheet. */
    public boolean carriesAlpha() {
        return alpha;
    }

    /** Sheets needed for a page that has alpha: a format without one needs a companion. */
    public int sheetCount(boolean pageHasAlpha) {
        return pageHasAlpha && !alpha ? 2 : 1;
    }

    /**
     * Bytes for a page of this size, mip chain included when asked for. The chain adds a third (1 + 1/4 + 1/16
     * ... is 4/3), and it is asked for per page because an icon drawn at 1:1 does not need one.
     */
    public long decodedBytes(int width, int height, boolean mipmapped) {
        long base = (long) (width * (double) height * bytesPerPixel + 0.5);
        return mipmapped ? base * 4L / 3L : base;
    }

    /** Every format, best compression first: the order a device walks down to find what it supports. */
    public static List<TextureFormat> preferredOrder() {
        return java.util.List.of(ASTC_6X6, ETC2_RGBA8, ETC1, RGBA8888);
    }

    /** The best format a device supports, with RGBA8888 as the floor nothing falls below. */
    public static TextureFormat bestFor(List<TextureFormat> supported) {
        for (TextureFormat format : preferredOrder()) {
            if (supported.contains(format)) return format;
        }
        return RGBA8888;
    }
}

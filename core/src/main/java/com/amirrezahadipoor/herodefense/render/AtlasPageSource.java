package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.files.FileHandle;

/**
 * What the loader should read for one sheet, and why: the encoded container when there is one this device can
 * decode, the reviewed PNG otherwise.
 *
 * <p>Roadmap R8.1's decision, in one place and testable without a GL context. {@link TexturePayloadPolicy} holds
 * the same decision as a function of a manifest; this holds it as a function of the build, which is what a running
 * device can actually see: a container is used exactly when the file exists beside the sheet, its own header says
 * it is a container this device's GL version may decode, and its dimensions are the sheet's. The third check
 * matters because a container from an older render of the same sheet would upload without complaint and sample
 * the wrong texels -- an error that shows up as art moving, not as a GL error.
 *
 * <p>Every answer carries the reason it was made, so "the game is still reading PNGs" has an answer that can be
 * printed rather than guessed, and the fallback is never silent.
 */
public final class AtlasPageSource {
    /** Where a sheet's container lives: beside the PNG, same stem, under {@code compressed/etc2/}. */
    private static final String CONTAINER_DIRECTORY = "compressed/etc2/";

    /** One decision: the file to read, whether it is compressed, in which format, and why. */
    public record Payload(FileHandle file, boolean compressed, int glInternalFormat, String reason) {
        /** One line for a log: the choice, the path, and the reason behind it. */
        public String describe() {
            return (compressed ? "compressed " : "png ") + file.path() + " -- " + reason;
        }
    }

    private final DeviceTextureSupport support;

    /** The source a device that proved nothing gets: the PNG, always. */
    public static AtlasPageSource pngOnly() {
        return new AtlasPageSource(DeviceTextureSupport.NONE);
    }

    /**
     * @param support what the device reported, from {@link DeviceTextureSupport#from}
     */
    public AtlasPageSource(DeviceTextureSupport support) {
        this.support = support == null ? DeviceTextureSupport.NONE : support;
    }

    /** The source for a sheet whose size the caller does not know: the container is taken at its word. */
    public Payload forPage(FileHandle page) {
        return forPage(page, 0, 0);
    }

    /**
     * The source for one sheet.
     *
     * @param page the PNG the game would otherwise read
     * @param pageWidth the width the atlas pack file declares, or zero when the caller has no page to compare
     *     against
     * @param pageHeight likewise
     */
    public Payload forPage(FileHandle page, int pageWidth, int pageHeight) {
        FileHandle container = containerBeside(page);
        if (!container.exists()) {
            return png(page, "no encoded container beside it");
        }
        KtxContainer header;
        try {
            header = KtxContainer.read(container);
        } catch (IllegalArgumentException refusal) {
            return png(page, "the container beside it was refused: " + refusal.getMessage());
        }
        if (!support.canDecode(header.glInternalFormat())) {
            return png(page, "this device cannot decode " + "0x" + Integer.toHexString(header.glInternalFormat()));
        }
        if (pageWidth > 0 && pageHeight > 0 && !header.agreesWith(pageWidth, pageHeight)) {
            return png(
                page,
                "the container is " + header.width() + "x" + header.height() + " and the page is " + pageWidth
                    + "x" + pageHeight
            );
        }
        return new Payload(container, true, header.glInternalFormat(), "ETC2 " + header.describe());
    }

    /** The container handle for a sheet: the PNG's directory, {@code compressed/etc2/}, the same stem. */
    public static FileHandle containerBeside(FileHandle page) {
        String name = page.name();
        int dot = name.lastIndexOf('.');
        String stem = dot < 0 ? name : name.substring(0, dot);
        return page.parent().child(CONTAINER_DIRECTORY + stem + ".ktx");
    }

    private static Payload png(FileHandle page, String reason) {
        return new Payload(page, false, 0, reason);
    }
}

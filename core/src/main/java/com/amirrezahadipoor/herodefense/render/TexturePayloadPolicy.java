package com.amirrezahadipoor.herodefense.render;

import java.util.Map;
import java.util.Set;

/**
 * Which file the loader should read for a sheet: the encoded container when the device can decode it and the
 * container exists, the PNG otherwise.
 *
 * <p>Roadmap R8.1's payload half. The decision is deliberately a pure function over three facts -- what the
 * device supports, which sheets have a container beside them, and what format each container holds -- so it can
 * be tested without a GL context and so the answer can be logged exactly once per sheet. The PNG is not a
 * degraded mode here: it is the reviewed payload, and it is what ships today, because the encoder's measured
 * quality on the shipped sheets has not cleared the bar the tool sets (see
 * {@code tools/texture/encode_textures.py}). The policy exists so that the day a payload passes, the loader
 * already knows what to do with it, per device, without a second encoding decision at runtime.
 */
public final class TexturePayloadPolicy {
    /** GLES 3.0's ETC2 opaque format, and the format id the encoder writes into its container headers. */
    public static final int ETC2_RGB8 = 0x9275;
    /** GLES 3.0's ETC2 format with a one-bit alpha: the right one for a sheet whose alpha is a mask. */
    public static final int ETC2_RGB8_PUNCHTHROUGH_ALPHA1 = 0x9276;

    private final DeviceTextureSupport support;
    private final Map<String, Integer> containerFormats;

    /**
     * @param support what the device reported, from {@link DeviceTextureSupport#from}
     * @param containerFormats sheet path (the PNG the game would otherwise read) to the GL internal format of
     *     the container sitting beside it, for the containers that are actually in the build
     */
    public TexturePayloadPolicy(DeviceTextureSupport support, Map<String, Integer> containerFormats) {
        this.support = support;
        this.containerFormats = Map.copyOf(containerFormats);
    }

    /** The path to read. Never null, never a container this device cannot decode. */
    public String sourceFor(String pngPath) {
        Integer format = containerFormats.get(pngPath);
        if (format == null || !support.canDecode(format)) {
            return pngPath;
        }
        return containerPath(pngPath);
    }

    /** Whether the source chosen for this sheet is an encoded container rather than the PNG. */
    public boolean isCompressed(String pngPath) {
        return !sourceFor(pngPath).equals(pngPath);
    }

    /** The sheets this policy would load compressed, in the order the map iterates. */
    public Set<String> compressedSheets() {
        java.util.Set<String> compressed = new java.util.LinkedHashSet<>();
        for (String path : containerFormats.keySet()) {
            if (isCompressed(path)) {
                compressed.add(path);
            }
        }
        return java.util.Collections.unmodifiableSet(compressed);
    }

    /** Where a sheet's container lives: beside the PNG, same stem, `.ktx`, under `compressed/etc2/`. */
    public static String containerPath(String pngPath) {
        int slash = pngPath.lastIndexOf('/');
        String directory = slash < 0 ? "" : pngPath.substring(0, slash + 1);
        String file = slash < 0 ? pngPath : pngPath.substring(slash + 1);
        int dot = file.lastIndexOf('.');
        String stem = dot < 0 ? file : file.substring(0, dot);
        return directory + "compressed/etc2/" + stem + ".ktx";
    }
}

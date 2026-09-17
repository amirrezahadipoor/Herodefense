package com.amirrezahadipoor.herodefense.render;

/**
 * What the running device can actually decode, decided from the two strings libGDX exposes.
 *
 * <p>Roadmap R8.1 measured what the catalog would cost in each compressed format; this is the other half of the
 * question, which is whether the device in front of the player can read one. ETC2 is the useful case: it is core
 * in OpenGL ES 3.0, which every device this game targets supports (the minimum is API 23), while the desktop and
 * emulator paths reach it through OpenGL 4.3. ASTC is deliberately *not* claimed here: it is a GLES 3.2 or
 * extension feature, the projection says the extra saving over ETC2 is real, and no shipped payload uses it yet,
 * so this enum stays honest about what an upload could succeed at rather than optimistic about what a device
 * might offer.
 */
public enum DeviceTextureSupport {
    /** No compressed format was proven: the loader reads the PNG. */
    NONE,
    /** ETC2 opaque and punchthrough formats, both one byte per two pixels in the projection's terms. */
    ETC2;

    /**
     * Reads the device's answer out of its GL version and extension list.
     *
     * @param glVersion GL_VERSION as the context reports it; null is treated as "unknown".
     * @param extensions GL_EXTENSIONS, space separated; null is treated as "none".
     */
    public static DeviceTextureSupport from(String glVersion, String extensions) {
        if (glVersion == null) {
            return NONE;
        }
        if (glVersion.contains("OpenGL ES 3") || glVersion.contains("OpenGL ES 4")) {
            return ETC2;
        }
        if (glVersion.startsWith("OpenGL") && versionAtLeast(glVersion, 4, 3)) {
            return ETC2;
        }
        return NONE;
    }

    /** Whether this device may be handed a payload with the given GL internal format. */
    public boolean canDecode(int glInternalFormat) {
        if (this == NONE) {
            return false;
        }
        return glInternalFormat == TexturePayloadPolicy.ETC2_RGB8
            || glInternalFormat == TexturePayloadPolicy.ETC2_RGB8_PUNCHTHROUGH_ALPHA1;
    }

    private static boolean versionAtLeast(String glVersion, int major, int minor) {
        String[] parts = glVersion.split("\\s+");
        for (String part : parts) {
            if (!part.isEmpty() && Character.isDigit(part.charAt(0))) {
                String[] numbers = part.split("\\.");
                try {
                    int foundMajor = Integer.parseInt(numbers[0]);
                    int foundMinor = numbers.length > 1 ? Integer.parseInt(numbers[1]) : 0;
                    return foundMajor > major || (foundMajor == major && foundMinor >= minor);
                } catch (NumberFormatException notAVersion) {
                    return false;
                }
            }
        }
        return false;
    }
}

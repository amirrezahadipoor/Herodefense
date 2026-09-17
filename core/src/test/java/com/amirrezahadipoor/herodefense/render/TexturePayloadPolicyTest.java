package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Roadmap R8.1: the per-device choice, asserted rather than described. */
final class TexturePayloadPolicyTest {
    private static final String SHEET = "generated/sprites/rootling.png";
    private static final Map<String, Integer> WITH_CONTAINER =
        Map.of(SHEET, TexturePayloadPolicy.ETC2_RGB8_PUNCHTHROUGH_ALPHA1);

    @Test
    void aDeviceThatDecodesEtc2GetsTheContainer() {
        TexturePayloadPolicy policy = new TexturePayloadPolicy(DeviceTextureSupport.ETC2, WITH_CONTAINER);
        assertEquals("generated/sprites/compressed/etc2/rootling.ktx", policy.sourceFor(SHEET));
        assertTrue(policy.isCompressed(SHEET));
        assertTrue(policy.compressedSheets().contains(SHEET));
    }

    @Test
    void aDeviceWithoutEtc2FallsBackToTheReviewedPng() {
        TexturePayloadPolicy policy = new TexturePayloadPolicy(DeviceTextureSupport.NONE, WITH_CONTAINER);
        assertEquals(SHEET, policy.sourceFor(SHEET));
        assertFalse(policy.isCompressed(SHEET));
        assertTrue(policy.compressedSheets().isEmpty());
    }

    @Test
    void aSheetWithoutAContainerIsAlwaysThePng() {
        TexturePayloadPolicy policy = new TexturePayloadPolicy(DeviceTextureSupport.ETC2, Map.of());
        assertEquals(SHEET, policy.sourceFor(SHEET));
        assertFalse(policy.isCompressed(SHEET));
    }

    @Test
    void aDeviceThatCannotReadTheFormatKeepsThePng() {
        Map<String, Integer> astcOnly = Map.of(SHEET, 0x93B0);
        TexturePayloadPolicy policy = new TexturePayloadPolicy(DeviceTextureSupport.ETC2, astcOnly);
        assertEquals(SHEET, policy.sourceFor(SHEET));
        assertFalse(policy.isCompressed(SHEET), "a format the device cannot decode must not be selected");
    }

    @Test
    void theDeviceSupportIsReadFromWhatTheContextReports() {
        assertEquals(DeviceTextureSupport.ETC2, DeviceTextureSupport.from("OpenGL ES 3.2 v1.r0", ""));
        assertEquals(DeviceTextureSupport.ETC2, DeviceTextureSupport.from("OpenGL ES 3.0", null));
        assertEquals(DeviceTextureSupport.ETC2, DeviceTextureSupport.from("OpenGL 4.6 (Core Profile)", ""));
        assertEquals(DeviceTextureSupport.NONE, DeviceTextureSupport.from("OpenGL 2.1", ""));
        assertEquals(DeviceTextureSupport.NONE, DeviceTextureSupport.from("OpenGL ES 2.0", ""));
        assertEquals(DeviceTextureSupport.NONE, DeviceTextureSupport.from(null, null));
        assertEquals(DeviceTextureSupport.NONE, DeviceTextureSupport.from("WebGL 2.0", ""));
    }

    @Test
    void theContainerPathKeepsTheDirectoryAndChangesTheExtension() {
        assertEquals("compressed/etc2/hero.ktx", TexturePayloadPolicy.containerPath("hero.png"));
        assertEquals(
            "generated/ui/compressed/etc2/frame.ktx",
            TexturePayloadPolicy.containerPath("generated/ui/frame.png")
        );
    }
}

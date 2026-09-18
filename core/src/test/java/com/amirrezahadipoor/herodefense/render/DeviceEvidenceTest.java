package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * I2: Device evidence is headless x86 emulator — now multi-profile + real-device notes.
 */
final class DeviceEvidenceTest {

    @Test
    void deviceEvidenceDocExistsAndMentionsI2() throws IOException {
        Path doc = Path.of("docs/device_evidence/DEVICE_EVIDENCE.md");
        assertTrue(Files.exists(doc), "DEVICE_EVIDENCE.md must exist for I2");
        String content = Files.readString(doc);
        assertTrue(content.contains("I2"), "Must mention I2");
        assertTrue(content.contains("pixel_3a") || content.contains("multi-profile"), "Must mention multi-profile");
        assertTrue(content.contains("Samsung") || content.contains("Pixel") || content.contains("real"), "Must mention real device");
        assertTrue(content.contains("getprop") || content.contains("device-info"), "Must mention device info capture");
        assertTrue(content.contains("Limitation") || content.contains("x86"), "Must document limitations honestly");
    }

    @Test
    void deviceEvidenceWorkflowExists() throws IOException {
        Path workflow = Path.of(".github/workflows/device-evidence.yml");
        assertTrue(Files.exists(workflow), "device-evidence.yml must exist");
        String content = Files.readString(workflow);
        assertTrue(content.contains("pixel_3a"), "Must test pixel_3a");
        assertTrue(content.contains("pixel_7") || content.contains("pixel_tablet"), "Must test additional profiles");
        assertTrue(content.contains("DeviceInfoCaptureTest"), "Must run DeviceInfoCaptureTest");
        assertTrue(content.contains("getprop"), "Must capture getprop");
    }

    @Test
    void humanReviewWorkflowNowCapturesDeviceInfo() throws IOException {
        Path workflow = Path.of(".github/workflows/human-review.yml");
        assertTrue(Files.exists(workflow));
        String content = Files.readString(workflow);
        assertTrue(content.contains("device-info") || content.contains("getprop"), "human-review.yml must capture device info for I2");
    }

    @Test
    void deviceInfoCaptureTestExists() throws IOException {
        Path test = Path.of("android/src/androidTest/java/com/amirrezahadipoor/herodefense/android/DeviceInfoCaptureTest.java");
        assertTrue(Files.exists(test), "DeviceInfoCaptureTest must exist");
        String content = Files.readString(test);
        assertTrue(content.contains("Build.MODEL"), "Must capture Build.MODEL");
        assertTrue(content.contains("DisplayMetrics"), "Must capture DisplayMetrics");
    }
}

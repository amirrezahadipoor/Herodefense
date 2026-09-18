package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The source decision, exercised on real files and without a GL context.
 *
 * <p>Each case is one of the ways a sheet can be loaded, and each asserts the *reason* as well as the answer:
 * "the game read the PNG" is only an acceptable answer when the reason says why, which is what makes this
 * decision debuggable on a device nobody can attach a debugger to.
 */
class AtlasPageSourceTest {
    private static final Path REPOSITORY = Path.of("..").normalize();
    private static final Path FIXTURE = REPOSITORY.resolve("android/src/androidTest/assets/etc2/rootling-64.ktx");
    private static final int PUNCHTHROUGH = TexturePayloadPolicy.ETC2_RGB8_PUNCHTHROUGH_ALPHA1;

    /** A container beside a sheet, at the path the policy and the loader both use. */
    private static FileHandle pageWithContainer(Path directory) throws IOException {
        Path sprites = directory.resolve("sprites");
        Files.createDirectories(sprites.resolve("compressed/etc2"));
        Files.write(sprites.resolve("hero.png"), new byte[] {1, 2, 3});
        Files.copy(FIXTURE, sprites.resolve("compressed/etc2/hero.ktx"));
        return new FileHandle(sprites.resolve("hero.png").toFile());
    }

    private static FileHandle pageOnly(Path directory) throws IOException {
        Path sprites = directory.resolve("sprites");
        Files.createDirectories(sprites);
        Files.write(sprites.resolve("hero.png"), new byte[] {1, 2, 3});
        return new FileHandle(sprites.resolve("hero.png").toFile());
    }

    @Test
    void a_device_that_decodes_etc2_reads_the_container(@TempDir Path directory) throws IOException {
        FileHandle page = pageWithContainer(directory);
        AtlasPageSource.Payload payload =
            new AtlasPageSource(DeviceTextureSupport.ETC2).forPage(page, 64, 64);
        assertTrue(payload.compressed(), payload.reason());
        assertEquals(PUNCHTHROUGH, payload.glInternalFormat(), "the container's own header names the format");
        assertTrue("hero.ktx".equals(payload.file().name()), payload.file().path());
        assertTrue(payload.reason().contains("64x64"), payload.reason());
    }

    @Test
    void the_container_the_loader_reads_is_the_one_the_policy_names(@TempDir Path directory) throws IOException {
        FileHandle page = pageWithContainer(directory);
        String byPolicy = TexturePayloadPolicy.containerPath(page.path());
        String byLoader = AtlasPageSource.containerBeside(page).path();
        assertEquals(byPolicy, byLoader, "the projected path and the loaded path are one decision, not two");
    }

    @Test
    void a_device_that_proved_nothing_reads_the_png(@TempDir Path directory) throws IOException {
        FileHandle page = pageWithContainer(directory);
        AtlasPageSource.Payload payload = AtlasPageSource.pngOnly().forPage(page, 64, 64);
        assertFalse(payload.compressed(), "no evidence of ETC2 support means the reviewed PNG");
        assertEquals(page.path(), payload.file().path());
        assertTrue(payload.reason().contains("cannot decode"), payload.reason());
    }

    @Test
    void a_format_the_device_cannot_decode_falls_back(@TempDir Path directory) throws IOException {
        FileHandle page = pageWithContainer(directory);
        Path container = directory.resolve("sprites/compressed/etc2/hero.ktx");
        byte[] bytes = Files.readAllBytes(container);
        bytes[28] = (byte) 0xB0;                       // 0x93B0: ASTC, which no payload here claims
        bytes[29] = (byte) 0x93;
        bytes[30] = 0x00;
        bytes[31] = 0x00;
        Files.write(container, bytes);
        AtlasPageSource.Payload payload = new AtlasPageSource(DeviceTextureSupport.ETC2).forPage(page, 64, 64);
        assertFalse(payload.compressed(), "a format this device cannot decode is not uploaded");
        assertTrue(payload.reason().contains("0x93b0"), payload.reason());
    }

    @Test
    void no_container_beside_the_sheet_means_the_png(@TempDir Path directory) throws IOException {
        FileHandle page = pageOnly(directory);
        AtlasPageSource.Payload payload = new AtlasPageSource(DeviceTextureSupport.ETC2).forPage(page, 64, 64);
        assertFalse(payload.compressed());
        assertTrue(payload.reason().contains("no encoded container"), payload.reason());
    }

    @Test
    void a_container_of_the_wrong_size_falls_back(@TempDir Path directory) throws IOException {
        FileHandle page = pageWithContainer(directory);
        AtlasPageSource.Payload payload = new AtlasPageSource(DeviceTextureSupport.ETC2).forPage(page, 32, 32);
        assertFalse(payload.compressed(), "a container that is not the page's size would sample the wrong texels");
        assertTrue(payload.reason().contains("64x64") && payload.reason().contains("32x32"), payload.reason());
    }

    @Test
    void a_truncated_container_falls_back(@TempDir Path directory) throws IOException {
        FileHandle page = pageWithContainer(directory);
        Path container = directory.resolve("sprites/compressed/etc2/hero.ktx");
        byte[] bytes = Files.readAllBytes(container);
        Files.write(container, Arrays.copyOf(bytes, 30));
        AtlasPageSource.Payload payload = new AtlasPageSource(DeviceTextureSupport.ETC2).forPage(page, 64, 64);
        assertFalse(payload.compressed());
        assertTrue(payload.reason().contains("refused"), payload.reason());
    }

    @Test
    void a_sheet_with_no_page_to_compare_against_takes_the_container_at_its_word(@TempDir Path directory)
        throws IOException {
        FileHandle page = pageWithContainer(directory);
        AtlasPageSource.Payload payload = new AtlasPageSource(DeviceTextureSupport.ETC2).forPage(page);
        assertTrue(payload.compressed(), "a backdrop or an icon has no pack file to disagree with");
    }

    @Test
    void the_projection_and_the_loader_agree_on_which_sheets_are_compressed(@TempDir Path directory)
        throws IOException {
        FileHandle page = pageWithContainer(directory);
        TexturePayloadPolicy policy = new TexturePayloadPolicy(
            DeviceTextureSupport.ETC2, Map.of(page.path(), PUNCHTHROUGH)
        );
        assertEquals(1, policy.compressedSheets().size(), "the policy counts this sheet as compressed");
        assertTrue(new AtlasPageSource(DeviceTextureSupport.ETC2).forPage(page, 64, 64).compressed(),
            "and the loader agrees with it");
    }

    @Test
    void an_empty_page_name_is_still_a_path_and_not_a_crash(@TempDir Path directory) {
        FileHandle page = new FileHandle(directory.resolve("nothing.png").toFile());
        AtlasPageSource.Payload payload = new AtlasPageSource(DeviceTextureSupport.ETC2).forPage(page, 64, 64);
        assertFalse(payload.compressed(), "a sheet that is not there falls back rather than throwing");
    }
}

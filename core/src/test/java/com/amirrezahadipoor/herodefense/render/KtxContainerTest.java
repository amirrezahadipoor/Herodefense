package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The container checks, held to the container the device test uploads.
 *
 * <p>The bytes are not a fixture written for this test: they are the tracked file the instrumentation test hands
 * to a driver, and the test asserts the header against the encoder's own record beside it. So "the runtime parses
 * the container" and "the container the encoder writes" cannot drift apart without one of the two going red.
 */
class KtxContainerTest {
    private static final Path REPOSITORY = Path.of("..").normalize();
    private static final Path FIXTURE = REPOSITORY.resolve("android/src/androidTest/assets/etc2");

    /** ETC2_RGB8_PUNCHTHROUGH_ALPHA1, the format the fixture's header names. */
    private static final int PUNCHTHROUGH = 0x9276;

    /** GL_RGBA, the base format of a mask sheet. */
    private static final int RGBA = 0x1908;

    private static byte[] fixture() throws IOException {
        return Files.readAllBytes(FIXTURE.resolve("rootling-64.ktx"));
    }

    /** The header and first level length of a good container, copied so a test can break one field of it. */
    private static byte[] prefix(byte[] container) {
        return Arrays.copyOf(container, KtxContainer.PREFIX_BYTES);
    }

    private static void poke(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) (value & 0xFF);
        bytes[offset + 1] = (byte) ((value >> 8) & 0xFF);
        bytes[offset + 2] = (byte) ((value >> 16) & 0xFF);
        bytes[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    @Test
    void the_container_the_device_uploads_parses_as_what_the_encoder_records() throws IOException {
        byte[] container = fixture();
        JsonValue record = new JsonReader().parse(
            Files.readString(FIXTURE.resolve("rootling-64.json"))
        );
        KtxContainer header = KtxContainer.parse(prefix(container));
        assertEquals(64, header.width(), "the fixture is a 64x64 crop");
        assertEquals(64, header.height(), "the fixture is a 64x64 crop");
        assertEquals(record.getInt("glInternalFormat"), header.glInternalFormat(),
            "the header's format is the one the encoder wrote down beside it");
        assertEquals(PUNCHTHROUGH, header.glInternalFormat(), "and it is the punchthrough format");
        assertEquals(RGBA, header.glBaseInternalFormat(), "a mask sheet's base format is RGBA");
        assertEquals(1, header.mipLevels(), "the encoder writes one level");
        assertEquals(64 * 64 / 16 * 8, header.payloadLength(), "eight bytes for every four-by-four block");
        assertTrue(header.agreesWith(64, 64), "and it agrees with the page it belongs to");
        assertFalse(header.agreesWith(65, 64), "a container that is not the page's size is not the page's");
    }

    @Test
    void a_prefix_shorter_than_a_header_is_refused() throws IOException {
        byte[] container = fixture();
        IllegalArgumentException refusal = assertThrows(
            IllegalArgumentException.class, () -> KtxContainer.parse(Arrays.copyOf(container, 40))
        );
        assertTrue(refusal.getMessage().contains("KTX v1 header"), refusal.getMessage());
    }

    @Test
    void a_file_that_is_not_a_container_is_refused(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("sheet.ktx");
        Files.write(file, new byte[128]);
        IllegalArgumentException refusal = assertThrows(
            IllegalArgumentException.class, () -> KtxContainer.read(new FileHandle(file.toFile()))
        );
        assertTrue(refusal.getMessage().contains("identification"), refusal.getMessage());
    }

    @Test
    void a_file_shorter_than_its_own_header_is_refused(@TempDir Path directory) throws IOException {
        byte[] container = fixture();
        Path file = directory.resolve("sheet.ktx");
        Files.write(file, Arrays.copyOf(container, 30));
        IllegalArgumentException refusal = assertThrows(
            IllegalArgumentException.class, () -> KtxContainer.read(new FileHandle(file.toFile()))
        );
        assertTrue(refusal.getMessage().contains("shorter than"), refusal.getMessage());
    }

    @Test
    void a_big_endian_container_is_refused() throws IOException {
        byte[] bytes = prefix(fixture());
        poke(bytes, 12, 0x01020304);
        assertThrows(IllegalArgumentException.class, () -> KtxContainer.parse(bytes));
    }

    @Test
    void an_uncompressed_container_is_refused() throws IOException {
        byte[] bytes = prefix(fixture());
        poke(bytes, 16, 0x1401);
        IllegalArgumentException refusal = assertThrows(IllegalArgumentException.class, () -> KtxContainer.parse(bytes));
        assertTrue(refusal.getMessage().contains("uncompressed"), refusal.getMessage());
    }

    @Test
    void key_value_data_is_refused() throws IOException {
        byte[] bytes = prefix(fixture());
        poke(bytes, 60, 12);
        assertThrows(IllegalArgumentException.class, () -> KtxContainer.parse(bytes));
    }

    @Test
    void a_cube_or_an_array_or_a_depth_is_refused() throws IOException {
        byte[] faces = prefix(fixture());
        poke(faces, 52, 6);
        assertThrows(IllegalArgumentException.class, () -> KtxContainer.parse(faces));
        byte[] array = prefix(fixture());
        poke(array, 48, 2);
        assertThrows(IllegalArgumentException.class, () -> KtxContainer.parse(array));
        byte[] depth = prefix(fixture());
        poke(depth, 44, 3);
        assertThrows(IllegalArgumentException.class, () -> KtxContainer.parse(depth));
    }

    @Test
    void a_container_with_no_levels_or_an_empty_one_is_refused() throws IOException {
        byte[] levels = prefix(fixture());
        poke(levels, 56, 0);
        assertThrows(IllegalArgumentException.class, () -> KtxContainer.parse(levels));
        byte[] empty = prefix(fixture());
        poke(empty, 64, 0);
        assertThrows(IllegalArgumentException.class, () -> KtxContainer.parse(empty));
    }

    @Test
    void a_zero_sized_container_is_refused() throws IOException {
        byte[] bytes = prefix(fixture());
        poke(bytes, 36, 0);
        poke(bytes, 40, 0);
        IllegalArgumentException refusal = assertThrows(IllegalArgumentException.class, () -> KtxContainer.parse(bytes));
        assertTrue(refusal.getMessage().contains("0x0"), refusal.getMessage());
    }
}

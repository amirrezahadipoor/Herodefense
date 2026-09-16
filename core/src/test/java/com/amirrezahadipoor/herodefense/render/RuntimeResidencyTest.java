package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The manifest's residency numbers must match arithmetic over the sheets that ship, and the live combat
 * set must stay inside its documented budget.
 *
 * <p>{@link RuntimeResidency} is the arithmetic; this test is the gate. It also proves the gate can fail
 * (negative control), because a budget that has never rejected anything is decoration.
 *
 * <p>See {@code docs/ROADMAP_TO_1000.md} R8.2 and R1.9.
 */
class RuntimeResidencyTest {

    private static final Path REPOSITORY = Path.of("..").normalize();
    private static final Path GENERATED = REPOSITORY.resolve("android/assets/generated").normalize();
    private static final Path MANIFEST = GENERATED.resolve("asset_manifest.json");

    @Test
    void catalogBytesMatchTheManifestAndItsBudget() throws IOException {
        JsonValue manifest = manifest();
        long computed = RuntimeResidency.catalogBytes(manifest, icons());
        long declared = manifest.getLong("decodedBytes");
        assertEquals(declared, computed,
            "decodedBytes in the manifest must equal the arithmetic over the sheets that ship");
        RuntimeResidency.requireWithin(
            "asset catalog", computed, manifest.getLong("decodedCatalogBudgetBytes")
        );
    }

    @Test
    void theCombatSetFitsTheCombatBudget() throws IOException {
        JsonValue manifest = manifest();
        long combat = RuntimeResidency.combatBytes(manifest);
        long budget = manifest.getLong("decodedCombatResidencyBudgetBytes");
        assertTrue(budget > 0, "manifest must declare a combat residency budget");
        RuntimeResidency.requireWithin("combat set", combat, budget);
        assertTrue(combat < manifest.getLong("decodedBytes"),
            "the live combat set must be smaller than the whole catalog");
    }

    @Test
    void anOverBudgetSetIsRejected() {
        IllegalStateException failure = assertThrows(IllegalStateException.class,
            () -> RuntimeResidency.requireWithin("synthetic", 200, 100));
        assertTrue(failure.getMessage().contains("above the budget"), failure.getMessage());
        RuntimeResidency.requireWithin("synthetic", 100, 100);
    }

    @Test
    void aGrownSheetIsRejectedByTheSameArithmetic() {
        JsonValue manifest = new JsonReader().parse(
            "{\"assets\":[{\"key\":\"hero\",\"sheets\":[{\"width\":1920,\"height\":768}]}]}");
        long before = RuntimeResidency.combatBytes(manifest);
        JsonValue doubled = new JsonReader().parse(
            "{\"assets\":[{\"key\":\"hero\",\"sheets\":[{\"width\":3840,\"height\":1536}]}]}");
        long after = RuntimeResidency.combatBytes(doubled);
        assertEquals(before * 4, after, "doubling a sheet must quadruple its decoded bytes");
        assertThrows(IllegalStateException.class,
            () -> RuntimeResidency.requireWithin("combat set", after, before));
    }

    private static JsonValue manifest() throws IOException {
        assertTrue(Files.isRegularFile(MANIFEST), "missing manifest " + MANIFEST);
        return new JsonReader().parse(Files.readString(MANIFEST));
    }

    /** Reads icon dimensions straight from the PNG header (IHDR), so no extra dependency is needed. */
    private static RuntimeResidency.IconSizes icons() {
        Map<String, int[]> sizes = new HashMap<>();
        return iconPath -> sizes.computeIfAbsent(iconPath, path -> {
            try {
                byte[] header = new byte[24];
                try (var stream = Files.newInputStream(GENERATED.resolve(path))) {
                    if (stream.read(header) != header.length) {
                        return null;
                    }
                }
                ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.BIG_ENDIAN);
                return new int[] {buffer.getInt(16), buffer.getInt(20)};
            } catch (IOException error) {
                return null;
            }
        });
    }
}

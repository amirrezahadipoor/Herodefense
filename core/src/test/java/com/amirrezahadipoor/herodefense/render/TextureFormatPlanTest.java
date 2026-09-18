package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Roadmap R8.1. The point of these assertions is that the projection is not allowed to drift from the thing it
 * projects: the RGBA8888 row has to reproduce the number the residency gate already enforces, page for page, and
 * the compressed rows have to follow from the format's own arithmetic rather than from a hopeful estimate.
 */
final class TextureFormatPlanTest {

    private static final Path GENERATED = Paths.get("..", "android", "assets", "generated");

    private static JsonValue manifest() throws IOException {
        return new JsonReader().parse(Files.readString(GENERATED.resolve("asset_manifest.json")));
    }

    /** The same PNG-header reader the report uses, so a test and the report cannot disagree. */
    private static RuntimeResidency.IconSizes icons() {
        Map<String, int[]> sizes = new HashMap<>();
        return path -> sizes.computeIfAbsent(path, key -> {
            try {
                byte[] header = new byte[24];
                try (var stream = Files.newInputStream(GENERATED.resolve(key))) {
                    if (stream.read(header) != header.length) return new int[0];
                }
                ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.BIG_ENDIAN);
                return new int[] {buffer.getInt(16), buffer.getInt(20)};
            } catch (IOException error) {
                return new int[0];
            }
        });
    }

    @Test
    void theUncompressedRowReproducesTheNumberTheResidencyGateEnforces() throws Exception {
        JsonValue manifest = manifest();
        long projected = TextureFormatPlan.catalogBytes(manifest, icons(), TextureFormat.RGBA8888);
        assertEquals(
            manifest.getLong("decodedBytes"),
            projected,
            "the projection's baseline must be the catalog the manifest already declares"
        );
        assertEquals(
            RuntimeResidency.catalogBytes(manifest, icons()),
            projected,
            "and it must match the arithmetic the residency gate runs"
        );
    }

    @Test
    void theMipChainCostsAThirdAndIsCountedPerPage() {
        assertEquals(4_000_000L, TextureFormat.RGBA8888.decodedBytes(1000, 1000, false));
        assertEquals(5_333_333L, TextureFormat.RGBA8888.decodedBytes(1000, 1000, true));
        assertEquals(250_000L, TextureFormat.ETC1.decodedBytes(1000, 500, false));
        assertEquals(1_000_000L, TextureFormat.ETC2_RGBA8.decodedBytes(1000, 1000, false));
        assertTrue(
            TextureFormat.ETC2_RGBA8.decodedBytes(1000, 1000, true)
                < TextureFormat.RGBA8888.decodedBytes(1000, 1000, false),
            "ETC2 with a mip chain is still smaller than plain RGBA"
        );
        assertTrue(TextureFormat.ETC2_RGBA8.decodedBytes(1000, 1000, true)
            < TextureFormat.RGBA8888.decodedBytes(1000, 1000, true) / 2, "and less than half of it");
    }

    @Test
    void aFormatWithoutAlphaNeedsACompanionSheetAndThePlanPaysForIt() {
        assertEquals(2, TextureFormat.ETC1.sheetCount(true), "ETC1 has no alpha channel");
        assertEquals(1, TextureFormat.ETC1.sheetCount(false));
        assertEquals(1, TextureFormat.ETC2_RGBA8.sheetCount(true));
        // The plan is charged for the companion sheet, so ETC1 on an alpha page costs the same per pixel as
        // ETC2 while holding a worse image: a plan that forgot the companion would claim a saving that is not
        // there, which is exactly the arithmetic this test exists to hold.
        assertEquals(
            TextureFormat.ETC2_RGBA8.decodedBytes(100, 100, false),
            TextureFormat.ETC1.decodedBytes(100, 100, false) * TextureFormat.ETC1.sheetCount(true),
            "ETC1 with a companion sheet costs what ETC2 costs"
        );
    }

    @Test
    void theCompressedRowsAreSmallerInTheShippedCatalogAndTheCombatSet() throws Exception {
        JsonValue manifest = manifest();
        RuntimeResidency.IconSizes icons = icons();
        Map<TextureFormat, TextureFormatPlan.Row> rows = new java.util.EnumMap<>(TextureFormat.class);
        for (TextureFormatPlan.Row row : TextureFormatPlan.project(manifest, icons)) {
            rows.put(row.format(), row);
        }
        assertNotNull(rows.get(TextureFormat.ETC2_RGBA8));
        long rgba = rows.get(TextureFormat.RGBA8888).catalogBytes();
        long etc2 = rows.get(TextureFormat.ETC2_RGBA8).catalogBytes();
        long astc = rows.get(TextureFormat.ASTC_6X6).catalogBytes();
        long etc1 = rows.get(TextureFormat.ETC1).catalogBytes();
        assertTrue(etc2 < rgba / 3, "ETC2 should be under a third of the decoded catalog, was "
            + etc2 + " of " + rgba);
        assertTrue(astc < etc1, "ASTC 6x6 is smaller than ETC1 plus its companion sheets");
        assertTrue(astc < etc2 / 2, "and much smaller than ETC2");
        assertTrue(rows.get(TextureFormat.ETC2_RGBA8).combatBytes()
            < rows.get(TextureFormat.RGBA8888).combatBytes() / 3,
            "the live combat set compresses by the same arithmetic");
        assertTrue(rows.get(TextureFormat.RGBA8888).catalogWithMipsBytes()
            > rows.get(TextureFormat.RGBA8888).catalogBytes() * 4 / 3 - 1_000,
            "a mip chain on every page costs a third more");
        assertTrue(rows.get(TextureFormat.ETC2_RGBA8).catalogWithMipsBytes()
            < rows.get(TextureFormat.RGBA8888).catalogBytes(),
            "ETC2 with a mip chain is still smaller than the decoded catalog without one");
    }

    @Test
    void aDeviceGetsTheBestFormatItSupportsAndNeverNothing() {
        List<TextureFormat> modern = TextureFormat.preferredOrder();
        assertEquals(TextureFormat.ASTC_6X6, TextureFormat.bestFor(modern));
        assertEquals(
            TextureFormat.ETC2_RGBA8,
            TextureFormat.bestFor(List.of(TextureFormat.ETC2_RGBA8, TextureFormat.ETC1,
                TextureFormat.RGBA8888))
        );
        assertEquals(TextureFormat.ETC1, TextureFormat.bestFor(List.of(TextureFormat.ETC1)));
        assertEquals(
            TextureFormat.RGBA8888,
            TextureFormat.bestFor(List.of(TextureFormat.RGBA8888)),
            "the fallback is the format that ships today, not a missing texture"
        );
        assertEquals(TextureFormat.RGBA8888, TextureFormat.bestFor(List.of()));
    }

    @Test
    void everySheetInTheManifestIsCountedByTheProjection() throws Exception {
        JsonValue manifest = manifest();
        int sheets = 0;
        long declared = 0L;
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            JsonValue assetSheets = asset.get("sheets");
            if (assetSheets == null) continue;
            for (JsonValue sheet = assetSheets.child; sheet != null; sheet = sheet.next) {
                sheets++;
                declared += sheet.getLong("decodedBytes", 0L);
            }
        }
        assertTrue(sheets > 50, "the manifest should describe the whole drawn catalog, found " + sheets);
        assertTrue(declared > 300_000_000L, "and its decoded size is the audit's finding: " + declared);
        // The projection's RGBA row counts these same sheets through their dimensions; the two agree because
        // every sheet in this manifest is 8-bit RGBA, which is exactly what the format's bytes per pixel says.
        assertEquals(
            declared,
            TextureFormatPlan.catalogBytes(manifest, icons(), TextureFormat.RGBA8888)
                - iconBytes(manifest, icons())
        );
    }

    private static long iconBytes(JsonValue manifest, RuntimeResidency.IconSizes icons) {
        long total = 0L;
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            String icon = asset.getString("icon", null);
            if (icon == null) continue;
            int[] size = icons.sizeOf(icon);
            if (size.length == 2) total += TextureFormat.RGBA8888.decodedBytes(size[0], size[1], false);
        }
        return total;
    }
}

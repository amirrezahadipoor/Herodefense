package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.gameplay.ArenaLayout;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The field's cover: which family stands on which field, and whether the art it names is really shipped.
 *
 * <p>Two failures this test exists for. The first is a field that shows a family it has no outcrop of -- the
 * mapping is small enough to be plausible and wrong. The second is worse: a renderer naming a texture the manifest
 * does not carry, which is a crash on the first wave that draws an outcrop and nothing at all in a unit test that
 * only reads the mapping.
 */
class ArenaCoverArtTest {

    private static final Path MANIFEST = Path.of("..", "android", "assets", "generated", "asset_manifest.json")
        .normalize();

    @Test
    void everyLayoutDrawsOnlyFamiliesItHasOutcropsOf() {
        int standing = ArenaEnvironmentRenderer.FAMILY_STANDING_STONE;
        int ruin = ArenaEnvironmentRenderer.FAMILY_RUIN_SLAB;
        int hedge = ArenaEnvironmentRenderer.FAMILY_THORN_HEDGE;
        int boulder = ArenaEnvironmentRenderer.FAMILY_MOSSY_BOULDER;

        assertEquals(standing, ArenaEnvironmentRenderer.coverFamily(ArenaLayout.OPEN_HEARTH, true));
        assertEquals(boulder, ArenaEnvironmentRenderer.coverFamily(ArenaLayout.OPEN_HEARTH, false));
        assertEquals(standing, ArenaEnvironmentRenderer.coverFamily(ArenaLayout.STANDING_STONES, true),
            "the stones field stands its cover up");
        assertEquals(boulder, ArenaEnvironmentRenderer.coverFamily(ArenaLayout.STANDING_STONES, false));
        assertEquals(ruin, ArenaEnvironmentRenderer.coverFamily(ArenaLayout.THORNHEDGE, true));
        assertEquals(hedge, ArenaEnvironmentRenderer.coverFamily(ArenaLayout.THORNHEDGE, false),
            "the hedge field is thorned");
        assertEquals(ruin, ArenaEnvironmentRenderer.coverFamily(ArenaLayout.RUINED_RING, true),
            "the ring's shelter is a broken wall, not a monolith");
        assertEquals(hedge, ArenaEnvironmentRenderer.coverFamily(ArenaLayout.RUINED_RING, false));
    }

    @Test
    void everyLayoutAndKindNamesAFamilyTheRendererLoads() {
        for (ArenaLayout layout : ArenaLayout.values()) {
            for (boolean shelters : new boolean[] {true, false}) {
                int family = ArenaEnvironmentRenderer.coverFamily(layout, shelters);
                assertTrue(
                    family >= 0 && family < ArenaEnvironmentRenderer.COVER_FAMILIES.length,
                    layout + " maps to family " + family + ", which the renderer has no texture for"
                );
            }
        }
        assertEquals(4, ArenaEnvironmentRenderer.COVER_FAMILIES.length, "four families are reviewed");
        assertEquals(3, ArenaEnvironmentRenderer.COVER_VARIANTS, "three variants are reviewed");
    }

    @Test
    void theShippedCatalogCarriesEveryCoverTextureTheRendererLoads() {
        JsonValue manifest;
        try {
            manifest = new JsonReader().parse(Files.readString(MANIFEST));
        } catch (IOException e) {
            throw new AssertionError("cannot read the generated manifest at " + MANIFEST.toAbsolutePath(), e);
        }
        Set<String> keys = new HashSet<>();
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            keys.add(asset.getString("key"));
        }
        int sheets = 0;
        for (String family : ArenaEnvironmentRenderer.COVER_FAMILIES) {
            for (int variant = 0; variant < ArenaEnvironmentRenderer.COVER_VARIANTS; variant++) {
                String key = "obstacle_" + family + "_" + variant;
                assertTrue(keys.contains(key), "the catalog does not carry " + key);
                sheets++;
            }
        }
        assertEquals(12, sheets);
        Path generated = MANIFEST.getParent();
        for (String family : ArenaEnvironmentRenderer.COVER_FAMILIES) {
            for (int variant = 0; variant < ArenaEnvironmentRenderer.COVER_VARIANTS; variant++) {
                Path sprite = generated.resolve("environment/obstacle_" + family + "_" + variant + ".png");
                assertTrue(Files.isRegularFile(sprite), sprite + " is named but not shipped");
                assertNotNull(sprite.getFileName());
            }
        }
    }

    @Test
    void theCoverIsDrawnOnTheGroundShadowsOwnLine() {
        // The shadows were measured against the landmark art's footprint, so the cover's base has to land on the
        // same line rather than derive its own: this pins the three numbers that place it.
        assertEquals(1.55f, ArenaEnvironmentRenderer.COVER_FRAME_SCALE, 1e-4f);
        assertEquals(0.23f, ArenaEnvironmentRenderer.COVER_BASE_SHARE, 1e-4f);
        assertEquals(0.87f, ArenaEnvironmentRenderer.COVER_BASE_BELOW_RADIUS, 1e-4f);
    }
}

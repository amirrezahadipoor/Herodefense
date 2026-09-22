package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.items.EquipmentCatalog;
import com.amirrezahadipoor.herodefense.items.EquipmentDefinition;
import com.amirrezahadipoor.herodefense.model.HeroAnimationState;
import com.amirrezahadipoor.herodefense.model.Item;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class EquipmentVisualContractTest {
    private static final Path REPOSITORY = Path.of("..").normalize();
    private static final Path ASSETS = REPOSITORY.resolve("android/assets");

    @Test
    void everyRuntimeItemMapsToItsReviewedAtlasAndFourHeroClips() {
        for (EquipmentDefinition definition : EquipmentCatalog.all()) {
            Item item = definition.createItem();
            assertEquals(EquipmentVisualContract.atlasPath(definition.artId()), item.visualKey);
            for (HeroAnimationState state : HeroAnimationState.values()) {
                assertEquals(
                    definition.artId() + "_" + state.name().toLowerCase(java.util.Locale.ROOT),
                    EquipmentVisualContract.regionName(item, state)
                );
            }
        }
    }

    @Test
    void onlyTheWalkFallsBackAndItFallsBackToIdle() {
        Item item = EquipmentCatalog.byId("ashwood_bow").createItem();
        assertEquals(
            List.of("ashwood_bow_walk", "ashwood_bow_idle"),
            EquipmentVisualContract.regionCandidates(item, HeroAnimationState.WALK),
            "a walking Hero wears the idle attachment when the sheet has no walk clip"
        );
        for (HeroAnimationState state : HeroAnimationState.values()) {
            if (state == HeroAnimationState.WALK) continue;
            assertEquals(
                List.of(EquipmentVisualContract.regionName(item, state)),
                EquipmentVisualContract.regionCandidates(item, state),
                state + " has the clip it was rendered with and no fallback"
            );
        }
    }

    /**
     * Every state the Hero can be in must resolve to a clip that is actually on the sheet, for every item in
     * the catalog. This is the crash the walk shipped with: the sheets carry idle, attack, hit and death, the
     * walk state asked for {@code <art>_walk}, and the renderer threw on the first step of every run (the run
     * starts with the ashwood bow equipped). The check reads the committed atlases, so a sheet re-rendered
     * without one of its clips, or a new state added without a fallback, fails here and not on a phone.
     */
    @Test
    void everyHeroStateResolvesToAClipOnEveryEquipmentSheet() throws IOException {
        int sheets = 0;
        for (EquipmentDefinition definition : EquipmentCatalog.all()) {
            Item item = definition.createItem();
            Path atlas = ASSETS.resolve(item.visualKey);
            assertTrue(Files.isRegularFile(atlas), "missing atlas " + atlas);
            Set<String> regions = regionNames(atlas);
            assertFalse(regions.isEmpty(), atlas + " names no regions");
            for (HeroAnimationState state : HeroAnimationState.values()) {
                List<String> candidates = EquipmentVisualContract.regionCandidates(item, state);
                assertTrue(
                    candidates.stream().anyMatch(regions::contains),
                    definition.id() + " has no clip for " + state + " on " + atlas.getFileName()
                        + ": none of " + candidates + " is on the sheet"
                );
            }
            sheets++;
        }
        assertTrue(sheets > 0, "the catalog names no equipment");
    }

    /** Region names of a libGDX text atlas: the unindented lines that are not a page header. */
    private static Set<String> regionNames(Path atlas) throws IOException {
        Set<String> names = new HashSet<>();
        for (String line : Files.readAllLines(atlas)) {
            if (line.isBlank() || line.startsWith(" ") || line.startsWith("\t") || line.contains(":")
                || line.toLowerCase(Locale.ROOT).endsWith(".png")) {
                continue;
            }
            names.add(line.trim());
        }
        return names;
    }
}

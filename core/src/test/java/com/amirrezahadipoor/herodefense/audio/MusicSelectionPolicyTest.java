package com.amirrezahadipoor.herodefense.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.GameScreenState;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Roadmap R6.3: the music state machine is asserted here, in the terms the roadmap asked for -- every screen
 * has a bed, the game's own screens are covered, and a boss fight outranks the screen it starts on.
 */
final class MusicSelectionPolicyTest {

    @Test
    void everyScreenTheGameCanBeInHasABedAndThisTestKnowsWhenAScreenIsAdded() {
        // The count is the contract: GameScreenState is the game's own list of screens, so a new screen that
        // the policy did not consider fails here rather than playing the menu theme over a fight.
        assertEquals(13, GameScreenState.values().length);
        for (GameScreenState state : GameScreenState.values()) {
            assertNotNull(MusicSelectionPolicy.bedFor(state, false), state + " has no bed");
            assertNotNull(MusicSelectionPolicy.bedFor(state, true), state + " has no bed in a boss fight");
            assertTrue(MusicSelectionPolicy.gainFor(state) > 0f, state + " would be silent");
        }
    }

    @Test
    void aBossOnTheFieldOutranksTheScreenAndOnlyDuringAFight() {
        for (GameScreenState state : GameScreenState.values()) {
            boolean combat = EnumSet.of(
                GameScreenState.PLAYING,
                GameScreenState.PAUSED,
                GameScreenState.LEVEL_UP,
                GameScreenState.CARD_CHOICE,
                GameScreenState.CINEMATIC
            ).contains(state);
            assertEquals(
                combat ? MusicBed.HOLLOW_MARCH : MusicSelectionPolicy.bedFor(state, false),
                MusicSelectionPolicy.bedFor(state, true),
                state + " in a boss fight"
            );
        }
    }

    @Test
    void theRunItsMenusAndItsEndingEachHaveTheirOwnBed() {
        assertEquals(MusicBed.VIGIL, MusicSelectionPolicy.bedFor(GameScreenState.PLAYING, false));
        assertEquals(MusicBed.HEARTWOOD_DAWN, MusicSelectionPolicy.bedFor(GameScreenState.MENU, false));
        assertEquals(MusicBed.HEARTWOOD_DAWN, MusicSelectionPolicy.bedFor(GameScreenState.CODEX, false));
        assertEquals(MusicBed.QUIET_AFTER, MusicSelectionPolicy.bedFor(GameScreenState.GAME_OVER, false));
        assertEquals(MusicBed.HEARTWOOD_DAWN, MusicSelectionPolicy.bedFor(null, true), "a missing state is not a crash");
    }

    @Test
    void atLeastThreeBedsAreReachableFromTheRealScreenList() {
        Map<MusicBed, Set<GameScreenState>> users = new EnumMap<>(MusicBed.class);
        for (MusicBed bed : MusicBed.values()) users.put(bed, EnumSet.noneOf(GameScreenState.class));
        for (GameScreenState state : GameScreenState.values()) {
            users.get(MusicSelectionPolicy.bedFor(state, false)).add(state);
            users.get(MusicSelectionPolicy.bedFor(state, true)).add(state);
        }
        Set<MusicBed> used = EnumSet.noneOf(MusicBed.class);
        users.forEach((bed, screens) -> {
            if (!screens.isEmpty()) used.add(bed);
        });
        assertEquals(Set.of(MusicBed.VIGIL, MusicBed.HOLLOW_MARCH, MusicBed.HEARTWOOD_DAWN, MusicBed.QUIET_AFTER), used);
        assertTrue(used.size() >= 3, "R6.1 asked for three or four beds in real use");
    }

    @Test
    void onlyTheScreensThatAreDecidingSomethingDuck() {
        assertEquals(1f, MusicSelectionPolicy.gainFor(GameScreenState.PLAYING));
        assertTrue(MusicSelectionPolicy.gainFor(GameScreenState.PAUSED) < 1f);
        assertTrue(MusicSelectionPolicy.gainFor(GameScreenState.CARD_CHOICE) < 1f);
        assertEquals(MusicSelectionPolicy.gainFor(null), 1f);
    }

    @Test
    void everyBedIsACommittedAssetWhoseFileNameMatchesItsEnumName() {
        for (MusicBed bed : MusicBed.values()) {
            Path file = Paths.get("..", "android", "assets", bed.path());
            assertTrue(Files.isRegularFile(file), bed.path() + " is not committed");
            assertTrue(bed.path().endsWith(".ogg"), bed.path());
            assertTrue(bed.path().toLowerCase(java.util.Locale.ROOT).contains(bed.name().toLowerCase(java.util.Locale.ROOT)), bed.path());
            assertTrue(bed.baseVolume() > 0f && bed.baseVolume() <= 0.5f, bed + " must sit under the effects");
        }
    }

    @Test
    void everyBedIsInTheLicenseLedgerWithItsHash() throws Exception {
        String ledger = Files.readString(Paths.get("..", "docs", "audio", "AUDIO_LICENSES.md"));
        for (MusicBed bed : MusicBed.values()) {
            Path file = Paths.get("..", "android", "assets", bed.path());
            String hash = java.util.HexFormat.of().formatHex(
                java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
            assertTrue(ledger.contains("`" + bed.path() + "`"), bed.path() + " missing from the ledger");
            assertTrue(ledger.contains(hash), bed.path() + " hash missing from the ledger");
        }
    }

    @Test
    void theCrossfadeHandsOverWithoutEverPlayingLouderThanOneBed() {
        Crossfade fade = new Crossfade();
        assertEquals(1f, fade.incoming(), "before any change there is one bed at full gain");
        assertFalse(fade.fading());
        fade.start();
        float loudest = 0f;
        assertEquals(0f, fade.incoming());
        assertEquals(1f, fade.outgoing());
        for (int frame = 0; frame < 120; frame++) {
            fade.advance(1f / 60f);
            loudest = Math.max(loudest, fade.incoming() + fade.outgoing());
            assertTrue(fade.incoming() >= 0f && fade.incoming() <= 1f);
        }
        assertEquals(1f, fade.incoming(), "the fade must end exactly full");
        assertFalse(fade.fading());
        assertEquals(1f, loudest, 1e-5f, "the two beds may never sum to more than one");
    }

    @Test
    void aStalledFrameCannotOvershootTheFadeAndNegativeTimeCannotRewindIt() {
        Crossfade fade = new Crossfade();
        fade.start();
        fade.advance(30f);
        assertEquals(1f, fade.incoming());
        fade.advance(-5f);
        assertEquals(1f, fade.incoming(), "time does not run backwards");
    }
}

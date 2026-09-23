package com.amirrezahadipoor.herodefense.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import com.amirrezahadipoor.herodefense.story.Deeds;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Mid-fight silence: no boss, Hollow, Pip or Warden line ever speaks while enemies live. Bosses
 * talk in their watch-only intro before the wave; once the fight starts, only deeds announce --
 * a payment, not dialogue -- and evolutions flash without words.
 */
class CombatSilenceTest {

    private final List<String> beats = new ArrayList<>();

    private final class RecordingBeatSink implements RunPresentationSystem.BeatSink {
        @Override public void showBeat(String line) {
            beats.add(line);
        }

        @Override public void save() {
        }
    }

    private RunPresentationSystem presentation() {
        return new RunPresentationSystem(
            new ParticleSystem(), new ScreenShakeSystem(), new CodexSystem(), new RecordingBeatSink());
    }

    @Test
    void aWoundedBossShowsNothingOnAnyFrame() {
        GameState state = GameState.newRun(41L);
        Boss boss = new Boss(state.allocateEntityId(), "ANCIENT_GOLEM", 300f, 400f, 1);
        boss.maxHealth = 1000f;
        boss.health = 1f;
        state.aliveBosses.add(boss);

        presentation().presentPlaytime(state);
        presentation().presentPlaytime(state);
        assertTrue(beats.isEmpty(), "a fight at 1 HP spoke: " + beats);
    }

    @Test
    void deedsStillAnnounceBecauseAPaymentIsNotDialogue() {
        GameState state = GameState.newRun(42L);
        state.waveNumber = 10;

        presentation().presentPlaytime(state);
        assertEquals(1, beats.size(), "the wave-10 deed owes its announcement");
        assertTrue(beats.get(0).contains("Deed: held to wave 10"), beats.get(0));
    }

    @Test
    void aDeedEarnedUnderABossIsPaidButSilent() {
        GameState state = GameState.newRun(43L);
        state.waveNumber = 50;
        Boss boss = new Boss(state.allocateEntityId(), "ANCIENT_GOLEM", 300f, 400f, 10);
        boss.maxHealth = 1000f;
        boss.health = 1f;
        state.aliveBosses.add(boss);

        presentation().presentPlaytime(state);

        assertTrue(beats.isEmpty(), "a deed announced over a living boss: " + beats);
        assertTrue(
            Boolean.TRUE.equals(state.codexUnlocked.get(Deeds.WAVE_50.key())),
            "the wave-50 deed must still pay while silent"
        );
        assertTrue(
            Boolean.TRUE.equals(state.codexUnlocked.get(Deeds.FLAWLESS_50.key())),
            "the flawless-50 deed must still pay while silent"
        );
        assertTrue(boss.evolutionPresented, "the ring is visual, not words: it still fires");
    }

    @Test
    void theSilentDeedAnnouncesOnTheNextBossFreeFrame() {
        GameState state = GameState.newRun(44L);
        state.waveNumber = 50;
        Boss boss = new Boss(state.allocateEntityId(), "ANCIENT_GOLEM", 300f, 400f, 10);
        state.aliveBosses.add(boss);
        RunPresentationSystem presentation = presentation();

        presentation.presentPlaytime(state);
        assertTrue(beats.isEmpty(), "a deed announced over a living boss: " + beats);

        boss.alive = false;
        presentation.presentPlaytime(state);
        assertEquals(1, beats.size(), "the oldest waiter announces first");
        assertTrue(beats.get(0).contains("Deed: held to wave 10"), beats.get(0));
        for (int frame = 0; frame < 4; frame++) {
            presentation.presentPlaytime(state);
        }
        assertEquals(5, beats.size(), "every waiter announces, one frame at a time");
        assertTrue(beats.get(4).contains("Deed: wave 50, never down"), beats.get(4));
    }
}

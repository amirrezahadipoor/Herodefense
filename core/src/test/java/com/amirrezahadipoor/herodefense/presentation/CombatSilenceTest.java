package com.amirrezahadipoor.herodefense.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
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
}

package com.amirrezahadipoor.herodefense.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.input.HapticFeedback;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Roadmap F4: the watcher speaks exactly when the run transitions, and never when it merely resumes. */
final class HapticRunWatcherTest {
    private final List<String> pulses = new ArrayList<>();
    private final HapticFeedback hands = new HapticFeedback() {
        @Override public void tap() {
            pulses.add("tap");
        }

        @Override public void cardSelection() {
            pulses.add("card");
        }

        @Override public void heroDamaged() {
            pulses.add("hit");
        }

        @Override public void bossDefeated() {
            pulses.add("boss");
        }

        @Override public void levelUp() {
            pulses.add("level");
        }

        @Override public void waveCleared() {
            pulses.add("wave");
        }

        @Override public void ultimateReleased() {
            pulses.add("ultimate");
        }
    };
    private final HapticRunWatcher watcher = new HapticRunWatcher(hands);

    private static GameState playing() {
        GameState state = GameState.newRun(31L);
        state.hero.maxHealth = 100f;
        state.hero.health = 100f;
        return state;
    }

    private void enter(GameState state) {
        watcher.watch(GameScreenState.PLAYING, state, 0.016f);
    }

    @Test
    void theFirstFrameOfARunOnlyRemembers() {
        GameState state = playing();
        state.waveNumber = 12;
        state.heroLevel = 4;
        state.focus = 80f;
        enter(state);
        assertTrue(pulses.isEmpty(), "entering the run is a baseline, not a transition");
    }

    @Test
    void everyRunTransitionSpeaksOnce() {
        GameState state = playing();
        enter(state);
        state.hero.health = 82f;
        enter(state);
        assertEquals(List.of("hit"), pulses);
        pulses.clear();

        Boss boss = new Boss(state.allocateEntityId(), "ANCIENT_GOLEM", 10f, 10f, 1);
        state.aliveBosses.add(boss);
        enter(state);
        assertTrue(pulses.isEmpty(), "a boss arriving is the entrance cue's job, not the hands'");
        boss.alive = false;
        enter(state);
        assertEquals(List.of("boss"), pulses);
        pulses.clear();

        state.heroLevel = 2;
        enter(state);
        assertEquals(List.of("level"), pulses);
        pulses.clear();

        state.waveNumber = 2;
        enter(state);
        assertEquals(List.of("wave"), pulses);
        pulses.clear();

        state.focus = 100f;
        enter(state);
        state.focus = 0f;
        enter(state);
        assertEquals(List.of("ultimate"), pulses);
    }

    @Test
    void aSwarmIsOneEventNotAJackhammer() {
        GameState state = playing();
        enter(state);
        state.hero.health = 95f;
        enter(state);
        state.hero.health = 90f;
        enter(state);
        state.hero.health = 85f;
        enter(state);
        assertEquals(List.of("hit"), pulses, "three hits inside the cooldown are one pulse");
        watcher.watch(GameScreenState.PLAYING, state, 0.7f);
        state.hero.health = 80f;
        enter(state);
        assertEquals(List.of("hit", "hit"), pulses, "and the cooldown expires into the next one");
    }

    @Test
    void focusThatMerelyRefillsOrDrainsSlowlyIsNotTheUltimate() {
        GameState state = playing();
        state.focus = 40f;
        enter(state);
        state.focus = 44f;
        enter(state);
        state.focus = 43f;
        enter(state);
        assertTrue(pulses.isEmpty(), "passive gain and small noise never fire the ultimate pulse");
    }

    @Test
    void leavingTheRunScreenAndComingBackRebaselinesInsteadOfFiring() {
        GameState state = playing();
        enter(state);
        watcher.watch(GameScreenState.PAUSED, state, 0.016f);
        state.waveNumber = 5;
        state.heroLevel = 3;
        state.focus = 90f;
        enter(state);
        assertTrue(pulses.isEmpty(), "everything that changed while away changed while nobody was watching");
    }

    @Test
    void aDeadHeroTakingCorpseDamageDoesNotPulse() {
        GameState state = playing();
        enter(state);
        state.hero.alive = false;
        state.hero.health = 0f;
        enter(state);
        assertTrue(pulses.isEmpty(), "the killing blow already pulsed or the death screen owns the moment");
    }

    @Test
    void aHostWithoutHandsIsASilenceNotACrash() {
        HapticRunWatcher mute = new HapticRunWatcher(null);
        GameState state = playing();
        mute.watch(GameScreenState.PLAYING, state, 0.016f);
        state.hero.health = 10f;
        mute.watch(GameScreenState.PLAYING, state, 0.016f);
        assertTrue(pulses.isEmpty());
    }
}

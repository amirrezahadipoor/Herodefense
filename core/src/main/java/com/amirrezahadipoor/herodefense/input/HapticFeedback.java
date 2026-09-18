package com.amirrezahadipoor.herodefense.input;

public interface HapticFeedback {
    void tap();

    void cardSelection();

    // The run's own punctuation (roadmap F4). Defaults because every fake that predates the vocabulary
    // has no reason to learn it, and a haptic nobody answers is a silence, not a crash.

    /** The hero took a hit: one heavy pulse, rate-limited by the watcher so a swarm is not a jackhammer. */
    default void heroDamaged() {
    }

    /** A boss fell: the biggest moment a run has, in three strikes. */
    default void bossDefeated() {
    }

    /** The hero leveled: a light rising double. */
    default void levelUp() {
    }

    /** The wave rolled over: the softest tick, once per wave, felt more than heard. */
    default void waveCleared() {
    }

    /** The ultimate went out: one long pulse under the release. */
    default void ultimateReleased() {
    }
}

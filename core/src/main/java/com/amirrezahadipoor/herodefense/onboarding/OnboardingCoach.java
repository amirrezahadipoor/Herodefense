package com.amirrezahadipoor.herodefense.onboarding;

/**
 * The first-run coaching state machine (roadmap R7.1).
 *
 * <p>Deliberately clock-driven and input-driven only: no rendering, no device, no randomness, so the whole
 * sequence can be stepped through in a unit test with a for-loop. The two ways a step ends — the action it
 * waits for, or its budget expiring — are the entire rule set, and they are the reason the coach can be
 * relied on: it can never wait forever for a player who is not listening, and it can never skip a lesson
 * because the player happened to do something unrelated first.
 */
public final class OnboardingCoach {
    private int stepIndex;
    private float secondsOnStep;
    private float elapsedSeconds;
    private boolean active;
    private boolean skipped;

    /**
     * Starts the sequence. Calling it while one is already running is a no-op rather than a restart, so a
     * caller cannot rewind the lesson by accident. That the lesson happens *once per device* is not this
     * class's rule — it is the saved flag in {@code OnboardingSystem}, which is where the player's history
     * lives.
     */
    public void begin() {
        if (active) return;
        stepIndex = 0;
        secondsOnStep = 0f;
        elapsedSeconds = 0f;
        active = true;
        skipped = false;
    }

    /** Advances the clock; a step whose budget runs out is left behind rather than waiting forever. */
    public void update(float deltaSeconds) {
        if (!active || deltaSeconds <= 0f) return;
        elapsedSeconds += deltaSeconds;
        secondsOnStep += deltaSeconds;
        while (active && stepIndex < OnboardingStep.values().length
            && secondsOnStep >= currentStep().budgetSeconds()) {
            advance();
        }
    }

    /**
     * Reports a player action. Only the current step's own action advances the sequence, and only while it
     * is active; anything else is ignored rather than queued.
     */
    public void notify(OnboardingAction action) {
        if (!active || action == null) return;
        if (currentStep().completesOn(action)) {
            advance();
        }
    }

    /** The player asked to stop being taught. The sequence ends without completing its steps. */
    public void skip() {
        if (!active) return;
        active = false;
        skipped = true;
    }

    /** True while a line is on screen and the coach is still waiting for (or timing) a step. */
    public boolean active() {
        return active;
    }

    /** True once the player skipped, as opposed to finishing every step. */
    public boolean skipped() {
        return skipped;
    }

    /** True once the sequence has ended, whether by teaching everything or by being skipped. */
    public boolean finished() {
        return !active && (skipped || stepIndex >= OnboardingStep.values().length);
    }

    /** The step being taught, or the last one once the sequence has ended. */
    public OnboardingStep currentStep() {
        return OnboardingStep.values()[Math.min(stepIndex, OnboardingStep.values().length - 1)];
    }

    /** How many steps the player has completed (the skipped one, if any, is not counted). */
    public int completedCount() {
        return skipped ? stepIndex : Math.min(stepIndex, OnboardingStep.values().length);
    }

    /** Seconds the sequence has been running, counting the time spent on completed steps. */
    public float elapsedSeconds() {
        return elapsedSeconds;
    }

    /** Seconds the current step has been on screen. */
    public float secondsOnStep() {
        return secondsOnStep;
    }

    /** How far into the current step's budget the player is, 0 to 1: what a progress ring would draw. */
    public float stepProgress() {
        float budget = currentStep().budgetSeconds();
        return budget <= 0f ? 1f : Math.min(1f, secondsOnStep / budget);
    }

    private void advance() {
        stepIndex++;
        secondsOnStep = 0f;
        if (stepIndex >= OnboardingStep.values().length) {
            active = false;
        }
    }
}

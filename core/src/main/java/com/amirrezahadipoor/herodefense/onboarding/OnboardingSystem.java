package com.amirrezahadipoor.herodefense.onboarding;

import com.amirrezahadipoor.herodefense.input.OnboardingTouchLayout;
import com.amirrezahadipoor.herodefense.settings.GameSettings;
import com.amirrezahadipoor.herodefense.settings.LocalSettingsRepository;

/**
 * The first vigil, as the rest of the game sees it (roadmap R7.1): one coach, one persisted flag, and the
 * three questions a caller has — does it want this run, did that tap belong to it, and what is it saying.
 *
 * <p>"Exactly once" is a property of the save file, not of the machine's memory: finishing or skipping the
 * sequence writes {@code tutorialSeen} through the settings repository, and a run that starts afterwards
 * never sees the coach again. A player who starts a second run in the same session is protected by the same
 * flag, because the flag lives in the settings object the game holds, not in the coach.
 */
public final class OnboardingSystem {
    private final OnboardingCoach coach = new OnboardingCoach();
    private int lastCoins;
    private int lastItems;
    private GameSettings settings;
    private LocalSettingsRepository repository;

    /**
     * Hands the system the device's settings (and, where the caller has it, the store they are written to).
     * Called by the touch host when a run starts; a later call only fills in what is still missing, so the
     * render side can attach the settings object without ever overwriting the persistence the run started
     * with.
     */
    public void attach(GameSettings attachedSettings, LocalSettingsRepository attachedRepository) {
        if (attachedSettings != null) {
            settings = attachedSettings;
        }
        if (attachedRepository != null) {
            repository = attachedRepository;
        }
    }

    /** True once this device has already been taught. */
    public boolean seen() {
        return settings != null && settings.tutorialSeen;
    }

    /**
     * Called when a run starts. A fresh save gets the sequence; a save that has seen it (or a Continue)
     * gets nothing, and the call is a no-op that a caller does not have to check for.
     */
    public void beginIfUnseen(boolean continuedRun) {
        if (continuedRun || settings == null || settings.tutorialSeen) return;
        coach.begin();
    }

    /**
     * Reads the run's own books once a frame and reports what the player has already done: coins arriving and
     * items entering the backpack are the two ways this game says "a drop reached you". The coach ignores
     * anything it is not waiting for, so this is a report and not a second state machine.
     */
    public void observe(com.amirrezahadipoor.herodefense.model.GameState state) {
        if (state == null) return;
        if (state.coins != lastCoins || state.inventory.size() != lastItems) {
            notify(OnboardingAction.PICKUP_COLLECTED);
        }
        lastCoins = state.coins;
        lastItems = state.inventory.size();
    }

    /** Advances the clock and closes the sequence out when the last step ends. */
    public void update(float deltaSeconds) {
        if (!coach.active()) return;
        coach.update(deltaSeconds);
        if (coach.finished()) {
            markSeen();
        }
    }

    /** Reports a player action; the coach decides whether it was the one it was waiting for. */
    public void notify(OnboardingAction action) {
        if (!coach.active()) return;
        coach.notify(action);
        if (coach.finished()) {
            markSeen();
        }
    }

    /**
     * Routes a tap. True means the tap was the Skip button and belongs to the coach: the caller must not
     * also treat it as a play input, or a player who skips the lesson would also walk the Hero into the
     * enemies while the banner is disappearing.
     */
    public boolean handleTap(float worldX, float worldY) {
        if (!coach.active() || !OnboardingTouchLayout.skipAt(worldX, worldY)) return false;
        coach.skip();
        markSeen();
        return true;
    }

    public boolean active() {
        return coach.active();
    }

    public OnboardingStep currentStep() {
        return coach.currentStep();
    }

    public int completedCount() {
        return coach.completedCount();
    }

    public boolean skipped() {
        return coach.skipped();
    }

    public float stepProgress() {
        return coach.stepProgress();
    }

    /** Records that this device has had its first vigil, so no later run does. */
    private void markSeen() {
        if (settings == null || settings.tutorialSeen) return;
        settings.tutorialSeen = true;
        if (repository != null) {
            repository.save(settings);
        }
    }
}

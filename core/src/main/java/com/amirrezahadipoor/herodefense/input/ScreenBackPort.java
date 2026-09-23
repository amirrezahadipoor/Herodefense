package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.render.GameOverOverlayRenderer;

/**
 * What Android's Back button does to this game, in calls (roadmap R7.4).
 *
 * <p>{@link BackNavigation} decides; this class applies. Every method here is the call sequence the matching
 * button on that screen already makes — the same cue, the same controller call, the same save, in the same
 * order — so a press and the button it stands in for cannot drift apart. R7.7 exists because two readers of
 * one menu table did exactly that.
 *
 * <p>It reaches the game through {@link ScreenTouchRouter.Host}, the port the whole touch chain uses, which
 * means the Back button cannot do anything a tap cannot.
 */
final class ScreenBackPort implements BackNavigation.Port {

    private final ScreenTouchRouter.Host host;

    ScreenBackPort(ScreenTouchRouter.Host host) {
        if (host == null) {
            throw new IllegalArgumentException("host cannot be null");
        }
        this.host = host;
    }

    @Override
    public GameScreenState state() {
        return host.flow().state();
    }

    @Override
    public boolean detailsPanelOpen(GameScreenState state) {
        if (state == GameScreenState.CODEX) {
            return host.codexTouchController().selectedIndex() >= 0;
        }
        if (state == GameScreenState.INVENTORY) {
            return host.inventoryTouchController().selectedIndex() >= 0;
        }
        return false;
    }

    @Override
    public void closeDetailsPanel(GameScreenState state) {
        if (state == GameScreenState.CODEX) {
            host.codexTouchController().clearSelection();
        } else {
            host.inventoryTouchController().clearSelection();
        }
    }

    @Override
    public void skipCeremony() {
        if (host.openingCinematic().isActive()) {
            host.openingCinematic().skip();
        } else if (host.bossIntroCinematic().isActive()) {
            host.bossIntroCinematic().skip();
        } else {
            host.plantingCeremony().skip();
        }
    }

    @Override
    public void resumeRun() {
        host.flow().returnFromOverlay();
    }

    @Override
    public void closeScreen(GameScreenState state) {
        // Branch by branch this is the screen's own Close button: the same cue, the same controller call
        // and the same save, in the same order.
        switch (state) {
            case SETTINGS -> {
                host.audioManager().play(AudioCue.UI_CLOSE);
                host.flow().transitionTo(GameScreenState.MENU);
            }
            case CODEX -> {
                host.audioManager().play(AudioCue.UI_CLOSE);
                host.codexTouchController().close();
                host.flow().returnFromOverlay();
                host.saveNow();
            }
            case SHOP -> {
                host.audioManager().play(AudioCue.UI_CLOSE);
                host.flow().returnFromOverlay();
                host.saveNow();
            }
            case INVENTORY, ROOT_NETWORK -> {
                host.flow().returnFromOverlay();
                host.saveNow();
            }
            // Unreachable while the policy table is the one being applied. Doing nothing is deliberate:
            // a transition that throws would reach a player as a crash on the phone's own navigation key.
            default -> {
            }
        }
    }

    @Override
    public void pauseRun() {
        host.flow().transitionTo(GameScreenState.PAUSED);
        // The HUD's pause button does not save, because the player stays in the app. Back is pressed on
        // the way out, and roadmap R13.5 asks that a session survive the process, so this one writes.
        host.saveNow();
    }

    @Override
    public void quitToMenu() {
        // The end screen ignores taps during its own presentation, and Back is no different: the run's
        // record was written when the run ended, not when the player is allowed to leave the screen.
        if (!GameOverOverlayRenderer.isInteractive(
                host.gameOverPresentationSeconds(), host.gameState().runComplete)) {
            return;
        }
        host.flow().transitionTo(GameScreenState.MENU);
        host.saveNow();
    }

    @Override
    public void exitApplication() {
        host.exitApplication();
    }
}

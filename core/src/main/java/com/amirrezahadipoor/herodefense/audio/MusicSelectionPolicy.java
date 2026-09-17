package com.amirrezahadipoor.herodefense.audio;

import com.amirrezahadipoor.herodefense.GameScreenState;

/**
 * Which music bed belongs to which game state, and how loud (roadmap R6.3).
 *
 * <p>Pure on purpose: the game flow and this table are the two halves of "the music follows the game", and the
 * half that decides is testable without a sound device. The half that plays is {@link MusicDeck}.
 *
 * <p>Two rules carry the design. A boss on the field outranks the screen, because the fight is the moment;
 * and overlays that are opened *over* a running fight -- inventory, shop, codex, the root network -- go to the
 * menu bed rather than stopping the fight's music, since the player is still standing in the arena.
 */
public final class MusicSelectionPolicy {

    private MusicSelectionPolicy() {
    }

    /** Headroom multiplier for a state that should sit under the player's attention. */
    private static final float DUCKED = 0.55f;

    public static MusicBed bedFor(GameScreenState state, boolean bossFightActive) {
        if (state == null) return MusicBed.HEARTWOOD_DAWN;
        if (bossFightActive && combatState(state)) return MusicBed.HOLLOW_MARCH;
        return switch (state) {
            case MENU, SETTINGS, INVENTORY, SHOP, ROOT_NETWORK, CODEX, TRIAL_DRAFT -> MusicBed.HEARTWOOD_DAWN;
            case GAME_OVER -> MusicBed.QUIET_AFTER;
            case PLAYING, PAUSED, LEVEL_UP, CARD_CHOICE, CINEMATIC -> MusicBed.VIGIL;
        };
    }

    /**
     * Gain for the current screen, as a fraction of the bed's own level. A paused game, a level-up wall and a
     * card choice all want the music present but not in front of the thing being decided.
     */
    public static float gainFor(GameScreenState state) {
        if (state == null) return 1f;
        boolean deciding = state == GameScreenState.PAUSED
            || state == GameScreenState.LEVEL_UP
            || state == GameScreenState.CARD_CHOICE
            || state == GameScreenState.SETTINGS
            || state == GameScreenState.TRIAL_DRAFT;
        return deciding ? DUCKED : 1f;
    }

    /** Whether the screen is a fight or a ceremony around one. */
    private static boolean combatState(GameScreenState state) {
        return state == GameScreenState.PLAYING
            || state == GameScreenState.PAUSED
            || state == GameScreenState.LEVEL_UP
            || state == GameScreenState.CARD_CHOICE
            || state == GameScreenState.CINEMATIC;
    }
}

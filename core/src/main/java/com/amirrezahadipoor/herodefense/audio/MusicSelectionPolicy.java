package com.amirrezahadipoor.herodefense.audio;

import com.amirrezahadipoor.herodefense.model.GameState;

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

    /** The wave the run's intensity layer starts to fade in at (roadmap F1). */
    private static final int TENSION_FIRST_WAVE = 41;
    private static final int TENSION_DEEP_WAVE = 101;
    private static final int TENSION_FINAL_WAVE = 151;
    private static final float TENSION_LOW_HEALTH_FRACTION = 0.30f;
    private static final float TENSION_LOW_HEALTH_BONUS = 0.30f;

    /**
     * How hard the run is pressing right now, 0..1: the vigil bed's intensity layer rides this. It climbs in
     * three steps with the wave tiers the difficulty curve itself uses, and a hero below 30% health adds
     * urgency wherever the run is. Only the run screen asks: every other screen is 0, and a boss fight needs
     * no layer because the whole bed changes to HOLLOW_MARCH.
     */
    public static float tensionFor(GameScreenState state, GameState run) {
        if (state != GameScreenState.PLAYING || run == null) return 0f;
        int wave = run.waveNumber;
        float tension;
        if (wave < TENSION_FIRST_WAVE) {
            tension = 0f;
        } else if (wave < TENSION_DEEP_WAVE) {
            tension = 0.35f;
        } else if (wave < TENSION_FINAL_WAVE) {
            tension = 0.70f;
        } else {
            tension = 1f;
        }
        if (run.hero != null && run.hero.alive && run.hero.maxHealth > 0f
            && run.hero.health / run.hero.maxHealth < TENSION_LOW_HEALTH_FRACTION) {
            tension = Math.min(1f, tension + TENSION_LOW_HEALTH_BONUS);
        }
        return tension;
    }

    /**
     * Whether the vigil's own sound (wind, a distant canopy) is under the music. It belongs to the arena and to
     * the walls that stand in it: in a menu the place is not around the player, and at the end of a run the
     * silence is the point.
     */
    public static boolean ambienceFor(GameScreenState state) {
        if (state == null) return false;
        return combatState(state);
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

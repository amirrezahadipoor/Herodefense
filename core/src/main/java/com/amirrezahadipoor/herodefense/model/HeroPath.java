package com.amirrezahadipoor.herodefense.model;

/**
 * The run-shaping choice the pre-run draft opens on (roadmap B3): before the two trials, the player picks the
 * path the hero walks, and every number of the run is bent by it. Each path raises one thing and lowers
 * another by design -- there is no strongest path, only different runs.
 *
 * <p>{@code UNBOUND} bends nothing, and so does an unchosen path ({@code GameState.heroPath == null}, which is
 * what every old save decodes to and what the balance simulator builds): identity multipliers keep both
 * bit-identical to the pre-path game, which is what keeps the frozen balance baseline honest.
 */
public enum HeroPath {
    /** The classic vigil, unchanged: no bonus and no price. */
    UNBOUND("strength"),
    /** Deep and slow: more health to stand in, less damage to finish with. */
    ROOT("health"),
    /** Fast and fragile: more attacks per second, less health per hit taken. */
    WIND("speed"),
    /** Focused and measured: the ultimate charges faster, the bow strings slower. */
    STAR("general_power");

    private static final float ROOT_MAX_HEALTH = 1.25f;
    private static final float ROOT_DAMAGE = 0.90f;
    private static final float WIND_ATTACK_SPEED = 1.15f;
    private static final float WIND_MAX_HEALTH = 0.90f;
    private static final float STAR_FOCUS_GAIN = 1.25f;
    private static final float STAR_ATTACK_SPEED = 0.90f;

    private final String iconKey;

    HeroPath(String iconKey) {
        this.iconKey = iconKey;
    }

    /**
     * The reviewed premium icon this path's card wears -- a bare key into the {@code ui_*} icon set the
     * trials already draw from, so the file is pinned to exist by the same asset check that pins theirs.
     */
    public String iconKey() {
        return iconKey;
    }

    public static HeroPath forName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (HeroPath path : values()) {
            if (path.name().equals(name)) {
                return path;
            }
        }
        return null;
    }

    /** The path the run walks, or {@code null} while the draft has not bound one (or on an old save). */
    public static HeroPath chosen(GameState state) {
        return state == null ? null : forName(state.heroPath);
    }

    public static float damageMultiplier(GameState state) {
        return chosen(state) == ROOT ? ROOT_DAMAGE : 1f;
    }

    public static float maxHealthMultiplier(GameState state) {
        HeroPath path = chosen(state);
        if (path == ROOT) {
            return ROOT_MAX_HEALTH;
        }
        return path == WIND ? WIND_MAX_HEALTH : 1f;
    }

    public static float attackSpeedMultiplier(GameState state) {
        HeroPath path = chosen(state);
        if (path == WIND) {
            return WIND_ATTACK_SPEED;
        }
        return path == STAR ? STAR_ATTACK_SPEED : 1f;
    }

    public static float focusGainMultiplier(GameState state) {
        return chosen(state) == STAR ? STAR_FOCUS_GAIN : 1f;
    }
}

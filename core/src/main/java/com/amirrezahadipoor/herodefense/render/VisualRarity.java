package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.graphics.Color;

/** Runtime visual tiers; glow is intentionally absent from generated sprites. */
public enum VisualRarity {
    COMMON(false, 0f, 0f, 0f, 0f),
    UNCOMMON(false, 0f, 0f, 0f, 0f),
    RARE(true, 0.22f, 0.55f, 1.0f, 0.90f), // Phase 52: 0.82->0.90 stronger
    LEGENDARY(true, 1.0f, 0.80f, 0.15f, 1.25f), // Phase 52: 0.67->0.80 gold brighter, 1.0->1.25 double halo
    /** Violet aura matching the Mythic inventory ink, burning hottest of all. */
    MYTHIC(true, 0.85f, 0.45f, 1.0f, 1.55f), // Phase 52: 1.3->1.55 stunning mythic
    /** Sickly chartreuse for blightburst Elites. */
    ELITE_BLIGHTBURST(true, 0.55f, 1.0f, 0.25f, 1.05f),
    /** Deep root-teal for rootward Elites. */
    ELITE_ROOTWARD(true, 0.25f, 0.9f, 0.6f, 1.05f),
    /** Bruised magenta for weeping Elites. */
    ELITE_WEEPING(true, 1.0f, 0.25f, 0.45f, 1.05f),
    /** Bone amber for hollowmolt Elites (roadmap D2). */
    ELITE_HOLLOWMOLT(true, 0.95f, 0.85f, 0.55f, 1.05f),
    /** Patient grey-green for gravemoss Elites (roadmap D2). */
    ELITE_GRAVEMOSS(true, 0.55f, 0.75f, 0.45f, 1.05f),
    /** Warm ember for cinderhalo Elites (roadmap D2). */
    ELITE_CINDERHALO(true, 1.0f, 0.5f, 0.2f, 1.15f);

    public static final Color DEFAULT_COMMON = Color.valueOf("E7D8B1");
    public static final Color DEFAULT_UNCOMMON = Color.valueOf("74C365");
    public static final Color DEFAULT_RARE = Color.valueOf("6FADEB");
    public static final Color DEFAULT_LEGENDARY = Color.valueOf("F2B84B");
    public static final Color DEFAULT_MYTHIC = Color.valueOf("C77DFF");

    /**
     * Accessible rarity palette (roadmap G3c) based on Okabe-Ito / Wong high-contrast color choices,
     * maintaining high pairwise Euclidean and luminance distance under protanopia,
     * deuteranopia, and tritanopia.
     */
    public static final Color ACCESSIBLE_COMMON = Color.valueOf("D9D2C9");
    public static final Color ACCESSIBLE_UNCOMMON = Color.valueOf("56B4E9");
    public static final Color ACCESSIBLE_RARE = Color.valueOf("0072B2");
    public static final Color ACCESSIBLE_LEGENDARY = Color.valueOf("E69F00");
    public static final Color ACCESSIBLE_MYTHIC = Color.valueOf("CC79A7");

    private final boolean glowing;
    private final float red;
    private final float green;
    private final float blue;
    private final float intensity;

    VisualRarity(boolean glowing, float red, float green, float blue, float intensity) {
        this.glowing = glowing;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.intensity = intensity;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public float red() {
        return red;
    }

    public float green() {
        return green;
    }

    public float blue() {
        return blue;
    }

    public float intensity() {
        return intensity;
    }

    public Color color(boolean colourBlindSafe) {
        if (!colourBlindSafe) {
            return switch (this) {
                case UNCOMMON -> DEFAULT_UNCOMMON;
                case RARE -> DEFAULT_RARE;
                case LEGENDARY -> DEFAULT_LEGENDARY;
                case MYTHIC -> DEFAULT_MYTHIC;
                default -> DEFAULT_COMMON;
            };
        }
        return switch (this) {
            case UNCOMMON -> ACCESSIBLE_UNCOMMON;
            case RARE -> ACCESSIBLE_RARE;
            case LEGENDARY -> ACCESSIBLE_LEGENDARY;
            case MYTHIC -> ACCESSIBLE_MYTHIC;
            default -> ACCESSIBLE_COMMON;
        };
    }

    public Color color() {
        return color(false);
    }

    public static Color colorForTier(String tier, boolean colourBlindSafe) {
        return fromTier(tier).color(colourBlindSafe);
    }

    public static Color colorForTier(String tier) {
        return colorForTier(tier, false);
    }

    public static VisualRarity fromTier(String tier) {
        try {
            if (tier == null) return COMMON;
            return VisualRarity.valueOf(tier);
        } catch (IllegalArgumentException error) {
            return COMMON;
        }
    }
}

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
    ELITE_CINDERHALO(true, 1.0f, 0.5f, 0.2f, 1.15f),
    /** Quarry-grey for stoneshell Elites: the colour of the hill they pulled over themselves. */
    ELITE_STONESHELL(true, 0.72f, 0.72f, 0.80f, 1.05f),
    /** Old blood for gravebloom Elites: the ground keeps what they spill. */
    ELITE_GRAVEBLOOM(true, 0.72f, 0.20f, 0.24f, 1.05f),
    /** Pale chitin for swarmcall Elites: a door standing open in a body. */
    ELITE_SWARMCALL(true, 0.85f, 0.95f, 0.70f, 1.10f),
    /** Cold needle-blue for spitebarb Elites: reach is a promise. */
    ELITE_SPITEBARB(true, 0.35f, 0.70f, 1.0f, 1.15f),
    /** Broken-ground ochre for hammerfall Elites: the colour of the ground about to give. */
    ELITE_HAMMERFALL(true, 0.95f, 0.62f, 0.18f, 1.20f),
    /** Arterial red for bloodhowl Elites: the one holding the leash. */
    ELITE_BLOODHOWL(true, 1.0f, 0.18f, 0.30f, 1.15f);

    /**
     * Outline colour for an Elite affix; regulars and unknowns never glow. The mapping lives with the palette, so
     * an affix is one row in {@code EliteAffix} and one row here, and the two lists are read together.
     */
    public static VisualRarity forEliteAffix(String affixId) {
        if (affixId == null) {
            return COMMON;
        }
        return switch (affixId) {
            case "blightburst" -> ELITE_BLIGHTBURST;
            case "rootward_ward" -> ELITE_ROOTWARD;
            case "weeping_rot" -> ELITE_WEEPING;
            case "hollowmolt" -> ELITE_HOLLOWMOLT;
            case "gravemoss" -> ELITE_GRAVEMOSS;
            case "cinderhalo" -> ELITE_CINDERHALO;
            case "stoneshell" -> ELITE_STONESHELL;
            case "gravebloom" -> ELITE_GRAVEBLOOM;
            case "swarmcall" -> ELITE_SWARMCALL;
            case "spitebarb" -> ELITE_SPITEBARB;
            case "hammerfall" -> ELITE_HAMMERFALL;
            case "bloodhowl" -> ELITE_BLOODHOWL;
            default -> COMMON;
        };
    }

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

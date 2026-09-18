package com.amirrezahadipoor.herodefense.model;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.RunStrings;

/**
 * An omen: a deterministic twist on an ordinary wave (roadmap R3.4, "wave modifiers").
 *
 * <p>Every wave already differs by its spawn count and its place on the curve, but nothing ever surprised a player
 * twice: wave 43 and wave 143 are the same wave with bigger numbers. An omen makes a subset of the regular waves
 * play differently without adding a single new asset -- more of them, harder to fell, hitting harder, or closing
 * faster -- and pays a little more for the trouble, so an omen wave is a decision rather than a punishment.
 *
 * <p>Omens are derived, never stored: {@link #forWave} is a pure function of the run seed and the wave number, so a
 * save that resumes mid-run resumes the same omen, and nothing has to be encoded or repaired. Boss waves and the
 * first two waves are never omen waves, because the opening should teach the plain game and a boss already has its
 * own script.
 */
public enum WaveModifier {
    NONE(null, null, 1f, 1f, 1f, 1f, 1f),
    SWARM(RunStrings.OMEN_SWARM, RunStrings.OMEN_SWARM_DETAIL, 1.25f, 1f, 1f, 1f, 1.25f),
    IRON_HIDE(RunStrings.OMEN_IRON_HIDE, RunStrings.OMEN_IRON_HIDE_DETAIL, 1f, 1.15f, 1f, 1f, 1.25f),
    BLOODRUSH(RunStrings.OMEN_BLOODRUSH, RunStrings.OMEN_BLOODRUSH_DETAIL, 1f, 1f, 1.12f, 1f, 1.25f),
    QUICKSTEP(RunStrings.OMEN_QUICKSTEP, RunStrings.OMEN_QUICKSTEP_DETAIL, 1f, 1f, 1f, 1.10f, 1.25f);

    /** One omen wave every six waves, on top of the pre-existing elite cadence. */
    public static final int OMEN_PERIOD = 6;
    /** The first omen wave; waves 1-2 stay plain. */
    public static final int FIRST_OMEN_WAVE = 3;
    /** Paid on top of an ordinary kill's coins, so a heavier wave is also a richer one. */
    private static final long OMEN_SALT = 0x0DE0A11L;

    /** Null for {@link #NONE}, which is the absence of an omen and has no words to draw. */
    private final RunStrings label;
    private final RunStrings detail;
    private final float enemyCountMultiplier;
    private final float healthMultiplier;
    private final float damageMultiplier;
    private final float speedMultiplier;
    private final float coinMultiplier;

    WaveModifier(
        RunStrings label,
        RunStrings detail,
        float enemyCountMultiplier,
        float healthMultiplier,
        float damageMultiplier,
        float speedMultiplier,
        float coinMultiplier
    ) {
        this.label = label;
        this.detail = detail;
        this.enemyCountMultiplier = enemyCountMultiplier;
        this.healthMultiplier = healthMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.speedMultiplier = speedMultiplier;
        this.coinMultiplier = coinMultiplier;
    }

    /** The omen's name in the language in force, or empty for a wave that is not an omen. */
    public String label() {
        return label == null ? "" : GameLocale.text(label);
    }

    /** What the omen changes, in the language in force; the HUD draws it beside the name. */
    public String detail() {
        return detail == null ? "" : GameLocale.text(detail);
    }

    public boolean isOmen() {
        return this != NONE;
    }

    public float enemyCountMultiplier() {
        return enemyCountMultiplier;
    }

    public float healthMultiplier() {
        return healthMultiplier;
    }

    public float damageMultiplier() {
        return damageMultiplier;
    }

    public float speedMultiplier() {
        return speedMultiplier;
    }

    public float coinMultiplier() {
        return coinMultiplier;
    }

    /** True when this wave number carries an omen at all, before the seed chooses which one. */
    public static boolean isOmenWave(int waveNumber) {
        return waveNumber >= FIRST_OMEN_WAVE
            && Math.floorMod(waveNumber - FIRST_OMEN_WAVE, OMEN_PERIOD) == 0
            && waveNumber % 5 != 0;
    }

    /** The omen of a wave: a pure function of the run seed and the wave number. */
    public static WaveModifier forWave(long seed, int waveNumber) {
        return forWave(seed, waveNumber, true);
    }

    public static WaveModifier forWave(long seed, int waveNumber, boolean omensEnabled) {
        if (!omensEnabled || !isOmenWave(waveNumber)) {
            return NONE;
        }
        WaveModifier[] omens = {SWARM, IRON_HIDE, BLOODRUSH, QUICKSTEP};
        return omens[Math.floorMod(mix(seed, waveNumber), omens.length)];
    }

    private static long mix(long seed, int waveNumber) {
        long value = seed + OMEN_SALT * (waveNumber * 131L + 1L);
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return value;
    }
}

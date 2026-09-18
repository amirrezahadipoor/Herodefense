package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.WaveModifier;
import com.amirrezahadipoor.herodefense.trials.TrialEffects;

/**
 * The policy that decides which waves carry an omen (roadmap R3.4).
 *
 * <p>{@link WaveModifier} owns the flavour -- the seed's choice of twist and what each twist multiplies. This class
 * owns the rules about <em>whether</em> and <em>where</em> a twist may land:
 *
 * <ul>
 *   <li><b>Whether:</b> only a run that drafted the Hollow Omens trial has omens at all. The untrialled run is the
 *       run every balance gate measures, so the default run stays bit-identical to what was measured before this
 *       feature existed -- and the trial carries its own band in the trial gate like the other twelve.
 *   <li><b>Where:</b> never two twists at once. A boss wave already has a script and an elite wave already carries
 *       triple-health enemies, so an omen on top of either stacks two surprises into one wave and shows up as
 *       exactly the kind of single-wave spike the balance gates exist to catch.
 * </ul>
 */
public final class WaveOmens {

    private WaveOmens() {
    }

    /** The omen of a wave, or {@link WaveModifier#NONE}: a pure function of the run state and the wave number. */
    public static WaveModifier of(GameState state, int waveNumber) {
        if (state == null) {
            return WaveModifier.NONE;
        }
        return of(state.runSeed, waveNumber, state.ascensionTier,
            TrialEffects.omensEnabled(state.activeTrials));
    }

    public static WaveModifier of(long seed, int waveNumber, int ascensionTier, boolean omensEnabled) {
        if (!omensEnabled) {
            return WaveModifier.NONE;
        }
        if (waveNumber % 5 == 0) {
            return WaveModifier.NONE;
        }
        if (EnemyWaveSpawner.isEliteWave(waveNumber, ascensionTier)) {
            return WaveModifier.NONE;
        }
        return WaveModifier.forWave(seed, waveNumber, true);
    }
}

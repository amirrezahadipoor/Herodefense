package com.amirrezahadipoor.herodefense.balance;

/**
 * One wave of a simulated run, as the balance gates read it (roadmap R4.1/R4.4).
 *
 * <p>It lives in its own file because it is a published shape: the gates, the playtest capture and the generated
 * tables in {@code docs/BALANCE.md} all read these ten fields, and the simulator's own file had grown past the
 * architecture ratchet carrying them.
 */
public record WaveSample(
    int wave,
    float startingHealth,
    float startingMaxHealth,
    float remainingHealth,
    float maximumHealth,
    float grossDamageTaken,
    float damageFraction,
    float dpsToEnemyHpRatio,
    float clearTimeSeconds,
    boolean timedOut
) {
}

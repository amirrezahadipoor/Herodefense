package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.Boss;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Eight fight scripts have to be eight real fights, not eight names. */
final class BossFightScriptTest {

    @Test
    void thereAreAtLeastEightScriptsAndNoTwoShareTheSameParameters() {
        assertTrue(BossFightScript.values().length >= 8, "the audit asked for eight distinct fight scripts");

        Set<String> parameterSets = new LinkedHashSet<>();
        for (BossFightScript script : BossFightScript.values()) {
            parameterSets.add(script.attackIntervalMultiplier() + "/" + script.movementMultiplier() + "/"
                + script.attackRangeMultiplier() + "/" + script.telegraphSeconds() + "/"
                + script.telegraphScale() + "/" + script.enrageHealthRatio() + "/"
                + script.enrageTellScale() + "/" + script.doubleStrike());
        }
        assertEquals(BossFightScript.values().length, parameterSets.size(),
            "every script must differ in how it warns or how it hits");
    }

    /**
     * The sweep-safe axes are pinned on purpose. Each of them was varied in an earlier draft of this phase and
     * moved a single-wave spike cell in {@code TrialSimulationTest} / {@code AscensionGateTest} by 15-45 %; with
     * them pinned, the cells the drafts broke reproduce the phase-87 numbers bit for bit. A later phase that
     * wants warning-length or tempo variety has to unpin these and re-run the sweep, so the pin is a test rather
     * than a comment.
     */
    @Test
    void everyScriptPinsTheAxesTheBalanceSweepCanFeel() {
        for (BossFightScript script : BossFightScript.values()) {
            assertEquals(BossFightScript.MEASURED.telegraphSeconds(), script.telegraphSeconds(), 1e-6f,
                script + " moves when the damage lands, which reshuffles the whole combat stream");
            assertEquals(1f, script.attackIntervalMultiplier(), 1e-6f, script + " tempo");
            assertEquals(1f, script.movementMultiplier(), 1e-6f, script + " legs");
            assertEquals(1f, script.attackRangeMultiplier(), 1e-6f, script + " reach");
        }
    }

    @Test
    void everyScriptStaysInsideSaneGameplayBounds() {
        for (BossFightScript script : BossFightScript.values()) {
            // The tell may never shrink to nothing: a warning the player cannot see is not a fight.
            assertTrue(script.telegraphScale() >= 0.85f && script.telegraphScale() <= 1.4f,
                script + " tell size");
            assertTrue(script.enrageHealthRatio() >= 0f && script.enrageHealthRatio() < 1f, script + " enrage");
            assertTrue(script.enrageTellScale() > 0.4f && script.enrageTellScale() <= 1f,
                script + " enrage tell");
            assertTrue(script.specialDamageMultiplier() <= 1f, script + " special damage per hit");
            assertTrue(script.hits() >= 1 && script.hits() <= 2, script + " hits per warning");
        }
    }

    @Test
    void theRosterCoversReadabilityShapeAndReactionNotOnlyNumbers() {
        boolean bigTell = false;
        boolean smallTell = false;
        boolean enrages = false;
        boolean strikesTwice = false;
        boolean strikesOnce = false;
        for (BossFightScript script : BossFightScript.values()) {
            bigTell |= script.telegraphScale() >= 1.30f;
            smallTell |= script.telegraphScale() <= 0.90f;
            enrages |= script.enrageHealthRatio() > 0f;
            strikesTwice |= script.doubleStrike();
            strikesOnce |= !script.doubleStrike();
        }
        assertTrue(bigTell && smallTell, "the roster varies how large the warning is drawn");
        assertTrue(enrages, "at least one script reacts to being wounded");
        assertTrue(strikesTwice && strikesOnce, "at least one script packs two hits into one warning");
    }

    @Test
    void everyCycleLandsExactlyTheReferenceSpecialDamage() {
        for (BossFightScript script : BossFightScript.values()) {
            assertEquals(script.attackIntervalMultiplier(), script.specialDamageMultiplier() * script.hits(), 1e-4f,
                script + " spends a different special-damage budget per cycle than the reference fight: a "
                    + "script may change the shape of a fight, never the budget the balance gates measure");
        }
    }

    @Test
    void noSingleSpecialHitLandsHarderThanTheReferenceFight() {
        for (BossFightScript script : BossFightScript.values()) {
            assertTrue(script.specialDamageMultiplier() <= 1f,
                script + " raises the biggest single special hit, and a wave damage spike is made of exactly that");
        }
    }

    @Test
    void anUnknownOrMissingScriptFallsBackToTheMeasuredFight() {
        assertEquals(BossFightScript.MEASURED, BossFightScript.of(null));
        Boss boss = boss("NOT_A_SCRIPT");
        assertEquals(BossFightScript.MEASURED, BossFightScript.of(boss),
            "a save file from before scripts existed must not break a fight");
        boss.fightScript = null;
        assertEquals(BossFightScript.MEASURED, BossFightScript.of(boss));
        boss.fightScript = "ASSASSIN";
        assertEquals(BossFightScript.ASSASSIN, BossFightScript.of(boss));
        assertEquals(BossFightScript.BULWARK, BossFightScript.ofName("BULWARK"));
        assertEquals(BossFightScript.MEASURED, BossFightScript.ofName("bulwark"), "names are matched exactly");
    }

    @Test
    void theEnrageWindowShrinksTheTellAndNeverTheWarningOrTheCycle() {
        Boss boss = boss("ENRAGED_HEART");
        boss.health = 1000f;
        boss.maxHealth = 1000f;
        float typeCooldown = 5.5f;
        assertFalse(BossFightScript.ENRAGED_HEART.enraged(boss));
        assertEquals(BossFightScript.ENRAGED_HEART.telegraphScale(),
            BossFightScript.ENRAGED_HEART.currentTellScale(boss), 1e-6f);
        float healthyCycle = BossFightScript.ENRAGED_HEART.armedCooldownSeconds(typeCooldown);

        boss.health = 400f;
        assertTrue(BossFightScript.ENRAGED_HEART.enraged(boss),
            "40 % health is the documented threshold, inclusive");
        assertEquals(BossFightScript.ENRAGED_HEART.telegraphScale()
                * BossFightScript.ENRAGED_HEART.enrageTellScale(),
            BossFightScript.ENRAGED_HEART.currentTellScale(boss), 1e-6f,
            "a wounded boss draws a smaller tell");
        assertEquals(healthyCycle, BossFightScript.ENRAGED_HEART.armedCooldownSeconds(typeCooldown), 1e-4f,
            "the enrage reaction buys readability from the tell, never tempo or damage from the budget");
        assertEquals(BossFightScript.MEASURED.telegraphSeconds(),
            BossFightScript.ENRAGED_HEART.currentTelegraphSeconds(boss), 1e-6f,
            "and it never touches how long the warning takes, which is what the sweep measures");

        boss.maxHealth = 0f;
        assertFalse(BossFightScript.ENRAGED_HEART.enraged(boss), "a degenerate boss never enrages");
        assertFalse(BossFightScript.MEASURED.enraged(boss), "the measured fight has no enrage window");
    }

    private static Boss boss(String script) {
        Boss boss = new Boss(1L, "ANCIENT_GOLEM", 0f, 0f, 1);
        boss.fightScript = script;
        return boss;
    }
}

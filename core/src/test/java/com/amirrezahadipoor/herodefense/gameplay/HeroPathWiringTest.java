package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.HeroPath;
import com.amirrezahadipoor.herodefense.render.UiIconRenderer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * The four bends of roadmap B3, measured at the stat chokepoints a run actually reads: damage, attack
 * interval, max health and focus fill. Every path must move exactly the numbers its card promises and
 * nothing else, and an unchosen path must be the classic game to the last bit -- the balance baseline
 * is built on states that never chose one.
 */
final class HeroPathWiringTest {
    private static final float TOLERANCE = 1e-4f;
    private final HeroStatCalculator stats = new HeroStatCalculator();

    private static GameState runWith(String path) {
        GameState state = GameState.newRun(3L);
        state.heroPath = path;
        return state;
    }

    @Test
    void anUnchosenPathAndTheUnboundPathAreTheClassicNumbers() {
        GameState unchosen = runWith(null);
        GameState unbound = runWith("UNBOUND");
        assertEquals(stats.damage(unchosen), stats.damage(unbound), 0f);
        assertEquals(stats.attackIntervalSeconds(unchosen), stats.attackIntervalSeconds(unbound), 0f);
        assertEquals(stats.maxHealth(unchosen), stats.maxHealth(unbound), 0f);
        assertEquals(FocusSystem.fillRateMultiplier(unchosen), FocusSystem.fillRateMultiplier(unbound), 0f);
    }

    @Test
    void rootTradesDamageForHealth() {
        GameState plain = runWith(null);
        GameState root = runWith("ROOT");
        assertEquals(stats.damage(plain) * 0.90f, stats.damage(root), TOLERANCE);
        assertEquals(stats.maxHealth(plain) * 1.25f, stats.maxHealth(root), TOLERANCE);
        assertEquals(stats.attackIntervalSeconds(plain), stats.attackIntervalSeconds(root), TOLERANCE);
        assertEquals(FocusSystem.fillRateMultiplier(plain), FocusSystem.fillRateMultiplier(root), TOLERANCE);
    }

    @Test
    void windTradesHealthForAttackSpeed() {
        GameState plain = runWith(null);
        GameState wind = runWith("WIND");
        assertEquals(stats.attackIntervalSeconds(plain) / 1.15f,
            stats.attackIntervalSeconds(wind), TOLERANCE);
        assertEquals(stats.maxHealth(plain) * 0.90f, stats.maxHealth(wind), TOLERANCE);
        assertEquals(stats.damage(plain), stats.damage(wind), TOLERANCE);
        assertEquals(FocusSystem.fillRateMultiplier(plain), FocusSystem.fillRateMultiplier(wind), TOLERANCE);
    }

    @Test
    void everyPathDrawsACommittedIcon() {
        // The draft screen draws these keys through UiIconRenderer, which resolves "ui_" + key + ".png" at
        // runtime -- a key with no committed file is a crash on the device, not a blank on a test (roadmap B3).
        Path assets = Paths.get("..", "android", "assets").normalize();
        for (HeroPath path : HeroPath.values()) {
            Path icon = assets.resolve(UiIconRenderer.assetPath(path.iconKey()));
            assertTrue(Files.isRegularFile(icon), path + " draws " + icon + ", which does not exist");
        }
    }

    @Test
    void starTradesAttackSpeedForFocus() {
        GameState plain = runWith(null);
        GameState star = runWith("STAR");
        assertEquals(FocusSystem.fillRateMultiplier(plain) * 1.25f,
            FocusSystem.fillRateMultiplier(star), TOLERANCE);
        assertEquals(stats.attackIntervalSeconds(plain) / 0.90f,
            stats.attackIntervalSeconds(star), TOLERANCE);
        assertEquals(stats.damage(plain), stats.damage(star), TOLERANCE);
        assertEquals(stats.maxHealth(plain), stats.maxHealth(star), TOLERANCE);
    }
}

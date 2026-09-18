package com.amirrezahadipoor.herodefense.balance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.GameMode;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Roadmap C3: the brief vigil keeps its floor and loses its farm.
 *
 * <p>{@code finding-brief-vigil-has-no-teeth} was accepted with a good argument -- the thirty-wave run is the
 * session that must not punish a new player, and a policy with no skill finishing it is the feature. What the
 * acceptance did not cover is the other side of the same coin: at the old formula a flawless brief clear paid
 * 26 heartwood for 30 waves while a flawless long clear paid 110 for 200, so the mode that cannot kill you was
 * the better earner per wave, and the player it served best was a veteran who had stopped being challenged.
 * The fix halves the brief run's formula, and these four assertions are the shape of that decision: the long
 * run pays exactly what it always did, the brief run pays half of the same formula, the per-wave ordering puts
 * the dangerous run on top, and both places that compute heartwood still tell the formula which mode they are
 * in -- because a preview or an award that forgets the mode is a number the player can watch disagree with the
 * one they receive.
 */
final class BriefVigilEconomyTest {

    private static final Path MAIN = Path.of("src", "main", "java", "com", "amirrezahadipoor", "herodefense");

    @Test
    void theLongVigilPaysExactlyWhatItAlwaysDid() {
        assertEquals(110, GameState.calculateHeartwoodReward(GameState.FINAL_WAVE, 0, true),
            "the progression equation's own anchor: a flawless full clear at tier zero");
        assertEquals(90, GameState.calculateHeartwoodReward(GameState.FINAL_WAVE, 0, false));
        assertEquals(26, GameState.calculateHeartwoodReward(30, 0, true, GameMode.STANDARD),
            "the three-argument call is the standard mode, so every older caller keeps its meaning");
    }

    @Test
    void theBriefVigilPaysHalfOfTheSameFormula() {
        assertEquals(13, GameState.calculateHeartwoodReward(30, 0, true, GameMode.BRIEF));
        assertEquals(3, GameState.calculateHeartwoodReward(30, 0, false, GameMode.BRIEF));
        assertEquals(18, GameState.calculateHeartwoodReward(30, 3, false, GameMode.BRIEF),
            "the halving is on the whole formula, tier bonus included, or the short run would out-earn the long"
                + " one again at higher tiers");
    }

    @Test
    void theRunThatCanKillYouIsTheBetterEarnerPerWave() {
        float briefPerWave = GameState.calculateHeartwoodReward(30, 0, true, GameMode.BRIEF) / 30f;
        float longPerWave = GameState.calculateHeartwoodReward(GameState.FINAL_WAVE, 0, true)
            / (float) GameState.FINAL_WAVE;
        assertTrue(briefPerWave < longPerWave,
            "brief pays " + briefPerWave + " a wave against the long vigil's " + longPerWave
                + ": an unloseable mode that out-earns a losable one is a farm, which is what C3 removed");
    }

    @Test
    void bothHeartwoodCallSitesStillNameTheirMode() throws IOException {
        String state = Files.readString(
            MAIN.resolve("model/GameState.java"), StandardCharsets.UTF_8);
        String gameOver = Files.readString(
            MAIN.resolve("render/GameOverOverlayRenderer.java"), StandardCharsets.UTF_8);
        assertTrue(state.contains("calculateHeartwoodReward(peakWaveReached, ascensionTier, flawless, mode)"),
            "the award stopped telling the formula which mode earned it");
        assertTrue(gameOver.contains("!state.heroDiedThisRun, state.mode)"),
            "the death screen's preview and the award would disagree for a brief run, and the player would"
                + " watch the two numbers differ");
    }
}

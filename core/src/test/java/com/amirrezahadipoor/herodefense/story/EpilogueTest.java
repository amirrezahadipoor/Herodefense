package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

final class EpilogueTest {

    @AfterEach
    void leaveTheLanguageAsItWasFound() {
        GameLocale.use(GameLanguage.ENGLISH);
    }

    @Test
    void flawlessVictoryNeedsNoDeathFewPotionsAndThirtyPercentHealth() {
        assertEquals(Epilogue.A, select(win(false, 2, 30f, 100f)));
        assertEquals(Epilogue.B, select(win(true, 2, 30f, 100f)));
        assertEquals(Epilogue.B, select(win(false, 3, 100f, 100f)));
        assertEquals(Epilogue.B, select(win(false, 0, 29.9f, 100f)));
        assertEquals(Epilogue.A, select(win(false, 0, 30f, 100f)));
    }

    @Test
    void lossesSplitByGameOverWave() {
        assertEquals(Epilogue.C, select(loss(1)));
        assertEquals(Epilogue.C, select(loss(49)));
        assertEquals(Epilogue.D, select(loss(50)));
        assertEquals(Epilogue.D, select(loss(149)));
        assertEquals(Epilogue.E, select(loss(150)));
        assertEquals(Epilogue.E, select(loss(200)));
    }

    @Test
    void everyEpilogueHoldsExactlyThreeBeats() {
        for (Epilogue epilogue : Epilogue.values()) {
            assertEquals(3, epilogue.lines().size(), epilogue.name());
        }
        assertEquals(2, Epilogue.TRANSITION.size());
    }

    @Test
    void textMatchesStoryContentVerbatim() {
        assertEquals(
            List.of(
                "Two hundred nights. Zero falls.",
                "The Night needs a new plan.",
                "Pip's plan worked! …Mostly."
            ),
            Epilogue.A.lines()
        );
        assertEquals(
            List.of(
                "Two hundred nights. All heart.",
                "I fell. I rose. I held.",
                "Best Chief ever. Don't argue."
            ),
            Epilogue.B.lines()
        );
        assertEquals(
            List.of(
                "Too soon. Too dark.",
                "Granny, keep my seat warm.",
                "We go again. Now. Up, Chief!"
            ),
            Epilogue.C.lines()
        );
        assertEquals(
            List.of(
                "Past Twig. Not past dawn.",
                "Next time, Night. Next time.",
                "Pip counted! Further next run!"
            ),
            Epilogue.D.lines()
        );
        assertEquals(
            List.of(
                "So close the dawn waved.",
                "It can wait one more run.",
                "One more run! Pip's got a NEW plan!"
            ),
            Epilogue.E.lines()
        );
        assertEquals(
            List.of(
                "The Night rests. It never leaves.",
                "Stand up. Granny stands with you."
            ),
            Epilogue.transitionLines()
        );
    }

    @Test
    void persistedEpilogueIdWinsOverLiveSelection() {
        GameState state = loss(10);
        state.epilogueId = "E";
        assertEquals(Epilogue.E, Epilogue.endingFor(state));
        state.epilogueId = "bogus";
        assertEquals(Epilogue.C, Epilogue.endingFor(state));
        state.epilogueId = "";
        assertEquals(Epilogue.C, Epilogue.endingFor(state));
    }

    private static Epilogue select(GameState state) {
        return Epilogue.select(state);
    }

    private static GameState win(boolean heroDied, int potions, float health, float maxHealth) {
        GameState state = GameState.newRun(5L);
        state.runComplete = true;
        state.waveNumber = GameState.FINAL_WAVE;
        state.heroDiedThisRun = heroDied;
        state.potionsUsedThisRun = potions;
        state.hero.maxHealth = maxHealth;
        state.hero.health = health;
        return state;
    }

    private static GameState loss(int wave) {
        GameState state = GameState.newRun(6L);
        state.waveNumber = wave;
        state.hero.alive = false;
        return state;
    }
}

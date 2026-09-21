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
                "Two hundred nights. Not one step lost.",
                "The night needs a new plan.",
                "Until then, the Tree and I stand."
            ),
            Epilogue.A.lines()
        );
        assertEquals(
            List.of(
                "Two hundred nights. Every one of them close.",
                "I do not remember all of it. I remember not letting go.",
                "That is enough. It has to be."
            ),
            Epilogue.B.lines()
        );
        assertEquals(
            List.of(
                "Not even the middle.",
                "The Tree fell quiet so early. It should not have.",
                "Next time it is loud."
            ),
            Epilogue.C.lines()
        );
        assertEquals(
            List.of(
                "So close to the second root.",
                "I went farther than before. Far is not far enough.",
                "Again."
            ),
            Epilogue.D.lines()
        );
        assertEquals(
            List.of(
                "One tree stood when I fell. That counts.",
                "The night paid for this run. It just lasted a little longer than me.",
                "Next time it pays for everything."
            ),
            Epilogue.E.lines()
        );
        assertEquals(
            List.of(
                "The night is not gone. It is quiet, learning how to fall again.",
                "Stand up. The Tree is still standing."
            ),
            Epilogue.transitionLines()
        );
    }

    @Test
    void everyEpilogueSpeaksPersianWhenPersianIsAsked() {
        GameLocale.use(GameLanguage.PERSIAN);
        assertEquals(List.of("دویست شب. بی‌آنکه گامی از دست بدهم.",
            "شب به نقشهٔ تازه‌ای نیاز دارد.",
            "تا آن زمان، من و درخت می‌ایستیم."), Epilogue.A.lines());
        assertEquals(
            List.of("شب نرفته. ساکت است. دارد دوباره یاد می‌گیرد چگونه بریزد.",
                "بلند شو. درخت هنوز ایستاده است."),
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

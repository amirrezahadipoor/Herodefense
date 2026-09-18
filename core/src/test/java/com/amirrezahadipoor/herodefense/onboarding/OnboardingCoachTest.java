package com.amirrezahadipoor.herodefense.onboarding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Roadmap R7.1: the coached steps — six since A1 added the walk — and the two rules that make them safe to ship:
 * a step ends on its own action or on its own budget, never on anything else.
 */
final class OnboardingCoachTest {

    private static final float STEP = 0.25f;

    @Test
    void theWholeLessonFitsInAMinute() {
        assertEquals(60f, OnboardingStep.totalSeconds(), 0.001f,
            "the onboarding is a sixty-second lesson by definition: seven steps, no slack -- A5 added the"
                + " brace lesson and paid for it out of the passive steps' budgets, not out of the minute");
        assertTrue(OnboardingStep.values().length >= 4, "four or more distinct lessons, not one wall of text");
    }

    @Test
    void everyStepWaitsForItsOwnTouchActionAndNothingElse() {
        OnboardingCoach coach = new OnboardingCoach();
        coach.begin();
        for (OnboardingStep step : OnboardingStep.values()) {
            assertEquals(step, coach.currentStep(), "steps are taught in the fixed order");
            for (OnboardingAction other : OnboardingAction.values()) {
                if (other == step.action()) continue;
                coach.notify(other);
                assertEquals(step, coach.currentStep(),
                    "a " + other + " may not complete the " + step.id() + " lesson");
            }
            coach.notify(step.action());
        }
        assertTrue(coach.finished(), "performing every lesson's own action ends the sequence");
        assertFalse(coach.skipped(), "and it ends as taught, not as skipped");
    }

    @Test
    void everyActionInTheVocabularyIsTaughtBySomeStep() {
        Set<OnboardingAction> taught = EnumSet.noneOf(OnboardingAction.class);
        Set<String> ids = new HashSet<>();
        for (OnboardingStep step : OnboardingStep.values()) {
            assertTrue(taught.add(step.action()),
                "two steps may not wait for the same action: " + step.action());
            assertTrue(ids.add(step.id()), "step ids are unique: " + step.id());
            assertFalse(step.line().isBlank(), "every step says something");
            assertFalse(step.hint().isBlank(), "every step says where to do it");
            assertTrue(step.budgetSeconds() > 0f, "every step has a budget");
        }
        assertEquals(EnumSet.allOf(OnboardingAction.class), taught,
            "the action vocabulary has no orphans: each one is something a step teaches");
    }

    @Test
    void aPlayerWhoIgnoresTheCoachIsNeverTrappedByIt() {
        OnboardingCoach coach = new OnboardingCoach();
        coach.begin();
        for (int tick = 0; tick < 400 && coach.active(); tick++) {
            coach.update(STEP);
        }
        assertFalse(coach.active(), "the clock alone ends the lesson");
        assertTrue(coach.finished());
        assertFalse(coach.skipped());
        assertTrue(coach.elapsedSeconds() <= OnboardingStep.totalSeconds() + STEP,
            "and it ends within the lesson's own budget, not later: " + coach.elapsedSeconds());
        assertEquals(OnboardingStep.values().length - 1, coach.currentStep().ordinal(),
            "the last line is the one left on screen when the budget runs out");
    }

    @Test
    void aStepLeavesWhenItsOwnBudgetRunsOutAndNotBefore() {
        OnboardingCoach coach = new OnboardingCoach();
        coach.begin();
        OnboardingStep first = coach.currentStep();
        coach.update(first.budgetSeconds() * 0.5f);
        assertEquals(first, coach.currentStep(), "half a budget is not a budget");
        assertTrue(coach.stepProgress() > 0.4f && coach.stepProgress() < 0.6f,
            "the progress the banner draws is the step's own clock: " + coach.stepProgress());
        coach.update(first.budgetSeconds() * 0.5f);
        assertEquals(1, coach.currentStep().ordinal(), "and one budget's worth moves the lesson on");
        assertEquals(0f, coach.secondsOnStep(), 0.0001f, "the next step starts its own clock at zero");
    }

    @Test
    void skippingEndsItImmediatelyAndIsRecordedAsASkip() {
        OnboardingCoach coach = new OnboardingCoach();
        coach.begin();
        coach.notify(OnboardingStep.values()[0].action());
        int taught = coach.completedCount();
        coach.skip();
        assertFalse(coach.active());
        assertTrue(coach.skipped(), "a skipped lesson is not a finished one, and the difference is recorded");
        assertTrue(coach.finished());
        assertEquals(taught, coach.completedCount(), "and the steps already taught stay counted");
        coach.skip();
        assertTrue(coach.skipped(), "skipping twice is harmless");
    }

    @Test
    void theClockKeepsRunningWhileALessonIsOnScreen() {
        OnboardingCoach coach = new OnboardingCoach();
        coach.begin();
        coach.update(3f);
        coach.notify(OnboardingStep.values()[0].action());
        coach.update(2f);
        assertEquals(5f, coach.elapsedSeconds(), 0.0001f,
            "elapsed time is the whole lesson's, not the current step's");
    }

    @Test
    void beginningTwiceDoesNotRestartTheLessonInProgress() {
        OnboardingCoach coach = new OnboardingCoach();
        coach.begin();
        coach.update(4f);
        coach.notify(OnboardingStep.values()[0].action());
        coach.begin();
        coach.notify(OnboardingStep.values()[0].action());
        coach.update(4f);
        assertEquals(1, coach.currentStep().ordinal(),
            "a second begin() on a running lesson leaves it where it was rather than replaying step one");
    }
}

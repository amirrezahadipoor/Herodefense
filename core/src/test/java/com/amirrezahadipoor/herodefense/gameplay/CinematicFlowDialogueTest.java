package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.SpeechVoice;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.StoryStrings;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.rewards.BossRewardCardSystem;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The ceremonies speak through the same box the arena's beats do: the opening's lines type out in
 * Pip's chirp, the planting's beats in the speaker's own, and a line the scene has moved off leaves the
 * box. The blips are the box's own, counted as the line types.
 */
final class CinematicFlowDialogueTest {

    private final List<AudioCue> played = new ArrayList<>();

    private static final class Host implements CinematicFlow.Host {
        private final GameState state;

        Host(GameState state) {
            this.state = state;
        }

        @Override
        public GameState gameState() {
            return state;
        }

        @Override
        public void saveNow() {
        }
    }

    private static void advance(CinematicFlow cinematic, float seconds) {
        float remaining = seconds;
        while (remaining > 0f) {
            float step = Math.min(0.05f, remaining);
            remaining -= step;
            cinematic.update(step);
        }
    }

    @Test
    void theOpeningLinesTypeOutInTheBoxInPipsVoice() {
        GameState state = GameState.newRun(21L);
        GameFlowController flow = new GameFlowController();
        flow.transitionTo(GameScreenState.PLAYING);
        CinematicFlow cinematic = makeFlow(
            state, flow, new OpeningCinematic(), new PlantingCeremony(),
            new WaveLifecycleSystem(
                new EnemyWaveSpawner(new EnemyFactory()),
                new BossWaveSpawner(new BossFactory()),
                new BossRewardCardSystem(),
                new ContinuousWaveRun()
            )
        );

        cinematic.beginOpening();
        advance(cinematic, 0.5f); // still pushing in: no line yet
        assertFalse(cinematic.dialogue().active(), "the box is empty while the camera moves");

        advance(cinematic, OpeningCinematic.ZOOM_IN_SECONDS - 0.5f + 0.1f); // into LINE_ONE
        String firstLine = OpeningCinematic.linesForTier(0)[0];
        assertEquals(firstLine, cinematic.dialogue().text(), "the opening line is in the box");
        assertEquals(SpeechVoice.PIP, cinematic.dialogue().voice(), "spoken in Pip's chirp");
        assertTrue(cinematic.dialogue().typing(), "and it is still typing, under its blips");

        advance(cinematic, OpeningCinematic.LINE_ONE_SECONDS);
        assertEquals(OpeningCinematic.linesForTier(0)[1], cinematic.dialogue().text(),
            "the next line replaces the first when its phase opens");

        advance(cinematic, OpeningCinematic.LINE_TWO_SECONDS + OpeningCinematic.LINE_THREE_SECONDS
            + OpeningCinematic.ZOOM_OUT_SECONDS + 0.2f);
        assertFalse(cinematic.dialogue().active(), "a line the scene has moved off leaves the box");
        assertEquals(GameScreenState.PLAYING, flow.state(), "and the run resumes");
    }

    @Test
    void thePlantingBeatsTypeInTheSpeakerOfEachPhaseAndTheGroveSpeaksGreen() {
        GameState state = GameState.newRun(21L);
        state.plantedTreesCount = 1; // the wave-100 full ceremony, the only one that grows
        GameFlowController flow = new GameFlowController();
        flow.transitionTo(GameScreenState.PLAYING);
        CinematicFlow cinematic = makeFlow(
            state, flow, new OpeningCinematic(), new PlantingCeremony(),
            new WaveLifecycleSystem(
                new EnemyWaveSpawner(new EnemyFactory()),
                new BossWaveSpawner(new BossFactory()),
                new BossRewardCardSystem(),
                new ContinuousWaveRun()
            )
        );

        cinematic.beginPlantingCeremony();
        advance(cinematic, 0.3f);
        assertEquals(GameLocale.text(StoryStrings.CEREMONY_WALK_OUT), cinematic.dialogue().text());
        assertEquals(SpeechVoice.TREE, cinematic.dialogue().voice(), "the walk-out is Granny's line");

        advance(cinematic, PlantingCeremony.WALK_OUT_SECONDS - 0.3f + 0.1f);
        assertEquals(GameLocale.text(StoryStrings.CEREMONY_PLANT), cinematic.dialogue().text(),
            "each phase moves its own line into the box");

        advance(cinematic, PlantingCeremony.PLANT_SECONDS + PlantingCeremony.WATER_SECONDS + 0.1f);
        assertEquals(GameLocale.text(StoryStrings.CEREMONY_GROW), cinematic.dialogue().text(),
            "the growth beat is the Tree's line");
        assertEquals(SpeechVoice.TREE, cinematic.dialogue().voice(),
            "and it types in the Tree's own voice, not the Hero's");

        advance(cinematic, PlantingCeremony.GROW_SECONDS + PlantingCeremony.WALK_BACK_SECONDS + 0.3f);
        assertFalse(cinematic.dialogue().active(), "the walk-back ends and the box clears with it");
    }

    private CinematicFlow makeFlow(
        GameState state, GameFlowController flow, OpeningCinematic opening, PlantingCeremony ceremony,
        WaveLifecycleSystem waves
    ) {
        return new CinematicFlow(
            new Host(state), flow, opening, ceremony, waves, new ParticleSystem(),
            new HeroAnimationController(), null, played::add
        );
    }
}

package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.SpeechVoice;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.presentation.RunPresentationSystem;
import com.amirrezahadipoor.herodefense.story.BossIntros;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The watch-only intro before a boss wave: the boss walks in as a prop, the box talks, the boss
 * walks back out, and only then does the fight spawn. Driven through a recording host, headless.
 */
final class CinematicFlowBossIntroTest {

    private final RecordingHost host = new RecordingHost();
    private final GameFlowController flow = new GameFlowController();
    private final OpeningCinematic opening = new OpeningCinematic();
    private final PlantingCeremony planting = new PlantingCeremony();
    private final BossIntroCinematic bossIntro = new BossIntroCinematic();
    private final ParticleSystem particles = new ParticleSystem();
    private final ScreenShakeSystem shake = new ScreenShakeSystem();
    private final List<AudioCue> played = new ArrayList<>();
    private final RunPresentationSystem presentation = new RunPresentationSystem(
        particles, shake, new CodexSystem(), new RecordingBeats()
    );
    private final WaveLifecycleSystem waves =
        new WaveLifecycleSystem(new EnemyWaveSpawner(new EnemyFactory()), new ContinuousWaveRun());
    private final CinematicFlow cinematic = new CinematicFlow(
        host, flow, opening, planting, waves, particles, new HeroAnimationController(),
        presentation, played::add, bossIntro, shake
    );

    @Test
    void beginOpensTheCinematicWithAPropOnItsEdge() {
        GameState state = deferredBossWave(5L, 5);

        cinematic.beginBossIntro();

        assertEquals(GameScreenState.CINEMATIC, flow.state());
        assertTrue(bossIntro.isActive());
        assertEquals("ANCIENT_GOLEM", bossIntro.bossType());
        assertEquals(1, bossIntro.meeting());
        assertEquals(1, state.aliveBosses.size());
        Boss prop = state.aliveBosses.get(0);
        assertEquals(-EnemyWaveSpawner.EDGE_OFFSET, prop.x, 0.001f);
        assertFalse(prop.entrancePresented, "the prop is scenery, never presented");
    }

    @Test
    void thePropWalksToItsMarkAndBackOut() {
        GameState state = deferredBossWave(6L, 5);
        cinematic.beginBossIntro();
        Boss prop = state.aliveBosses.get(0);

        advance(BossIntroCinematic.ENTER_SECONDS * 0.5f + 0.05f);
        assertEquals(55f, prop.x, 5f, "mid-entrance the prop is between its edge and its mark");

        advance(BossIntroCinematic.ENTER_SECONDS + BossIntroCinematic.TITLE_SECONDS);
        assertEquals(BossIntroCinematic.Phase.TALK, bossIntro.phase());
        assertEquals(150f, prop.x, 0.01f, "the talk plays on the mark");
        assertEquals(600f, prop.y, 0.01f);

        advance(4 * BossIntroCinematic.TALK_LINE_SECONDS + BossIntroCinematic.COMEBACK_SECONDS + 0.01f);
        assertEquals(BossIntroCinematic.Phase.EXIT, bossIntro.phase());
        advance(0.1f); // two more steps down the walk-out, still on stage
        assertTrue(prop.x > -40f && prop.x < 150f, "the walk-out heads back to the edge");
    }

    @Test
    void theBoxSpeaksTheCardThenTheTalkThenTheComeback() {
        deferredBossWave(7L, 5);
        cinematic.beginBossIntro();

        advance(BossIntroCinematic.ENTER_SECONDS + 0.1f);
        assertEquals(BossIntroCinematic.Phase.TITLE, bossIntro.phase());
        assertEquals(BossIntros.titleFor("ANCIENT_GOLEM"), cinematic.dialogue().text());
        assertEquals(SpeechVoice.BOSS, cinematic.dialogue().voice());

        advance(BossIntroCinematic.TITLE_SECONDS + 0.1f);
        assertEquals(BossIntroCinematic.Phase.TALK, bossIntro.phase());
        assertEquals(
            GameLocale.text(BossIntros.talkKey("ANCIENT_GOLEM", 1, 0)),
            cinematic.dialogue().text()
        );

        advance(4 * BossIntroCinematic.TALK_LINE_SECONDS + 0.1f);
        assertEquals(BossIntroCinematic.Phase.COMEBACK, bossIntro.phase());
        assertEquals(
            GameLocale.text(BossIntros.comebackKey("ANCIENT_GOLEM", 1)),
            cinematic.dialogue().text()
        );
        assertEquals(SpeechVoice.PIP, cinematic.dialogue().voice(), "Pip answers back in his chirp");
    }

    @Test
    void finishingTradesThePropForTheFight() {
        GameState state = deferredBossWave(8L, 5);
        cinematic.beginBossIntro();
        Boss prop = state.aliveBosses.get(0);

        runToHandoff();

        assertEquals(GameScreenState.PLAYING, flow.state());
        assertTrue(state.waveActive);
        assertFalse(state.bossIntroPending);
        assertEquals(1, state.aliveBosses.size());
        Boss fighter = state.aliveBosses.get(0);
        assertNotSame(prop, fighter, "the last frame trades the prop for the fight's own boss");
        assertTrue(fighter.entrancePresented);
        assertFalse(played.isEmpty(), "the entrance voice plays at the hand-off");
        assertEquals(1, host.reflections, "the banner shows when the fight starts");
        assertEquals(1, host.saves);
    }

    @Test
    void skipHandsOffFromTheMiddle() {
        GameState state = deferredBossWave(9L, 5);
        cinematic.beginBossIntro();
        advance(1f);

        bossIntro.skip();
        cinematic.update(0.05f);

        assertEquals(GameScreenState.PLAYING, flow.state());
        assertTrue(state.waveActive);
        assertEquals(1, state.aliveBosses.size());
    }

    @Test
    void knightTripsOnHisRepeatEntrance() {
        deferredBossWave(10L, 60);
        cinematic.beginBossIntro();

        assertEquals("VOID_KNIGHT", bossIntro.bossType());
        assertEquals(2, bossIntro.meeting());
        assertTrue(shake.active(), "the scripted stumble shakes the screen");
        assertFalse(particles.particles().isEmpty(), "and kicks up dust");
    }

    @Test
    void knightWalksCleanOnHisFirstEntrance() {
        deferredBossWave(11L, 20);
        cinematic.beginBossIntro();

        assertEquals("VOID_KNIGHT", bossIntro.bossType());
        assertEquals(1, bossIntro.meeting());
        assertFalse(shake.active(), "no trip on the first meeting");
        assertTrue(particles.particles().isEmpty(), "the dust starts with the walk, not the begin");
    }

    @Test
    void repeatMeetingSkipsTheCard() {
        deferredBossWave(12L, 45);
        cinematic.beginBossIntro();

        assertEquals(2, bossIntro.meeting());
        advance(BossIntroCinematic.ENTER_SECONDS + 0.1f);
        assertEquals(BossIntroCinematic.Phase.TALK, bossIntro.phase());
        assertFalse(bossIntro.titleCard().isEmpty(), "the small plate still has its line");
    }

    @Test
    void corruptWavePlaysAnEmptyShowThenSpawns() {
        GameState state = deferredBossWave(13L, 5);
        state.bossIntroWave = 6;
        cinematic.beginBossIntro();

        assertTrue(state.aliveBosses.isEmpty(), "no prop for a wave that is not a boss wave");
        assertTrue(bossIntro.isActive(), "but the show still walks its timeline");

        runToHandoff();

        assertEquals(GameScreenState.PLAYING, flow.state());
        assertTrue(state.waveActive);
        assertEquals(1, state.aliveBosses.size());
    }

    /** A run whose boss wave advanced and deferred, waiting on its intro. */
    private GameState deferredBossWave(long seed, int waveNumber) {
        GameState state = GameState.newRun(seed);
        state.waveNumber = waveNumber;
        host.state = state;
        flow.transitionTo(GameScreenState.PLAYING);
        assertTrue(waves.startCurrentWave(state));
        assertTrue(state.bossIntroPending);
        return state;
    }

    private void runToHandoff() {
        for (int step = 0; step < 400 && flow.state() != GameScreenState.PLAYING; step++) {
            cinematic.update(0.05f);
        }
    }

    private void advance(float seconds) {
        float remaining = seconds;
        while (remaining > 0f) {
            float step = Math.min(0.05f, remaining);
            cinematic.update(step);
            remaining -= step;
        }
    }

    private static final class RecordingHost implements CinematicFlow.Host {
        private GameState state;
        private int reflections;
        private int saves;

        @Override
        public GameState gameState() {
            return state;
        }

        @Override
        public void showWaveReflection() {
            reflections++;
        }

        @Override
        public void saveNow() {
            saves++;
        }
    }

    private static final class RecordingBeats implements RunPresentationSystem.BeatSink {
        @Override
        public void showBeat(String line) {
        }

        @Override
        public void save() {
        }
    }
}

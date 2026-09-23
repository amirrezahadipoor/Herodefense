package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.AudioPlayback;
import com.amirrezahadipoor.herodefense.audio.IdentityCues;
import com.amirrezahadipoor.herodefense.audio.SpeechBlip;
import com.amirrezahadipoor.herodefense.audio.SpeechVoice;
import com.amirrezahadipoor.herodefense.presentation.DialogueBox;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.SpawnLane;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.presentation.RunPresentationSystem;
import com.amirrezahadipoor.herodefense.story.BossIntros;
import com.amirrezahadipoor.herodefense.story.CeremonyLines;

/**
 * The ceremonies a run is built around: the prologue that opens it, the planting of the grove trees every
 * fifty waves (roadmap R2.2), and the watch-only intro before every boss wave.
 *
 * <p>Extracted from {@code HeroDefenseGame}, which used to own the state machine, the water-drop accumulator and
 * the wave hand-off inline. The layer ticks that run in every state (shake, particles, floating text) stay in the
 * game; this class owns only what a ceremony decides: which tree is being planted, when the prologue ends, when
 * the ceremony hands the run back to the wave loop, and which boss walks in at that moment.
 */
public final class CinematicFlow {

    /** What the ceremony needs from the game: the run state, the wave banner, a save at the hand-off. */
    public interface Host {
        GameState gameState();

        void showWaveReflection();

        void saveNow();
    }

    /** How often a pouring ceremony drops water from the can. */
    private static final float WATER_DROP_INTERVAL_SECONDS = 0.07f;
    /** How often the intro's walking prop kicks dust off its lane's edge. */
    private static final float INTRO_DUST_INTERVAL_SECONDS = 0.16f;
    /** How far inside its lane's edge the intro prop takes its mark, in world units. */
    private static final float INTRO_MARK_INSET = 150f;

    private final Host host;
    private final GameFlowController flow;
    private final OpeningCinematic openingCinematic;
    private final PlantingCeremony plantingCeremony;
    private final WaveLifecycleSystem waveLifecycleSystem;
    private final ParticleSystem particleSystem;
    private final HeroAnimationController heroAnimationController;
    private final RunPresentationSystem presentationSystem;
    private final AudioPlayback audioManager;
    private final BossIntroCinematic bossIntroCinematic;
    private final ScreenShakeSystem screenShakeSystem;

    private float waterDropAccumulator;
    /** The intro's prop boss, walked from its edge to its mark and back out again. */
    private Boss introProp;
    private float introEdgeX;
    private float introEdgeY;
    private float introMarkX;
    private float introMarkY;
    private float introDustAccumulator;
    /** Which grove tree the running planting ceremony plants: 0 Sprout, 1 Twig, 2 Leaf. */
    private int ceremonyGroveIndex;
    /**
     * The ceremony's message box: the opening's lines and the planting's beats type out in the same
     * Undertale box the arena's beats use. It is sticky, because the scene -- not a reading timer --
     * moves each line off.
     */
    private final DialogueBox dialogue;

    public CinematicFlow(
        Host host,
        GameFlowController flow,
        OpeningCinematic openingCinematic,
        PlantingCeremony plantingCeremony,
        WaveLifecycleSystem waveLifecycleSystem,
        ParticleSystem particleSystem,
        HeroAnimationController heroAnimationController,
        RunPresentationSystem presentationSystem,
        AudioPlayback audioManager,
        BossIntroCinematic bossIntroCinematic,
        ScreenShakeSystem screenShakeSystem
    ) {
        this.host = host;
        this.flow = flow;
        this.openingCinematic = openingCinematic;
        this.plantingCeremony = plantingCeremony;
        this.waveLifecycleSystem = waveLifecycleSystem;
        this.particleSystem = particleSystem;
        this.heroAnimationController = heroAnimationController;
        this.presentationSystem = presentationSystem;
        this.audioManager = audioManager;
        this.bossIntroCinematic = bossIntroCinematic;
        this.screenShakeSystem = screenShakeSystem;
        // The prop starts absent: only a boss intro puts one on stage.
        this.introProp = null;
        this.dialogue = new DialogueBox(audioManager);
        this.dialogue.setSticky(true);
    }

    /** The ceremony's message box, for the frame to draw. */
    public DialogueBox dialogue() {
        return dialogue;
    }

    /** Snapshots the run's opening tier, then plays that tier's lines. */
    public void beginOpening() {
        GameState state = host.gameState();
        state.openingTier = state.ascensionTier;
        openingCinematic.begin(state.openingTier);
        dialogue.clear();
    }

    /**
     * Starts the ceremony for the wave that just ended. Waves 50, 100 and 150 plant a named grove tree; every
     * other ceremony replays the shortest one.
     */
    public void beginPlantingCeremony() {
        GameState state = host.gameState();
        flow.transitionTo(GameScreenState.CINEMATIC);
        int groveIndex = Math.max(0, Math.min(2, state.plantedTreesCount));
        if (state.ceremonyPending) {
            int pendingWave = state.waveNumber - 1;
            if (pendingWave == 50) groveIndex = 0;
            else if (pendingWave == 100) groveIndex = 1;
            else if (pendingWave == 150) groveIndex = 2;
        }
        boolean shortCeremony = groveIndex == 0 || groveIndex == 2;
        ceremonyGroveIndex = groveIndex;
        plantingCeremony.begin(shortCeremony, groveIndex);
        state.anchorHeroAtArenaCenter();
        dialogue.clear();
    }

    /**
     * Starts the pending wave's watch-only intro: the boss spawns as a prop on its lane's edge
     * and walks to its mark while the box talks. Combat never ticks in CINEMATIC, so the prop
     * is scenery with hit points until the last frame trades it for the fight's own boss.
     */
    public void beginBossIntro() {
        GameState state = host.gameState();
        flow.transitionTo(GameScreenState.CINEMATIC);
        introProp = waveLifecycleSystem.spawnBossIntroProp(state);
        String bossType = introProp == null || introProp.bossType == null ? "" : introProp.bossType;
        bossIntroCinematic.begin(bossType, BossIntros.meetingForWave(state.bossIntroWave));
        if (introProp != null) {
            introEdgeX = introProp.x;
            introEdgeY = introProp.y;
            markIntroStage(state.bossIntroWave);
            positionIntroProp();
            if (bossIntroCinematic.bossTrips()) {
                screenShakeSystem.triggerBossTrip();
                particleSystem.emitWalkDust(introProp.x, introProp.y);
            }
        }
        dialogue.clear();
    }

    /** The mark the prop takes: inside its own lane's edge, facing the anchored Hero. */
    private void markIntroStage(int waveNumber) {
        SpawnLane lane = SpawnLane.fromIndex(waveNumber / 5 - 1);
        switch (lane) {
            case LEFT -> {
                introMarkX = INTRO_MARK_INSET;
                introMarkY = WorldLayout.HERO_CENTER_Y;
            }
            case RIGHT -> {
                introMarkX = WorldLayout.REFERENCE_WIDTH - INTRO_MARK_INSET;
                introMarkY = WorldLayout.HERO_CENTER_Y;
            }
            case SOUTH -> {
                introMarkX = WorldLayout.HERO_CENTER_X;
                introMarkY = INTRO_MARK_INSET;
            }
            default -> throw new IllegalStateException("Unhandled boss lane: " + lane);
        }
    }

    /** Slides the prop along its walk with the cinematic's eased progress. */
    private void positionIntroProp() {
        float walk = bossIntroCinematic.walkProgress();
        introProp.x = introEdgeX + (introMarkX - introEdgeX) * walk;
        introProp.y = introEdgeY + (introMarkY - introEdgeY) * walk;
    }

    /** Presentation-only ceremony tick; the wave-101 hand-off happens once it completes. */
    public void update(float deltaSeconds) {
        GameState state = host.gameState();
        dialogue.tick(deltaSeconds, false);
        if (openingCinematic.isActive()) {
            speakLine(openingCinematic.line(), SpeechVoice.PIP);
            state.anchorHeroAtArenaCenter();
            heroAnimationController.update(state.hero, deltaSeconds);
            if (openingCinematic.update(deltaSeconds)) {
                waveLifecycleSystem.startCurrentWave(state);
                flow.transitionTo(GameScreenState.PLAYING);
                host.saveNow();
            }
            return;
        }
        if (bossIntroCinematic.isActive()) {
            speakLine(introSpokenLine(), introVoice());
            state.anchorHeroAtArenaCenter();
            heroAnimationController.update(state.hero, deltaSeconds);
            if (introProp != null) {
                positionIntroProp();
                if (bossIntroCinematic.phase() == BossIntroCinematic.Phase.ENTER) {
                    introDustAccumulator += deltaSeconds;
                    while (introDustAccumulator >= INTRO_DUST_INTERVAL_SECONDS) {
                        introDustAccumulator -= INTRO_DUST_INTERVAL_SECONDS;
                        particleSystem.emitWalkDust(introProp.x, introProp.y);
                    }
                } else {
                    introDustAccumulator = 0f;
                }
            }
            if (bossIntroCinematic.update(deltaSeconds)) {
                if (introProp != null) {
                    state.aliveBosses.remove(introProp);
                    introProp = null;
                }
                waveLifecycleSystem.completeBossIntro(state);
                flow.transitionTo(GameScreenState.PLAYING);
                audioManager.play(IdentityCues.bossEntranceFor(state));
                presentationSystem.presentBossEntrance(state);
                host.showWaveReflection();
                host.saveNow();
            }
            return;
        }
        speakLine(CeremonyLines.lineFor(plantingCeremony.phase(), ceremonyGroveIndex),
            SpeechBlip.voiceFor(CeremonyLines.entryFor(plantingCeremony.phase(), ceremonyGroveIndex)));
        boolean finished = plantingCeremony.update(deltaSeconds);
        if (plantingCeremony.pouring()) {
            waterDropAccumulator += deltaSeconds;
            while (waterDropAccumulator >= WATER_DROP_INTERVAL_SECONDS) {
                waterDropAccumulator -= WATER_DROP_INTERVAL_SECONDS;
                particleSystem.emitWaterDrops(
                    plantingCeremony.heroX() + 52f, plantingCeremony.heroY() + 58f
                );
            }
        } else {
            waterDropAccumulator = 0f;
        }
        if (finished) {
            int bossesBefore = ArenaQueries.livingBossCount(state);
            waveLifecycleSystem.completePlantingCeremony(state);
            flow.transitionTo(GameScreenState.PLAYING);
            if (ArenaQueries.livingBossCount(state) > bossesBefore) {
                audioManager.play(IdentityCues.bossEntranceFor(state));
                presentationSystem.presentBossEntrance(state);
            }
            host.saveNow();
        }
    }

    /** What the intro's box reads: the title card while it slams in, the spoken line after. */
    private String introSpokenLine() {
        if (bossIntroCinematic.phase() == BossIntroCinematic.Phase.TITLE) {
            return bossIntroCinematic.titleCard();
        }
        return bossIntroCinematic.line();
    }

    /** The Night Shift's low square for the boss's own lines, Pip's chirp for the comeback. */
    private SpeechVoice introVoice() {
        if (bossIntroCinematic.phase() == BossIntroCinematic.Phase.COMEBACK) {
            return SpeechVoice.PIP;
        }
        return SpeechVoice.BOSS;
    }

    /**
     * Puts the scene's current line in the box, in its speaker's voice. A new line types from its first
     * character; a line the scene has moved off clears the box. The box's tick runs on the frame the
     * ceremony runs in, so the blips tap out over time, not at once (roadmap ST-voice).
     */
    private void speakLine(String line, SpeechVoice voice) {
        if (line == null || line.isBlank()) {
            if (dialogue.active()) {
                dialogue.clear();
            }
            return;
        }
        if (!line.equals(dialogue.text())) {
            dialogue.speak(line, voice, DialogueBox.Source.BEAT);
        }
    }
}

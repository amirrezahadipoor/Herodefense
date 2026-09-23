package com.amirrezahadipoor.herodefense;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.AudioFocusState;
import com.amirrezahadipoor.herodefense.audio.GameAudioManager;
import com.amirrezahadipoor.herodefense.gameplay.BossFactory;
import com.amirrezahadipoor.herodefense.gameplay.BossIntroCinematic;
import com.amirrezahadipoor.herodefense.gameplay.BossSpecialAttackSystem;
import com.amirrezahadipoor.herodefense.gameplay.BossWaveSpawner;
import com.amirrezahadipoor.herodefense.gameplay.ContinuousWaveRun;
import com.amirrezahadipoor.herodefense.gameplay.DropPickupSystem;
import com.amirrezahadipoor.herodefense.gameplay.EliteAffixSystem;
import com.amirrezahadipoor.herodefense.gameplay.EnemyFactory;
import com.amirrezahadipoor.herodefense.gameplay.EnemyMeleeAttackSystem;
import com.amirrezahadipoor.herodefense.gameplay.EnemyMovementSystem;
import com.amirrezahadipoor.herodefense.gameplay.EnemyWaveSpawner;
import com.amirrezahadipoor.herodefense.gameplay.ArenaQueries;
import com.amirrezahadipoor.herodefense.gameplay.FocusFireSystem;
import com.amirrezahadipoor.herodefense.gameplay.FocusSystem;
import com.amirrezahadipoor.herodefense.gameplay.HeroAnimationController;
import com.amirrezahadipoor.herodefense.gameplay.CombatEvent;
import com.amirrezahadipoor.herodefense.gameplay.HeroAttackUpdateResult;
import com.amirrezahadipoor.herodefense.gameplay.HeroAutoAttackSystem;
import com.amirrezahadipoor.herodefense.gameplay.HeroDamageSystem;
import com.amirrezahadipoor.herodefense.gameplay.HeroMovementSystem;
import com.amirrezahadipoor.herodefense.gameplay.HeroProgressionSystem;
import com.amirrezahadipoor.herodefense.gameplay.HeroUltimateSystem;
import com.amirrezahadipoor.herodefense.gameplay.InventoryEquipmentSystem;
import com.amirrezahadipoor.herodefense.gameplay.ItemDropSystem;
import com.amirrezahadipoor.herodefense.gameplay.KillRewardResult;
import com.amirrezahadipoor.herodefense.gameplay.KillRewardSystem;
import com.amirrezahadipoor.herodefense.gameplay.CinematicFlow;
import com.amirrezahadipoor.herodefense.gameplay.CombatSystem;
import com.amirrezahadipoor.herodefense.gameplay.SessionController;
import com.amirrezahadipoor.herodefense.gameplay.WaveDirector;
import com.amirrezahadipoor.herodefense.gameplay.WaveCompletion;
import com.amirrezahadipoor.herodefense.gameplay.OpeningCinematic;
import com.amirrezahadipoor.herodefense.gameplay.PlantingCeremony;
import com.amirrezahadipoor.herodefense.gameplay.UltimateResult;
import com.amirrezahadipoor.herodefense.gameplay.WaveLifecycleSystem;
import com.amirrezahadipoor.herodefense.input.CodexTouchController;
import com.amirrezahadipoor.herodefense.input.ScreenTouchRouter;
import com.amirrezahadipoor.herodefense.input.SystemBackKeyHandler;
import com.amirrezahadipoor.herodefense.presentation.DialogueBox;
import com.amirrezahadipoor.herodefense.presentation.FrameDriver;
import com.amirrezahadipoor.herodefense.progression.TrophyBook;
import com.amirrezahadipoor.herodefense.progression.TrophyPresenter;
import com.amirrezahadipoor.herodefense.presentation.RunPresentationSystem;
import com.amirrezahadipoor.herodefense.presentation.UltimatePresentation;
import com.amirrezahadipoor.herodefense.presentation.ScreenStateComposer;
import com.amirrezahadipoor.herodefense.input.GameOverTouchLayout;
import com.amirrezahadipoor.herodefense.input.GdxHapticFeedback;
import com.amirrezahadipoor.herodefense.input.HapticFeedback;
import com.amirrezahadipoor.herodefense.input.HudTouchLayout;
import com.amirrezahadipoor.herodefense.input.InventoryTouchController;
import com.amirrezahadipoor.herodefense.input.LevelUpTouchLayout;
import com.amirrezahadipoor.herodefense.input.MainMenuTouchLayout;
import com.amirrezahadipoor.herodefense.input.PauseTouchController;
import com.amirrezahadipoor.herodefense.input.PauseTouchLayout;
import com.amirrezahadipoor.herodefense.input.RewardCardTouchController;
import com.amirrezahadipoor.herodefense.input.SettingsTouchController;
import com.amirrezahadipoor.herodefense.input.SettingsTouchLayout;
import com.amirrezahadipoor.herodefense.input.SimulationSpeedTouchController;
import com.amirrezahadipoor.herodefense.input.StatShopTouchLayout;
import com.amirrezahadipoor.herodefense.input.TouchInputController;
import com.amirrezahadipoor.herodefense.input.TrialDraftTouchController;
import com.amirrezahadipoor.herodefense.items.StarterLoadoutSystem;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.DropCollectionStage;
import com.amirrezahadipoor.herodefense.model.DropEntity;
import com.amirrezahadipoor.herodefense.model.Enemy;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.HeroStat;
import com.amirrezahadipoor.herodefense.potions.AutoPotionSystem;
import com.amirrezahadipoor.herodefense.potions.HealthPotionSystem;
import com.amirrezahadipoor.herodefense.potions.PotionDropSystem;
import com.amirrezahadipoor.herodefense.polish.FloatingCoinTextSystem;
import com.amirrezahadipoor.herodefense.polish.FloatingDamageTextSystem;
import com.amirrezahadipoor.herodefense.polish.HitStopSystem;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.polish.TouchFeedbackSystem;
import com.amirrezahadipoor.herodefense.render.RenderStack;
import com.amirrezahadipoor.herodefense.render.SheetPayloads;
import com.amirrezahadipoor.herodefense.render.ArenaEnvironmentRenderer;
import com.amirrezahadipoor.herodefense.render.CodexOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.CombatEntityRenderer;
import com.amirrezahadipoor.herodefense.render.DialogueBoxRenderer;
import com.amirrezahadipoor.herodefense.render.PostProcessRenderer;
import com.amirrezahadipoor.herodefense.render.DisplayMetrics;
import com.amirrezahadipoor.herodefense.render.GameFonts;
import com.amirrezahadipoor.herodefense.render.ScreenEdges;
import com.amirrezahadipoor.herodefense.render.EquipmentSpriteRenderer;
import com.amirrezahadipoor.herodefense.render.FloatingCoinTextRenderer;
import com.amirrezahadipoor.herodefense.render.FloatingDamageTextRenderer;
import com.amirrezahadipoor.herodefense.render.GameOverOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.CeremonyHeroRenderer;
import com.amirrezahadipoor.herodefense.render.HeroSpriteRenderer;
import com.amirrezahadipoor.herodefense.render.OpeningCinematicRenderer;
import com.amirrezahadipoor.herodefense.render.SaplingTreeRenderer;
import com.amirrezahadipoor.herodefense.render.HudRenderer;
import com.amirrezahadipoor.herodefense.render.InventoryOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.LevelUpOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.MainMenuRenderer;
import com.amirrezahadipoor.herodefense.render.ParticleRenderer;
import com.amirrezahadipoor.herodefense.render.PauseOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.RewardCardOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.SettingsOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.StatShopOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.TouchFeedbackRenderer;
import com.amirrezahadipoor.herodefense.render.TrialDraftOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.UiFrameRenderer;
import com.amirrezahadipoor.herodefense.render.UiIconRenderer;
import com.amirrezahadipoor.herodefense.rewards.BossRewardCardSystem;
import com.amirrezahadipoor.herodefense.playtest.RunRecordStore;
import com.amirrezahadipoor.herodefense.save.LocalSaveRepository;
import com.amirrezahadipoor.herodefense.settings.GameSettings;
import com.amirrezahadipoor.herodefense.settings.LocalSettingsRepository;
import com.amirrezahadipoor.herodefense.ascension.RootNetworkSystem;
import com.amirrezahadipoor.herodefense.input.RootNetworkTouchController;
import com.amirrezahadipoor.herodefense.render.RootNetworkOverlayRenderer;
import com.amirrezahadipoor.herodefense.shop.StatShopSystem;
import com.amirrezahadipoor.herodefense.skills.SkillId;
import com.amirrezahadipoor.herodefense.skills.SkillEvolution;
import com.amirrezahadipoor.herodefense.skills.SkillShopSystem;
import com.amirrezahadipoor.herodefense.story.BossTitleCards;
import com.amirrezahadipoor.herodefense.story.CeremonyLines;
import com.amirrezahadipoor.herodefense.story.CodexSystem;
import com.amirrezahadipoor.herodefense.story.DailyGift;
import com.amirrezahadipoor.herodefense.story.MercySystem;
import com.amirrezahadipoor.herodefense.story.ReflectionLines;
import com.amirrezahadipoor.herodefense.story.EliteFragments;
import com.amirrezahadipoor.herodefense.story.Epilogue;
import com.amirrezahadipoor.herodefense.story.WhisperLines;
import com.amirrezahadipoor.herodefense.trials.TrialDraftSystem;

import java.util.Optional;

/** Android-only libGDX game loop and top-level state coordinator. */
public final class HeroDefenseGame extends ApplicationAdapter {
    private static final float MAX_FRAME_DELTA = 1f / 15f;

    private GameFlowController flow;
    private GameAudioManager audioManager;
    private AutoPotionSystem autoPotionSystem;
    private BossRewardCardSystem bossRewardCardSystem;
    private FloatingCoinTextSystem floatingCoinTextSystem;
    private FloatingDamageTextSystem floatingDamageTextSystem;
    private BossSpecialAttackSystem bossSpecialAttackSystem;
    private DropPickupSystem dropPickupSystem;
    private EnemyMeleeAttackSystem enemyMeleeAttackSystem;
    private EnemyMovementSystem enemyMovementSystem;
    private WaveLifecycleSystem waveLifecycleSystem;
    private WaveDirector waveDirector;
    private CombatSystem combatSystem;
    private SessionController sessionController;
    private FrameDriver frameDriver;
    private TrophyPresenter trophyPresenter;
    private CinematicFlow cinematicFlow;
    private HeroAnimationController heroAnimationController;
    private HeroAutoAttackSystem heroAutoAttackSystem;
    private HeroProgressionSystem heroProgressionSystem;
    private final PlantingCeremony plantingCeremony = new PlantingCeremony();
    private final OpeningCinematic openingCinematic = new OpeningCinematic();
    private final BossIntroCinematic bossIntroCinematic = new BossIntroCinematic();
    private final CodexSystem codexSystem = new CodexSystem();
    private HapticFeedback hapticFeedback;
    private HitStopSystem hitStopSystem;
    private ScreenStateComposer screenStateComposer;
    private RenderStack renderers;
    private CodexTouchController codexTouchController;
    private RunPresentationSystem presentationSystem;
    private InventoryTouchController inventoryTouchController;
    private ItemDropSystem itemDropSystem;
    private KillRewardSystem killRewardSystem;
    private EliteAffixSystem eliteAffixSystem;
    private ParticleSystem particleSystem;
    private PauseTouchController pauseTouchController;
    private PotionDropSystem potionDropSystem;
    private RewardCardTouchController rewardCardTouchController;
    private TrialDraftSystem trialDraftSystem;
    private TrialDraftTouchController trialDraftTouchController;
    private ScreenShakeSystem screenShakeSystem;
    private SettingsTouchController settingsTouchController;
    private SimulationSpeedTouchController simulationSpeedTouchController;
    private StatShopSystem statShopSystem;
    private SkillShopSystem skillShopSystem;
    private RootNetworkSystem rootNetworkSystem;
    private RootNetworkTouchController rootNetworkTouchController;
    private StatShopTouchLayout.Tab shopTab = StatShopTouchLayout.Tab.STATS;
    private TouchFeedbackSystem touchFeedbackSystem;
    private LocalSaveRepository saves;
    private RunRecordStore runRecords;
    private LocalSettingsRepository settingsRepository;
    private GameSettings settings;
    private GameState gameState;
    private boolean continueAvailable;
    private volatile boolean readyForTouch;
    private final java.util.concurrent.atomic.AtomicLong handledTouchUpCount =
        new java.util.concurrent.atomic.AtomicLong();
    private volatile float lastTouchWorldX = Float.NaN;
    private volatile float lastTouchWorldY = Float.NaN;
    private OrthographicCamera camera;
    private DisplayMetrics displayMetrics;
    private Viewport viewport;
    private float simulationSeconds;

    @Override
    public void create() {
        SheetPayloads.installForThisDevice();   // R8.1: what this device can decode, decided once
        flow = new GameFlowController();
        autoPotionSystem = new AutoPotionSystem(new HealthPotionSystem());
        HeroDamageSystem heroDamageSystem = new HeroDamageSystem();
        bossSpecialAttackSystem = new BossSpecialAttackSystem(heroDamageSystem);
        eliteAffixSystem = new EliteAffixSystem(heroDamageSystem);
        dropPickupSystem = new DropPickupSystem();
        enemyMeleeAttackSystem = new EnemyMeleeAttackSystem(heroDamageSystem);
        enemyMovementSystem = new EnemyMovementSystem();
        EnemyWaveSpawner enemyWaveSpawner = new EnemyWaveSpawner(new EnemyFactory());
        bossRewardCardSystem = new BossRewardCardSystem();
        rewardCardTouchController = new RewardCardTouchController(bossRewardCardSystem);
        trialDraftSystem = new TrialDraftSystem();
        trialDraftTouchController = new TrialDraftTouchController(trialDraftSystem);
        waveLifecycleSystem = new WaveLifecycleSystem(
            enemyWaveSpawner,
            new BossWaveSpawner(new BossFactory()),
            bossRewardCardSystem,
            new ContinuousWaveRun()
        );
        heroAnimationController = new HeroAnimationController();
        heroAutoAttackSystem = new HeroAutoAttackSystem();
        heroProgressionSystem = new HeroProgressionSystem();
        hapticFeedback = new GdxHapticFeedback();
        hitStopSystem = new HitStopSystem();
        killRewardSystem = new KillRewardSystem(heroProgressionSystem);
        codexTouchController = new CodexTouchController();
        inventoryTouchController = new InventoryTouchController(new InventoryEquipmentSystem());
        itemDropSystem = new ItemDropSystem();
        floatingCoinTextSystem = new FloatingCoinTextSystem();
        floatingDamageTextSystem = new FloatingDamageTextSystem();
        particleSystem = new ParticleSystem();
        pauseTouchController = new PauseTouchController();
        potionDropSystem = new PotionDropSystem();
        screenShakeSystem = new ScreenShakeSystem();
        presentationSystem = new RunPresentationSystem(particleSystem, screenShakeSystem, codexSystem,
            new RunPresentationSystem.BeatSink() {
                @Override public void showBeat(String line) { showStoryBeat(line); }
                @Override public void save() { saveNow(); }
            });
        settingsTouchController = new SettingsTouchController();
        simulationSpeedTouchController = new SimulationSpeedTouchController();
        statShopSystem = new StatShopSystem();
        skillShopSystem = new SkillShopSystem();
        rootNetworkSystem = new RootNetworkSystem();
        rootNetworkTouchController = new RootNetworkTouchController(rootNetworkSystem);
        touchFeedbackSystem = new TouchFeedbackSystem();
        saves = new LocalSaveRepository(Gdx.app.getPreferences(LocalSaveRepository.PREFERENCES_NAME));
        // Session records live beside the save: readable with `adb exec-out run-as` (roadmap R3.6).
        runRecords = new RunRecordStore(Gdx.files.local("playtests"));
        settingsRepository = new LocalSettingsRepository(
            Gdx.app.getPreferences(LocalSettingsRepository.PREFERENCES_NAME)
        );
        settings = settingsRepository.load();
        audioManager = new GameAudioManager(settings);
        Optional<GameState> loadedRun = saves.load();
        gameState = loadedRun.orElseGet(() -> GameState.newRun(System.currentTimeMillis()));
        continueAvailable = loadedRun.isPresent() && SessionController.canContinue(gameState);
        // A save written before trophies existed is read once here, so a returning player keeps the credit
        // their heartwood, ascensions and codex entries had already earned (roadmap R3.3).
        TrophyBook.migrate(gameState);
        new StarterLoadoutSystem().provisionOnce(gameState);
        camera = new OrthographicCamera();
        // Width is pinned to 720; tall panels reveal more arena instead of black bars.
        viewport = new ExtendViewport(
            WorldLayout.REFERENCE_WIDTH,
            DisplayMetrics.MIN_WORLD_HEIGHT,
            WorldLayout.REFERENCE_WIDTH,
            DisplayMetrics.MAX_WORLD_HEIGHT,
            camera
        );
        displayMetrics = ViewportMetrics.apply(
            viewport, camera, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight()
        );
        renderers = new RenderStack();
        screenStateComposer = new ScreenStateComposer(new ComposerHost(), camera, renderers.spriteBatch);
        cinematicFlow = new CinematicFlow(
            new CinematicHost(), flow, openingCinematic, plantingCeremony, waveLifecycleSystem, particleSystem,
            heroAnimationController, presentationSystem, audioManager, bossIntroCinematic, screenShakeSystem
        );
        sessionController = new SessionController(
            new SessionHost(), saves, flow, new StarterLoadoutSystem(), rootNetworkSystem, trialDraftSystem,
            hitStopSystem, particleSystem, floatingCoinTextSystem, floatingDamageTextSystem,
            waveLifecycleSystem, openingCinematic
        );
        combatSystem = new CombatSystem(
            heroAutoAttackSystem, bossSpecialAttackSystem, enemyMeleeAttackSystem, autoPotionSystem,
            itemDropSystem, potionDropSystem, killRewardSystem, eliteAffixSystem, dropPickupSystem,
            presentationSystem, codexSystem, particleSystem, floatingCoinTextSystem,
            floatingDamageTextSystem, hitStopSystem, screenShakeSystem, audioManager
        );
        waveDirector = new WaveDirector(
            new DirectorHost(), waveLifecycleSystem, audioManager, particleSystem,
            screenShakeSystem, presentationSystem, codexSystem
        );
        frameDriver = new FrameDriver(
            new FrameHost(), flow, audioManager, audioManager, settings, touchFeedbackSystem, inventoryTouchController,
            statShopSystem, skillShopSystem, rootNetworkSystem, hitStopSystem, screenShakeSystem, particleSystem,
            codexSystem,
            System::nanoTime
        );
        trophyPresenter = new TrophyPresenter(
            audioManager, hapticFeedback, frameDriver::showStoryBeat,
            () -> frameDriver.storyBeatLine() != null
        );
        installTouchInput();
        readyForTouch = true;
    }

    @Override
    public void resize(int width, int height) {
        displayMetrics = ViewportMetrics.apply(viewport, camera, width, height);
    }

    /** Current panel mapping, exposed for instrumentation tests. */
    public DisplayMetrics displayMetrics() {
        return displayMetrics;
    }

    @Override
    public void render() {
        frameDriver.update(Math.min(Gdx.graphics.getDeltaTime(), MAX_FRAME_DELTA));
    }

    public boolean readyForTouch() {
        return readyForTouch;
    }

    public GameScreenState screenState() {
        return flow.state();
    }

    public void transitionTo(GameScreenState state) {
        flow.transitionTo(state);
    }

    public GameState gameState() {
        return gameState;
    }

    /** Read-only test visibility; auto-sell chips still toggle only through touch. */
    public boolean autoSellEnabled(com.amirrezahadipoor.herodefense.model.ItemTier tier) {
        return settings != null && settings.autoSells(tier);
    }

    /** Read-only test visibility; inventory actions themselves still require touch. */
    public boolean inventoryOpen() {
        return inventoryTouchController != null && inventoryTouchController.isOpen();
    }

    /** Read-only test visibility; inventory selection still changes only through touch. */
    public int inventorySelectedIndex() {
        return inventoryTouchController == null ? -1 : inventoryTouchController.selectedIndex();
    }

    /** Read-only test visibility: a message box on screen (a beat, a whisper, the parting word). */
    public boolean storyDialogueActive() { return frameDriver.storyDialogueActive(); }

    /** Read-only test visibility: defeat panel buttons revealed and tappable. */
    public boolean gameOverRevealInteractive() {
        return GameOverOverlayRenderer.isInteractive(
            frameDriver.gameOverPresentationSeconds(), gameState.runComplete);
    }

    /** Read-only test visibility; action feedback still originates only from touch. */
    public String inventoryFeedbackMessage() {
        return inventoryTouchController == null ? null : inventoryTouchController.feedbackMessage();
    }

    /** Read-only test visibility; Shop feedback still originates only from touch. */
    public String shopFeedbackMessage() {
        if (shopTab == StatShopTouchLayout.Tab.SKILLS && skillShopSystem != null) {
            return skillShopSystem.feedbackMessage();
        }
        return statShopSystem == null ? null : statShopSystem.feedbackMessage();
    }

    /** Read-only test visibility of the active shop tab; changed only by touch. */
    public StatShopTouchLayout.Tab shopTab() {
        return shopTab;
    }

    /** Read-only test visibility used to confirm device touches reached libGDX coordinates. */
    public long handledTouchUpCount() {
        return handledTouchUpCount.get();
    }

    public float lastTouchWorldX() {
        return lastTouchWorldX;
    }

    public float lastTouchWorldY() {
        return lastTouchWorldY;
    }

    /** Awards kill XP and opens the touch allocation overlay whenever a level is gained. */
    public int grantHeroExperience(int experience) {
        int levelsGained = heroProgressionSystem.grantExperience(gameState, experience);
        if (levelsGained > 0) audioManager.play(AudioCue.LEVEL_UP);
        if (levelsGained > 0 && flow.state() == GameScreenState.PLAYING) {
            flow.transitionTo(GameScreenState.LEVEL_UP);
            saveNow();
        }
        return levelsGained;
    }

    @Override
    public void pause() {
        if (flow != null && flow.state() == GameScreenState.PLAYING) {
            flow.transitionTo(GameScreenState.PAUSED);
        }
        if (audioManager != null) audioManager.pauseForBackground();
        saveNow();
    }

    @Override
    public void resume() {
        if (audioManager != null) audioManager.resumeFromBackground();
    }

    /** The platform's audio-focus event (roadmap R6.4). */
    public void onAudioFocus(AudioFocusState.Event event) {
        if (audioManager != null) audioManager.onAudioFocus(event);
    }

    /** F3: platform TTS provider for lore and boss narration. */
    public void setTtsProvider(com.amirrezahadipoor.herodefense.audio.NarrationSystem.TtsProvider provider) {
        if (audioManager != null) audioManager.setTtsProvider(provider);
    }

    public com.amirrezahadipoor.herodefense.audio.NarrationSystem narrationSystem() {
        return audioManager != null ? audioManager.narration() : null;
    }

    /** G3d: accessibility bridge for TalkBack announcements. */
    public void setAccessibilityBridge(Object bridge) {
        // Platform accessibility bridge hook for TalkBack announcements
    }

    public com.amirrezahadipoor.herodefense.accessibility.ScreenReaderSystem screenReaderSystem() {
        return audioManager != null ? audioManager.screenReader() : null;
    }

    @Override
    public void dispose() {
        saveNow();
        if (saves != null) saves.close(); // B4: the disk is the writer's; a queued save must not be dropped here
        GameFonts.closeSharedFor(Gdx.app);
        if (audioManager != null) {
            audioManager.close();
        }
        if (renderers != null) {
            renderers.close();
        }
    }

    /** Writes the session record of the run that just ended; the store ignores a repeated call (roadmap R3.6). */
    private void recordRunEnd() {
        if (runRecords == null) return;
        runRecords.record(gameState, Gdx.app.getType().name(), Gdx.app.getVersion(), System.currentTimeMillis());
        com.amirrezahadipoor.herodefense.story.HollowVoice.markRunComplete(gameState);
    }
    private void saveNow() {
        if (saves != null && gameState != null) {
            trophyPresenter.announce(TrophyBook.evaluate(gameState));
            saves.save(gameState);
            continueAvailable = SessionController.canContinue(gameState);
        }
    }

    private void installTouchInput() {
        ScreenTouchRouter router = new ScreenTouchRouter(new TouchHost());
        // R7.4: catching Back is what stops the platform finishing the activity on every press. What a press
        // then does is decided in the core, where it can be tested without a device.
        Gdx.input.setCatchKey(Keys.BACK, true);
        Gdx.input.setInputProcessor(
            new InputMultiplexer(new SystemBackKeyHandler(router), new TouchInputController(viewport, router))
        );
    }

    /** Adapter the touch router uses; one line per member, so the game keeps ownership of its state. */
    private final class TouchHost implements ScreenTouchRouter.Host {
        @Override public GameAudioManager audioManager() { return audioManager; }
        @Override public CodexSystem codexSystem() { return codexSystem; }
        @Override public CodexTouchController codexTouchController() { return codexTouchController; }
        @Override public boolean continueAvailable() { return continueAvailable; }
        @Override public GameFlowController flow() { return flow; }
        @Override public float gameOverPresentationSeconds() { return frameDriver.gameOverPresentationSeconds(); }
        @Override public GameState gameState() { return gameState; }
        @Override public HapticFeedback hapticFeedback() { return hapticFeedback; }
        @Override public HeroProgressionSystem heroProgressionSystem() { return heroProgressionSystem; }
        @Override public InventoryTouchController inventoryTouchController() { return inventoryTouchController; }
        @Override public OpeningCinematic openingCinematic() { return openingCinematic; }
        @Override public PauseTouchController pauseTouchController() { return pauseTouchController; }
        @Override public PlantingCeremony plantingCeremony() { return plantingCeremony; }
        @Override public RewardCardTouchController rewardCardTouchController() { return rewardCardTouchController; }
        @Override public RootNetworkSystem rootNetworkSystem() { return rootNetworkSystem; }
        @Override public RootNetworkTouchController rootNetworkTouchController() { return rootNetworkTouchController; }
        @Override public GameSettings settings() { return settings; }
        @Override public LocalSettingsRepository settingsRepository() { return settingsRepository; }
        @Override public SettingsTouchController settingsTouchController() { return settingsTouchController; }
        @Override public SimulationSpeedTouchController simulationSpeedTouchController() { return simulationSpeedTouchController; }
        @Override public SkillShopSystem skillShopSystem() { return skillShopSystem; }
        @Override public StatShopSystem statShopSystem() { return statShopSystem; }
        @Override public StatShopTouchLayout.Tab shopTab() { return shopTab; }
        @Override public boolean storyDialogueActive() { return frameDriver.storyDialogueActive(); }
        @Override public TouchFeedbackSystem touchFeedbackSystem() { return touchFeedbackSystem; }
        @Override public TrialDraftTouchController trialDraftTouchController() { return trialDraftTouchController; }
        @Override public UiFrameRenderer uiFrameRenderer() { return renderers.uiFrameRenderer; }
        @Override public WaveLifecycleSystem waveLifecycleSystem() { return waveLifecycleSystem; }
        @Override public void setShopTab(StatShopTouchLayout.Tab tab) { shopTab = tab; }
        @Override public void advanceStoryDialogue() { frameDriver.advanceStoryDialogue(); }
        @Override public void setLastTouchWorldX(float value) { lastTouchWorldX = value; }
        @Override public void setLastTouchWorldY(float value) { lastTouchWorldY = value; }
        @Override public void countHandledTouchUp() { handledTouchUpCount.incrementAndGet(); }
        @Override public void saveNow() { HeroDefenseGame.this.saveNow(); }
        @Override public void recordRunEnd() { HeroDefenseGame.this.recordRunEnd(); }
        @Override public void startNewRunSameTier() { sessionController.startNewRunSameTier(); }
        @Override public void startBriefRun() { sessionController.startBriefRun(); }
        @Override public void ascendRun() { sessionController.ascendRun(); }
        @Override public void continueRun() { sessionController.continueRun(); }
        @Override public void beginOpening() { HeroDefenseGame.this.beginOpening(); }
        @Override public void fireUltimate() {
            UltimateResult result = new HeroUltimateSystem().fire(gameState);
            if (!result.fired()) return;
            new UltimatePresentation(particleSystem, screenShakeSystem, audioManager).release(result);
        }
        @Override public void beginPlantingCeremony() { HeroDefenseGame.this.beginPlantingCeremony(); }
        @Override public void beginBossIntro() { HeroDefenseGame.this.beginBossIntro(); }

        @Override public void focusFireAt(float worldX, float worldY) { HeroDefenseGame.this.focusFireAt(worldX, worldY); }

        @Override public void exitApplication() { Gdx.app.exit(); }
    }

    /** Snapshots the run's opening tier, then plays that tier's lines. */
    private void beginOpening() {
        cinematicFlow.beginOpening();
    }

    /**
     * Tap-to-focus (roadmap R3.1): marks the enemy under the finger for the bow, clears the mark on a miss.
     * A tap on a silent watcher greets it instead (roadmap ST3); three greetings and it departs.
     */
    private void focusFireAt(float worldX, float worldY) {
        if (gameState == null || !gameState.hero.alive) return;
        if (MercySystem.greet(gameState, worldX, worldY) == MercySystem.Result.SPARED) {
            showStoryBeat(presentationSystem.presentMercySpare(gameState, worldX, worldY));
            hapticFeedback.tap();
            return;
        }
        Enemy marked = FocusFireSystem.markAt(gameState, worldX, worldY);
        touchFeedbackSystem.triggerTap(worldX, worldY);
        if (marked != null) {
            hapticFeedback.tap();
        }
    }

    /** Shows the opening line for a freshly started wave, unless a beat already shows. */
    private void showWaveReflection() {
        if (frameDriver.storyBeatLine() != null) {
            return;
        }
        String reflection = presentationSystem.waveOpenLine(gameState);
        if (reflection != null) {
            showStoryBeat(reflection);
        }
    }

    private void showStoryBeat(String line) {
        frameDriver.showStoryBeat(line, com.amirrezahadipoor.herodefense.audio.SpeechBlip.voiceForRunningText(line));
    }

    private void beginPlantingCeremony() {
        cinematicFlow.beginPlantingCeremony();
    }

    private void beginBossIntro() {
        cinematicFlow.beginBossIntro();
    }

    /** Presentation-only ceremony tick; the wave-101 hand-off happens once the flow completes it. */
    private void updateCinematic(float deltaSeconds) {
        screenShakeSystem.update(deltaSeconds);
        particleSystem.update(deltaSeconds);
        floatingCoinTextSystem.update(deltaSeconds);
        floatingDamageTextSystem.update(deltaSeconds);
        cinematicFlow.update(deltaSeconds);
    }

    private void updatePlaying(float deltaSeconds) {
        float simulationDelta = deltaSeconds * gameState.simulationSpeed;
        if (gameState.waveActive) gameState.waveElapsedSeconds += simulationDelta;
        screenShakeSystem.update(simulationDelta);
        particleSystem.update(simulationDelta);
        floatingCoinTextSystem.update(simulationDelta);
        floatingDamageTextSystem.update(simulationDelta);
        // Roadmap A1: the frame used to re-anchor the Hero to the arena centre here, every tick, which is what
        // made the defender stationary. Stepping now runs in that slot instead -- before the animation
        //  everything downstream sees this tick's position.
        HeroMovementSystem.update(gameState, simulationDelta);
        heroAnimationController.update(gameState.hero, simulationDelta);
        enemyMovementSystem.update(gameState, simulationDelta);
        FocusFireSystem.tick(gameState, simulationDelta);
        presentationSystem.presentPlaytime(gameState);
        showStoryBeat(DailyGift.claim(gameState, settingsRepository, java.time.LocalDate.now().toEpochDay()));
        CombatSystem.Frame frame = combatSystem.update(gameState, simulationDelta, settings);
        if (frame.criticalHits() > 0) {
            renderers.postProcessRenderer.pulseCrit();
        }
        waveDirector.afterCombat(frame.gameOver(), frame.leveledUp());
        simulationSeconds += simulationDelta;
    }

    private final class FrameHost implements FrameDriver.Host {
        @Override public HapticFeedback hapticFeedback() { return hapticFeedback; }

        @Override
        public GameState gameState() {
            return gameState;
        }

        @Override
        public void saveNow() {
            HeroDefenseGame.this.saveNow();
        }

        @Override
        public void updatePlaying(float deltaSeconds) {
            HeroDefenseGame.this.updatePlaying(deltaSeconds);
        }

        @Override
        public void updateCinematic(float deltaSeconds) {
            HeroDefenseGame.this.updateCinematic(deltaSeconds);
        }

        @Override
        public void draw(float presentationDeltaSeconds) {
            screenStateComposer.draw(presentationDeltaSeconds);
        }
    }

    /** What the ceremony flow needs from the game: the run state and a save point at the hand-off. */
    private final class CinematicHost implements CinematicFlow.Host {
        @Override
        public GameState gameState() {
            return gameState;
        }

        @Override
        public void showWaveReflection() {
            HeroDefenseGame.this.showWaveReflection();
        }

        @Override
        public void saveNow() {
            HeroDefenseGame.this.saveNow();
        }
    }

    /** What the session controller needs: the run state, its clock, saving and two screen hand-offs. */
    private final class SessionHost implements SessionController.Host {
        @Override
        public GameState gameState() {
            return gameState;
        }

        @Override
        public void setGameState(GameState state) {
            gameState = state;
        }

        @Override
        public void resetRunClock() {
            simulationSeconds = 0f;
            frameDriver.resetGameOverPresentation();
        }

        @Override
        public void saveNow() {
            HeroDefenseGame.this.saveNow();
        }

        @Override
        public boolean continueAvailable() {
            return continueAvailable;
        }

        @Override
        public void showWaveReflection() {
            HeroDefenseGame.this.showWaveReflection();
        }

        @Override
        public void beginPlantingCeremony() {
            HeroDefenseGame.this.beginPlantingCeremony();
        }

        @Override
        public void beginBossIntro() {
            HeroDefenseGame.this.beginBossIntro();
        }
    }

    /** What the wave director needs from the game: its state, screens, saving and the two ceremonies. */
    private final class DirectorHost implements WaveDirector.Host {
        @Override
        public GameState gameState() {
            return gameState;
        }

        @Override
        public void transitionTo(GameScreenState screen) {
            flow.transitionTo(screen);
        }

        @Override
        public void saveNow() {
            HeroDefenseGame.this.saveNow();
        }

        @Override
        public void showWaveReflection() {
            HeroDefenseGame.this.showWaveReflection();
        }

        @Override
        public void beginPlantingCeremony() {
            HeroDefenseGame.this.beginPlantingCeremony();
        }

        @Override
        public void beginBossIntro() {
            HeroDefenseGame.this.beginBossIntro();
        }

        @Override
        public void recordRunEnd() {
            HeroDefenseGame.this.recordRunEnd();
        }
    }

    private final class ComposerHost implements ScreenStateComposer.Host {
        @Override public float ambientSeconds() { return frameDriver.ambientSeconds(); }
        @Override public ArenaEnvironmentRenderer arenaEnvironmentRenderer() { return renderers.arenaEnvironmentRenderer; }
        @Override public CeremonyHeroRenderer ceremonyHeroRenderer() { return renderers.ceremonyHeroRenderer; }
        @Override public CodexOverlayRenderer codexOverlayRenderer() { return renderers.codexOverlayRenderer; }
        @Override public CodexTouchController codexTouchController() { return codexTouchController; }
        @Override public CombatEntityRenderer combatEntityRenderer() { return renderers.combatEntityRenderer; }
        @Override public PostProcessRenderer postProcessRenderer() { return renderers.postProcessRenderer; }
        @Override public boolean continueAvailable() { return continueAvailable; }
        @Override public EquipmentSpriteRenderer equipmentSpriteRenderer() { return renderers.equipmentSpriteRenderer; }
        @Override public FloatingCoinTextRenderer floatingCoinTextRenderer() { return renderers.floatingCoinTextRenderer; }
        @Override public FloatingCoinTextSystem floatingCoinTextSystem() { return floatingCoinTextSystem; }
        @Override public FloatingDamageTextRenderer floatingDamageTextRenderer() { return renderers.floatingDamageTextRenderer; }
        @Override public FloatingDamageTextSystem floatingDamageTextSystem() { return floatingDamageTextSystem; }
        @Override public GameFlowController flow() { return flow; }
        @Override public GameOverOverlayRenderer gameOverOverlayRenderer() { return renderers.gameOverOverlayRenderer; }
        @Override public float gameOverPresentationSeconds() { return frameDriver.gameOverPresentationSeconds(); }
        @Override public GameState gameState() { return gameState; }
        @Override public HeroAnimationController heroAnimationController() { return heroAnimationController; }
        @Override public HeroSpriteRenderer heroSpriteRenderer() { return renderers.heroSpriteRenderer; }
        @Override public HudRenderer hudRenderer() { return renderers.hudRenderer; }
        @Override public InventoryOverlayRenderer inventoryOverlayRenderer() { return renderers.inventoryOverlayRenderer; }
        @Override public InventoryTouchController inventoryTouchController() { return inventoryTouchController; }
        @Override public LevelUpOverlayRenderer levelUpOverlayRenderer() { return renderers.levelUpOverlayRenderer; }
        @Override public MainMenuRenderer mainMenuRenderer() { return renderers.mainMenuRenderer; }
        @Override public BossIntroCinematic bossIntroCinematic() { return bossIntroCinematic; }
        @Override public BossIntroCinematic bossIntroCinematic() { return bossIntroCinematic; }
        @Override public OpeningCinematic openingCinematic() { return openingCinematic; }
        @Override public OpeningCinematicRenderer openingCinematicRenderer() { return renderers.openingCinematicRenderer; }
        @Override public ParticleRenderer particleRenderer() { return renderers.particleRenderer; }
        @Override public ParticleSystem particleSystem() { return particleSystem; }
        @Override public PauseOverlayRenderer pauseOverlayRenderer() { return renderers.pauseOverlayRenderer; }
        @Override public PlantingCeremony plantingCeremony() { return plantingCeremony; }
        @Override public RewardCardOverlayRenderer rewardCardOverlayRenderer() { return renderers.rewardCardOverlayRenderer; }
        @Override public RootNetworkOverlayRenderer rootNetworkOverlayRenderer() { return renderers.rootNetworkOverlayRenderer; }
        @Override public RootNetworkSystem rootNetworkSystem() { return rootNetworkSystem; }
        @Override public SaplingTreeRenderer saplingTreeRenderer() { return renderers.saplingTreeRenderer; }
        @Override public ScreenShakeSystem screenShakeSystem() { return screenShakeSystem; }
        @Override public GameSettings settings() { return settings; }
        @Override public SettingsTouchController settingsTouchController() { return settingsTouchController; }
        @Override public SettingsOverlayRenderer settingsOverlayRenderer() { return renderers.settingsOverlayRenderer; }
        @Override public StatShopTouchLayout.Tab shopTab() { return shopTab; }
        @Override public float simulationSeconds() { return simulationSeconds; }
        @Override public SkillShopSystem skillShopSystem() { return skillShopSystem; }
        @Override public StatShopOverlayRenderer statShopOverlayRenderer() { return renderers.statShopOverlayRenderer; }
        @Override public StatShopSystem statShopSystem() { return statShopSystem; }
        @Override public DialogueBox storyDialogue() { return frameDriver.storyDialogue(); }
        @Override public DialogueBox cinematicDialogue() { return cinematicFlow.dialogue(); }
        @Override public TouchFeedbackRenderer touchFeedbackRenderer() { return renderers.touchFeedbackRenderer; }
        @Override public TouchFeedbackSystem touchFeedbackSystem() { return touchFeedbackSystem; }
        @Override public TrialDraftOverlayRenderer trialDraftOverlayRenderer() { return renderers.trialDraftOverlayRenderer; }
        @Override public UiFrameRenderer uiFrameRenderer() { return renderers.uiFrameRenderer; }
        @Override public UiIconRenderer uiIconRenderer() { return renderers.uiIconRenderer; }
        @Override public DialogueBoxRenderer dialogueBoxRenderer() { return renderers.dialogueBoxRenderer; }
    }

}
oxRenderer; }
    }

}

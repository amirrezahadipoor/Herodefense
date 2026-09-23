package com.amirrezahadipoor.herodefense.presentation;

import com.amirrezahadipoor.herodefense.GameFlowController;
import com.amirrezahadipoor.herodefense.GameScreenState;
import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.ascension.RootNetworkSystem;
import com.amirrezahadipoor.herodefense.gameplay.BossIntroCinematic;
import com.amirrezahadipoor.herodefense.gameplay.BreatherCinematic;
import com.amirrezahadipoor.herodefense.gameplay.HeroAnimationController;
import com.amirrezahadipoor.herodefense.gameplay.LastStandCamera;
import com.amirrezahadipoor.herodefense.gameplay.OpeningCinematic;
import com.amirrezahadipoor.herodefense.gameplay.PlantingCeremony;
import com.amirrezahadipoor.herodefense.input.CodexTouchController;
import com.amirrezahadipoor.herodefense.input.InventoryTouchController;
import com.amirrezahadipoor.herodefense.input.SettingsTouchController;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.onboarding.OnboardingSystem;
import com.amirrezahadipoor.herodefense.polish.FloatingCoinTextSystem;
import com.amirrezahadipoor.herodefense.polish.FloatingDamageTextSystem;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ReducedMotion;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;
import com.amirrezahadipoor.herodefense.polish.TouchFeedbackSystem;
import com.amirrezahadipoor.herodefense.render.ArenaEnvironmentRenderer;
import com.amirrezahadipoor.herodefense.render.CeremonyHeroRenderer;
import com.amirrezahadipoor.herodefense.render.CodexOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.CombatEntityRenderer;
import com.amirrezahadipoor.herodefense.render.DialogueBoxRenderer;
import com.amirrezahadipoor.herodefense.render.PostProcessRenderer;
import com.amirrezahadipoor.herodefense.render.EquipmentSpriteRenderer;
import com.amirrezahadipoor.herodefense.render.FloatingCoinTextRenderer;
import com.amirrezahadipoor.herodefense.render.FloatingDamageTextRenderer;
import com.amirrezahadipoor.herodefense.render.GameOverOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.HeroSpriteRenderer;
import com.amirrezahadipoor.herodefense.render.HudRenderer;
import com.amirrezahadipoor.herodefense.render.InventoryOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.LevelUpOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.MainMenuRenderer;
import com.amirrezahadipoor.herodefense.render.OnboardingOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.OpeningCinematicRenderer;
import com.amirrezahadipoor.herodefense.render.ParticleRenderer;
import com.amirrezahadipoor.herodefense.render.PauseOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.RewardCardOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.RootNetworkOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.SaplingTreeRenderer;
import com.amirrezahadipoor.herodefense.render.SettingsOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.StatShopOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.TouchFeedbackRenderer;
import com.amirrezahadipoor.herodefense.render.TrialDraftOverlayRenderer;
import com.amirrezahadipoor.herodefense.render.UiFrameRenderer;
import com.amirrezahadipoor.herodefense.render.UiIconRenderer;
import com.amirrezahadipoor.herodefense.settings.GameSettings;
import com.amirrezahadipoor.herodefense.shop.StatShopSystem;
import com.amirrezahadipoor.herodefense.skills.SkillShopSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.amirrezahadipoor.herodefense.input.StatShopTouchLayout;

/**
 * Draws one frame of the current screen state: arena, actors, effects, overlays and whisper lines.
 *
 * <p>Extracted from {@code HeroDefenseGame} (roadmap R2.2, the audit's {@code ArenaRendererFacade} and
 * {@code HudFlow} items). The composer owns no state: it reads the run and the renderers through its
 * {@link Host} port and receives the camera and the sprite batch it is allowed to drive, so the game class
 * stays the single owner of the simulation, the save file and the story line. The dispatch itself was moved
 * verbatim, so the frame the emulator smoke journeys capture is built by the same code as before.
 */
public final class ScreenStateComposer {

    private final Host host;

    private final OrthographicCamera camera;

    private final SpriteBatch spriteBatch;
    /**
     * The first vigil's banner (roadmap R7.1). The one thing this composer owns, because the alternative was
     * a field on the game object the architecture ratchet will not let grow: it is created on first use and
     * is stateless between frames.
     */
    private OnboardingOverlayRenderer onboardingOverlayRenderer;

    public ScreenStateComposer(Host host, OrthographicCamera camera, SpriteBatch spriteBatch) {
        this.host = host;
        this.camera = camera;
        this.spriteBatch = spriteBatch;
    }

    /** What the composer needs from the game; implemented by an adapter inside the game class. */
    public interface Host {
        float ambientSeconds();

        ArenaEnvironmentRenderer arenaEnvironmentRenderer();

        CeremonyHeroRenderer ceremonyHeroRenderer();

        CodexOverlayRenderer codexOverlayRenderer();

        CodexTouchController codexTouchController();

        CombatEntityRenderer combatEntityRenderer();

        /** The E1 chain the in-run frame renders into; HUD and overlays stay out of it. */
        PostProcessRenderer postProcessRenderer();

        BossIntroCinematic bossIntroCinematic();

        BreatherCinematic breatherCinematic();

        boolean continueAvailable();

        EquipmentSpriteRenderer equipmentSpriteRenderer();

        FloatingCoinTextRenderer floatingCoinTextRenderer();

        FloatingCoinTextSystem floatingCoinTextSystem();

        FloatingDamageTextRenderer floatingDamageTextRenderer();

        FloatingDamageTextSystem floatingDamageTextSystem();

        GameFlowController flow();

        GameOverOverlayRenderer gameOverOverlayRenderer();

        float gameOverPresentationSeconds();

        GameState gameState();

        HeroAnimationController heroAnimationController();

        HeroSpriteRenderer heroSpriteRenderer();

        HudRenderer hudRenderer();

        InventoryOverlayRenderer inventoryOverlayRenderer();

        InventoryTouchController inventoryTouchController();

        LevelUpOverlayRenderer levelUpOverlayRenderer();

        MainMenuRenderer mainMenuRenderer();

        OpeningCinematic openingCinematic();

        OpeningCinematicRenderer openingCinematicRenderer();

        ParticleRenderer particleRenderer();

        ParticleSystem particleSystem();

        PauseOverlayRenderer pauseOverlayRenderer();

        PlantingCeremony plantingCeremony();

        RewardCardOverlayRenderer rewardCardOverlayRenderer();

        RootNetworkOverlayRenderer rootNetworkOverlayRenderer();

        RootNetworkSystem rootNetworkSystem();

        SaplingTreeRenderer saplingTreeRenderer();

        ScreenShakeSystem screenShakeSystem();

        GameSettings settings();

        SettingsTouchController settingsTouchController();

        SettingsOverlayRenderer settingsOverlayRenderer();

        StatShopTouchLayout.Tab shopTab();

        float simulationSeconds();

        SkillShopSystem skillShopSystem();

        StatShopOverlayRenderer statShopOverlayRenderer();

        StatShopSystem statShopSystem();

        /** The frame's message box; what a beat or whisper is typing out, in whose voice. */
        DialogueBox storyDialogue();

        /** The ceremony's message box; the opening's lines and the planting's beats. */
        DialogueBox cinematicDialogue();

        TouchFeedbackRenderer touchFeedbackRenderer();

        TouchFeedbackSystem touchFeedbackSystem();

        TrialDraftOverlayRenderer trialDraftOverlayRenderer();

        UiFrameRenderer uiFrameRenderer();

        UiIconRenderer uiIconRenderer();

        DialogueBoxRenderer dialogueBoxRenderer();
    }

    /** Builds one frame for the state the flow is currently in. */
    public void draw(float presentationDeltaSeconds) {
        // The first vigil (roadmap R7.1) runs on the frame clock, and it watches the run's own books for the
        // one lesson the touch layer cannot see: loot arriving. Both are no-ops once the lesson is over.
        OnboardingSystem onboarding = host.flow().onboarding();
        onboarding.attach(host.settings(), null);
        onboarding.update(presentationDeltaSeconds);
        if (host.flow().state() == GameScreenState.PLAYING) {
            onboarding.observe(host.gameState());
        }
float tint = switch (host.flow().state()) {
    case MENU -> 0.14f;
    case SETTINGS -> 0.13f;
    case PLAYING -> 0.20f;
    case PAUSED -> 0.11f;
    case LEVEL_UP -> 0.22f;
    case CARD_CHOICE -> 0.24f;
    case TRIAL_DRAFT -> 0.23f;
    case CINEMATIC -> 0.21f;
    case INVENTORY -> 0.18f;
    case SHOP -> 0.18f;
    case CODEX -> 0.18f;
    case ROOT_NETWORK -> 0.10f;
    case GAME_OVER -> 0.08f;
};
Gdx.gl.glClearColor(tint * 0.55f, tint, tint * 0.78f, 1f);
Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

if (host.flow().state() != GameScreenState.MENU && host.flow().state() != GameScreenState.SETTINGS) {
    // E1: the in-run world renders into the post chain's scene target; the composite returns it
    // below, and everything after that -- HUD, overlays, whispers -- stays on the screen itself,
    // because interface text must never pass through a blur.
    host.postProcessRenderer().beginScene(tint * 0.55f, tint, tint * 0.78f);
    float baseCameraX = WorldLayout.REFERENCE_WIDTH * 0.5f;
    float baseCameraY = WorldLayout.REFERENCE_HEIGHT * 0.5f;
    boolean opening = host.flow().state() == GameScreenState.CINEMATIC && host.openingCinematic().isActive();
    boolean bossIntro = host.flow().state() == GameScreenState.CINEMATIC
        && host.bossIntroCinematic().isActive();
    boolean breather = host.flow().state() == GameScreenState.CINEMATIC
        && host.breatherCinematic().isActive();
    // Roadmap G3a: the camera impulse and the spore drift are decoration, so a player who asked not to be
    // shaken gets the same frame without them. The shake system itself keeps running -- its state belongs to
    // the simulation and stays deterministic -- and only the camera stops reading it.
    boolean reducedMotion = ReducedMotion.suppresses(host.settings());
    float shakeX = reducedMotion ? 0f : host.screenShakeSystem().offsetX();
    float shakeY = reducedMotion ? 0f : host.screenShakeSystem().offsetY();
    float focus = opening ? host.openingCinematic().cameraFocus() : 0f;
    camera.zoom = opening ? host.openingCinematic().cameraZoom()
        : bossIntro ? host.bossIntroCinematic().cameraZoom()
        : LastStandCamera.zoomFor(host.gameState());
    camera.position.set(
        baseCameraX + shakeX
            + (GameState.ARENA_CENTER_X - baseCameraX) * focus,
        baseCameraY + shakeY
            + (GameState.ARENA_CENTER_Y + 60f - baseCameraY) * focus,
        camera.position.z
    );
    camera.update();
    spriteBatch.setProjectionMatrix(camera.combined);
    spriteBatch.begin();
    host.arenaEnvironmentRenderer().draw(
        spriteBatch,
        host.gameState(),
        host.simulationSeconds(),
        presentationDeltaSeconds,
        reducedMotion,
        host.flow().state() == GameScreenState.GAME_OVER
    );
    spriteBatch.end();
    if (!reducedMotion) {
        host.particleRenderer().drawAmbient(camera.combined, host.ambientSeconds());
    }
    spriteBatch.begin();
    boolean cinematic = host.flow().state() == GameScreenState.CINEMATIC && !opening && !bossIntro && !breather;
    if (cinematic) {
        if (host.gameState().plantedTreesCount > 0) {
            host.saplingTreeRenderer().drawGroveIdle(spriteBatch, host.gameState(), host.ambientSeconds());
        } else if (host.gameState().secondTreePlanted) {
            host.saplingTreeRenderer().drawIdle(spriteBatch, host.ambientSeconds());
        }
        if (host.plantingCeremony().saplingVisible()) {
            host.saplingTreeRenderer().drawGrowing(spriteBatch, host.plantingCeremony());
        }
    } else {
        if (host.gameState().plantedTreesCount > 0) {
            host.saplingTreeRenderer().drawGroveIdle(spriteBatch, host.gameState(), host.ambientSeconds());
        } else if (host.gameState().secondTreePlanted) {
            host.saplingTreeRenderer().drawIdle(spriteBatch, host.ambientSeconds());
        }
    }
    host.combatEntityRenderer().drawActors(spriteBatch, host.gameState(), host.simulationSeconds());
    if (cinematic) {
        host.ceremonyHeroRenderer().draw(spriteBatch, host.plantingCeremony());
    } else {
        int heroFrame = host.heroAnimationController().frameIndex(host.gameState().hero);
        host.heroSpriteRenderer().draw(spriteBatch, host.gameState().hero, heroFrame);
        host.equipmentSpriteRenderer().draw(spriteBatch, host.gameState(), heroFrame, host.simulationSeconds());
    }
    host.combatEntityRenderer().drawEffects(spriteBatch, host.gameState(), host.simulationSeconds());
    spriteBatch.end();
    host.particleRenderer().draw(camera.combined, host.particleSystem(), host.simulationSeconds());
    host.floatingDamageTextRenderer().draw(
        spriteBatch, camera.combined, host.floatingDamageTextSystem()
    );
    host.floatingCoinTextRenderer().draw(
        spriteBatch, camera.combined, host.floatingCoinTextSystem()
    );
    camera.zoom = 1f;
    camera.position.set(baseCameraX, baseCameraY, camera.position.z);
    camera.update();
    if (opening || host.plantingCeremony().isActive() || bossIntro || breather) {
        // The opening's cloud, then the scene's line typing out in the same box the arena's beats use.
        host.openingCinematicRenderer().draw(camera.combined, host.openingCinematic());
        host.dialogueBoxRenderer().draw(spriteBatch, camera.combined, host.cinematicDialogue());
    }
    host.postProcessRenderer().endSceneAndComposite();
}
boolean openingActive = host.flow().state() == GameScreenState.CINEMATIC && host.openingCinematic().isActive();
if (host.flow().state() == GameScreenState.PLAYING
    || (host.flow().state() == GameScreenState.CINEMATIC && !openingActive)) {
    host.hudRenderer().draw(
        spriteBatch, camera.combined, host.gameState(), host.uiIconRenderer(), host.uiFrameRenderer(),
        presentationDeltaSeconds
    );
}
if (host.flow().state() == GameScreenState.PLAYING && onboarding.active()) {
    if (onboardingOverlayRenderer == null) {
        onboardingOverlayRenderer = new OnboardingOverlayRenderer();
    }
    onboardingOverlayRenderer.draw(spriteBatch, camera.combined, onboarding, host.uiFrameRenderer());
}
if (host.flow().state() == GameScreenState.MENU) {
    host.mainMenuRenderer().draw(
        spriteBatch,
        camera.combined,
        host.continueAvailable(),
        host.gameState().coins,
        host.uiIconRenderer(),
        host.uiFrameRenderer(),
        host.gameState()
    );
} else if (host.flow().state() == GameScreenState.SETTINGS) {
    host.settingsOverlayRenderer().draw(
        spriteBatch, camera.combined, host.settings(), host.uiIconRenderer(), host.uiFrameRenderer(),
        host.settingsTouchController().firstVisibleIndex()
    );
} else if (host.flow().state() == GameScreenState.LEVEL_UP) {
    host.levelUpOverlayRenderer().draw(
        spriteBatch, camera.combined, host.gameState(), host.uiIconRenderer(), host.uiFrameRenderer()
    );
} else if (host.flow().state() == GameScreenState.GAME_OVER) {
    host.gameOverOverlayRenderer().draw(
        spriteBatch,
        camera.combined,
        host.gameState(),
        host.uiIconRenderer(),
        host.uiFrameRenderer(),
        host.gameOverPresentationSeconds()
    );
} else if (host.flow().state() == GameScreenState.CARD_CHOICE) {
    host.rewardCardOverlayRenderer().draw(
        spriteBatch, camera.combined, host.gameState(), host.uiIconRenderer(), host.uiFrameRenderer()
    );
} else if (host.flow().state() == GameScreenState.TRIAL_DRAFT) {
    host.trialDraftOverlayRenderer().draw(
        spriteBatch, camera.combined, host.gameState(), host.uiIconRenderer(), host.uiFrameRenderer()
    );
} else if (host.flow().state() == GameScreenState.SHOP) {
    host.statShopOverlayRenderer().draw(
        spriteBatch,
        camera.combined,
        host.gameState(),
        host.statShopSystem(),
        host.skillShopSystem(),
        host.shopTab(),
        host.uiIconRenderer(),
        host.uiFrameRenderer(),
        host.flow().returnState() == GameScreenState.PAUSED
    );
} else if (host.flow().state() == GameScreenState.INVENTORY) {
    host.inventoryOverlayRenderer().drawInventory(
        spriteBatch,
        camera.combined,
        host.gameState(),
        host.inventoryTouchController(),
        host.uiIconRenderer(),
        host.uiFrameRenderer(),
        host.settings()
    );
} else if (host.flow().state() == GameScreenState.CODEX) {
    host.codexOverlayRenderer().draw(
        spriteBatch,
        camera.combined,
        host.gameState(),
        host.codexTouchController(),
        host.uiIconRenderer(),
        host.uiFrameRenderer()
    );
} else if (host.flow().state() == GameScreenState.PAUSED) {
    host.pauseOverlayRenderer().draw(
        spriteBatch, camera.combined, host.gameState(), host.uiIconRenderer(), host.uiFrameRenderer()
    );
} else if (host.flow().state() == GameScreenState.ROOT_NETWORK) {
    host.rootNetworkOverlayRenderer().draw(
        spriteBatch, camera.combined, host.gameState(), host.rootNetworkSystem(),
        host.uiIconRenderer(), host.uiFrameRenderer(), host.saplingTreeRenderer(), host.ambientSeconds()
    );
}
if (host.flow().state() == GameScreenState.PLAYING || host.flow().state() == GameScreenState.GAME_OVER
    || host.flow().state() == GameScreenState.ROOT_NETWORK) {
    // The message box: a beat or a whisper on the arena, the Hollow's parting word on the game-over
    // screen, and the Tree's letter on the hub. It is drawn after the HUD rows and overlays so the
    // box sits over them, and it is on the un-zoomed camera like the HUD itself.
    host.dialogueBoxRenderer().draw(spriteBatch, camera.combined, host.storyDialogue());
}
host.touchFeedbackRenderer().draw(camera.combined, host.touchFeedbackSystem());
    }
}

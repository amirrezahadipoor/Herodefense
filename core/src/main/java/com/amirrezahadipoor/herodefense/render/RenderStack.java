package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * The renderers the game draws with, created in one place and closed in one place (roadmap R2.2, the audit's
 * "ArenaRendererFacade" item).
 *
 * <p>{@code HeroDefenseGame} used to declare, create and close twenty-seven renderers itself: three blocks that
 * had to be edited in step, with a close call that could be forgotten when a renderer was added. The stack owns
 * them now, in the same order the game created them, and {@link #close()} mirrors the original teardown
 * statement for statement — renderer by renderer, then the batch. Nothing here makes a decision: the drawing
 * itself still happens in {@code ScreenStateComposer} and in the per-state overlays, and the game reaches these
 * objects exactly as before.
 */
public final class RenderStack {

    public final SpriteBatch spriteBatch = new SpriteBatch();
    public final ArenaEnvironmentRenderer arenaEnvironmentRenderer = new ArenaEnvironmentRenderer();
    public final CombatEntityRenderer combatEntityRenderer = new CombatEntityRenderer();
    public final PostProcessRenderer postProcessRenderer = new PostProcessRenderer();
    public final FloatingCoinTextRenderer floatingCoinTextRenderer = new FloatingCoinTextRenderer();
    public final FloatingDamageTextRenderer floatingDamageTextRenderer = new FloatingDamageTextRenderer();
    public final GameOverOverlayRenderer gameOverOverlayRenderer = new GameOverOverlayRenderer();
    public final HeroSpriteRenderer heroSpriteRenderer = new HeroSpriteRenderer();
    public final CeremonyHeroRenderer ceremonyHeroRenderer = new CeremonyHeroRenderer();
    public final SaplingTreeRenderer saplingTreeRenderer = new SaplingTreeRenderer();
    public final OpeningCinematicRenderer openingCinematicRenderer = new OpeningCinematicRenderer();
    public final HudRenderer hudRenderer = new HudRenderer();
    public final EquipmentSpriteRenderer equipmentSpriteRenderer = new EquipmentSpriteRenderer();
    public final CodexOverlayRenderer codexOverlayRenderer = new CodexOverlayRenderer();
    public final IdleWhisperRenderer idleWhisperRenderer = new IdleWhisperRenderer();
    public final DialogueBoxRenderer dialogueBoxRenderer = new DialogueBoxRenderer();
    public final InventoryOverlayRenderer inventoryOverlayRenderer = new InventoryOverlayRenderer();
    public final LevelUpOverlayRenderer levelUpOverlayRenderer = new LevelUpOverlayRenderer();
    public final MainMenuRenderer mainMenuRenderer = new MainMenuRenderer();
    public final ParticleRenderer particleRenderer = new ParticleRenderer();
    public final PauseOverlayRenderer pauseOverlayRenderer = new PauseOverlayRenderer();
    public final RewardCardOverlayRenderer rewardCardOverlayRenderer = new RewardCardOverlayRenderer();
    public final TrialDraftOverlayRenderer trialDraftOverlayRenderer = new TrialDraftOverlayRenderer();
    public final SettingsOverlayRenderer settingsOverlayRenderer = new SettingsOverlayRenderer();
    public final StatShopOverlayRenderer statShopOverlayRenderer = new StatShopOverlayRenderer();
    public final RootNetworkOverlayRenderer rootNetworkOverlayRenderer = new RootNetworkOverlayRenderer();
    public final TouchFeedbackRenderer touchFeedbackRenderer = new TouchFeedbackRenderer();
    public final UiFrameRenderer uiFrameRenderer = new UiFrameRenderer();
    public final UiIconRenderer uiIconRenderer = new UiIconRenderer();

    /** Releases every renderer, then the batch. The order is the order the game used before this class existed. */
    public void close() {
        arenaEnvironmentRenderer.close();
        combatEntityRenderer.close();
        postProcessRenderer.close();
        floatingDamageTextRenderer.close();
        floatingCoinTextRenderer.close();
        gameOverOverlayRenderer.close();
        heroSpriteRenderer.close();
        ceremonyHeroRenderer.close();
        saplingTreeRenderer.close();
        openingCinematicRenderer.close();
        hudRenderer.close();
        equipmentSpriteRenderer.close();
        codexOverlayRenderer.close();
        idleWhisperRenderer.close();
        dialogueBoxRenderer.close();
        inventoryOverlayRenderer.close();
        levelUpOverlayRenderer.close();
        mainMenuRenderer.close();
        particleRenderer.close();
        pauseOverlayRenderer.close();
        rewardCardOverlayRenderer.close();
        trialDraftOverlayRenderer.close();
        settingsOverlayRenderer.close();
        statShopOverlayRenderer.close();
        rootNetworkOverlayRenderer.close();
        touchFeedbackRenderer.close();
        uiFrameRenderer.close();
        uiIconRenderer.close();
        spriteBatch.dispose();
    }
}

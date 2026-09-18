package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.gameplay.WaveOmens;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.amirrezahadipoor.herodefense.gameplay.HeroProgressionSystem;
import com.amirrezahadipoor.herodefense.gameplay.FocusSystem;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.HudStrings;
import com.amirrezahadipoor.herodefense.input.HudTouchLayout;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.WaveModifier;
import com.amirrezahadipoor.herodefense.trials.TrialId;

/** Premium segmented portrait HUD that preserves a clear view of the active arena. */
public final class HudRenderer implements AutoCloseable {
    static final float HEALTH_PANEL_X = 18f;
    static final float HEALTH_PANEL_Y = 1180f;
    static final float HEALTH_PANEL_WIDTH = 684f;
    static final float HEALTH_PANEL_HEIGHT = 78f;
    static final float HEALTH_BAR_X = 91f;
    static final float HEALTH_BAR_Y = 1203f;
    static final float HEALTH_BAR_WIDTH = 580f;
    static final float HEALTH_BAR_HEIGHT = 25f;
    static final float EXP_BAR_X = 91f;
    static final float EXP_BAR_Y = 1188f;
    static final float EXP_BAR_WIDTH = 580f;
    static final float EXP_BAR_HEIGHT = 9f;
    static final float INFO_PANEL_Y = 1065f;
    static final float INFO_PANEL_HEIGHT = 100f;
    /** Active-trial mini icons stacked right of the pause button; first trial on top. */
    static final float TRIAL_ICON_X = 660f;
    static final float TRIAL_ICON_TOP_Y = 1112f;
    static final float TRIAL_ICON_STRIDE = 44f;
    static final float TRIAL_ICON_SIZE = 40f;

    private static final Color GOLD = Color.valueOf("EAC66D");
    private static final Color IVORY = Color.valueOf("F3E4BC");
    private static final Color SUBTLE = Color.valueOf("B8C4AF");
    private static final Color HEALTHY = Color.valueOf("48A96A");
    private static final Color WOUNDED = Color.valueOf("D39A43");
    private static final Color CRITICAL = Color.valueOf("C6534F");
    private static final Color EXP = Color.valueOf("8FD4F2");
    private static final Color EXP_FLASH = Color.valueOf("F3E4BC");

    private static final HeroProgressionSystem PROGRESSION = new HeroProgressionSystem();
    /** Real-time countdown of the level-up flash on the EXP bar. */
    private float levelFlashSeconds;
    private int lastSeenLevel = -1;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OverlayText text = new OverlayText();

    public HudRenderer() {
    }

    public void draw(
        SpriteBatch batch,
        Matrix4 projection,
        GameState state,
        UiIconRenderer icons,
        UiFrameRenderer frames
    ) {
        draw(batch, projection, state, icons, frames, 0f);
    }

    public void draw(
        SpriteBatch batch,
        Matrix4 projection,
        GameState state,
        UiIconRenderer icons,
        UiFrameRenderer frames,
        float realDeltaSeconds
    ) {
        float healthRatio = healthRatio(state.hero.health, state.hero.maxHealth);
        float expRatio = experienceRatio(state);
        if (lastSeenLevel >= 0 && state.heroLevel > lastSeenLevel) levelFlashSeconds = LEVEL_FLASH_SECONDS;
        lastSeenLevel = state.heroLevel;
        levelFlashSeconds = Math.max(0f, levelFlashSeconds - Math.max(0f, realDeltaSeconds));
        UiFrameRenderer.State speedState = frames.resolve(
            true, state.simulationSpeed > 1f,
            HudTouchLayout.speedX(), HudTouchLayout.buttonY(),
            HudTouchLayout.BUTTON_WIDTH, HudTouchLayout.BUTTON_HEIGHT
        );
        UiFrameRenderer.State pauseState = frames.resolve(
            true, false,
            HudTouchLayout.pauseX(), HudTouchLayout.buttonY(),
            HudTouchLayout.BUTTON_WIDTH, HudTouchLayout.BUTTON_HEIGHT
        );
        UiFrameRenderer.State inventoryState = frames.resolve(
            true, false,
            HudTouchLayout.inventoryX(), HudTouchLayout.utilityButtonY(),
            HudTouchLayout.UTILITY_BUTTON_WIDTH, HudTouchLayout.UTILITY_BUTTON_HEIGHT
        );
        UiFrameRenderer.State shopState = frames.resolve(
            true, false,
            HudTouchLayout.shopX(), HudTouchLayout.utilityButtonY(),
            HudTouchLayout.UTILITY_BUTTON_WIDTH, HudTouchLayout.UTILITY_BUTTON_HEIGHT
        );
        UiFrameRenderer.State ultimateState = frames.resolve(
            true, false,
            HudTouchLayout.ultimateX(), HudTouchLayout.utilityButtonY(),
            HudTouchLayout.UTILITY_BUTTON_WIDTH, HudTouchLayout.UTILITY_BUTTON_HEIGHT
        );

        float up = HudTouchLayout.topShift();
        batch.setProjectionMatrix(projection);
        batch.begin();
        frames.draw(
            batch, UiFrameRenderer.Kind.PANEL,
            HEALTH_PANEL_X, HEALTH_PANEL_Y + up, HEALTH_PANEL_WIDTH, HEALTH_PANEL_HEIGHT,
            true, false
        );
        frames.draw(batch, UiFrameRenderer.Kind.PANEL,
            UiMirror.leadingOnScreen(18f, 194f), INFO_PANEL_Y + up, 194f,
            INFO_PANEL_HEIGHT, true, false);
        frames.draw(batch, UiFrameRenderer.Kind.PANEL,
            UiMirror.leadingOnScreen(220f, 194f), INFO_PANEL_Y + up, 194f,
            INFO_PANEL_HEIGHT, true, false);
        frames.draw(
            batch, UiFrameRenderer.Kind.BUTTON,
            HudTouchLayout.speedX(), HudTouchLayout.buttonY(),
            HudTouchLayout.BUTTON_WIDTH, HudTouchLayout.BUTTON_HEIGHT,
            true, state.simulationSpeed > 1f
        );
        frames.draw(
            batch, UiFrameRenderer.Kind.BUTTON,
            HudTouchLayout.pauseX(), HudTouchLayout.buttonY(),
            HudTouchLayout.BUTTON_WIDTH, HudTouchLayout.BUTTON_HEIGHT, true, false
        );
        frames.draw(
            batch, UiFrameRenderer.Kind.BUTTON,
            HudTouchLayout.inventoryX(), HudTouchLayout.utilityButtonY(),
            HudTouchLayout.UTILITY_BUTTON_WIDTH, HudTouchLayout.UTILITY_BUTTON_HEIGHT,
            true, false
        );
        frames.draw(
            batch, UiFrameRenderer.Kind.BUTTON,
            HudTouchLayout.shopX(), HudTouchLayout.utilityButtonY(),
            HudTouchLayout.UTILITY_BUTTON_WIDTH, HudTouchLayout.UTILITY_BUTTON_HEIGHT,
            true, false
        );
        if (FocusSystem.isFull(state)) {
            frames.draw(
                batch, UiFrameRenderer.Kind.BUTTON,
                HudTouchLayout.ultimateX(), HudTouchLayout.utilityButtonY(),
                HudTouchLayout.UTILITY_BUTTON_WIDTH, HudTouchLayout.UTILITY_BUTTON_HEIGHT,
                true, true
            );
        }
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // Roadmap G4: the tracks mirror with the screen and the fills grow from the leading edge, so a
        // Persian bar drains toward the screen's right and an English one toward its left.
        float healthBarX = UiMirror.leadingOnScreen(HEALTH_BAR_X, HEALTH_BAR_WIDTH);
        float expBarX = UiMirror.leadingOnScreen(EXP_BAR_X, EXP_BAR_WIDTH);
        shapes.setColor(0.055f, 0.035f, 0.030f, 0.98f);
        shapes.rect(healthBarX, HEALTH_BAR_Y + up, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        float healthFill = Math.max(0f, (HEALTH_BAR_WIDTH - 4f) * healthRatio);
        shapes.setColor(healthColor(healthRatio));
        shapes.rect(
            barFillX(healthBarX, HEALTH_BAR_WIDTH, 2f, healthFill),
            HEALTH_BAR_Y + up + 2f,
            healthFill,
            HEALTH_BAR_HEIGHT - 4f
        );
        float glossFill = Math.max(0f, (HEALTH_BAR_WIDTH - 6f) * healthRatio);
        shapes.setColor(0.90f, 0.98f, 0.82f, 0.18f);
        shapes.rect(
            barFillX(healthBarX, HEALTH_BAR_WIDTH, 3f, glossFill),
            HEALTH_BAR_Y + up + HEALTH_BAR_HEIGHT - 7f,
            glossFill,
            3f
        );
        // EXP: a slim cyan bar under the health bar; flashes ivory for a moment on level-up.
        shapes.setColor(0.055f, 0.035f, 0.030f, 0.98f);
        shapes.rect(expBarX, EXP_BAR_Y + up, EXP_BAR_WIDTH, EXP_BAR_HEIGHT);
        float flash = levelFlashSeconds / LEVEL_FLASH_SECONDS;
        shapes.setColor(
            EXP.r + (EXP_FLASH.r - EXP.r) * flash,
            EXP.g + (EXP_FLASH.g - EXP.g) * flash,
            EXP.b + (EXP_FLASH.b - EXP.b) * flash,
            1f
        );
        float expFill = flash > 0f ? Math.max(expRatio, flash) : expRatio;
        float expFillWidth = Math.max(0f, (EXP_BAR_WIDTH - 3f) * expFill);
        shapes.rect(
            barFillX(expBarX, EXP_BAR_WIDTH, 1.5f, expFillWidth),
            EXP_BAR_Y + up + 1.5f,
            expFillWidth,
            EXP_BAR_HEIGHT - 3f
        );
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        icons.draw(batch, "health", UiMirror.leadingOnScreen(28f, 50f), 1194f + up, 50f);
        drawShadowed(batch, GameLocale.text(HudStrings.LEVEL, GameLocale.number(state.heroLevel)),
            102f, 1199f + up, 0.52f,
            flash > 0f ? EXP_FLASH : EXP);
        // The caption hangs off the panel's trailing edge: the right one in English, the left one in Persian.
        text.drawTrailing(batch, experienceLabel(state), 0f, UiMirror.SCREEN_WIDTH,
            UiMirror.SCREEN_WIDTH - 580f, 1199f + up, 0.46f, SUBTLE);
        if (state.ascensionTier > 0) {
            drawShadowed(batch,
                GameLocale.text(HudStrings.TIER_BADGE, GameLocale.number(state.ascensionTier)),
                620f, 1199f + up, 0.58f, GOLD);
        }
        drawShadowed(batch, GameLocale.text(HudStrings.HEALTH), 102f, 1244f + up, 0.68f, GOLD);
        drawShadowedCentered(
            batch,
            GameLocale.text(
                HudStrings.FRACTION,
                GameLocale.number(Math.round(state.hero.health)),
                GameLocale.number(Math.round(state.hero.maxHealth))
            ),
            381f,
            1226f + up,
            0.84f,
            IVORY
        );

        icons.draw(batch, "wave", UiMirror.leadingOnScreen(29f, 48f), 1087f + up, 48f);
        drawShadowed(batch, GameLocale.text(HudStrings.WAVE), 84f, 1144f + up, 0.66f, GOLD);
        drawShadowed(
            batch,
            GameLocale.text(
                HudStrings.FRACTION,
                GameLocale.number(state.waveNumber), GameLocale.number(GameState.FINAL_WAVE)
            ),
            84f, 1107f + up, 1.02f, IVORY
        );
        // Wave omens (roadmap R3.4): the wave says what it is going to do to you, in one line, while it runs.
        WaveModifier omen = WaveOmens.of(state, state.waveNumber);
        if (omen.isOmen()) {
            drawShadowed(batch, omen.label(), 84f, 1076f + up, 0.44f, CRITICAL);
        }

        icons.draw(batch, "coin", UiMirror.leadingOnScreen(231f, 48f), 1087f + up, 48f);
        drawShadowed(batch, GameLocale.text(HudStrings.COINS), 286f, 1144f + up, 0.66f, GOLD);
        drawShadowed(batch, MainMenuRenderer.coinTotalLabel(state.coins), 286f, 1107f + up, 1.02f, IVORY);
        // Grove HP (32.3): show planted count and health ratio reusing groveHealthRatio
        int groveTotal = 1 + Math.max(0, state.plantedTreesCount);
        float groveRatio = WorldTreeAnimationController.groveHealthRatio(state);
        String groveLabel = GameLocale.text(
            HudStrings.GROVE_STATUS,
            GameLocale.number(groveTotal), GameLocale.percent(Math.round(groveRatio * 100))
        );
        drawShadowed(batch, groveLabel, 286f, 1075f + up, 0.52f, groveRatio < 0.4f ? CRITICAL : (groveRatio < 0.7f ? WOUNDED : HEALTHY));

        float speedOffset = MainMenuRenderer.pressedOffset(speedState);
        icons.draw(batch, "speed", UiMirror.leadingOnScreen(440f, 44f),
            1091f + up + speedOffset, 44f, speedState);
        drawShadowed(batch,
            GameLocale.text(HudStrings.SPEED, GameLocale.number(Math.round(state.simulationSpeed))),
            487f, 1124f + up + speedOffset, 0.96f, IVORY);

        float pauseOffset = MainMenuRenderer.pressedOffset(pauseState);
        icons.draw(batch, "pause", UiMirror.leadingOnScreen(603f, 54f),
            1088f + up + pauseOffset, 54f, pauseState);

        if (state.activeTrials != null) {
            int shown = 0;
            for (String name : state.activeTrials) {
                TrialId trial = TrialId.forName(name);
                if (trial == null) {
                    continue;
                }
                icons.draw(batch, trial.iconKey(),
                    UiMirror.leadingOnScreen(TRIAL_ICON_X, TRIAL_ICON_SIZE),
                    TRIAL_ICON_TOP_Y + up - shown * TRIAL_ICON_STRIDE, TRIAL_ICON_SIZE);
                shown++;
            }
        }

        drawUtilityAction(
            batch, icons, "inventory", GameLocale.text(HudStrings.INVENTORY),
            HudTouchLayout.inventoryX(), inventoryState
        );
        drawUtilityAction(
            batch, icons, "shop", GameLocale.text(HudStrings.SHOP),
            HudTouchLayout.shopX(), shopState
        );
        if (FocusSystem.isFull(state)) {
            drawUtilityAction(
                batch, icons, "general_power", GameLocale.text(HudStrings.ULTIMATE),
                HudTouchLayout.ultimateX(), ultimateState
            );
        }
        batch.end();
    }

    private void drawUtilityAction(
        SpriteBatch batch,
        UiIconRenderer icons,
        String icon,
        String label,
        float x,
        UiFrameRenderer.State state
    ) {
        float offset = MainMenuRenderer.pressedOffset(state) - HudTouchLayout.bottomShift();
        // x is the English design-grid edge; the button itself is drawn where the mirror puts it, and the
        // icon and label follow it inside the button rather than inside the screen (roadmap G4).
        float buttonX = UiMirror.leadingOnScreen(x, HudTouchLayout.UTILITY_BUTTON_WIDTH);
        icons.draw(batch, icon,
            UiMirror.leading(buttonX, HudTouchLayout.UTILITY_BUTTON_WIDTH, 13f, 56f),
            48f + offset, 56f, state);
        float labelCentreX = GameLocale.rightToLeft()
            ? buttonX + HudTouchLayout.UTILITY_BUTTON_WIDTH - 105f
            : buttonX + 105f;
        text.drawCentered(batch, label, labelCentreX, 87f + offset, 0.74f, IVORY);
    }

    private void drawShadowedCentered(
        SpriteBatch batch, String label, float centerX, float y, float scale, Color color
    ) {
        text.drawCentered(batch, label,
            UiMirror.centre(0f, UiMirror.SCREEN_WIDTH, centerX), y, scale, color);
    }

    /**
     * A left-aligned HUD label at its design-grid x, mirrored with the screen (roadmap G4). The run is
     * measured shaped, because the advance of joined Persian text is not the sum of its letters' advances
     * and mirroring with the wrong width would push the label off the edge it was measured against.
     * In English {@code UiMirror.leadingOnScreen} returns x untouched, pixel for pixel.
     */
    private void drawShadowed(
        SpriteBatch batch, String label, float x, float y, float scale, Color color
    ) {
        text.draw(batch, label,
            UiMirror.leadingOnScreen(x, text.width(label, scale)), y, scale, color);
    }

    static final float LEVEL_FLASH_SECONDS = 0.9f;

    /** Progress toward the next level; a capped Hero shows a full bar. */
    static float experienceRatio(GameState state) {
        int required = PROGRESSION.experienceRequiredForNextLevel(state.heroLevel);
        if (required <= 0) return 1f;
        return Math.max(0f, Math.min(1f, state.heroExperience / (float) required));
    }

    /** The bar's caption, in the language in force: a capped hero reads MAX / کامل. */
    static String experienceLabel(GameState state) {
        int required = PROGRESSION.experienceRequiredForNextLevel(state.heroLevel);
        return required <= 0
            ? GameLocale.text(HudStrings.READY)
            : GameLocale.text(
                HudStrings.XP_PROGRESS,
                GameLocale.number(state.heroExperience), GameLocale.number(required)
            );
    }

    /**
     * Where a fill {@code fillWidth} wide starts inside a bar: {@code inset} in from the bar's leading edge,
     * which is its left edge in English and its right one in Persian, so both bars drain toward the trailing
     * edge (roadmap G4). Pure arithmetic on the already-mirrored bar position, and therefore testable.
     */
    static float barFillX(float barX, float barWidth, float inset, float fillWidth) {
        return GameLocale.rightToLeft()
            ? barX + barWidth - inset - fillWidth
            : barX + inset;
    }

    static float healthRatio(float health, float maxHealth) {
        if (maxHealth <= 0f) return 0f;
        return Math.max(0f, Math.min(1f, health / maxHealth));
    }

    static Color healthColor(float ratio) {
        if (ratio < 0.25f) return CRITICAL;
        if (ratio < 0.50f) return WOUNDED;
        return HEALTHY;
    }

    static float occupiedTopArea() {
        return HEALTH_PANEL_WIDTH * HEALTH_PANEL_HEIGHT
            + 194f * INFO_PANEL_HEIGHT * 2f
            + HudTouchLayout.BUTTON_WIDTH * HudTouchLayout.BUTTON_HEIGHT * 2f;
    }

    @Override
    public void close() {
        text.close();
        shapes.dispose();
    }
}

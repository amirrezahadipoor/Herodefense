package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.GameOverStrings;
import com.amirrezahadipoor.herodefense.input.GameOverTouchLayout;
import com.amirrezahadipoor.herodefense.items.EquipmentCatalog;
import com.amirrezahadipoor.herodefense.items.EquipmentDefinition;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.story.Epilogue;
import com.amirrezahadipoor.herodefense.story.HollowVoice;
import com.amirrezahadipoor.herodefense.trials.TrialEffects;

/** Premium end-of-run surface with distinct defeat and victory treatments and a framed restart. */
public final class GameOverOverlayRenderer implements AutoCloseable {
    static final float DESTRUCTION_REVEAL_DELAY_SECONDS = 0.82f;
    static final float REVEAL_FADE_SECONDS = 0.28f;
    static final float TITLE_PANEL_X = 60f;
    static final float TITLE_PANEL_Y = 830f;
    static final float TITLE_PANEL_WIDTH = 600f;
    static final float TITLE_PANEL_HEIGHT = 370f;
    static final float SUMMARY_PANEL_X = 60f;
    static final float SUMMARY_PANEL_Y = 400f;
    static final float SUMMARY_PANEL_WIDTH = 600f;
    static final float SUMMARY_PANEL_HEIGHT = 390f;
    static final int SUMMARY_ROWS = 5;
    static final float EPILOGUE_FIRST_LINE_Y = 1122f;
    static final float EPILOGUE_LINE_STRIDE = 30f;
    static final float EPILOGUE_STANZA_GAP = 12f;
    static final float EPILOGUE_SCALE = 0.72f;
    static final float EPILOGUE_MAX_WIDTH = 540f;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OverlayText text = new OverlayText();

    public void draw(
        SpriteBatch batch,
        Matrix4 projection,
        GameState state,
        UiIconRenderer icons,
        UiFrameRenderer frames,
        float presentationSeconds
    ) {
        float reveal = revealProgress(presentationSeconds, state.runComplete);
        if (reveal <= 0f) return;
        boolean victory = state.runComplete;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (victory) {
            shapes.setColor(0.065f, 0.140f, 0.105f, 0.84f * reveal);
            shapes.rect(0f, ScreenEdges.bottom(), 720f, ScreenEdges.height());
            shapes.setColor(0.24f, 0.19f, 0.05f, 0.42f * reveal);
            shapes.rect(0f, 830f, 720f, 450f);
        } else {
            shapes.setColor(0.110f, 0.055f, 0.055f, 0.88f * reveal);
            shapes.rect(0f, ScreenEdges.bottom(), 720f, ScreenEdges.height());
            shapes.setColor(0.16f, 0.05f, 0.04f, 0.42f * reveal);
            shapes.rect(0f, 830f, 720f, 450f);
        }
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        boolean interactive = isInteractive(presentationSeconds, state.runComplete);
        UiFrameRenderer.State restartState = frames.resolve(
            interactive, false,
            GameOverTouchLayout.RESTART_X, GameOverTouchLayout.RESTART_Y,
            GameOverTouchLayout.RESTART_WIDTH, GameOverTouchLayout.RESTART_HEIGHT
        );
        UiFrameRenderer.State ascendState = frames.resolve(
            interactive, false,
            GameOverTouchLayout.ASCEND_X, GameOverTouchLayout.ASCEND_Y,
            GameOverTouchLayout.ASCEND_WIDTH, GameOverTouchLayout.ASCEND_HEIGHT
        );
        UiFrameRenderer.State rootState = frames.resolve(
            interactive, false,
            GameOverTouchLayout.ROOT_X, GameOverTouchLayout.ROOT_Y,
            GameOverTouchLayout.ROOT_WIDTH, GameOverTouchLayout.ROOT_HEIGHT
        );

        batch.setProjectionMatrix(projection);
        batch.setColor(1f, 1f, 1f, reveal);
        batch.begin();
        frames.draw(batch, UiFrameRenderer.Kind.PANEL, TITLE_PANEL_X, TITLE_PANEL_Y,
            TITLE_PANEL_WIDTH, TITLE_PANEL_HEIGHT, true, false);
        frames.draw(batch, UiFrameRenderer.Kind.PANEL, SUMMARY_PANEL_X, SUMMARY_PANEL_Y,
            SUMMARY_PANEL_WIDTH, SUMMARY_PANEL_HEIGHT, true, false);
        frames.draw(batch, UiFrameRenderer.Kind.BUTTON,
            GameOverTouchLayout.RESTART_X, GameOverTouchLayout.RESTART_Y,
            GameOverTouchLayout.RESTART_WIDTH, GameOverTouchLayout.RESTART_HEIGHT,
            interactive, victory);
        frames.draw(batch, UiFrameRenderer.Kind.BUTTON,
            GameOverTouchLayout.ASCEND_X, GameOverTouchLayout.ASCEND_Y,
            GameOverTouchLayout.ASCEND_WIDTH, GameOverTouchLayout.ASCEND_HEIGHT,
            interactive, true);
        frames.draw(batch, UiFrameRenderer.Kind.BUTTON,
            GameOverTouchLayout.ROOT_X, GameOverTouchLayout.ROOT_Y,
            GameOverTouchLayout.ROOT_WIDTH, GameOverTouchLayout.ROOT_HEIGHT,
            interactive, false);

        Color titleColor = victory ? OverlayText.GOLD : OverlayText.NEGATIVE;
        text.drawCentered(batch,
            victory ? GameLocale.text(GameOverStrings.RUN_COMPLETE) : GameLocale.text(GameOverStrings.DEFEAT),
            360f, 1168f, 0.78f,
            titleColor, reveal);
        drawEpilogue(batch, state, victory, reveal);

        text.drawCentered(batch, GameLocale.text(GameOverStrings.SUMMARY_TITLE), 360f, 762f, 0.82f,
            OverlayText.GOLD, reveal);
        drawRow(batch, icons, 0, "wave", GameLocale.text(GameOverStrings.WAVE_REACHED),
            GameLocale.text(
                GameOverStrings.WAVE_COUNT,
                GameLocale.number(state.waveNumber), GameLocale.number(state.runLengthWaves())
            ), reveal);
        drawRow(batch, icons, 1, "health", GameLocale.text(GameOverStrings.HERO_LEVEL),
            GameLocale.number(state.heroLevel), reveal);
        drawRow(batch, icons, 2, "strength", GameLocale.text(GameOverStrings.ENEMIES_DEFEATED),
            GameLocale.number(state.totalKills), reveal);
        drawRow(batch, icons, 3, "general_power", GameLocale.text(GameOverStrings.BOSSES_DEFEATED),
            GameLocale.text(GameOverStrings.BOSSES_COUNT, GameLocale.number(state.defeatedBosses)), reveal);
        drawRow(batch, icons, 4, "coin", GameLocale.text(GameOverStrings.COINS_EARNED),
            MainMenuRenderer.coinTotalLabel(state.totalKillCoinsEarned), reveal);
        String mythicName = mythicEarnedName(state);
        if (victory && mythicName != null) {
            drawRow(batch, icons, 5, null, GameLocale.text(GameOverStrings.MYTHIC_EARNED), mythicName, reveal);
        }

        float offset = MainMenuRenderer.pressedOffset(restartState);
        icons.draw(batch, "restart", UiMirror.leadingOnScreen(152f, 64f),
            GameOverTouchLayout.RESTART_Y + 24f + offset, 64f, restartState);
        text.drawLeading(batch,
            victory
                ? GameLocale.text(GameOverStrings.DEFEND_AGAIN)
                : GameLocale.text(GameOverStrings.RESTART_AT_WAVE_ONE),
            0f, UiMirror.SCREEN_WIDTH, 228f,
            GameOverTouchLayout.RESTART_Y + 68f + offset, 1.08f,
            interactive ? OverlayText.IVORY : OverlayText.MUTED, reveal);
        text.drawLeading(batch, GameLocale.text(GameOverStrings.DEFEND_AGAIN_SUBTITLE),
            0f, UiMirror.SCREEN_WIDTH, 228f,
            GameOverTouchLayout.RESTART_Y + 34f + offset, 0.62f, OverlayText.SUBTLE, reveal);

        float ascOffset = MainMenuRenderer.pressedOffset(ascendState);
        icons.draw(batch, "general_power", UiMirror.leadingOnScreen(152f, 56f),
            GameOverTouchLayout.ASCEND_Y + 20f + ascOffset, 56f, ascendState);
        int heartwoodPreview = Math.round(GameState.calculateHeartwoodReward(state.peakWaveReached, state.ascensionTier, !state.heroDiedThisRun, state.mode)
            * TrialEffects.heartwoodMultiplier(state.activeTrials));
        text.drawLeading(batch,
            GameLocale.text(GameOverStrings.ASCEND, GameLocale.number(heartwoodPreview)),
            0f, UiMirror.SCREEN_WIDTH, 228f,
            GameOverTouchLayout.ASCEND_Y + 60f + ascOffset, 1.0f,
            interactive ? OverlayText.GOLD : OverlayText.MUTED, reveal);
        text.drawLeading(
            batch,
            GameLocale.text(
                GameOverStrings.TIER_PROGRESS,
                GameLocale.number(state.ascensionTier), GameLocale.number(state.ascensionTier + 1)
            ),
            0f, UiMirror.SCREEN_WIDTH, 228f,
            GameOverTouchLayout.ASCEND_Y + 30f + ascOffset, 0.58f, OverlayText.SUBTLE, reveal);

        float rootOffset = MainMenuRenderer.pressedOffset(rootState);
        icons.draw(batch, "health", UiMirror.leadingOnScreen(152f, 56f),
            GameOverTouchLayout.ROOT_Y + 20f + rootOffset, 56f, rootState);
        text.drawLeading(batch,
            GameLocale.text(GameOverStrings.ROOT_NETWORK, GameLocale.number(state.heartwood)),
            0f, UiMirror.SCREEN_WIDTH, 228f,
            GameOverTouchLayout.ROOT_Y + 60f + rootOffset, 1.0f,
            interactive ? OverlayText.IVORY : OverlayText.MUTED, reveal);
        text.drawLeading(batch, GameLocale.text(GameOverStrings.ROOT_NETWORK_SUBTITLE),
            0f, UiMirror.SCREEN_WIDTH, 228f,
            GameOverTouchLayout.ROOT_Y + 30f + rootOffset, 0.58f, OverlayText.SUBTLE, reveal);

        batch.end();
        batch.setColor(Color.WHITE);
    }

    /** Name of this run's Wave-200 Mythic, or null when none was granted. */
    static String mythicEarnedName(GameState state) {
        if (state == null || state.mythicGrantedItemId == null) return null;
        EquipmentDefinition definition = EquipmentCatalog.byId(state.mythicGrantedItemId);
        return definition == null ? state.mythicGrantedItemId : definition.name();
    }

    private void drawRow(
        SpriteBatch batch,
        UiIconRenderer icons,
        int row,
        String icon,
        String label,
        String value,
        float alpha
    ) {
        float y = summaryRowY(row);
        // The summary panel is centred on the screen, so mirroring about the screen's centre is mirroring about
        // the panel's: 612f from the left is 108f from the right, and the row reads label-then-value in both
        // languages rather than value-then-label in one of them.
        if (icon != null) icons.draw(batch, icon, UiMirror.leadingOnScreen(96f, 56f), y - 20f, 56f);
        text.drawLeading(batch, label, 0f, UiMirror.SCREEN_WIDTH, 172f, y + 18f, 0.92f, OverlayText.IVORY, alpha);
        text.drawTrailing(batch, value, 0f, UiMirror.SCREEN_WIDTH, 108f, y + 20f, 1.06f, OverlayText.GOLD, alpha);
    }

    static float summaryRowY(int row) {
        return 726f - row * 62f;
    }

    private void drawEpilogue(
        SpriteBatch batch, GameState state, boolean victory, float reveal
    ) {
        Epilogue epilogue = Epilogue.endingFor(state);
        float y = EPILOGUE_FIRST_LINE_Y;
        // The Hollow's word on this death, above the Tree's own ending; once revealed, it leaves the queue.
        String hollowLine = HollowVoice.pendingDeathLine(state);
        if (hollowLine != null) {
            for (String line : CodexOverlayRenderer.wrapLines(
                hollowLine, this::epilogueWidth, EPILOGUE_MAX_WIDTH)) {
                text.drawCentered(batch, line, 360f, y, EPILOGUE_SCALE, OverlayText.GOLD, reveal);
                y -= EPILOGUE_LINE_STRIDE;
            }
            if (reveal > 0.5f) {
                HollowVoice.markDeathLineShown(state);
            }
            y -= EPILOGUE_STANZA_GAP * 0.5f;
        }
        for (String beat : epilogue.lines()) {
            for (String line : CodexOverlayRenderer.wrapLines(
                beat, this::epilogueWidth, EPILOGUE_MAX_WIDTH)) {
                text.drawCentered(batch, line, 360f, y, EPILOGUE_SCALE, OverlayText.IVORY, reveal);
                y -= EPILOGUE_LINE_STRIDE;
            }
        }
        if (victory) {
            y -= EPILOGUE_STANZA_GAP;
            for (String beat : Epilogue.TRANSITION) {
                for (String line : CodexOverlayRenderer.wrapLines(
                    beat, this::epilogueWidth, EPILOGUE_MAX_WIDTH)) {
                    text.drawCentered(
                        batch, line, 360f, y, EPILOGUE_SCALE, OverlayText.GOLD, reveal);
                    y -= EPILOGUE_LINE_STRIDE;
                }
            }
        }
    }

    private double epilogueWidth(String line) {
        return text.width(line, EPILOGUE_SCALE);
    }

    static float revealProgress(float presentationSeconds, boolean runComplete) {
        if (runComplete) return 1f;
        if (!Float.isFinite(presentationSeconds)
            || presentationSeconds <= DESTRUCTION_REVEAL_DELAY_SECONDS) {
            return 0f;
        }
        return Math.min(
            1f,
            (presentationSeconds - DESTRUCTION_REVEAL_DELAY_SECONDS) / REVEAL_FADE_SECONDS
        );
    }

    public static boolean isInteractive(float presentationSeconds, boolean runComplete) {
        return revealProgress(presentationSeconds, runComplete) >= 0.95f;
    }

    @Override
    public void close() {
        text.close();
        shapes.dispose();
    }
}

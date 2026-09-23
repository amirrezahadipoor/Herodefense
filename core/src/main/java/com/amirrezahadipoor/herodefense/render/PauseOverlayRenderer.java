package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.PauseStrings;
import com.amirrezahadipoor.herodefense.input.PauseTouchLayout;
import com.amirrezahadipoor.herodefense.model.GameState;


/** Premium pause surface: dimmed live arena, run context, and four framed touch actions. */
public final class PauseOverlayRenderer implements AutoCloseable {
    static final float TITLE_PANEL_X = 60f;
    static final float TITLE_PANEL_Y = 1096f;
    static final float TITLE_PANEL_WIDTH = 600f;
    static final float TITLE_PANEL_HEIGHT = 128f;
    static final float CONTEXT_PANEL_X = 60f;
    static final float CONTEXT_PANEL_Y = 250f;
    static final float CONTEXT_PANEL_WIDTH = 600f;
    static final float CONTEXT_PANEL_HEIGHT = 108f;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OverlayText text = new OverlayText();

    public void draw(
        SpriteBatch batch,
        Matrix4 projection,
        GameState state,
        UiIconRenderer icons,
        UiFrameRenderer frames
    ) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.060f, 0.130f, 0.112f, 0.70f);
        shapes.rect(0f, ScreenEdges.bottom(), 720f, ScreenEdges.height());
        shapes.setColor(0.07f, 0.19f, 0.16f, 0.55f);
        shapes.rect(0f, 1096f, 720f, ScreenEdges.top() - 1096f);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        UiFrameRenderer.State resumeState = frames.resolve(
            true, false, PauseTouchLayout.BUTTON_X, PauseTouchLayout.RESUME_Y,
            PauseTouchLayout.BUTTON_WIDTH, PauseTouchLayout.RESUME_HEIGHT
        );
        UiFrameRenderer.State inventoryState = frames.resolve(
            true, false, PauseTouchLayout.BUTTON_X, PauseTouchLayout.INVENTORY_Y,
            PauseTouchLayout.BUTTON_WIDTH, PauseTouchLayout.SECONDARY_HEIGHT
        );
        UiFrameRenderer.State shopState = frames.resolve(
            true, false, PauseTouchLayout.BUTTON_X, PauseTouchLayout.SHOP_Y,
            PauseTouchLayout.BUTTON_WIDTH, PauseTouchLayout.SECONDARY_HEIGHT
        );
        UiFrameRenderer.State rootState = frames.resolve(
            true, false, PauseTouchLayout.BUTTON_X, PauseTouchLayout.ROOT_Y,
            PauseTouchLayout.BUTTON_WIDTH, PauseTouchLayout.SECONDARY_HEIGHT
        );
        UiFrameRenderer.State codexState = frames.resolve(
            true, false, PauseTouchLayout.BUTTON_X, PauseTouchLayout.CODEX_Y,
            PauseTouchLayout.BUTTON_WIDTH, PauseTouchLayout.SECONDARY_HEIGHT
        );

        batch.setProjectionMatrix(projection);
        batch.begin();
        frames.draw(
            batch, UiFrameRenderer.Kind.PANEL,
            TITLE_PANEL_X, TITLE_PANEL_Y, TITLE_PANEL_WIDTH, TITLE_PANEL_HEIGHT, true, false
        );
        frames.draw(
            batch, UiFrameRenderer.Kind.BUTTON,
            PauseTouchLayout.BUTTON_X, PauseTouchLayout.SHOP_Y,
            PauseTouchLayout.BUTTON_WIDTH, PauseTouchLayout.SECONDARY_HEIGHT, true, false
        );
        frames.draw(
            batch, UiFrameRenderer.Kind.BUTTON,
            PauseTouchLayout.BUTTON_X, PauseTouchLayout.INVENTORY_Y,
            PauseTouchLayout.BUTTON_WIDTH, PauseTouchLayout.SECONDARY_HEIGHT, true, false
        );
        frames.draw(
            batch, UiFrameRenderer.Kind.BUTTON,
            PauseTouchLayout.BUTTON_X, PauseTouchLayout.ROOT_Y,
            PauseTouchLayout.BUTTON_WIDTH, PauseTouchLayout.SECONDARY_HEIGHT, true, false
        );
        frames.draw(
            batch, UiFrameRenderer.Kind.BUTTON,
            PauseTouchLayout.BUTTON_X, PauseTouchLayout.CODEX_Y,
            PauseTouchLayout.BUTTON_WIDTH, PauseTouchLayout.SECONDARY_HEIGHT, true, false
        );
        frames.draw(
            batch, UiFrameRenderer.Kind.BUTTON,
            PauseTouchLayout.BUTTON_X, PauseTouchLayout.RESUME_Y,
            PauseTouchLayout.BUTTON_WIDTH, PauseTouchLayout.RESUME_HEIGHT, true, true
        );
        frames.draw(
            batch, UiFrameRenderer.Kind.PANEL,
            CONTEXT_PANEL_X, CONTEXT_PANEL_Y, CONTEXT_PANEL_WIDTH, CONTEXT_PANEL_HEIGHT,
            true, false
        );

        icons.draw(batch, "pause", UiMirror.leadingOnScreen(84f, 76f), 1122f, 76f);
        text.drawLeading(batch, GameLocale.text(PauseStrings.TITLE), 0f, UiMirror.SCREEN_WIDTH,
            180f, 1196f, 1.36f, OverlayText.GOLD);
        text.drawLeading(batch, GameLocale.text(PauseStrings.SUBTITLE), 0f, UiMirror.SCREEN_WIDTH,
            180f, 1150f, 0.74f, OverlayText.SUBTLE);

        drawAction(
            batch, icons, "shop", GameLocale.text(PauseStrings.STAT_SHOP),
            GameLocale.text(PauseStrings.STAT_SHOP_SUBTITLE),
            PauseTouchLayout.SHOP_Y, PauseTouchLayout.SECONDARY_HEIGHT, shopState
        );
        drawAction(
            batch, icons, "inventory", GameLocale.text(PauseStrings.INVENTORY),
            GameLocale.text(PauseStrings.INVENTORY_SUBTITLE),
            PauseTouchLayout.INVENTORY_Y, PauseTouchLayout.SECONDARY_HEIGHT, inventoryState
        );
        drawAction(
            batch, icons, "general_power", GameLocale.text(PauseStrings.ROOT_NETWORK),
            GameLocale.text(PauseStrings.ROOT_NETWORK_SUBTITLE),
            PauseTouchLayout.ROOT_Y, PauseTouchLayout.SECONDARY_HEIGHT, rootState
        );
        drawAction(
            batch, icons, "inventory", GameLocale.text(PauseStrings.GROVE_CODEX),
            GameLocale.text(PauseStrings.GROVE_CODEX_SUBTITLE),
            PauseTouchLayout.CODEX_Y, PauseTouchLayout.SECONDARY_HEIGHT, codexState
        );
        float resumeOffset = MainMenuRenderer.pressedOffset(resumeState);
        icons.draw(batch, "continue", UiMirror.leadingOnScreen(132f, 116f),
            PauseTouchLayout.RESUME_Y + 62f + resumeOffset, 116f, resumeState);
        text.drawLeading(batch, GameLocale.text(PauseStrings.RESUME), 0f, UiMirror.SCREEN_WIDTH,
            274f, PauseTouchLayout.RESUME_Y + 158f + resumeOffset, 1.62f, OverlayText.GOLD);
        text.drawLeading(batch, GameLocale.text(PauseStrings.RESUME_SUBTITLE), 0f, UiMirror.SCREEN_WIDTH,
            274f, PauseTouchLayout.RESUME_Y + 104f + resumeOffset, 0.80f, OverlayText.IVORY);

        icons.draw(batch, "wave", UiMirror.leadingOnScreen(84f, 62f), 272f, 62f);
        text.drawLeading(batch, waveLabel(state), 0f, UiMirror.SCREEN_WIDTH, 160f, 328f, 0.66f,
            OverlayText.GOLD);
        text.drawLeading(
            batch,
            GameLocale.text(
                PauseStrings.WAVE_VALUE,
                GameLocale.number(state.waveNumber), GameLocale.number(GameState.FINAL_WAVE)
            ),
            0f, UiMirror.SCREEN_WIDTH, 160f, 294f, 0.96f, OverlayText.IVORY
        );
        icons.draw(batch, "coin", UiMirror.leadingOnScreen(392f, 62f), 272f, 62f);
        text.drawLeading(batch, GameLocale.text(PauseStrings.COINS), 0f, UiMirror.SCREEN_WIDTH, 468f,
            328f, 0.66f, OverlayText.GOLD);
        text.drawLeading(batch, MainMenuRenderer.coinTotalLabel(state.coins), 0f, UiMirror.SCREEN_WIDTH,
            468f, 294f, 0.96f, OverlayText.IVORY);
        batch.end();
    }

    private void drawAction(
        SpriteBatch batch,
        UiIconRenderer icons,
        String icon,
        String title,
        String subtitle,
        float y,
        float height,
        UiFrameRenderer.State state
    ) {
        float offset = MainMenuRenderer.pressedOffset(state);
        icons.draw(batch, icon, UiMirror.leadingOnScreen(132f, 84f), y + (height - 84f) * 0.5f + offset,
            84f, state);
        text.drawLeading(batch, title, 0f, UiMirror.SCREEN_WIDTH, 246f, y + height - 40f + offset,
            1.16f, OverlayText.IVORY);
        text.drawLeading(batch, subtitle, 0f, UiMirror.SCREEN_WIDTH, 246f, y + 44f + offset,
            0.72f, OverlayText.SUBTLE);
    }

    /**
     * The caption over the wave readout. The words are the table's, so the upper-casing the call site used to
     * do is now a property of the entry.
     */
    static String waveLabel(GameState state) {
        if (state == null) return GameLocale.text(PauseStrings.CURRENT_WAVE);
        if (state.waveNumber % 5 == 0) return GameLocale.text(PauseStrings.BOSS_WAVE);
        return state.heroLevel > 0
            ? GameLocale.text(PauseStrings.HERO_LEVEL, GameLocale.number(state.heroLevel))
            : GameLocale.text(PauseStrings.CURRENT_WAVE);
    }

    @Override
    public void close() {
        text.close();
        shapes.dispose();
    }
}

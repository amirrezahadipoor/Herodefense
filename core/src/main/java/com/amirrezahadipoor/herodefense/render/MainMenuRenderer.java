package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.MenuStrings;
import com.amirrezahadipoor.herodefense.input.MainMenuTouchLayout;

/** Premium touch-first menu using the reviewed arena, Heartwood frames, and clear type hierarchy. */
public final class MainMenuRenderer implements AutoCloseable {
    static final float TITLE_PANEL_X = 44f;
    static final float TITLE_PANEL_Y = 912f;
    static final float TITLE_PANEL_WIDTH = 632f;
    static final float TITLE_PANEL_HEIGHT = 252f;
    static final float COIN_PANEL_X = 500f;
    static final float COIN_PANEL_Y = 1192f;
    static final float COIN_PANEL_WIDTH = 176f;
    static final float COIN_PANEL_HEIGHT = 64f;
    /** The coin icon's inset inside its own panel: 508f - 500f. The panel is not centred on the screen, so its
     *  contents mirror about the panel and not about the screen. */
    static final float COIN_ICON_INSET = 8f;

    private static final Color GOLD = Color.valueOf("F2D58A");
    private static final Color IVORY = Color.valueOf("F3E4BC");
    private static final Color MUTED = Color.valueOf("85877E");
    private static final Color SUBTLE = Color.valueOf("B8C4AF");

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OverlayText text = new OverlayText();
    private Texture backdrop;

    public MainMenuRenderer() {
    }

    public void draw(
        SpriteBatch batch,
        Matrix4 projection,
        boolean continueAvailable,
        int coins,
        UiIconRenderer icons,
        UiFrameRenderer frames,
        com.amirrezahadipoor.herodefense.model.GameState state
    ) {
        draw(batch, projection, continueAvailable, coins, icons, frames, state != null ? state.ascensionTier : 0, state != null ? state.peakWaveReached : 0, state != null ? state.heartwood : 0);
    }

    public void draw(
        SpriteBatch batch,
        Matrix4 projection,
        boolean continueAvailable,
        int coins,
        UiIconRenderer icons,
        UiFrameRenderer frames
    ) {
        draw(batch, projection, continueAvailable, coins, icons, frames, 0, 0, 0);
    }

    private void draw(
        SpriteBatch batch,
        Matrix4 projection,
        boolean continueAvailable,
        int coins,
        UiIconRenderer icons,
        UiFrameRenderer frames,
        int ascensionTier,
        int peakWave,
        int heartwood
    ) {
        batch.setProjectionMatrix(projection);
        batch.begin();
        batch.setColor(0.82f, 0.90f, 0.86f, 1f);
        ScreenEdges.drawCover(batch, backdrop());
        batch.setColor(Color.WHITE);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.060f, 0.130f, 0.112f, 0.40f);
        shapes.rect(0f, ScreenEdges.bottom(), 720f, ScreenEdges.height());
        shapes.setColor(0.040f, 0.095f, 0.085f, 0.34f);
        shapes.rect(0f, ScreenEdges.bottom(), 54f, ScreenEdges.height());
        shapes.rect(666f, ScreenEdges.bottom(), 54f, ScreenEdges.height());
        shapes.setColor(0.05f, 0.15f, 0.12f, 0.45f);
        shapes.rect(0f, ScreenEdges.bottom(), 720f, 220f - ScreenEdges.bottom());
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // The frame, the label and the press state all come off the layout's rows. These resolutions used to
        // carry the old five-row table's 780/620/460/300/140, so pressing a drawn button lit whichever button was
        // nearest its stale rectangle, and pressing Brief Vigil lit New Game because the two shared one state.
        UiFrameRenderer.State newGameState = frames.resolve(
            true, false, MainMenuTouchLayout.BUTTON_X, MainMenuTouchLayout.rowBottom(0),
            MainMenuTouchLayout.BUTTON_WIDTH, MainMenuTouchLayout.BUTTON_HEIGHT
        );
        UiFrameRenderer.State briefState = frames.resolve(
            true, false, MainMenuTouchLayout.BUTTON_X, MainMenuTouchLayout.rowBottom(1),
            MainMenuTouchLayout.BUTTON_WIDTH, MainMenuTouchLayout.BUTTON_HEIGHT
        );
        UiFrameRenderer.State continueState = frames.resolve(
            continueAvailable, false, MainMenuTouchLayout.BUTTON_X, MainMenuTouchLayout.rowBottom(2),
            MainMenuTouchLayout.BUTTON_WIDTH, MainMenuTouchLayout.BUTTON_HEIGHT
        );
        UiFrameRenderer.State rootState = frames.resolve(
            true, false, MainMenuTouchLayout.BUTTON_X, MainMenuTouchLayout.rowBottom(3),
            MainMenuTouchLayout.BUTTON_WIDTH, MainMenuTouchLayout.BUTTON_HEIGHT
        );
        UiFrameRenderer.State codexState = frames.resolve(
            true, false, MainMenuTouchLayout.BUTTON_X, MainMenuTouchLayout.rowBottom(4),
            MainMenuTouchLayout.BUTTON_WIDTH, MainMenuTouchLayout.BUTTON_HEIGHT
        );
        UiFrameRenderer.State settingsState = frames.resolve(
            true, false, MainMenuTouchLayout.BUTTON_X, MainMenuTouchLayout.rowBottom(5),
            MainMenuTouchLayout.BUTTON_WIDTH, MainMenuTouchLayout.BUTTON_HEIGHT
        );

        batch.begin();
        frames.draw(
            batch, UiFrameRenderer.Kind.PANEL,
            TITLE_PANEL_X, TITLE_PANEL_Y, TITLE_PANEL_WIDTH, TITLE_PANEL_HEIGHT,
            true, false
        );
        frames.draw(
            batch, UiFrameRenderer.Kind.PANEL,
            UiMirror.leadingOnScreen(COIN_PANEL_X, COIN_PANEL_WIDTH), COIN_PANEL_Y,
            COIN_PANEL_WIDTH, COIN_PANEL_HEIGHT,
            true, false
        );
        drawButton(batch, frames, MainMenuTouchLayout.rowBottom(0), true);
        drawButton(batch, frames, MainMenuTouchLayout.rowBottom(1), true);
        drawButton(batch, frames, MainMenuTouchLayout.rowBottom(2), continueAvailable);
        drawButton(batch, frames, MainMenuTouchLayout.rowBottom(3), true);
        drawButton(batch, frames, MainMenuTouchLayout.rowBottom(4), true);
        drawButton(batch, frames, MainMenuTouchLayout.rowBottom(5), true);

        icons.draw(batch, "coin",
            UiMirror.leading(COIN_PANEL_X, COIN_PANEL_WIDTH, COIN_ICON_INSET, 46f), 1201f, 46f);
        drawShadowedCentered(batch, coinTotalLabel(coins),
            UiMirror.centre(COIN_PANEL_X, COIN_PANEL_WIDTH, 611f), 1232f, 1.05f, GOLD);
        drawShadowedCentered(batch, GameLocale.text(MenuStrings.TAGLINE), 360f, 1120f, 0.86f, GOLD);
        drawShadowedCentered(batch, GameLocale.text(MenuStrings.TITLE), 360f, 1058f, 2.28f, GOLD);
        drawShadowedCentered(
            batch, GameLocale.text(MenuStrings.PITCH),
            360f, 988f, 0.92f, IVORY
        );

        drawMenuAction(
            batch, icons, "new_game", GameLocale.text(MenuStrings.NEW_GAME),
            GameLocale.text(MenuStrings.NEW_GAME_SUBTITLE),
            780f, newGameState, true
        );
        // The three numbers are formatted rather than concatenated so the row reads "ردهٔ ۳ | اوج ۴۱ | ۱۲ چوب دل"
        // in Persian digits and does not mix two numbering systems inside one sentence.
        String continueSubtitle = continueAvailable
            ? GameLocale.text(
                MenuStrings.PROGRESS_SUMMARY,
                GameLocale.number(ascensionTier), GameLocale.number(peakWave), GameLocale.number(heartwood)
            )
            : GameLocale.text(MenuStrings.CONTINUE_SUBTITLE);
        drawMenuAction(
            batch, icons, "general_power", GameLocale.text(MenuStrings.BRIEF_VIGIL),
            GameLocale.text(MenuStrings.BRIEF_VIGIL_SUBTITLE),
            MainMenuTouchLayout.rowBottom(1), briefState, true
        );
        drawMenuAction(
            batch, icons, "continue", GameLocale.text(MenuStrings.CONTINUE), continueSubtitle,
            MainMenuTouchLayout.rowBottom(2), continueState, continueAvailable
        );
        drawMenuAction(
            batch, icons, "general_power", GameLocale.text(MenuStrings.ROOT_NETWORK),
            GameLocale.text(MenuStrings.ROOT_NETWORK_SUBTITLE, GameLocale.number(heartwood)),
            MainMenuTouchLayout.rowBottom(3), rootState, true
        );
        drawMenuAction(
            batch, icons, "inventory", GameLocale.text(MenuStrings.GROVE_CODEX),
            GameLocale.text(MenuStrings.GROVE_CODEX_SUBTITLE),
            MainMenuTouchLayout.rowBottom(4), codexState, true
        );
        drawMenuAction(
            batch, icons, "settings", GameLocale.text(MenuStrings.SETTINGS),
            GameLocale.text(MenuStrings.SETTINGS_SUBTITLE),
            MainMenuTouchLayout.rowBottom(5), settingsState, true
        );
        drawShadowedCentered(
            batch, GameLocale.text(MenuStrings.FOOTER, GameLocale.number(ascensionTier)),
            360f, 80f, 0.74f, SUBTLE
        );
        batch.end();
    }

    private void drawButton(SpriteBatch batch, UiFrameRenderer frames, float y, boolean enabled) {
        frames.draw(
            batch, UiFrameRenderer.Kind.BUTTON,
            MainMenuTouchLayout.BUTTON_X, y,
            MainMenuTouchLayout.BUTTON_WIDTH, MainMenuTouchLayout.BUTTON_HEIGHT,
            enabled, false
        );
    }

    private void drawMenuAction(
        SpriteBatch batch,
        UiIconRenderer icons,
        String icon,
        String title,
        String subtitle,
        float y,
        UiFrameRenderer.State state,
        boolean enabled
    ) {
        float offset = pressedOffset(state);
        Color primary = enabled ? IVORY : MUTED;
        Color secondary = enabled ? SUBTLE : MUTED;
        icons.draw(batch, icon, UiMirror.leadingOnScreen(146f, 82f), y + 27f + offset, 82f, state);
        drawShadowed(batch, title, 258f, y + 92f + offset, 1.34f, primary);
        drawShadowed(batch, subtitle, 258f, y + 49f + offset, 0.78f, secondary);
    }

    private void drawShadowedCentered(
        SpriteBatch batch, String label, float centerX, float y, float scale, Color color
    ) {
        text.drawCentered(batch, label, centerX, y, scale, color);
    }

    /**
     * A run that starts {@code inset} from the screen's leading edge. Both callers are the two lines of a menu
     * row, and the row's button spans 120f..600f of a 720f screen, so mirroring about the screen's centre keeps
     * both lines inside the button they belong to.
     */
    private void drawShadowed(
        SpriteBatch batch, String label, float inset, float y, float scale, Color color
    ) {
        text.drawLeading(batch, label, 0f, UiMirror.SCREEN_WIDTH, inset, y, scale, color);
    }

    private Texture backdrop() {
        if (backdrop == null) {
            backdrop = SheetPayloads.texture("generated/environment/arena_backdrop.png");
            backdrop.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        return backdrop;
    }

    static float pressedOffset(UiFrameRenderer.State state) {
        return state == UiFrameRenderer.State.PRESSED ? -4f : 0f;
    }

    /**
     * The coin count as the three screens that draw it draw it: the menu's corner, the pause overlay and the
     * end screen all call this rather than keeping their own copy of the format.
     */
    static String coinTotalLabel(int coins) {
        return GameLocale.text(MenuStrings.COINS, GameLocale.number(Math.max(0, coins)));
    }

    @Override
    public void close() {
        if (backdrop != null) backdrop.dispose();
        text.close();
        shapes.dispose();
    }
}

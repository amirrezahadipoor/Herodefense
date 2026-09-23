package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.SettingsStrings;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.amirrezahadipoor.herodefense.input.SettingsTouchLayout;
import com.amirrezahadipoor.herodefense.settings.GameSettings;

/**
 * Premium settings surface: large explicit ON/OFF toggles, level rows and accessibility
 * rows inside a scrollable viewport over the reviewed arena.
 *
 * <p>Every word on this screen comes from {@link SettingsStrings} through {@link GameLocale}, and
 * {@code DrawnStringProvenanceTest} fails the build if a literal is drawn here instead. Every element is placed
 * by {@link UiMirror} rather than by a number, so the layout keeps its leading and trailing edges straight.
 */
public final class SettingsOverlayRenderer implements AutoCloseable {
    static final float CLOSE_X = 570f;
    static final float CLOSE_Y = 1120f;
    static final float CLOSE_SIZE = 100f;
    static final float SOUND_ROW_Y = SettingsTouchLayout.SOUND_ROW_Y;
    static final float MUSIC_ROW_Y = SettingsTouchLayout.MUSIC_ROW_Y;
    static final float SOUND_LEVEL_ROW_Y = SettingsTouchLayout.SOUND_LEVEL_ROW_Y;
    static final float MUSIC_LEVEL_ROW_Y = SettingsTouchLayout.MUSIC_LEVEL_ROW_Y;
    static final float REDUCED_MOTION_ROW_Y = SettingsTouchLayout.REDUCED_MOTION_ROW_Y;
    /**
     * The insets this screen's elements sit at, named because a mirrored screen has to move each of them by the
     * same distance from the other edge: 136f is {@code ROW_X + ROW_TEXT_INSET} and 590f is the row's right edge
     * minus {@code ROW_VALUE_INSET}. The numbers the draws used to carry were those sums, already added.
     */
    static final float HEADER_ICON_INSET = 60f;
    static final float HEADER_ICON_SIZE = 76f;
    static final float HEADER_TEXT_INSET = 156f;
    static final float CLOSE_ICON_INSET = 68f;
    static final float CLOSE_ICON_SIZE = 64f;
    static final float ROW_TEXT_INSET = 36f;
    static final float ROW_VALUE_INSET = 30f;
    static final float NOTE_INSET = 30f;
    static final float NOTE_PANEL_X = 100f;
    /**
     * Below the rows. The panel sits apart from the row spacing, because the rows' spacing is what
     * {@code MainMenuAndSettingsTouchTest} measures against {@code ROW_HEIGHT} and the panel is decoration.
     */
    static final float NOTE_PANEL_Y = 140f;
    static final float NOTE_PANEL_WIDTH = 520f;
    static final float NOTE_PANEL_HEIGHT = 120f;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OverlayText text = new OverlayText();
    private Texture backdrop;

    public void draw(
        SpriteBatch batch,
        Matrix4 projection,
        GameSettings settings,
        UiIconRenderer icons,
        UiFrameRenderer frames
    ) {
        draw(batch, projection, settings, icons, frames, 0);
    }

    public void draw(
        SpriteBatch batch,
        Matrix4 projection,
        GameSettings settings,
        UiIconRenderer icons,
        UiFrameRenderer frames,
        int firstVisibleIndex
    ) {
        batch.setProjectionMatrix(projection);
        batch.begin();
        // The backdrop at its own brightness under a lighter scrim, the way the menu draws it now: it was
        // 0.70/0.80/0.76 under a 0.58 scrim, the darkest screen the game had.
        batch.setColor(1f, 1f, 1f, 1f);
        ScreenEdges.drawCover(batch, backdrop());
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.060f, 0.130f, 0.112f, 0.44f);
        shapes.rect(0f, ScreenEdges.bottom(), 720f, ScreenEdges.height());
        shapes.setColor(0.07f, 0.19f, 0.16f, 0.62f);
        shapes.rect(0f, 1096f, 720f, ScreenEdges.top() - 1096f);

        int totalRows = SettingsTouchLayout.TOTAL_ROWS;
        if (totalRows > SettingsTouchLayout.VISIBLE_ROWS) {
            float railX = UiMirror.trailingOnScreen(42f, 6f);
            float trackY = SettingsTouchLayout.REDUCED_MOTION_ROW_Y;
            float trackHeight = (SettingsTouchLayout.SOUND_ROW_Y + SettingsTouchLayout.ROW_HEIGHT) - trackY;
            shapes.setColor(0.15f, 0.28f, 0.24f, 0.45f);
            shapes.rect(railX, trackY, 6f, trackHeight);

            float thumbHeight = trackHeight * ((float) SettingsTouchLayout.VISIBLE_ROWS / totalRows);
            int maxFirst = totalRows - SettingsTouchLayout.VISIBLE_ROWS;
            float scrollFraction = maxFirst > 0 ? (float) firstVisibleIndex / maxFirst : 0f;
            float thumbY = trackY + (trackHeight - thumbHeight) * (1f - scrollFraction);
            shapes.setColor(0.85f, 0.72f, 0.38f, 0.65f);
            shapes.rect(railX, thumbY, 6f, thumbHeight);
        }
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        UiFrameRenderer.State closeState = frames.resolve(
            true, false, CLOSE_X, CLOSE_Y, CLOSE_SIZE, CLOSE_SIZE
        );

        batch.begin();
        frames.draw(batch, UiFrameRenderer.Kind.BUTTON, CLOSE_X, CLOSE_Y, CLOSE_SIZE, CLOSE_SIZE,
            true, false);

        for (int slot = 0; slot < SettingsTouchLayout.VISIBLE_ROWS; slot++) {
            int rowIndex = firstVisibleIndex + slot;
            if (rowIndex >= totalRows) break;
            float y = SettingsTouchLayout.slotY(slot);
            drawRow(batch, frames, rowIndex, y, settings);
        }

        frames.draw(batch, UiFrameRenderer.Kind.PANEL, NOTE_PANEL_X, NOTE_PANEL_Y,
            NOTE_PANEL_WIDTH, NOTE_PANEL_HEIGHT, true, false);

        icons.draw(batch, "settings",
            UiMirror.leadingOnScreen(HEADER_ICON_INSET, HEADER_ICON_SIZE), 1122f, HEADER_ICON_SIZE);
        text.drawLeading(batch, GameLocale.text(SettingsStrings.TITLE), 0f, UiMirror.SCREEN_WIDTH,
            HEADER_TEXT_INSET, 1196f, 1.36f, OverlayText.GOLD);
        text.drawLeading(batch, GameLocale.text(SettingsStrings.FOOTER), 0f, UiMirror.SCREEN_WIDTH,
            HEADER_TEXT_INSET, 1150f, 0.74f, OverlayText.SUBTLE);
        icons.draw(batch, "close",
            UiMirror.trailingOnScreen(CLOSE_ICON_INSET, CLOSE_ICON_SIZE), 1138f, CLOSE_ICON_SIZE,
            closeState);

        text.drawLeading(batch, GameLocale.text(SettingsStrings.TOUCH_ONLY), NOTE_PANEL_X,
            NOTE_PANEL_WIDTH, NOTE_INSET, 226f, 0.66f, OverlayText.GOLD);
        // 0.66f, down from 0.74f: at the larger step this line ran past the panel's right border on the CI
        // screenshot, and a note that overflows the box drawn around it is a box that is not framing anything.
        text.drawLeading(batch, GameLocale.text(SettingsStrings.HINT), NOTE_PANEL_X,
            NOTE_PANEL_WIDTH, NOTE_INSET, 192f, 0.66f, OverlayText.IVORY);
        text.drawLeading(batch, GameLocale.text(SettingsStrings.CLOSE_HINT), NOTE_PANEL_X,
            NOTE_PANEL_WIDTH, NOTE_INSET, 164f, 0.74f, OverlayText.SUBTLE);
        batch.end();
    }

    private void drawRow(
        SpriteBatch batch,
        UiFrameRenderer frames,
        int rowIndex,
        float y,
        GameSettings settings
    ) {
        switch (rowIndex) {
            case 0 -> {
                UiFrameRenderer.State state = frames.resolve(
                    true, settings.soundEnabled, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
                );
                frames.draw(
                    batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true,
                    settings.soundEnabled
                );
                drawToggle(batch, SettingsStrings.SOUND_EFFECTS, SettingsStrings.SOUND_EFFECTS_SUBTITLE,
                    y, settings.soundEnabled, state);
            }
            case 1 -> {
                UiFrameRenderer.State state = frames.resolve(
                    true, settings.musicEnabled, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
                );
                frames.draw(
                    batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true,
                    settings.musicEnabled
                );
                drawToggle(batch, SettingsStrings.MUSIC, SettingsStrings.MUSIC_SUBTITLE,
                    y, settings.musicEnabled, state);
            }
            case 2 -> {
                UiFrameRenderer.State state = frames.resolve(
                    true, settings.soundVolume > 0f, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
                );
                frames.draw(batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true,
                    settings.soundVolume > 0f);
                drawLevel(batch, SettingsStrings.EFFECT_LEVEL, SettingsStrings.EFFECT_LEVEL_SUBTITLE,
                    y, settings.soundVolume, state);
            }
            case 3 -> {
                UiFrameRenderer.State state = frames.resolve(
                    true, settings.musicVolume > 0f, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
                );
                frames.draw(batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true,
                    settings.musicVolume > 0f);
                drawLevel(batch, SettingsStrings.MUSIC_LEVEL, SettingsStrings.MUSIC_LEVEL_SUBTITLE,
                    y, settings.musicVolume, state);
            }
            case 4 -> {
                UiFrameRenderer.State state = frames.resolve(
                    true, settings.reducedMotion, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
                );
                frames.draw(batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true,
                    settings.reducedMotion);
                drawToggle(batch, SettingsStrings.REDUCED_MOTION, SettingsStrings.REDUCED_MOTION_SUBTITLE,
                    y, settings.reducedMotion, state, SettingsStrings.TAP_TO_RESTORE_MOTION);
            }
            case 5 -> {
                UiFrameRenderer.State state = frames.resolve(
                    true, true, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
                );
                frames.draw(batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true, true);
                drawTextSize(batch, settings, y, state);
            }
            case 6 -> {
                UiFrameRenderer.State state = frames.resolve(
                    true, settings.colourBlindRarity, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
                );
                frames.draw(batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true,
                    settings.colourBlindRarity);
                drawToggle(batch, SettingsStrings.COLOUR_BLIND_RARITY, SettingsStrings.COLOUR_BLIND_RARITY_SUBTITLE,
                    y, settings.colourBlindRarity, state, SettingsStrings.TAP_TO_RESTORE_RARITY);
            }
            case 7 -> {
                UiFrameRenderer.State state = frames.resolve(
                    true, settings.narrationEnabled, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
                );
                frames.draw(batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true,
                    settings.narrationEnabled);
                drawToggle(batch, SettingsStrings.NARRATION, SettingsStrings.NARRATION_SUBTITLE,
                    y, settings.narrationEnabled, state, SettingsStrings.TAP_TO_MUTE);
            }
            case 8 -> {
                UiFrameRenderer.State state = frames.resolve(
                    true, settings.narrationVolume > 0f, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
                );
                frames.draw(batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true,
                    settings.narrationVolume > 0f);
                drawLevel(batch, SettingsStrings.NARRATION_LEVEL, SettingsStrings.NARRATION_LEVEL_SUBTITLE,
                    y, settings.narrationVolume, state);
            }
            case 9 -> {
                UiFrameRenderer.State state = frames.resolve(
                    true, settings.screenReaderEnabled, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
                );
                frames.draw(batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, y,
                    SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true,
                    settings.screenReaderEnabled);
                drawToggle(batch, SettingsStrings.SCREEN_READER, SettingsStrings.SCREEN_READER_SUBTITLE,
                    y, settings.screenReaderEnabled, state, SettingsStrings.TAP_TO_MUTE);
            }
            default -> {}
        }
    }

    private void drawToggle(
        SpriteBatch batch,
        SettingsStrings title,
        SettingsStrings subtitle,
        float y,
        boolean enabled,
        UiFrameRenderer.State state
    ) {
        drawToggle(batch, title, subtitle, y, enabled, state, SettingsStrings.TAP_TO_MUTE);
    }

    /**
     * A toggle row, with the hint it shows while it is on chosen by the caller: \"tap to mute\" is right for the
     * two audio rows and wrong for reduced motion, where on is the calm state and the way back is what the
     * player might want. The offsets are the row's own -- 90f and 46f of a 130f row are where 104f and 54f of
     * the 150f row were -- and they moved with the row instead of being left floating inside a shorter frame.
     */
    private void drawToggle(
        SpriteBatch batch,
        SettingsStrings title,
        SettingsStrings subtitle,
        float y,
        boolean enabled,
        UiFrameRenderer.State state,
        SettingsStrings onHint
    ) {
        float offset = MainMenuRenderer.pressedOffset(state);
        rowTitle(batch, GameLocale.text(title), y + 90f + offset, 1.16f, OverlayText.IVORY);
        rowTitle(batch, GameLocale.text(subtitle), y + 46f + offset, 0.68f, OverlayText.SUBTLE);
        rowValue(batch, toggleLabel(enabled), y + 84f + offset, 1.14f,
            enabled ? OverlayText.GOLD : OverlayText.MUTED);
        rowValue(batch,
            GameLocale.text(enabled ? onHint : SettingsStrings.TAP_TO_ENABLE),
            y + 46f + offset, 0.62f, OverlayText.SUBTLE);
    }

    private void drawLevel(
        SpriteBatch batch,
        SettingsStrings title,
        SettingsStrings subtitle,
        float y,
        float volume,
        UiFrameRenderer.State state
    ) {
        float offset = MainMenuRenderer.pressedOffset(state);
        rowTitle(batch, GameLocale.text(title), y + 90f + offset, 1.16f, OverlayText.IVORY);
        rowTitle(batch, GameLocale.text(subtitle), y + 46f + offset, 0.68f, OverlayText.SUBTLE);
        rowValue(batch, GameSettings.levelLabel(GameSettings.levelIndex(volume)),
            y + 84f + offset, 1.06f, OverlayText.GOLD);
        rowValue(batch, GameLocale.text(SettingsStrings.TAP_TO_STEP),
            y + 46f + offset, 0.62f, OverlayText.SUBTLE);
    }

    private void drawTextSize(
        SpriteBatch batch,
        GameSettings settings,
        float y,
        UiFrameRenderer.State state
    ) {
        float offset = MainMenuRenderer.pressedOffset(state);
        rowTitle(batch, GameLocale.text(SettingsStrings.TEXT_SIZE), y + 90f + offset, 1.16f,
            OverlayText.IVORY);
        rowTitle(batch, GameLocale.text(SettingsStrings.TEXT_SIZE_SUBTITLE), y + 46f + offset,
            0.68f, OverlayText.SUBTLE);
        rowValue(batch, settings.textSizeLabel(), y + 84f + offset, 1.06f, OverlayText.GOLD);
        rowValue(batch, GameLocale.text(SettingsStrings.TAP_TO_STEP),
            y + 46f + offset, 0.62f, OverlayText.SUBTLE);
    }

    /**
     * A row's two leading lines and its two trailing ones. All six rows call these rather than drawing at a
     * number, because the number is a sum of the row's edge and an inset and only the inset survives mirroring:
     * 136f means \"36f in from the leading edge\", whichever side that is.
     */
    private void rowTitle(SpriteBatch batch, String value, float y, float scale, Color color) {
        text.drawLeading(batch, value, SettingsTouchLayout.ROW_X, SettingsTouchLayout.ROW_WIDTH,
            ROW_TEXT_INSET, y, scale, color);
    }

    private void rowValue(SpriteBatch batch, String value, float y, float scale, Color color) {
        text.drawTrailing(batch, value, SettingsTouchLayout.ROW_X, SettingsTouchLayout.ROW_WIDTH,
            ROW_VALUE_INSET, y, scale, color);
    }

    static String toggleLabel(boolean enabled) {
        return GameLocale.text(enabled ? SettingsStrings.ON : SettingsStrings.OFF);
    }

    private Texture backdrop() {
        if (backdrop == null) {
            backdrop = SheetPayloads.texture("generated/environment/arena_backdrop.png");
            backdrop.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        return backdrop;
    }

    @Override
    public void close() {
        text.close();
        shapes.dispose();
        if (backdrop != null) {
            backdrop.dispose();
            backdrop = null;
        }
    }
}

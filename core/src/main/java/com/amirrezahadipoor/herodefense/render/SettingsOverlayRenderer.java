package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.i18n.GameLanguage;
import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.SettingsStrings;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.amirrezahadipoor.herodefense.input.SettingsTouchLayout;
import com.amirrezahadipoor.herodefense.settings.GameSettings;

/**
 * Premium settings surface: two large explicit ON/OFF toggles, the two level rows and the language row, over the
 * reviewed arena.
 *
 * <p>Every word on this screen comes from {@link SettingsStrings} through {@link GameLocale}, and
 * {@code SettingsStringsTest} fails if a literal reaches the text layer instead -- which matters more here than
 * anywhere else, because this is the screen where a Persian player switches to Persian. If the row that changes
 * the language were itself a hard-coded English string, the player who needs it could not read it.
 *
 * <p>The row is drawn with each language's own name for itself -- "English" and "فارسی" -- rather than the
 * current language's word for it. A player reading a screen they do not understand is looking for the word they
 * recognise, and "Persian" is not it.
 */
public final class SettingsOverlayRenderer implements AutoCloseable {
    static final float CLOSE_X = 570f;
    static final float CLOSE_Y = 1120f;
    static final float CLOSE_SIZE = 100f;
    static final float SOUND_ROW_Y = SettingsTouchLayout.SOUND_ROW_Y;
    static final float MUSIC_ROW_Y = SettingsTouchLayout.MUSIC_ROW_Y;
    static final float SOUND_LEVEL_ROW_Y = SettingsTouchLayout.SOUND_LEVEL_ROW_Y;
    static final float MUSIC_LEVEL_ROW_Y = SettingsTouchLayout.MUSIC_LEVEL_ROW_Y;
    static final float LANGUAGE_ROW_Y = SettingsTouchLayout.LANGUAGE_ROW_Y;
    static final float NOTE_PANEL_X = 100f;
    /**
     * Below the fifth row. The note used to sit at 250f, which is where the language row now is; the panel moved
     * down rather than the rows moving up, because the rows' spacing is what {@code MainMenuAndSettingsTouchTest}
     * measures against {@code ROW_HEIGHT} and the panel is decoration.
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
        batch.setProjectionMatrix(projection);
        batch.begin();
        batch.setColor(0.70f, 0.80f, 0.76f, 1f);
        ScreenEdges.drawCover(batch, backdrop());
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.060f, 0.130f, 0.112f, 0.58f);
        shapes.rect(0f, ScreenEdges.bottom(), 720f, ScreenEdges.height());
        shapes.setColor(0.07f, 0.19f, 0.16f, 0.62f);
        shapes.rect(0f, 1096f, 720f, ScreenEdges.top() - 1096f);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        UiFrameRenderer.State closeState = frames.resolve(
            true, false, CLOSE_X, CLOSE_Y, CLOSE_SIZE, CLOSE_SIZE
        );
        UiFrameRenderer.State soundState = frames.resolve(
            true, settings.soundEnabled, SettingsTouchLayout.ROW_X, SOUND_ROW_Y,
            SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
        );
        UiFrameRenderer.State musicState = frames.resolve(
            true, settings.musicEnabled, SettingsTouchLayout.ROW_X, MUSIC_ROW_Y,
            SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
        );
        UiFrameRenderer.State soundLevelState = frames.resolve(
            true, settings.soundVolume > 0f, SettingsTouchLayout.ROW_X, SOUND_LEVEL_ROW_Y,
            SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
        );
        UiFrameRenderer.State musicLevelState = frames.resolve(
            true, settings.musicVolume > 0f, SettingsTouchLayout.ROW_X, MUSIC_LEVEL_ROW_Y,
            SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
        );
        UiFrameRenderer.State languageState = frames.resolve(
            true, settings.language != GameLanguage.ENGLISH, SettingsTouchLayout.ROW_X, LANGUAGE_ROW_Y,
            SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT
        );

        batch.begin();
        frames.draw(batch, UiFrameRenderer.Kind.BUTTON, CLOSE_X, CLOSE_Y, CLOSE_SIZE, CLOSE_SIZE,
            true, false);
        frames.draw(
            batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, SOUND_ROW_Y,
            SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true,
            settings.soundEnabled
        );
        frames.draw(
            batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, MUSIC_ROW_Y,
            SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true,
            settings.musicEnabled
        );
        frames.draw(batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, SOUND_LEVEL_ROW_Y,
            SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true,
            settings.soundVolume > 0f);
        frames.draw(batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, MUSIC_LEVEL_ROW_Y,
            SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true,
            settings.musicVolume > 0f);
        frames.draw(batch, UiFrameRenderer.Kind.BUTTON, SettingsTouchLayout.ROW_X, LANGUAGE_ROW_Y,
            SettingsTouchLayout.ROW_WIDTH, SettingsTouchLayout.ROW_HEIGHT, true,
            settings.language != GameLanguage.ENGLISH);
        frames.draw(batch, UiFrameRenderer.Kind.PANEL, NOTE_PANEL_X, NOTE_PANEL_Y,
            NOTE_PANEL_WIDTH, NOTE_PANEL_HEIGHT, true, false);

        icons.draw(batch, "settings", 60f, 1122f, 76f);
        text.draw(batch, GameLocale.text(SettingsStrings.TITLE), 156f, 1196f, 1.36f, OverlayText.GOLD);
        text.draw(batch, GameLocale.text(SettingsStrings.FOOTER), 156f, 1150f, 0.74f,
            OverlayText.SUBTLE);
        icons.draw(batch, "close", 588f, 1138f, 64f, closeState);

        drawToggle(batch, SettingsStrings.SOUND_EFFECTS, SettingsStrings.SOUND_EFFECTS_SUBTITLE,
            SOUND_ROW_Y, settings.soundEnabled, soundState);
        drawToggle(batch, SettingsStrings.MUSIC, SettingsStrings.MUSIC_SUBTITLE,
            MUSIC_ROW_Y, settings.musicEnabled, musicState);

        drawLevel(batch, SettingsStrings.EFFECT_LEVEL, SettingsStrings.EFFECT_LEVEL_SUBTITLE,
            SOUND_LEVEL_ROW_Y, settings.soundVolume, soundLevelState);
        drawLevel(batch, SettingsStrings.MUSIC_LEVEL, SettingsStrings.MUSIC_LEVEL_SUBTITLE,
            MUSIC_LEVEL_ROW_Y, settings.musicVolume, musicLevelState);

        drawLanguage(batch, settings.language, LANGUAGE_ROW_Y, languageState);

        text.draw(batch, GameLocale.text(SettingsStrings.TOUCH_ONLY), 130f, 226f, 0.66f, OverlayText.GOLD);
        text.draw(batch, GameLocale.text(SettingsStrings.HINT), 130f, 192f, 0.74f,
            OverlayText.IVORY);
        text.draw(batch, GameLocale.text(SettingsStrings.CLOSE_HINT), 130f, 164f, 0.74f,
            OverlayText.SUBTLE);
        batch.end();
    }

    private void drawToggle(
        SpriteBatch batch,
        SettingsStrings title,
        SettingsStrings subtitle,
        float y,
        boolean enabled,
        UiFrameRenderer.State state
    ) {
        float offset = MainMenuRenderer.pressedOffset(state);
        text.draw(batch, GameLocale.text(title), 136f, y + 104f + offset, 1.16f, OverlayText.IVORY);
        text.draw(batch, GameLocale.text(subtitle), 136f, y + 54f + offset, 0.68f, OverlayText.SUBTLE);
        text.drawRightAligned(batch, toggleLabel(enabled), 590f, y + 96f + offset, 1.14f,
            enabled ? OverlayText.GOLD : OverlayText.MUTED);
        text.drawRightAligned(batch,
            GameLocale.text(enabled ? SettingsStrings.TAP_TO_MUTE : SettingsStrings.TAP_TO_ENABLE),
            590f, y + 54f + offset, 0.62f, OverlayText.SUBTLE);
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
        text.draw(batch, GameLocale.text(title), 136f, y + 104f + offset, 1.16f, OverlayText.IVORY);
        text.draw(batch, GameLocale.text(subtitle), 136f, y + 54f + offset, 0.68f, OverlayText.SUBTLE);
        text.drawRightAligned(
            batch,
            GameSettings.levelLabel(GameSettings.levelIndex(volume)),
            590f,
            y + 96f + offset,
            1.06f,
            OverlayText.GOLD
        );
        text.drawRightAligned(batch, GameLocale.text(SettingsStrings.TAP_TO_STEP), 590f,
            y + 54f + offset, 0.62f, OverlayText.SUBTLE);
    }

    /**
     * The language row. It is drawn like the level rows -- title and subtitle on the left, current value and the
     * hint on the right -- because a player who has read four rows already knows what the fifth one does.
     */
    private void drawLanguage(
        SpriteBatch batch,
        GameLanguage language,
        float y,
        UiFrameRenderer.State state
    ) {
        float offset = MainMenuRenderer.pressedOffset(state);
        text.draw(batch, GameLocale.text(SettingsStrings.LANGUAGE), 136f, y + 104f + offset, 1.16f,
            OverlayText.IVORY);
        text.draw(batch, GameLocale.text(SettingsStrings.LANGUAGE_SUBTITLE), 136f, y + 54f + offset,
            0.68f, OverlayText.SUBTLE);
        text.drawRightAligned(batch, nativeName(language), 590f, y + 96f + offset, 1.06f,
            OverlayText.GOLD);
        text.drawRightAligned(batch, GameLocale.text(SettingsStrings.TAP_TO_SWITCH), 590f,
            y + 54f + offset, 0.62f, OverlayText.SUBTLE);
    }

    /**
     * The name a language calls itself, which is the name shown on the row: a player who cannot read the screen
     * is looking for the word they recognise, and the current language's word for it is not that word.
     */
    static String nativeName(GameLanguage language) {
        return language == GameLanguage.PERSIAN
            ? SettingsStrings.LANGUAGE_PERSIAN.persian()
            : SettingsStrings.LANGUAGE_ENGLISH.english();
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

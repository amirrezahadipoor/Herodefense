package com.amirrezahadipoor.herodefense.i18n;

/**
 * The settings screen's words (roadmap R7.3), in both shipped languages.
 *
 * <p>This screen is where the language itself is chosen, so it carries the row that switches between the two
 * columns of every other table in this package. Values were literals in
 * {@code render/SettingsOverlayRenderer}, plus the three volume step labels that lived in
 * {@code settings/GameSettings}.
 */
public enum SettingsStrings implements Translated {

    TITLE("SETTINGS", "تنظیمات"),
    FOOTER("Comfort choices saved on this device", "گزینه‌های راحتی روی این دستگاه ذخیره می‌شوند"),
    HINT("Tap a row to switch it, tap again to step the level.",
        "برای تغییر یک ردیف را لمس کنید، برای پلهٔ سطح دوباره لمس کنید."),
    CLOSE_HINT("Tap Close to return to the main menu.",
        "برای بازگشت به منوی اصلی دکمهٔ بستن را لمس کنید."),

    MUSIC("MUSIC", "موسیقی"),
    MUSIC_SUBTITLE("World Tree vigil theme", "قطعهٔ پاسداری درخت جهان"),
    MUSIC_LEVEL("MUSIC LEVEL", "سطح موسیقی"),
    MUSIC_LEVEL_SUBTITLE("How loud the vigil sits", "بلندی صدای پاسداری"),

    SOUND_EFFECTS("SOUND EFFECTS", "جلوه‌های صوتی"),
    SOUND_EFFECTS_SUBTITLE("Arrows, hits, loot", "تیرها، ضربه‌ها و غنیمت"),
    EFFECT_LEVEL("EFFECT LEVEL", "سطح جلوه‌ها"),
    EFFECT_LEVEL_SUBTITLE("Hits, drops, level-ups, and boss entrances", "ضربه‌ها، غنیمت‌ها و ارتقای سطح"),

    ON("ON", "روشن"),
    OFF("OFF", "خاموش"),
    TAP_TO_ENABLE("tap to enable", "برای فعال‌سازی لمس کنید"),
    TAP_TO_MUTE("tap to mute", "برای بی‌صدا کردن لمس کنید"),
    TAP_TO_STEP("tap to step", "برای پله لمس کنید"),
    TAP_TO_SWITCH("tap to switch", "برای تغییر لمس کنید"),

    /** The three named volume steps, which lived beside the values they label in GameSettings. */
    LEVEL_QUIET("QUIET", "آرام"),
    LEVEL_NORMAL("NORMAL", "عادی"),
    LEVEL_FULL("FULL", "کامل"),

    TOUCH_ONLY("TOUCH ONLY", "فقط لمسی"),

    LANGUAGE("LANGUAGE", "زبان"),
    LANGUAGE_SUBTITLE("The screen's words and its direction", "واژه‌های صفحه و جهت آن"),
    LANGUAGE_ENGLISH("English", "انگلیسی"),
    LANGUAGE_PERSIAN("Persian", "فارسی"),

    CLOSE("Close", "بستن");

    private final String english;
    private final String persian;

    SettingsStrings(String english, String persian) {
        this.english = english;
        this.persian = persian;
    }

    @Override
    public String key() {
        return name();
    }

    @Override
    public String english() {
        return english;
    }

    @Override
    public String persian() {
        return persian;
    }
}

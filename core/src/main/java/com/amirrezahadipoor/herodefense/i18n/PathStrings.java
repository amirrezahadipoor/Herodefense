package com.amirrezahadipoor.herodefense.i18n;

/**
 * The hero-path phase of the pre-run draft (roadmap B3): the four path cards, their bend-one/price-one
 * lines, and the header and footer around them. Same screen, same card grammar as the trial draft that
 * follows -- only the words change.
 */
public enum PathStrings implements Translated {
    HEADER_TITLE(
        "path_header_title",
        "CHOOSE YOUR PATH",
        "راه خود را برگزینید"),
    HEADER_SUB(
        "path_header_sub",
        "One path shapes every wave",
        "یک راه تمام موج‌ها را شکل می‌دهد"),
    CARD_CORNER(
        "path_card_corner",
        "PATH",
        "راه"),
    FOOTER_NOTE(
        "path_footer_note",
        "Your path lasts until the vigil ends",
        "راه شما تا پایان نگهبانی می‌ماند"),
    NAME_UNBOUND(
        "path_name_unbound",
        "UNBOUND",
        "آزاده"),
    REWARD_UNBOUND(
        "path_reward_unbound",
        "The classic numbers",
        "عددهای کلاسیک"),
    RISK_UNBOUND(
        "path_risk_unbound",
        "Nothing is bent",
        "هیچ عددی خم نمی‌شود"),
    NAME_ROOT(
        "path_name_root",
        "PATH OF THE ROOT",
        "راه ریشه"),
    REWARD_ROOT(
        "path_reward_root",
        "+25% max health",
        "۲۵٪ جان بیشینه بیشتر"),
    RISK_ROOT(
        "path_risk_root",
        "-10% damage",
        "۱۰٪ آسیب کمتر"),
    NAME_WIND(
        "path_name_wind",
        "PATH OF THE WIND",
        "راه باد"),
    REWARD_WIND(
        "path_reward_wind",
        "+15% attack speed",
        "۱۵٪ سرعت حمله بیشتر"),
    RISK_WIND(
        "path_risk_wind",
        "-10% max health",
        "۱۰٪ جان بیشینه کمتر"),
    NAME_STAR(
        "path_name_star",
        "PATH OF THE STAR",
        "راه ستاره"),
    REWARD_STAR(
        "path_reward_star",
        "+25% focus gain",
        "۲۵٪ تمرکز بیشتر"),
    RISK_STAR(
        "path_risk_star",
        "-10% attack speed",
        "۱۰٪ سرعت حمله کمتر");

    private final String key;
    private final String english;
    private final String persian;

    PathStrings(String key, String english, String persian) {
        this.key = key;
        this.english = english;
        this.persian = persian;
    }

    @Override
    public String key() {
        return key;
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

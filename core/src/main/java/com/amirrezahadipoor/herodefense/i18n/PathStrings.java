package com.amirrezahadipoor.herodefense.i18n;

/**
 * The hero-path phase of the pre-run draft (roadmap B3): the four path cards, their bend-one/price-one
 * lines, and the header and footer around them. Same screen, same card grammar as the trial draft that
 * follows -- only the words change.
 */
public enum PathStrings implements Translated {
    HEADER_TITLE(
        "path_header_title",
        "CHOOSE YOUR PATH"),
    HEADER_SUB(
        "path_header_sub",
        "One path shapes every wave"),
    CARD_CORNER(
        "path_card_corner",
        "PATH"),
    FOOTER_NOTE(
        "path_footer_note",
        "Your path lasts until the vigil ends"),
    NAME_UNBOUND(
        "path_name_unbound",
        "UNBOUND"),
    REWARD_UNBOUND(
        "path_reward_unbound",
        "The classic numbers"),
    RISK_UNBOUND(
        "path_risk_unbound",
        "Nothing is bent"),
    NAME_ROOT(
        "path_name_root",
        "PATH OF THE ROOT"),
    REWARD_ROOT(
        "path_reward_root",
        "+25% max health"),
    RISK_ROOT(
        "path_risk_root",
        "-10% damage"),
    NAME_WIND(
        "path_name_wind",
        "PATH OF THE WIND"),
    REWARD_WIND(
        "path_reward_wind",
        "+15% attack speed"),
    RISK_WIND(
        "path_risk_wind",
        "-10% max health"),
    NAME_STAR(
        "path_name_star",
        "PATH OF THE STAR"),
    REWARD_STAR(
        "path_reward_star",
        "+25% focus gain"),
    RISK_STAR(
        "path_risk_star",
        "-10% attack speed");

    private final String key;
    private final String english;

    PathStrings(String key, String english) {
        this.key = key;
        this.english = english;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String english() {
        return english;
    }

}

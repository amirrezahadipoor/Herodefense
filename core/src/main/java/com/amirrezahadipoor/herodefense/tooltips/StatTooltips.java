package com.amirrezahadipoor.herodefense.tooltips;

import com.amirrezahadipoor.herodefense.model.HeroStat;
import com.amirrezahadipoor.herodefense.model.HeroStats;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * What every stat and every readout on screen means (roadmap R7.2).
 *
 * <p>The 2026-09-13 review's line about this category was blunt: "the shop/inventory value math is opaque to a
 * new player". The screens did show numbers — the level-up overlay has shown current-to-next values since Phase
 * 18 — but nothing said what the numbers *were*: a player reading "DODGE 0.5% → 1.0%" has to infer the cap, the
 * roll, and whether it applies to bosses from a bar chart.
 *
 * <p>Two rules keep this file honest rather than decorative. The numbers in the sentences are interpolated from
 * {@link HeroStats}, so a rebalance that changes a constant changes the sentence with it, and the test that
 * guards them compares the text against the constant rather than against a copied string. And the shop's
 * benefit line is no longer written in the shop: it comes from {@link #shortLine(HeroStat)} here, so the two
 * screens that teach talents cannot teach different things.
 *
 * <p>Length is a layout budget, not a taste: the help panel has room for {@link #NOTE_LINE_COUNT} lines of
 * {@link #NOTE_LINE_CHARACTER_BUDGET} characters, and a tooltip that needs a third line is a tooltip that has to
 * be made shorter. {@link #noteLines(String)} does the wrapping and is what the panel draws.
 */
public final class StatTooltips {
    /** Characters that fit one line of the help panel, measured on the panel the shop draws. */
    public static final int NOTE_LINE_CHARACTER_BUDGET = 62;
    /** Lines the help panel has room for. A tooltip that needs a third one does not fit and fails its test. */
    public static final int NOTE_LINE_COUNT = 2;
    /** Longest tooltip that fits the help panel: the two budgets above, multiplied. */
    public static final int TOOLTIP_CHARACTER_BUDGET = NOTE_LINE_CHARACTER_BUDGET * NOTE_LINE_COUNT;
    /** Longest row line; the row is a fixed-width button, so this is a layout budget as well. */
    public static final int SHORT_LINE_CHARACTER_BUDGET = 42;

    /**
     * The numbers the game puts on screen outside the five talents. Each one is something a player can see and
     * ask about, which is why they are catalogued next to the talents rather than left unexplained.
     */
    public enum Readout {
        HEALTH("HEALTH", "How much punishment you can take before the Tree falls with you. Regular hits are "
            + "capped against this number."),
        DAMAGE("DAMAGE", "What one arrow takes off an enemy. Strength adds to it, weapon upgrades multiply it: "
            + "the same strength buys less late."),
        ATTACK_SPEED("ATTACKS/S", "How many arrows leave the bow each second. Agility raises it."),
        CRIT_CHANCE("CRIT", "The chance a hit lands for extra damage. Luck raises the rating; the roll happens "
            + "per enemy hit."),
        DODGE_CHANCE("DODGE", "The chance an incoming hit misses you, capped at " + HeroStats.MAX_DODGE_PERCENT
            + "%: a full dodge build is never untouchable."),
        ITEM_FIND("DROP", "The multiplier on how often enemies drop equipment. Luck compounds it."),
        COINS("COINS", "What you can spend. Coins come from kills, from drops you walk over, and from bosses."),
        WAVE("WAVE", "Where you are in the run. Every fifth wave is a boss, every seventh carries elites."),
        KILLS("KILLS", "Enemies defeated. The Tree's health is the only progress that matters; this is the "
            + "tally."),
        HEARTWOOD("HEARTWOOD", "Earned per run's peak, spent on the Root Network between runs. It never resets.");

        private final String label;
        private final String explanation;

        Readout(String label, String explanation) {
            this.label = label;
            this.explanation = explanation;
        }

        /** The short label the HUD draws. */
        public String label() {
            return label;
        }

        /** One or two sentences: what it is, what moves it, and where it stops. */
        public String explanation() {
            return explanation;
        }
    }

    private StatTooltips() {
    }

    /**
     * The row line for a talent: what one point buys, in words. This is the line the stat shop's rows show, and
     * both screens that teach talents read it from here.
     */
    public static String shortLine(HeroStat stat) {
        if (stat == null) return "";
        return switch (stat) {
            case STRENGTH -> "+" + trim(HeroStats.DAMAGE_PER_STRENGTH) + " base damage";
            case AGILITY -> "+" + trim(HeroStats.ATTACK_SPEED_PER_AGILITY) + " attacks per second";
            case LUCK -> "+" + HeroStats.DROP_MULTIPLIER_PERCENT + "% item-drop multiplier";
            case DODGE -> "+" + trim(HeroStats.DODGE_CHANCE_PER_POINT * 100f) + "% dodge chance";
            case HEALTH -> "+" + HeroStats.MAX_HEALTH_PER_POINT_ROUNDED + " maximum HP";
        };
    }

    /**
     * The tooltip: what the talent moves, by how much, and what it is worth next to the others. Every number in
     * it comes from {@link HeroStats}, and every one of them fits the help panel.
     */
    public static String tooltip(HeroStat stat) {
        if (stat == null) return "";
        return switch (stat) {
            case STRENGTH -> "+" + trim(HeroStats.DAMAGE_PER_STRENGTH) + " damage per point, on a base of "
                + trim(HeroStats.BASE_DAMAGE) + ". Worth most early, before weapon multipliers.";

            case AGILITY -> "Attack speed: +" + trim(HeroStats.ATTACK_SPEED_PER_AGILITY)
                + " attacks per second per point, on a base of " + trim(HeroStats.BASE_ATTACKS_PER_SECOND)
                + ". More arrows, more crits.";

            case LUCK -> "Luck compounds drops: +" + HeroStats.DROP_MULTIPLIER_PERCENT
                + "% drop rate per point, and it feeds the crit rating. Slow, but an item run wants it.";

            case DODGE -> "The chance a hit misses you: +" + trim(HeroStats.DODGE_CHANCE_PER_POINT * 100f)
                + "% per point, capped at " + HeroStats.MAX_DODGE_PERCENT
                + "%. The cap means some hits always land.";

            case HEALTH -> "+" + HeroStats.MAX_HEALTH_PER_POINT_ROUNDED + " maximum HP per point, on top of "
                + trim(HeroStats.BASE_MAX_HEALTH)
                + ". The per-hit cap is measured against it: time, not immunity.";
        };
    }

    /** The label of a readout, for callers that draw it. */
    public static String label(Readout readout) {
        return readout == null ? "" : readout.label();
    }

    /** The tooltip of a readout. */
    public static String explain(Readout readout) {
        return readout == null ? "" : readout.explanation();
    }

    /**
     * Wraps a tooltip onto the help panel's lines, keeping whole words. Pure, and here rather than in the
     * renderer, so the budget the text has to live inside is the same number the test checks. A word longer than
     * the panel is left on its own line rather than cut in half: the layout gives, the words do not.
     */
    public static List<String> noteLines(String text) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return lines;
        }
        StringBuilder line = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            if (line.length() > 0 && line.length() + 1 + word.length() > NOTE_LINE_CHARACTER_BUDGET) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) {
                line.append(' ');
            }
            line.append(word);
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    /** Trims a float to the shortest text that is still exact to two decimals: 2.00 -> 2, 0.03 stays 0.03. */
    private static String trim(float value) {
        String text = String.format(Locale.ROOT, "%.2f", value);
        while (text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        if (text.endsWith(".")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}

package com.amirrezahadipoor.herodefense.i18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Every string table the game has, in one place.
 *
 * <p>Two things need to walk the whole table rather than one screen's, and neither can be trusted to find the
 * tables for itself: {@code render/GameFonts}, which rasterises a glyph set derived from what the tables can
 * actually put on screen, and {@code TranslationTableTest}, which checks the tables as tables. Without this list
 * each of them would carry its own, and a new screen's strings would be missing from one of the two -- silently
 * from the font, which is how a player ends up looking at a box.
 *
 * <p>{@code TranslationTableTest.everyTableIsListed} reads this package's sources and fails if a type implements
 * {@link Translated} without appearing here, so the list cannot fall behind the package.
 */
public final class GameStrings {

    /** The tables, one entry per screen, in the order a player meets them. */
    private static final List<Translated[]> TABLES = List.of(
        MenuStrings.values(),
        RunStrings.values(),
        OnboardingStrings.values(),
        PauseStrings.values(),
        GameOverStrings.values(),
        HudStrings.values(),
        ItemStrings.values(),
        RootNetworkStrings.values(),
        PathStrings.values(),
        TrialStrings.values(),
        StoryStrings.values(),
        SettingsStrings.values());

    private GameStrings() {
    }

    /** The tables, as arrays, for a caller that wants to keep each screen's entries together. */
    public static List<Translated[]> tables() {
        return TABLES;
    }

    /** Every entry of every table, flat. */
    public static List<Translated> all() {
        List<Translated> all = new ArrayList<>();
        for (Translated[] table : TABLES) {
            all.addAll(Arrays.asList(table));
        }
        return all;
    }

    /** How many user-facing strings the game holds, which is the number the coverage gates report against. */
    public static int count() {
        return TABLES.stream().mapToInt(table -> table.length).sum();
    }
}

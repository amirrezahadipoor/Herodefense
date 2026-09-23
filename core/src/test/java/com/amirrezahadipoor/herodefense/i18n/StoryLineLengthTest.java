package com.amirrezahadipoor.herodefense.i18n;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Every story-table line reads at a glance: at most 60 characters, one idea per line. The
 * dialogue box, the beats and the cards all draw these verbatim, so a long line is a clipped
 * line on the smallest phone -- and a young reader's line is a short line anyway.
 */
final class StoryLineLengthTest {

    private static final int MAX_LINE = 60;

    @Test
    void everyStoryLineFitsInSixtyCharacters() {
        List<String> problems = new ArrayList<>();
        for (StoryStrings entry : StoryStrings.values()) {
            int length = entry.english().length();
            if (length > MAX_LINE) {
                problems.add(entry.key() + " is " + length + " characters: " + entry.english());
            }
        }
        assertTrue(problems.isEmpty(), () -> String.join(System.lineSeparator(), problems));
    }
}

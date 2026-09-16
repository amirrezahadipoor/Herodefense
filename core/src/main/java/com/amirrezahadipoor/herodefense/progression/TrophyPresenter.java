package com.amirrezahadipoor.herodefense.progression;

import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.AudioPlayback;
import com.amirrezahadipoor.herodefense.input.HapticFeedback;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tells the player the moment a trophy is earned (roadmap R3.3).
 *
 * <p>Three small reactions, in the order a player notices them: the hand feels a tap, the ear hears the level-up
 * chime, and — only if no story line is on screen — the HUD says which trophy it was. The story line comes last
 * and yields, because a trophy must never wipe out the Tree's own words.
 */
public final class TrophyPresenter {

    /** Longest list of titles read out in one line before the rest just chime. */
    private static final int NAMES_PER_LINE = 2;

    private final AudioPlayback audio;
    private final HapticFeedback haptics;
    private final Consumer<String> storyLine;
    private final java.util.function.BooleanSupplier storyLineBusy;

    public TrophyPresenter(
        AudioPlayback audio,
        HapticFeedback haptics,
        Consumer<String> storyLine,
        java.util.function.BooleanSupplier storyLineBusy
    ) {
        this.audio = audio;
        this.haptics = haptics;
        this.storyLine = storyLine;
        this.storyLineBusy = storyLineBusy;
    }

    /** Announces newly earned trophies; does nothing when the list is empty. */
    public void announce(List<Trophy> earned) {
        if (earned == null || earned.isEmpty()) {
            return;
        }
        if (haptics != null) haptics.tap();
        if (audio != null) audio.play(AudioCue.LEVEL_UP);
        if (storyLine != null && (storyLineBusy == null || !storyLineBusy.getAsBoolean())) {
            storyLine.accept(lineFor(earned));
        }
    }

    /** "Trophy - Steady Hand and Gardener", or just the chime when there are too many to read at once. */
    static String lineFor(List<Trophy> earned) {
        if (earned.size() > NAMES_PER_LINE) {
            return "Trophy - " + earned.size() + " earned";
        }
        StringBuilder line = new StringBuilder("Trophy - ");
        for (int index = 0; index < earned.size(); index++) {
            if (index > 0) line.append(" and ");
            line.append(earned.get(index).title());
        }
        return line.toString();
    }
}

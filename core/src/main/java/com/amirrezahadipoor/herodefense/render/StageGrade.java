package com.amirrezahadipoor.herodefense.render;

/**
 * The run's colour arc, applied while the arena is drawn rather than after it is drawn.
 *
 * <p>Roadmap R5.4. The four stage recipes already existed in the review tooling
 * ({@code tools/visual/review_strips.py}, {@code STAGE_GRADES}) and every review sheet is stamped with them, but
 * nothing in the game rendered under them: the arena looked identical at wave 5 and at wave 170. This class is
 * that arc as a runtime decision. It is deliberately *not* a post-processing filter over a finished frame --
 * there is no full-screen pass and no pixel shader over the frame buffer -- the grade is a property of the
 * light the arena is drawn in, so it multiplies the ambient tint each ground tile and backdrop is drawn with.
 *
 * <p>The stops are the review tooling's numbers, and a test in {@code tools/visual/tests} reads both files and
 * fails if they drift apart, because a review strip that shows a grade the game does not render is exactly the
 * kind of claim this repository does not make. Between stops the arc interpolates linearly by wave, so crossing
 * a stage boundary is a colour that moves rather than a colour that jumps.
 *
 * <p>One honest simplification, stated rather than implied: the tooling applies the shadow lift per pixel from
 * luma, and a runtime tint has no luma to read. The lift is therefore applied as an ambient floor -- dark
 * ground is raised toward the stage teal by the same fraction -- which is what the strip's lift does to the
 * dark parts of a sprite.
 */
public final class StageGrade {
    /** Per stop: red, green and blue multipliers and the shadow lift, at the wave the stop begins. */
    private static final float[][] STOPS = {
        {1.00f, 1.00f, 1.00f, 0.00f},
        {1.02f, 0.98f, 0.92f, 0.00f},
        {0.94f, 1.00f, 1.02f, 0.00f},
        {0.86f, 0.90f, 1.00f, 0.06f},
    };
    /** The waves those stops begin at, and the names the review strips print. */
    private static final int[] STOP_WAVES = {1, 51, 101, 151};
    private static final String[] STOP_NAMES = {"DAWN 1-50", "AMBER 51-100", "TEAL 101-150", "HOLLOW 151-200"};
    /** The colour the shadow lift raises dark ground toward. */
    private static final float LIFT_RED = 0.0f;
    private static final float LIFT_GREEN = 1.0f;
    private static final float LIFT_BLUE = 0.925f;

    private final float red;
    private final float green;
    private final float blue;
    private final float lift;

    private StageGrade(float red, float green, float blue, float lift) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.lift = lift;
    }

    /** The grade a wave is played under; waves below the first stop take the first, and there is no last. */
    public static StageGrade forWave(int wave) {
        int index = STOPS.length - 1;
        for (int candidate = 0; candidate < STOP_WAVES.length; candidate++) {
            if (wave < STOP_WAVES[candidate]) {
                index = Math.max(0, candidate - 1);
                break;
            }
        }
        if (index == STOPS.length - 1 && wave >= STOP_WAVES[STOPS.length - 1]) {
            return new StageGrade(STOPS[index][0], STOPS[index][1], STOPS[index][2], STOPS[index][3]);
        }
        int next = Math.min(STOPS.length - 1, index + 1);
        if (next == index) {
            return new StageGrade(STOPS[index][0], STOPS[index][1], STOPS[index][2], STOPS[index][3]);
        }
        float span = STOP_WAVES[next] - STOP_WAVES[index];
        float progress = span <= 0f ? 1f : Math.min(1f, Math.max(0f, (wave - STOP_WAVES[index]) / span));
        return new StageGrade(
            mix(STOPS[index][0], STOPS[next][0], progress),
            mix(STOPS[index][1], STOPS[next][1], progress),
            mix(STOPS[index][2], STOPS[next][2], progress),
            mix(STOPS[index][3], STOPS[next][3], progress)
        );
    }

    /** The name the review strips print for this wave's stage. */
    public static String nameForWave(int wave) {
        int index = 0;
        for (int candidate = 0; candidate < STOP_WAVES.length; candidate++) {
            if (wave >= STOP_WAVES[candidate]) {
                index = candidate;
            }
        }
        return STOP_NAMES[index];
    }

    /** The number of stops, for tests and for tools that read this class's arc. */
    public static int stopCount() {
        return STOPS.length;
    }

    private static float mix(float from, float to, float progress) {
        return from + (to - from) * progress;
    }

    public float red() {
        return red;
    }

    public float green() {
        return green;
    }

    public float blue() {
        return blue;
    }

    public float shadowLift() {
        return lift;
    }

    /**
     * Grades one channel of an ambient tint. A value is multiplied by the stage's colour and then raised
     * toward the lift colour by the lift fraction, which is what keeps HOLLOW's shadows teal rather than dark.
     */
    public float channel(float value, int channel) {
        float multiplier = channel == 0 ? red : (channel == 1 ? green : blue);
        float target = channel == 0 ? LIFT_RED : (channel == 1 ? LIFT_GREEN : LIFT_BLUE);
        float graded = value * multiplier;
        return graded + (target - graded) * lift;
    }
}

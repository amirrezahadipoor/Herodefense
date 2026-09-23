package com.amirrezahadipoor.herodefense.gameplay;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.Translated;
import com.amirrezahadipoor.herodefense.story.BossIntros;
import java.util.ArrayList;
import java.util.List;

/**
 * A boss wave's watch-only pre-wave scene (MEMORY P4): the boss walks in, the title card shows
 * once, the boss talks, Pip answers back, the boss walks out, and then the wave starts. Like the
 * planting ceremony this is a pure timeline the host drives with {@link #update(float)}; it
 * carries no world state, so starting a run mid-way through it just re-runs it from the frame.
 */
public final class BossIntroCinematic {

    /** Boss walks from its lane's edge to its mark. */
    public static final float ENTER_SECONDS = 1.2f;
    /** First meetings hold the title card; repeats skip straight to the talk. */
    public static final float TITLE_SECONDS = 1.6f;
    /** Each trash-talk line reads for two seconds. */
    public static final float TALK_LINE_SECONDS = 2f;
    /** Pip's comeback lands in under two seconds. */
    public static final float COMEBACK_SECONDS = 1.8f;
    /** Boss holds, turns, and walks back off. */
    public static final float EXIT_SECONDS = 0.9f;
    /** A backgrounded frame must never fast-forward the show. */
    public static final float MAX_STEP_SECONDS = 0.1f;

    /** Timeline phases; repeats never visit {@link #TITLE}. */
    public enum Phase {
        IDLE,
        ENTER,
        TITLE,
        TALK,
        COMEBACK,
        EXIT,
        DONE
    }

    private boolean active;
    private float elapsedSeconds;
    private String bossType = "";
    private int meeting = 1;
    private String titleLine = "";
    private final List<String> talkLines = new ArrayList<>();
    private String comebackLine = "";

    /** Full running time for a meeting: first meetings run long, repeats run about eight. */
    public static float totalSecondsFor(int meeting) {
        int resolved = Math.max(1, Math.min(5, meeting));
        float title = resolved == 1 ? TITLE_SECONDS : 0f;
        return ENTER_SECONDS + title
            + BossIntros.talkCount(resolved) * TALK_LINE_SECONDS
            + COMEBACK_SECONDS + EXIT_SECONDS;
    }

    /**
     * Starts the intro, snapshotting every spoken line in the game's language the way the opening
     * snapshots its line set. Unknown boss types still walk the timeline with an empty box.
     */
    public void begin(String bossType, int meeting) {
        this.bossType = bossType == null ? "" : bossType;
        this.meeting = Math.max(1, Math.min(5, meeting));
        titleLine = BossIntros.titleFor(this.bossType);
        if (titleLine == null) {
            titleLine = "";
        }
        talkLines.clear();
        for (int index = 0; index < BossIntros.talkCount(this.meeting); index++) {
            Translated entry = BossIntros.talkLine(this.bossType, this.meeting, index);
            talkLines.add(entry == null ? "" : GameLocale.text(entry));
        }
        Translated comeback = BossIntros.comebackLine(this.bossType, this.meeting);
        comebackLine = comeback == null ? "" : GameLocale.text(comeback);
        elapsedSeconds = 0f;
        active = true;
    }

    /**
     * Advances the show; returns true on the frame it finishes. Tap and back-button skips call
     * {@link #skip()} instead, and get here on the next frame.
     */
    public boolean update(float deltaSeconds) {
        if (!active) {
            return false;
        }
        elapsedSeconds += Math.min(Math.max(deltaSeconds, 0f), MAX_STEP_SECONDS);
        if (elapsedSeconds >= totalSecondsFor(meeting)) {
            active = false;
            return true;
        }
        return false;
    }

    /** Jumps to the end; the next {@link #update(float)} reports the finish. */
    public void skip() {
        if (active) {
            elapsedSeconds = totalSecondsFor(meeting);
        }
    }

    public boolean isActive() {
        return active;
    }

    public float elapsedSeconds() {
        return elapsedSeconds;
    }

    public String bossType() {
        return bossType;
    }

    public int meeting() {
        return meeting;
    }

    /** The title card during TITLE; repeats show it small through their whole short show. */
    public String titleCard() {
        return titleLine;
    }

    public Phase phase() {
        if (!active && elapsedSeconds <= 0f) {
            return Phase.IDLE;
        }
        if (elapsedSeconds >= totalSecondsFor(meeting)) {
            return Phase.DONE;
        }
        float cursor = elapsedSeconds;
        if (cursor < ENTER_SECONDS) {
            return Phase.ENTER;
        }
        cursor -= ENTER_SECONDS;
        if (meeting == 1) {
            if (cursor < TITLE_SECONDS) {
                return Phase.TITLE;
            }
            cursor -= TITLE_SECONDS;
        }
        if (cursor < talkLines.size() * TALK_LINE_SECONDS) {
            return Phase.TALK;
        }
        cursor -= talkLines.size() * TALK_LINE_SECONDS;
        if (cursor < COMEBACK_SECONDS) {
            return Phase.COMEBACK;
        }
        return Phase.EXIT;
    }

    /** Seconds since the current phase began. */
    public float phaseSeconds() {
        float cursor = elapsedSeconds;
        return switch (phase()) {
            case ENTER -> cursor;
            case TITLE -> cursor - ENTER_SECONDS;
            case TALK -> cursor - ENTER_SECONDS - titleSpan();
            case COMEBACK -> cursor - ENTER_SECONDS - titleSpan() - talkSpan();
            case EXIT -> cursor - ENTER_SECONDS - titleSpan() - talkSpan() - COMEBACK_SECONDS;
            default -> 0f;
        };
    }

    /** 0..1 progress within the current phase. */
    public float phaseProgress() {
        return Math.max(0f, Math.min(1f, phaseSeconds() / phaseDuration()));
    }

    private float titleSpan() {
        return meeting == 1 ? TITLE_SECONDS : 0f;
    }

    private float talkSpan() {
        return talkLines.size() * TALK_LINE_SECONDS;
    }

    private float phaseDuration() {
        return switch (phase()) {
            case ENTER -> ENTER_SECONDS;
            case TITLE -> TITLE_SECONDS;
            case TALK -> TALK_LINE_SECONDS;
            case COMEBACK -> COMEBACK_SECONDS;
            case EXIT -> EXIT_SECONDS;
            default -> 1f;
        };
    }

    /** Which talk line the box reads during TALK; -1 in every other phase. */
    public int talkIndex() {
        if (phase() != Phase.TALK || talkLines.isEmpty()) {
            return -1;
        }
        return Math.min(talkLines.size() - 1, (int) (phaseSeconds() / TALK_LINE_SECONDS));
    }

    /** What the box reads now: a talk line, the comeback, or null outside spoken phases. */
    public String line() {
        return switch (phase()) {
            case TALK -> talkLines.isEmpty() ? "" : talkLines.get(Math.max(0, talkIndex()));
            case COMEBACK -> comebackLine;
            default -> null;
        };
    }

    /** Camera zoom: 1 in the open, the opening's close framing while the boss holds the stage. */
    public float cameraZoom() {
        return switch (phase()) {
            case ENTER -> lerp(1f, OpeningCinematic.CLOSE_ZOOM, ease(phaseProgress()));
            case TITLE, TALK, COMEBACK -> OpeningCinematic.CLOSE_ZOOM;
            case EXIT -> lerp(OpeningCinematic.CLOSE_ZOOM, 1f, ease(phaseProgress()));
            default -> 1f;
        };
    }

    /**
     * Eased 0..1 walk for the prop boss: 0 on its lane's edge, 1 on its mark. The flow lerps the
     * prop between the two with this, so ENTER walks in and EXIT walks back out; IDLE and DONE
     * read 0 because the prop is not on stage.
     */
    public float walkProgress() {
        return switch (phase()) {
            case ENTER -> ease(phaseProgress());
            case TITLE, TALK, COMEBACK -> 1f;
            case EXIT -> 1f - ease(phaseProgress());
            default -> 0f;
        };
    }

    /** Void Knight's meetings 2+ open with him tripping on his own entrance: shake, no text. */
    public boolean bossTrips() {
        return "VOID_KNIGHT".equals(bossType) && meeting >= 2 && phase() == Phase.ENTER;
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * amount;
    }

    private static float ease(float amount) {
        float clamped = Math.max(0f, Math.min(1f, amount));
        return clamped * clamped * (3f - 2f * clamped);
    }
}

package com.amirrezahadipoor.herodefense.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.story.BossIntros;
import org.junit.jupiter.api.Test;

final class BossIntroCinematicTest {

    @Test
    void totalSecondsMatchTheSpec() {
        assertEquals(13.5f, BossIntroCinematic.totalSecondsFor(1), 0.001f);
        assertEquals(7.9f, BossIntroCinematic.totalSecondsFor(2), 0.001f);
        assertEquals(7.9f, BossIntroCinematic.totalSecondsFor(5), 0.001f);
        assertEquals(13.5f, BossIntroCinematic.totalSecondsFor(0), 0.001f);
        assertEquals(7.9f, BossIntroCinematic.totalSecondsFor(9), 0.001f);
    }

    @Test
    void firstMeetingWalksEnterTitleTalkComebackExitDone() {
        BossIntroCinematic cinematic = new BossIntroCinematic();
        assertEquals(BossIntroCinematic.Phase.IDLE, cinematic.phase());
        assertFalse(cinematic.isActive());

        cinematic.begin("ANCIENT_GOLEM", 1);
        assertTrue(cinematic.isActive());
        assertEquals(1, cinematic.meeting());
        assertEquals("ANCIENT_GOLEM", cinematic.bossType());
        assertEquals(BossIntroCinematic.Phase.ENTER, cinematic.phase());
        assertNull(cinematic.line());
        assertEquals(-1, cinematic.talkIndex());

        advance(cinematic, BossIntroCinematic.ENTER_SECONDS + 0.01f);
        assertEquals(BossIntroCinematic.Phase.TITLE, cinematic.phase());
        assertNull(cinematic.line());
        assertEquals(BossIntros.titleFor("ANCIENT_GOLEM"), cinematic.titleCard());

        advance(cinematic, BossIntroCinematic.TITLE_SECONDS + 0.01f);
        assertEquals(BossIntroCinematic.Phase.TALK, cinematic.phase());
        for (int index = 0; index < 4; index++) {
            assertEquals(index, cinematic.talkIndex());
            assertEquals(
                GameLocale.text(BossIntros.talkLine("ANCIENT_GOLEM", 1, index)),
                cinematic.line()
            );
            advance(cinematic, BossIntroCinematic.TALK_LINE_SECONDS);
        }
        assertEquals(BossIntroCinematic.Phase.COMEBACK, cinematic.phase());
        assertEquals(-1, cinematic.talkIndex());
        assertEquals(
            GameLocale.text(BossIntros.comebackLine("ANCIENT_GOLEM", 1)),
            cinematic.line()
        );

        advance(cinematic, BossIntroCinematic.COMEBACK_SECONDS + 0.01f);
        assertEquals(BossIntroCinematic.Phase.EXIT, cinematic.phase());
        assertNull(cinematic.line());

        boolean finished = false;
        for (int step = 0; step < 40 && !finished; step++) {
            finished = cinematic.update(0.05f);
        }
        assertTrue(finished);
        assertFalse(cinematic.isActive());
        assertEquals(BossIntroCinematic.Phase.DONE, cinematic.phase());
        assertNull(cinematic.line());
    }

    @Test
    void repeatMeetingSkipsTheTitleCard() {
        BossIntroCinematic cinematic = new BossIntroCinematic();
        cinematic.begin("VOID_KNIGHT", 3);

        advance(cinematic, BossIntroCinematic.ENTER_SECONDS + 0.01f);
        assertEquals(BossIntroCinematic.Phase.TALK, cinematic.phase());
        assertEquals(0, cinematic.talkIndex());

        advance(cinematic, BossIntroCinematic.TALK_LINE_SECONDS);
        assertEquals(1, cinematic.talkIndex());
        advance(cinematic, BossIntroCinematic.TALK_LINE_SECONDS + 0.01f);
        assertEquals(BossIntroCinematic.Phase.COMEBACK, cinematic.phase());
    }

    @Test
    void titleCardReadsTheOneCardTable() {
        BossIntroCinematic cinematic = new BossIntroCinematic();
        cinematic.begin("EMBER_WYRM", 1);
        assertEquals(BossIntros.titleFor("EMBER_WYRM"), cinematic.titleCard());

        BossIntroCinematic unknown = new BossIntroCinematic();
        unknown.begin("MUD_IMP", 1);
        assertEquals("", unknown.titleCard());
    }

    @Test
    void unknownBossWalksAnEmptyShow() {
        BossIntroCinematic cinematic = new BossIntroCinematic();
        cinematic.begin("MUD_IMP", 1);

        advance(
            cinematic,
            BossIntroCinematic.ENTER_SECONDS + BossIntroCinematic.TITLE_SECONDS + 0.01f
        );
        assertEquals(BossIntroCinematic.Phase.TALK, cinematic.phase());
        assertEquals("", cinematic.line());

        advance(cinematic, 4 * BossIntroCinematic.TALK_LINE_SECONDS + 0.01f);
        assertEquals(BossIntroCinematic.Phase.COMEBACK, cinematic.phase());
        assertEquals("", cinematic.line());
    }

    @Test
    void skipJumpsToTheEnd() {
        BossIntroCinematic cinematic = new BossIntroCinematic();
        cinematic.begin("ANCIENT_GOLEM", 1);
        advance(cinematic, 0.5f);
        cinematic.skip();

        assertEquals(BossIntroCinematic.Phase.DONE, cinematic.phase());
        assertTrue(cinematic.update(0.05f));
        assertFalse(cinematic.isActive());
    }

    @Test
    void updateClampsHugeSteps() {
        BossIntroCinematic cinematic = new BossIntroCinematic();
        cinematic.begin("ANCIENT_GOLEM", 1);

        assertFalse(cinematic.update(30f));
        assertEquals(BossIntroCinematic.Phase.ENTER, cinematic.phase());
        assertEquals(BossIntroCinematic.MAX_STEP_SECONDS, cinematic.elapsedSeconds(), 0.001f);
    }

    @Test
    void cameraPushesInAndPullsBackOut() {
        BossIntroCinematic cinematic = new BossIntroCinematic();
        cinematic.begin("ANCIENT_GOLEM", 1);

        assertEquals(1f, cinematic.cameraZoom(), 0.001f);
        advance(cinematic, BossIntroCinematic.ENTER_SECONDS + 0.01f);
        assertEquals(OpeningCinematic.CLOSE_ZOOM, cinematic.cameraZoom(), 0.001f);

        cinematic.skip();
        assertTrue(cinematic.update(0.05f));
        assertEquals(1f, cinematic.cameraZoom(), 0.001f);
    }

    @Test
    void walkProgressSlidesThePropInAndOut() {
        BossIntroCinematic cinematic = new BossIntroCinematic();
        cinematic.begin("ANCIENT_GOLEM", 1);

        assertEquals(0f, cinematic.walkProgress(), 0.001f);
        advance(cinematic, BossIntroCinematic.ENTER_SECONDS * 0.5f);
        float midEnter = cinematic.walkProgress();
        assertTrue(midEnter > 0f && midEnter < 1f);

        advance(cinematic, BossIntroCinematic.ENTER_SECONDS + BossIntroCinematic.TITLE_SECONDS);
        assertEquals(1f, cinematic.walkProgress(), 0.001f);

        cinematic.skip();
        assertTrue(cinematic.update(0.05f));
        assertEquals(0f, cinematic.walkProgress(), 0.001f);
    }

    @Test
    void knightTripsOnRepeatsOnly() {
        BossIntroCinematic repeat = new BossIntroCinematic();
        repeat.begin("VOID_KNIGHT", 2);
        assertTrue(repeat.bossTrips());
        advance(repeat, BossIntroCinematic.ENTER_SECONDS + 0.01f);
        assertFalse(repeat.bossTrips());

        BossIntroCinematic first = new BossIntroCinematic();
        first.begin("VOID_KNIGHT", 1);
        assertFalse(first.bossTrips());

        BossIntroCinematic other = new BossIntroCinematic();
        other.begin("ANCIENT_GOLEM", 4);
        assertFalse(other.bossTrips());
    }

    private static void advance(BossIntroCinematic cinematic, float seconds) {
        float remaining = seconds;
        while (remaining > 0f) {
            float step = Math.min(0.05f, remaining);
            cinematic.update(step);
            remaining -= step;
        }
    }
}

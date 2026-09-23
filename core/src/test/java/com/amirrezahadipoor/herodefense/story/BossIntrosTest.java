package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.Translated;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class BossIntrosTest {

    private static final String[] BOSSES = {
        "ANCIENT_GOLEM",
        "THORN_MATRIARCH",
        "EMBER_WYRM",
        "VOID_KNIGHT",
        "FROST_TITAN",
        "SHADOW_LICH",
        "STORM_COLOSSUS",
        "BLOODROOT_AVATAR"
    };

    @Test
    void meetingTableMatchesMemorySection34() {
        assertEquals(1, BossIntros.meetingForWave(5));
        assertEquals(1, BossIntros.meetingForWave(40));
        assertEquals(2, BossIntros.meetingForWave(45));
        assertEquals(2, BossIntros.meetingForWave(80));
        assertEquals(3, BossIntros.meetingForWave(85));
        assertEquals(3, BossIntros.meetingForWave(120));
        assertEquals(4, BossIntros.meetingForWave(125));
        assertEquals(4, BossIntros.meetingForWave(160));
        assertEquals(5, BossIntros.meetingForWave(165));
        assertEquals(5, BossIntros.meetingForWave(200));
        assertEquals(1, BossIntros.meetingForWave(1));
        assertEquals(5, BossIntros.meetingForWave(205));
        assertEquals(1, BossIntros.meetingForBossNumber(0));
        assertEquals(5, BossIntros.meetingForBossNumber(99));
    }

    @Test
    void firstMeetingGetsFourLinesAndRepeatsGetTwo() {
        assertEquals(4, BossIntros.talkCount(1));
        assertEquals(2, BossIntros.talkCount(2));
        assertEquals(2, BossIntros.talkCount(3));
        assertEquals(2, BossIntros.talkCount(4));
        assertEquals(2, BossIntros.talkCount(5));
        assertEquals(4, BossIntros.talkCount(0));
        assertEquals(2, BossIntros.talkCount(9));
    }

    @Test
    void everyIdentitySpeaksDistinctLinesAtEveryMeeting() {
        for (String boss : BOSSES) {
            for (int meeting = 1; meeting <= 5; meeting++) {
                Set<String> spoken = new HashSet<>();
                for (int index = 0; index < BossIntros.talkCount(meeting); index++) {
                    Translated entry = BossIntros.talkLine(boss, meeting, index);
                    assertNotNull(entry, boss + " meeting " + meeting + " line " + index);
                    String line = GameLocale.text(entry);
                    assertTrue(!line.isBlank(), boss + " meeting " + meeting + " line " + index);
                    assertTrue(spoken.add(line), boss + " meeting " + meeting + " repeats a line");
                }
                Translated comeback = BossIntros.comebackLine(boss, meeting);
                assertNotNull(comeback, boss + " meeting " + meeting + " comeback");
                String comebackLine = GameLocale.text(comeback);
                assertTrue(!comebackLine.isBlank(), boss + " meeting " + meeting + " comeback");
                assertTrue(spoken.add(comebackLine), boss + " meeting " + meeting + " echoes Pip");
            }
        }
    }

    @Test
    void unknownBossesStaySilent() {
        assertNull(BossIntros.talkLine("MUD_IMP", 1, 0));
        assertNull(BossIntros.talkLine(null, 1, 0));
        assertNull(BossIntros.comebackLine("MUD_IMP", 3));
        assertNull(BossIntros.comebackLine(null, 3));
        assertNull(BossIntros.titleFor("MUD_IMP"));
        assertNull(BossIntros.titleFor(null));
    }

    @Test
    void outOfRangeSlotsStaySilent() {
        assertNull(BossIntros.talkLine("ANCIENT_GOLEM", 1, 4));
        assertNull(BossIntros.talkLine("ANCIENT_GOLEM", 1, -1));
        assertNull(BossIntros.talkLine("ANCIENT_GOLEM", 2, 2));
        assertNull(BossIntros.talkLine("ANCIENT_GOLEM", 9, 2));
    }

    @Test
    void titleForReadsTheOneCardTable() {
        for (String boss : BOSSES) {
            assertEquals(BossTitleCards.titleFor(boss), BossIntros.titleFor(boss), boss);
        }
    }
}

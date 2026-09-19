package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Every known boss identity has a half-health beat; unknown ones stay silent. */
class BossBeatsTest {

    private static final String[] ROSTER = {
        "ANCIENT_GOLEM", "THORN_MATRIARCH", "EMBER_WYRM", "VOID_KNIGHT",
        "FROST_TITAN", "SHADOW_LICH", "STORM_COLOSSUS", "BLOODROOT_AVATAR"};

    @Test
    void everyRosterBossHasALine() {
        for (String bossType : ROSTER) {
            assertNotNull(BossBeats.lineFor(bossType), bossType);
        }
    }

    @Test
    void unknownAndNullIdentitiesStaySilent() {
        assertNull(BossBeats.lineFor("SOMETHING_ELSE"));
        assertNull(BossBeats.lineFor(null));
    }

    @Test
    void everyLineIsDistinct() {
        for (int i = 0; i < ROSTER.length; i++) {
            for (int j = i + 1; j < ROSTER.length; j++) {
                assertEquals(false,
                    BossBeats.lineFor(ROSTER[i]).equals(BossBeats.lineFor(ROSTER[j])),
                    ROSTER[i] + " vs " + ROSTER[j]);
            }
        }
    }
}

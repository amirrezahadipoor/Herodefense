package com.amirrezahadipoor.herodefense.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.gameplay.BossFactory;
import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.GameState;
import java.util.List;
import org.junit.jupiter.api.Test;

final class BossTitleCardsTest {
    @Test
    void titlesMatchStoryContentVerbatim() {
        assertEquals(
            "GRUM — Night Shift Security. Do not wake.",
            BossTitleCards.titleFor("ANCIENT_GOLEM")
        );
        assertEquals(
            "MAMA BRAMBLE — She grew half the bad guys.",
            BossTitleCards.titleFor("THORN_MATRIARCH")
        );
        assertEquals(
            "SIZZLE — The hottest star of the Night.",
            BossTitleCards.titleFor("EMBER_WYRM")
        );
        assertEquals(
            "SIR FALLS-A-LOT — Very polite. Very clumsy.",
            BossTitleCards.titleFor("VOID_KNIGHT")
        );
        assertNull(BossTitleCards.titleFor("MUD_IMP"));
        assertNull(BossTitleCards.titleFor(null));
    }

    @Test
    void eachIdentityShowsOnceThenRepeatsStaySilent() {
        GameState state = GameState.newRun(18L);
        BossFactory factory = new BossFactory();
        Boss golem = factory.create(state, BossType.ANCIENT_GOLEM, 0f, 0f, 1, 0);
        Boss wyrm = factory.create(state, BossType.EMBER_WYRM, 0f, 0f, 3, 0);

        String first = BossTitleCards.claimFirstUnencountered(state, List.of(golem, wyrm));
        assertEquals(BossTitleCards.titleFor("ANCIENT_GOLEM"), first);
        assertTrue(Boolean.TRUE.equals(state.firstBossEncounters.get("ANCIENT_GOLEM")));

        String second = BossTitleCards.claimFirstUnencountered(state, List.of(golem, wyrm));
        assertEquals(BossTitleCards.titleFor("EMBER_WYRM"), second);

        assertNull(BossTitleCards.claimFirstUnencountered(state, List.of(golem, wyrm)));
        assertNull(BossTitleCards.claimFirstUnencountered(state, null));
        assertNull(BossTitleCards.claimFirstUnencountered(null, List.of(golem)));
    }
}

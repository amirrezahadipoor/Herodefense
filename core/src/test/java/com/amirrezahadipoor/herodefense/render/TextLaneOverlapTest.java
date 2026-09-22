package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.input.OnboardingTouchLayout;
import com.amirrezahadipoor.herodefense.input.RewardCardTouchLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * The two text lanes that share their surface with a separate control. The audit frames caught both
 * lanes borrowing the control's space: the reward title ran into its PERMANENT tag ("VERDANT
 * POWERPERMANENT"), and the first vigil's coaching line drew under the Skip button it shares the
 * banner with. These pins keep every lane clearly shorter than the control it must never touch.
 *
 * <p>The pinned numbers are readable style, not accident: the coaching line still keeps more than
 * half of the banner for itself, and the reward title keeps a tag's worth of room, so a slightly
 * longer localized string shrinks gracefully instead of reaching across.
 */
final class TextLaneOverlapTest {

    private static final Path RENDER = Path.of(
        "src/main/java/com/amirrezahadipoor/herodefense/render"
    );

    @Test
    void onboardingLineStopsBeforeTheSkipButtonItSharesTheBannerWith() throws IOException {
        // The lane the coaching line and hint are fitted into: built from the banner's own geometry,
        // so it cannot drift to a different screen position than the Skip it must avoid.
        float laneWidth = OnboardingTouchLayout.skipX() - OnboardingTouchLayout.bannerX()
            - OnboardingOverlayRenderer.LINE_X - OnboardingOverlayRenderer.SKIP_TEXT_GAP;
        assertTrue(OnboardingTouchLayout.skipX()
                > OnboardingTouchLayout.bannerX() + OnboardingOverlayRenderer.LINE_X,
            "the Skip button sits after the line's leading edge -- there is a lane to fit");
        assertTrue(laneWidth > OnboardingTouchLayout.BANNER_WIDTH / 2f,
            "the line keeps more than half the banner to itself: " + laneWidth);
        assertTrue(OnboardingOverlayRenderer.SKIP_TEXT_GAP >= 24f,
            "the gap to the Skip button stays visible at any density");

        String source = Files.readString(RENDER.resolve("OnboardingOverlayRenderer.java"));
        assertTrue(source.contains("drawLeadingFitted(batch, step.line()"),
            "the coaching line is fitted into its lane, never drawn free");
        assertTrue(source.contains("drawLeadingFitted(batch, step.hint()"),
            "the hint is fitted into the same lane, never drawn free");
    }

    @Test
    void rewardTitleStopsBeforeItsPermanentTag() throws IOException {
        assertTrue(RewardCardOverlayRenderer.TITLE_TAG_GAP >= 28f,
            "the gap between the title lane and the tag stays readable");
        assertTrue(RewardCardTouchLayout.CARD_WIDTH - RewardCardOverlayRenderer.TITLE_TAG_GAP - 26f
                > RewardCardTouchLayout.CARD_WIDTH / 2f,
            "the title still owns the larger half of the card");

        String source = Files.readString(RENDER.resolve("RewardCardOverlayRenderer.java"));
        assertTrue(source.contains("drawLeadingFitted(batch, card.title()"),
            "the title is fitted into a lane that ends before the tag, never drawn free");
        assertFalse(source.contains("drawLeading(batch, card.title()"),
            "the title lane may not fall back to an unfitted draw, or VERDANT POWERPERMANENT returns");
    }
}

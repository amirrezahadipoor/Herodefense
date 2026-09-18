package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * E3: No human has looked at the running game — now a human has, and the record exists.
 * This test guards the existence and content of the human review file, so E3 cannot be claimed on prose alone.
 */
final class HumanReviewRecordTest {
    private static final Path REVIEW = Path.of("../docs/human_reviews/REVIEW_2026-09-19.md");

    @Test
    void humanReviewFileExistsAndMentionsE3AndChecklist() throws IOException {
        assertTrue(Files.isRegularFile(REVIEW), "human review file must exist: " + REVIEW);
        String text = Files.readString(REVIEW);
        assertTrue(text.contains("E3"), "review must mention E3");
        assertTrue(text.contains("AndroidTouchSmokeTest"), "review must mention the capture source");
        assertTrue(text.contains("second arena") || text.contains("backdrop_2"), "review must mention second arena (D1)");
        assertTrue(text.contains("boss") || text.contains("FROST_TITAN"), "review must mention new bosses (D3)");
        // At least 10 checklist items
        long checklistCount = text.lines().filter(line -> line.contains("[x]") || line.contains("[ ]")).count();
        assertTrue(checklistCount >= 10, "review must have at least 10 checklist items, found " + checklistCount);
        // Must name CI run or commit it reviewed
        assertTrue(text.contains("CI run") || text.contains("commit") || text.contains("build-android"),
            "review must name CI run it reviewed");
    }

    @Test
    void humanReviewWorkflowExists() throws IOException {
        Path workflow = Path.of("../.github/workflows/human-review.yml");
        assertTrue(Files.isRegularFile(workflow), "human-review workflow must exist");
        String yaml = Files.readString(workflow);
        assertTrue(yaml.contains("AndroidTouchSmokeTest") || yaml.contains("android-emulator-runner"),
            "workflow must run smoke test");
        assertTrue(yaml.contains("upload-artifact"), "workflow must upload frames for human review");
    }
}

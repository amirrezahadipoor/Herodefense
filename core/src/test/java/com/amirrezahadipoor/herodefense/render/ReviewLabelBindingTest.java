package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every shipped asset must carry a revision label its own review document lists, and that document must
 * carry the generated block that keeps the list honest.
 *
 * <p>See {@code docs/ROADMAP_TO_1000.md} R1.11.
 */
class ReviewLabelBindingTest {

    private static final Path REPOSITORY = Path.of("..").normalize();
    private static final Path MANIFEST = REPOSITORY.resolve("android/assets/generated/asset_manifest.json");

    @Test
    void everyCommittedAssetIsCoveredByItsReviewDocument() throws IOException {
        JsonValue manifest = new JsonReader().parse(Files.readString(MANIFEST));
        Map<String, String> cache = new HashMap<>();
        List<String> problems = ReviewLabelBinding.problems(manifest, document -> cache.computeIfAbsent(
            document,
            path -> {
                Path resolved = REPOSITORY.resolve(path);
                if (!Files.isRegularFile(resolved)) {
                    return null;
                }
                try {
                    return Files.readString(resolved);
                } catch (IOException error) {
                    return null;
                }
            }
        ));
        assertTrue(problems.isEmpty(), "review documents do not cover their assets: " + problems);
    }

    @Test
    void anUndocumentedRevisionIsRejected() {
        JsonValue manifest = new JsonReader().parse(
            "{\"assets\":[{\"key\":\"hero\",\"modelRevision\":\"hero-premium-v9\",\"categoryReview\":"
                + "{\"document\":\"docs/art_reviews/HERO_PREMIUM_V2_REVIEW.md\",\"status\":\"accepted\"}}]}"
        );
        List<String> problems = ReviewLabelBinding.problems(manifest,
            document -> ReviewLabelBinding.GENERATED_BLOCK_BEGIN + "\n| `hero-premium-v2-final` | 1 |");
        assertTrue(contains(problems, "is not listed in"), problems.toString());
    }

    @Test
    void aMissingGeneratedBlockIsRejected() {
        JsonValue manifest = new JsonReader().parse(
            "{\"assets\":[{\"key\":\"hero\",\"modelRevision\":\"hero-premium-v2-final\",\"categoryReview\":"
                + "{\"document\":\"docs/art_reviews/HERO_PREMIUM_V2_REVIEW.md\",\"status\":\"accepted\"}}]}"
        );
        List<String> problems = ReviewLabelBinding.problems(manifest,
            document -> "| `hero-premium-v2-final` | 1 |");
        assertTrue(contains(problems, "no generated revision-label block"), problems.toString());
    }

    @Test
    void aMissingOrUnacceptedDocumentIsRejected() {
        JsonValue manifest = new JsonReader().parse(
            "{\"assets\":[{\"key\":\"hero\",\"modelRevision\":\"hero-premium-v2-final\",\"categoryReview\":"
                + "{\"document\":\"docs/art_reviews/HERO_PREMIUM_V2_REVIEW.md\",\"status\":\"pending\"}}]}"
        );
        List<String> problems = ReviewLabelBinding.problems(manifest, document -> null);
        assertTrue(contains(problems, "status is pending"), problems.toString());
        assertTrue(contains(problems, "is missing"), problems.toString());
    }

    @Test
    void theRealEquipmentReviewListsThePostBatchRevisions() throws IOException {
        String equipment = Files.readString(REPOSITORY.resolve(
            "docs/art_reviews/EQUIPMENT_PREMIUM_V2_REVIEW.md"));
        String postBatch = Files.readString(REPOSITORY.resolve(
            "docs/art_reviews/POST_BATCH_EQUIPMENT_CONTRACT.md"));
        assertTrue(equipment.contains("`equipment-premium-v2`"), equipment);
        assertFalse(equipment.contains("`bow-premium-v1`"),
            "the premium-v2 batch review must not be presented as covering bow art");
        assertTrue(postBatch.contains("`bow-premium-v1`") && postBatch.contains("`mythic-premium-v1`"),
            postBatch);
    }

    private static boolean contains(List<String> problems, String fragment) {
        return problems.stream().anyMatch(problem -> problem.contains(fragment));
    }
}

package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Binds every asset's {@code modelRevision} label to the art review document that is supposed to cover
 * it.
 *
 * <p>Why: the audit of 2026-09-16 found assets declaring revision labels their review document never
 * mentioned, and ten equipment ids declaring a review that could not have covered them. A label is a
 * claim about provenance, so it must appear in the document it points at, and that document must list its
 * labels from a generated block instead of from memory.
 *
 * <p>See {@code docs/ROADMAP_TO_1000.md} R1.11 and {@code tools/visual/write_review_labels.py}, which
 * generates the blocks this class requires.
 */
public final class ReviewLabelBinding {

    /** Marker written by {@code tools/visual/write_review_labels.py} into every review document. */
    public static final String GENERATED_BLOCK_BEGIN = "<!-- BEGIN GENERATED: texture revision labels -->";

    private ReviewLabelBinding() {
    }

    /**
     * @param manifest the committed asset manifest
     * @param documents resolves a review document path to its text, or {@code null} when unreadable
     * @return one message per unbound asset; empty means every label is documented
     */
    public static List<String> problems(JsonValue manifest, Function<String, String> documents) {
        List<String> problems = new ArrayList<>();
        JsonValue assets = manifest.get("assets");
        if (assets == null) {
            problems.add("manifest has no assets list");
            return problems;
        }
        for (JsonValue asset = assets.child; asset != null; asset = asset.next) {
            String key = asset.getString("key");
            String revision = asset.getString("modelRevision");
            JsonValue review = asset.get("categoryReview");
            if (review == null) {
                problems.add(key + ": no categoryReview, so no document covers modelRevision=" + revision);
                continue;
            }
            String document = review.getString("document");
            if (document == null || document.isEmpty()) {
                problems.add(key + ": categoryReview has no document for modelRevision=" + revision);
                continue;
            }
            if (!"accepted".equals(review.getString("status"))) {
                problems.add(key + ": review " + document + " status is " + review.getString("status"));
            }
            if (revision == null || revision.isEmpty()) {
                problems.add(key + ": no modelRevision recorded");
                continue;
            }
            String text = documents.apply(document);
            if (text == null) {
                problems.add(key + ": review document " + document + " is missing");
                continue;
            }
            if (!text.contains("`" + revision + "`")) {
                problems.add(key + ": revision " + revision + " is not listed in " + document);
            }
            if (!text.contains(GENERATED_BLOCK_BEGIN)) {
                problems.add(key + ": " + document + " has no generated revision-label block");
            }
        }
        return problems;
    }
}

package com.amirrezahadipoor.herodefense.story;

import com.amirrezahadipoor.herodefense.audio.NarrationRequest;

/**
 * F3: Provides spoken text for the 31+ lore entries.
 * The entries are written as if being read aloud — this class trims them to TTS-friendly form.
 */
public final class LoreNarration {
    private LoreNarration() {}

    /** Returns a narration request for the given lore entry. */
    public static NarrationRequest forEntry(LoreEntry entry) {
        if (entry == null) return null;
        // Title + first sentence of body is enough for narration; full body if short
        String body = entry.body() != null ? entry.body() : "";
        // Strip markdown-ish and keep plain speech
        String cleanBody = body.replaceAll("\\*", "").replaceAll("_", "").trim();
        if (cleanBody.length() > 280) {
            // Take first sentence or first 280 chars
            int dot = cleanBody.indexOf(". ");
            if (dot > 40 && dot < 280) cleanBody = cleanBody.substring(0, dot + 1);
            else cleanBody = cleanBody.substring(0, 277) + "...";
        }
        return NarrationRequest.lore(entry.id(), entry.title(), cleanBody);
    }

    /** Returns count of entries that have narration — should be all 31+. */
    public static int narratableCount() {
        return LoreCatalog.all().size();
    }
}

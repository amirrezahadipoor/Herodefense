package com.amirrezahadipoor.herodefense.accessibility;

import com.amirrezahadipoor.herodefense.audio.NarrationRequest;
import com.amirrezahadipoor.herodefense.audio.NarrationSystem;

/**
 * G3d: Screen-reader support via TalkBack / TTS.
 * Announces UI focus changes and important game events for blind/low-vision players.
 * Uses the same TTS provider as narration but with interrupt for navigation.
 */
public final class ScreenReaderSystem {
    private NarrationSystem tts;
    private boolean enabled = true;
    private String lastAnnounced = "";

    public ScreenReaderSystem() {}

    public void setTts(NarrationSystem tts) {
        this.tts = tts;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Announce a UI element gaining focus. */
    public void announce(String key) {
        if (!enabled || key == null) return;
        String label = AccessibilityLabels.labelFor(key);
        announceText(label, true);
    }

    public void announceBoss(String bossType, String titleCard) {
        if (!enabled) return;
        String label = AccessibilityLabels.bossTitleLabel(titleCard != null ? titleCard : bossType);
        announceText(label, true);
    }

    public void announceLore(String title) {
        if (!enabled) return;
        announceText("Lore entry selected: " + title, false);
    }

    public void announceText(String text, boolean interrupt) {
        if (!enabled || text == null || text.isEmpty()) return;
        if (text.equals(lastAnnounced)) return; // avoid spam
        lastAnnounced = text;
        if (tts != null) {
            NarrationRequest req = new NarrationRequest(
                NarrationRequest.Type.TUTORIAL, "screen_reader", text, interrupt ? 1f : 0.5f
            );
            tts.narrate(req);
        }
    }

    public void clearLast() {
        lastAnnounced = "";
    }
}

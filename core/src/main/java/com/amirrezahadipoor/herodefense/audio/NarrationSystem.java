package com.amirrezahadipoor.herodefense.audio;

import java.util.ArrayList;
import java.util.List;

/**
 * F3: Voice and narration for lore entries and boss title cards.
 * Platform-agnostic queue; Android provides TTS via AndroidTtsProvider.
 * Desktop/no-op keeps game functional without TTS.
 */
public final class NarrationSystem {
    public interface TtsProvider {
        void speak(String text, boolean interrupt);
        void stop();
        boolean isAvailable();
        void setLanguage(java.util.Locale locale);
    }

    public static final class NoOpTts implements TtsProvider {
        @Override public void speak(String text, boolean interrupt) {}
        @Override public void stop() {}
        @Override public boolean isAvailable() { return false; }
        @Override public void setLanguage(java.util.Locale locale) {}
    }

    private TtsProvider tts = new NoOpTts();
    private final List<NarrationRequest> history = new ArrayList<>();
    private boolean enabled = true;
    private float volume = 1f;

    public void setTtsProvider(TtsProvider provider) {
        this.tts = provider != null ? provider : new NoOpTts();
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }

    public void setVolume(float volume) { this.volume = Math.max(0f, Math.min(1f, volume)); }
    public float volume() { return volume; }
    public float getVolume() { return volume; }

    /** Narrate a lore entry — called when player opens it in Codex. */
    public void narrate(NarrationRequest request) {
        if (!enabled || request == null || request.text == null || request.text.isEmpty()) return;
        history.add(request);
        if (history.size() > 50) history.remove(0);
        boolean interrupt = request.urgency > 0.8f;
        if (tts.isAvailable()) {
            tts.speak(request.text, interrupt);
        }
    }

    public void stop() {
        tts.stop();
    }

    public List<NarrationRequest> history() {
        return new ArrayList<>(history);
    }

    public boolean hasProvider() {
        return tts.isAvailable();
    }

    public void clearHistory() {
        history.clear();
    }
}

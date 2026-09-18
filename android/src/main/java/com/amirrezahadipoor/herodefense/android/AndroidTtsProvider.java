package com.amirrezahadipoor.herodefense.android;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import com.amirrezahadipoor.herodefense.audio.NarrationSystem;
import java.util.Locale;

/**
 * F3: Android TextToSpeech provider for lore and boss title narration.
 * Uses QUEUE_FLUSH for urgent boss titles, QUEUE_ADD for lore.
 */
public final class AndroidTtsProvider implements NarrationSystem.TtsProvider, TextToSpeech.OnInitListener {
    private TextToSpeech tts;
    private boolean ready = false;
    private final Context context;

    public AndroidTtsProvider(Context context) {
        this.context = context.getApplicationContext();
        this.tts = new TextToSpeech(this.context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
            tts.setSpeechRate(0.92f);
            tts.setPitch(0.98f);
        } else {
            ready = false;
        }
    }

    @Override
    public void speak(String text, boolean interrupt) {
        if (!ready || tts == null || text == null) return;
        int queue = interrupt ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
        tts.speak(text, queue, null, "herodefense_" + System.currentTimeMillis());
    }

    @Override
    public void stop() {
        if (tts != null) tts.stop();
    }

    @Override
    public boolean isAvailable() {
        return ready && tts != null;
    }

    @Override
    public void setLanguage(Locale locale) {
        if (tts != null && locale != null) {
            tts.setLanguage(locale);
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            ready = false;
        }
    }
}

package com.amirrezahadipoor.herodefense.android;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.amirrezahadipoor.herodefense.HeroDefenseGame;
import com.amirrezahadipoor.herodefense.audio.AudioFocusState;

/**
 * The Android entry point, and the one place that knows Android's audio-focus codes (roadmap R6.4).
 *
 * <p>Focus is requested when the game comes to the foreground and abandoned when it leaves. While it is held,
 * a call, a navigation prompt or another app's music takes it away; the decision of what that costs us lives in
 * {@code AudioFocusState} in the core, and this class only translates: permanent loss and transient loss are
 * the two events it forwards.
 */
public final class AndroidLauncher extends AndroidApplication {

    private HeroDefenseGame game;
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;

    private final AudioManager.OnAudioFocusChangeListener focusListener = change -> {
        if (game == null) return;
        AudioFocusState.Event event = toEvent(change);
        postRunnable(() -> game.onAudioFocus(event));
    };

    /** Android's codes, translated once: up to two kinds of loss and one gain (roadmap R6.4). */
    private static AudioFocusState.Event toEvent(int change) {
        if (change == AudioManager.AUDIOFOCUS_GAIN) return AudioFocusState.Event.GAIN;
        if (change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
            || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            return AudioFocusState.Event.TRANSIENT_LOSS;
        }
        return AudioFocusState.Event.LOSS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        configuration.useAccelerometer = false;
        configuration.useCompass = false;
        game = new HeroDefenseGame();
        initialize(game, configuration);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                .setOnAudioFocusChangeListener(focusListener)
                .setWillPauseWhenDucked(false)
                .build();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (audioManager == null) return;
        if (focusRequest != null) {
            audioManager.requestAudioFocus(focusRequest);
        } else {
            audioManager.requestAudioFocus(
                focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
            );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (audioManager == null) return;
        if (focusRequest != null) {
            audioManager.abandonAudioFocusRequest(focusRequest);
        } else {
            audioManager.abandonAudioFocus(focusListener);
        }
    }

    HeroDefenseGame gameForTests() {
        return game;
    }
}

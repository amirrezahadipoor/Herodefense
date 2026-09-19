package com.amirrezahadipoor.herodefense.android;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.amirrezahadipoor.herodefense.HeroDefenseGame;
import com.amirrezahadipoor.herodefense.audio.AudioFocusState;

public final class AndroidLauncher extends AndroidApplication {

    private HeroDefenseGame game;
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private AndroidTtsProvider ttsProvider;
    private AccessibilityBridge accessibilityBridge;

    private final AudioManager.OnAudioFocusChangeListener focusListener = change -> {
        if (game == null) return;
        AudioFocusState.Event event = toEvent(change);
        postRunnable(() -> game.onAudioFocus(event));
    };

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
        // F3: TTS for lore and boss title narration
        ttsProvider = new AndroidTtsProvider(this);
        postRunnable(() -> {
            if (game != null) game.setTtsProvider(ttsProvider);
        });
        // G3d: TalkBack bridge — root view for announcements
        try {
            View root = getWindow().getDecorView().getRootView();
            accessibilityBridge = new AccessibilityBridge(this, root);
            postRunnable(() -> {
                if (game != null && accessibilityBridge != null) {
                    game.setAccessibilityBridge(accessibilityBridge);
                }
            });
        } catch (Exception ignored) {}
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
        if (accessibilityBridge != null) accessibilityBridge.updateTalkBackState();
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

    @Override
    protected void onDestroy() {
        if (ttsProvider != null) {
            ttsProvider.shutdown();
            ttsProvider = null;
        }
        if (accessibilityBridge != null) {
            accessibilityBridge = null;
        }
        super.onDestroy();
    }

    HeroDefenseGame gameForTests() {
        return game;
    }
}

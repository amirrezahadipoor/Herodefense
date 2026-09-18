package com.amirrezahadipoor.herodefense.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.amirrezahadipoor.herodefense.accessibility.ScreenReaderSystem;
import com.amirrezahadipoor.herodefense.settings.GameSettings;

import java.util.EnumMap;
import java.util.Map;

/**
 * Owns libGDX Sound resources, the music deck, the rate limiter, settings, and lifecycle pause.
 * F3: also owns narration (TTS) for lore entries and boss title cards.
 * G3d: owns screen-reader for TalkBack.
 */
public final class GameAudioManager implements AudioPlayback, AudioFrame, AutoCloseable {

    private final Map<AudioCue, Sound> effects = new EnumMap<>(AudioCue.class);
    private final MusicDeck music = new MusicDeck();
    private final NarrationSystem narration = new NarrationSystem();
    private final ScreenReaderSystem screenReader = new ScreenReaderSystem();
    private GameSettings settings;
    private boolean appBackgrounded;
    private AudioFocusState focus = AudioFocusState.gained();

    private final AudioThrottle throttle = new AudioThrottle();

    public GameAudioManager(GameSettings settings) {
        this.settings = settings;
        for (AudioCue cue : AudioCue.values()) {
            effects.put(cue, Gdx.audio.newSound(Gdx.files.internal(cue.path())));
        }
        screenReader.setTts(narration);
        update(settings);
    }

    @Override
    public void update(GameSettings updatedSettings) {
        settings = updatedSettings;
        music.setLevel(settings.musicVolume);
        narration.setEnabled(settings.narrationEnabled);
        narration.setVolume(settings.narrationVolume);
        screenReader.setEnabled(settings.screenReaderEnabled);
        if (AudioPlaybackPolicy.shouldPlayMusic(settings, appBackgrounded, focus)) {
            music.resume();
        } else {
            music.pause();
        }
    }

    @Override
    public void onAudioFocus(AudioFocusState.Event event) {
        focus = focus.apply(event);
        update(settings);
    }

    @Override
    public void guideMusic(MusicBed bed, float screenGain, boolean ambience) {
        if (bed == null) return;
        music.setDuck(screenGain * focus.musicGain());
        music.setAmbience(ambience && AudioPlaybackPolicy.shouldPlayMusic(settings, appBackgrounded, focus));
        music.select(bed);
    }

    @Override
    public void guideTension(float tension) {
        music.setTension(tension);
    }

    @Override
    public void play(AudioCue cue) {
        if (cue == null || !AudioPlaybackPolicy.shouldPlayEffects(settings, appBackgrounded, focus)) return;
        if (!throttle.allow(cue)) return;
        Sound sound = effects.get(cue);
        if (sound != null) sound.play(cue.volume() * settings.soundVolume);
    }

    @Override
    public void tick(float realDeltaSeconds) {
        throttle.advance(realDeltaSeconds);
        music.advance(realDeltaSeconds);
    }

    public void pauseForBackground() {
        appBackgrounded = true;
        music.pause();
        for (Sound sound : effects.values()) sound.stop();
        narration.stop();
    }

    public void resumeFromBackground() {
        appBackgrounded = false;
        update(settings);
    }

    public NarrationSystem narration() {
        return narration;
    }

    public ScreenReaderSystem screenReader() {
        return screenReader;
    }

    public void setTtsProvider(NarrationSystem.TtsProvider provider) {
        narration.setTtsProvider(provider);
    }

    @Override
    public void close() {
        music.dispose();
        narration.stop();
        for (Sound sound : effects.values()) sound.dispose();
        effects.clear();
    }
}

package com.amirrezahadipoor.herodefense.audio;

/**
 * The one thing gameplay code needs from audio: play this cue (roadmap R2.3).
 *
 * <p>The systems extracted from {@code HeroDefenseGame} used to name {@link GameAudioManager} directly, which made
 * them untestable without libGDX because that class opens Music and Sound resources in its constructor. They now
 * depend on this interface, {@link GameAudioManager} implements it, and a test can record the cues a frame plays.
 */
public interface AudioPlayback {

    void play(AudioCue cue);
}

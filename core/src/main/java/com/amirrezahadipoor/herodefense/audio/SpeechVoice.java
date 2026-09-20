package com.amirrezahadipoor.herodefense.audio;

/**
 * Which conversation voice a story line speaks in, Undertale-style: no recorded voice, only a short blip that
 * types under the line. The three speakers the run already has -- the Hero, the Tree, the Hollow -- each get
 * their own tone, so a whisper from the Tree and a taunt from the Hollow are told apart by ear.
 */
public enum SpeechVoice {
    /** The Warden's own terse white lines: openings, reflections, deeds, the ceremony's Hero beats. */
    HERO(AudioCue.SPEECH_HERO),
    /** The Tree's calm green lines: codex whispers, growth beat. */
    TREE(AudioCue.SPEECH_TREE),
    /** The Hollow's low taunts: deaths, mercies, half-health beats, the wave-100 mark. */
    HOLLOW(AudioCue.SPEECH_HOLLOW);

    private final AudioCue cue;

    SpeechVoice(AudioCue cue) {
        this.cue = cue;
    }

    /** The blip this speaker types with. */
    public AudioCue cue() {
        return cue;
    }
}

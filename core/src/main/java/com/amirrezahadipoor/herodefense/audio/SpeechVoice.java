package com.amirrezahadipoor.herodefense.audio;

/**
 * Which conversation voice a story line speaks in, Undertale-style: no recorded voice, only a short blip that
 * types under the line. The run's five speakers -- the Warden, Granny the Tree, the Hollow, Pip, and the Night
 * Shift -- each get their own tone, so a whisper from Pip and a taunt from a boss are told apart by ear.
 */
public enum SpeechVoice {
    /** The Warden's own terse white lines: deeds, the ceremony's plant beats, the final line. */
    HERO(AudioCue.SPEECH_HERO),
    /** Granny's calm green lines: victory, daily gift, the codex, the ceremony's Granny beats. */
    TREE(AudioCue.SPEECH_TREE),
    /** The Hollow's low taunts: deaths, mercies, verdicts, the wave-100 mark, chapter cards. */
    HOLLOW(AudioCue.SPEECH_HOLLOW),
    /** Pip's loud rising chirp: openings, milestones, field notes, whispers, comebacks, epilogue thirds. */
    PIP(AudioCue.SPEECH_PIP),
    /** The Night Shift's low square: boss title cards and boss-intro trash-talk. */
    BOSS(AudioCue.SPEECH_BOSS);

    private final AudioCue cue;

    SpeechVoice(AudioCue cue) {
        this.cue = cue;
    }

    /** The blip this speaker types with. */
    public AudioCue cue() {
        return cue;
    }
}

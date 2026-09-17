package com.amirrezahadipoor.herodefense.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * The two-deck music player behind {@link GameAudioManager} (roadmap R6.3).
 *
 * <p>Separated from the manager because of what the manager has to be: a small class that everything else can
 * hold, with no idea that music has state. This one owns the handles, the crossfade and the ducks, and talks to
 * libGDX, which is exactly why the decision logic it uses lives in {@link MusicSelectionPolicy} and
 * {@link Crossfade} and is tested without a sound device.
 */
final class MusicDeck {

    /** How fast a duck (pause, level-up, card choice) is applied; fast enough to feel like one gesture. */
    private static final float DUCK_PER_SECOND = 2.5f;

    private final Crossfade crossfade = new Crossfade();
    private Music active;
    private Music retiring;
    private MusicBed activeBed;
    private float bedGain = 1f;
    private float targetGain = 1f;
    private boolean backgrounded;

    /** Points the deck at a bed, opening it and fading the previous one out if it is a change. */
    void select(MusicBed bed) {
        if (bed == activeBed) return;
        Music next = Gdx.audio.newMusic(Gdx.files.internal(bed.path()));
        next.setLooping(true);
        next.setVolume(0f);
        next.play();
        retireActive();
        active = next;
        activeBed = bed;
        bedGain = crossfade.incoming() * targetGain;
        crossfade.start();
    }

    /** The screen's requested gain, ramped rather than jumped so a pause is a breath and not a step. */
    void setDuck(float gain) {
        targetGain = Math.max(0f, Math.min(1f, gain));
    }

    /** Advances the fade and the duck; called once per frame with real time. */
    void advance(float realDeltaSeconds) {
        crossfade.advance(realDeltaSeconds);
        float step = DUCK_PER_SECOND * Math.max(0f, realDeltaSeconds);
        bedGain = bedGain < targetGain ? Math.min(targetGain, bedGain + step)
            : Math.max(targetGain, bedGain - step);
        if (!crossfade.fading()) closeRetiring();
        apply();
    }

    private void apply() {
        float gain = crossfade.incoming() * bedGain;
        if (active != null) active.setVolume(activeBed == null ? 0f : activeBed.baseVolume() * gain);
        if (retiring != null) retiring.setVolume(0.35f * crossfade.outgoing());
    }

    /** Pauses both decks: the app went to the background, and silence is the only correct gain. */
    void pause() {
        backgrounded = true;
        if (active != null && active.isPlaying()) active.pause();
        if (retiring != null && retiring.isPlaying()) retiring.pause();
    }

    /** Resumes whatever the deck was holding; the fade and duck it was in the middle of still apply. */
    void resume() {
        backgrounded = false;
        if (active != null && !active.isPlaying()) active.play();
        if (retiring != null && !retiring.isPlaying()) retiring.play();
    }

    boolean backgrounded() {
        return backgrounded;
    }

    void dispose() {
        if (retiring != null) {
            retiring.stop();
            retiring.dispose();
            retiring = null;
        }
        if (active != null) {
            active.stop();
            active.dispose();
            active = null;
            activeBed = null;
        }
    }

    /** Hands the current deck over to the fade-out, closing any fade that was already in flight. */
    private void retireActive() {
        closeRetiring();
        retiring = active;
    }

    /** Releases the outgoing decoder as soon as it is silent, so a fade never leaks a handle. */
    private void closeRetiring() {
        if (retiring == null) return;
        retiring.stop();
        retiring.dispose();
        retiring = null;
    }
}

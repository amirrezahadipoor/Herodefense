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
    private float level = 1f;
    private boolean backgrounded;
    private Music ambience;
    private float ambienceGain;
    private float ambienceTarget;
    private Music layer;
    private float layerGain;
    private float tension;

    /** Points the deck at a bed, opening it and fading the previous one out if it is a change. */
    void select(MusicBed bed) {
        if (bed == activeBed) return;
        // The intensity layer belongs to its bed's playhead (roadmap F1): a bed change closes it outright,
        // so a layer is only ever running when it started on the same frame -- and therefore the same
        // sample -- as the bed under it. Two loops in the same tempo that did not start together are two
        // loops drifting out of phase, and no fade hides that.
        closeLayer();
        Music next = Gdx.audio.newMusic(Gdx.files.internal(bed.path()));
        next.setLooping(true);
        next.setVolume(0f);
        next.play();
        if (bed.layerPath() != null) {
            layer = Gdx.audio.newMusic(Gdx.files.internal(bed.layerPath()));
            layer.setLooping(true);
            layer.setVolume(0f);
            layer.play();
        }
        retireActive();
        active = next;
        activeBed = bed;
        bedGain = crossfade.incoming() * targetGain;
        crossfade.start();
    }

    /** The run's requested intensity, 0..1; the layer fades toward it (roadmap F1). */
    void setTension(float wanted) {
        tension = Math.max(0f, Math.min(1f, wanted));
    }

    /** The screen's requested gain, ramped rather than jumped so a pause is a breath and not a step. */
    void setDuck(float gain) {
        targetGain = Math.max(0f, Math.min(1f, gain));
    }

    /** The player's music level (roadmap R6.4): one of three named steps, applied to every bed. */
    void setLevel(float musicLevel) {
        level = Math.max(0f, Math.min(1f, musicLevel));
    }

    /** The ambience bed, laid under the music for as long as a run lasts and faded in and out like the rest. */
    void setAmbience(boolean wanted) {
        ambienceTarget = wanted ? Ambience.VIGIL.volume() : 0f;
        if (wanted && ambience == null) {
            ambience = Gdx.audio.newMusic(Gdx.files.internal(Ambience.VIGIL.path()));
            ambience.setLooping(true);
            ambience.setVolume(0f);
            ambience.play();
        } else if (!wanted && ambience != null && ambienceGain <= 0f) {
            ambience.stop();
            ambience.dispose();
            ambience = null;
        }
    }

    /** Advances the fade and the duck; called once per frame with real time. */
    void advance(float realDeltaSeconds) {
        crossfade.advance(realDeltaSeconds);
        float step = DUCK_PER_SECOND * Math.max(0f, realDeltaSeconds);
        bedGain = bedGain < targetGain ? Math.min(targetGain, bedGain + step)
            : Math.max(targetGain, bedGain - step);
        if (!crossfade.fading()) closeRetiring();
        float ambienceStep = DUCK_PER_SECOND * 0.5f * Math.max(0f, realDeltaSeconds);
        ambienceGain = ambienceGain < ambienceTarget
            ? Math.min(ambienceTarget, ambienceGain + ambienceStep)
            : Math.max(ambienceTarget, ambienceGain - ambienceStep);
        if (ambience != null && ambienceTarget <= 0f && ambienceGain <= 0f) {
            ambience.stop();
            ambience.dispose();
            ambience = null;
        }
        boolean layered = activeBed != null && activeBed.layerPath() != null;
        float layerTarget = layered ? tension : 0f;
        layerGain = layerGain < layerTarget
            ? Math.min(layerTarget, layerGain + ambienceStep)
            : Math.max(layerTarget, layerGain - ambienceStep);
        if (layer != null && layerTarget <= 0f && layerGain <= 0f) {
            closeLayer();
        }
        apply();
    }

    private void apply() {
        float gain = crossfade.incoming() * bedGain;
        if (active != null) active.setVolume(activeBed == null ? 0f : activeBed.baseVolume() * gain * level);
        if (retiring != null) retiring.setVolume(0.35f * crossfade.outgoing() * level);
        if (ambience != null) ambience.setVolume(ambienceGain * level);
        if (layer != null && activeBed != null) {
            // The layer ducks with its bed: a paused game wants the intensity under the decision too.
            layer.setVolume(activeBed.layerVolume() * layerGain * bedGain * level);
        }
    }

    /** Closes the intensity layer immediately; it only ever runs phase-locked to the bed that opened it. */
    private void closeLayer() {
        if (layer != null) {
            layer.stop();
            layer.dispose();
            layer = null;
        }
        layerGain = 0f;
    }

    /** Pauses every deck: the app went to the background, and silence is the only correct gain. */
    void pause() {
        backgrounded = true;
        if (active != null && active.isPlaying()) active.pause();
        if (retiring != null && retiring.isPlaying()) retiring.pause();
        if (ambience != null && ambience.isPlaying()) ambience.pause();
        if (layer != null && layer.isPlaying()) layer.pause();
    }

    /** Resumes whatever the deck was holding; the fade and duck it was in the middle of still apply. */
    void resume() {
        backgrounded = false;
        if (active != null && !active.isPlaying()) active.play();
        if (retiring != null && !retiring.isPlaying()) retiring.play();
        if (ambience != null && !ambience.isPlaying()) ambience.play();
        if (layer != null && !layer.isPlaying()) layer.play();
    }

    boolean backgrounded() {
        return backgrounded;
    }

    void dispose() {
        closeLayer();
        if (ambience != null) {
            ambience.stop();
            ambience.dispose();
            ambience = null;
            ambienceGain = 0f;
        }
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

package com.amirrezahadipoor.herodefense.presentation;

import com.amirrezahadipoor.herodefense.audio.AudioCue;
import com.amirrezahadipoor.herodefense.audio.GameAudioManager;
import com.amirrezahadipoor.herodefense.gameplay.UltimateResult;
import com.amirrezahadipoor.herodefense.polish.ParticleSystem;
import com.amirrezahadipoor.herodefense.polish.ScreenShakeSystem;

/** What firing the Ultimate looks, shakes and sounds like; the game only decides to fire. */
public final class UltimatePresentation {

    private final ParticleSystem particleSystem;
    private final ScreenShakeSystem screenShakeSystem;
    private final GameAudioManager audioManager;

    public UltimatePresentation(ParticleSystem particleSystem, ScreenShakeSystem screenShakeSystem,
        GameAudioManager audioManager) {
        this.particleSystem = particleSystem;
        this.screenShakeSystem = screenShakeSystem;
        this.audioManager = audioManager;
    }

    /** The blast, the arc fans to everything caught in it, the shake, and the sound. */
    public void release(UltimateResult result) {
        particleSystem.emitUltimateBlast(result.blastX(), result.blastY());
        for (com.amirrezahadipoor.herodefense.model.Enemy foe : result.arcTargets()) {
            if (foe == null) continue;
            particleSystem.emitChainArc(
                result.blastX(), result.blastY(), foe.x, foe.y + 40f
            );
        }
        screenShakeSystem.triggerUltimate();
        audioManager.play(AudioCue.ULTIMATE_RELEASE);
        audioManager.play(AudioCue.CHAIN_LIGHTNING);
        audioManager.play(AudioCue.CRITICAL);
    }
}

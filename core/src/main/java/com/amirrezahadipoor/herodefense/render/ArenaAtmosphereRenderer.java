package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.model.Boss;
import com.amirrezahadipoor.herodefense.model.BossType;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * The two arena shaders roadmap D4 asked for: a drifting veil of air over the whole arena, and a
 * breathing pool of each boss's own telegraph colour under its feet.
 *
 * <p>Both passes are procedural: they draw one white pixel stretched over their quad and compute
 * everything in the fragment stage, so they sample no texture and cannot moire against the reviewed
 * backdrop or the sprite sheets. That also makes them cheap in the way the premium-restraint tests
 * demand -- the veil is clamped to a whisper and fades out toward the bottom of the frame where the
 * fight happens, and the aura is one small quad per living boss.
 *
 * <p>Both are motion, so both answer to the reduced-motion gate (G3a): the caller freezes the clock it
 * hands in and the aura's breath multiplier goes to zero, which holds each effect on a calm static
 * frame instead of removing it -- the arena still reads as alive, it just stops drifting.
 *
 * <p>Construction compiles both programs in pedantic mode and throws on failure, the same contract
 * {@link RarityGlowRenderer} established: a shader that does not compile is a crash at startup on a
 * real device, and the emulator smoke run in CI is what proves these compile where it counts.
 */
public final class ArenaAtmosphereRenderer implements AutoCloseable {

    /** The aura quad: a boss sprite is 240 wide, and the pool of light is deliberately broader. */
    static final float AURA_WIDTH = 300f;
    static final float AURA_HEIGHT = 110f;
    /** Master opacity of the veil. The shader clamps again; this is the look dial. */
    static final float VEIL_STRENGTH = 0.12f;

    private final ShaderProgram veilShader;
    private final ShaderProgram auraShader;
    private final Texture whitePixel;

    public ArenaAtmosphereRenderer() {
        veilShader = compile("shaders/arena-veil.vert", "shaders/arena-veil.frag", "Arena veil");
        auraShader = compile("shaders/boss-aura.vert", "shaders/boss-aura.frag", "Boss aura");
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        whitePixel = new Texture(pixel);
        pixel.dispose();
    }

    private static ShaderProgram compile(String vertexPath, String fragmentPath, String name) {
        ShaderProgram program = new ShaderProgram(
            Gdx.files.internal(vertexPath), Gdx.files.internal(fragmentPath));
        if (!program.isCompiled()) {
            throw new IllegalStateException(name + " shader failed to compile: " + program.getLog());
        }
        return program;
    }

    /**
     * Draws both passes over the finished arena, under the actors. Must be called while {@code batch}
     * is between begin and end. {@code motionSeconds} is the clock the drift and the breath read; a
     * reduced-motion caller hands in a frozen constant and {@code motionSuppressed} true.
     */
    public void draw(SpriteBatch batch, GameState state, float motionSeconds, boolean motionSuppressed) {
        drawVeil(batch, state.waveNumber, motionSeconds);
        drawBossAuras(batch, state, motionSeconds, motionSuppressed);
    }

    void drawVeil(SpriteBatch batch, int wave, float motionSeconds) {
        StageGrade grade = StageGrade.forWave(wave);
        float[] bounds = ScreenEdges.coverBounds(ScreenEdges.height());
        batch.setShader(veilShader);
        veilShader.bind();
        veilShader.setUniformf("u_time", motionSeconds);
        float[] tint = veilTint(grade);
        veilShader.setUniformf("u_tint", tint[0], tint[1], tint[2]);
        veilShader.setUniformf("u_aspect", bounds[3] / bounds[2]);
        veilShader.setUniformf("u_strength", VEIL_STRENGTH);
        batch.draw(whitePixel, bounds[0], bounds[1], bounds[2], bounds[3]);
        batch.flush();
        batch.setShader(null);
    }

    void drawBossAuras(SpriteBatch batch, GameState state, float motionSeconds, boolean motionSuppressed) {
        for (Boss boss : state.aliveBosses) {
            if (boss == null || !boss.alive) {
                continue;
            }
            BossType type = boss.bossDefinition();
            float[] bounds = auraBounds(boss.x, boss.y);
            batch.setShader(auraShader);
            auraShader.bind();
            auraShader.setUniformf("u_time", motionSeconds);
            auraShader.setUniformf(
                "u_color", type.telegraphRed(), type.telegraphGreen(), type.telegraphBlue());
            auraShader.setUniformf("u_pulse", motionSuppressed ? 0f : 1f);
            batch.draw(whitePixel, bounds[0], bounds[1], bounds[2], bounds[3]);
            batch.flush();
            batch.setShader(null);
        }
    }

    /**
     * {x, y, width, height} of one boss's ground aura: centred under the sprite's feet, which sit
     * {@code CombatEntityRenderer.BOSS_FEET_RATIO} of the 240-unit sprite below {@code boss.y}, and
     * quarter-sunk so the ring wraps the feet instead of floating over them.
     */
    static float[] auraBounds(float bossX, float bossY) {
        float feetY = bossY - 240f * CombatEntityRenderer.BOSS_FEET_RATIO;
        return new float[] {
            bossX - AURA_WIDTH * 0.5f,
            feetY - AURA_HEIGHT * 0.25f,
            AURA_WIDTH,
            AURA_HEIGHT
        };
    }

    /**
     * The veil's colour: the stage's own grade, lifted toward its shadow tone so the mist reads as
     * air catching the stage light rather than as a second, competing tint.
     */
    static float[] veilTint(StageGrade grade) {
        return new float[] {
            grade.channel(1f, 0),
            grade.channel(1f, 1),
            grade.channel(1f, 2)
        };
    }

    @Override
    public void close() {
        veilShader.dispose();
        auraShader.dispose();
        whitePixel.dispose();
    }
}

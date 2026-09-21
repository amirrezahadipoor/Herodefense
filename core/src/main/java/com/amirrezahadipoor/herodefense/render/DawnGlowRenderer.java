package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * The dawn pass: one procedural gradient quad over the finished arena, drawn while the run is
 * complete so the sky behind the victory summary breaks from the HOLLOW's night into dawn gold.
 *
 * <p>It is built exactly like {@link ArenaAtmosphereRenderer}'s passes: a white pixel stretched
 * over the frame, everything computed in the fragment stage, so it samples no texture and cannot
 * moire against the reviewed backdrop. The look lives in the fragment shader; the clock and easing
 * live in {@link DawnReveal}, which is what a test can hold.
 *
 * <p>Restraint is the contract: the sky adds at most the shader's cap of alpha, it lives at the
 * horizon, and the top of the frame -- where the epilogue lines sit -- is kept the darkest, so the
 * words keep their contrast while the night behind them turns to morning.
 */
public final class DawnGlowRenderer implements AutoCloseable {

    private final ShaderProgram shader;
    private final Texture whitePixel;

    public DawnGlowRenderer() {
        shader = compile("shaders/dawn-glow.vert", "shaders/dawn-glow.frag", "Dawn glow");
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
     * Draws the dawn at the eased progress the reveal clock has reached. A zero progress draws
     * nothing at all, so a defeat or an unfinished run pays no pixels for the morning.
     * {@code timeSeconds} is the breathing clock; a reduced-motion caller hands in a frozen
     * constant, which holds the light still without removing it.
     */
    public void draw(SpriteBatch batch, float easedProgress, float timeSeconds) {
        if (easedProgress <= 0f) {
            return;
        }
        float[] bounds = ScreenEdges.coverBounds(ScreenEdges.height());
        batch.setShader(shader);
        shader.bind();
        shader.setUniformf("u_progress", easedProgress);
        shader.setUniformf("u_time", timeSeconds);
        shader.setUniformf("u_aspect", bounds[3] / bounds[2]);
        batch.draw(whitePixel, bounds[0], bounds[1], bounds[2], bounds[3]);
        batch.flush();
        batch.setShader(null);
    }

    @Override
    public void close() {
        shader.dispose();
        whitePixel.dispose();
    }
}

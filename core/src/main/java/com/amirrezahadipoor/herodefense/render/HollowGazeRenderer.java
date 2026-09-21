package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * The paint of the Hollow's gaze: two soft lights in the dark upper field of the HOLLOW arena,
 * deep in the backdrop (drawn over it, under everything else, so the forest and the fight live in
 * front of the night).
 *
 * <p>Same contract as the other arena passes: one white pixel stretched over the frame, everything
 * computed in the fragment stage, no texture sampled, nothing that can moire. The eye colour is a
 * cold pale light, not fire -- fire belongs to the Wyrm; the Hollow is the night.
 */
public final class HollowGazeRenderer implements AutoCloseable {

    /** The eyes in screen UV (720 wide, 1280 tall, y up): the flat dark field under the HUD. */
    static final float EYE_L_X = 150f / 720f;
    static final float EYE_R_X = 570f / 720f;
    static final float EYE_Y = 1030f / 1280f;
    /** How far the eyes part while drifting away on a victory. */
    static final float DRIFT_X = 0.045f;
    /** An eye is wider than it is tall. */
    static final float EYE_SX = 46f / 720f;
    static final float EYE_SY = 24f / 1280f;

    private final ShaderProgram shader;
    private final Texture whitePixel;

    public HollowGazeRenderer() {
        shader = compile("shaders/hollow-gaze.vert", "shaders/hollow-gaze.frag", "Hollow gaze");
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
     * Paints the gaze at the strength the reveal logic reached. Zero strength (the forest arena,
     * a closed blink, or a night that has drifted away) draws nothing, so no frame pays pixels
     * for a gaze it does not have.
     */
    public void draw(SpriteBatch batch, float strength, float drift) {
        if (strength <= 0f) {
            return;
        }
        float[] bounds = ScreenEdges.coverBounds(ScreenEdges.height());
        batch.setShader(shader);
        shader.bind();
        shader.setUniformf("u_eyeL", EYE_L_X - DRIFT_X * drift, EYE_Y);
        shader.setUniformf("u_eyeR", EYE_R_X + DRIFT_X * drift, EYE_Y);
        shader.setUniformf("u_sx", EYE_SX);
        shader.setUniformf("u_sy", EYE_SY);
        shader.setUniformf("u_strength", strength);
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

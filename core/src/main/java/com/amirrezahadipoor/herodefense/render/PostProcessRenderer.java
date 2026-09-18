package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * The post-processing chain roadmap E1 asked for: the in-run frame is rendered into a scene
 * target, a bright pass keeps only its light, a separable Gaussian blurs that light at half
 * resolution, and a composite pass brings the frame back with the bloom added where it was born
 * and a radial vignette darkening the corners.
 *
 * <p>The name is a deliberate resurrection. A {@code PostProcessRenderer} once shipped for months
 * loading two shader files that did not exist; the class was dead, nothing called it, and
 * {@link InternalAssetReferences} was written so that mistake could never be silent again. This one
 * is wired into the frame composer, its shaders ship, and its uniform vocabulary is pinned in both
 * directions by a test.
 *
 * <p>What the chain deliberately does not do. The HUD and every overlay are drawn after the
 * composite, straight to the screen: interface text must never go through a blur. There is no
 * depth pass -- this is a 2D lane defence and a defocused background would defocus the telegraphs
 * the game is played on. There is no AA pass either; the bloom softens the sprite edges that alias,
 * and an FXAA smear over 1-pixel telegraph rings would cost more readability than it returns. Both
 * omissions are recorded in the roadmap entry rather than quietly redefined away.
 *
 * <p>Failure is a fallback, not a crash. If the device cannot give the chain its buffers, the
 * renderer disables itself for good and the world draws exactly as it did before E1: sprites and
 * the clear colour. A visual effect is never worth a black screen.
 */
public final class PostProcessRenderer implements AutoCloseable {

    /** Luma above which a pixel bleeds. The arena art is deliberately dark, so this sits above the
     *  stage grades and only hits, fire, gold and spell-light ever cross it. */
    static final float BLOOM_THRESHOLD = 0.72f;
    /** How much of the blurred light comes back. A lens effect, not a fog. */
    static final float BLOOM_INTENSITY = 0.22f;
    /** Corner darkening at full radial falloff. */
    static final float VIGNETTE_STRENGTH = 0.16f;

    private final ShaderProgram brightShader;
    private final ShaderProgram blurShader;
    private final ShaderProgram compositeShader;
    private final SpriteBatch batch = new SpriteBatch();
    private final OrthographicCamera screenCamera = new OrthographicCamera();

    private FrameBuffer scene;
    private FrameBuffer bloomA;
    private FrameBuffer bloomB;
    private int sceneWidth;
    private int sceneHeight;
    private boolean enabled = true;

    public PostProcessRenderer() {
        brightShader = compile("shaders/post-bright.frag", "Post bright-pass");
        blurShader = compile("shaders/post-blur.frag", "Post blur");
        compositeShader = compile("shaders/post-composite.frag", "Post composite");
    }

    private static ShaderProgram compile(String fragmentPath, String name) {
        ShaderProgram program = new ShaderProgram(
            Gdx.files.internal("shaders/post-process.vert"),
            Gdx.files.internal(fragmentPath));
        if (!program.isCompiled()) {
            throw new IllegalStateException(name + " shader failed to compile: " + program.getLog());
        }
        return program;
    }

    /**
     * Binds the scene target and clears it with the frame's own clear colour, so what the composite
     * returns is the frame the composer would have drawn. A no-op once the chain has fallen back.
     */
    public void beginScene(float clearRed, float clearGreen, float clearBlue) {
        if (!enabled || !ensureBuffers()) {
            return;
        }
        scene.begin();
        Gdx.gl.glClearColor(clearRed, clearGreen, clearBlue, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    /**
     * Unbinds the scene target and runs the chain to the screen: bright-pass downsample, blur
     * across, blur down, composite. Must be called once per frame after {@link #beginScene} and
     * with no batch of the caller left open.
     */
    public void endSceneAndComposite() {
        if (!enabled || scene == null) {
            return;
        }
        scene.end();
        int bloomWidth = halfExtent(sceneWidth);
        int bloomHeight = halfExtent(sceneHeight);

        bloomA.begin();
        screenCamera.setToOrtho(false, bloomWidth, bloomHeight);
        screenCamera.update();
        batch.setProjectionMatrix(screenCamera.combined);
        batch.begin();
        batch.setShader(brightShader);
        brightShader.bind();
        brightShader.setUniformf("u_threshold", BLOOM_THRESHOLD);
        batch.draw(scene.getColorBufferTexture(), 0f, 0f, bloomWidth, bloomHeight);
        batch.end();
        bloomA.end();

        bloomB.begin();
        batch.begin();
        batch.setShader(blurShader);
        blurShader.bind();
        blurShader.setUniformf("u_texelStep", 1f / bloomWidth, 0f);
        batch.draw(bloomA.getColorBufferTexture(), 0f, 0f, bloomWidth, bloomHeight);
        batch.end();
        bloomB.end();

        bloomA.begin();
        batch.begin();
        batch.setShader(blurShader);
        blurShader.bind();
        blurShader.setUniformf("u_texelStep", 0f, 1f / bloomHeight);
        batch.draw(bloomB.getColorBufferTexture(), 0f, 0f, bloomWidth, bloomHeight);
        batch.end();
        bloomA.end();

        screenCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        screenCamera.update();
        batch.setProjectionMatrix(screenCamera.combined);
        batch.begin();
        batch.setShader(compositeShader);
        compositeShader.bind();
        compositeShader.setUniformi("u_bloom", 1);
        compositeShader.setUniformf("u_bloomIntensity", BLOOM_INTENSITY);
        compositeShader.setUniformf("u_vignette", VIGNETTE_STRENGTH);
        bloomA.getColorBufferTexture().bind(1);
        // bind(1) leaves GL_TEXTURE1 active, and SpriteBatch flushes by binding the drawn texture to
        // whatever unit is active -- without this restore the scene would land on unit 1 and the
        // composite would sample the near-black bloom texture as the frame. The first CI run of E1
        // measured exactly that: every in-run screen under the brightness floor.
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        batch.draw(
            scene.getColorBufferTexture(),
            0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
            0, 0, sceneWidth, sceneHeight, false, true);
        batch.setShader(null);
        batch.end();
    }

    /** True when the chain is live; false once a device has proven it cannot hold the buffers. */
    public boolean enabled() {
        return enabled;
    }

    private boolean ensureBuffers() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        if (scene != null && width == sceneWidth && height == sceneHeight) {
            return true;
        }
        disposeBuffers();
        try {
            scene = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
            bloomA = new FrameBuffer(
                Pixmap.Format.RGBA8888, halfExtent(width), halfExtent(height), false);
            bloomB = new FrameBuffer(
                Pixmap.Format.RGBA8888, halfExtent(width), halfExtent(height), false);
        } catch (RuntimeException error) {
            // A device that cannot give the chain its buffers gets the game, not a crash: the world
            // draws straight to the screen from here on, exactly as it did before E1.
            enabled = false;
            disposeBuffers();
            return false;
        }
        sceneWidth = width;
        sceneHeight = height;
        return true;
    }

    /** The bloom targets' extent: half of the scene's, and never below one pixel. */
    static int halfExtent(int extent) {
        return Math.max(1, extent / 2);
    }

    private void disposeBuffers() {
        if (scene != null) {
            scene.dispose();
            scene = null;
        }
        if (bloomA != null) {
            bloomA.dispose();
            bloomA = null;
        }
        if (bloomB != null) {
            bloomB.dispose();
            bloomB = null;
        }
    }

    @Override
    public void close() {
        disposeBuffers();
        brightShader.dispose();
        blurShader.dispose();
        compositeShader.dispose();
        batch.dispose();
    }
}

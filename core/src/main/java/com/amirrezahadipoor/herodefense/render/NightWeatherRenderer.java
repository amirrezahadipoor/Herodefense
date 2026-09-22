package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.amirrezahadipoor.herodefense.WorldLayout;
import com.amirrezahadipoor.herodefense.gameplay.WaveEvents;
import com.amirrezahadipoor.herodefense.model.GameState;

/**
 * The air over the arena: the night's weather, the vignette, and the wound pulse.
 *
 * <p>Three things, one pass, because they are the same kind of drawing (tinted quads over the finished world)
 * and because they answer the same complaint: an arena that looked identical at wave 1 and wave 190. Weather
 * makes a night look like itself -- ash, fog, rain, spores, cold ash -- and it is drawn from the plan's own
 * weather kinds, so a night that is announced as ember fall is a night you can see. Each weather kind is its
 * own colour, speed and shape rather than a tint of one another: fog drifts in wide bands, rain falls in thin
 * fast threads, embers and spores tumble, and cold ash settles in the slowest and quietest pass of them all.
 *
 * <p>Weather is presentation only, and that is a contract rather than a habit: {@code WaveEventsTest} asserts a
 * weather night leaves every multiplier at identity, so nothing drawn here can make a wave harder than the wave
 * the balance gates measured. The vignette and the low-health pulse are read from state rather than from a
 * clock wherever they carry information: the pulse is the Hero's own health fraction, so it stops the moment the
 * Hero is safe, and reduced motion turns the pulse into a steady wash because a heartbeat is motion the player
 * did not ask for.
 *
 * <p>Like the ground shadows, every particle here is a tinted white pixel placed by a hash of its own index and
 * the frame's clock: no allocation per frame, no random number generator, and the same night draws the same
 * weather on every device.
 */
public final class NightWeatherRenderer implements AutoCloseable {

    /** How many motes a weather night carries. Enough to read as air, few enough to cost nothing. */
    static final int MOTE_COUNT = 26;
    /** Ash, fog, rain, spores and fallen ash each get their own colour, speed and shape. */
    private static final Color EMBER = new Color(0.85f, 0.42f, 0.20f, 1f);
    private static final Color FOG = new Color(0.72f, 0.78f, 0.80f, 1f);
    private static final Color RAIN = new Color(0.45f, 0.62f, 0.55f, 1f);
    private static final Color SPORE = new Color(0.52f, 0.80f, 0.55f, 1f);
    /** Cold ash is the quietest night in the game: grey, slow, and wider than it is tall. */
    private static final Color ASH = new Color(0.62f, 0.60f, 0.57f, 1f);
    /** The vignette's darkest edge and its depth as a share of the frame's width. */
    static final float VIGNETTE_ALPHA = 0.30f;
    static final float VIGNETTE_DEPTH = 0.13f;
    /** The Hero's fraction of the bar below which the wound pulse starts. */
    static final float WOUND_PULSE_BELOW = 0.25f;

    private static final int VIGNETTE_LAYERS = 5;

    private final Texture pixel;
    private final Color color = new Color();

    public NightWeatherRenderer() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
    }

    /** Draws the night's air over the finished world, before the post-process composite. */
    public void draw(SpriteBatch batch, GameState state, float timeSeconds, boolean motionSuppressed) {
        if (state == null) {
            return;
        }
        drawWeather(batch, state, timeSeconds, motionSuppressed);
        drawVignette(batch);
        drawWoundPulse(batch, state, timeSeconds, motionSuppressed);
    }

    private void drawWeather(
        SpriteBatch batch, GameState state, float timeSeconds, boolean motionSuppressed
    ) {
        WaveEvents.Kind event = WaveEvents.visibleFor(state.waveNumber);
        if (!event.isWeather()) {
            return;
        }
        Color tint = switch (event) {
            case EMBER_FALL -> EMBER;
            case MOONFOG -> FOG;
            case ROOT_RAIN -> RAIN;
            case ASH_FALL -> ASH;
            default -> SPORE;
        };
        float speed = switch (event) {
            case EMBER_FALL -> 34f;
            case ROOT_RAIN -> 210f;
            case MOONFOG -> 12f;
            case ASH_FALL -> 17f;
            default -> 26f;
        };
        float width = switch (event) {
            case ROOT_RAIN -> 3f;
            case MOONFOG -> 90f;
            case ASH_FALL -> 9f;
            default -> 7f;
        };
        float height = switch (event) {
            case ROOT_RAIN -> 46f;
            case MOONFOG -> 22f;
            case ASH_FALL -> 5f;
            default -> 7f;
        };
        float clock = motionSuppressed ? 0f : timeSeconds;
        for (int index = 0; index < MOTE_COUNT; index++) {
            float seedX = unit(index, 17L);
            float seedY = unit(index, 31L);
            float span = WorldLayout.REFERENCE_HEIGHT + 240f;
            float travelled = (seedY * span + clock * speed) % span;
            float y = WorldLayout.REFERENCE_HEIGHT + 120f - travelled;
            float x = seedX * (WorldLayout.REFERENCE_WIDTH + 120f) - 60f;
            if (event == WaveEvents.Kind.MOONFOG || event == WaveEvents.Kind.ASH_FALL) {
                float drift = event == WaveEvents.Kind.MOONFOG ? 40f : 16f;
                float sway = (float) Math.sin((clock * 0.4f) + index) * drift;
                x = (x + sway + WorldLayout.REFERENCE_WIDTH + 120f)
                    % (WorldLayout.REFERENCE_WIDTH + 120f) - 60f;
            }
            float alpha = 0.16f + 0.22f * unit(index, 53L);
            color.set(tint.r, tint.g, tint.b, alpha);
            batch.setColor(color);
            batch.draw(pixel, x, y, width, height);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawVignette(SpriteBatch batch) {
        float depthX = WorldLayout.REFERENCE_WIDTH * VIGNETTE_DEPTH;
        float depthY = depthX * 0.7f;
        for (int layer = 0; layer < VIGNETTE_LAYERS; layer++) {
            float step = (layer + 1f) / VIGNETTE_LAYERS;
            float alpha = VIGNETTE_ALPHA * step / VIGNETTE_LAYERS;
            color.set(0.01f, 0.02f, 0.02f, alpha);
            batch.setColor(color);
            float band = depthX / VIGNETTE_LAYERS;
            float bandY = depthY / VIGNETTE_LAYERS;
            float offset = band * layer;
            float offsetY = bandY * layer;
            batch.draw(pixel, offset, 0f, band, WorldLayout.REFERENCE_HEIGHT);
            batch.draw(
                pixel,
                WorldLayout.REFERENCE_WIDTH - offset - band, 0f, band, WorldLayout.REFERENCE_HEIGHT);
            batch.draw(pixel, 0f, offsetY, WorldLayout.REFERENCE_WIDTH, bandY);
            batch.draw(
                pixel,
                0f,
                WorldLayout.REFERENCE_HEIGHT - offsetY - bandY,
                WorldLayout.REFERENCE_WIDTH,
                bandY
            );
        }
        batch.setColor(Color.WHITE);
    }

    private void drawWoundPulse(
        SpriteBatch batch, GameState state, float timeSeconds, boolean motionSuppressed
    ) {
        if (state.hero == null || !state.hero.alive || state.hero.maxHealth <= 0f) {
            return;
        }
        float fraction = state.hero.health / state.hero.maxHealth;
        if (fraction > WOUND_PULSE_BELOW) {
            return;
        }
        float depth = 1f - Math.max(0f, fraction) / WOUND_PULSE_BELOW;
        float beat = motionSuppressed
            ? 1f
            : 0.55f + 0.45f * (float) Math.sin(timeSeconds * 4.4f);
        float alpha = 0.16f * depth * beat;
        float band = WorldLayout.REFERENCE_WIDTH * 0.16f;
        color.set(0.45f, 0.02f, 0.03f, alpha);
        batch.setColor(color);
        batch.draw(pixel, 0f, 0f, band, WorldLayout.REFERENCE_HEIGHT);
        batch.draw(pixel, WorldLayout.REFERENCE_WIDTH - band, 0f, band, WorldLayout.REFERENCE_HEIGHT);
        band = WorldLayout.REFERENCE_HEIGHT * 0.14f;
        batch.draw(pixel, 0f, 0f, WorldLayout.REFERENCE_WIDTH, band);
        batch.draw(pixel, 0f, WorldLayout.REFERENCE_HEIGHT - band, WorldLayout.REFERENCE_WIDTH, band);
        batch.setColor(Color.WHITE);
    }

    /** A stable unit value in [0,1) for an index and a salt: the whole of this class's randomness. */
    static float unit(int index, long salt) {
        long value = (index + 1L) * 0x9E3779B97F4A7C15L + salt * 0xBF58476D1CE4E5B9L;
        value ^= value >>> 31;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 29;
        return (float) ((value >>> 11) / (double) (1L << 53));
    }

    @Override
    public void close() {
        pixel.dispose();
    }
}

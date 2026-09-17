package com.amirrezahadipoor.herodefense;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.amirrezahadipoor.herodefense.render.DisplayMetrics;
import com.amirrezahadipoor.herodefense.render.GameFonts;
import com.amirrezahadipoor.herodefense.render.ScreenEdges;

/** The window-to-design mapping, recomputed on every resize (the game class keeps only the call). */
public final class ViewportMetrics {

    private ViewportMetrics() {
    }

    public static com.amirrezahadipoor.herodefense.render.DisplayMetrics apply(Viewport viewport, Camera camera, int width, int height) {
        DisplayMetrics metrics = new DisplayMetrics(width, height, Gdx.graphics.getDensity());
        viewport.update(width, height, false);
        // Keep the 1280-unit design area centred; overflow is split above and below it.
        camera.position.set(
            WorldLayout.REFERENCE_WIDTH * 0.5f,
            WorldLayout.REFERENCE_HEIGHT * 0.5f,
            0f
        );
        camera.update();
        GameFonts.shared().rebuild(metrics);
        ScreenEdges.update(metrics);
        return metrics;
    }
}

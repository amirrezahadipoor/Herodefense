package com.amirrezahadipoor.herodefense.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Converts Android pointer coordinates into fixed world coordinates and emits only
 * tap/drag callbacks. Keyboard and mouse-specific controls are intentionally absent.
 *
 * <p>A finger never kills the game: whatever a handler throws, the dispatch catches, logs,
 * and answers as a consumed touch. A crashed run loses the wave, the vigil, and the save
 * cadence with it; a swallowed tap only loses the tap.
 */
public final class TouchInputController extends InputAdapter {
    public interface Listener {
        boolean onTouchDown(float worldX, float worldY, int pointer);
        boolean onTouchDragged(float worldX, float worldY, float deltaX, float deltaY, int pointer);
        boolean onTouchUp(float worldX, float worldY, int pointer, boolean isTap);
    }

    private static final float TAP_SLOP_WORLD_UNITS = 28f;

    private final Viewport viewport;
    private final Listener listener;
    private final Vector3 unprojected = new Vector3();
    private final Vector2[] downPositions = new Vector2[10];
    private final Vector2[] previousPositions = new Vector2[10];
    private final boolean[] dragging = new boolean[10];

    public TouchInputController(Viewport viewport, Listener listener) {
        this.viewport = viewport;
        this.listener = listener;
        for (int index = 0; index < downPositions.length; index++) {
            downPositions[index] = new Vector2();
            previousPositions[index] = new Vector2();
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!supports(pointer)) {
            return false;
        }
        Vector3 world = toWorld(screenX, screenY);
        downPositions[pointer].set(world.x, world.y);
        previousPositions[pointer].set(world.x, world.y);
        dragging[pointer] = false;
        return guard(() -> listener.onTouchDown(world.x, world.y, pointer), "touchDown");
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!supports(pointer)) {
            return false;
        }
        Vector3 world = toWorld(screenX, screenY);
        Vector2 previous = previousPositions[pointer];
        float deltaX = world.x - previous.x;
        float deltaY = world.y - previous.y;
        if (downPositions[pointer].dst2(world.x, world.y)
            > TAP_SLOP_WORLD_UNITS * TAP_SLOP_WORLD_UNITS) {
            dragging[pointer] = true;
        }
        previous.set(world.x, world.y);
        return guard(() -> listener.onTouchDragged(world.x, world.y, deltaX, deltaY, pointer),
            "touchDragged");
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (!supports(pointer)) {
            return false;
        }
        Vector3 world = toWorld(screenX, screenY);
        boolean isTap = !dragging[pointer]
            && downPositions[pointer].dst2(world.x, world.y)
            <= TAP_SLOP_WORLD_UNITS * TAP_SLOP_WORLD_UNITS;
        dragging[pointer] = false;
        return guard(() -> listener.onTouchUp(world.x, world.y, pointer, isTap), "touchUp");
    }

    /**
     * Runs one dispatch leg; anything it throws is logged and answered as a consumed touch.
     * Throwable is deliberate: an input handler is the one place the game must survive even an
     * Error, because the alternative is a dead app holding an unsaved run.
     */
    @SuppressWarnings({"PMD.AvoidCatchingThrowable", "PMD.GuardLogStatement"})
    private boolean guard(java.util.concurrent.Callable<Boolean> leg, String where) {
        try {
            return leg.call();
        } catch (Throwable problem) {
            if (Gdx.app != null && Gdx.app.getLogLevel() >= com.badlogic.gdx.Application.LOG_ERROR) {
                Gdx.app.error("TouchInputController", "touch handler threw in " + where, problem);
            }
            return true;
        }
    }

    private Vector3 toWorld(int screenX, int screenY) {
        return viewport.unproject(unprojected.set(screenX, screenY, 0f));
    }

    private boolean supports(int pointer) {
        return pointer >= 0 && pointer < downPositions.length;
    }
}

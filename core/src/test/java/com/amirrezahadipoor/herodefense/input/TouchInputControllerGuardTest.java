package com.amirrezahadipoor.herodefense.input;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.viewport.Viewport;
import org.junit.jupiter.api.Test;

/**
 * A finger never kills the game: whatever the routed handler throws, the dispatch survives,
 * logs, and answers as a consumed touch -- because a crashed run loses the save cadence too.
 */
class TouchInputControllerGuardTest {

    /** A viewport whose unproject is the identity: screen coords are world coords. */
    private static Viewport identityViewport() {
        return new Viewport() {
            @Override
            public com.badlogic.gdx.math.Vector3 unproject(
                com.badlogic.gdx.math.Vector3 screenCoords
            ) {
                return screenCoords;
            }
        };
    }

    private static final class ExplodingListener implements TouchInputController.Listener {
        @Override public boolean onTouchDown(float x, float y, int pointer) {
            throw new IllegalStateException("handler blew up on down");
        }

        @Override public boolean onTouchDragged(float x, float y, float dx, float dy, int pointer) {
            throw new IllegalStateException("handler blew up on drag");
        }

        @Override public boolean onTouchUp(float x, float y, int pointer, boolean isTap) {
            throw new IllegalStateException("handler blew up on up");
        }
    }

    @Test
    void aHandlerThatThrowsStillAnswersAsAConsumedTouch() {
        TouchInputController controller =
            new TouchInputController(identityViewport(), new ExplodingListener());
        assertTrue(controller.touchDown(10, 10, 0, 0));
        assertTrue(controller.touchDragged(40, 40, 0));
        assertTrue(controller.touchUp(10, 10, 0, 0));
    }

    @Test
    void aHealthyListenerStillReachesThePlayer() {
        TouchInputController.Listener healthy = new TouchInputController.Listener() {
            @Override public boolean onTouchDown(float x, float y, int pointer) {
                return true;
            }

            @Override public boolean onTouchDragged(float x, float y, float dx, float dy, int p) {
                return true;
            }

            @Override public boolean onTouchUp(float x, float y, int pointer, boolean isTap) {
                return isTap;
            }
        };
        TouchInputController controller = new TouchInputController(identityViewport(), healthy);
        assertTrue(controller.touchDown(0, 0, 0, 0));
        assertTrue(controller.touchUp(1, 1, 0, 0));
        assertFalse(controller.touchUp(500, 500, 0, 0));
    }

    @Test
    void outOfRangePointersAreIgnoredWithoutTouchingTheListener() {
        TouchInputController controller =
            new TouchInputController(identityViewport(), new ExplodingListener());
        assertFalse(controller.touchDown(0, 0, 11, 0));
        assertFalse(controller.touchDragged(0, 0, 11));
        assertFalse(controller.touchUp(0, 0, 11, 0));
    }
}

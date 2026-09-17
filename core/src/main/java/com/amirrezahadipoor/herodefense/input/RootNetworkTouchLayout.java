package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.ascension.RootNetworkCatalog;
import com.amirrezahadipoor.herodefense.ascension.RootNodeDefinition;
import com.amirrezahadipoor.herodefense.render.UiMirror;

/**
 * Touch mapping for Root Network nodes laid along the tree.
 *
 * <p>The close box is this screen's one asymmetric element -- it sits 20f from the left edge of a 720f screen,
 * which is not its own mirror image -- so it gets its x from {@link #closeX()}, the same {@link UiMirror} call
 * the renderer makes for the box and for the word centred in it. A mirrored drawing over an unmoved hit test is
 * a button the player can see in one place and press in another. The nodes are not mirrored: their coordinates
 * are the shape of the tree itself, they carry icons rather than words, and moving them would move the whole
 * network's hit tests for a screen whose only text on a node is centred inside it.
 */
public final class RootNetworkTouchLayout {
    public static final float CLOSE_X = 20f;
    /** How far the close box's trailing edge sits from the screen's trailing edge: 720f - 20f - 120f. */
    static final float CLOSE_TRAILING_INSET = 580f;
    public static final float CLOSE_Y = 1140f;
    public static final float CLOSE_W = 120f;
    public static final float CLOSE_H = 96f;
    public static final float NODE_RADIUS = 36f;

    private RootNetworkTouchLayout() {}

    /** The close box's leading edge in the language in force; {@link #CLOSE_X} is the left-to-right one. */
    public static float closeX() {
        return UiMirror.leadingOnScreen(CLOSE_X, CLOSE_W);
    }

    public static boolean closeAt(float x, float y) {
        float closeX = closeX();
        return x >= closeX && x <= closeX + CLOSE_W && y >= CLOSE_Y && y <= CLOSE_Y + CLOSE_H;
    }

    public static String nodeAt(float x, float y) {
        for (RootNodeDefinition def : RootNetworkCatalog.all()) {
            float dx = x - def.x();
            float dy = y - def.y();
            if (dx * dx + dy * dy <= NODE_RADIUS * NODE_RADIUS) {
                return def.id();
            }
        }
        return null;
    }
}

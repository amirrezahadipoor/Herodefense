package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Builds the three arrow sprites at startup.
 *
 * <p>Extracted from {@code CombatEntityRenderer} (roadmap R2.2 extraction, paying for R8.3): that class is
 * frozen by the architecture ratchet, so the lines the residency release pass needed had to come from
 * somewhere. Each arrow is drawn into a small pixmap - shaft, a head that is at least a quarter of the
 * length, two feathers - and the three shipped looks are the parameters.
 */
final class ArrowTextures {

    static Texture arrow(int w, int h,
                                              float shaftR, float shaftG, float shaftB,
                                              float headR, float headG, float headB,
                                              float fletchR, float fletchG, float fletchB) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        // shaft
        int shaftX0 = Math.max(1, (int) (w * 0.18f));
        int shaftX1 = (int) (w * 0.74f);
        int shaftY0 = h / 2 - Math.max(1, h / 6);
        int shaftY1 = h / 2 + Math.max(1, h / 6);
        pm.setColor(shaftR, shaftG, shaftB, 1f);
        pm.fillRectangle(shaftX0, shaftY0, shaftX1 - shaftX0, shaftY1 - shaftY0 + 1);
        // head triangle pointed +X, head is >=25% of length
        pm.setColor(headR, headG, headB, 1f);
        int headBase = shaftX1;
        int tipX = w - 1;
        int mid = h / 2;
        for (int x = headBase; x <= tipX; x++) {
            float t = (x - headBase) / (float) Math.max(1, tipX - headBase);
            int half = (int) ((1f - t) * (h * 0.5f));
            int y0 = mid - half;
            int y1 = mid + half;
            int hh = Math.max(1, y1 - y0 + 1);
            pm.fillRectangle(x, y0, 1, hh);
        }
        // fletching: two small feathers at tail
        pm.setColor(fletchR, fletchG, fletchB, 1f);
        int f0 = 1;
        int f1 = shaftX0;
        int featherH = Math.max(2, h / 3);
        pm.fillRectangle(f0, 0, f1 - f0, featherH);
        pm.fillRectangle(f0, h - featherH, f1 - f0, featherH);
        // small notch
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fillRectangle(f0, mid, 1, 1);
        Texture tex = new Texture(pm);
        pm.dispose();
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return tex;
    }

    private ArrowTextures() {
    }
}

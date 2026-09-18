package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.ItemStrings;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.amirrezahadipoor.herodefense.polish.FloatingCoinText;
import com.amirrezahadipoor.herodefense.polish.FloatingCoinTextSystem;

/** High-contrast golden "$ +N" feedback that rises above the Hero after kills. */
public final class FloatingCoinTextRenderer implements AutoCloseable {
    private final GlyphLayout layout = new GlyphLayout();

    public void draw(
        SpriteBatch batch,
        Matrix4 projection,
        FloatingCoinTextSystem system
    ) {
        if (system.labels().isEmpty()) return;
        BitmapFont font = GameFonts.shared().font(GameFonts.Role.HEADING);
        batch.setProjectionMatrix(projection);
        batch.begin();
        for (FloatingCoinText label : system.labels()) {
            String text = labelFor(label.amount);
            layout.setText(font, text);
            float x = label.x - layout.width * 0.5f;
            float alpha = Math.min(1f, label.lifeRatio() * 1.8f);
            font.setColor(0.04f, 0.075f, 0.09f, alpha * 0.88f);
            font.draw(batch, text, x + 2f, label.y - 3f);
            font.setColor(1f, 0.82f, 0.30f, alpha);
            font.draw(batch, text, x, label.y);
        }
        batch.end();
    }

    /** The label in the language in force: "$ +30", or "+۳۰ سکه" which the shaper lays out as "سکه ۳۰+". */
    static String labelFor(int amount) {
        return GameLocale.text(ItemStrings.FLOATING_COIN, GameLocale.number(Math.max(0, amount)));
    }

    @Override
    public void close() {
        // Shared font owned by GameFonts.
    }
}

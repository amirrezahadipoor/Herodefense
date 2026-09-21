package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.RootNetworkStrings;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.amirrezahadipoor.herodefense.ascension.RootNetworkCatalog;
import com.amirrezahadipoor.herodefense.ascension.RootNetworkSystem;
import com.amirrezahadipoor.herodefense.ascension.RootNodeDefinition;
import com.amirrezahadipoor.herodefense.ascension.RootNodeBonusType;
import com.amirrezahadipoor.herodefense.input.RootNetworkTouchLayout;
import com.amirrezahadipoor.herodefense.model.GameState;

/** Renders World Tree full-screen with root-node overlays. Reuses existing tree art concept. */
public final class RootNetworkOverlayRenderer implements AutoCloseable {
    /** The header's inset from the screen's leading edge. */
    static final float TITLE_INSET = 40f;
    /** Where the Heartwood total starts, in the left-to-right layout; it mirrors about the screen. */
    static final float HEARTWOOD_X = 480f;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OverlayText text = new OverlayText();

    public void draw(
        SpriteBatch batch,
        Matrix4 projection,
        GameState state,
        RootNetworkSystem system,
        UiIconRenderer icons,
        UiFrameRenderer frames,
        SaplingTreeRenderer treeRenderer,
        float ambientSeconds
    ) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.06f, 0.12f, 0.08f, 0.92f);
        shapes.rect(0f, 0f, 720f, 1280f);
        shapes.setColor(0.12f, 0.18f, 0.11f, 1f);
        shapes.rect(200f, 0f, 320f, 1280f);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(projection);
        batch.begin();
        frames.draw(batch, UiFrameRenderer.Kind.PANEL, 20f, 1120f, 680f, 140f, true, false);
        frames.draw(batch, UiFrameRenderer.Kind.PANEL, 460f, 1140f, 200f, 80f, true, false);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (RootNodeDefinition def : RootNetworkCatalog.all()) {
            for (String reqId : def.requires()) {
                RootNodeDefinition req = RootNetworkCatalog.byId(reqId);
                if (req == null) continue;
                boolean purchased = system.isPurchased(state, reqId) && system.isPurchased(state, def.id());
                if (purchased) shapes.setColor(0.74f, 0.76f, 0.35f, 0.9f);
                else shapes.setColor(0.3f, 0.35f, 0.3f, 0.6f);
                shapes.rectLine(req.x(), req.y(), def.x(), def.y(), 4f);
            }
        }
        for (RootNodeDefinition def : RootNetworkCatalog.all()) {
            boolean purchased = system.isPurchased(state, def.id());
            boolean canBuy = system.canPurchase(state, def.id());
            if (purchased) shapes.setColor(0.85f, 0.78f, 0.25f, 1f);
            else if (canBuy) shapes.setColor(0.45f, 0.65f, 0.45f, 0.9f);
            else shapes.setColor(0.25f, 0.25f, 0.25f, 0.7f);
            shapes.circle(def.x(), def.y(), RootNetworkTouchLayout.NODE_RADIUS);
            if (purchased) {
                shapes.setColor(1f, 0.95f, 0.5f, 0.6f);
                shapes.circle(def.x(), def.y(), RootNetworkTouchLayout.NODE_RADIUS + 6f);
            }
        }
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        drawDawnLedgerShapes(projection, state);

        batch.setProjectionMatrix(projection);
        batch.begin();
        for (RootNodeDefinition def : RootNetworkCatalog.all()) {
            boolean purchased = system.isPurchased(state, def.id());
            String icon = iconKeyFor(def.bonusType());
            icons.draw(batch, icon, def.x() - 16f, def.y() - 16f, 32f);
            if (purchased) {
                text.drawCentered(batch, GameLocale.text(RootNetworkStrings.AWAKENED),
                    def.x(), def.y() + 28f, 0.8f, OverlayText.GOLD, 1f);
            }
        }
        text.drawLeading(batch, GameLocale.text(RootNetworkStrings.TITLE), 0f, UiMirror.SCREEN_WIDTH,
            TITLE_INSET, 1220f, 1.2f, OverlayText.GOLD, 1f);
        text.drawLeading(batch, GameLocale.text(RootNetworkStrings.SUBTITLE), 0f, UiMirror.SCREEN_WIDTH,
            TITLE_INSET, 1180f, 0.7f, OverlayText.SUBTLE, 1f);
        String heartwood = GameLocale.text(
            RootNetworkStrings.HEARTWOOD, GameLocale.number(state.heartwood));
        text.draw(batch, heartwood,
            UiMirror.leadingOnScreen(HEARTWOOD_X, text.width(heartwood, 0.9f)),
            1190f, 0.9f, OverlayText.IVORY, 1f);
        float closeX = RootNetworkTouchLayout.closeX();
        frames.draw(batch, UiFrameRenderer.Kind.BUTTON,
            closeX, RootNetworkTouchLayout.CLOSE_Y,
            RootNetworkTouchLayout.CLOSE_W, RootNetworkTouchLayout.CLOSE_H, true, false);
        text.drawCentered(batch, GameLocale.text(RootNetworkStrings.CLOSE),
            closeX + RootNetworkTouchLayout.CLOSE_W * 0.5f, 1190f, 0.8f, OverlayText.IVORY, 1f);
        String feedback = system.feedbackMessage();
        if (feedback != null) {
            float alpha = system.feedbackAlpha();
            text.drawCentered(batch, feedback, 360f, 200f, 1.0f, OverlayText.GOLD, alpha);
        }
        text.drawCentered(batch, GameLocale.text(RootNetworkStrings.HINT),
            UiMirror.SCREEN_WIDTH * 0.5f, 140f, 0.7f, OverlayText.SUBTLE, 1f);
        drawDawnLedgerText(batch, state);
        batch.end();
    }

    /**
     * The dawn ledger: the dark band at the hub's foot, one carved notch per dawn the player has
     * brought back (one per completed ascension). The band is the record where a player can look
     * at it -- the tree remembers, and this is what remembering looks like.
     */
    private void drawDawnLedgerShapes(Matrix4 projection, GameState state) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.04f, 0.07f, 0.05f, 0.9f);
        shapes.rect(
            DawnLedger.BAND_X, DawnLedger.BAND_Y, DawnLedger.BAND_WIDTH, DawnLedger.BAND_HEIGHT
        );
        int notches = DawnLedger.notchesFor(Math.max(0, state.totalAscensionsCompleted));
        for (int i = 0; i < notches; i++) {
            boolean newest = i == notches - 1;
            // The newest dawn is the one still warm.
            shapes.setColor(
                0.85f, newest ? 0.95f : 0.78f, 0.45f, newest ? 0.95f : 0.65f
            );
            shapes.rect(
                DawnLedger.notchX(i), DawnLedger.notchY(i),
                DawnLedger.NOTCH_WIDTH, DawnLedger.NOTCH_HEIGHT
            );
        }
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawDawnLedgerText(SpriteBatch batch, GameState state) {
        int dawns = Math.max(0, state.totalAscensionsCompleted);
        text.drawTrailing(
            batch,
            GameLocale.text(RootNetworkStrings.DAWNS, GameLocale.number(dawns)),
            0f, UiMirror.SCREEN_WIDTH,
            UiMirror.SCREEN_WIDTH - DawnLedger.BAND_X - DawnLedger.BAND_WIDTH + 16f,
            DawnLedger.BAND_Y + DawnLedger.BAND_HEIGHT * 0.5f - 5f,
            0.8f, OverlayText.GOLD, 1f
        );
        if (dawns == 0) {
            text.drawCentered(
                batch, GameLocale.text(RootNetworkStrings.DAWNS_EMPTY),
                UiMirror.SCREEN_WIDTH * 0.5f,
                DawnLedger.BAND_Y + DawnLedger.BAND_HEIGHT * 0.5f - 5f,
                0.7f, OverlayText.SUBTLE, 1f
            );
        }
    }

    /** Reviewed icon key per node bonus; every key must resolve in `UiIconRenderer`. */
    static String iconKeyFor(RootNodeBonusType bonusType) {
        return switch (bonusType) {
            case STARTING_STRENGTH -> "strength";
            case STARTING_AGILITY -> "agility";
            case STARTING_LUCK -> "luck";
            case STARTING_DODGE -> "dodge";
            case STARTING_HEALTH -> "health";
            case STARTING_COIN -> "coin";
            case STARTING_TALENT_POINT -> "general_power";
            case FOCUS_FILL_BONUS -> "skill_chain_lightning";
            case MAX_HEALTH_BONUS -> "health";
        };
    }

    @Override
    public void close() {
        text.close();
        shapes.dispose();
    }
}

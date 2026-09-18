package com.amirrezahadipoor.herodefense.render;

import com.amirrezahadipoor.herodefense.i18n.GameLocale;
import com.amirrezahadipoor.herodefense.i18n.PathStrings;
import com.amirrezahadipoor.herodefense.i18n.TrialStrings;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.amirrezahadipoor.herodefense.input.TrialDraftTouchLayout;
import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.model.HeroPath;
import com.amirrezahadipoor.herodefense.trials.TrialDraftSystem;
import com.amirrezahadipoor.herodefense.trials.TrialId;

import java.util.Locale;

/**
 * Pre-run trial draft in the post-boss card-choice presentation: convergence context,
 * four framed trial cards with green reward and red risk lines, and explicit rules.
 *
 * <p>The draft opens on its path phase (roadmap B3): while no hero path is bound, the same
 * four slots carry the four path cards in the same grammar -- icon, name, green bend, red
 * price -- and the trials appear only once a path is chosen.
 */
public final class TrialDraftOverlayRenderer implements AutoCloseable {
    static final float HEADER_PANEL_X = 60f;
    static final float HEADER_PANEL_Y = 1010f;
    static final float HEADER_PANEL_WIDTH = 600f;
    static final float HEADER_PANEL_HEIGHT = 190f;
    static final float FOOTER_PANEL_Y = 140f;
    static final float FOOTER_PANEL_HEIGHT = 104f;
    static final float ICON_SIZE = 84f;
    /**
     * A card's text starts this far in from its leading edge, clear of the 84f icon at 26f, and the corner
     * word ends the same distance from its trailing edge. The header and footer panels are 600f wide at x=60f
     * of a 720f screen, so each is its own mirror image and neither needs a call: 2 * 60f + 600f is 720f.
     */
    static final float CARD_TEXT_INSET = 130f;
    static final float CARD_TEXT_INSET_RIGHT = 26f;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OverlayText text = new OverlayText();

    public void draw(
        SpriteBatch batch,
        Matrix4 projection,
        GameState state,
        UiIconRenderer icons,
        UiFrameRenderer frames
    ) {
        if (!state.draftPending()
            || state.pendingTrialOffer.size() != TrialDraftSystem.OFFER_COUNT) {
            return;
        }
        boolean pathPhase = state.heroPath == null;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.065f, 0.140f, 0.120f, 0.84f);
        shapes.rect(0f, ScreenEdges.bottom(), 720f, ScreenEdges.height());
        shapes.setColor(0.09f, 0.16f, 0.06f, 0.55f);
        shapes.rect(0f, 1010f, 720f, 270f);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(projection);
        batch.begin();
        frames.draw(batch, UiFrameRenderer.Kind.PANEL, HEADER_PANEL_X, HEADER_PANEL_Y,
            HEADER_PANEL_WIDTH, HEADER_PANEL_HEIGHT, true, false);
        for (int index = 0; index < TrialDraftSystem.OFFER_COUNT; index++) {
            float y = cardY(index);
            boolean picked = !pathPhase && isPicked(state, index);
            frames.draw(batch, UiFrameRenderer.Kind.BUTTON, TrialDraftTouchLayout.CARD_X, y,
                TrialDraftTouchLayout.CARD_WIDTH, TrialDraftTouchLayout.CARD_HEIGHT,
                true, picked);
        }
        frames.draw(batch, UiFrameRenderer.Kind.PANEL, HEADER_PANEL_X, FOOTER_PANEL_Y,
            HEADER_PANEL_WIDTH, FOOTER_PANEL_HEIGHT, true, false);

        text.drawCentered(batch,
            GameLocale.text(pathPhase ? PathStrings.HEADER_TITLE : TrialStrings.OFFER_TITLE),
            360f, 1168f, 0.78f, OverlayText.GOLD);
        text.drawCentered(batch,
            GameLocale.text(pathPhase ? PathStrings.HEADER_SUB : TrialStrings.CHOOSE_TWO),
            360f, 1122f, 1.62f, OverlayText.GOLD);
        if (!pathPhase) {
            text.drawCentered(batch, picksLabel(state), 360f, 1066f, 0.74f,
                OverlayText.SUBTLE);
        }

        for (int index = 0; index < TrialDraftSystem.OFFER_COUNT; index++) {
            float y = cardY(index);
            boolean picked = !pathPhase && isPicked(state, index);
            UiFrameRenderer.State cardState = frames.resolve(
                true, picked, TrialDraftTouchLayout.CARD_X, y,
                TrialDraftTouchLayout.CARD_WIDTH, TrialDraftTouchLayout.CARD_HEIGHT
            );
            float offset = MainMenuRenderer.pressedOffset(cardState);
            if (pathPhase) {
                drawPathCard(batch, index, y, offset, cardState, icons);
                continue;
            }
            TrialId trial = TrialId.valueOf(state.pendingTrialOffer.get(index));
            icons.draw(batch, trial.iconKey(), TrialDraftTouchLayout.CARD_X + 26f,
                y + 40f + offset, ICON_SIZE, cardState);
            text.drawLeading(batch, trial.title().toUpperCase(Locale.ROOT),
                TrialDraftTouchLayout.CARD_X, TrialDraftTouchLayout.CARD_WIDTH, CARD_TEXT_INSET,
                y + 126f + offset, 1.12f, OverlayText.IVORY);
            text.drawLeading(batch, trial.reward(),
                TrialDraftTouchLayout.CARD_X, TrialDraftTouchLayout.CARD_WIDTH, CARD_TEXT_INSET,
                y + 88f + offset, 0.92f, OverlayText.POSITIVE);
            text.drawLeading(batch, trial.risk(),
                TrialDraftTouchLayout.CARD_X, TrialDraftTouchLayout.CARD_WIDTH, CARD_TEXT_INSET,
                y + 52f + offset, 0.92f, OverlayText.NEGATIVE);
            text.drawTrailing(batch,
                GameLocale.text(picked ? TrialStrings.CARD_CHOSEN : TrialStrings.CARD_TRIAL),
                TrialDraftTouchLayout.CARD_X, TrialDraftTouchLayout.CARD_WIDTH, CARD_TEXT_INSET_RIGHT,
                y + 126f + offset, 0.60f,
                picked ? OverlayText.GOLD : OverlayText.SUBTLE);
        }

        text.drawCentered(batch,
            GameLocale.text(pathPhase ? PathStrings.FOOTER_NOTE : TrialStrings.BIND_NOTE),
            360f, FOOTER_PANEL_Y + 66f, 0.70f, OverlayText.IVORY);
        text.drawCentered(batch, GameLocale.text(TrialStrings.TAP_HINT),
            360f, FOOTER_PANEL_Y + 34f, 0.62f, OverlayText.SUBTLE);
        batch.end();
    }

    /** One path card in the trial card's exact grammar: icon, name, green bend, red price, corner word. */
    private void drawPathCard(
        SpriteBatch batch,
        int index,
        float y,
        float offset,
        UiFrameRenderer.State cardState,
        UiIconRenderer icons
    ) {
        HeroPath path = HeroPath.values()[index];
        icons.draw(batch, path.iconKey(), TrialDraftTouchLayout.CARD_X + 26f,
            y + 40f + offset, ICON_SIZE, cardState);
        text.drawLeading(batch, GameLocale.text(nameOf(path)).toUpperCase(Locale.ROOT),
            TrialDraftTouchLayout.CARD_X, TrialDraftTouchLayout.CARD_WIDTH, CARD_TEXT_INSET,
            y + 126f + offset, 1.12f, OverlayText.IVORY);
        text.drawLeading(batch, GameLocale.text(rewardOf(path)),
            TrialDraftTouchLayout.CARD_X, TrialDraftTouchLayout.CARD_WIDTH, CARD_TEXT_INSET,
            y + 88f + offset, 0.92f, OverlayText.POSITIVE);
        text.drawLeading(batch, GameLocale.text(riskOf(path)),
            TrialDraftTouchLayout.CARD_X, TrialDraftTouchLayout.CARD_WIDTH, CARD_TEXT_INSET,
            y + 52f + offset, 0.92f, OverlayText.NEGATIVE);
        text.drawTrailing(batch, GameLocale.text(PathStrings.CARD_CORNER),
            TrialDraftTouchLayout.CARD_X, TrialDraftTouchLayout.CARD_WIDTH, CARD_TEXT_INSET_RIGHT,
            y + 126f + offset, 0.60f, OverlayText.SUBTLE);
    }

    static PathStrings nameOf(HeroPath path) {
        return switch (path) {
            case UNBOUND -> PathStrings.NAME_UNBOUND;
            case ROOT -> PathStrings.NAME_ROOT;
            case WIND -> PathStrings.NAME_WIND;
            case STAR -> PathStrings.NAME_STAR;
        };
    }

    static PathStrings rewardOf(HeroPath path) {
        return switch (path) {
            case UNBOUND -> PathStrings.REWARD_UNBOUND;
            case ROOT -> PathStrings.REWARD_ROOT;
            case WIND -> PathStrings.REWARD_WIND;
            case STAR -> PathStrings.REWARD_STAR;
        };
    }

    static PathStrings riskOf(HeroPath path) {
        return switch (path) {
            case UNBOUND -> PathStrings.RISK_UNBOUND;
            case ROOT -> PathStrings.RISK_ROOT;
            case WIND -> PathStrings.RISK_WIND;
            case STAR -> PathStrings.RISK_STAR;
        };
    }

    static float cardY(int index) {
        return TrialDraftTouchLayout.FIRST_CARD_Y - index * TrialDraftTouchLayout.CARD_STRIDE;
    }

    static String picksLabel(GameState state) {
        int picks = state.trialDraftPicks == null ? 0 : state.trialDraftPicks.size();
        return GameLocale.text(
            TrialStrings.PICK_STATUS,
            GameLocale.number(picks),
            GameLocale.number(TrialDraftSystem.PICK_COUNT));
    }

    private static boolean isPicked(GameState state, int index) {
        return state.trialDraftPicks != null
            && state.trialDraftPicks.contains(state.pendingTrialOffer.get(index));
    }

    @Override
    public void close() {
        text.close();
        shapes.dispose();
    }
}

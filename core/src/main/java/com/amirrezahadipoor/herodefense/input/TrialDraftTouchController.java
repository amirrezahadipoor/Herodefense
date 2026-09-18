package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.trials.TrialDraftSystem;

/** Converts card taps into the path choice first and the draft picks after; true only when the pair is bound. */
public final class TrialDraftTouchController {
    private final TrialDraftSystem draft;

    public TrialDraftTouchController(TrialDraftSystem draft) {
        this.draft = draft;
    }

    public boolean tap(GameState state, float x, float y) {
        int choiceIndex = TrialDraftTouchLayout.cardIndexAt(x, y);
        if (choiceIndex < 0) {
            return false;
        }
        if (state != null && state.heroPath == null) {
            // The same four slots show the paths until one is bound (roadmap B3), then the trials.
            draft.choosePath(state, choiceIndex);
            return false; // binding a path does not end the draft
        }
        return draft.chooseTrial(state, choiceIndex);
    }
}

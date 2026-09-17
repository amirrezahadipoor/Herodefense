package com.amirrezahadipoor.herodefense.input;

import com.amirrezahadipoor.herodefense.model.GameState;
import com.amirrezahadipoor.herodefense.progression.Trophy;
import com.amirrezahadipoor.herodefense.story.LoreCatalog;

/** Read-only Codex list controller: select entries, drag-scroll, close. */
public final class CodexTouchController {
    public enum Action {
        NONE,
        SELECTED,
        CLOSED,
        /** The player switched shelves; the selection belongs to the shelf that is now showing. */
        TAB_CHANGED
    }

    private static final float ROW_DRAG_THRESHOLD = 55f;

    private volatile boolean open;
    private CodexTouchLayout.Tab tab = CodexTouchLayout.Tab.LORE;
    private int selectedIndex = -1;
    private int firstVisibleIndex;
    private float accumulatedDrag;

    public void open() {
        open = true;
        tab = CodexTouchLayout.Tab.LORE;
        selectedIndex = -1;
        firstVisibleIndex = 0;
        accumulatedDrag = 0f;
    }

    public boolean isOpen() {
        return open;
    }

    public void close() {
        this.open = false;
    }

    public CodexTouchLayout.Tab tab() {
        return tab;
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    /**
     * Closes the entry being read and keeps the shelf open (roadmap R7.4).
     *
     * <p>Back closes the deepest thing on screen first, which on this shelf is the selected entry: a player
     * reading a lore page who presses Back expects the page, not the Codex. A tap on the same row selects
     * again, so nothing is lost by the press.
     *
     * @return true when an entry was open and is now closed
     */
    public boolean clearSelection() {
        if (selectedIndex < 0) {
            return false;
        }
        selectedIndex = -1;
        return true;
    }

    /** How many rows the visible shelf has. */
    public int rowCount() {
        return tab == CodexTouchLayout.Tab.TROPHIES ? Trophy.values().length : LoreCatalog.all().size();
    }

    public int firstVisibleIndex() {
        return firstVisibleIndex;
    }

    public Action tap(GameState state, float x, float y) {
        if (!open) return Action.NONE;
        if (CodexTouchLayout.closeAt(x, y)) {
            open = false;
            return Action.CLOSED;
        }
        CodexTouchLayout.Tab hitTab = CodexTouchLayout.tabAt(x, y);
        if (hitTab != null) {
            if (hitTab != tab) {
                tab = hitTab;
                selectedIndex = -1;
                firstVisibleIndex = 0;
                accumulatedDrag = 0f;
                return Action.TAB_CHANGED;
            }
            return Action.NONE;
        }
        int row = CodexTouchLayout.visibleRowAt(tab, x, y);
        if (row < 0) return Action.NONE;
        int index = firstVisibleIndex + row;
        if (index < 0 || index >= rowCount()) return Action.NONE;
        selectedIndex = index;
        return Action.SELECTED;
    }

    public void drag(GameState state, float deltaY) {
        if (!open || state == null) return;
        accumulatedDrag += deltaY;
        int maxFirst = Math.max(0, rowCount() - CodexTouchLayout.VISIBLE_ROWS);
        while (accumulatedDrag >= ROW_DRAG_THRESHOLD) {
            firstVisibleIndex = Math.min(maxFirst, firstVisibleIndex + 1);
            accumulatedDrag -= ROW_DRAG_THRESHOLD;
        }
        while (accumulatedDrag <= -ROW_DRAG_THRESHOLD) {
            firstVisibleIndex = Math.max(0, firstVisibleIndex - 1);
            accumulatedDrag += ROW_DRAG_THRESHOLD;
        }
    }
}

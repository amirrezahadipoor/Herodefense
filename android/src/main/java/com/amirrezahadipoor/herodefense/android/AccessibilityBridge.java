package com.amirrezahadipoor.herodefense.android;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import com.amirrezahadipoor.herodefense.accessibility.AccessibilityLabels;

/**
 * G3d: Android TalkBack bridge.
 * Provides content descriptions for the libGDX view and announces via AccessibilityManager.
 * libGDX does not use Android Views for game UI, so we announce via the root view's
 * accessibility events, which TalkBack will speak.
 */
public final class AccessibilityBridge {
    private final View rootView;
    private final AccessibilityManager accessibilityManager;
    private boolean talkBackEnabled;

    public AccessibilityBridge(Context context, View rootView) {
        this.rootView = rootView;
        this.accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        updateTalkBackState();
    }

    public void updateTalkBackState() {
        if (accessibilityManager != null) {
            talkBackEnabled = accessibilityManager.isEnabled() && accessibilityManager.isTouchExplorationEnabled();
        } else {
            talkBackEnabled = false;
        }
    }

    public boolean isTalkBackEnabled() {
        updateTalkBackState();
        return talkBackEnabled;
    }

    /** Announce text via TalkBack if enabled, otherwise no-op (TTS will handle it). */
    public void announce(String text) {
        if (text == null || text.isEmpty() || rootView == null) return;
        updateTalkBackState();
        if (!talkBackEnabled) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            rootView.announceForAccessibility(text);
        } else {
            AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT);
            event.getText().add(text);
            accessibilityManager.sendAccessibilityEvent(event);
        }
    }

    public void announceLabel(String key) {
        announce(AccessibilityLabels.labelFor(key));
    }

    public void setContentDescription(String key) {
        if (rootView == null) return;
        rootView.setContentDescription(AccessibilityLabels.labelFor(key));
    }
}

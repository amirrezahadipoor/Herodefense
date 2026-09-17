package com.amirrezahadipoor.herodefense.input;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;

/**
 * The one key this game binds: Android's Back (roadmap R7.4).
 *
 * <p>Touch-only play is a rule of this repository, and {@code TouchOnlyInputPolicyTest} fails the build if the
 * touch controller grows a key method. Back is not a gameplay action: it is the platform's own navigation, and
 * on a phone it arrives as a key event. libGDX hands {@code KEYCODE_BACK} to the input processor as
 * {@link Keys#BACK} once the backend is told to catch the key, which is what {@code AndroidLauncher} does —
 * catching it is also what stops the activity finishing on every press, the behaviour the re-audit of
 * 2026-09-17 measured and scored at zero of ten.
 *
 * <p>Everything the press *means* is decided in the core by {@link BackNavigation}, so this class holds no
 * policy. It sits in an {@code InputMultiplexer} ahead of {@link TouchInputController} rather than inside it,
 * because that controller's contract is pointers.
 */
public final class SystemBackKeyHandler extends InputAdapter {

    private final ScreenTouchRouter router;

    public SystemBackKeyHandler(ScreenTouchRouter router) {
        if (router == null) {
            throw new IllegalArgumentException("router cannot be null");
        }
        this.router = router;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode != Keys.BACK) {
            return false;
        }
        router.systemBack();
        return true;
    }
}

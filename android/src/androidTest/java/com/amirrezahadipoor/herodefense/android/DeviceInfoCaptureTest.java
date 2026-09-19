package com.amirrezahadipoor.herodefense.android;

import android.content.Context;
import android.content.res.Configuration;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Build;
import android.util.DisplayMetrics;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * I2: Device evidence beyond headless x86 emulator.
 * Captures Build, DisplayMetrics, Configuration, GPU renderer into a file that CI uploads.
 *
 * <p>The GPU strings are read through an EGL context this test creates and destroys itself. The first
 * version of the test called {@code GLES20.glGetString} bare, which only survived while an earlier
 * instrumented test had initialized EGL in the same process: run on its own — exactly how the
 * human-review and device-evidence workflows run it, filtered to this class — the call has no dispatch
 * table to go through and killed the process outright ("Instrumentation run failed due to Process
 * crashed"). A context of our own makes the query legal wherever the test runs, and the reason an
 * EGL context cannot be made is recorded as evidence instead of crashing the run.
 */
@RunWith(AndroidJUnit4.class)
public final class DeviceInfoCaptureTest {

    @Test
    public void captureDeviceInfo() throws IOException {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        Configuration cfg = ctx.getResources().getConfiguration();

        StringBuilder sb = new StringBuilder();
        sb.append("=== Device Evidence I2 ===\n");
        sb.append("MODEL=").append(Build.MODEL).append("\n");
        sb.append("MANUFACTURER=").append(Build.MANUFACTURER).append("\n");
        sb.append("BOARD=").append(Build.BOARD).append("\n");
        sb.append("HARDWARE=").append(Build.HARDWARE).append("\n");
        sb.append("BRAND=").append(Build.BRAND).append("\n");
        sb.append("DEVICE=").append(Build.DEVICE).append("\n");
        sb.append("PRODUCT=").append(Build.PRODUCT).append("\n");
        sb.append("ABI=").append(Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "unknown").append("\n");
        sb.append("SDK_INT=").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("RELEASE=").append(Build.VERSION.RELEASE).append("\n");
        sb.append("WIDTH_PX=").append(dm.widthPixels).append("\n");
        sb.append("HEIGHT_PX=").append(dm.heightPixels).append("\n");
        sb.append("DENSITY=").append(dm.density).append("\n");
        sb.append("DENSITY_DPI=").append(dm.densityDpi).append("\n");
        sb.append("XDPI=").append(dm.xdpi).append("\n");
        sb.append("YDPI=").append(dm.ydpi).append("\n");
        sb.append("ORIENTATION=").append(cfg.orientation).append("\n");
        sb.append("LOCALE=").append(cfg.getLocales().get(0)).append("\n");
        sb.append("SCREEN_LAYOUT=").append(cfg.screenLayout).append("\n");
        sb.append("TOUCHSCREEN=").append(cfg.touchscreen).append("\n");
        String[] gl = glStrings();
        sb.append("GL_RENDERER=").append(gl[0]).append("\n");
        sb.append("GL_VENDOR=").append(gl[1]).append("\n");
        sb.append("GL_VERSION=").append(gl[2]).append("\n");
        sb.append("=== End ===\n");

        // Write to external files dir that CI can pull via adb or artifact
        File outDir = ctx.getExternalFilesDir(null);
        if (outDir == null) outDir = ctx.getFilesDir();
        File outFile = new File(outDir, "device-info.txt");
        try (FileWriter w = new FileWriter(outFile)) {
            w.write(sb.toString());
        }

        // Also log to instrumentation output for CI summary
        System.out.println(sb.toString());

        // Always passes — this is evidence, not assertion
    }

    /**
     * The GL identity strings, read through an EGL pbuffer context this test owns, or the reason there
     * is none — three copies of the reason, one per line of the report.
     */
    private static String[] glStrings() {
        EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (display == EGL14.EGL_NO_DISPLAY) {
            return unavailable("no EGL display");
        }
        if (!EGL14.eglInitialize(display, null, 0, null, 0)) {
            return unavailable("eglInitialize failed");
        }
        int[] configAttribs = {
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
            EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] matched = new int[1];
        if (!EGL14.eglChooseConfig(display, configAttribs, 0, configs, 0, 1, matched, 0)
            || matched[0] < 1) {
            return unavailable("no EGL config with a GLES2 pbuffer");
        }
        int[] surfaceAttribs = {EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE};
        EGLSurface surface = EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttribs, 0);
        int[] contextAttribs = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
        EGLContext context =
            EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0);
        if (surface == null || surface == EGL14.EGL_NO_SURFACE
            || context == null || context == EGL14.EGL_NO_CONTEXT) {
            return unavailable("could not create an EGL pbuffer context");
        }
        try {
            if (!EGL14.eglMakeCurrent(display, surface, surface, context)) {
                return unavailable("eglMakeCurrent failed");
            }
            return new String[] {
                glString(GLES20.GL_RENDERER), glString(GLES20.GL_VENDOR), glString(GLES20.GL_VERSION)
            };
        } finally {
            // The display stays initialized: another test in this process may already be using EGL,
            // and eglTerminate is process-global. Our surface, context and thread-local state are ours.
            EGL14.eglMakeCurrent(
                display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(display, surface);
            EGL14.eglDestroyContext(display, context);
            EGL14.eglReleaseThread();
        }
    }

    private static String glString(int name) {
        String value = GLES20.glGetString(name);
        return value == null ? "unavailable (glGetString returned null)" : value;
    }

    private static String[] unavailable(String reason) {
        return new String[] {"unavailable (" + reason + ")", "unavailable (" + reason + ")",
            "unavailable (" + reason + ")"};
    }
}

package com.amirrezahadipoor.herodefense.android;

import android.content.res.Configuration;
import android.opengl.GLES20;
import android.os.Build;
import android.util.DisplayMetrics;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * I2: Device evidence beyond headless x86 emulator.
 * Captures Build, DisplayMetrics, Configuration, GPU renderer into a file that CI uploads.
 */
@RunWith(AndroidJUnit4.class)
public final class DeviceInfoCaptureTest {

    @Test
    public void captureDeviceInfo() throws IOException {
        android.content.Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
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
        // GPU renderer requires GL context; try best effort
        try {
            String renderer = GLES20.glGetString(GLES20.GL_RENDERER);
            String vendor = GLES20.glGetString(GLES20.GL_VENDOR);
            String version = GLES20.glGetString(GLES20.GL_VERSION);
            sb.append("GL_RENDERER=").append(renderer).append("\n");
            sb.append("GL_VENDOR=").append(vendor).append("\n");
            sb.append("GL_VERSION=").append(version).append("\n");
        } catch (Exception e) {
            sb.append("GL_RENDERER=unavailable (no GL context in test)\n");
        }
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

        // Always pass — this is evidence, not assertion
    }
}

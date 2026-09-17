package com.amirrezahadipoor.herodefense.android;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.amirrezahadipoor.herodefense.render.DeviceTextureSupport;
import com.amirrezahadipoor.herodefense.render.TexturePayloadPolicy;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Roadmap R8.1's device half: the container the repository's own encoder wrote, uploaded by the GPU in this
 * emulator through GLES3, and compared pixel by pixel with the repository's own decode of the same bytes.
 *
 * <p>The encoder tool refuses to write a payload for every sheet that ships, so nothing in the game reads a
 * container today -- which left the upload path itself unproven. This test proves it against a fixture built
 * for exactly that purpose by {@code tools/texture/make_device_fixture.py}: a 64x64 crop of a real shipped
 * sheet, encoded to ETC2 punchthrough alpha, with the expected RGBA committed beside it. The fixture never
 * enters the game's assets and is never a candidate payload; it is the smallest thing that can answer the
 * question "would this device decode what we would hand it?".
 *
 * <p>The context is a plain EGL pbuffer rather than the running game's: the game asks libGDX for a GLES2
 * context, and ETC2 is a GLES3 feature, so a pbuffer is what lets this test ask the device's driver directly
 * instead of asserting against a context that could never load the payload anyway.
 */
@RunWith(AndroidJUnit4.class)
public final class CompressedTextureDeviceTest {
    /** The instrumentation APK's own assets, not the game's. */
    private static final String FIXTURE = "etc2/rootling-64";
    /** The PNG the fixture's container would sit beside at runtime, which is what the policy is asked about. */
    private static final String SHEET = "generated/sprites/rootling.png";
    /** EGL_OPENGL_ES3_BIT, spelled out because EGL14 does not name it. */
    private static final int EGL_OPENGL_ES3_BIT = 0x0040;
    /** The KTX v1 header, before the four-byte image size and the level data. */
    private static final int HEADER_BYTES = 64;
    /** How far one channel may sit from the decoder's own value: drivers are allowed to round, not to invent. */
    private static final int CHANNEL_TOLERANCE = 8;
    /** A decode that produced one flat colour would pass a tolerance test; this catches that. */
    private static final int MINIMUM_DISTINCT_COLOURS = 64;

    @Test
    public void theDeviceDecodesTheContainerTheEncoderWrote() throws IOException {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        byte[] container = read(context.getAssets(), FIXTURE + ".ktx");
        byte[] expected = read(context.getAssets(), FIXTURE + ".rgba");

        Header header = Header.parse(container);
        assertEquals("the fixture is an ETC2 punchthrough container",
            TexturePayloadPolicy.ETC2_RGB8_PUNCHTHROUGH_ALPHA1, header.glInternalFormat);
        assertEquals("expected pixels are one RGBA per texel",
            header.width * header.height * 4, expected.length);

        EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        assertNotNull("no EGL display", display);
        int[] versions = new int[2];
        assertTrue("EGL could not be initialised", EGL14.eglInitialize(display, versions, 0, versions, 1));
        int[] configAttributes = {
            EGL14.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_NONE,
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] configCount = new int[1];
        assertTrue("no GLES3 pbuffer config on this device",
            EGL14.eglChooseConfig(display, configAttributes, 0, configs, 0, 1, configCount, 0)
                && configCount[0] > 0);
        int[] contextAttributes = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE};
        EGLContext eglContext =
            EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, contextAttributes, 0);
        int[] surfaceAttributes = {
            EGL14.EGL_WIDTH, header.width,
            EGL14.EGL_HEIGHT, header.height,
            EGL14.EGL_NONE,
        };
        EGLSurface surface = EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttributes, 0);
        assertTrue("could not make the GLES3 context current",
            EGL14.eglMakeCurrent(display, surface, surface, eglContext));

        String glVersion = GLES30.glGetString(GLES30.GL_VERSION);
        String glExtensions = GLES30.glGetString(GLES30.GL_EXTENSIONS);
        DeviceTextureSupport support = DeviceTextureSupport.from(glVersion, glExtensions);
        assertEquals("an OpenGL ES 3 driver is what makes an ETC2 payload legal here",
            DeviceTextureSupport.ETC2, support);
        TexturePayloadPolicy policy = new TexturePayloadPolicy(
            support, Collections.singletonMap(SHEET, header.glInternalFormat));
        assertEquals("with a real GL version string the policy picks the container",
            "generated/sprites/compressed/etc2/rootling.ktx", policy.sourceFor(SHEET));

        int texture = upload(header, container);
        int program = program();
        byte[] rendered = render(texture, program, header.width, header.height);
        // The comparison runs against the raw readback; the mismatch counter below is what this test asserts.
        Comparison direct = compare(rendered, expected, header, false);
        Comparison flipped = compare(rendered, expected, header, true);
        Comparison best = direct.maxDelta <= flipped.maxDelta ? direct : flipped;
        Log.i("HERODEFENSE_TEXTURE", "HERODEFENSE_TEXTURE glVersion=" + glVersion
            + " format=" + header.glInternalFormat
            + " size=" + header.width + "x" + header.height
            + " containerBytes=" + container.length
            + " orientation=" + (best == direct ? "top-down" : "bottom-up")
            + " maxDelta=" + best.maxDelta
            + " exactFraction=" + best.exactFraction()
            + " sampledColors=" + best.distinctColours);
        assertTrue("the GPU produced too few colours to be a decode: " + best.distinctColours,
            best.distinctColours >= MINIMUM_DISTINCT_COLOURS);
        String diagnosis = diagnose(rendered, expected, header, container, best == flipped, best);
        // The evidence a failing run cannot otherwise show: the two pictures and the one-line account of how
        // they differ. Written before the assertions so that a failure still leaves them behind, and pulled by
        // the Android workflow out of the app's own files directory with `run-as`, which is why they live there
        // rather than on the shared card -- a scoped-storage device would not hand them over.
        writePixels("texture-rendered.png", rendered, header.width, header.height);
        writePixels("texture-expected.png", expected, header.width, header.height);
        writeText("texture-comparison.txt", diagnosis + "\n" + glLine(glVersion, header));
        assertEquals("the GPU's decode disagrees with the encoder's own decode by more than "
                + CHANNEL_TOLERANCE + " -- " + diagnosis,
            0, best.beyondTolerance);
        assertTrue("alpha must decode to the container's one-bit mask -- " + diagnosis,
            best.alphaMismatches == 0);
        assertEquals("no GL error for the whole upload and draw", GLES30.GL_NO_ERROR, GLES30.glGetError());

        GLES30.glDeleteTextures(1, new int[] {texture}, 0);
        GLES30.glDeleteProgram(program);
        EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroySurface(display, surface);
        EGL14.eglDestroyContext(display, eglContext);
        EGL14.eglTerminate(display);
    }

    private static byte[] read(AssetManager assets, String name) throws IOException {
        try (InputStream stream = assets.open(name)) {
            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int step;
            while ((step = stream.read(chunk)) > 0) {
                blob.write(chunk, 0, step);
            }
            return blob.toByteArray();
        }
    }

    private static int upload(Header header, byte[] container) {
        int[] names = new int[1];
        GLES30.glGenTextures(1, names, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, names[0]);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        ByteBuffer payload = ByteBuffer.allocateDirect(header.payloadLength).order(ByteOrder.nativeOrder());
        payload.put(container, header.payloadOffset, header.payloadLength).position(0);
        GLES30.glCompressedTexImage2D(GLES30.GL_TEXTURE_2D, 0, header.glInternalFormat, header.width,
            header.height, 0, header.payloadLength, payload);
        assertEquals("the upload was rejected by the driver", GLES30.GL_NO_ERROR, GLES30.glGetError());
        return names[0];
    }

    /** A minimal textured-quad program, in the GLSL the requested ES3 context speaks. */
    private static int program() {
        String vertex = "#version 300 es\n"
            + "in vec2 aPosition;\n"
            + "in vec2 aTexel;\n"
            + "out vec2 vTexel;\n"
            + "void main() { vTexel = aTexel; gl_Position = vec4(aPosition, 0.0, 1.0); }\n";
        String fragment = "#version 300 es\n"
            + "precision mediump float;\n"
            + "in vec2 vTexel;\n"
            + "uniform sampler2D uTexture;\n"
            + "out vec4 oColour;\n"
            + "void main() { oColour = texture(uTexture, vTexel); }\n";
        int vertexShader = compile(GLES30.GL_VERTEX_SHADER, vertex);
        int fragmentShader = compile(GLES30.GL_FRAGMENT_SHADER, fragment);
        int program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);
        int[] status = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, status, 0);
        assertEquals("the test program did not link: " + GLES30.glGetProgramInfoLog(program),
            1, status[0]);
        GLES30.glDeleteShader(vertexShader);
        GLES30.glDeleteShader(fragmentShader);
        return program;
    }

    private static int compile(int type, String source) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, source);
        GLES30.glCompileShader(shader);
        int[] status = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0);
        assertEquals("shader did not compile: " + GLES30.glGetShaderInfoLog(shader), 1, status[0]);
        return shader;
    }

    private static byte[] render(int texture, int program, int width, int height) {
        GLES30.glUseProgram(program);
        GLES30.glUniform1i(GLES30.glGetUniformLocation(program, "uTexture"), 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture);
        int position = GLES30.glGetAttribLocation(program, "aPosition");
        int texel = GLES30.glGetAttribLocation(program, "aTexel");
        float[] quad = {
            -1f, -1f, 0f, 0f,
            1f, -1f, 1f, 0f,
            -1f, 1f, 0f, 1f,
            1f, 1f, 1f, 1f,
        };
        ByteBuffer vertices = ByteBuffer.allocateDirect(quad.length * 4).order(ByteOrder.nativeOrder());
        vertices.asFloatBuffer().put(quad).position(0);
        GLES30.glVertexAttribPointer(position, 2, GLES30.GL_FLOAT, false, 16, vertices);
        GLES30.glEnableVertexAttribArray(position);
        vertices.position(8);
        GLES30.glVertexAttribPointer(texel, 2, GLES30.GL_FLOAT, false, 16, vertices);
        GLES30.glEnableVertexAttribArray(texel);
        GLES30.glViewport(0, 0, width, height);
        GLES30.glDisable(GLES30.GL_BLEND);
        GLES30.glClearColor(0f, 0f, 0f, 0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        GLES30.glFinish();
        ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
        GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, pixels);
        return pixels.array();
    }

    /**
     * Compares a readback with the decoder's expectation. Rows are compared in both directions because the two
     * conventions have to agree for the comparison to mean anything and the readback's is the device's to
     * state: whichever orientation matches better is reported in the evidence line.
     */
    /**
     * What a mismatch looks like, in one line: a failing run on a device nobody can attach a debugger to still
     * has to say which rule the driver implemented. The interesting split is the opaque bit -- a driver that
     * reads a punchthrough block's colour part as the individual layout, or that ignores the bit's choice of
     * intensity-modifier table, gets the non-opaque blocks wrong and the opaque ones right, and that shows up
     * here as mismatches in one column and not the other.
     */
    private static String diagnose(
        byte[] rendered, byte[] expected, Header header, byte[] container, boolean flipped, Comparison best) {
        int firstX = -1;
        int firstY = -1;
        int firstExpected = 0;
        int firstRendered = 0;
        int inOpaqueBlocks = 0;
        int inNonOpaqueBlocks = 0;
        int expectedClear = 0;
        for (int y = 0; y < header.height; y++) {
            int sourceRow = (flipped ? header.height - 1 - y : y) * header.width * 4;
            int renderedRow = y * header.width * 4;
            for (int x = 0; x < header.width; x++) {
                int a = renderedRow + x * 4;
                int b = sourceRow + x * 4;
                int worst = 0;
                for (int channel = 0; channel < 3; channel++) {
                    worst = Math.max(worst,
                        Math.abs((rendered[a + channel] & 0xff) - (expected[b + channel] & 0xff)));
                }
                if (worst > CHANNEL_TOLERANCE) {
                    if (opaqueBit(container, header, x, y)) {
                        inOpaqueBlocks++;
                    } else {
                        inNonOpaqueBlocks++;
                    }
                    if (firstX < 0) {
                        firstX = x;
                        firstY = y;
                        firstExpected = rgbAt(expected, b);
                        firstRendered = rgbAt(rendered, a);
                    }
                }
                if ((expected[b + 3] & 0xff) == 0) {
                    expectedClear++;
                }
            }
        }
        return "orientation=" + (flipped ? "bottom-up" : "top-down")
            + " maxDelta=" + best.maxDelta + " beyondTolerance=" + best.beyondTolerance
            + " alphaMismatches=" + best.alphaMismatches + " exact=" + best.exact
            + " distinctColours=" + best.distinctColours
            + " mismatchesInOpaqueBlocks=" + inOpaqueBlocks
            + " mismatchesInNonOpaqueBlocks=" + inNonOpaqueBlocks
            + " expectedClearPixels=" + expectedClear
            + " firstMismatch=(x=" + firstX + ",y=" + firstY
            + " expected=0x" + String.format("%06x", firstExpected)
            + " rendered=0x" + String.format("%06x", firstRendered) + ")";
    }

    /** The opaque bit of the block a texel belongs to, read out of the container the device uploaded. */
    private static boolean opaqueBit(byte[] container, Header header, int x, int y) {
        int blocksPerRow = (header.width + 3) / 4;
        int block = (y / 4) * blocksPerRow + (x / 4);
        int offset = HEADER_BYTES + 4 + block * 8;
        int high = ((container[offset] & 0xff) << 24) | ((container[offset + 1] & 0xff) << 16)
            | ((container[offset + 2] & 0xff) << 8) | (container[offset + 3] & 0xff);
        return ((high >> 1) & 1) == 1;
    }

    private static String glLine(String glVersion, Header header) {
        return "glVersion=" + glVersion + " format=" + header.glInternalFormat
            + " size=" + header.width + "x" + header.height;
    }

    /** One pixel buffer as a PNG in the app's own files directory, where the workflow can pull it. */
    private static void writePixels(String name, byte[] pixels, int width, int height) throws IOException {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] argb = new int[width * height];
        for (int i = 0; i < argb.length; i++) {
            int offset = i * 4;
            argb[i] = ((pixels[offset + 3] & 0xff) << 24) | ((pixels[offset] & 0xff) << 16)
                | ((pixels[offset + 1] & 0xff) << 8) | (pixels[offset + 2] & 0xff);
        }
        bitmap.setPixels(argb, 0, width, 0, 0, width, height);
        File directory = InstrumentationRegistry.getInstrumentation().getTargetContext().getFilesDir();
        try (FileOutputStream stream = new FileOutputStream(new File(directory, name))) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        }
        bitmap.recycle();
    }

    private static void writeText(String name, String text) throws IOException {
        File directory = InstrumentationRegistry.getInstrumentation().getTargetContext().getFilesDir();
        try (FileOutputStream stream = new FileOutputStream(new File(directory, name))) {
            stream.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static int rgbAt(byte[] pixels, int offset) {
        return ((pixels[offset] & 0xff) << 16) | ((pixels[offset + 1] & 0xff) << 8) | (pixels[offset + 2] & 0xff);
    }

    private static Comparison compare(byte[] rendered, byte[] expected, Header header, boolean flipped) {
        Comparison comparison = new Comparison();
        Set<Integer> colours = new HashSet<>();
        for (int y = 0; y < header.height; y++) {
            int sourceRow = (flipped ? header.height - 1 - y : y) * header.width * 4;
            int renderedRow = y * header.width * 4;
            for (int x = 0; x < header.width; x++) {
                int a = renderedRow + x * 4;
                int b = sourceRow + x * 4;
                colours.add(((rendered[a] & 0xff) << 16) | ((rendered[a + 1] & 0xff) << 8)
                    | (rendered[a + 2] & 0xff));
                int worst = 0;
                for (int channel = 0; channel < 3; channel++) {
                    worst = Math.max(worst,
                        Math.abs((rendered[a + channel] & 0xff) - (expected[b + channel] & 0xff)));
                }
                comparison.maxDelta = Math.max(comparison.maxDelta, worst);
                if (worst > CHANNEL_TOLERANCE) {
                    comparison.beyondTolerance++;
                } else if (worst == 0 && (rendered[a + 3] & 0xff) == (expected[b + 3] & 0xff)) {
                    comparison.exact++;
                }
                if (Math.abs((rendered[a + 3] & 0xff) - (expected[b + 3] & 0xff)) > 0) {
                    comparison.alphaMismatches++;
                }
            }
        }
        comparison.total = header.width * header.height;
        comparison.distinctColours = colours.size();
        return comparison;
    }

    private static final class Comparison {
        private int maxDelta;
        private int beyondTolerance;
        private int alphaMismatches;
        private int exact;
        private int total;
        private int distinctColours;

        private String exactFraction() {
            return String.format(java.util.Locale.ROOT, "%.3f", total == 0 ? 0f : (float) exact / total);
        }
    }

    /** The parts of a KTX v1 header this test reads, so the test checks the container rather than assume it. */
    private static final class Header {
        private int glInternalFormat;
        private int width;
        private int height;
        private int payloadOffset;
        private int payloadLength;

        private static Header parse(byte[] container) {
            ByteBuffer header = ByteBuffer.wrap(container).order(ByteOrder.LITTLE_ENDIAN);
            byte[] identifier = new byte[12];
            header.get(identifier);
            byte[] ktxIdentifier = {(byte) 0xAB, 'K', 'T', 'X', ' ', '1', '1', (byte) 0xBB,
                '\r', '\n', (byte) 0x1A, '\n'};
            assertArrayEquals("the fixture is not a KTX container", ktxIdentifier, identifier);
            assertEquals("the fixture's header is not little-endian", 0x04030201, header.getInt());
            header.getInt();
            header.getInt();
            header.getInt();
            Header parsed = new Header();
            parsed.glInternalFormat = header.getInt();
            header.getInt();
            parsed.width = header.getInt();
            parsed.height = header.getInt();
            header.getInt();
            header.getInt();
            header.getInt();
            header.getInt();
            header.getInt();
            parsed.payloadOffset = header.position() + 4;
            parsed.payloadLength = header.getInt();
            assertEquals("the fixture's payload is shorter than its header says",
                container.length - parsed.payloadOffset, parsed.payloadLength);
            return parsed;
        }
    }
}

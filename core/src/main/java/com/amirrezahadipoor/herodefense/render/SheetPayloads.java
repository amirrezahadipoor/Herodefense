package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.KTXTextureData;

/**
 * The one place a sheet becomes a GL texture, so that R8.1's decision has exactly one implementation.
 *
 * <p>Every renderer used to load its own art -- {@code new Texture(png)} for a backdrop or an icon, a
 * {@code SheetPayloads.atlas(pack)} for a clip -- which meant the compressed path had nowhere to live. Now they all
 * come through here, and this class asks {@link AtlasPageSource} what to read. Two shapes are needed because the
 * catalog has two: sheets that are their own texture, and pages that a {@code .atlas} pack file slices into
 * frames.
 *
 * <p>An atlas page is handed to libGDX by filling in the page's own {@code Texture} before the atlas is built.
 * That is the seam {@code AssetManager}'s own atlas loader uses, and it is the only one libGDX offers: the page's
 * filters, wrap and mipmap flags still come from the pack file, so a compressed page is drawn exactly like the
 * PNG was. The container's payload goes to the driver through {@code KTXTextureData}, which reads the same header
 * this repository writes in {@code tools/texture/ktx.py} and calls {@code glCompressedTexImage2D} with the
 * internal format the header names -- no second container format and no new dependency on either side.
 *
 * <p>With no containers in the build -- which is the case today, because no sheet has cleared the encoder's bar --
 * every call here reads the PNG it always read, and {@link #source()} says so if anyone asks.
 */
public final class SheetPayloads {
    private static AtlasPageSource source = AtlasPageSource.pngOnly();

    private SheetPayloads() {
    }

    /**
     * Asks the running context what it can decode and installs the answer. Called once, from the game's own
     * {@code create()}, after the GL context exists and before the first sheet is loaded.
     */
    public static void installForThisDevice() {
        install(new AtlasPageSource(DeviceTextureSupport.from(
            Gdx.gl.glGetString(GL20.GL_VERSION),
            Gdx.gl.glGetString(GL20.GL_EXTENSIONS)
        )));
    }

    /** Tells the loader what this device can decode, without asking the context. */

    public static void install(AtlasPageSource pages) {
        source = pages == null ? AtlasPageSource.pngOnly() : pages;
    }

    /** What the loader was told, for a report or a test. */
    public static AtlasPageSource source() {
        return source;
    }

    /** A sheet that is its own texture: a backdrop, a UI frame, a slot icon. */
    public static Texture texture(String pngPath) {
        return texture(Gdx.files.internal(pngPath));
    }

    /** A sheet that is its own texture, from a handle the caller already holds. */
    public static Texture texture(FileHandle png) {
        AtlasPageSource.Payload payload = source.forPage(png);
        return payload.compressed() ? new Texture(new KTXTextureData(payload.file(), false)) : new Texture(png);
    }

    /** An atlas whose pages are resolved through the same decision. */
    public static TextureAtlas atlas(FileHandle packFile) {
        return atlas(packFile, packFile.parent());
    }

    /** An atlas whose pages are resolved through the same decision, with an explicit images directory. */
    public static TextureAtlas atlas(FileHandle packFile, FileHandle imagesDir) {
        TextureAtlas.TextureAtlasData data = new TextureAtlas.TextureAtlasData(packFile, imagesDir, false);
        for (TextureAtlas.TextureAtlasData.Page page : data.getPages()) {
            AtlasPageSource.Payload payload = source.forPage(page.textureFile, (int) page.width, (int) page.height);
            if (payload.compressed()) {
                page.texture = new Texture(new KTXTextureData(payload.file(), false));
            }
        }
        return new TextureAtlas(data);
    }
}

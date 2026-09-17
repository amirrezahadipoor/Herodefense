package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * What the shipped catalog would cost as compressed textures (roadmap R8.1), computed from the manifest that
 * ships rather than quoted from a specification sheet.
 *
 * <p>This is a projection and it says so: the encoded containers are not in the APK yet, so the arithmetic here
 * is the page dimensions in the manifest multiplied by each format's bytes per pixel, mip chain included. The
 * values are worth publishing because they are reproducible -- {@code :core:residencyReport} prints them for the
 * same manifest the residency gate already checks -- and because they answer the question the memory work needs
 * answered: how much of the catalog would still be resident after compression, and what would mipmaps add back.
 * The RGBA8888 row is not a projection at all: it is the number the existing gate already enforces, and a test
 * asserts that this file reproduces it, so the other rows are measured against something known to be right.
 */
public final class TextureFormatPlan {

    /**
     * One row: a format, what the whole catalog's base levels cost in it, the same catalog with a mip chain on
     * every page, and the live combat set. The base-level column is what makes the row checkable: in RGBA8888 it
     * equals the decoded catalog the residency gate already enforces.
     */
    public record Row(
        TextureFormat format,
        long catalogBytes,
        long catalogWithMipsBytes,
        long combatBytes,
        double catalogMiB
    ) {
    }

    private TextureFormatPlan() {
    }

    /** Every format, with the mip chain counted the way the runtime would count it on every page. */
    public static List<Row> project(JsonValue manifest, RuntimeResidency.IconSizes icons) {
        List<Row> rows = new ArrayList<>();
        for (TextureFormat format : TextureFormat.values()) {
            long catalog = catalogBytes(manifest, icons, format);
            rows.add(new Row(
                format,
                catalog,
                catalogWithMipsBytes(manifest, icons, format),
                combatBytes(manifest, format),
                catalog / 1048576.0
            ));
        }
        return rows;
    }

    /** The catalog's base levels in one format: every sheet plus every icon the manifest names. */
    public static long catalogBytes(JsonValue manifest, RuntimeResidency.IconSizes icons, TextureFormat format) {
        return catalogFor(manifest, icons, format, false);
    }

    /** The same catalog with a mip chain on every page, which is what the runtime would hold if it mipmapped. */
    public static long catalogWithMipsBytes(
        JsonValue manifest,
        RuntimeResidency.IconSizes icons,
        TextureFormat format
    ) {
        return catalogFor(manifest, icons, format, true);
    }

    private static long catalogFor(
        JsonValue manifest,
        RuntimeResidency.IconSizes icons,
        TextureFormat format,
        boolean mipmapped
    ) {
        long total = 0L;
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            total += sheetBytes(asset, format, mipmapped);
            String icon = asset.getString("icon", null);
            if (icon != null) {
                int[] size = icons.sizeOf(icon);
                if (size.length == 2) {
                    total += format.decodedBytes(size[0], size[1], mipmapped);
                }
            }
        }
        return total;
    }

    /** The live combat set in one format: the enemy and boss sheets that are resident during a wave. */
    public static long combatBytes(JsonValue manifest, TextureFormat format) {
        long total = 0L;
        List<String> keys = new ArrayList<>(RuntimeResidency.REGULAR_ENEMIES);
        keys.addAll(RuntimeResidency.BOSSES);
        for (String key : keys) {
            JsonValue asset = assetNamed(manifest, key);
            if (asset != null) {
                total += sheetBytes(asset, format, false);
            }
        }
        return total;
    }

    private static JsonValue assetNamed(JsonValue manifest, String key) {
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            if (key.equals(asset.getString("key", null))) return asset;
        }
        return null;
    }

    /** An asset's sheets in one format. An asset with no sheets (an icon entry, a data entry) contributes zero. */
    private static long sheetBytes(JsonValue asset, TextureFormat format, boolean mipmapped) {
        JsonValue sheets = asset.get("sheets");
        if (sheets == null || sheets.child == null) return 0L;
        boolean alpha = hasAlpha(asset);
        long total = 0L;
        for (JsonValue sheet = sheets.child; sheet != null; sheet = sheet.next) {
            int width = sheet.getInt("width", 0);
            int height = sheet.getInt("height", 0);
            if (width <= 0 || height <= 0) continue;
            total += format.decodedBytes(width, height, mipmapped) * format.sheetCount(alpha);
        }
        return total;
    }

    /**
     * Whether a sheet carries transparency, read from the manifest's own alpha mode. ETC1 has none and needs a
     * companion sheet for these; a page that is opaque does not pay for one.
     */
    private static boolean hasAlpha(JsonValue asset) {
        String mode = asset.getString("alphaMode", "STRAIGHT_RGBA");
        return !mode.toUpperCase(java.util.Locale.ROOT).contains("OPAQUE");
    }
}

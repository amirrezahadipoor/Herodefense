package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.utils.JsonValue;

import java.util.List;

/**
 * Decoded texture residency arithmetic for the committed asset manifest.
 *
 * <p>Reason this exists: the audit of 2026-09-16 measured 1,277 MiB of decoded texture memory in a
 * manifest whose documentation claimed 42 MB of residency, and no test or gate noticed. Residency is now
 * arithmetic over the manifest that ships, checked by a test against a documented budget, so a future
 * batch cannot quietly raise the number again.
 *
 * <p>The combat set is the worst case that is actually live at once: the hero, the regular enemies that
 * can be on screen together, the equipment sheets of a fully dressed hero, one boss and the arena. It is
 * deliberately smaller than the whole catalog (which includes every item, icon and effect).
 *
 * <p>See {@code docs/ROADMAP_TO_1000.md} R8.2.
 */
public final class RuntimeResidency {

    /**
     * Regular enemies that can share a wave: the whole roster, because the spawner cycles every type and a wave can
     * therefore contain all of them. Measured for the eight-role roster (R3.4): 95.9 MiB of the 100 MiB budget, so
     * the roster has roughly one more 5.6 MiB sheet of headroom; a ninth role would need smaller enemy frames or a
     * shared page rather than a quiet budget bump.
     */
    public static final List<String> REGULAR_ENEMIES = List.of(
        "bark_stalker", "bramble_thrall", "fungal_brute", "gloom_wolf",
        "husk_warden", "rootling", "sap_hound", "stonekin"
    );

    /** One boss is alive at a time; the worst single sheet is counted. */
    public static final List<String> BOSSES =
        List.of("ancient_golem", "ember_wyrm", "thorn_matriarch", "void_knight");

    /** Equipment slots the hero can wear at once, as art sheets. */
    public static final int EQUIPPED_SHEETS = 6;

    private RuntimeResidency() {
    }

    /** Decoded bytes of every sheet in the manifest, plus the icons referenced by name. */
    public static long catalogBytes(JsonValue manifest, IconSizes icons) {
        long total = 0;
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            total += sheetBytes(asset);
            if (asset.has("icon")) {
                int[] size = icons.sizeOf(asset.getString("icon"));
                if (size != null && size.length == 2) {
                    total += (long) size[0] * size[1] * 4L;
                }
            }
        }
        return total;
    }

    /** Decoded bytes of the sheets that are resident during a wave. */
    public static long combatBytes(JsonValue manifest) {
        long total = 0;
        JsonValue hero = byKey(manifest, "hero");
        if (hero == null) {
            throw new IllegalArgumentException("manifest has no hero asset");
        }
        total += sheetBytes(hero);
        for (String key : REGULAR_ENEMIES) {
            JsonValue asset = byKey(manifest, key);
            if (asset != null) {
                total += sheetBytes(asset);
            }
        }
        JsonValue boss = heaviest(manifest, BOSSES);
        if (boss != null) {
            total += sheetBytes(boss);
        }
        long equipment = 0;
        int sheets = 0;
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            if (!asset.getString("key").startsWith("equipment_") || sheets >= EQUIPPED_SHEETS) {
                continue;
            }
            equipment += sheetBytes(asset);
            sheets++;
        }
        total += equipment;
        for (String key : List.of("arena_backdrop", "ground_tile_0", "ground_tile_1", "ground_tile_2",
            "crystal_prop_0", "crystal_prop_1", "crystal_prop_2")) {
            JsonValue asset = byKey(manifest, key);
            if (asset != null) {
                total += sheetBytes(asset);
            }
        }
        return total;
    }

    /** Throws when {@code actual} exceeds {@code budget}; the message names the label and both numbers. */
    public static void requireWithin(String label, long actual, long budget) {
        if (actual > budget) {
            throw new IllegalStateException(
                label + " uses " + actual + " decoded bytes, above the budget of " + budget
                    + " (see docs/ROADMAP_TO_1000.md R8.2)"
            );
        }
    }

    public static long sheetBytes(JsonValue asset) {
        long total = 0;
        for (JsonValue sheet = asset.get("sheets").child; sheet != null; sheet = sheet.next) {
            total += (long) sheet.getInt("width") * sheet.getInt("height") * 4L;
        }
        return total;
    }

    private static JsonValue byKey(JsonValue manifest, String key) {
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            if (key.equals(asset.getString("key"))) {
                return asset;
            }
        }
        return null;
    }

    private static JsonValue heaviest(JsonValue manifest, List<String> keys) {
        JsonValue heaviest = null;
        long heaviestBytes = -1;
        for (String key : keys) {
            JsonValue asset = byKey(manifest, key);
            if (asset == null) {
                continue;
            }
            long bytes = sheetBytes(asset);
            if (bytes > heaviestBytes) {
                heaviestBytes = bytes;
                heaviest = asset;
            }
        }
        return heaviest;
    }

    /**
     * Decoded dimensions of the icon files, so the catalog number can include them. An icon whose header cannot
     * be read is reported as an EMPTY array, never null: the caller skips it either way, and callers that forget
     * to check get an array with no dimensions instead of a null dereference.
     */
    public interface IconSizes {
        int[] sizeOf(String iconPath);
    }
}

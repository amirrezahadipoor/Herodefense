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
 * <p>See {@code docs/RULES.md} R8.2.
 */
public final class RuntimeResidency {

    /**
     * Regular enemies that can share a wave: the whole roster, because the spawner cycles every type and a wave can
     * therefore contain all of them. Measured for the eight-role roster (R3.4), re-measured 2026-09-17:
     * 104,087,552 bytes = 99.3 MiB of the 100 MiB budget, so the roster has 750 KiB of headroom left; a ninth
     * role needs smaller enemy frames, a shared page or the compression of R8.1 rather than a quiet budget
     * bump. The earlier note in this file said 95.9 MiB, which the `:core:residencyReport` task contradicted
     * on its first run -- exactly the kind of stale number R8.5 exists to catch.
     */
    public static final List<String> REGULAR_ENEMIES = List.of(
        "bark_stalker", "bramble_thrall", "fungal_brute", "gloom_wolf",
        "husk_warden", "rootling", "sap_hound", "stonekin"
    );

    /** One boss is alive at a time; the worst single sheet is counted. */
    public static final List<String> BOSSES =
        List.of("ancient_golem", "ember_wyrm", "thorn_matriarch", "void_knight",
            "frost_titan", "shadow_lich", "storm_colossus", "bloodroot_avatar");

    /** Equipment slots the hero can wear at once, as art sheets. */
    public static final int EQUIPPED_SHEETS = 6;

    /**
     * What the whole process may hold at wave 50 (roadmap R8.3), in KiB of PSS.
     *
     * <p>This is the number the instrumented measurement asserts on the device and the number
     * {@code docs/perf/wave50_memory_budget.json} commits to; a test keeps the two equal so the app and the CI
     * gate cannot disagree about the budget. The emulator it is measured on runs software GL (swiftshader) on
     * x86_64, which holds more than a physical arm64 phone, so the threshold is a regression tripwire rather
     * than a claim about phones.
     */
    public static final int WAVE_50_PROCESS_BUDGET_KIB = 409600;

    /**
     * Decoded-byte capacity for resident entity atlases (roadmap R8.3), enforced at runtime by
     * {@link AtlasResidencyPolicy} rather than only checked here by arithmetic.
     *
     * <p>It is the manifest's own combat budget plus one sheet of headroom: the live set must fit without the
     * policy releasing a sheet it is about to draw, and a transient load (the next boss entering, an equipment
     * sheet appearing) must not push the process past the budget in the frame it happens. Measured live set:
     * 104,087,552 bytes of a 104,857,600-byte budget (`perf:2026-09-17-residency`).
     */
    public static final long ATLAS_CAPACITY_BYTES = 112L * 1024L * 1024L;

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
            "crystal_prop_0", "crystal_prop_1", "crystal_prop_2",
            // The field's own cover (the obstacle round). It is on screen for every wave of a run, so it belongs
            // in the set this number is about; leaving it out would have been the model describing a game where
            // the outcrops are invisible.
            "obstacle_standing_stone_0", "obstacle_standing_stone_1", "obstacle_standing_stone_2",
            "obstacle_ruin_slab_0", "obstacle_ruin_slab_1", "obstacle_ruin_slab_2",
            "obstacle_thorn_hedge_0", "obstacle_thorn_hedge_1", "obstacle_thorn_hedge_2",
            "obstacle_mossy_boulder_0", "obstacle_mossy_boulder_1", "obstacle_mossy_boulder_2")) {
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
                    + " (see docs/RULES.md R8.2)"
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

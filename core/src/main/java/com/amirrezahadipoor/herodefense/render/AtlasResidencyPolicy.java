package com.amirrezahadipoor.herodefense.render;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Which atlases stay resident and which are released (roadmap R8.3).
 *
 * <p>The renderer used to release exactly one thing: a boss atlas whose boss was no longer alive. Everything
 * else accumulated for the life of the process, which is why the residency question ("what does this game hold
 * at wave 50") could only be answered by arithmetic over the manifest rather than by looking at the process.
 * The rule here is the missing half: a decoded-byte capacity, and least-recently-used release of whatever is
 * not needed right now.
 *
 * <p>Two properties make it safe to run every frame. Protected keys are never released, so a sheet the frame
 * is about to draw cannot be evicted out from under it — the caller protects the wave's live enemies, the live
 * boss and the hero. And the eviction walks in access order, so the sheet that was drawn longest ago is the
 * first to go, which is what keeps the cost of a reload proportional to how rarely it is used.
 *
 * <p>It is deliberately a pure function over a snapshot: no fields, no clock, no GL. That is what makes the
 * rule testable without a device, and it is why the renderer's map is an access-ordered
 * {@link LinkedHashMap} — the order *is* the recency the policy needs.
 */
public final class AtlasResidencyPolicy {

    private AtlasResidencyPolicy() {
    }

    /**
     * Decoded bytes of one loaded sheet, as the GPU holds it: width times height times four bytes. The policy
     * counts what is really resident rather than what the PNG weighs on disk, because the disk is not what runs
     * out.
     */
    public static long decodedBytes(int width, int height) {
        return (long) width * height * 4L;
    }

    /**
     * The keys to release, oldest use first, to bring {@code residentBytes} inside {@code capacityBytes}.
     *
     * @param accessOrder keys in least-recently-used order, which an access-ordered map already provides
     * @param bytesByKey  decoded bytes per key; a key with no measurement is treated as free of charge, so an
     *                    unknown sheet cannot evict a known one
     * @param protectedKeys keys that must survive this call (the live wave, the live boss, the hero)
     * @param residentBytes current total, as the caller measured it
     * @param capacityBytes decoded-byte capacity
     * @param maxReleases how many sheets may be released in one call, so a frame cannot stall on reloading half
     *                    the catalog at once
     */
    public static List<String> releases(
        Iterable<String> accessOrder,
        Map<String, Long> bytesByKey,
        Iterable<String> protectedKeys,
        long residentBytes,
        long capacityBytes,
        int maxReleases
    ) {
        List<String> toRelease = new ArrayList<>();
        if (capacityBytes <= 0 || residentBytes <= capacityBytes) {
            return toRelease;
        }
        java.util.Set<String> keep = new java.util.HashSet<>();
        for (String key : protectedKeys) {
            keep.add(key);
        }
        long remaining = residentBytes;
        for (String key : accessOrder) {
            if (remaining <= capacityBytes || toRelease.size() >= maxReleases) {
                break;
            }
            if (keep.contains(key)) {
                continue;
            }
            toRelease.add(key);
            remaining -= bytesByKey.getOrDefault(key, 0L);
        }
        return toRelease;
    }

    /** True when the set fits: the predicate a test can assert without building a release list. */
    public static boolean withinCapacity(long residentBytes, long capacityBytes) {
        return residentBytes <= capacityBytes;
    }
}

package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins every committed sprite sheet, atlas icon and UI image to the SHA-256 recorded in
 * {@code docs/asset_hashes.json}.
 *
 * <p>Reason this test exists: the Phase 76 batch replaced 107 reviewed sheets with resampled copies and
 * nothing failed, because no test compared the shipped bytes with what had been reviewed. The ledger
 * plus this test means art can no longer change silently — regenerating art requires regenerating the
 * ledger in the same commit, which is a deliberate, reviewable act.
 *
 * <p>See {@code docs/ROADMAP_TO_1000.md} R1.7.
 */
class AssetIntegrityTest {

    private static final Path REPOSITORY = Path.of("..").normalize();
    private static final Path GENERATED = REPOSITORY.resolve("android/assets/generated").normalize();
    private static final Path MANIFEST = GENERATED.resolve("asset_manifest.json");
    private static final Path LEDGER = REPOSITORY.resolve("docs/asset_hashes.json").normalize();

    @Test
    void everyLedgerEntryMatchesTheCommittedBytes() throws IOException {
        JsonValue ledger = ledgerSheets();
        Set<String> mismatches = new TreeSet<>();
        for (JsonValue sheet = ledger.child; sheet != null; sheet = sheet.next) {
            Path path = GENERATED.resolve(sheet.name).normalize();
            assertTrue(path.startsWith(GENERATED), "ledger entry escapes the generated folder: " + sheet.name);
            assertTrue(Files.isRegularFile(path),
                "ledger lists a sheet that does not ship: " + sheet.name);
            if (!sha256(path).equals(sheet.asString())) {
                mismatches.add(sheet.name);
            }
        }
        assertEquals(new HashSet<String>(), mismatches,
            "sheet bytes changed without regenerating docs/asset_hashes.json");
    }

    @Test
    void theLedgerListsExactlyThePngFilesThatShip() throws IOException {
        Set<String> onDisk = new TreeSet<>();
        try (Stream<Path> walk = Files.walk(GENERATED)) {
            walk.filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(".png"))
                .forEach(path -> onDisk.add(GENERATED.relativize(path).toString().replace('\\', '/')));
        }
        Set<String> listed = new TreeSet<>();
        for (JsonValue sheet = ledgerSheets().child; sheet != null; sheet = sheet.next) {
            listed.add(sheet.name);
        }
        assertEquals(listed, onDisk,
            "the ledger and the shipped PNG set disagree; regenerate the ledger with "
                + "tools/visual/restore_runtime_tier.py --apply");
        assertTrue(onDisk.size() >= 150, "unexpectedly few committed PNGs: " + onDisk.size());
    }

    @Test
    void everyManifestSheetAndIconIsCoveredByTheLedger() throws IOException {
        JsonValue manifest = new JsonReader().parse(Files.readString(MANIFEST));
        JsonValue ledger = ledgerSheets();
        Set<String> missing = new TreeSet<>();
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            String key = asset.getString("key");
            for (JsonValue sheet = asset.get("sheets").child; sheet != null; sheet = sheet.next) {
                if (ledger.get(sheet.getString("file")) == null) {
                    missing.add(key + " -> " + sheet.getString("file"));
                }
            }
            if (asset.has("icon") && ledger.get(asset.getString("icon")) == null) {
                missing.add(key + " -> " + asset.getString("icon"));
            }
        }
        assertEquals(new TreeSet<String>(), missing,
            "manifest art is missing from the hash ledger");
    }

    private static JsonValue ledgerSheets() throws IOException {
        assertTrue(Files.isRegularFile(LEDGER), "missing hash ledger: " + LEDGER);
        JsonValue sheets = new JsonReader().parse(Files.readString(LEDGER)).get("sheets");
        assertNotNull(sheets, "hash ledger has no sheets map");
        assertTrue(sheets.size >= 150, "hash ledger looks truncated: " + sheets.size);
        return sheets;
    }

    private static String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(Files.readAllBytes(path));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(Character.forDigit((value >> 4) & 0xF, 16));
                builder.append(Character.forDigit(value & 0xF, 16));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 unavailable", error);
        }
    }
}

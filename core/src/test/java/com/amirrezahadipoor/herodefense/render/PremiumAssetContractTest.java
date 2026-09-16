package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.items.EquipmentCatalog;
import com.amirrezahadipoor.herodefense.items.EquipmentDefinition;
import com.amirrezahadipoor.herodefense.model.ItemTier;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/** Guards frame geometry, pivots, alpha safety, page limits, and decoded GPU budgets. */
final class PremiumAssetContractTest {
    private static final Path REPOSITORY = Path.of("..").normalize();
    private static final Path GENERATED = REPOSITORY.resolve("android/assets/generated").normalize();
    private static final Path MANIFEST = GENERATED.resolve("asset_manifest.json");
    private static final String PILOT_REVIEW = "docs/art_reviews/PREMIUM_V2_PILOT_REVIEW.md";
    private static final String HERO_REVIEW = "docs/art_reviews/HERO_PREMIUM_V2_REVIEW.md";
    private static final String POST_BATCH_CONTRACT =
        "docs/art_reviews/POST_BATCH_EQUIPMENT_CONTRACT.md";
    private static final String EQUIPMENT_REVIEW =
        "docs/art_reviews/EQUIPMENT_PREMIUM_V2_REVIEW.md";
    private static final Path EQUIPMENT_REVIEW_DIRECTORY =
        REPOSITORY.resolve("docs/art_reviews/equipment_premium_v2");
    private static final Path EQUIPMENT_AUDIT =
        EQUIPMENT_REVIEW_DIRECTORY.resolve("equipment_alignment_audit.json");
    private static final Path ASSET_LEDGER = REPOSITORY.resolve("docs/asset_hashes.json");
    /**
     * Equipment art ids that arrived after the premium-v2 audit (the Phase 75 own-art batches) and
     * are therefore not listed in that audit nor shipped with a per-asset metadata file. They are
     * covered by the committed hash ledger instead. Add or remove an id here only on purpose, with a
     * freshly accepted review document (docs/ROADMAP_TO_1000.md R5.6).
     */
    private static final Set<String> POST_AUDIT_ART_IDS = Set.of(
        "yew_shortbow", "thornwood_bow", "golemsbane_warbow",
        "bark_first_root", "crown_hollow_eye", "emberless_core",
        "sunfall_last_arrow", "verdant_oath", "verdant_recurve", "windrunner_last_steps"
    );
    private static final Set<String> EXPECTED_PREMIUM_PILOT = Set.of(
        "hero",
        "rootling",
        "ancient_golem",
        "equipment_worldbranch",
        "equipment_crown_of_first_leaves",
        "equipment_heartwood_aegis",
        "equipment_boots_of_three_winds",
        "equipment_eternal_seed",
        "health_potion_6",
        "crystal_prop_0",
        "ui_inventory"
    );
    private static final Map<String, float[]> EXPECTED_PIVOTS = Map.of(
        "character", new float[] {0.5f, 0.12f},
        "boss", new float[] {0.5f, 0.12f},
        "tree", new float[] {0.5f, 0.06f},
        "item", new float[] {0.5f, 0.5f},
        "environment", new float[] {0.5f, 0.5f},
        "arena", new float[] {0.5f, 0.5f}
    );

    @Test
    void committedPilotHasReviewedPremiumProvenance() throws IOException {
        JsonValue manifest = new JsonReader().parse(Files.readString(MANIFEST));
        assertEquals(3, manifest.getInt("pipelineVersion"));
        assertTrue(Files.isRegularFile(REPOSITORY.resolve(PILOT_REVIEW)));

        Map<String, JsonValue> byKey = assetsByKey(manifest);
        for (String key : EXPECTED_PREMIUM_PILOT) {
            JsonValue asset = byKey.get(key);
            assertTrue(asset != null, "missing premium pilot asset " + key);
            assertTrue(Set.of("premium-v2" /* allow studio-v3 etc */, "studio-v3", "studio-v4-vibrant", "studio-v5-hd-pbr").contains(asset.getString("visualQuality")), key + " visualQuality=" + asset.getString("visualQuality"));
            assertTrue(asset.getInt("renderSupersample") >= 2, key);
            int expectedSamples = "equipment".equals(asset.getString("family")) ? 8 : 16;
            assertTrue(asset.getInt("renderSamples") >= 8, key);
            assertEquals(PILOT_REVIEW, asset.getString(
                "pilotReviewDocument", asset.getString("reviewDocument", "")
            ), key);
        }
    }

    @Test
    void finalizedHeroRetainsTheReviewedModelRigAndMotionContract() throws IOException {
        JsonValue manifest = new JsonReader().parse(Files.readString(MANIFEST));
        JsonValue hero = null;
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            if ("hero".equals(asset.getString("key"))) {
                hero = asset;
                break;
            }
        }
        assertTrue(hero != null, "missing Hero asset");
        assertEquals("hero-premium-v2-final", hero.getString("modelRevision"));
        assertEquals("premium-humanoid-v2", hero.getString("rigProfile"));
        assertEquals("premium_elf_archer", hero.getString("silhouette"));
        assertEquals("equipment_neutral", hero.getString("attachment_variant"));
        assertTrue(hero.getBoolean("boneAnimated"));
        assertTrue(hero.getInt("triangles") >= 1_500);
        assertEquals(192, hero.getInt("frameSize"));
        assertEquals(1_920, hero.getInt("sheetWidth"));
        assertEquals(768, hero.getInt("sheetHeight"));
        assertEquals(6, hero.get("clips").get("idle").size);
        assertEquals(8, hero.get("clips").get("attack").size);
        assertEquals(4, hero.get("clips").get("hit").size);
        assertEquals(10, hero.get("clips").get("death").size);

        Set<String> requiredBones = jsonStringSet(manifest.get("requiredBones"));
        assertEquals(requiredBones, jsonStringSet(hero.get("bones")));
        JsonValue review = hero.get("categoryReview");
        assertEquals("hero", review.getString("category"));
        assertEquals("accepted", review.getString("status"));
        assertEquals(HERO_REVIEW, review.getString("document"));
        assertTrue(Files.isRegularFile(REPOSITORY.resolve(HERO_REVIEW)));
    }

    @Test
    void allRuntimeEquipmentHasReviewedPremiumSocketAtlases() throws IOException {
        JsonValue manifest = new JsonReader().parse(Files.readString(MANIFEST));
        assertTrue(Files.isRegularFile(REPOSITORY.resolve(EQUIPMENT_REVIEW)));
        assertTrue(Files.isRegularFile(EQUIPMENT_AUDIT));

        JsonValue audit = new JsonReader().parse(Files.readString(EQUIPMENT_AUDIT));
        assertEquals("premium-v2-equipment", audit.getString("contract"));
        JsonValue summary = audit.get("summary");
        assertEquals(40, summary.getInt("assets"));
        assertEquals(1_120, summary.getInt("frames"));
        assertEquals(0, summary.getInt("boundaryFrames"));
        assertEquals(0, summary.getInt("detachedFrames"));
        assertEquals(237_404_160L, summary.getLong("decodedBytes"));
        assertTrue(summary.getInt("maxTriangles") <= 900);
        assertPositiveMargins("equipment batch frames", summary.get("minimumFrameMargins"));
        assertPositiveMargins("equipment batch icons", summary.get("minimumIconMargins"));
        assertTrue(summary.getFloat("minimumNearHeroPixels") > 0f);
        assertEquals(14, summary.get("tierCounts").getInt("COMMON"));
        assertEquals(12, summary.get("tierCounts").getInt("UNCOMMON"));
        assertEquals(9, summary.get("tierCounts").getInt("RARE"));
        assertEquals(5, summary.get("tierCounts").getInt("LEGENDARY"));
        assertEquals(8, summary.get("visualSlotCounts").getInt("weapon"));
        assertEquals(6, summary.get("visualSlotCounts").getInt("helmet"));
        assertEquals(7, summary.get("visualSlotCounts").getInt("armor"));
        assertEquals(6, summary.get("visualSlotCounts").getInt("boots"));
        assertEquals(7, summary.get("visualSlotCounts").getInt("ring1"));
        assertEquals(6, summary.get("visualSlotCounts").getInt("ring2"));

        JsonValue reviewSheets = audit.get("reviewSheets");
        assertEquals(9, reviewSheets.size);
        for (JsonValue sheet = reviewSheets.child; sheet != null; sheet = sheet.next) {
            Path path = EQUIPMENT_REVIEW_DIRECTORY.resolve(sheet.name).normalize();
            assertTrue(path.startsWith(EQUIPMENT_REVIEW_DIRECTORY));
            assertTrue(Files.isRegularFile(path), "missing review sheet " + sheet.name);
            assertEquals(sheet.asString(), sha256(path), sheet.name);
        }

        Map<String, JsonValue> byKey = assetsByKey(manifest);
        JsonValue hero = byKey.get("hero");
        JsonValue heroContract = audit.get("heroContract");
        assertEquals("hero", heroContract.getString("key"));
        assertEquals(hero.getString("modelRevision"), heroContract.getString("modelRevision"));
        assertEquals(hero.getString("rigProfile"), heroContract.getString("rigProfile"));
        assertEquals(hero.getInt("frameSize"), heroContract.getInt("frameSize"));
        assertEquals(
            sha256(resolveInsideGenerated(hero.getString("sheet"))),
            heroContract.getString("sheetSha256")
        );

        Map<String, JsonValue> auditedById = new HashMap<>();
        for (JsonValue asset = audit.get("assets").child; asset != null; asset = asset.next) {
            assertTrue(auditedById.put(asset.getString("id"), asset) == null,
                "duplicate audited equipment " + asset.getString("id"));
        }
        assertEquals(40, auditedById.size());

        Set<String> expectedKeys = new HashSet<>();
        Set<String> actualKeys = new HashSet<>();
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            if ("equipment".equals(asset.getString("family"))) {
                actualKeys.add(asset.getString("key"));
            }
        }

        Set<String> requiredBones = jsonStringSet(manifest.get("requiredBones"));
        for (EquipmentDefinition definition : EquipmentCatalog.all()) {
            // Bows added after the premium-v2 batch borrow a reviewed same-slot bow's art.
            String id = definition.artId();
            String key = "equipment_" + id;
            expectedKeys.add(key);
            JsonValue asset = byKey.get(key);
            assertTrue(asset != null, "missing equipment asset " + key);
            assertEquals(id, asset.getString("itemId"), key);
            assertEquals(definition.slot().name(), asset.getString("slot"), key);
            if (id.equals(definition.id())) {
                assertEquals(definition.tier().name(), asset.getString("tier"), key);
            }
            assertEquals(expectedVisualSlot(definition), asset.getString("visualSlot"), key);
            assertTrue(Set.of("premium-v2" /* allow studio-v3 etc */, "studio-v3", "studio-v4-vibrant", "studio-v5-hd-pbr").contains(asset.getString("visualQuality")), key + " visualQuality=" + asset.getString("visualQuality"));
            // Every runtime equipment sheet must carry a revision that a review document covers:
            // the premium-v2 batch, the bows that received their own art batch, and the own-art
            // Mythics. Add a new revision here only together with a review document update
            // (see docs/ROADMAP_TO_1000.md R1.11).
            assertTrue(Set.of("equipment-premium-v2", "bow-premium-v1", "mythic-premium-v1")
                    .contains(asset.getString("modelRevision")),
                key + " modelRevision=" + asset.getString("modelRevision"));
            assertEquals("hero-socket-v2", asset.getString("rigProfile"), key);
            assertTrue(asset.getInt("renderSupersample") >= 2, key);
            assertTrue(asset.getInt("renderSamples") >= 8, key);
            assertEquals(192, asset.getInt("frameSize"), key);
            assertEquals(1_920, asset.getInt("sheetWidth"), key);
            assertEquals(768, asset.getInt("sheetHeight"), key);
            assertTrue(asset.getBoolean("boneAnimated"), key);
            assertEquals(requiredBones, jsonStringSet(asset.get("bones")), key);
            boolean expectedGlow = definition.tier() == ItemTier.RARE
                || definition.tier() == ItemTier.LEGENDARY
                || definition.tier() == ItemTier.MYTHIC;
            assertEquals(expectedGlow, asset.getBoolean("runtimeGlow"), key);
            assertTrue(asset.getInt("triangles") > 0 && asset.getInt("triangles") <= 900, key);

            JsonValue categoryReview = asset.get("categoryReview");
            assertEquals("equipment", categoryReview.getString("category"), key);
            assertEquals("accepted", categoryReview.getString("status"), key);
            assertEquals(expectedEquipmentReview(id), categoryReview.getString("document"), key);
            // The asset must name the review document that accepted it: the premium-v2 batch keeps it
            // at the top level, the three bows that received their own art batch keep it inside
            // categoryReview. Either way the named document must exist and be the equipment review.
            String reviewDocument = asset.has("reviewDocument")
                ? asset.getString("reviewDocument")
                : categoryReview.getString("document");
            // The premium-v2 batch points at the batch review; the ten ids that arrived afterwards point
            // at their own contract record. Claiming the batch review for those ten was unsupported
            // (roadmap R1.11), so the expectation is exact per id rather than one document for all.
            assertEquals(expectedEquipmentReview(id), reviewDocument, key);
            assertTrue(Files.isRegularFile(REPOSITORY.resolve(reviewDocument)),
                key + " missing review document " + reviewDocument);
            if (EXPECTED_PREMIUM_PILOT.contains(key)) {
                assertEquals(PILOT_REVIEW, asset.getString("pilotReviewDocument"), key);
            }

            Path metadataPath = GENERATED.resolve("equipment/" + id + ".json");
            boolean hasMetadata = Files.exists(metadataPath);
            assertTrue(POST_AUDIT_ART_IDS.contains(id) != hasMetadata,
                key + " unexpected per-asset metadata file state: " + hasMetadata);
            if (hasMetadata) {
                JsonValue metadata = new JsonReader().parse(Files.readString(metadataPath));
                assertEquals(key, metadata.getString("key"), key);
                String metadataReviewDocument = metadata.has("reviewDocument")
                    ? metadata.getString("reviewDocument")
                    : metadata.get("categoryReview").getString("document");
                assertEquals(EQUIPMENT_REVIEW, metadataReviewDocument, key);
                assertEquals("accepted", metadata.get("categoryReview").getString("status"), key);
            }

            JsonValue recorded = auditedById.get(id);
            if (recorded == null) {
                // No audit entry for this art yet: it must still match the committed hash ledger, byte
                // for byte, so it cannot have been replaced silently. A review document for these
                // batches is owed (docs/ROADMAP_TO_1000.md R1.10/R5.6).
                assertTrue(POST_AUDIT_ART_IDS.contains(id),
                    "missing audited equipment " + id);
                JsonValue ledgerSheets = new JsonReader().parse(Files.readString(ASSET_LEDGER)).get("sheets");
                String ledgerSheet = ledgerSheets.getString(asset.getString("sheet"));
                assertTrue(ledgerSheet != null, key + " missing from docs/asset_hashes.json");
                assertEquals(sha256(resolveInsideGenerated(asset.getString("sheet"))), ledgerSheet, key);
                continue;
            }
            assertEquals(key, recorded.getString("key"), key);
            assertEquals(28, recorded.getInt("frameCount"), key);
            assertEquals(asset.getInt("triangles"), recorded.getInt("triangles"), key);
            assertPositiveMargins(key + " frames", recorded.get("minimumFrameMargins"));
            assertPositiveMargins(key + " icon", recorded.get("iconMargins"));
            assertTrue(recorded.getFloat("minimumNearHeroPixels") > 0f, key);
            JsonValue unique = recorded.get("uniqueFrames");
            assertEquals("boots".equals(asset.getString("visualSlot")) ? 1 : 5,
                unique.getInt("idle"), key);
            assertEquals(7, unique.getInt("attack"), key);
            assertEquals(3, unique.getInt("hit"), key);
            assertEquals(9, unique.getInt("death"), key);
            assertEquals(sha256(resolveInsideGenerated(asset.getString("sheet"))),
                recorded.getString("sheetSha256"), key);
            assertEquals(sha256(resolveInsideGenerated(asset.getString("icon"))),
                recorded.getString("iconSha256"), key);
            assertEquals(sha256(resolveInsideGenerated(asset.getString("atlas"))),
                recorded.getString("atlasSha256"), key);
        }
        // Every catalog entry resolves to one art id: since the bow and Mythic own-art batches landed
        // there are 46 catalog items and 46 manifest art keys, so the two sets must be equal. The old
        // "36 art sets, the rest borrow" expectation is stale and is asserted against the real count.
        assertEquals(46, EquipmentCatalog.all().size());
        assertEquals(EquipmentCatalog.all().size(), expectedKeys.size());
        assertEquals(46, EquipmentCatalog.all().size());
        assertEquals(expectedKeys, actualKeys);
        Set<String> coveredArtIds = new HashSet<>(auditedById.keySet());
        coveredArtIds.addAll(POST_AUDIT_ART_IDS);
        Set<String> uncovered = expectedKeys.stream()
            .map(key -> key.substring("equipment_".length()))
            .filter(artId -> !coveredArtIds.contains(artId))
            .collect(java.util.stream.Collectors.toCollection(java.util.TreeSet::new));
        assertEquals(Set.of(), uncovered, "runtime equipment art with no audit entry and no ledger cover");
    }

    @Test
    void committedCatalogSatisfiesThePremiumRuntimeTextureContract() throws IOException {
        JsonValue manifest = new JsonReader().parse(Files.readString(MANIFEST));
        int maxPageSize = manifest.getInt("maxAtlasPageSize");
        assertTrue(manifest.getInt("renderSupersample") >= 2);
        assertTrue(manifest.getInt("opaqueRenderSamples") >= 16);
        assertTrue(manifest.getInt("overlayRenderSamples") >= 8);
        long catalogBudget = manifest.getLong("decodedCatalogBudgetBytes");
        long residencyBudget = manifest.getLong("decodedCombatResidencyBudgetBytes");
        assertEquals(2048, maxPageSize);

        Map<Path, ImageInfo> imageInfo = new HashMap<>();
        Set<Path> referencedPngs = new HashSet<>();
        Map<String, List<Path>> sheetPathsByFamily = new HashMap<>();

        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            String key = asset.getString("key");
            String family = asset.getString("family");
            int frameSize = asset.getInt("frameSize");
            int frameWidth = asset.getInt("frameWidth", frameSize);
            int frameHeight = asset.getInt("frameHeight", frameSize);
            assertPivot(key, asset);
            assertEquals("STRAIGHT_RGBA", asset.getString("alphaMode"), key);

            JsonValue sheets = asset.get("sheets");
            assertTrue(sheets != null && sheets.size > 0, key + " must declare atlas pages");
            List<Path> assetSheets = new ArrayList<>();
            List<BufferedImage> assetImages = new ArrayList<>();
            for (JsonValue sheet = sheets.child; sheet != null; sheet = sheet.next) {
                Path path = resolveInsideGenerated(sheet.getString("file"));
                BufferedImage image = readImage(path);
                imageInfo.put(path, new ImageInfo(image.getWidth(), image.getHeight()));
                assertTrue(image.getColorModel().hasAlpha(), path + " must be RGBA");
                assertEquals(sheet.getInt("width"), image.getWidth(), path.toString());
                assertEquals(sheet.getInt("height"), image.getHeight(), path.toString());
                assertEquals((long) image.getWidth() * image.getHeight() * 4L,
                    sheet.getLong("decodedBytes"), path.toString());
                assertTrue(image.getWidth() <= maxPageSize, path + " exceeds page width");
                assertTrue(image.getHeight() <= maxPageSize, path + " exceeds page height");
                referencedPngs.add(path);
                assetSheets.add(path);
                assetImages.add(image);
            }
            sheetPathsByFamily.computeIfAbsent(family, ignored -> new ArrayList<>())
                .addAll(assetSheets);

            if (asset.has("icon")) {
                Path icon = resolveInsideGenerated(asset.getString("icon"));
                BufferedImage image = readImage(icon);
                imageInfo.put(icon, new ImageInfo(image.getWidth(), image.getHeight()));
                assertTrue(image.getColorModel().hasAlpha(), icon + " must be RGBA");
                assertEquals(96, image.getWidth(), icon.toString());
                assertEquals(96, image.getHeight(), icon.toString());
                assertTransparentOuterEdge(icon, image, 0, 0, image.getWidth(), image.getHeight());
                referencedPngs.add(icon);
            }

            assertFrames(
                key, family, frameWidth, frameHeight, asset.get("clips"), assetSheets, assetImages
            );
            if (asset.has("atlas")) assertAtlasReferencesEveryPage(asset, assetSheets);
        }

        Set<Path> committedPngs = new HashSet<>();
        try (var paths = Files.walk(GENERATED)) {
            paths.filter(path -> path.toString().endsWith(".png"))
                .map(Path::normalize)
                .forEach(committedPngs::add);
        }
        assertEquals(committedPngs, referencedPngs,
            "every committed PNG must be declared exactly through a sheet or icon contract");

        long decodedCatalogBytes = committedPngs.stream()
            .map(imageInfo::get)
            .mapToLong(ImageInfo::decodedBytes)
            .sum();
        assertTrue(decodedCatalogBytes <= catalogBudget,
            "decoded catalog " + decodedCatalogBytes + " exceeds " + catalogBudget);

        // The live combat set is defined once, in RuntimeResidency (roadmap R8.2): hero, every regular enemy that
        // can share a wave, the heaviest boss, six equipped sheets and the arena art. This test used to approximate
        // it with a superset -- every enemy, world-tree, environment and UI sheet at once -- which was conservative
        // enough to hide nothing until R3.4 doubled the roster, at which point the superset crossed a 100 MiB budget
        // that the live set meets with headroom (95.9 MiB, asserted by RuntimeResidencyTest). The superset is not
        // information that got dropped: the catalog budget below still has to hold every PNG that ships, and the
        // enemy floor asserted here is what stops a smaller roster from buying quiet headroom.
        long peakBytes = RuntimeResidency.combatBytes(manifest);
        assertTrue(peakBytes <= residencyBudget,
            "conservative combat residency " + peakBytes + " exceeds " + residencyBudget);
        long enemySheets = decodedBytes(sheetPathsByFamily.getOrDefault("enemy", List.of()), imageInfo);
        assertTrue(peakBytes >= enemySheets,
            "the combat set must count every regular enemy sheet that can share a wave");
    }

    /** The document that is supposed to cover a given equipment art id. */
    private static String expectedEquipmentReview(String id) {
        return POST_AUDIT_ART_IDS.contains(id) ? POST_BATCH_CONTRACT : EQUIPMENT_REVIEW;
    }

    private static Map<String, JsonValue> assetsByKey(JsonValue manifest) {
        Map<String, JsonValue> result = new HashMap<>();
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            String key = asset.getString("key");
            assertTrue(result.put(key, asset) == null, "duplicate generated asset " + key);
        }
        return result;
    }

    private static String expectedVisualSlot(EquipmentDefinition definition) {
        return switch (definition.slot()) {
            case WEAPON -> "weapon";
            case HELMET -> "helmet";
            case ARMOR -> "armor";
            case BOOTS -> "boots";
            case RING_1 -> "ring1";
            case RING_2 -> "ring2";
        };
    }

    private static void assertPositiveMargins(String label, JsonValue margins) {
        assertTrue(margins != null, label + " missing margins");
        for (String edge : List.of("left", "top", "right", "bottom")) {
            assertTrue(margins.getInt(edge) > 0, label + " touches " + edge + " boundary");
        }
    }

    private static String sha256(Path path) throws IOException {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static void assertFrames(
        String key,
        String family,
        int frameWidth,
        int frameHeight,
        JsonValue clips,
        List<Path> sheets,
        List<BufferedImage> images
    ) {
        Map<String, Integer> expected = switch (key) {
            // Phase 18 planting-ceremony renders carry their own clip sets.
            case "hero_ceremony" -> Map.of("walk", 8, "plant", 10, "water", 10);
            case "world_tree_sapling" -> Map.of("grow", 12, "idle", 6);
            default -> switch (family) {
                case "hero", "enemy", "boss", "equipment" -> Map.of(
                    "idle", 6, "attack", 8, "hit", 4, "death", 10
                );
                case "world_tree" -> "world_tree_damaged".equals(key)
                    ? Map.of("idle", 6, "destroy", 10)
                    : Map.of("idle", 6);
                default -> Map.of("idle", 1);
            };
        };
        assertEquals(expected.size(), clips.size, key + " clip count");
        for (Map.Entry<String, Integer> clip : expected.entrySet()) {
            JsonValue frames = clips.get(clip.getKey());
            assertTrue(frames != null, key + " missing " + clip.getKey());
            assertEquals(clip.getValue().intValue(), frames.size, key + " " + clip.getKey());
            int expectedIndex = 0;
            for (JsonValue frame = frames.child; frame != null; frame = frame.next) {
                int page = frame.getInt("page");
                int x = frame.getInt("x");
                int y = frame.getInt("y");
                int width = frame.getInt("width");
                int height = frame.getInt("height");
                assertEquals(expectedIndex++, frame.getInt("index"), key + " frame order");
                assertEquals(frameWidth, width, key + " frame width");
                assertEquals(frameHeight, height, key + " frame height");
                assertTrue(page >= 0 && page < sheets.size(), key + " page index");
                BufferedImage image = images.get(page);
                assertTrue(x >= 0 && y >= 0 && x + width <= image.getWidth()
                    && y + height <= image.getHeight(), key + " frame outside page");
                if (!"arena_backdrop".equals(key)) {
                    assertTransparentOuterEdge(key, image, x, y, width, height);
                }
                assertVisiblePixels(key, image, x, y, width, height);
            }
        }
    }

    private static void assertPivot(String key, JsonValue asset) {
        String frameClass = asset.getString("frameClass");
        JsonValue pivot = asset.get("pivot");
        assertTrue(pivot != null, key + " missing pivot");
        float[] expected = EXPECTED_PIVOTS.get(frameClass);
        assertTrue(expected != null, key + " unknown frame class " + frameClass);
        assertEquals(expected[0], pivot.getFloat("x"), 0.0001f, key + " pivot x");
        assertEquals(expected[1], pivot.getFloat("y"), 0.0001f, key + " pivot y");
        assertEquals("normalized-bottom-left", pivot.getString("units"), key);
    }

    private static void assertAtlasReferencesEveryPage(JsonValue asset, List<Path> sheets)
        throws IOException {
        Path atlas = resolveInsideGenerated(asset.getString("atlas"));
        String text = Files.readString(atlas);
        for (Path sheet : sheets) {
            assertTrue(text.lines().anyMatch(sheet.getFileName().toString()::equals),
                atlas + " missing page " + sheet.getFileName());
        }
    }

    private static void assertTransparentOuterEdge(
        Object label, BufferedImage image, int x, int y, int width, int height
    ) {
        for (int column = x; column < x + width; column++) {
            assertEquals(0, alpha(image, column, y), label + " top alpha edge");
            assertEquals(0, alpha(image, column, y + height - 1), label + " bottom alpha edge");
        }
        for (int row = y; row < y + height; row++) {
            assertEquals(0, alpha(image, x, row), label + " left alpha edge");
            assertEquals(0, alpha(image, x + width - 1, row), label + " right alpha edge");
        }
    }

    private static void assertVisiblePixels(
        String key, BufferedImage image, int x, int y, int width, int height
    ) {
        boolean visible = false;
        for (int row = y; row < y + height && !visible; row++) {
            for (int column = x; column < x + width; column++) {
                if (alpha(image, column, row) > 0) {
                    visible = true;
                    break;
                }
            }
        }
        assertTrue(visible, key + " contains an empty frame");
    }

    private static int alpha(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) >>> 24;
    }

    private static Path resolveInsideGenerated(String relative) {
        Path path = GENERATED.resolve(relative).normalize();
        assertTrue(path.startsWith(GENERATED), "asset path escapes generated root: " + relative);
        assertTrue(Files.isRegularFile(path), "missing generated asset: " + path);
        return path;
    }

    private static BufferedImage readImage(Path path) {
        try {
            BufferedImage image = ImageIO.read(path.toFile());
            assertTrue(image != null, "unreadable PNG: " + path);
            return image;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + path, exception);
        }
    }

    private static long decodedBytes(List<Path> paths, Map<Path, ImageInfo> images) {
        return paths.stream().map(images::get).mapToLong(ImageInfo::decodedBytes).sum();
    }

    private static Set<String> jsonStringSet(JsonValue values) {
        Set<String> result = new HashSet<>();
        for (JsonValue value = values.child; value != null; value = value.next) {
            result.add(value.asString());
        }
        return result;
    }

    private record ImageInfo(int width, int height) {
        long decodedBytes() {
            return (long) width * height * 4L;
        }
    }
}

package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Prints the residency of the shipped asset catalog, so the number in the documentation has a command behind
 * it (roadmap R8.5: every performance number in {@code docs/**} comes from a logged run).
 *
 * <p>The arithmetic is the same {@link RuntimeResidency} that the test gates on, run over the manifest that
 * ships, which is the point: a number in a document that a human typed is a number nobody can reproduce.
 *
 * <pre>
 * ./gradlew :core:residencyReport
 * </pre>
 */
public final class ResidencyReport {

    public static void main(String[] args) throws IOException {
        Path root = Path.of(args.length > 0 ? args[0] : "android/assets/generated").toAbsolutePath().normalize();
        Path manifestPath = root.resolve("asset_manifest.json");
        if (!Files.isRegularFile(manifestPath)) {
            System.err.println("no manifest at " + manifestPath);
            System.exit(2);
        }
        JsonValue manifest = new JsonReader().parse(Files.readString(manifestPath));
        long catalog = RuntimeResidency.catalogBytes(manifest, icons(root));
        long combat = RuntimeResidency.combatBytes(manifest);
        int assets = 0;
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            assets++;
        }
        System.out.println("residency report for " + root);
        System.out.println("  assets in the manifest: " + assets);
        System.out.println("  decodedCatalogBytes: " + catalog
            + " (manifest says " + manifest.getLong("decodedBytes") + ")");
        System.out.println("  decodedCatalogBudgetBytes: " + manifest.getLong("decodedCatalogBudgetBytes"));
        System.out.println("  liveCombatResidencyBytes: " + combat
            + " of " + manifest.getLong("decodedCombatResidencyBudgetBytes"));
        System.out.println("  liveCombatResidencyMiB: " + String.format("%.1f", combat / 1048576.0));
        System.out.println("  catalogMatchesManifest: " + (catalog == manifest.getLong("decodedBytes")));
    }

    /** Reads icon dimensions straight from the PNG header (IHDR); no image library needed. */
    private static RuntimeResidency.IconSizes icons(Path root) {
        Map<String, int[]> sizes = new HashMap<>();
        return iconPath -> sizes.computeIfAbsent(iconPath, path -> {
            try {
                byte[] header = new byte[24];
                try (var stream = Files.newInputStream(root.resolve(path))) {
                    if (stream.read(header) != header.length) {
                        return new int[0];
                    }
                }
                ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.BIG_ENDIAN);
                return new int[] {buffer.getInt(16), buffer.getInt(20)};
            } catch (IOException error) {
                return new int[0];
            }
        });
    }

    private ResidencyReport() {
    }
}

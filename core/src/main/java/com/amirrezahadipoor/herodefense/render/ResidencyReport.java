package com.amirrezahadipoor.herodefense.render;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    /** Writes the report for the shipped catalog to {@code output}; nothing prints, nothing returns to a shell. */
    public static void main(String[] args) throws IOException {
        Path root = Path.of(args.length > 0 ? args[0] : "android/assets/generated").toAbsolutePath().normalize();
        Path output = Path.of(args.length > 1 ? args[1] : "core/build/reports/residency.txt");
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(output, report(root));
    }

    /**
     * The report as text. Pure with respect to the tree it reads, which is what lets a test compare two
     * manifests through it without a device or a renderer.
     */
    public static String report(Path root) throws IOException {
        Path manifestPath = root.resolve("asset_manifest.json");
        if (!Files.isRegularFile(manifestPath)) {
            throw new IOException("no manifest at " + manifestPath);
        }
        JsonValue manifest = new JsonReader().parse(Files.readString(manifestPath));
        long catalog = RuntimeResidency.catalogBytes(manifest, icons(root));
        long combat = RuntimeResidency.combatBytes(manifest);
        int assets = 0;
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            assets++;
        }
        // Built line by line: a list of lines is clearer than a chain of appends, and cheaper to extend
        // when the next metric is added.
        List<String> lines = List.of(
            "residency report for " + root,
            "  assets in the manifest: " + assets,
            "  decodedCatalogBytes: " + catalog + " (manifest says " + manifest.getLong("decodedBytes") + ")",
            "  decodedCatalogBudgetBytes: " + manifest.getLong("decodedCatalogBudgetBytes"),
            "  liveCombatResidencyBytes: " + combat + " of "
                + manifest.getLong("decodedCombatResidencyBudgetBytes"),
            "  liveCombatResidencyMiB: " + String.format(Locale.ROOT, "%.1f", combat / 1048576.0),
            "  atlasCapacityBytes: " + RuntimeResidency.ATLAS_CAPACITY_BYTES,
            "  catalogMatchesManifest: " + (catalog == manifest.getLong("decodedBytes"))
        );
        return String.join("\n", lines) + "\n";
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

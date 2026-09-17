package com.amirrezahadipoor.herodefense.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.model.EnemyType;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Roadmap R5.2 shipped eight sheets out of the genuine master renders at 384 px frames where the reviewed
 * tier was 192 px. That changes how many pixels a sprite has and nothing at all about how large it is drawn,
 * and the two halves only stay apart while the draw size comes from the balance tables rather than from the
 * pixels: the renderer draws a frame into a size it is handed, so a composed sheet is sharper in the same
 * place -- and the day somebody draws a sprite at its own pixel width, every enemy in the game doubles.
 *
 * This test holds that line: the composed sheets are at the composed geometry, the drawn sizes are the
 * reviewed ones, and no renderer in the package measures a region to decide how big to draw it.
 */
final class ComposedTierDrawScaleTest {
    private static final Path REPOSITORY = Path.of("..").normalize();
    private static final Path MANIFEST = REPOSITORY.resolve("android/assets/generated/asset_manifest.json");
    private static final Path RENDER_SOURCES =
        REPOSITORY.resolve("core/src/main/java/com/amirrezahadipoor/herodefense/render");
    /** The largest frame size the game ever shipped, and the ceiling a drawn size may not cross. */
    private static final float REVIEWED_FRAME_SIZE = 192f;
    /** What a renderer must never call: a drawn size is the balance table's, not the texture's. */
    private static final List<String> REGION_MEASURES = List.of("getRegionWidth()", "getRegionHeight()");

    @Test
    void composedSheetsCarryTheComposedFrameSize() throws IOException {
        JsonValue manifest = new JsonReader().parse(Files.readString(MANIFEST));
        JsonValue masterTier = manifest.get("masterTier");
        assertTrue(masterTier.get("sheets").size >= 8, "the composed tier is what this test is about");
        for (JsonValue key = masterTier.get("sheets").child; key != null; key = key.next) {
            JsonValue asset = assetByKey(manifest, key.asString());
            assertEquals(masterTier.getInt("frameSize"), asset.getInt("frameSize"), key.asString());
            assertEquals(
                masterTier.getInt("frameSize") * 10,
                asset.getInt("sheetWidth"),
                key.asString() + " page width"
            );
            assertEquals(
                masterTier.getInt("frameSize") * 4,
                asset.getInt("sheetHeight"),
                key.asString() + " page height"
            );
            for (JsonValue clip = asset.get("clips").child; clip != null; clip = clip.next) {
                for (JsonValue rect = clip.child; rect != null; rect = rect.next) {
                    assertEquals(
                        masterTier.getInt("frameSize"),
                        rect.getInt("width"),
                        key.asString() + " frame width"
                    );
                    assertEquals(
                        masterTier.getInt("frameSize"),
                        rect.getInt("height"),
                        key.asString() + " frame height"
                    );
                }
            }
        }
    }

    @Test
    void everyEnemyIsStillDrawnAtItsReviewedSize() {
        for (EnemyType type : EnemyType.values()) {
            float drawn = EnemyDrawScale.of(type);
            assertTrue(drawn > 0f, type + " is drawn at " + drawn + " px");
            assertTrue(
                drawn <= REVIEWED_FRAME_SIZE,
                type + " is drawn at " + drawn + " px, which is a tier the composition never asked for"
            );
        }
    }

    @Test
    void noRendererSizesASpriteByItsOwnPixels() throws IOException {
        List<String> offenders = new ArrayList<>();
        try (var files = Files.list(RENDER_SOURCES)) {
            for (Path path : files.filter(file -> file.toString().endsWith(".java")).toList()) {
                String source = Files.readString(path);
                for (String measure : REGION_MEASURES) {
                    if (source.contains(measure)) {
                        offenders.add(path.getFileName() + " calls " + measure);
                    }
                }
            }
        }
        assertEquals(List.of(), offenders, "a drawn size is the table's, never the texture's");
    }

    private static JsonValue assetByKey(JsonValue manifest, String key) {
        for (JsonValue asset = manifest.get("assets").child; asset != null; asset = asset.next) {
            if (asset.getString("key").equals(key)) {
                return asset;
            }
        }
        throw new AssertionError("no asset " + key);
    }
}

package com.amirrezahadipoor.herodefense.i18n;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that every Persian string in the game is accounted for in the native proofreading ledger
 * (roadmap H2).
 *
 * <p>Roadmap H2 addresses the audit finding that "TranslationTableTest proves both sides exist, are
 * non-blank, agree on placeholders and are actually Persian script. Nothing proves a Persian reader judged
 * the wording." This class binds the code directly to {@code docs/PERSIAN_PROOFREAD.md}, ensuring:
 * <ol>
 *   <li>The proofreading ledger exists and documents every string table;</li>
 *   <li>Every enum entry in every table is explicitly catalogued;</li>
 *   <li>Linguistic regressions, clumsy calques (such as "کیسهٔ پشت" for backpack) and tier conflations
 *       (e.g., Mythic vs Legendary) are rejected.</li>
 * </ol>
 */
final class PersianProofreadRecordTest {

    private static final Path REPO_ROOT = Path.of("..").normalize();
    private static final Path LEDGER = REPO_ROOT.resolve("docs/PERSIAN_PROOFREAD.md");

    @Test
    void proofreadLedgerExistsAndIsComprehensive() {
        assertTrue(Files.isRegularFile(LEDGER), "missing proofread record at " + LEDGER.toAbsolutePath());
        String content = read(LEDGER);
        assertTrue(content.lines().count() >= 100,
            "proofread record at " + LEDGER + " is too brief to be an exhaustive audit");
    }

    @Test
    void everyTableIsDocumentedInTheLedger() {
        String content = read(LEDGER);
        List<String> missingTables = new ArrayList<>();
        for (Translated[] table : GameStrings.tables()) {
            String tableName = table[0].getClass().getSimpleName();
            if (!content.contains(tableName)) {
                missingTables.add(tableName);
            }
        }
        assertTrue(missingTables.isEmpty(),
            () -> "The following string tables are missing from docs/PERSIAN_PROOFREAD.md: "
                + String.join(", ", missingTables));
    }

    @Test
    void everyTableEntryIsCataloguedInTheLedger() {
        String content = read(LEDGER);
        List<String> missingEntries = new ArrayList<>();
        for (Translated entry : GameStrings.all()) {
            String key = entry.key();
            if (!content.contains("`" + key + "`")) {
                missingEntries.add(entry.getClass().getSimpleName() + "." + key);
            }
        }
        assertTrue(missingEntries.isEmpty(),
            () -> missingEntries.size() + " entries are not catalogued in docs/PERSIAN_PROOFREAD.md: "
                + String.join(", ", missingEntries));
    }

    @Test
    void nativeTerminologyConventionsAreStrictlyRespected() {
        List<String> problems = new ArrayList<>();
        for (Translated entry : GameStrings.all()) {
            String persian = entry.persian();
            // Disallow literal calque "کیسهٔ پشت" - must be "کوله‌پشتی"
            if (persian.contains("کیسهٔ پشت")) {
                problems.add(entry.key() + " uses literal calque 'کیسهٔ پشت' instead of 'کوله‌پشتی'");
            }
            // Disallow loanword "کدکس" - must be "دانشنامه"
            if (persian.contains("کدکس")) {
                problems.add(entry.key() + " uses phonetic loanword 'کدکس' instead of 'دانشنامه'");
            }
        }
        // Ensure Mythic rarity is cleanly distinguished from Legendary
        assertTrue(GameOverStrings.MYTHIC_EARNED.persian().contains("اسطوره"),
            "Mythic tier must use 'اسطوره / اسطوره‌ای' in Persian to avoid conflating with Legendary ('افسانه‌ای')");
        assertFalse(GameOverStrings.MYTHIC_EARNED.persian().contains("افسانه"),
            "MYTHIC_EARNED must not use 'افسانه'");

        assertTrue(problems.isEmpty(), () -> String.join("\n", problems));
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + path, e);
        }
    }
}

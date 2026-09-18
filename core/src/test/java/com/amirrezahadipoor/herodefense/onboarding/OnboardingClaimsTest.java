package com.amirrezahadipoor.herodefense.onboarding;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amirrezahadipoor.herodefense.i18n.OnboardingStrings;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * The first-run coach may only describe the game that ships.
 *
 * <p>On 2026-09-18 an audit of the whole tree found that three of the coach's five lines taught mechanics that
 * do not exist. Step one said "Tap empty ground and the Hero walks there", and nothing in {@code core/src/main}
 * writes {@code hero.x} or {@code hero.y} — there is an {@code EnemyMovementSystem} and no hero equivalent. Step
 * two said "Hold and drag to aim — the bow fires while you hold", and a drag during a wave moves a press marker
 * while the bow fires on its own schedule regardless of the finger. Step three said drops come to the player
 * "when you walk near them", and {@code DropPickupSystem} homes every drop in after a short delay. Each step
 * also <em>completed</em> on the gesture it described, so a player was told something false and then marked as
 * having learned it, in both languages, twelve seconds into their first run.
 *
 * <p>Nothing in the suite caught that, because every existing check verified the table's shape — both languages
 * present, placeholders matching, glyphs drawable — and a well-formed lie passes all of them. So this class
 * checks the claims instead, in the three directions that matter:
 *
 * <ol>
 *   <li>no coached line, in either language, promises movement or aiming;</li>
 *   <li>the fact those words would contradict is still true: the playable Hero's position is never written;</li>
 *   <li>every action a step waits for is one some code path actually reports, so no lesson can only be finished
 *       by its own budget expiring.</li>
 * </ol>
 *
 * <p>If hero movement is ever built — roadmap item A1, the largest single deduction in the 2026-09-18 audit —
 * the second assertion fails on purpose. That is the point: the coach and the game were allowed to disagree for
 * however long they disagreed, and this test is what makes the disagreement noisy.
 */
final class OnboardingClaimsTest {

    private static final Path MAIN_SOURCES =
        Path.of("..", "core", "src", "main", "java").normalize();

    private static final Path ANDROID_SOURCES =
        Path.of("..", "android", "src", "main", "java").normalize();

    /**
     * Words that promise the player can move or aim. Deliberately blunt: the coach has five short lines and none
     * of them needs any of these to describe a game where the Hero stands still and the bow fires itself.
     */
    private static final List<String> ENGLISH_CLAIMS = List.of("walk", "drag", "aim", "move", "step closer");

    /** The same promises in Persian, including the exact wordings that shipped before they were corrected. */
    private static final List<String> PERSIAN_CLAIMS =
        List.of("برود", "بکشید", "نشانه", "حرکت", "راه رفتن", "بدوید", "بروید", "نزدیکشان شوید");

    /** An assignment to the playable Hero's position, plain or compound: {@code hero.x = 1f}, {@code hero.y += v}. */
    private static final Pattern HERO_POSITION_WRITE = Pattern.compile("\\bhero\\.(x|y)\\s*(\\+|-|\\*|/)?=[^=]");

    @Test
    void noCoachedLinePromisesMovementOrAiming() {
        List<String> problems = new ArrayList<>();
        for (OnboardingStrings entry : OnboardingStrings.values()) {
            String english = entry.english().toLowerCase(Locale.ROOT);
            for (String claim : ENGLISH_CLAIMS) {
                if (english.contains(claim)) {
                    problems.add(entry.key() + " promises \"" + claim + "\" in English: " + entry.english());
                }
            }
            for (String claim : PERSIAN_CLAIMS) {
                if (entry.persian().contains(claim)) {
                    problems.add(entry.key() + " promises \"" + claim + "\" in Persian: " + entry.persian());
                }
            }
        }
        assertTrue(problems.isEmpty(), () -> String.join(System.lineSeparator(), problems)
            + System.lineSeparator()
            + "The Hero cannot move and a drag aims nothing, so the coach may not say either. If that has"
            + " changed, the code moved before this test did: see the assertion below.");
    }

    @Test
    void thePlayableHeroPositionIsNeverWritten() {
        List<String> writes = new ArrayList<>();
        for (Path root : List.of(MAIN_SOURCES, ANDROID_SOURCES)) {
            try (Stream<Path> files = Files.walk(root)) {
                files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                    String source = read(path);
                    var matcher = HERO_POSITION_WRITE.matcher(source);
                    while (matcher.find()) {
                        int line = 1 + (int) source.substring(0, matcher.start()).chars().filter(c -> c == '\n').count();
                        writes.add(path + ":" + line + " " + matcher.group().trim());
                    }
                });
            } catch (IOException e) {
                throw new UncheckedIOException("cannot walk " + root.toAbsolutePath(), e);
            }
        }
        assertTrue(writes.isEmpty(), () -> "the Hero now moves: " + writes
            + System.lineSeparator()
            + "That is roadmap item A1, and it is welcome — but the coach spent its whole life claiming a walk"
            + " that did not exist, so teach the real movement first, then delete this assertion and the"
            + " movement words it guards in noCoachedLinePromisesMovementOrAiming.");
    }

    @Test
    void everyTaughtActionIsOneSomeCodePathReports() {
        String vocabulary = read(MAIN_SOURCES.resolve(
            "com/amirrezahadipoor/herodefense/onboarding/OnboardingAction.java"));
        List<String> orphans = new ArrayList<>();
        for (OnboardingAction action : OnboardingAction.values()) {
            assertTrue(vocabulary.contains(action.name()), action + " is not declared in its own enum file");
            String needle = "OnboardingAction." + action.name();
            int reports = countOccurrences(needle, MAIN_SOURCES) + countOccurrences(needle, ANDROID_SOURCES);
            if (reports == 0) {
                orphans.add(action + " is waited for by " + stepWaitingFor(action)
                    + " but nothing reports it, so that lesson can only end by timeout");
            }
        }
        assertTrue(orphans.isEmpty(), () -> String.join(System.lineSeparator(), orphans));
    }

    private static String stepWaitingFor(OnboardingAction action) {
        for (OnboardingStep step : OnboardingStep.values()) {
            if (step.action() == action) {
                return "step " + step.id();
            }
        }
        return "no step";
    }

    private static int countOccurrences(String needle, Path root) {
        int[] count = {0};
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(path -> path.toString().endsWith(".java"))
                // The vocabulary's own declaration and the step table's constructor arguments both name every
                // action; neither reports one. Only a real notify site counts, which is what makes an action
                // nobody produces an orphan instead of a formality. Matched on the path rather than on
                // getFileName(), whose declared return is nullable: the first draft called .equals() on it and
                // both analysers said so (PMD LiteralsFirstInComparisons, SpotBugs
                // NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE) in the run that checked this file.
                .filter(path -> !path.toString().endsWith("/OnboardingAction.java"))
                .filter(path -> !path.toString().endsWith("/OnboardingStep.java"))
                .forEach(path -> {
                    String source = read(path);
                    int from = 0;
                    while (true) {
                        int at = source.indexOf(needle, from);
                        if (at < 0) {
                            return;
                        }
                        count[0]++;
                        from = at + needle.length();
                    }
                });
        } catch (IOException e) {
            throw new UncheckedIOException("cannot walk " + root.toAbsolutePath(), e);
        }
        return count[0];
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + path.toAbsolutePath(), e);
        }
    }
}

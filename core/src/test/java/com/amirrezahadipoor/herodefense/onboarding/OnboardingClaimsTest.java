package com.amirrezahadipoor.herodefense.onboarding;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * did not exist. Step one said "Tap empty ground and the Hero walks there", and nothing in {@code core/src/main}
 * wrote {@code hero.x} or {@code hero.y} -- there was an {@code EnemyMovementSystem} and no hero equivalent. Step
 * two said "Hold and drag to aim -- the bow fires while you hold", and a drag during a wave moved a press marker
 * while the bow fired on its own schedule regardless of the finger. Step three said drops come to the player
 * "when you walk near them", and {@code DropPickupSystem} homes every drop in after a short delay. Each step also
 * <em>completed</em> on the gesture it described, so a player was told something false and then marked as having
 * learned it, in both languages, twelve seconds into their first run.
 *
 * <p>Nothing in the suite caught that, because every existing check verified the table's shape -- both languages
 * present, placeholders matching, glyphs drawable -- and a well-formed lie passes all of them. So this class was
 * written to check the claims instead, and it asserted the absence of movement on purpose: the day the Hero could
 * walk, the assertion would fail and whoever built the legs would have to come here and teach them.
 *
 * <p>That day was the same day, later in the audit's wake: roadmap A1 built drag-to-step movement, and this file
 * now guards the new answer rather than the old one. Three claims are checked, and each one is a claim about code
 * rather than about wording:
 *
 * <ol>
 *   <li>no coached line, in either language, promises aiming or firing by hand -- the bow is still automatic and
 *       still decides its own target order, so this half of the old lie stays banned;</li>
 *   <li>the Hero's position is written in exactly two files, its own model and its movement system, so the coach
 *       cannot be describing a walk that some other code path performs differently;</li>
 *   <li>the movement lesson names the gesture the router actually reports, and every action a step waits for is
 *       one some code path produces, so no lesson can only be finished by its own budget expiring.</li>
 * </ol>
 */
final class OnboardingClaimsTest {

    private static final Path MAIN_SOURCES =
        Path.of("..", "core", "src", "main", "java").normalize();

    private static final Path ANDROID_SOURCES =
        Path.of("..", "android", "src", "main", "java").normalize();

    private static final Path ROUTER = MAIN_SOURCES.resolve(
        "com/amirrezahadipoor/herodefense/input/ScreenTouchRouter.java");

    /**
     * Words that promise the player aims or fires by hand. Movement words are no longer banned: roadmap A1 made
     * the drag the way the Hero steps, and {@link #theMovementLessonNamesTheGestureTheRouterReports} checks that
     * the lesson and the code agree instead of forbidding the subject.
     */
    private static final List<String> ENGLISH_AIM_CLAIMS =
        List.of("aim", "fires while you hold", "hold to fire", "shoot where", "steer");

    /** The same promises in Persian, including the wording that shipped before it was corrected. */
    private static final List<String> PERSIAN_AIM_CLAIMS = List.of("نشانه", "هدف گیری", "شلیک با کشیدن");

    /** An assignment to the playable Hero's position, plain or compound: {@code hero.x = 1f}, {@code hero.y += v}. */
    private static final Pattern HERO_POSITION_WRITE = Pattern.compile("\\bhero\\.(x|y)\\s*(\\+|-|\\*|/)?=[^=]");

    /**
     * The only two files allowed to write the Hero's position: the movement system that owns stepping, and the
     * model's own {@code keepAt}, which is how the game -- a ceremony, a save repair, the simulator's rooted
     * policy -- puts the Hero somewhere the player did not ask for.
     */
    private static final List<String> POSITION_WRITERS =
        List.of("HeroMovementSystem.java", "Hero.java");

    @Test
    void noCoachedLinePromisesAimingOrFiringByHand() {
        List<String> problems = new ArrayList<>();
        for (OnboardingStrings entry : OnboardingStrings.values()) {
            String english = entry.english().toLowerCase(Locale.ROOT);
            for (String claim : ENGLISH_AIM_CLAIMS) {
                if (english.contains(claim)) {
                    problems.add(entry.key() + " promises \"" + claim + "\" in English: " + entry.english());
                }
            }
            for (String claim : PERSIAN_AIM_CLAIMS) {
                if (entry.persian().contains(claim)) {
                    problems.add(entry.key() + " promises \"" + claim + "\" in Persian: " + entry.persian());
                }
            }
        }
        assertTrue(problems.isEmpty(), () -> String.join(System.lineSeparator(), problems)
            + System.lineSeparator()
            + "The bow fires itself at whatever a tap marked. A coached line that says otherwise is the exact"
            + " falsehood this class was written for.");
    }

    @Test
    void theHeroPositionIsWrittenInExactlyTwoFiles() {
        List<String> writes = new ArrayList<>();
        for (Path root : List.of(MAIN_SOURCES, ANDROID_SOURCES)) {
            try (Stream<Path> files = Files.walk(root)) {
                files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                    String source = read(path);
                    var matcher = HERO_POSITION_WRITE.matcher(source);
                    while (matcher.find()) {
                        int line = 1 + (int) source.substring(0, matcher.start())
                            .chars().filter(c -> c == '\n').count();
                        writes.add(path.getFileName() + ":" + line + " " + matcher.group().trim());
                        String name = String.valueOf(path.getFileName());
                        assertTrue(POSITION_WRITERS.contains(name),
                            "the Hero's position is written outside its movement system: " + path + ":" + line
                                + System.lineSeparator()
                                + "Stepping is one system with one budget and one clamp. A second writer means a"
                                + " second idea of where the Hero may stand, and the coach, the meter and the"
                                + " walkable band all describe the first one.");
                    }
                });
            } catch (IOException e) {
                throw new UncheckedIOException("cannot walk " + root.toAbsolutePath(), e);
            }
        }
        assertTrue(writes.stream().anyMatch(write -> write.startsWith("HeroMovementSystem.java")),
            "the movement system no longer writes hero.x or hero.y, so the Hero cannot move and this whole"
                + " vocabulary is stale again: " + writes);
    }

    @Test
    void theMovementLessonNamesTheGestureTheRouterReports() {
        OnboardingStep move = OnboardingStep.MOVE;
        assertEquals(OnboardingAction.HERO_MOVED, move.action(),
            "the movement lesson completes on a real step order, not on any finger movement");
        String shown = move.line();
        assertTrue(shown.equals(OnboardingStrings.MOVE_LINE.english())
                || shown.equals(OnboardingStrings.MOVE_LINE.persian()),
            "the movement lesson shows the movement line, in one of the two languages: " + shown);
        assertTrue(OnboardingStrings.MOVE_LINE.english().toLowerCase(Locale.ROOT).contains("drag")
                && OnboardingStrings.MOVE_LINE.persian().contains("بکشید"),
            "and it has to name it in both languages, because a Persian player who is told to tap will tap and"
                + " the Hero will stand still: " + OnboardingStrings.MOVE_LINE.persian());

        String router = read(ROUTER);
        int from = router.indexOf("public boolean onTouchDragged");
        assertTrue(from >= 0, "the router no longer has a drag handler, so nothing can report HERO_MOVED");
        int to = router.indexOf("@Override", from);
        String dragHandler = router.substring(from, to < 0 ? router.length() : to);
        assertTrue(dragHandler.contains("HeroMovementSystem.orderStepTo"),
            "the drag handler has to be the thing that orders the step");
        assertTrue(dragHandler.contains("OnboardingAction.HERO_MOVED"),
            "and it has to report the lesson from inside the same handler, or the step teaches a gesture the"
                + " game does not connect to walking");
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

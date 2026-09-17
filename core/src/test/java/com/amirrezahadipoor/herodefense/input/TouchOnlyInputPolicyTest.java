package com.amirrezahadipoor.herodefense.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

final class TouchOnlyInputPolicyTest {
    @Test
    void controllerDeclaresPointerCallbacksAndNoKeyboardOrMouseOnlyBindings() {
        Set<String> declared = Arrays.stream(TouchInputController.class.getDeclaredMethods())
            .map(Method::getName)
            .collect(Collectors.toSet());

        assertTrue(declared.contains("touchDown"));
        assertTrue(declared.contains("touchDragged"));
        assertTrue(declared.contains("touchUp"));
        assertFalse(declared.contains("keyDown"));
        assertFalse(declared.contains("keyUp"));
        assertFalse(declared.contains("keyTyped"));
        assertFalse(declared.contains("mouseMoved"));
        assertFalse(declared.contains("scrolled"));
    }

    /** A key constant, as the shipped sources would have to spell it (`Keys.BACK`, `Keys.SPACE`, ...). */
    private static final Pattern KEY_REFERENCE = Pattern.compile("\\bKeys\\.([A-Z][A-Z0-9_]*)");

    private static final Path SOURCES = Path.of("..", "core", "src", "main", "java").normalize();

    /**
     * R7.4 binds exactly one key, and it is the platform's own navigation rather than a gameplay action.
     *
     * <p>Touch-only play is a rule of this repository, so the rule is stated as an invariant over the shipped
     * sources instead of as a promise: the only key constant any of them may name is {@code BACK}, and only in
     * the two files that catch it and handle it. A second key -- a debug shortcut, a cheat, a keyboard binding
     * for a desktop build -- has to be argued for here, in the open, before it can compile.
     */
    @Test
    void theOnlyKeyTheShippedSourcesNameIsAndroidsBack() throws IOException {
        Map<String, Set<String>> named = new TreeMap<>();
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                Matcher matcher = KEY_REFERENCE.matcher(Files.readString(file));
                Set<String> keys = new TreeSet<>();
                while (matcher.find()) {
                    keys.add(matcher.group(1));
                }
                if (!keys.isEmpty()) {
                    named.put(SOURCES.relativize(file).toString().replace('\\', '/'), keys);
                }
            }
        }

        assertEquals(
            Map.of(
                "com/amirrezahadipoor/herodefense/HeroDefenseGame.java", Set.of("BACK"),
                "com/amirrezahadipoor/herodefense/input/SystemBackKeyHandler.java", Set.of("BACK")),
            named,
            "a key was named outside the Back button's two files");
    }
}

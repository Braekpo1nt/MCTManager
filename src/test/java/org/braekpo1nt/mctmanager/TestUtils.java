package org.braekpo1nt.mctmanager;

import net.kyori.adventure.text.*;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

public class TestUtils {
    /**
     * Takes in a Component with 1 or more children, and converts it to a plaintext string without formatting.
     * Assumes it is made up of TextComponents and empty components.
     * @param component The component to get the plaintext version of
     * @return The concatenation of the contents() of the TextComponent children that this component is made of. Null if the component is null
     */
    public static @Nullable String toPlainText(@Nullable Component component) {
        if (component == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        
        if (component instanceof TextComponent textComponent) {
            builder.append(textComponent.content());
        }
        else if (component instanceof TranslatableComponent) {
            for (Component arg : ((TranslatableComponent) component).args()) {
                builder.append(toPlainText(arg));
            }
        } else if (component instanceof ScoreComponent scoreComponent) {
            builder.append(scoreComponent.name());
        } else if (component instanceof SelectorComponent selectorComponent) {
            builder.append(selectorComponent.pattern());
        } else if (component instanceof KeybindComponent keybindComponent) {
            builder.append(keybindComponent.keybind());
        } else if (component instanceof NBTComponent<?, ?> nbtComponent) {
            builder.append(nbtComponent.nbtPath());
        }
        
        for (Component child : component.children()) {
            builder.append(toPlainText(child));
        }
        
        return builder.toString();
    }
    
    /**
     * Asserts that the given component's plaintext is equal to the expected string.
     * @param expected The plaintext string you're expecting
     * @param actual The actual component to check the plaintext of
     */
    public static void assertComponentPlaintextEquals(String expected, Component actual) {
        String actualString = toPlainText(actual);
        Assertions.assertEquals(expected, actualString);
    }
}

package org.braekpo1nt.mctmanager.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class UIUtils {
    private UIUtils() {
        // do not instantiate
    }
    
    /**
     * Formats the input components with 40 spaces between the centers of each component.
     *
     * @param left the left component
     * @param middle the middle component
     * @param right the right component
     * @param spaceBetween the number of spaces between the centers of each component
     * @return the formatted component
     */
    public static @NotNull Component formatBossBarName(@NotNull Component left, @NotNull Component middle, @NotNull Component right, int spaceBetween) {
        String leftStr = PlainTextComponentSerializer.plainText().serialize(left);
        String middleStr = PlainTextComponentSerializer.plainText().serialize(middle);
        String rightStr = PlainTextComponentSerializer.plainText().serialize(right);
    
        int leftLength = leftStr.length();
        int middleLength = middleStr.length();
        int rightLength = rightStr.length();
        return formatBossBarName(left, leftLength, middle, middleLength, right, rightLength, spaceBetween);
    }
    
    /**
     * Formats the input components with 40 spaces between the centers of each component.
     *
     * @param left the left component
     * @param leftLength the length of the left component
     * @param middle the middle component
     * @param middleLength the length of the middle component
     * @param right the right component
     * @param rightLength the length of the right component
     * @param spaceBetween the number of spaces between the centers of each component
     * @return the formatted component
     */
    public static @NotNull Component formatBossBarName(@NotNull Component left, int leftLength, @NotNull Component middle, int middleLength, @NotNull Component right, int rightLength, int spaceBetween) {
        int leftPadding = spaceBetween - leftLength / 2 - middleLength / 2;
        int rightPadding = spaceBetween - middleLength / 2 - rightLength / 2;
        
        TextComponent paddingLeft = Component.text(" ".repeat(Math.max(0, leftPadding)));
        TextComponent paddingRight = Component.text(" ".repeat(Math.max(0, rightPadding)));
        
        return Component.empty()
                .append(left)
                .append(paddingLeft)
                .append(middle)
                .append(paddingRight)
                .append(right);
    }
}

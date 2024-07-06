package org.braekpo1nt.mctmanager.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class UIUtils {
    private static final Component KILL_PREFIX = Component.empty()
            .append(Component.text("["))
            .append(Component.text("k")
                    .color(NamedTextColor.GREEN))
            .append(Component.text("] "))
            ;
    private static final Title.Times KILL_TIMES = Title.Times.times(
            Duration.ZERO,
            Duration.ofSeconds(3),
            Duration.ofMillis(500)
    );
    private static final Title.Times DEFAULT_ANNOUNCEMENT_TIMES = Title.Times.times(
            Duration.ZERO,
            Duration.ofSeconds(3),
            Duration.ofMillis(500)
    );
    
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
    
    /**
     * Shows a subtitle to the killer indicating that they killed the given player
     * @param killer the one who killed
     * @param killed the one who was killed (the one who died)
     */
    public static void showKillTitle(Player killer, Player killed) {
        killer.showTitle(Title.title(
                Component.empty(), 
                Component.empty()
                        .append(KILL_PREFIX)
                        .append(killed.displayName()),
                KILL_TIMES
        ));
    }
}

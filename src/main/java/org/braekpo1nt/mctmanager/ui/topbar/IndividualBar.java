package org.braekpo1nt.mctmanager.ui.topbar;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the BossBar gui for an individual player. Has a left, middle, and right components which
 * can be easily updated for convenient display of information. 
 */
public class IndividualBar {
    /**
     * the number of characters between the centers of the right, middle, and left components
     */
    private static final int SPACE_BETWEEN = 40;
    private final @NotNull BossBar bossBar;
    /**
     * The component in the left portion of the BossBar
     */
    private @NotNull Component left;
    /**
     * The component in the middle portion of the BossBar
     */
    private @NotNull Component middle;
    /**
     * The component in the right portion of the BossBar
     */
    private @NotNull Component right;
    /**
     * the length (or number of characters) in the plaintext version of the left component
     */
    private int leftLength;
    /**
     * the length (or number of characters) in the plaintext version of the middle component
     */
    private int middleLength;
    /**
     * the length (or number of characters) in the plaintext version of the right component
     */
    private int rightLength;
    
    public IndividualBar() {
        this.bossBar = BossBar.bossBar(Component.empty(), 1f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
        this.left = Component.empty();
        this.middle = Component.empty();
        this.right = Component.empty();
        this.leftLength = 0;
        this.middleLength = 0;
        this.rightLength = 0;
    }
    
    /**
     * Set the left portion of the BossBar. The distance between components will automatically be adjusted.
     * @param left the component
     */
    public void setLeft(@NotNull Component left) {
        this.left = left;
        leftLength = PlainTextComponentSerializer.plainText().serialize(left).length();
        reformat();
    }
    
    /**
     * Set the middle portion of the BossBar. The distance between components will automatically be adjusted.
     * @param middle the component
     */
    public void setMiddle(@NotNull Component middle) {
        this.middle = middle;
        middleLength = PlainTextComponentSerializer.plainText().serialize(middle).length();
        reformat();
    }
    
    /**
     * Set the right portion of the BossBar. The distance between components will automatically be adjusted.
     * @param right the component
     */
    public void setRight(@NotNull Component right) {
        this.right = right;
        rightLength = PlainTextComponentSerializer.plainText().serialize(right).length();
        reformat();
    }
    
    /**
     * Used when one or more of the components are changed. Sets the name of the BossBar to be the
     * left, middle, and right components with the {@link IndividualBar#SPACE_BETWEEN} each component's middle
     * appropriately padded with spaces.
     */
    private void reformat() {
        bossBar.name(UIUtils.formatBossBarName(left, leftLength, middle, middleLength, right, rightLength, SPACE_BETWEEN));
    }
    
    /**
     * Show the given player this BossBar
     * @param player the player to show this to
     */
    public void show(@NotNull Player player) {
        player.showBossBar(bossBar);
    }
    
    /**
     * Hide this BossBar from the given player
     * @param player the player to hide this from
     */
    public void hide(@NotNull Player player) {
        player.hideBossBar(bossBar);
    }
    
    
}

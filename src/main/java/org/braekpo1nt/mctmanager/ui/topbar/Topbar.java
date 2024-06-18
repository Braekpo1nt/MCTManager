package org.braekpo1nt.mctmanager.ui.topbar;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Topbar {
    
    private final Map<UUID, IndividualBar> bossBars = new HashMap<>();
    
    public static class IndividualBar {
        private final @NotNull BossBar bossBar;
        private @NotNull Component left;
        private @NotNull Component middle;
        private @NotNull Component right;
        
        public IndividualBar() {
            this.bossBar = BossBar.bossBar(Component.empty(), 1f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
            this.left = Component.empty();
            this.middle = Component.empty();
            this.right = Component.empty();
        }
        
        public void setLeft(@NotNull Component left) {
            this.left = left;
            reformat();
        }
        
        public void setMiddle(@NotNull Component middle) {
            this.middle = middle;
            reformat();
        }
        
        public void setRight(@NotNull Component right) {
            this.right = right;
            reformat();
        }
        
        private void reformat() {
            bossBar.name(format(left, middle, right));
        }
        
        public void show(@NotNull Player player) {
            player.showBossBar(bossBar);
        }
        
        public void hide(@NotNull Player player) {
            player.hideBossBar(bossBar);
        }
        
        private static final int SPACE_BETWEEN = 40;
        
        /**
         * Formats the input components with 40 spaces between the centers of each component.
         *
         * @param left the left component
         * @param middle the middle component
         * @param right the right component
         * @return the formatted component
         */
        public static @NotNull Component format(@NotNull Component left, @NotNull Component middle, @NotNull Component right) {
            String leftStr = PlainTextComponentSerializer.plainText().serialize(left);
            String middleStr = PlainTextComponentSerializer.plainText().serialize(middle);
            String rightStr = PlainTextComponentSerializer.plainText().serialize(right);
            
            int leftPadding = SPACE_BETWEEN - leftStr.length() / 2 - middleStr.length() / 2;
            int rightPadding = SPACE_BETWEEN - middleStr.length() / 2 - rightStr.length() / 2;
            
            TextComponent paddingLeft = Component.text(" ".repeat(Math.max(0, leftPadding)));
            TextComponent paddingRight = Component.text(" ".repeat(Math.max(0, rightPadding)));
            
            return left.append(paddingLeft).append(middle).append(paddingRight).append(right);
        }
        
    }
    
    public void addPlayer(@NotNull Player player) {
        IndividualBar bossBar = new IndividualBar();
        bossBar.show(player);
        bossBars.put(player.getUniqueId(), bossBar);
    }
    
    public void removePlayer(@NotNull Player player) {
        IndividualBar bossBar = bossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            bossBar.hide(player);
        }
    }
    
    public void removePlayers(@NotNull List<@NotNull Player> players) {
        for (Player player : players) {
            this.removePlayer(player);
        }
    }
    
    public void setLeft(@NotNull Component left) {
        for (IndividualBar bossBar : bossBars.values()) {
            bossBar.setLeft(left);
        }
    }
    
    public void setLeft(@NotNull UUID playerUUID, @NotNull Component left) {
        IndividualBar bossBar = bossBars.get(playerUUID);
        if (bossBar != null) {
            bossBar.setLeft(left);
        }
    }
    
    public void setMiddle(@NotNull Component middle) {
        for (IndividualBar bossBar : bossBars.values()) {
            bossBar.setMiddle(middle);
        }
    }
    
    public void setMiddle(@NotNull UUID playerUUID, @NotNull Component middle) {
        IndividualBar bossBar = bossBars.get(playerUUID);
        if (bossBar != null) {
            bossBar.setMiddle(middle);
        }
    }
    
    public void setRight(@NotNull Component right) {
        for (IndividualBar bossBar : bossBars.values()) {
            bossBar.setRight(right);
        }
    }
    
    public void setRight(@NotNull UUID playerUUID, @NotNull Component right) {
        IndividualBar bossBar = bossBars.get(playerUUID);
        if (bossBar != null) {
            bossBar.setRight(right);
        }
    }
    
}

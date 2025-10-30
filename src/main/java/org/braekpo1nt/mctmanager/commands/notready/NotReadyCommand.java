package org.braekpo1nt.mctmanager.commands.notready;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NotReadyCommand implements TabExecutor {
    
    private final GameManager gameManager;
    private final Main plugin;
    private static final long COOLDOWN_MILLISECONDS = 5000; // 5 seconds
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    
    public NotReadyCommand(Main plugin, GameManager gameManager) {
        PluginCommand command = plugin.getCommand("notready");
        this.gameManager = gameManager;
        this.plugin = plugin;
        if (command == null) {
            Main.logger().severe("Unable to find /notready command in plugin.yml");
            return;
        }
        command.setExecutor(this);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
            return true;
        }
        
        Participant participant = gameManager.getOnlineParticipant(player.getUniqueId());
        if (participant == null) {
            sender.sendMessage(Component.text("Only participants can use this command", NamedTextColor.RED));
            return true;
        }
        
        UUID playerUuid = player.getUniqueId();
        
        // Check cooldown
        if (cooldowns.containsKey(playerUuid)) {
            long lastUsed = cooldowns.get(playerUuid);
            long now = System.currentTimeMillis();
            long remaining = COOLDOWN_MILLISECONDS - (now - lastUsed);
            
            if (remaining > 0) {
                long secondsRemaining = (remaining + 999) / 1000; // Round up
                sender.sendMessage(Component.text("You must wait " + secondsRemaining + " seconds before using this command again", NamedTextColor.RED));
                return true;
            }
        }
        
        // Update cooldown
        cooldowns.put(playerUuid, System.currentTimeMillis());
        
        // Notify player
        sender.sendMessage(Component.text("Admin team has been notified that you are not ready", NamedTextColor.YELLOW));
        
        // Prepare message for admins
        Component subtitle = Component.empty()
                .append(participant.displayName())
                .append(Component.text(" is not ready!"));
        Component chatMessage = Component.empty()
                .append(Component.text("[NOT READY]: ", NamedTextColor.RED))
                .append(participant.displayName()
                        .clickEvent(ClickEvent.runCommand("/tp " + participant.getName())))
                .append(Component.text(" is not ready, ", NamedTextColor.WHITE))
                .append(Component.text("click here to teleport", NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.runCommand("/tp " + participant.getName())));
        
        for (Player admin : gameManager.getOnlineAdmins()) {
            // Play notification sound on voice channel
            admin.playSound(admin.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, SoundCategory.VOICE, 0.7f, 1.0f);
            
            // Show subtitle
            Title title = Title.title(
                    Component.empty(),
                    subtitle,
                    Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(2500), Duration.ofMillis(500))
            );
            admin.showTitle(title);
            
            // Send chat message
            admin.sendMessage(chatMessage);
        }
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
package org.braekpo1nt.mctmanager.commands.dynamic.top;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TopCommand implements CommandExecutor {
    private final GameManager gameManager;
    
    private static boolean enabled = false;
    
    public static void setEnabled(boolean enabled) {
        TopCommand.enabled = enabled;
    }
    
    public TopCommand(Main plugin, GameManager gameManager) {
        PluginCommand command = plugin.getCommand("top");
        this.gameManager = gameManager;
        if (command == null) {
            Main.logger().severe("Unable to find /top command in plugin.yml");
            return;
        }
        command.setExecutor(this);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command");
            return true;
        }
        if (gameManager.isAdmin(player.getUniqueId())) {
            EntityUtils.top(player);
        } else if (gameManager.isParticipant(player.getUniqueId())) {
            if (!enabled) {
                sender.sendMessage("Can't use this command right now");
                return true;
            } else {
                EntityUtils.top(player);
            }
        }
        return true;
    }
    
}

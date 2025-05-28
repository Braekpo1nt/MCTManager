package org.braekpo1nt.mctmanager.commands.dynamic.top;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TopCommand implements CommandExecutor {
    private final GameManager gameManager;
    
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
        Participant participant = gameManager.getOnlineParticipant(player.getUniqueId());
        if (participant != null) {
            CommandResult commandResult = gameManager.top(player.getUniqueId());
            player.sendMessage(commandResult.getMessageOrEmpty());
            return true;
        }
        if (gameManager.isAdmin(player.getUniqueId())) {
            CommandResult commandResult = EntityUtils.top(player);
            player.sendMessage(commandResult.getMessageOrEmpty());
        }
        return true;
    }
    
}

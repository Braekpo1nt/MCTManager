package org.braekpo1nt.mctmanager.commands.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class LoadSubCommand implements CommandExecutor {
    
    private final GameManager gameManager;
    
    public LoadSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!gameManager.gameIsRunning()) {
            sender.sendMessage(Component.text("No game is running.")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        if (!gameManager.loadGameConfig(sender)) {
            sender.sendMessage(Component.text("Nothing changed.")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        return true;
    }
}

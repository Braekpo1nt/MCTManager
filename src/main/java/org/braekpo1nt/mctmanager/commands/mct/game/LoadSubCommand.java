package org.braekpo1nt.mctmanager.commands.mct.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.commands.commandmanager.SubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class LoadSubCommand implements SubCommand {
    
    private final GameManager gameManager;
    
    public LoadSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull String getName() {
        return "load";
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!gameManager.gameIsRunning()) {
            return CommandResult.failed(Component.text("No game is running."));
        }
        
        if (!gameManager.loadGameConfig(sender)) {
            return CommandResult.succeeded(Component.text("Nothing changed."));
        }
        
        return CommandResult.succeeded();
    }
}

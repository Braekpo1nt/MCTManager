package org.braekpo1nt.mctmanager.commands.mct.game;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.commandmanager.SubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class LoadSubCommand implements SubCommand {
    
    private final GameManager gameManager;
    private final @NotNull String name;
    
    public LoadSubCommand(GameManager gameManager, @NotNull String name) {
        this.name = name;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull String getName() {
        return name;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!gameManager.gameIsRunning()) {
            return CommandResult.failure(Component.text("No game is running."));
        }
        
        if (!gameManager.loadGameConfig(sender)) {
            return CommandResult.success(Component.text("Nothing changed."));
        }
        
        return CommandResult.success();
    }
}

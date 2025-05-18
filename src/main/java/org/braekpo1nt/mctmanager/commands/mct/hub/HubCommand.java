package org.braekpo1nt.mctmanager.commands.mct.hub;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class HubCommand extends CommandManager {
    
    private final TPCommand tpCommand;
    
    public HubCommand(@NotNull GameManager gameManager, @NotNull String name) {
        super(name);
        this.tpCommand = new TPCommand(gameManager, "tp");
        addSubCommand(tpCommand);
        addSubCommand(new MenuCommand(gameManager, "menu"));
    }
    
    @Override
    protected @NotNull CommandResult noArgumentAction(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
        return tpCommand.onSubCommand(sender, command, label, new String[0]);
    }
}

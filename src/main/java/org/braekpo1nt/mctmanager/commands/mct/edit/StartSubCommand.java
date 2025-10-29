package org.braekpo1nt.mctmanager.commands.mct.edit;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class StartSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    private final Main plugin;
    
    public StartSubCommand(Main plugin, GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (args.length != 2) {
            return CommandResult.failure(getUsage().of("<game>").of("<configFile>"));
        }
        
        String gameID = args[0];
        GameType gameType = GameType.fromID(gameID);
        if (gameType == null) {
            return CommandResult.failure(Component.text(gameID)
                    .append(Component.text(" is not a valid game")));
        }
        
        String configFile = args[1];
        
        CommandUtils.refreshGameConfigs(plugin);
        return gameManager.startEditor(gameType, configFile);
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return GameType.GAME_IDS.keySet().stream().sorted().toList();
        }
        if (args.length == 2) {
            String gameID = args[0];
            return CommandUtils.partialMatchTabList(
                    CommandUtils.getGameConfigs(gameID),
                    args[1]
            );
        }
        return Collections.emptyList();
    }
}

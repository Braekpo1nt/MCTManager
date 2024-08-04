package org.braekpo1nt.mctmanager.commands.readyup;

import org.braekpo1nt.mctmanager.commands.manager.MasterCommandManager;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class UnReadyCommand extends MasterCommandManager {
    private final GameManager gameManager;
    
    /**
     * Instantiates a new {@link MasterCommandManager} with the given plugin and name
     *
     * @param plugin      the plugin to register this command with.
     * @param gameManager the game manager
     * @throws IllegalArgumentException if the given plugin can't find a command by the given name, or if the given command doesn't have a permission
     */
    public UnReadyCommand(@NotNull JavaPlugin plugin, GameManager gameManager) {
        super(plugin, "unready");
        this.gameManager = gameManager;
        onInit(plugin.getServer().getPluginManager());
    }
    
    @Override
    protected @NotNull CommandResult noArgumentAction(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
        if (!(sender instanceof Player participant) ||
                !gameManager.isParticipant(participant.getUniqueId())) {
            return CommandResult.failure("Only participants can run this command");
        }
        gameManager.getEventManager().unReadyParticipant(participant);
        return CommandResult.success();
    }
}

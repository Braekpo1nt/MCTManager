package org.braekpo1nt.mctmanager.commands.readyup;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.commands.manager.MasterCommandManager;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ReadyUpCommand extends MasterCommandManager {
    private final GameManager gameManager;
    
    /**
     * Instantiates a new {@link MasterCommandManager} with the given plugin and name
     *
     * @param plugin the plugin to register this command with.
     * @param gameManager the gameManager
     * @throws IllegalArgumentException if the given plugin can't find a command by the given name, or if the given command doesn't have a permission
     */
    public ReadyUpCommand(@NotNull JavaPlugin plugin, GameManager gameManager) {
        super(plugin, "readyup");
        this.gameManager = gameManager;
        addSubCommand(new ListSubCommand(gameManager, "list"));
        onInit(plugin.getServer().getPluginManager());
    }
    
    @Override
    protected @NotNull CommandResult noArgumentAction(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
        if (!(sender instanceof Player participant) || 
                !gameManager.isParticipant(participant.getUniqueId())) {
            return CommandResult.failure("Only participants can run this command");
        }
        gameManager.getEventManager().readyUpParticipant(participant);
        return CommandResult.success();
    }
}

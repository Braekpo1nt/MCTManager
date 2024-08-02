package org.braekpo1nt.mctmanager.commands.readyup;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.MasterCommandManager;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class UnReady extends MasterCommandManager {
    private final GameManager gameManager;
    
    /**
     * Instantiates a new {@link MasterCommandManager} with the given plugin and name
     *
     * @param plugin      the plugin to register this command with.
     * @param gameManager the game manager
     * @throws IllegalArgumentException if the given plugin can't find a command by the given name, or if the given command doesn't have a permission
     */
    public UnReady(@NotNull JavaPlugin plugin, GameManager gameManager) {
        super(plugin, "unready");
        this.gameManager = gameManager;
    }
    
    @Override
    protected @NotNull CommandResult noArgumentAction(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
        // TODO: implement unready
        return CommandResult.success(Component.text("You are no longer ready"));
    }
}

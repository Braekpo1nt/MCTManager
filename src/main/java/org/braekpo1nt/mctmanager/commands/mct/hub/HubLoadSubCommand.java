package org.braekpo1nt.mctmanager.commands.mct.hub;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class HubLoadSubCommand extends SubCommand {
    
    private final GameManager gameManager;
    
    public HubLoadSubCommand(@NotNull GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            gameManager.loadHubConfig();
        } catch (ConfigException e) {
            Main.logger().severe(String.format("Could not load hubConfig. %s", e.getMessage()));
            e.printStackTrace();
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Error occurred loading hubConfig. See console for details: "))
                    .append(Component.text(e.getMessage())));
        }
        return CommandResult.success(Component.text("Loaded hubConfig"));
    }
}

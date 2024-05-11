package org.braekpo1nt.mctmanager.commands.mct.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class RemoveSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    
    public RemoveSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return CommandResult.failure(getUsage().of("<admin>"));
        }
        String name = args[0];
        OfflinePlayer admin = Bukkit.getOfflinePlayer(name);
        if (!gameManager.isAdmin(admin.getUniqueId())) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(name)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not an admin")));
        }
        gameManager.removeAdmin(sender, admin, name);
        return CommandResult.success();
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return null;
        }
        return Collections.emptyList();
    }
}

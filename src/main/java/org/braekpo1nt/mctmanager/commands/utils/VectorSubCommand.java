package org.braekpo1nt.mctmanager.commands.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.commandmanager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

class VectorSubCommand extends TabSubCommand {
    
    public VectorSubCommand(@NotNull String name) {
        super(name);
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {
            return CommandResult.failure(getUsage().of("[<player>]"));
        }
        
        if (args.length == 1) {
            String playerName = args[0];
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                return CommandResult.failure(Component.text("Player ")
                        .append(Component.text(playerName)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" not found")));
            }
            displayVector(sender, player.getLocation());
            return CommandResult.success();
        }
        
        if (!(sender instanceof Player player)) {
            return CommandResult.failure(getUsage().of("[<player>]"));
        }
        displayVector(sender, player.getLocation());
        return CommandResult.success();
    }
    
    private void displayVector(@NotNull CommandSender sender, Location location) {
        Vector precise = location.toVector();
        String preciseJson = UtilsUtils.GSON.toJson(precise);
        Vector block = location.toBlockLocation().toVector();
        String blockJson = UtilsUtils.GSON.toJson(block);
        sender.sendMessage(Component.empty()
                .append(UtilsUtils.attribute("Precise", preciseJson, NamedTextColor.WHITE))
                .append(UtilsUtils.attribute("Block", blockJson, NamedTextColor.WHITE))
        );
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return null;
        }
        return Collections.emptyList();
    }
}

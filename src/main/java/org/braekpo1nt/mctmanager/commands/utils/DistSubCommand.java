package org.braekpo1nt.mctmanager.commands.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.commandmanager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

class DistSubCommand extends TabSubCommand {
    
    public DistSubCommand(@NotNull String name) {
        super(name);
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 6) {
            return CommandResult.failure(getUsage().of("<x1>", "<y1>", "<z1>", "<x2>", "<y2>", "<z2>"));
        }
        
        for (String coordinate : args) {
            if (!CommandUtils.isDouble(coordinate)) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(coordinate)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a number")));
            }
        }
        
        double x1 = Double.parseDouble(args[0]);
        double y1 = Double.parseDouble(args[1]);
        double z1 = Double.parseDouble(args[2]);
        double x2 = Double.parseDouble(args[3]);
        double y2 = Double.parseDouble(args[4]);
        double z2 = Double.parseDouble(args[5]);
        
        Vector vector1 = new Vector(x1, y1, z1);
        Vector vector2 = new Vector(x2, y2, z2);
        double distance = vector1.distance(vector2);
        String vector1Str = String.format("%s, %s, %s", x1, y1, z1);
        String vector2Str = String.format("%s, %s, %s", x2, y2, z2);
        sender.sendMessage(Component.empty()
                .append(Component.text("Distance from ("))
                .append(Component.text(vector1Str)
                        .decorate(TextDecoration.BOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Copy to clipboard")))
                        .clickEvent(ClickEvent.copyToClipboard(vector1Str)))
                .append(Component.text(") to ("))
                .append(Component.text(vector2Str)
                        .decorate(TextDecoration.BOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Copy to clipboard")))
                        .clickEvent(ClickEvent.copyToClipboard(vector2Str)))
                .append(Component.text(") is "))
                .append(Component.text(distance)
                        .decorate(TextDecoration.BOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Copy to clipboard")))
                        .clickEvent(ClickEvent.copyToClipboard(String.format("%s", distance))))
        );
        return CommandResult.success();
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }
        return UtilsUtils.tabCompleteCoordinates(player, args.length);
    }
    
}

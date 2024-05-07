package org.braekpo1nt.mctmanager.commands.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.display.Display;
import org.braekpo1nt.mctmanager.display.geometry.Edge;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

class DistSubCommand extends TabSubCommand {
    
    private final Main plugin;
    
    public DistSubCommand(Main plugin, @NotNull String name) {
        super(name);
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 6 || args.length > 7) {
            return CommandResult.failure(getUsage().of("<x1>", "<y1>", "<z1>", "<x2>", "<y2>", "<z2>").of("[true|false]"));
        }
    
        for (int i = 0; i < 6; i++) {
            String coordinate = args[i];
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
    
        if (args.length < 7) {
            return CommandResult.success();
        }
        Boolean shouldDisplay = CommandUtils.toBoolean(args[6]);
        if (shouldDisplay == null) {
            return CommandResult.failure(Component.text(args[6])
                    .append(Component.text(" is not a boolean value")));
        }
        
        if (!(sender instanceof Player player)) {
            return CommandResult.failure(Component.text("Only players can be shown a display"));
        }
    
        Display display = new Display(plugin, new Edge(vector1, vector2).pointsAlongEdgeWithDistance(1.0), Color.FUCHSIA);
        display.show(player, 3*20);
        return CommandResult.success();
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 6 || !(sender instanceof Player player)) {
            return Collections.emptyList();
        }
        return UtilsUtils.tabCompleteCoordinates(player, args.length);
    }
    
}

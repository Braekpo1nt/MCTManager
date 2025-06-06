package org.braekpo1nt.mctmanager.commands.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.display.BoxDisplay;
import org.braekpo1nt.mctmanager.display.Display;
import org.braekpo1nt.mctmanager.display.geometry.GeometryUtils;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

class BoundingBoxSubCommand extends TabSubCommand {
    
    private final Main plugin;
    
    public BoundingBoxSubCommand(Main plugin, @NotNull String name) {
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
        
        BoundingBox boundingBox = new BoundingBox(x1, y1, z1, x2, y2, z2);
        if (boundingBox.getMin().equals(boundingBox.getMax())) {
            return CommandResult.failure(Component.text("The bounding box's min and max corners can't be equal."));
        }
        String boundingBoxJson = UtilsUtils.GSON.toJson(boundingBox);
        sender.sendMessage(Component.empty()
                .append(UtilsUtils.attribute("BoundingBox", boundingBoxJson, NamedTextColor.WHITE))
                .append(UtilsUtils.attribute("Height", boundingBox.getHeight(), NamedTextColor.GREEN))
                .append(UtilsUtils.attribute("WidthX", boundingBox.getWidthX(), NamedTextColor.RED))
                .append(UtilsUtils.attribute("WidthZ", boundingBox.getWidthZ(), NamedTextColor.BLUE))
                .append(UtilsUtils.attribute("Volume", boundingBox.getVolume(), NamedTextColor.WHITE))
                .append(UtilsUtils.attribute("Center", String.format("%s, %s, %s", boundingBox.getCenterX(), boundingBox.getCenterY(), boundingBox.getCenterZ()), NamedTextColor.WHITE))
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
        
        Display display = new BoxDisplay(boundingBox, Color.FUCHSIA);
        display.show(player.getWorld());
        plugin.getServer().getScheduler().runTaskLater(plugin, display::hide, 5*20L);
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

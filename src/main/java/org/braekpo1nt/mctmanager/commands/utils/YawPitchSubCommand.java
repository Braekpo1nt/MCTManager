package org.braekpo1nt.mctmanager.commands.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.dto.YawPitch;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class YawPitchSubCommand extends TabSubCommand {
    
    public YawPitchSubCommand(@NotNull String name) {
        super(name);
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 3) {
            return CommandResult.failure(getUsage().of("<x>", "<y>", "<z>"));
        }
        
        for (String coordinate : args) {
            if (!CommandUtils.isDouble(coordinate)) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(coordinate)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a number")));
            }
        }
        
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        double z = Double.parseDouble(args[2]);
        
        if (!(sender instanceof Player player)) {
            return CommandResult.failure(Component.text("Only a player can use this command"));
        }
        yawPitch(sender, x, y, z, player);
        return CommandResult.success();
    }
    
    private void yawPitch(@NotNull CommandSender sender, double x, double y, double z, Player player) {
        Vector source = player.getLocation().toVector();
        Vector target = new Vector(x, y, z);
        YawPitch precise = EntityUtils.getPlayerLookAtYawPitch(source, target);
        Location location = player.getLocation();
        location.setYaw(precise.yaw());
        location.setPitch(precise.pitch());
        player.teleport(location);
        String preciseJson = UtilsUtils.GSON.toJson(precise);
        
        YawPitch rounded = new YawPitch(
                MathUtils.specialRound(precise.yaw(), 0.5f),
                MathUtils.specialRound(precise.pitch(), 0.5f)
        );
        String roundedJson = UtilsUtils.GSON.toJson(rounded);
        
        sender.sendMessage(Component.empty()
                .append(UtilsUtils.attribute("Precise", preciseJson, NamedTextColor.WHITE))
                .append(UtilsUtils.attribute("Rounded", roundedJson, NamedTextColor.WHITE))
        );
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 3 || !(sender instanceof Player player)) {
            return Collections.emptyList();
        }
        return UtilsUtils.tabCompleteCoordinates(player, args.length);
    }
}

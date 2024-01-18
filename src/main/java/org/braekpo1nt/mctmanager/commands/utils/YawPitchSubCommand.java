package org.braekpo1nt.mctmanager.commands.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.games.game.config.YawPitch;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class YawPitchSubCommand implements TabExecutor {
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 3) {
            sender.sendMessage(Component.text("Usage: /utils lookat <x> <y> <z>")
                    .color(NamedTextColor.RED));
            return true;
        }
    
        for (String coordinate : args) {
            if (!CommandUtils.isDouble(coordinate)) {
                sender.sendMessage(Component.text(coordinate)
                        .append(Component.text(" is not a number"))
                        .color(NamedTextColor.RED));
                return true;
            }
        }
    
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        double z = Double.parseDouble(args[2]);
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can use this command")
                    .color(NamedTextColor.RED));
            return true;
        }
        return yawPitch(sender, x, y, z, player);
    }
    
    private boolean yawPitch(@NotNull CommandSender sender, double x, double y, double z, Player player) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Float.class, new FloatSerializer())
                .create();
        
        Vector source = player.getLocation().toVector();
        Vector target = new Vector(x, y, z);
        YawPitch precise = EntityUtils.getPlayerLookAtYawPitch(source, target);
        Location location = player.getLocation();
        location.setYaw(precise.yaw());
        location.setPitch(precise.pitch());
        player.teleport(location);
        String preciseJson = gson.toJson(precise);
        
        YawPitch rounded = new YawPitch(
                UtilsUtils.specialRound(precise.yaw(), 0.5f),
                UtilsUtils.specialRound(precise.pitch(), 0.5f)
        );
        String roundedJson = gson.toJson(rounded);
        
        sender.sendMessage(Component.empty()
                .append(UtilsUtils.attribute("Precise", preciseJson, NamedTextColor.WHITE))
                .append(UtilsUtils.attribute("Rounded", roundedJson, NamedTextColor.WHITE))
        );
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args.length > 3) {
            return Collections.emptyList();
        }
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }
        Block targetBlock = player.getTargetBlock(UtilsUtils.TRANSPARENT, 5);
        if (UtilsUtils.TRANSPARENT.contains(targetBlock.getType())) {
            return Collections.emptyList();
        }
        switch (args.length) {
            case 1 -> {
                return Collections.singletonList(""+targetBlock.getLocation().getBlockX());
            }
            case 2 -> {
                return Collections.singletonList(""+targetBlock.getLocation().getBlockY());
            }
            case 3 -> {
                return Collections.singletonList(""+targetBlock.getLocation().getBlockZ());
            }
        }
        return Collections.emptyList();
    }
}

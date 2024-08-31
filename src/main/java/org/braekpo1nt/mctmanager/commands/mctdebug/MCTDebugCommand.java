package org.braekpo1nt.mctmanager.commands.mctdebug;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements TabExecutor, Listener {
    
    
    private final Main plugin;
    
    public MCTDebugCommand(Main plugin) {
        Objects.requireNonNull(plugin.getCommand("mctdebug")).setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Must be a player to run this command")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /mctdebug <arg> [options]")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        switch (args[0]) {
            case "save" -> {
                if (args.length != 8) {
                    sender.sendMessage(Component.text("Usage: /mctdebug save <file> <x1> <y1> <z1> <x2> <y2> <z2>"));
                    return true;
                }
                String fileName = args[1];
                for (int i = 2; i < 8; i++) {
                    String coordinate = args[i];
                    if (!CommandUtils.isInteger(coordinate)) {
                        sender.sendMessage(Component.empty()
                                .append(Component.text(coordinate)
                                        .decorate(TextDecoration.BOLD))
                                .append(Component.text(" is not an integer")));
                        return true;
                    }
                }
                int x1 = Integer.parseInt(args[2]);
                int y1 = Integer.parseInt(args[3]);
                int z1 = Integer.parseInt(args[4]);
                int x2 = Integer.parseInt(args[5]);
                int y2 = Integer.parseInt(args[6]);
                int z2 = Integer.parseInt(args[7]);
                // copy specified region
                CuboidRegion region = new CuboidRegion(new BlockVector3(x1, y1, z1), new BlockVector3(x2, y2, z2));
                BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
                ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                        BukkitAdapter.adapt(player.getWorld()), region, clipboard, region.getMinimumPoint()
                );
                try {
                    Operations.complete(forwardExtentCopy);
                } catch (WorldEditException e) {
                    Main.logger().log(Level.SEVERE, "exception while trying to copy region", e);
                    sender.sendMessage(Component.text("An error occurred while trying to copy, please see console for details"));
                    return true;
                }
                // world edit save schematic
                File file = new File(plugin.getDataFolder(), fileName);
                try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(new FileOutputStream(file))) {
                    writer.write(clipboard);
                } catch (IOException e) {
                    Main.logger().log(Level.SEVERE, "Exception while performing operation", e);
                }
            }
            case "load" -> {
                if (args.length != 5) {
                    sender.sendMessage(Component.text("Usage: /mctdebug load <file> <x> <y> <z>"));
                    return true;
                }
                String fileName = args[1];
                for (int i = 2; i < 5; i++) {
                    String coordinate = args[i];
                    if (!CommandUtils.isDouble(coordinate)) {
                        sender.sendMessage(Component.empty()
                                .append(Component.text(coordinate)
                                        .decorate(TextDecoration.BOLD))
                                .append(Component.text(" is not a number")));
                        return true;
                    }
                }
                double x = Double.parseDouble(args[2]);
                double y = Double.parseDouble(args[3]);
                double z = Double.parseDouble(args[4]);
                // world edit load schematic
                
            }
            default -> {
                sender.sendMessage("Unknown argument " + args[0]);
                return true;
            }
        }
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}

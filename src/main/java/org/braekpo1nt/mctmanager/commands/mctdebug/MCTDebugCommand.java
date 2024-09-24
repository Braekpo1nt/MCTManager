package org.braekpo1nt.mctmanager.commands.mctdebug;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements TabExecutor, Listener {
    
    
    private final Main plugin;
    
    private final World world;
    private final Random random = new Random();
    private final List<Block> hyperGrowBlocks;
    private final double numOfBlocks;
    
    private double chancePerGameTick;
    private double factor = 1.0;
    
    public MCTDebugCommand(Main plugin) {
        Objects.requireNonNull(plugin.getCommand("mctdebug")).setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        
        this.world = Objects.requireNonNull(plugin.getServer().getWorld("FT"));
        double height = world.getMaxHeight() - world.getMinHeight();
        numOfBlocks = 16*16*height;
        double randomTickSpeed = Objects.requireNonNull(world.getGameRuleValue(GameRule.RANDOM_TICK_SPEED));
        updateChancePerGameTick(randomTickSpeed);
        
        hyperGrowBlocks = List.of(
                world.getBlockAt(-7, -60, 157),
                world.getBlockAt(-8, -60, 157),
                world.getBlockAt(-9, -60, 157),
                
                world.getBlockAt(-7, -60, 156),
                world.getBlockAt(-9, -60, 156),
                
                world.getBlockAt(-7, -60, 155),
                world.getBlockAt(-8, -60, 155),
                world.getBlockAt(-9, -60, 155)
                );
        
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block hyperGrowBlock : hyperGrowBlocks) {
                    if (random.nextDouble() <= chancePerGameTick) {
                        hyperGrowBlock.randomTick();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void updateChancePerGameTick(double randomTickSpeed) {
        chancePerGameTick = factor * randomTickSpeed / numOfBlocks;
        Main.logger().info(String.format("randomTickSpeed=%s, numOfBlocks=%s, chancePerGameTick=%s", randomTickSpeed, numOfBlocks, chancePerGameTick));
    }
    
    @EventHandler
    public void onGameRuleChange(WorldGameRuleChangeEvent event) {
        if (!event.getGameRule().equals(GameRule.RANDOM_TICK_SPEED)) {
            return;
        }
        int newRandomTickSpeed = Integer.parseInt(event.getValue());
        updateChancePerGameTick(newRandomTickSpeed);
    }
    
    @EventHandler
    public void growEvent(BlockGrowEvent event) {
        Block block = event.getBlock();
        if (!(block.getBlockData() instanceof Ageable ageable)) {
            return;
        }
        Location l = block.getLocation();
        int indexOf = hyperGrowBlocks.indexOf(block);
        if (indexOf >= 0) {
            Main.logger().info(String.format("Hyper grow %d: (%d, %d, %d), %d/%d", indexOf, l.getBlockX(), l.getBlockY(), l.getBlockZ(), ageable.getAge(), ageable.getMaximumAge()));
        } else {
            Main.logger().info(String.format("(%d, %d, %d), %d/%d", l.getBlockX(), l.getBlockY(), l.getBlockZ(), ageable.getAge(), ageable.getMaximumAge()));
        }
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
        
        factor = Double.parseDouble(args[0]);
        double randomTickSpeed = Objects.requireNonNull(world.getGameRuleValue(GameRule.RANDOM_TICK_SPEED));
        updateChancePerGameTick(randomTickSpeed);
//        switch (args[0]) {
//            case "save" -> {
//                if (args.length != 8) {
//                    sender.sendMessage(Component.text("Usage: /mctdebug save <file> <x1> <y1> <z1> <x2> <y2> <z2>"));
//                    return true;
//                }
//                String fileName = args[1];
//                for (int i = 2; i < 8; i++) {
//                    String coordinate = args[i];
//                    if (!CommandUtils.isInteger(coordinate)) {
//                        sender.sendMessage(Component.empty()
//                                .append(Component.text(coordinate)
//                                        .decorate(TextDecoration.BOLD))
//                                .append(Component.text(" is not an integer")));
//                        return true;
//                    }
//                }
//                int x1 = Integer.parseInt(args[2]);
//                int y1 = Integer.parseInt(args[3]);
//                int z1 = Integer.parseInt(args[4]);
//                int x2 = Integer.parseInt(args[5]);
//                int y2 = Integer.parseInt(args[6]);
//                int z2 = Integer.parseInt(args[7]);
//                // copy specified region
//                CuboidRegion region = new CuboidRegion(new BlockVector3(x1, y1, z1), new BlockVector3(x2, y2, z2));
//                BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
//                ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
//                        BukkitAdapter.adapt(player.getWorld()), region, clipboard, region.getMinimumPoint()
//                );
//                try {
//                    Operations.complete(forwardExtentCopy);
//                } catch (WorldEditException e) {
//                    Main.logger().log(Level.SEVERE, "exception while trying to copy region", e);
//                    sender.sendMessage(Component.text("An error occurred while trying to save, please see console for details"));
//                    return true;
//                }
//                // world edit save schematic
//                File file = new File(plugin.getDataFolder(), fileName);
//                try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(new FileOutputStream(file))) {
//                    writer.write(clipboard);
//                } catch (FileNotFoundException e) {
//                    Main.logger().log(Level.SEVERE, "Could not find file " + file, e);
//                    sender.sendMessage(Component.text("An error occurred while trying to save, please see console for details"));
//                    return true;
//                } catch (IOException e) {
//                    Main.logger().log(Level.SEVERE, "Exception while writing to file " + file, e);
//                    sender.sendMessage(Component.text("An error occurred while trying to save, please see console for details"));
//                    return true;
//                }
//                sender.sendMessage("Success");
//            }
//            case "load" -> {
//                if (args.length != 5) {
//                    sender.sendMessage(Component.text("Usage: /mctdebug load <file> <x> <y> <z>"));
//                    return true;
//                }
//                String fileName = args[1];
//                for (int i = 2; i < 5; i++) {
//                    String coordinate = args[i];
//                    if (!CommandUtils.isInteger(coordinate)) {
//                        sender.sendMessage(Component.empty()
//                                .append(Component.text(coordinate)
//                                        .decorate(TextDecoration.BOLD))
//                                .append(Component.text(" is not an integer")));
//                        return true;
//                    }
//                }
//                int x = Integer.parseInt(args[2]);
//                int y = Integer.parseInt(args[3]);
//                int z = Integer.parseInt(args[4]);
//                // world edit load schematic
//                File file = new File(plugin.getDataFolder(), fileName);
//                Clipboard clipboard;
//                ClipboardFormat format = ClipboardFormats.findByFile(file);
//                if (format == null) {
//                    Main.logger().severe("Could not find file " + file);
//                    sender.sendMessage(Component.text("An error occurred while trying to load, please see console for details"));
//                    return true;
//                }
//                try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
//                    clipboard = reader.read();
//                } catch (FileNotFoundException e) {
//                    Main.logger().log(Level.SEVERE, "Could not find file " + file, e);
//                    sender.sendMessage(Component.text("An error occurred while trying to load, please see console for details"));
//                    return true;
//                } catch (IOException e) {
//                    Main.logger().log(Level.SEVERE, "Exception while reading from file " + file, e);
//                    sender.sendMessage(Component.text("An error occurred while trying to load, please see console for details"));
//                    return true;
//                }
//                try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(player.getWorld()))) {
//                    Operation operation = new ClipboardHolder(clipboard)
//                            .createPaste(editSession)
//                            .to(BlockVector3.at(x, y, z))
//                            .build();
//                    Operations.complete(operation);
//                } catch (WorldEditException e) {
//                    Main.logger().log(Level.SEVERE, "Exception while pasting", e);
//                    sender.sendMessage(Component.text("An error occurred while trying to load, please see console for details"));
//                    return true;
//                }
//                sender.sendMessage("Success");
//            }
//            default -> {
//                sender.sendMessage("Unknown argument " + args[0]);
//                return true;
//            }
//        }
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}

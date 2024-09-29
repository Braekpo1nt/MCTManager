package org.braekpo1nt.mctmanager.commands.mctdebug;

import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
        
        this.world = plugin.getServer().getWorld("FT");
        if (this.world == null) {
            hyperGrowBlocks = null;
            numOfBlocks = 0;
            return;
        }
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
        
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                for (Block hyperGrowBlock : hyperGrowBlocks) {
//                    if (random.nextDouble() <= chancePerGameTick) {
//                        hyperGrowBlock.randomTick();
//                        hyperGrowBlock.randomTick();
//                    }
//                }
//            }
//        }.runTaskTimer(plugin, 0L, 1L);
        
        ItemStack result = new ItemStack(Material.GRASS_BLOCK);
        NamespacedKey key = new NamespacedKey(plugin, "grass_from_dirt");
        ShapedRecipe shapedRecipe = new ShapedRecipe(key, result);
        shapedRecipe.shape("DD");
        shapedRecipe.setIngredient('D', Material.DIRT);
        plugin.getServer().addRecipe(shapedRecipe);
        plugin.getServer().removeRecipe(key);
        
    }
    
    private void updateChancePerGameTick(double randomTickSpeed) {
        chancePerGameTick = factor * randomTickSpeed / numOfBlocks;
        Main.logger().info(String.format("randomTickSpeed=%s, numOfBlocks=%s, chancePerGameTick=%s", randomTickSpeed, numOfBlocks, chancePerGameTick));
    }
    
//    @EventHandler
//    public void onGameRuleChange(WorldGameRuleChangeEvent event) {
//        if (!event.getGameRule().equals(GameRule.RANDOM_TICK_SPEED)) {
//            return;
//        }
//        int newRandomTickSpeed = Integer.parseInt(event.getValue());
//        updateChancePerGameTick(newRandomTickSpeed);
//    }
//    
//    @EventHandler
//    public void growEvent(BlockGrowEvent event) {
//        Block block = event.getBlock();
//        if (!(block.getBlockData() instanceof Ageable ageable)) {
//            return;
//        }
//        Location l = block.getLocation();
//        int indexOf = hyperGrowBlocks.indexOf(block);
//        if (indexOf >= 0) {
//            Main.logger().info(String.format("Hyper grow %d: (%d, %d, %d), %d/%d", indexOf, l.getBlockX(), l.getBlockY(), l.getBlockZ(), ageable.getAge(), ageable.getMaximumAge()));
//        } else {
//            Main.logger().info(String.format("(%d, %d, %d), %d/%d", l.getBlockX(), l.getBlockY(), l.getBlockZ(), ageable.getAge(), ageable.getMaximumAge()));
//        }
//    }
    
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
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}

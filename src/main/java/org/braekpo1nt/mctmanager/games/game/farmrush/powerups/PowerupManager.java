package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.PowerupSpec;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PowerupManager {
    
    public static final Map<Powerup.Type, ItemStack> typeToItem;
    
    static {
        ItemStack cropGrowerItem = new ItemStack(Material.BEACON);
        cropGrowerItem.editMeta(meta -> {
            meta.displayName(Component.text("Crop Grower"));
            meta.lore(List.of(
                    Component.text("Place this near crops"),
                    Component.text("to make them grow faster")
            ));
        });
        
        typeToItem = Map.of(
                Powerup.Type.CROP_GROWER, cropGrowerItem
        );
    }
    
    private final FarmRushGame context;
    /**
     * the physically placed powerups in the world
     */
    private final Map<Vector, Powerup> powerups = new HashMap<>();
    private BukkitTask powerupActionTask;
    
    public PowerupManager(FarmRushGame context) {
        this.context = context;
    }
    
    public void start() {
        addPowerupRecipes();
        Main.logger().info("Farm Rush Powerups started");
        
        powerupActionTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Powerup powerup : powerups.values()) {
                    powerup.performAction();
                }
            }
        }.runTaskTimer(context.getPlugin(), 0L, 20L);
    }
    
    public void stop() {
        // TODO: must be idempotent
        if (powerupActionTask != null) {
            powerupActionTask.cancel();
            powerupActionTask = null;
        }
        removePowerupRecipes();
        powerups.clear();
        Main.logger().info("Farm Rush Powerups stopped");
    }
    
    private void removePowerupRecipes() {
        if (context.getConfig().getCropGrowerSpec() != null) {
            context.getPlugin().getServer().removeRecipe(context.getConfig().getCropGrowerSpec().getRecipeKey());
        }
        context.getPlugin().getServer().updateRecipes();
    }
    
    private void addPowerupRecipes() {
        if (context.getConfig().getCropGrowerSpec() != null) {
            context.getPlugin().getServer().addRecipe(context.getConfig().getCropGrowerSpec().getRecipe());
        }
        context.getPlugin().getServer().updateRecipes();
    }
    
    /**
     * Checks against all {@link PowerupSpec#getItem()} to retrieve the powerup associated
     * with the given item. 
     * @param item the item that might be associated with a {@link PowerupSpec}
     * @return the {@link PowerupSpec} associated with the item, if there is one
     */
    private @Nullable PowerupSpec itemToPowerupSpec(@NotNull ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return null;
        }
        if (typeToItem.get(Powerup.Type.CROP_GROWER).getItemMeta().equals(itemMeta)) {
            return context.getConfig().getCropGrowerSpec();
        }
        return null;
    }
    
    /**
     * Called when a participant places a block
     * @param event the event
     */
    public void onPlaceBlock(BlockPlaceEvent event) {
        Main.logger().info("PowerupManager.onPlaceBlock");
        PowerupSpec powerupSpec = itemToPowerupSpec(event.getItemInHand());
        if (powerupSpec == null) {
            return;
        }
        Main.logger().info(String.format("placed powerup %s", powerupSpec.getType()));
        Location location = event.getBlockPlaced().getLocation();
        Powerup powerup = powerupSpec.createPowerup(location);
        powerups.put(location.toVector(), powerup);
    }
    
    public void onBlockBreak(Block block, Cancellable event) {
        Location location = block.getLocation();
        Powerup powerup = powerups.remove(location.toVector());
        if (powerup == null) {
            return;
        }
        event.setCancelled(true);
        ItemStack powerupItem = typeToItem.get(powerup.getType());
        location.getWorld().dropItemNaturally(location.add(new Vector(0.5, 0.5, 0.5)), powerupItem);
        block.setType(Material.AIR);
    }
    
    /**
     * Goes through the list to check if any of them are powerup blocks. If found,
     * a powerup block is replaced with air and the appropriate item is dropped.
     * 
     * @param blocks the blocks that are broken
     * @return the blocks from the list that were powerup blocks. Use this list to remove
     * these from the event, so they don't drop their natural item.
     */
    public List<Block> onBlocksBreak(List<Block> blocks) {
        List<Block> blocksToRemove = new ArrayList<>();
        for (Block block : blocks) {
            Location location = block.getLocation();
            Powerup powerup = powerups.remove(location.toVector());
            if (powerup == null) {
                return Collections.emptyList();
            }
            blocksToRemove.add(block);
            ItemStack powerupItem = typeToItem.get(powerup.getType());
            location.getWorld().dropItemNaturally(location.add(new Vector(0.5, 0.5, 0.5)), powerupItem);
            block.setType(Material.AIR);
        }
        return blocksToRemove;
    }
}

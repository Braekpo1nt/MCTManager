package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.AnimalGrowerSpec;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.CropGrowerSpec;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PowerupManager {
    
    public static final ItemStack cropGrowerItem;
    public static final ItemStack animalGrowerItem;
    
    static {
        cropGrowerItem = new ItemStack(Material.FURNACE);
        cropGrowerItem.editMeta(meta -> {
            meta.displayName(Component.text("Crop Grower"));
            meta.lore(List.of(
                    Component.text("Place this near crops"),
                    Component.text("to make them grow faster")
            ));
        });
        
        animalGrowerItem = new ItemStack(Material.BLAST_FURNACE);
        animalGrowerItem.editMeta(meta -> {
            meta.displayName(Component.text("Animal Grower"));
            meta.lore(List.of(
                    Component.text("Place this near animals"),
                    Component.text("to make them grow up"),
                    Component.text("and breed faster")
            ));
        });
        
    }
    
    private final FarmRushGame context;
    /**
     * the physically placed crop growers in the world
     */
    private final Map<Vector, CropGrower> cropGrowers = new HashMap<>();
    /**
     * the physically placed animalGrowers in the world
     */
    private final Map<Vector, AnimalGrower> animalGrowers = new HashMap<>();
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
                for (CropGrower cropGrower : cropGrowers.values()) {
                    cropGrower.performAction();
                }
                for (AnimalGrower animalGrower : animalGrowers.values()) {
                    animalGrower.performAction();
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
        cropGrowers.clear();
        Main.logger().info("Farm Rush Powerups stopped");
    }
    
    private void removePowerupRecipes() {
        CropGrowerSpec cropGrowerSpec = context.getConfig().getCropGrowerSpec();
        if (cropGrowerSpec != null) {
            context.getPlugin().getServer().removeRecipe(cropGrowerSpec.getRecipeKey());
        }
        AnimalGrowerSpec animalGrowerSpec = context.getConfig().getAnimalGrowerSpec();
        if (animalGrowerSpec != null) {
            context.getPlugin().getServer().removeRecipe(animalGrowerSpec.getRecipeKey());
        }
        context.getPlugin().getServer().updateRecipes();
    }
    
    private void addPowerupRecipes() {
        CropGrowerSpec cropGrowerSpec = context.getConfig().getCropGrowerSpec();
        if (cropGrowerSpec != null) {
            context.getPlugin().getServer().addRecipe(cropGrowerSpec.getRecipe());
        }
        AnimalGrowerSpec animalGrowerSpec = context.getConfig().getAnimalGrowerSpec();
        if (animalGrowerSpec != null) {
            context.getPlugin().getServer().addRecipe(animalGrowerSpec.getRecipe());
        }
        context.getPlugin().getServer().updateRecipes();
    }
    
    /**
     * Called when a participant places a block
     * @param event the event
     */
    public void onPlaceBlock(@NotNull BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Location location = event.getBlockPlaced().getLocation();
        if (context.getConfig().getCropGrowerSpec() != null) {
            if (context.getConfig().getCropGrowerSpec().isItem(item)) {
                CropGrower cropGrower = context.getConfig().getCropGrowerSpec().createPowerup(location);
                cropGrowers.put(location.toVector(), cropGrower);
                Main.logger().info("Placed Crop Grower");
            }
        }
        if (context.getConfig().getAnimalGrowerSpec() != null) {
            if (context.getConfig().getAnimalGrowerSpec().isItem(item)) {
                AnimalGrower animalGrower = context.getConfig().getAnimalGrowerSpec().createPowerup(location);
                animalGrowers.put(location.toVector(), animalGrower);
                Main.logger().info("Placed Animal Grower");
            }
        }
    }
    
    public void onBlockBreak(Block block, Cancellable event) {
        if (onBreakCropGrower(block)) {
            event.setCancelled(true);
            return;
        }
        if (onBreakAnimalGrower(block)) {
            event.setCancelled(true);
            return;
        }
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
            if (onBreakCropGrower(block)) {
                blocksToRemove.add(block);
            }
            if (onBreakAnimalGrower(block)) {
                blocksToRemove.add(block);
            }
        }
        return blocksToRemove;
    }
    
    private boolean onBreakCropGrower(Block block) {
        if (context.getConfig().getCropGrowerSpec() == null) {
            return false;
        }
        Location location = block.getLocation();
        CropGrower cropGrower = cropGrowers.remove(location.toVector());
        if (cropGrower == null) {
            return false;
        }
        location.getWorld().dropItemNaturally(location.add(new Vector(0.5, 0.5, 0.5)), cropGrowerItem);
        block.setType(Material.AIR);
        return true;
    }
    
    private boolean onBreakAnimalGrower(Block block) {
        if (context.getConfig().getAnimalGrowerSpec() == null) {
            return false;
        }
        Location location = block.getLocation();
        AnimalGrower animalGrower = animalGrowers.remove(location.toVector());
        if (animalGrower == null) {
            return false;
        }
        location.getWorld().dropItemNaturally(location.add(new Vector(0.5, 0.5, 0.5)), animalGrowerItem);
        block.setType(Material.AIR);
        return true;
    }
}

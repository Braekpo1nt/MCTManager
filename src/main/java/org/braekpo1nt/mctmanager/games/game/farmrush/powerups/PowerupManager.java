package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.AnimalGrowerSpec;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.CropGrowerSpec;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PowerupManager {
    
    private final FarmRushGame context;
    /**
     * the physically placed crop growers in the world
     */
    private final Map<Vector, CropGrower> cropGrowers = new HashMap<>();
    /**
     * the physically placed animalGrowers in the world
     */
    private final Map<Vector, AnimalGrower> animalGrowers = new HashMap<>();
    private @Nullable BukkitTask cropGrowerTask;
    private @Nullable BukkitTask cropGrowerParticleTask;
    private @Nullable BukkitTask animalGrowerTask;
    private @Nullable BukkitTask animalGrowerParticleTask;
    
    public PowerupManager(FarmRushGame context) {
        this.context = context;
    }
    
    public void start() {
        addPowerupRecipes();
        
        CropGrowerSpec cropGrowerSpec = context.getConfig().getCropGrowerSpec();
        AnimalGrowerSpec animalGrowerSpec = context.getConfig().getAnimalGrowerSpec();
        cropGrowerTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (CropGrower cropGrower : cropGrowers.values()) {
                    cropGrower.growAttempt();
                }
            }
        }.runTaskTimer(context.getPlugin(), 0L, cropGrowerSpec.getTicksPerCycle());
        animalGrowerTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (AnimalGrower animalGrower : animalGrowers.values()) {
                    animalGrower.updateAnimals();
                }
            }
        }.runTaskTimer(context.getPlugin(), 0L, animalGrowerSpec.getTicksPerCycle());
        
        cropGrowerParticleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (CropGrower cropGrower : cropGrowers.values()) {
                    cropGrower.displayRadius(
                            cropGrowerSpec.getNumberOfParticles(),
                            cropGrowerSpec.getParticle(),
                            cropGrowerSpec.getParticleCount()
                    );
                }
            }
        }.runTaskTimer(context.getPlugin(), 0L, cropGrowerSpec.getTicksPerParticleCycle());
        animalGrowerParticleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (AnimalGrower animalGrower : animalGrowers.values()) {
                    animalGrower.displayRadius(
                        animalGrowerSpec.getNumberOfParticles(),
                        animalGrowerSpec.getParticle(),
                        animalGrowerSpec.getParticleCount()
                    );
                }
            }
        }.runTaskTimer(context.getPlugin(), 0L, animalGrowerSpec.getTicksPerParticleCycle());
        Main.logger().info("Farm Rush Powerups started");
    }
    
    public void stop() {
        // TODO: must be idempotent
        if (cropGrowerTask != null) {
            cropGrowerTask.cancel();
            cropGrowerTask = null;
        }
        if (animalGrowerTask != null) {
            animalGrowerTask.cancel();
            animalGrowerTask = null;
        }
        if (cropGrowerParticleTask != null) {
            cropGrowerParticleTask.cancel();
            cropGrowerParticleTask = null;
        }
        if (animalGrowerParticleTask != null) {
            animalGrowerParticleTask.cancel();
            animalGrowerParticleTask = null;
        }
        removePowerupRecipes();
        cropGrowers.clear();
        Main.logger().info("Farm Rush Powerups stopped");
    }
    
    private void removePowerupRecipes() {
        context.getPlugin().getServer().removeRecipe(context.getConfig().getCropGrowerSpec().getRecipeKey());
        context.getPlugin().getServer().removeRecipe(context.getConfig().getAnimalGrowerSpec().getRecipeKey());
        context.getPlugin().getServer().updateRecipes();
    }
    
    private void addPowerupRecipes() {
        context.getPlugin().getServer().addRecipe(context.getConfig().getCropGrowerSpec().getRecipe());
        context.getPlugin().getServer().addRecipe(context.getConfig().getAnimalGrowerSpec().getRecipe());
        context.getPlugin().getServer().updateRecipes();
    }
    
    /**
     * Called when a participant places a block
     * @param event the event
     */
    public void onPlaceBlock(@NotNull BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Location location = event.getBlockPlaced().getLocation();
        if (context.getConfig().getCropGrowerSpec().isItem(item)) {
            CropGrower cropGrower = context.getConfig().getCropGrowerSpec().createPowerup(location);
            cropGrowers.put(location.toVector(), cropGrower);
            Main.logger().info("Placed Crop Grower");
        }
        if (context.getConfig().getAnimalGrowerSpec().isItem(item)) {
            AnimalGrower animalGrower = context.getConfig().getAnimalGrowerSpec().createPowerup(location);
            animalGrowers.put(location.toVector(), animalGrower);
            Main.logger().info("Placed Animal Grower");
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
    
    /**
     * @param block the block to remove, if it is a crop grower
     * @return true if the given block is a cropGrower and was removed, false
     * otherwise
     */
    private boolean onBreakCropGrower(Block block) {
        Location location = block.getLocation();
        CropGrower cropGrower = cropGrowers.remove(location.toVector());
        if (cropGrower == null) {
            return false;
        }
        location.getWorld().dropItemNaturally(location.add(new Vector(0.5, 0.5, 0.5)), 
                context.getConfig().getCropGrowerSpec().getCropGrowerItem());
        block.setType(Material.AIR);
        return true;
    }
    
    private boolean onBreakAnimalGrower(Block block) {
        Location location = block.getLocation();
        AnimalGrower animalGrower = animalGrowers.remove(location.toVector());
        if (animalGrower == null) {
            return false;
        }
        location.getWorld().dropItemNaturally(location.add(new Vector(0.5, 0.5, 0.5)), 
                context.getConfig().getAnimalGrowerSpec().getAnimalGrowerItem());
        block.setType(Material.AIR);
        return true;
    }
    
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder == null) {
            return;
        }
        if (!(holder instanceof BlockInventoryHolder blockInventoryHolder)) {
            return;
        }
        Vector vector = blockInventoryHolder.getBlock().getLocation().toVector();
        if (cropGrowers.containsKey(vector)
        || animalGrowers.containsKey(vector)) {
            event.setCancelled(true);
        }
    }
}

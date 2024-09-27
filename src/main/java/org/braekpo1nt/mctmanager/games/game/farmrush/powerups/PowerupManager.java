package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfig;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.CropGrowerSpec;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.PowerupSpec;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PowerupManager {
    
    public static final Map<Powerup.Type, PowerupSpec> typeToPowerupSpec;
    static {
        ItemStack cropGrowerItem = new ItemStack(Material.BEACON);
        cropGrowerItem.editMeta(meta -> {
            meta.displayName(Component.text("Crop Grower"));
            meta.lore(List.of(
                    Component.text("Place this near crops"),
                    Component.text("to make them grow faster")
            ));
        });
        CropGrowerSpec cropGrowerSpec = new CropGrowerSpec(cropGrowerItem, Powerup.Type.CROP_GROWER);
        
        typeToPowerupSpec = Map.of(
                Powerup.Type.CROP_GROWER, cropGrowerSpec
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
        setUpPowerups();
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
        for (FarmRushConfig.PowerupData powerupData : context.getConfig().getPowerupSpecData()) {
            context.getPlugin().getServer().removeRecipe(powerupData.getRecipeKey());
        }
        context.getPlugin().getServer().updateRecipes();
        powerups.clear();
        Main.logger().info("Farm Rush Powerups stopped");
    }
    
    private void setUpPowerups() {
        for (FarmRushConfig.PowerupData powerupData : context.getConfig().getPowerupSpecData()) {
            PowerupSpec powerup = typeToPowerupSpec.get(powerupData.getType());
            powerup.setRadius(powerupData.getRadius());
            context.getPlugin().getServer().addRecipe(powerupData.getRecipe());
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
        for (PowerupSpec powerupSpec : typeToPowerupSpec.values()) {
            if (powerupSpec.getItem().getItemMeta().equals(itemMeta)) {
                return powerupSpec;
            }
        }
        return null;
    }
    
    /**
     * Called when a participant places a block
     * @param event the event
     * @param participant the participant who placed the block
     */
    public void onPlaceBlock(BlockPlaceEvent event, FarmRushGame.Participant participant) {
        PowerupSpec powerupSpec = itemToPowerupSpec(event.getItemInHand());
        if (powerupSpec == null) {
            return;
        }
        Main.logger().info(String.format("placed powerup %s", powerupSpec.getType()));
        Location location = event.getBlockPlaced().getLocation();
        Powerup powerup = powerupSpec.createPowerup(location);
        powerups.put(location.toVector(), powerup);
    }
    
}

package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class PowerupManager {
    
    public static final Map<Powerup.Type, Powerup> typeToPowerup;
    static {
        ItemStack cropGrowerItem = new ItemStack(Material.BEACON);
        cropGrowerItem.editMeta(meta -> {
            meta.displayName(Component.text("Crop Grower"));
            meta.lore(List.of(
                    Component.text("Place this near crops"),
                    Component.text("to make them grow faster")
            ));
        });
        CropGrower cropGrower = new CropGrower(cropGrowerItem, Powerup.Type.CROP_GROWER);
        
        typeToPowerup = Map.of(
                Powerup.Type.CROP_GROWER, cropGrower
        );
    }
    
    private final FarmRushGame context;
    
    public PowerupManager(FarmRushGame context) {
        this.context = context;
    }
    
    public void start() {
        setUpPowerups();
        Main.logger().info("Farm Rush Powerups started");
    }
    
    public void stop() {
        // TODO: must be idempotent
        for (FarmRushConfig.PowerupData powerupData : context.getConfig().getPowerupData()) {
            context.getPlugin().getServer().removeRecipe(powerupData.getRecipeKey());
        }
        context.getPlugin().getServer().updateRecipes();
        Main.logger().info("Farm Rush Powerups stopped");
    }
    
    private void setUpPowerups() {
        for (FarmRushConfig.PowerupData powerupData : context.getConfig().getPowerupData()) {
            Powerup powerup = typeToPowerup.get(powerupData.getType());
            powerup.setRadius(powerupData.getRadius());
            context.getPlugin().getServer().addRecipe(powerupData.getRecipe());
        }
        context.getPlugin().getServer().updateRecipes();
    }
    
}

package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.config.FarmRushConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class PowerupManager {
    
    public static final String POWERUP_METADATA_KEY = "powerup";
    public static final String CROP_GROWER_METADATA_VALUE = "crop_grower";
    
    public static final Map<Powerup.Type, Powerup> typeToPowerup;
    static {
        ItemStack cropGrowerItem = new ItemStack(Material.BEACON);
        cropGrowerItem.editMeta(meta -> {
            meta.displayName(Component.text("Crop Grower"));
            meta.lore(List.of(
                    Component.text("Place this near crops to make them grow faster")
            ));
        });
        CropGrower cropGrower = new CropGrower(cropGrowerItem, Powerup.Type.CROP_GROWER);
        
        typeToPowerup = Map.of(
                Powerup.Type.CROP_GROWER, cropGrower
        );
    }
    
    private final FarmRushConfig config;
    
    public PowerupManager(FarmRushGame context) {
        this.config = context.getConfig();
        setUpPowerups();
    }
    
    private void setUpPowerups() {
        for (FarmRushConfig.PowerupData powerupData : config.getPowerupData()) {
            Powerup powerup = typeToPowerup.get(powerupData.getType());
            powerup.setRadius(powerupData.getRadius());
        }
    }
    
}

package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;

import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
public class CropGrower extends Powerup {
    /**
     * a list of the crops in the area of effect
     */
    private @Nullable List<Location> crops;
    
    public CropGrower(Location location, double radius) {
        super(location.getWorld(), location, radius);
    }
    
    @Override
    public void performAction() {
        detectCrops();
        // randomly grow crops
    }
    
    private void detectCrops() {
        this.crops = BlockPlacementUtils.getBlocksInRadius(location, radius, Tag.CROPS);
        Main.logger().info(String.format("Discovered %d crops", crops.size()));
    }
}


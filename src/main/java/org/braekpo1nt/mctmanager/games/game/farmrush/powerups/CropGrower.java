package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;

import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.jetbrains.annotations.Nullable;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
public class CropGrower extends Powerup {
    /**
     * a list of the crops in the area of effect
     */
    private @Nullable List<Block> crops;
    /**
     * how many cycles before running a probability check
     */
    private final int count;
    /**
     * the probability per second of a crop increasing in age per cycle
     */
    private final double probability;
    
    public CropGrower(Location location, double radius, int count, double probability) {
        super(location.getWorld(), location, radius);
        this.count = count;
        this.probability = probability;
    }
    
    @Override
    public void performAction() {
        detectCrops();
        if (crops == null) {
            return;
        }
        for (Block crop : crops) {
            if (crop.getBlockData() instanceof Ageable ageable) {
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    ageable.setAge(ageable.getAge() + 1);
                    crop.setBlockData(ageable);
                }
            }
        }
    }
    
    private void detectCrops() {
        this.crops = BlockPlacementUtils.getBlocksInRadius(location, radius, Tag.CROPS);
    }
}


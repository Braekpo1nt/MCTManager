package org.braekpo1nt.mctmanager.games.game.farmrush.powerups;

import lombok.EqualsAndHashCode;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;


@EqualsAndHashCode(callSuper = true)
public class CropGrower extends Powerup {
    private static final Random random = new Random();
    
    /**
     * a list of the crops in the area of effect
     */
    private @Nullable List<Block> crops;
    /**
     * how many seconds before running a probability check
     */
    private final int seconds;
    /**
     * the probability per {@link #performAction()} of a crop increasing in age
     */
    private final double growthChance;
    /**
     * number of {@link #performAction()} cycles before running a probability check
     */
    private int count;
    
    public CropGrower(Location location, double radius, int seconds, double growthChance) {
        super(location.getWorld(), location, radius);
        this.seconds = seconds;
        this.count = seconds;
        this.growthChance = growthChance;
    }
    
    @Override
    public Type getType() {
        return Type.CROP_GROWER;
    }
    
    @Override
    public void performAction() {
        if (count > 0) {
            count--;
            return;
        }
        count = seconds;
        detectCrops();
        if (crops == null) {
            return;
        }
        for (Block crop : crops) {
            if (crop.getBlockData() instanceof Ageable ageable) {
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    if (random.nextDouble() <= growthChance) {
                        ageable.setAge(ageable.getAge() + 1);
                        crop.setBlockData(ageable);
                    }
                }
            }
        }
    }
    
    private void detectCrops() {
        this.crops = BlockPlacementUtils.getBlocksInRadius(location, radius, Tag.CROPS);
    }
}


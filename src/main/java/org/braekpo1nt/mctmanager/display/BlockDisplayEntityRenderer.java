package org.braekpo1nt.mctmanager.display;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;

public class BlockDisplayEntityRenderer extends DisplayEntityRenderer<BlockDisplay> {
    
    private @NotNull BlockData blockData;
    
    public BlockDisplayEntityRenderer(
            @NotNull Location location, 
            boolean glowing, 
            @NotNull Color glowColor, 
            @NotNull Transformation transformation, 
            int interpolationDuration, 
            int teleportDuration,
            @NotNull BlockData blockData) {
        super(location, glowing, glowColor, transformation, interpolationDuration, teleportDuration);
        this.blockData = blockData;
    }
    
    public BlockDisplayEntityRenderer(@NotNull Location location, @NotNull Transformation transformation, @NotNull BlockData blockData) {
        this(
            location,
            false,
            Color.WHITE,
            transformation,
            0,
            0,
            blockData
        );
    }
    
    public BlockDisplayEntityRenderer(@NotNull Location location, @NotNull BlockData blockData) {
        this(location, DisplayEntityRenderer.NO_TRANSFORMATION, blockData);
    }
    
    @Override
    public @NotNull Class<BlockDisplay> getClazz() {
        return BlockDisplay.class;
    }
    
    @Override
    protected void show(@NotNull BlockDisplay entity) {
        super.show(entity);
        entity.setBlock(blockData);
    }
    
    public void setBlockData(@NotNull BlockData blockData) {
        this.blockData = blockData;
        if (entity == null) {
            return;
        }
        entity.setBlock(blockData);
    }
    
}

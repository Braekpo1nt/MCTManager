package org.braekpo1nt.mctmanager.display;

import lombok.Builder;
import org.braekpo1nt.mctmanager.display.delegates.BlockDisplayDelegate;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Renderer representing a {@link BlockDisplay} entity
 */
public class BlockDisplayEntityRenderer extends DisplayEntityRenderer<BlockDisplay> implements BlockDisplayDelegate {
    
    private @NotNull BlockData blockData;
    
    @Builder
    public BlockDisplayEntityRenderer(
            @NotNull Location location, 
            boolean glowing, 
            @Nullable Color glowColor, 
            @Nullable Display.Brightness brightness,
            @Nullable Transformation transformation, 
            int interpolationDuration, 
            int teleportDuration,
            @Nullable BlockData blockData) {
        super(location, glowing, glowColor, brightness, transformation, interpolationDuration, teleportDuration);
        this.blockData = (blockData != null) ? blockData : Material.GRASS_BLOCK.createBlockData();
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
    
    @Override
    public void setBlockData(@NotNull BlockData blockData) {
        this.blockData = blockData;
        if (entity == null) {
            return;
        }
        entity.setBlock(blockData);
    }
    
    @Override
    public @NotNull BlockData getBlockData() {
        return blockData;
    }
    
}

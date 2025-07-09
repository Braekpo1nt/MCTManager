package org.braekpo1nt.mctmanager.display.delegates;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * The interface for any Renderer which allows you to set attributes specific to
 * {@link org.bukkit.entity.BlockDisplay}, specifically block data
 */
public interface HasBlockData {
    /**
     * @param blockData The BlockData for the {@link org.bukkit.entity.BlockDisplay}
     */
    void setBlockData(@NotNull BlockData blockData);
    
    /**
     * Convenience method for setting the BlockData. Uses the {@link Material#createBlockData()}
     * output as the input for {@link #setBlockData(BlockData)}.
     * @param material the material to get the block data from.
     */
    default void setMaterial(@NotNull Material material) {
        setBlockData(material.createBlockData());
    }
    
    @NotNull BlockData getBlockData();
}

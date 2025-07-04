package org.braekpo1nt.mctmanager.display.delegates;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * The interface for any Renderer which allows you to set attributes specific to
 * {@link org.bukkit.entity.BlockDisplay}, specifically block data
 */
// TODO: this may not need to be an inheritor of DisplayDelegate. Instead, make this its own interface, and have BlockDisplayRenderer implementations implement both this and DisplayDelegate. You may also be able to rename this to be more like "BlockDataAble" or something similar. That way if something is a DisplayComposite<DisplayDelegate> made up of both a text display and a block display (and anything else) you can implement those individually. Instead of getRenderer() you would have DisplayComposite#getDisplayRenderers() and BlockDisplayComposite#getBlockDisplayRenderers().
public interface BlockDisplayDelegate extends DisplayDelegate {
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

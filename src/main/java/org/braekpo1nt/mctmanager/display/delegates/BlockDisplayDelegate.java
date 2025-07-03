package org.braekpo1nt.mctmanager.display.delegates;

import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * The interface for any Renderer which allows you to set attributes specific to
 * {@link org.bukkit.entity.BlockDisplay}, specifically block data
 */
public interface BlockDisplayDelegate extends DisplayDelegate {
    void setBlockData(@NotNull BlockData blockData);
}

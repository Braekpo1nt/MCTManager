package org.braekpo1nt.mctmanager.display.delegates;

import org.braekpo1nt.mctmanager.display.Renderer;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * A convenience interface for implementing {@link BlockDisplayDelegate} on a {@link Renderer} which
 * is composed of a single {@link BlockDisplayDelegate} renderer
 */
public interface BlockDisplaySingleton extends DisplaySingleton<BlockDisplayDelegate>, BlockDisplayDelegate {
    
    @Override
    @NotNull BlockDisplayDelegate getRenderer();
    
    @Override
    default void setBlockData(@NotNull BlockData blockData) {
        getRenderer().setBlockData(blockData);
    }
    
    @Override
    default @NotNull BlockData getBlockData() {
        return getRenderer().getBlockData();
    }
}

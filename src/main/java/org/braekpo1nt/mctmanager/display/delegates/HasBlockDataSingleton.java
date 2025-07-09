package org.braekpo1nt.mctmanager.display.delegates;

import org.braekpo1nt.mctmanager.display.Renderer;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * A convenience interface for implementing {@link HasBlockData} on a {@link Renderer} which
 * is composed of a single {@link HasBlockData} renderer
 */
public interface HasBlockDataSingleton extends HasBlockData {
    
    @NotNull
    HasBlockData getHasBlockData();
    
    @Override
    default void setBlockData(@NotNull BlockData blockData) {
        getHasBlockData().setBlockData(blockData);
    }
    
    @Override
    default @NotNull BlockData getBlockData() {
        return getHasBlockData().getBlockData();
    }
}

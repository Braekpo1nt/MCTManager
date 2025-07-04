package org.braekpo1nt.mctmanager.display.delegates;

import org.braekpo1nt.mctmanager.display.Renderer;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A convenience interface for implementing {@link BlockDisplayDelegate} on a {@link Renderer} which
 * is composed of multiple {@link BlockDisplayDelegate} renderers
 */
public interface BlockDisplayComposite extends DisplayComposite<BlockDisplayDelegate>, BlockDisplayDelegate {
    
    @Override
    @NotNull Collection<? extends BlockDisplayDelegate> getRenderers();
    
    @Override
    default void setBlockData(@NotNull BlockData blockData) {
        getRenderers().forEach(r -> r.setBlockData(blockData));
    }
    
    @Override
    default @NotNull BlockData getBlockData() {
        return getPrimaryRenderer().getBlockData();
    }
}

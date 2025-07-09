package org.braekpo1nt.mctmanager.display.delegates;

import org.braekpo1nt.mctmanager.display.Renderer;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A convenience interface for implementing {@link HasBlockData} on a {@link Renderer} which
 * is composed of multiple {@link HasBlockData} renderers
 */
public interface HasBlockDataComposite extends HasBlockDataSingleton {
    
    @NotNull Collection<? extends HasBlockData> getHasBlockDatas();
    
    @Override
    default void setBlockData(@NotNull BlockData blockData) {
        getHasBlockDatas().forEach(r -> r.setBlockData(blockData));
    }
}

package org.braekpo1nt.mctmanager.display.boundingbox;

import org.braekpo1nt.mctmanager.display.delegates.DisplayDelegate;
import org.braekpo1nt.mctmanager.display.delegates.HasBlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

/**
 * Interface of renderers which display a BoundingBox in some way
 */
public interface BoundingBoxRenderer extends DisplayDelegate, HasBlockData {
    /**
     * Set the BoundingBox to display
     * @param boundingBox the BoundingBox to display
     */
    void setBoundingBox(@NotNull BoundingBox boundingBox);
    
    /**
     * @return the BoundingBox rendered by this display
     */
    @NotNull BoundingBox getBoundingBox();
}

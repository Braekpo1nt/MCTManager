package org.braekpo1nt.mctmanager.display.boundingbox;

import org.braekpo1nt.mctmanager.display.delegates.BlockDisplayComposite;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

/**
 * Interface of renderers which display a BoundingBox in some way
 */
public interface BoundingBoxRenderer extends BlockDisplayComposite {
    /**
     * Set the BoundingBox to display
     * @param boundingBox the BoundingBox to display
     */
    void setBoundingBox(@NotNull BoundingBox boundingBox);
}

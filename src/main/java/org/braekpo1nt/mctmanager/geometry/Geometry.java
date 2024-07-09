package org.braekpo1nt.mctmanager.geometry;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public interface Geometry {
    boolean overlaps(@NotNull Geometry geometry);
    boolean contains(Vector vector);
}

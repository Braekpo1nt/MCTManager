package org.braekpo1nt.mctmanager.geometry;

import lombok.Data;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Made up of multiple {@link Geometry}s. Not necessarily cohesive. 
 */
@Data
public class CompositeGeometry {
    private List<Geometry> geometries;
    
    /**
     * @return true if each {@link Geometry} object in {@link CompositeGeometry#geometries}
     * overlaps at least one other object in the list. False otherwise. true if the list is empty.
     */
    public boolean isCohesive() {
        if (geometries.isEmpty()) {
            return true;
        }
        
        for (int i = 0; i < geometries.size(); i++) {
            Geometry current = geometries.get(i);
            boolean hasOverlap = false;
            for (int j = 0; j < geometries.size(); j++) {
                if (i != j && current.overlaps(geometries.get(j))) {
                    hasOverlap = true;
                    break;
                }
            }
            if (!hasOverlap) {
                return false;
            }
        }
        return true;
    }
    
    public boolean contains(Vector vector) {
        for (Geometry geometry : geometries) {
            if (geometry.contains(vector)) {
                return true;
            }
        }
        return false;
    }
}

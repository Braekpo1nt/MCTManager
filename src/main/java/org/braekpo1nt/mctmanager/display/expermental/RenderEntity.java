package org.braekpo1nt.mctmanager.display.expermental;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

@Data
public abstract class RenderEntity<T extends Entity> {
    private final Class<T> clazz;
    private final Location location;
    public abstract void init(T entity);
    public abstract void update(T entity);
}

package org.braekpo1nt.mctmanager.display.expermental;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingleEntityRenderer<T extends Entity> {
    private @Nullable T entity;
    
    public void render(@NotNull RenderEntity<T> part) {
        if (entity == null) {
            entity = part.getLocation().getWorld().spawn(part.getLocation(), part.getClazz());
            part.init(entity);
        }
        entity.teleport(part.getLocation());
        part.update(entity);
    }
    
    public void close() {
        if (entity != null) {
            entity.remove();
        }
        entity = null;
    }
}

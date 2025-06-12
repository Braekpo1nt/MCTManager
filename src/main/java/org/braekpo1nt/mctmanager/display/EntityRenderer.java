package org.braekpo1nt.mctmanager.display;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EntityRenderer<T extends Entity> implements Display {
    protected @NotNull Location location;
    protected boolean glowing;
    protected @Nullable T entity;
    
    public EntityRenderer(@NotNull Location location, boolean glowing) {
        this.location = location;
        this.glowing = glowing;
    }
    
    public abstract @NotNull Class<T> getClazz();
    
    @Override
    public void show() {
        entity = location.getWorld().spawn(location, getClazz());
        show(entity);
    }
    
    /**
     * Convenience method called internally, after the entity has been spawned,
     * and thus {@link #entity} is not null
     * @param entity the entity to show
     */
    protected abstract void show(@NotNull T entity);
    
    public void setLocation(@NotNull Location location) {
        this.location = location;
        if (entity == null) {
            return;
        }
        entity.teleport(location);
    }
    
    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
        if (entity == null) {
            return;
        }
        entity.setGlowing(glowing);
    }
    
    @Override
    public void hide() {
        if (entity == null) {
            return;
        }
        entity.remove();
        entity = null;
    }
    // can the entity object be non-null if it is not in the world? 
}

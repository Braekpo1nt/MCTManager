package org.braekpo1nt.mctmanager.display;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A Renderer implementation for any type of Entity
 * @param <T> the type of entity we're rendering
 */
public abstract class EntityRenderer<T extends Entity> implements Renderer {
    @Getter
    protected @NotNull Location location;
    protected boolean glowing;
    protected @Nullable T entity;
    
    public EntityRenderer(@NotNull Location location, boolean glowing) {
        this.location = Objects.requireNonNull(location, "location can't be null");
        this.glowing = glowing;
    }
    
    /**
     * @return a class reference for the type of entity represented by this renderer
     */
    public abstract @NotNull Class<T> getClazz();
    
    @Override
    public void show() {
        entity = location.getWorld().spawn(location, getClazz());
        entity.setGlowing(glowing);
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

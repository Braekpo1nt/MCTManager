package org.braekpo1nt.mctmanager.display;

import lombok.Getter;
import net.kyori.adventure.text.Component;
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
    @Getter
    protected @Nullable Component customName;
    @Getter
    protected boolean customNameVisible;
    protected boolean glowing;
    protected @Nullable T entity;
    private boolean showing;
    
    public EntityRenderer(
            @NotNull Location location, 
            @Nullable Component customName, 
            boolean customNameVisible, 
            boolean glowing) {
        this.location = Objects.requireNonNull(location, "location can't be null");
        this.customName = customName;
        this.customNameVisible = customNameVisible;
        this.glowing = glowing;
        this.showing = false;
    }
    
    /**
     * @return a class reference for the type of entity represented by this renderer
     */
    public abstract @NotNull Class<T> getClazz();
    
    @Override
    public void show() {
        if (showing) {
            return;
        }
        showing = true;
        entity = location.getWorld().spawn(location, getClazz());
        entity.customName(customName);
        entity.setCustomNameVisible(customNameVisible);
        entity.setGlowing(glowing);
        show(entity);
    }
    
    @Override
    public boolean showing() {
        return showing;
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
    
    public void customName(@Nullable Component customName) {
        this.customName = customName;
        if (entity == null) {
            return;
        }
        entity.customName(customName);
    }
    
    public @Nullable Component customName() {
        return customName;
    }
    
    public void setCustomNameVisible(boolean customNameVisible) {
        this.customNameVisible = customNameVisible;
        if (entity == null) {
            return;
        }
        entity.setCustomNameVisible(customNameVisible);
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
        if (!showing) {
            return;
        }
        showing = false;
        if (entity == null) {
            return;
        }
        entity.remove();
        entity = null;
    }
    // can the entity object be non-null if it is not in the world? 
}

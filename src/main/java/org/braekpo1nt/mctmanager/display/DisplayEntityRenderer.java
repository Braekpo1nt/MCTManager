package org.braekpo1nt.mctmanager.display;

import lombok.Getter;
import org.braekpo1nt.mctmanager.display.delegates.DisplayDelegate;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A Renderer implementation for rendering a {@link Display} entity
 * @param <T> the specific implementation of {@link Display} which we are rendering
 */
public abstract class DisplayEntityRenderer<T extends Display> extends EntityRenderer<T> implements DisplayDelegate {
    
    /**
     * A default {@link Transformation} which represents no Transformation on the entity
     * (no rotation, no translation, no scaling)
     */
    public static final Transformation NO_TRANSFORMATION = new Transformation(
            new Vector3f(),
            new Quaternionf(),
            new Vector3f(1, 1, 1),
            new Quaternionf()
    );
    
    @Getter
    private @NotNull Color glowColor;
    @Getter
    private @Nullable Display.Brightness brightness;
    private @NotNull Transformation transformation;
    @Getter
    private int interpolationDuration;
    @Getter
    private int teleportDuration;
    
    public DisplayEntityRenderer(
            @NotNull Location location, 
            boolean glowing,
            @Nullable Color glowColor,
            @Nullable Display.Brightness brightness,
            @Nullable Transformation transformation,
            int interpolationDuration,
            int teleportDuration) {
        super(location, glowing);
        this.glowColor = (glowColor != null) ? glowColor : Color.WHITE;
        this.brightness = brightness;
        this.transformation = (transformation != null) ? transformation : NO_TRANSFORMATION;
        this.interpolationDuration = interpolationDuration;
        this.teleportDuration = teleportDuration;
    }
    
    @Override
    protected void show(@NotNull T entity) {
        entity.setGlowColorOverride(glowColor);
        entity.setBrightness(brightness);
        entity.setTransformation(transformation);
        entity.setInterpolationDuration(interpolationDuration);
        entity.setTeleportDuration(teleportDuration);
    }
    
    @Override
    public void setGlowColor(@NotNull Color glowColor) {
        this.glowColor = glowColor;
        if (entity == null) {
            return;
        }
        entity.setGlowColorOverride(glowColor);
    }
    
    @Override
    public void setBrightness(@Nullable Display.Brightness brightness) {
        this.brightness = brightness;
        if (entity == null) {
            return;
        }
        entity.setBrightness(brightness);
    }
    
    public void setTransformation(@NotNull Transformation transformation) {
        this.transformation = transformation;
        if (entity == null) {
            return;
        }
        entity.setTransformation(transformation);
    }
    
    @Override
    public void setInterpolationDuration(int interpolationDuration) {
        this.interpolationDuration = interpolationDuration;
        if (entity == null) {
            return;
        }
        entity.setInterpolationDuration(interpolationDuration);
    }
    
    @Override
    public void setTeleportDuration(int teleportDuration) {
        this.teleportDuration = teleportDuration;
        if (entity == null) {
            return;
        }
        entity.setTeleportDuration(teleportDuration);
    }
    
    public void setTranslation(@NotNull Vector3f translation) {
        if (entity == null) {
            return;
        }
        Transformation old = entity.getTransformation();
        setTransformation(new Transformation(
                translation,
                old.getLeftRotation(),
                old.getScale(),
                old.getRightRotation()
        ));
    }
    
    public void setScale(@NotNull Vector3f scale) {
        if (entity == null) {
            return;
        }
        Transformation old = entity.getTransformation();
        setTransformation(new Transformation(
                old.getTranslation(),
                old.getLeftRotation(),
                scale,
                old.getRightRotation()
        ));
    }
    
    @Override
    public boolean isGlowing() {
        return glowing;
    }
}

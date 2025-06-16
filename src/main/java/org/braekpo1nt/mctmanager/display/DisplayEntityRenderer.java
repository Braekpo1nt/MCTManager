package org.braekpo1nt.mctmanager.display;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class DisplayEntityRenderer<T extends Display> extends EntityRenderer<T> {
    
    public static final Transformation NO_TRANSFORMATION = new Transformation(
            new Vector3f(),
            new Quaternionf(),
            new Vector3f(1, 1, 1),
            new Quaternionf()
    );
    
    private @NotNull Color glowColor;
    private @NotNull Transformation transformation;
    private int interpolationDuration;
    private int teleportDuration;
    
    public DisplayEntityRenderer(
            @NotNull Location location, 
            boolean glowing,
            @NotNull Color glowColor,
            @NotNull Transformation transformation,
            int interpolationDuration,
            int teleportDuration) {
        super(location, glowing);
        this.glowColor = glowColor;
        this.transformation = transformation;
        this.interpolationDuration = interpolationDuration;
        this.teleportDuration = teleportDuration;
    }
    
    @Override
    protected void show(@NotNull T entity) {
        entity.setGlowColorOverride(glowColor);
        entity.setTransformation(transformation);
        entity.setInterpolationDuration(interpolationDuration);
        entity.setTeleportDuration(teleportDuration);
    }
    
    public void setGlowColor(@NotNull Color glowColor) {
        this.glowColor = glowColor;
        if (entity == null) {
            return;
        }
        entity.setGlowColorOverride(glowColor);
    }
    
    public void setTransformation(@NotNull Transformation transformation) {
        this.transformation = transformation;
        if (entity == null) {
            return;
        }
        entity.setTransformation(transformation);
    }
    
    public void setInterpolationDuration(int interpolationDuration) {
        this.interpolationDuration = interpolationDuration;
        if (entity == null) {
            return;
        }
        entity.setInterpolationDuration(interpolationDuration);
    }
    
    public void setTeleportDuration(int teleportDuration) {
        this.teleportDuration = teleportDuration;
        if (entity == null) {
            return;
        }
        entity.setTeleportDuration(teleportDuration);
    }
}

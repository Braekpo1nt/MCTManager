package org.braekpo1nt.mctmanager.display;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * For displaying {@link org.bukkit.Location}s
 */
public class LocationDisplay implements Display {
    
    
    private @NotNull Location location;
    private @NotNull Color glowColor;
    private @NotNull Material material;
    private boolean glowing;
    private @Nullable BlockDisplay blockDisplay;
    
    private boolean showing;
    
    public LocationDisplay(@NotNull Location location, @NotNull Color glowColor, @NotNull Material material) {
        this.location = location;
        this.glowColor = glowColor;
        this.material = material;
        this.showing = false;
        this.glowing = false;
    }
    
    public void setLocation(@NotNull Location location) {
        this.location = location;
        if (showing) {
            if (blockDisplay != null) {
                blockDisplay.setTransformation(new Transformation(
                        new Vector3f(), // no translation
                        new Quaternionf(), // no left rotation
                        new Vector3f(0.1f, 0.1f, 0.1f), // small
                        new Quaternionf() // no right rotation
                ));
            }
        }
    }
    
    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
        if (showing) {
            if (blockDisplay != null) {
                blockDisplay.setGlowing(glowing);
            }
        }
    }
    
    public void setGlowColor(@NotNull Color glowColor) {
        this.glowColor = glowColor;
        if (showing) {
            if (blockDisplay != null) {
                blockDisplay.setGlowColorOverride(glowColor);
            }
        }
    }
    
    public void setMaterial(@NotNull Material material) {
        this.material = material;
        if (showing) {
            if (blockDisplay != null) {
                blockDisplay.setBlock(material.createBlockData());
            }
        }
    }
    
    @Override
    public void show(@NotNull World world) {
        if (showing) {
            return;
        }
        showing = true;
        blockDisplay = world.spawn(location.toLocation(world), BlockDisplay.class, entity -> {
            entity.setBlock(material.createBlockData());
            entity.setTransformation(new Transformation(
                    new Vector3f(), // no translation
                    new Quaternionf(), // no left rotation
                    new Vector3f(0.1f, 0.1f, 0.1f), // small
                    new Quaternionf() // no right rotation
            ));
            entity.setInterpolationDelay(0);
            entity.setInterpolationDuration(10);
            entity.setGlowing(glowing);
            entity.setGlowColorOverride(glowColor);
        });
    }
    
    @Override
    public void hide() {
        if (!showing) {
            return;
        }
        showing = false;
        if (blockDisplay != null) {
            blockDisplay.remove();
            blockDisplay = null;
        }
    }
}

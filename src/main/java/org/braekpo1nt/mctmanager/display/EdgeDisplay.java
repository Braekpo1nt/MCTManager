package org.braekpo1nt.mctmanager.display;

import org.braekpo1nt.mctmanager.display.geometry.Edge;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EdgeDisplay implements Display {
    
    private @NotNull Edge edge;
    private @NotNull Color glowColor;
    private @NotNull Material material;
    private boolean glowing;
    private @Nullable BlockDisplay blockDisplay;
    
    private boolean showing;
    
    public EdgeDisplay(@NotNull Edge edge, @NotNull Color glowColor, @NotNull Material material) {
        this.edge = edge;
        this.glowColor = glowColor;
        this.material = material;
        this.showing = false;
        this.glowing = false;
    }
    
    public EdgeDisplay(@NotNull Edge edge, @NotNull Color glowColor) {
        this(edge, glowColor, Material.RED_WOOL);
    }
    
    public EdgeDisplay(@NotNull Edge edge) {
        this(edge, Color.RED, Material.RED_WOOL);
    }
    
    public void setEdge(@NotNull Edge edge) {
        this.edge = edge;
        if (showing) {
            if (blockDisplay != null) {
                Vector direction = edge.getB().clone().subtract(edge.getA());
                float length = (float) direction.length();
                Vector unitDirection = direction.clone().normalize();
                Vector3f scale = new Vector3f(0.05f, length, 0.05f);
                Vector3f defaultDirection = new Vector3f(0, 1, 0);
                Vector3f targetDirection = new Vector3f((float) unitDirection.getX(), (float) unitDirection.getY(), (float) unitDirection.getZ());
                Quaternionf rotationQuaternion = new Quaternionf().rotateTo(defaultDirection, targetDirection);
                blockDisplay.setTransformation(new Transformation(
                        new Vector3f(), // no translation
                        rotationQuaternion, // left rotation
                        scale, // long and thin
                        new Quaternionf() // no right rotation
                ));
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
    
    @Override
    public void show(@NotNull World world) {
        if (showing) {
            return;
        }
        showing = true;
        Vector direction = edge.getB().clone().subtract(edge.getA());
        float length = (float) direction.length();
        Vector unitDirection = direction.clone().normalize();
        Vector3f scale = new Vector3f(0.05f, length, 0.05f);
        Vector3f defaultDirection = new Vector3f(0, 1, 0);
        Vector3f targetDirection = new Vector3f((float) unitDirection.getX(), (float) unitDirection.getY(), (float) unitDirection.getZ());
        Quaternionf rotationQuaternion = new Quaternionf().rotateTo(defaultDirection, targetDirection);
        blockDisplay = world.spawn(edge.getA().toLocation(world), BlockDisplay.class, entity -> {
            entity.setBlock(material.createBlockData());
            entity.setTransformation(new Transformation(
                    new Vector3f(), // no translation
                    rotationQuaternion, // left rotation
                    scale, // long and thin
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

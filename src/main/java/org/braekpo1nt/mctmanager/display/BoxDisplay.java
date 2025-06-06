package org.braekpo1nt.mctmanager.display;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class BoxDisplay implements Display {
    
    private @NotNull BoundingBox boundingBox;
    private @NotNull Material material;
    private boolean glowing;
    private @NotNull Color glowColor;
    private @Nullable BlockDisplay normal;
    private @Nullable BlockDisplay inverted;
    
    private boolean showing;
    
    public BoxDisplay(@NotNull BoundingBox boundingBox, @NotNull Material material, boolean glowing, @NotNull Color glowColor) {
        this.boundingBox = boundingBox;
        this.material = material;
        this.glowing = glowing;
        this.glowColor = glowColor;
        this.showing = false;
    }
    
    public BoxDisplay(@NotNull BoundingBox boundingBox) {
        this(boundingBox, Material.GLASS, false, Color.WHITE);
    }
    
    public BoxDisplay(@NotNull BoundingBox boundingBox, Color glowColor) {
        this(boundingBox, Material.GLASS, false, glowColor);
    }
    
    public void setBoundingBox(@NotNull BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        if (showing) {
            if (normal != null) {
                normal.setTransformation(new Transformation(
                        new Vector3f(), // no translation
                        new AxisAngle4f(), // no left rotation
                        new Vector3f(
                                (float) boundingBox.getWidthX(),
                                (float) boundingBox.getHeight(),
                                (float) boundingBox.getWidthZ()),
                        new AxisAngle4f() // no right rotation
                ));
            }
            if (inverted != null) {
                inverted.setTransformation(new Transformation(
                        new Vector3f(), // no translation
                        new AxisAngle4f(), // no left rotation
                        new Vector3f(
                                (float) -boundingBox.getWidthX(), // inverted on the x-axis
                                (float) boundingBox.getHeight(),
                                (float) boundingBox.getWidthZ()),
                        new AxisAngle4f() // no right rotation
                ));
            }
        }
    }
    
    public void setMaterial(@NotNull Material material) {
        this.material = material;
        if (showing) {
            if (normal != null) {
                normal.setBlock(material.createBlockData());
            }
            if (inverted != null) {
                inverted.setBlock(material.createBlockData());
            }
        }
    }
    
    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
        if (showing) {
            if (normal != null) {
                normal.setGlowing(glowing);
            }
            if (inverted != null) {
                inverted.setGlowing(glowing);
            }
        }
    }
    
    public void setGlowColor(@NotNull Color glowColor) {
        this.glowColor = glowColor;
        if (showing) {
            if (normal != null) {
                normal.setGlowColorOverride(glowColor);
            }
            if (inverted != null) {
                inverted.setGlowColorOverride(glowColor);
            }
        }
    }
    
    @Override
    public void show(@NotNull World world) {
        if (showing) {
            return;
        }
        showing = true;
        normal = world.spawn(boundingBox.getMin().toLocation(world), BlockDisplay.class, entity -> {
            entity.setBlock(material.createBlockData());
            entity.setTransformation(new Transformation(
                    new Vector3f(), // no translation
                    new AxisAngle4f(), // no left rotation
                    new Vector3f(
                            (float) boundingBox.getWidthX(), 
                            (float) boundingBox.getHeight(),
                            (float) boundingBox.getWidthZ()),
                    new AxisAngle4f() // no right rotation
            ));
            entity.setInterpolationDelay(0);
            entity.setInterpolationDuration(10);
            entity.setGlowing(glowing);
            entity.setGlowColorOverride(glowColor);
        });
        inverted = world.spawn(boundingBox.getMin().add(new Vector(boundingBox.getWidthX(),0, 0)).toLocation(world), BlockDisplay.class, entity -> { // offset on the x-axis
            entity.setBlock(material.createBlockData());
            entity.setTransformation(new Transformation(
                    new Vector3f(), // no translation
                    new AxisAngle4f(), // no left rotation
                    new Vector3f(
                            (float) -boundingBox.getWidthX(), // inverted on the x-axis
                            (float) boundingBox.getHeight(),
                            (float) boundingBox.getWidthZ()),
                    new AxisAngle4f() // no right rotation
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
        this.showing = false;
        if (this.normal != null) {
            this.normal.remove();
            this.normal = null;
        }
        if (this.inverted != null) {
            this.inverted.remove();
            this.inverted = null;
        }
    }
}

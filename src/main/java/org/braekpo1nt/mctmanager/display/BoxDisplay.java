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
        this(boundingBox, Material.WHITE_STAINED_GLASS, true, Color.WHITE);
    }
    
    @Override
    public void addChild(@NotNull Display child) {
        
    }
    
    @Override
    public void show(@NotNull World world) {
        if (showing) {
            return;
        }
        this.showing = true;
        this.normal = world.spawn(boundingBox.getMin().toLocation(world), BlockDisplay.class, entity -> {
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
            entity.setGlowing(glowing);
            entity.setGlowColorOverride(glowColor);
        });
        this.inverted = world.spawn(boundingBox.getMin().add(new Vector(boundingBox.getWidthX(),0, 0)).toLocation(world), BlockDisplay.class, entity -> { // offset on the x-axis
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
        }
        if (this.inverted != null) {
            this.inverted.remove();
        }
    }
}

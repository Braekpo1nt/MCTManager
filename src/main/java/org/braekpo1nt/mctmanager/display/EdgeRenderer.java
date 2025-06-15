package org.braekpo1nt.mctmanager.display;

import lombok.Getter;
import org.braekpo1nt.mctmanager.display.geometry.Edge;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EdgeRenderer implements Renderer {
    
    private final @NotNull BlockDisplayEntityRenderer renderer;
    @Getter
    private @NotNull Location location;
    
    public EdgeRenderer(@NotNull World world, @NotNull Edge edge, @NotNull Material material) {
        this.location = edge.getA().toLocation(world);
        this.renderer = new BlockDisplayEntityRenderer(
                location, 
                edgeToTransformation(edge), 
                material.createBlockData()
        );
    }
    
    private @NotNull Transformation edgeToTransformation(@NotNull Edge e) {
        Vector direction = e.getB().clone().subtract(e.getA());
        float length = (float) direction.length();
        Vector unitDirection = direction.clone().normalize();
        Vector3f scale = new Vector3f(0.05f, length, 0.05f);
        Vector3f defaultDirection = new Vector3f(0, 1, 0);
        Vector3f targetDirection = new Vector3f((float) unitDirection.getX(), (float) unitDirection.getY(), (float) unitDirection.getZ());
        Quaternionf rotationQuaternion = new Quaternionf().rotateTo(defaultDirection, targetDirection);
        return new Transformation(
                new Vector3f(), // no translation
                rotationQuaternion, // left rotation
                scale, // long and thin
                new Quaternionf() // no right rotation
        );
    }
    
    public void setEdge(@NotNull Edge edge) {
        this.location = edge.getA().toLocation(location.getWorld());
        renderer.setLocation(location);
        renderer.setTransformation(edgeToTransformation(edge));
    }
    
    @Override
    public void show() {
        renderer.show();
    }
    
    @Override
    public void hide() {
        renderer.hide();
    }
}

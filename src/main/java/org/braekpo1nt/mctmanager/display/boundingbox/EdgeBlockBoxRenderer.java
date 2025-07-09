package org.braekpo1nt.mctmanager.display.boundingbox;

import lombok.Builder;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.delegates.DisplayDelegate;
import org.braekpo1nt.mctmanager.display.delegates.DisplaySingleton;
import org.braekpo1nt.mctmanager.display.delegates.HasBlockData;
import org.braekpo1nt.mctmanager.display.delegates.HasBlockDataSingleton;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A combination of {@link BlockBoxRenderer} and {@link EdgeBoxRenderer}, which
 * is designed to provide a glowing effect to the edges of a bounding box without
 * occluding other glowing effects behind and in front of the faces of the box.
 * When this renderer is glowing, the edges appear and glow. When not, the edge disappear. 
 * The faces are visible in both states.
 */
public class EdgeBlockBoxRenderer implements BoundingBoxRenderer, DisplaySingleton, HasBlockDataSingleton {
    
    private final @NotNull BlockBoxRenderer blockRenderer;
    private final @NotNull EdgeBoxRenderer edgeBoxRenderer;
    private boolean shown = false;
    private boolean glowing;
    
    @Builder
    public EdgeBlockBoxRenderer(
            @NotNull World world,
            @NotNull BoundingBox boundingBox,
            @Nullable Display.Brightness brightness,
            @Nullable Component customName,
            boolean customNameVisible,
            boolean glowing,
            @Nullable Color glowColor,
            int interpolationDuration,
            int teleportDuration,
            @Nullable BlockData blockData) {
        this.glowing = glowing;
        this.blockRenderer = BlockBoxRenderer.builder()
                .world(world)
                .boundingBox(boundingBox)
                .brightness(brightness)
                .customName(customName)
                .customNameVisible(customNameVisible)
                .glowing(false)
                .glowColor(glowColor)
                .interpolationDuration(interpolationDuration)
                .teleportDuration(teleportDuration)
                .blockData(blockData)
                .build();
        this.edgeBoxRenderer = EdgeBoxRenderer.builder()
                .world(world)
                .boundingBox(boundingBox)
                .brightness(brightness)
                .glowing(true)
                .glowColor(glowColor)
                .interpolationDuration(interpolationDuration)
                .teleportDuration(teleportDuration)
                .blockData(blockData)
                .build();
    }
    
    @Override
    public void setBoundingBox(@NotNull BoundingBox boundingBox) {
        blockRenderer.setBoundingBox(boundingBox);
        edgeBoxRenderer.setBoundingBox(boundingBox);
    }
    
    @Override
    public @NotNull BoundingBox getBoundingBox() {
        return blockRenderer.getBoundingBox();
    }
    
    @Override
    public @NotNull DisplayDelegate getDisplay() {
        return blockRenderer;
    }
    
    /**
     * true: Show the glowing edges
     * false: Hide the glowing edges
     * @param glowing whether to glow
     */
    @Override
    public void setGlowing(boolean glowing) {
        if (this.glowing == glowing) {
            return;
        }
        this.glowing = glowing;
        if (shown) {
            if (glowing) { // from not glowing to glowing
                edgeBoxRenderer.show();
            } else { // from glowing to not glowing
                edgeBoxRenderer.hide();
            }
        }
    }
    
    @Override
    public boolean isGlowing() {
        return glowing;
    }
    
    @Override
    public @NotNull Location getLocation() {
        return blockRenderer.getLocation();
    }
    
    @Override
    public void show() {
        shown = true;
        blockRenderer.show();
        if (glowing) {
            edgeBoxRenderer.show();
        }
    }
    
    @Override
    public void hide() {
        shown = false;
        blockRenderer.hide();
        if (glowing) {
            edgeBoxRenderer.hide();
        }
    }
    
    @Override
    public @NotNull HasBlockData getHasBlockData() {
        return blockRenderer;
    }
}

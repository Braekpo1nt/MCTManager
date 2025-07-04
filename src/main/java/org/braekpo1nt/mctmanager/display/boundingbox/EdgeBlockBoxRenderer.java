package org.braekpo1nt.mctmanager.display.boundingbox;

import lombok.Builder;
import org.braekpo1nt.mctmanager.display.delegates.BlockDisplayDelegate;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class EdgeBlockBoxRenderer implements BoundingBoxRenderer {
    
    private final @NotNull BlockBoxRenderer blockRenderer;
    private final @NotNull EdgeBoxRenderer edgeBoxRenderer;
    private boolean shown = false;
    private boolean glowing;
    
    @Builder
    public EdgeBlockBoxRenderer(
            @NotNull World world,
            @NotNull BoundingBox boundingBox,
            @Nullable Display.Brightness brightness,
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
                .glowing(glowing)
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
    public @NotNull Collection<? extends BlockDisplayDelegate> getRenderers() {
        return List.of(blockRenderer, edgeBoxRenderer);
    }
    
    @Override
    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
        edgeBoxRenderer.setGlowing(glowing);
        if (shown) {
            edgeBoxRenderer.show();
        }
    }
    
    @Override
    public boolean isGlowing() {
        return glowing;
    }
    
    @Override
    public @NotNull BlockDisplayDelegate getPrimaryRenderer() {
        return blockRenderer;
    }
    
    @Override
    public @NotNull Location getLocation() {
        return blockRenderer.getLocation();
    }
    
    @Override
    public void show() {
        shown = true;
        blockRenderer.show();
        if (isGlowing()) {
            edgeBoxRenderer.show();
        }
    }
    
    @Override
    public void hide() {
        shown = false;
        blockRenderer.hide();
        if (isGlowing()) {
            edgeBoxRenderer.hide();
        }
    }
}

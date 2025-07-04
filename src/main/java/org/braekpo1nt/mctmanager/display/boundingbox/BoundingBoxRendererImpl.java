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
import java.util.Collections;

/**
 * A Renderer for displaying BoundingBoxes, which can shift between multiple types of display
 * (e.g. wireframe, faces, inverted, etc.)
 */
public class BoundingBoxRendererImpl implements BoundingBoxRenderer {
    
    private @NotNull BoundingBoxRenderer state;
    private boolean shown;
    
    private @NotNull BoundingBox boundingBox;
    private final @NotNull World world;
    
    /**
     * Represents different types of {@link BoundingBoxRenderer} that this renderer can
     * appear as.
     */
    public enum Type {
        /**
         * {@link BlockBoxRenderer}
         */
        BLOCK,
        /**
         * {@link EdgeBoxRenderer}
         */
        EDGE,
        /**
         * {@link EdgeBlockBoxRenderer}
         */
        EDGE_BLOCK,
        /**
         * {@link RectBoxRenderer}
         */
        RECT;
        
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
    
    @Builder
    public BoundingBoxRendererImpl(
            @NotNull World world,
            @NotNull BoundingBox boundingBox,
            @Nullable Type type,
            @Nullable Display.Brightness brightness,
            boolean glowing,
            @Nullable Color glowColor,
            int interpolationDuration,
            int teleportDuration,
            @Nullable BlockData blockData) {
        this.world = world;
        this.boundingBox = boundingBox;
        this.state = createState(
                (type != null) ? type : Type.BLOCK,
                brightness,
                glowing,
                glowColor,
                interpolationDuration,
                teleportDuration,
                blockData);
    }
    
    private @NotNull BoundingBoxRenderer createState(
            @NotNull Type type,
            @Nullable Display.Brightness brightness,
            boolean glowing,
            @Nullable Color glowColor,
            int interpolationDuration,
            int teleportDuration,
            @Nullable BlockData blockData) {
        return switch (type) {
            case BLOCK -> BlockBoxRenderer.builder()
                    .world(world)
                    .boundingBox(boundingBox)
                    .brightness(brightness)
                    .glowing(glowing)
                    .glowColor(glowColor)
                    .interpolationDuration(interpolationDuration)
                    .teleportDuration(teleportDuration)
                    .blockData(blockData)
                    .build();
            case EDGE -> EdgeBoxRenderer.builder()
                    .world(world)
                    .boundingBox(boundingBox)
                    .brightness(brightness)
                    .glowing(glowing)
                    .glowColor(glowColor)
                    .interpolationDuration(interpolationDuration)
                    .teleportDuration(teleportDuration)
                    .blockData(blockData)
                    .build();
            case EDGE_BLOCK -> EdgeBlockBoxRenderer.builder()
                    .world(world)
                    .boundingBox(boundingBox)
                    .brightness(brightness)
                    .glowing(glowing)
                    .glowColor(glowColor)
                    .interpolationDuration(interpolationDuration)
                    .teleportDuration(teleportDuration)
                    .blockData(blockData)
                    .build();
            case RECT -> RectBoxRenderer.builder()
                    .world(world)
                    .boundingBox(boundingBox)
                    .brightness(brightness)
                    .glowing(glowing)
                    .glowColor(glowColor)
                    .interpolationDuration(interpolationDuration)
                    .teleportDuration(teleportDuration)
                    .blockData(blockData)
                    .build();
        };
    }
    
    public void setType(@NotNull Type type) {
        BoundingBoxRenderer oldState = this.state;
        BoundingBoxRenderer newState = createState(
                type,
                oldState.getBrightness(),
                oldState.isGlowing(),
                oldState.getGlowColor(),
                oldState.getInterpolationDuration(),
                oldState.getTeleportDuration(),
                oldState.getBlockData()
        );
        
        if (shown) {
            oldState.hide();
            newState.show();
        }
        this.state = newState;
    }
    
    @Override
    public @NotNull Location getLocation() {
        return state.getLocation();
    }
    
    @Override
    public void setBoundingBox(@NotNull BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        state.setBoundingBox(boundingBox);
    }
    
    @Override
    public @NotNull Collection<? extends BlockDisplayDelegate> getRenderers() {
        return Collections.singletonList(state);
    }
    
    @Override
    public @NotNull BlockDisplayDelegate getPrimaryRenderer() {
        return state;
    }
    
    @Override
    public void show() {
        BoundingBoxRenderer.super.show();
        shown = true;
    }
    
    @Override
    public void hide() {
        BoundingBoxRenderer.super.hide();
        shown = false;
    }
}

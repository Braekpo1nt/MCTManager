package org.braekpo1nt.mctmanager.display.boundingbox;

import lombok.Builder;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.TransientTextDisplayRenderer;
import org.braekpo1nt.mctmanager.display.delegates.DisplayDelegate;
import org.braekpo1nt.mctmanager.display.delegates.DisplaySingleton;
import org.braekpo1nt.mctmanager.display.delegates.HasBlockData;
import org.braekpo1nt.mctmanager.display.delegates.HasBlockDataSingleton;
import org.braekpo1nt.mctmanager.display.delegates.HasText;
import org.braekpo1nt.mctmanager.display.delegates.HasTextSingleton;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Renderer for displaying BoundingBoxes, which can shift between multiple types of display
 * (e.g. wireframe, faces, inverted, etc.)
 */
public class BoundingBoxRendererImpl implements BoundingBoxRenderer, DisplaySingleton, HasBlockDataSingleton, HasTextSingleton {
    
    private @NotNull BoundingBoxRenderer state;
    
    private @NotNull BoundingBox boundingBox;
    private final @NotNull TransientTextDisplayRenderer titleRenderer;
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
            @Nullable Component title,
            @Nullable Component customName,
            boolean customNameVisible,
            boolean glowing,
            @Nullable Color glowColor,
            int interpolationDuration,
            int teleportDuration,
            @Nullable BlockData blockData) {
        this.world = world;
        this.boundingBox = boundingBox.clone();
        this.state = createState(
                (type != null) ? type : Type.BLOCK,
                brightness,
                customName,
                customNameVisible,
                glowing,
                glowColor,
                interpolationDuration,
                teleportDuration,
                blockData);
        this.titleRenderer = TransientTextDisplayRenderer.builder()
                .location(titleLocation(boundingBox))
                .text(title)
                .billboard(Display.Billboard.CENTER)
                .brightness(brightness)
                .interpolationDuration(interpolationDuration)
                .teleportDuration(teleportDuration)
                .build();
    }
    
    private @NotNull BoundingBoxRenderer createState(
            @NotNull Type type,
            @Nullable Display.Brightness brightness,
            @Nullable Component customName,
            boolean customNameVisible,
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
                    .customName(customName)
                    .customNameVisible(customNameVisible)
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
                    .customName(customName)
                    .customNameVisible(customNameVisible)
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
                    .customName(customName)
                    .customNameVisible(customNameVisible)
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
                    .customName(customName)
                    .customNameVisible(customNameVisible)
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
                oldState.customName(),
                oldState.isCustomNameVisible(),
                oldState.isGlowing(),
                oldState.getGlowColor(),
                oldState.getInterpolationDuration(),
                oldState.getTeleportDuration(),
                oldState.getBlockData()
        );
        
        if (showing()) {
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
        titleRenderer.setLocation(titleLocation(boundingBox));
    }
    
    protected @NotNull Location titleLocation(@NotNull BoundingBox boundingBox) {
        return new Location(
                world,
                boundingBox.getCenterX(),
                boundingBox.getMaxY(),
                boundingBox.getCenterZ()
        );
    }
    
    public void setTitle(@Nullable Component title) {
        this.titleRenderer.setText(title);
    }
    
    @Override
    public @NotNull BoundingBox getBoundingBox() {
        return boundingBox;
    }
    
    @Override
    public @NotNull HasBlockData getHasBlockData() {
        return state;
    }
    
    
    @Override
    public @NotNull DisplayDelegate getDisplay() {
        return state;
    }
    
    @Override
    public @NotNull HasText getHasText() {
        return titleRenderer;
    }
    
}

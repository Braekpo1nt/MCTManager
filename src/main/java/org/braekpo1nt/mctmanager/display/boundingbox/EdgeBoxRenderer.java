package org.braekpo1nt.mctmanager.display.boundingbox;

import lombok.Builder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.EdgeRenderer;
import org.braekpo1nt.mctmanager.display.delegates.DisplayComposite;
import org.braekpo1nt.mctmanager.display.delegates.DisplayDelegate;
import org.braekpo1nt.mctmanager.display.delegates.HasBlockData;
import org.braekpo1nt.mctmanager.display.delegates.HasBlockDataComposite;
import org.braekpo1nt.mctmanager.display.geometry.Edge;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Displays the edges of a BoundingBox, not the faces
 */
public class EdgeBoxRenderer implements BoundingBoxRenderer, HasBlockDataComposite, DisplayComposite {
    
    @Getter
    private @NotNull Location location;
    @Getter
    private @NotNull BoundingBox boundingBox;
    private final List<EdgeRenderer> edgeRenderers;
    
    @Builder
    public EdgeBoxRenderer(
            @NotNull World world, 
            @NotNull BoundingBox boundingBox, 
            @Nullable Float strokeWidth,
            @Nullable Display.Brightness brightness,
            @Nullable Component customName,
            boolean customNameVisible,
            boolean glowing,
            @Nullable Color glowColor,
            int interpolationDuration,
            int teleportDuration,
            @Nullable BlockData blockData) {
        Objects.requireNonNull(world, "world can't be null");
        Objects.requireNonNull(boundingBox, "boundingBox can't be null");
        Vector origin = boundingBox.getMin();
        this.location = origin.toLocation(world);
        this.boundingBox = boundingBox.clone();
        List<Edge> edges = Edge.toEdges(boundingBox);
        this.edgeRenderers = edges.stream()
                .map(e -> EdgeRenderer.builder()
                        .world(world)
                        .edge(e)
                        .strokeWidth(strokeWidth)
                        .blockData(blockData)
                        .brightness(brightness)
                        .customName(customName)
                        .customNameVisible(customNameVisible)
                        .glowing(glowing)
                        .glowColor(glowColor)
                        .interpolationDuration(interpolationDuration)
                        .teleportDuration(teleportDuration)
                        .build()).toList();
    }
    
    @Override
    public void setBoundingBox(@NotNull BoundingBox boundingBox) {
        Vector origin = boundingBox.getMin();
        this.location = origin.toLocation(location.getWorld());
        this.boundingBox = boundingBox.clone();
        List<Edge> edges = Edge.toEdges(boundingBox);
        for (int i = 0; i < edgeRenderers.size(); i++) {
            EdgeRenderer edgeRenderer = edgeRenderers.get(i);
            Edge edge = edges.get(i);
            edgeRenderer.setEdge(edge);
        }
    }
    
    
    @Override
    public @NotNull Collection<? extends DisplayDelegate> getDisplays() {
        return edgeRenderers;
    }
    
    @Override
    public @NotNull DisplayDelegate getDisplay() {
        return edgeRenderers.getFirst();
    }
    
    @Override
    public @NotNull Collection<? extends HasBlockData> getHasBlockDatas() {
        return edgeRenderers;
    }
    
    @Override
    public @NotNull HasBlockData getHasBlockData() {
        return edgeRenderers.getFirst();
    }
}

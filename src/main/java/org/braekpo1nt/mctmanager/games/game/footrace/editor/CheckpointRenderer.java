package org.braekpo1nt.mctmanager.games.game.footrace.editor;

import lombok.Builder;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.display.EdgeRenderer;
import org.braekpo1nt.mctmanager.display.TransientTextDisplayRenderer;
import org.braekpo1nt.mctmanager.display.boundingbox.BoundingBoxRendererImpl;
import org.braekpo1nt.mctmanager.display.delegates.DisplayComposite;
import org.braekpo1nt.mctmanager.display.delegates.DisplayDelegate;
import org.braekpo1nt.mctmanager.display.delegates.HasText;
import org.braekpo1nt.mctmanager.display.delegates.HasTextSingleton;
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

public class CheckpointRenderer implements DisplayComposite, HasTextSingleton {
    private @NotNull BoundingBox detectionArea;
    private final double length;
    private final BoundingBoxRendererImpl detectionAreaRenderer;
    private final EdgeRenderer directionRenderer;
    private final TransientTextDisplayRenderer title; // TODO: BoundingBoxRendererImpl has a transient title, so this is not needed
    
    @Builder
    public CheckpointRenderer(
            @NotNull World world,
            @NotNull BoundingBox checkpoint,
            @NotNull Vector direction,
            @Nullable Double directionLength,
            @NotNull BoundingBoxRendererImpl.Type type,
            @NotNull Color glowColor,
            int interpolationDuration,
            int teleportDuration,
            @Nullable Display.Billboard titleBillboard,
            @Nullable Component title,
            @NotNull BlockData detectionAreaBlock,
            @NotNull BlockData directionBlock) {
        this.detectionArea = checkpoint.clone();
        this.length = (directionLength != null) ? directionLength : 2;
        this.detectionAreaRenderer = BoundingBoxRendererImpl.builder()
                .world(world)
                .boundingBox(checkpoint)
                .blockData(detectionAreaBlock)
                .type(type)
                .glowColor(glowColor)
                .interpolationDuration(interpolationDuration)
                .teleportDuration(teleportDuration)
                .build();
        this.directionRenderer = EdgeRenderer.builder()
                .world(world)
                .edge(new Edge(
                        checkpoint.getCenter(), 
                        checkpoint.getCenter()
                                .add(direction.clone()
                                        .normalize()
                                        .multiply(this.length))))
                .blockData(directionBlock)
                .glowColor(glowColor)
                .interpolationDuration(interpolationDuration)
                .teleportDuration(teleportDuration)
                .build();
        this.title = TransientTextDisplayRenderer.builder()
                .location(detectionArea.getCenter().toLocation(world))
                .text(title)
                .billboard(titleBillboard)
                .glowColor(glowColor)
                .interpolationDuration(interpolationDuration)
                .teleportDuration(teleportDuration)
                .build();
    }
    
    @Override
    public @NotNull Collection<? extends DisplayDelegate> getDisplays() {
        return List.of(detectionAreaRenderer, directionRenderer, title);
    }
    
    @Override
    public @NotNull DisplayDelegate getDisplay() {
        return detectionAreaRenderer;
    }
    
    @Override
    public @NotNull HasText getHasText() {
        return title;
    }
    
    public void setTitle(@Nullable Component text) {
        title.setText(text);
    }
    
    @Override
    public @NotNull Location getLocation() {
        return detectionAreaRenderer.getLocation();
    }
    
    public void setDetectionArea(@NotNull BoundingBox boundingBox, @NotNull BoundingBox toPointTo) {
        setDetectionArea(boundingBox, FootRaceEditor.getDirection(boundingBox, toPointTo));
    }
    
    public void setDetectionArea(@NotNull BoundingBox boundingBox, @NotNull Vector direction) {
        this.detectionArea = boundingBox.clone();
        this.detectionAreaRenderer.setBoundingBox(detectionArea);
        this.setDirection(direction);
        this.title.setLocation(
                detectionArea.getCenter().toLocation(
                        detectionAreaRenderer.getLocation().getWorld()));
    }
    
    public void pointTo(@NotNull BoundingBox checkpoint) {
        Vector direction = FootRaceEditor.getDirection(detectionArea, checkpoint);
        setDirection(direction);
    }
    
    public void setTitleBillboard(@NotNull Display.Billboard titleBillboard) {
        title.setBillboard(titleBillboard);
    }
    
    public void setDirection(Vector direction) {
        Edge edge = new Edge(
                detectionArea.getCenter(),
                detectionArea.getCenter()
                        .add(direction.clone()
                                .normalize()
                                .multiply(length)));
        this.directionRenderer.setEdge(edge);
    }
    
    public void setType(BoundingBoxRendererImpl.Type type) {
        detectionAreaRenderer.setType(type);
    }
}

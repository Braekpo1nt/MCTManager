package org.braekpo1nt.mctmanager.commands.utils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EnumArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierCommand;
import org.braekpo1nt.mctmanager.display.EdgeRenderer;
import org.braekpo1nt.mctmanager.display.RectangleRenderer;
import org.braekpo1nt.mctmanager.display.boundingbox.BoundingBoxRendererImpl;
import org.braekpo1nt.mctmanager.display.geometry.Edge;
import org.braekpo1nt.mctmanager.display.geometry.rectangle.Rectangle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UtilsDebugCommand implements BrigadierCommand {
    
    private @Nullable BoundingBoxRendererImpl boxRenderer;
    private @Nullable RectangleRenderer rectangleRenderer;
    private @Nullable EdgeRenderer edgeRenderer;
    
    private final @NotNull Main plugin;
    
    public UtilsDebugCommand(@NotNull Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("ctdebug")
                .requires(sender -> sender.getSender().isOp())
                .then(Commands.literal("freeRect")
                        .then(Commands.argument("edge1", ArgumentTypes.finePosition())
                                .then(Commands.argument("edge2", ArgumentTypes.finePosition())
                                        .executes(ctx -> {
                                            Location location = ctx.getSource().getExecutor().getLocation();
                                            final Vector edge1 = ctx.getArgument("edge1", FinePositionResolver.class).resolve(ctx.getSource()).toVector();
                                            
                                            final Vector edge2 = ctx.getArgument("edge2", FinePositionResolver.class).resolve(ctx.getSource()).toVector();
                                            
                                            RectangleRenderer renderer = RectangleRenderer.builder()
                                                    .rectangle(Rectangle.of(location.toVector(), edge1, edge2))
                                                    .blockData(Material.LIME_STAINED_GLASS.createBlockData())
                                                    .build();
                                            renderer.show();
                                            plugin.getServer().getScheduler().runTaskLater(plugin, renderer::hide, 5 * 20L);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("rect")
                        .then(Commands.literal("reset")
                                .executes(ctx -> {
                                    if (rectangleRenderer != null) {
                                        rectangleRenderer.hide();
                                        rectangleRenderer = null;
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.argument("corner1", ArgumentTypes.blockPosition())
                                .then(Commands.argument("corner2", ArgumentTypes.blockPosition())
                                        .executes(this::rect))))
                .then(Commands.literal("rectbox")
                        .then(Commands.literal("reset")
                                .executes(ctx -> {
                                    if (boxRenderer != null) {
                                        boxRenderer.hide();
                                        boxRenderer = null;
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("settype")
                                .then(Commands.argument("type", new EnumArgumentType<>(BoundingBoxRendererImpl.Type.class, BoundingBoxRendererImpl.Type.values()))
                                        .executes(ctx -> {
                                            if (boxRenderer != null) {
                                                BoundingBoxRendererImpl.Type type = ctx.getArgument("type", BoundingBoxRendererImpl.Type.class);
                                                boxRenderer.setType(type);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(Commands.literal("glowing")
                                .then(Commands.argument("glowing", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            boolean glowing = BoolArgumentType.getBool(ctx, "glowing");
                                            if (boxRenderer != null) {
                                                boxRenderer.setGlowing(glowing);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(Commands.argument("corner1", ArgumentTypes.blockPosition())
                                .then(Commands.argument("corner2", ArgumentTypes.blockPosition())
                                        .executes(ctx -> rectBox(ctx, null))
                                        .then(Commands.argument("blockLight", IntegerArgumentType.integer(0, 15))
                                                .then(Commands.argument("skyLight", IntegerArgumentType.integer(0, 15))
                                                        .executes(ctx -> {
                                                            final int blockLight = IntegerArgumentType.getInteger(ctx, "blockLight");
                                                            final int skyLight = IntegerArgumentType.getInteger(ctx, "skyLight");
                                                            Display.Brightness brightness = new Display.Brightness(blockLight, skyLight);
                                                            return rectBox(ctx, brightness);
                                                        }))))))
                .then(Commands.literal("edge")
                        .then(Commands.literal("reset")
                                .executes(ctx -> {
                                    if (edgeRenderer != null) {
                                        edgeRenderer.hide();
                                        edgeRenderer = null;
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.argument("from", ArgumentTypes.blockPosition())
                                .then(Commands.argument("to", ArgumentTypes.blockPosition())
                                        .executes(ctx -> {
                                            Vector from = ctx.getArgument("from", BlockPositionResolver.class).resolve(ctx.getSource()).toVector();
                                            Vector to = ctx.getArgument("to", BlockPositionResolver.class).resolve(ctx.getSource()).toVector();
                                            edge(ctx.getSource().getLocation().getWorld(), new Edge(from, to), 0.05f);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(Commands.argument("stroke", FloatArgumentType.floatArg())
                                                .executes(ctx -> {
                                                    Vector from = ctx.getArgument("from", BlockPositionResolver.class).resolve(ctx.getSource()).toVector();
                                                    Vector to = ctx.getArgument("to", BlockPositionResolver.class).resolve(ctx.getSource()).toVector();
                                                    float stroke = FloatArgumentType.getFloat(ctx, "stroke");
                                                    edge(ctx.getSource().getLocation().getWorld(), new Edge(from, to), stroke);
                                                    return Command.SINGLE_SUCCESS;
                                                })))))
                .build();
    }
    
    public void edge(@NotNull World world, @NotNull Edge edge, float strokeWidth) {
        if (edgeRenderer == null) {
            edgeRenderer = EdgeRenderer.builder()
                    .world(world)
                    .edge(edge)
                    .strokeWidth(strokeWidth)
                    .blockData(Material.LIGHT_BLUE_STAINED_GLASS.createBlockData())
                    .build();
            edgeRenderer.show();
        } else {
            edgeRenderer.setEdge(edge);
            edgeRenderer.setStrokeWidth(strokeWidth);
        }
    }
    
    public int rect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final Vector corner1 = ctx.getArgument("corner1", BlockPositionResolver.class).resolve(ctx.getSource()).toVector();
        final Vector corner2 = ctx.getArgument("corner2", BlockPositionResolver.class).resolve(ctx.getSource()).toVector();
        Rectangle rectangle = Rectangle.of(
                corner1.getX(),
                corner1.getY(),
                corner1.getZ(),
                corner2.getX(),
                corner2.getY(),
                corner2.getZ()
        );
        if (rectangleRenderer == null) {
            rectangleRenderer = RectangleRenderer.builder()
                    .world(ctx.getSource().getLocation().getWorld())
                    .rectangle(rectangle)
                    .blockData(Material.LIGHT_BLUE_STAINED_GLASS.createBlockData())
                    .build();
            rectangleRenderer.show();
        } else {
            rectangleRenderer.setRectangle(rectangle);
        }
        return Command.SINGLE_SUCCESS;
    }
    
    public int rectBox(CommandContext<CommandSourceStack> ctx, @Nullable Display.Brightness brightness) throws CommandSyntaxException {
        final Vector corner1 = ctx.getArgument("corner1", BlockPositionResolver.class).resolve(ctx.getSource()).toVector();
        final Vector corner2 = ctx.getArgument("corner2", BlockPositionResolver.class).resolve(ctx.getSource()).toVector();
        BoundingBox boundingBox = new BoundingBox(
                corner1.getX(),
                corner1.getY(),
                corner1.getZ(),
                corner2.getX(),
                corner2.getY(),
                corner2.getZ()
        );
        if (boxRenderer == null) {
            boxRenderer = BoundingBoxRendererImpl.builder()
                    .world(ctx.getSource().getLocation().getWorld())
                    .boundingBox(boundingBox)
                    .blockData(Material.LIME_STAINED_GLASS.createBlockData())
                    .brightness(brightness)
                    .customName(Component.text("rectbox"))
                    .customNameVisible(true)
                    .build();
            boxRenderer.show();
        } else {
            boxRenderer.setBoundingBox(boundingBox);
            boxRenderer.setBrightness(brightness);
        }
        return Command.SINGLE_SUCCESS;
    }
    
    public void cleanup() {
        if (boxRenderer != null) {
            boxRenderer.hide();
            boxRenderer = null;
        }
        if (rectangleRenderer != null) {
            rectangleRenderer.hide();
            rectangleRenderer = null;
        }
        if (edgeRenderer != null) {
            edgeRenderer.hide();
            edgeRenderer = null;
        }
    }
}

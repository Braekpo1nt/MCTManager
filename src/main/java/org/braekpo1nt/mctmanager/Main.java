package org.braekpo1nt.mctmanager;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.math.FinePosition;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.extern.java.Log;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EnumResolver;
import org.braekpo1nt.mctmanager.commands.dynamic.top.TopCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.FailureCommandResult;
import org.braekpo1nt.mctmanager.commands.mct.MCTCommand;
import org.braekpo1nt.mctmanager.commands.mctdebug.MCTDebugCommand;
import org.braekpo1nt.mctmanager.commands.readyup.ReadyUpCommand;
import org.braekpo1nt.mctmanager.commands.readyup.UnReadyCommand;
import org.braekpo1nt.mctmanager.commands.teammsg.TeamMsgCommand;
import org.braekpo1nt.mctmanager.commands.utils.UtilsCommand;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.display.EdgeRenderer;
import org.braekpo1nt.mctmanager.display.RectangleRenderer;
import org.braekpo1nt.mctmanager.display.boundingbox.BoundingBoxRendererImpl;
import org.braekpo1nt.mctmanager.display.geometry.Edge;
import org.braekpo1nt.mctmanager.display.geometry.rectangle.Rectangle;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.hub.config.HubConfigController;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Log
public class Main extends JavaPlugin {

    public static final List<String> VALID_CONFIG_VERSIONS = List.of("0.1.0", "0.1.1", "0.1.2");
    
    /**
     * A default Gson instance for general use
     */
    public static final Gson GSON = new Gson();
    /**
     * A pretty printing Gson instance for general use
     */
    public static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();
    private GameManager gameManager;
    private boolean saveGameStateOnDisable = true;
    public final static PotionEffect NIGHT_VISION = new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 3, true, false, false);
    private MCTCommand mctCommand;
    /**
     * This should be the application-wide logger used to print logs to the console or standard out. 
     * Initialized to Lombok log value so that tests don't trigger NullPointerExceptions
     */
    private static Logger logger = log;
    private static final Map<LogType, @NotNull Boolean> logTypeActive = new HashMap<>();
    
    protected GameManager initialGameManager(Scoreboard mctScoreboard, @NotNull HubConfig config) {
        return new GameManager(
                this, 
                mctScoreboard,
                new GameStateStorageUtil(this),
                new SidebarFactory(),
                config);
    }
    
    /**
     * @return the plugin's logger
     */
    public static Logger logger() {
        return Main.logger;
    }
    
    /**
     * Use the plugin's logger to send the log message at the info level
     * @param message the message with {@link String#format(String, Object...)} style patterns
     * @param args the args to {@link String#format(String, Object...)}
     */
    public static void logf(String message, Object... args) {
        logger().info(String.format(message, args));
    }
    
    public static void setLogTypeActive(@NotNull LogType logType, boolean active) {
        logTypeActive.put(logType, active);
    }
    
    /**
     * Logs the message if the given {@link LogType} should be logged (as determined by {@link #logTypeActive})
     * @param logType the {@link LogType} of the message
     * @param message the message to log
     * @see #setLogTypeActive(LogType, boolean)
     */
    public static void debugLog(@NotNull LogType logType, @NotNull String message) {
        if (logTypeActive.getOrDefault(logType, false)) {
            Main.logger().info(message);
        }
    }
    
    /**
     * Logs the message if the given {@link LogType} should be logged (as determined by {@link #logTypeActive})
     * @param logType the {@link LogType} of the message
     * @param message the message to log. 
     *                Must be a valid {@link Logger#log(Level, String, Object[])} string. 
     *                The provided args will be used as the {code Object...}
     *                arguments of the format string.
     * @param args the args the arguments of the {@link Logger#log(Level, String, Object[])} which 
     *             uses the message as the pattern.
     */
    public static void debugLog(@NotNull LogType logType, @NotNull String message, Object... args) {
        if (logTypeActive.get(logType)) {
            Main.logger.log(Level.INFO, message, args);
        }
    }
    
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }
    
    @Override
    public void onEnable() {
        Main.logger = this.getLogger();
        Scoreboard mctScoreboard = this.getServer().getScoreboardManager().getNewScoreboard();
        ParticipantInitializer.setPlugin(this); //TODO: remove this in favor of death and respawn combination 
        
        PacketEvents.getAPI().init();
        
        HubConfig config;
        try {
            config = new HubConfigController(getDataFolder()).getConfig();
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Could not load hub config, using default config. See console for details. %s", e.getMessage()), e);
            config = new HubConfigController(getDataFolder()).getDefaultConfig();
        }
        gameManager = initialGameManager(mctScoreboard, config);
        CommandResult result = gameManager.loadGameState();
        if (result instanceof FailureCommandResult) {
            getServer().getConsoleSender().sendMessage(result.getMessageOrEmpty());
            Main.logger().severe("[MCTManager] Could not load game state from memory. Disabling plugin.");
            saveGameStateOnDisable = false;
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Listeners
        BlockEffectsListener blockEffectsListener = new BlockEffectsListener(this);
        
        // Commands
        new MCTDebugCommand(this, gameManager);
        mctCommand = new MCTCommand(this, gameManager, blockEffectsListener);
        new UtilsCommand(this);
        new ReadyUpCommand(this, gameManager);
        new UnReadyCommand(this, gameManager);
        new TopCommand(this, gameManager);
        new TeamMsgCommand(this, gameManager);
        
        registerCommands();
        
        alwaysGiveNightVision();
    }
    
    protected void registerCommands() {
        LiteralCommandNode<CommandSourceStack> ctDebugCommand = Commands.literal("ctdebug")
                .then(Commands.literal("custommodel")
                        .executes(ctx -> {
                            if (!(ctx.getSource().getExecutor() instanceof Player player)) {
                                ctx.getSource().getSender().sendMessage("Must be a player to run this command");
                                return Command.SINGLE_SUCCESS;
                            }
                            givePlayerCustomModelItem(player, new ItemStack(Material.SNOWBALL), "playerswapball");
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.argument("item", ArgumentTypes.itemStack())
                                .then(Commands.argument("modelstring", StringArgumentType.word())
                                        .executes(ctx -> {
                                            if (!(ctx.getSource().getExecutor() instanceof Player player)) {
                                                ctx.getSource().getSender().sendMessage("Must be a player to run this command");
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            ItemStack itemStack = ctx.getArgument("item", ItemStack.class);
                                            String modelstring = ctx.getArgument("modelstring", String.class);
                                            givePlayerCustomModelItem(player, itemStack, modelstring);
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal("elytra")
                        .then(Commands.argument("location", ArgumentTypes.blockPosition())
                                .executes(ctx -> {
                                    Location location = ctx.getArgument("location", BlockPositionResolver.class)
                                            .resolve(ctx.getSource())
                                            .toLocation(ctx.getSource().getLocation().getWorld());
                                    if (!(ctx.getSource().getExecutor() instanceof Player player)) {
                                        ctx.getSource().getSender().sendMessage("Must be a player to run this command");
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    player.teleport(location);
                                    player.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
                                    this.getServer().getScheduler().runTaskLater(this, () -> {
                                        player.setGliding(true);
                                        player.sendMessage("You are flying");
                                    }, 10L);
                                    return Command.SINGLE_SUCCESS;
                                })))
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
                                            this.getServer().getScheduler().runTaskLater(this, renderer::hide, 5*20L);
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
                                .then(Commands.argument("type", new EnumResolver<>(BoundingBoxRendererImpl.Type.class, BoundingBoxRendererImpl.Type.values()))
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
                .then(Commands.literal("enumtest")
                        .then(Commands.argument("value", new EnumResolver<>(BlockFace.class, BlockFace.values()))
                                .executes(ctx -> {
                                    BlockFace value = ctx.getArgument("value", BlockFace.class);
                                    ctx.getSource().getSender().sendMessage(Component.empty()
                                            .append(Component.text("You chose "))
                                            .append(Component.text(value.toString())));
                                    return Command.SINGLE_SUCCESS;
                                })))
                .build();
        
        // Brigadier commands
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(ctDebugCommand);
        });
    }
    
    private static void givePlayerCustomModelItem(Player player, ItemStack itemStack, String customModelString) {
        itemStack.editMeta(meta -> {
            CustomModelDataComponent customModelDataComponent = meta.getCustomModelDataComponent();
            List<String> newStrings = new ArrayList<>(customModelDataComponent.getStrings());
            newStrings.add(customModelString);
            customModelDataComponent.setStrings(newStrings);
            meta.setCustomModelDataComponent(customModelDataComponent);
        });
        player.getInventory().addItem(itemStack);
    }
    
    private @Nullable BoundingBoxRendererImpl boxRenderer;
    private @Nullable RectangleRenderer rectangleRenderer;
    private @Nullable EdgeRenderer edgeRenderer;
    
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
    
    public MCTCommand getMctCommand() {
        return mctCommand;
    }
    
    private void alwaysGiveNightVision() {
        Main.logger().info("[MCTManager] Night vision activated");
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.addPotionEffect(NIGHT_VISION);
                }
            }
        }.runTaskTimer(this, 0L, 60L);
    }
    
    @Override
    public void onDisable() {
        ParticipantInitializer.setPlugin(null); //TODO: remove this in favor of death and respawn combination 
        PacketEvents.getAPI().terminate();
        if (gameManager != null) {
            if (gameManager.eventIsActive()) {
                gameManager.stopEvent();
            }
            gameManager.stopAllGames();
            if (gameManager.editorIsRunning()) {
                gameManager.stopEditor();
            }
            if (saveGameStateOnDisable) {
                gameManager.saveGameState();
            }
            gameManager.cleanup();
        } else {
            Main.logger().info("[MCTManager] Skipping save game state.");
        }
        gameManager = null;
        mctCommand = null;
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
        logTypeActive.clear();
    }
    
    // Testing methods for mocking components
    
    public GameManager getGameManager() {
        return gameManager;
    }
}

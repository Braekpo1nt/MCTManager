package org.braekpo1nt.mctmanager.games.game.footrace.editor;

import io.papermc.paper.entity.LookAnchor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.display.LocationRenderer;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.braekpo1nt.mctmanager.display.TitledRenderer;
import org.braekpo1nt.mctmanager.display.boundingbox.BlockBoxRenderer;
import org.braekpo1nt.mctmanager.display.boundingbox.BoundingBoxRendererImpl;
import org.braekpo1nt.mctmanager.games.editor.EditorBase;
import org.braekpo1nt.mctmanager.games.editor.wand.Wand;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfig;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfigController;
import org.braekpo1nt.mctmanager.games.game.footrace.editor.states.EditingState;
import org.braekpo1nt.mctmanager.games.game.footrace.editor.states.FootRaceEditorState;
import org.braekpo1nt.mctmanager.games.game.footrace.editor.states.InitialState;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class FootRaceEditor extends EditorBase<FootRaceAdmin, FootRaceEditorState> {
    
    
    public static final Material CHECKPOINT_BLOCK = Material.RED_STAINED_GLASS;
    public static final Material DIRECTION_BLOCK = Material.PINK_STAINED_GLASS;
    private final Color CHECKPOINT_GLOW = Color.RED;
    
    private final FootRaceConfigController configController;
    private @NotNull FootRaceConfig config;
    private final List<CheckpointRenderer> checkpointRenderers;
    private final TitledRenderer<LocationRenderer> startingLocationRenderer;
    private final TitledRenderer<BlockBoxRenderer> glassBarrierRenderer;
    
    private int checkpointType = 0;
    private BoundingBoxRendererImpl.Type[] availableTypes = new BoundingBoxRendererImpl.Type[]{
            BoundingBoxRendererImpl.Type.EDGE_BLOCK,
            BoundingBoxRendererImpl.Type.EDGE
    };
    
    public FootRaceEditor(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull FootRaceConfig config,
            @NotNull String configFile,
            @NotNull Collection<Player> newAdmins) {
        super(GameType.FOOT_RACE, configFile, plugin, gameManager, new InitialState());
        this.configController = new FootRaceConfigController(plugin.getDataFolder(), getType().getId());
        this.config = config;
        this.startingLocationRenderer = new TitledRenderer<>(
                LocationRenderer.builder()
                        .location(config.getStartingLocation())
                        .blockData(Material.LIME_WOOL.createBlockData())
                        .interpolationDuration(1)
                        .teleportDuration(1)
                        .build(),
                Component.text("Starting Location"),
                r -> r.getLocation().add(new Vector(0, 0.5, 0))
        );
        this.glassBarrierRenderer = new TitledRenderer<>(
                BlockBoxRenderer.builder()
                        .world(config.getWorld())
                        .boundingBox(config.getGlassBarrier())
                        .blockData(Material.BLACK_STAINED_GLASS.createBlockData())
                        .interpolationDuration(1)
                        .teleportDuration(1)
                        .build(),
                Component.text("Glass Barrier"),
                r -> r.getBoundingBox().getCenter().toLocation(r.getLocation().getWorld())
        );
        this.checkpointRenderers = createCheckpointRenderers(config);
        startingLocationRenderer.show();
        glassBarrierRenderer.show();
        checkpointRenderers.forEach(Renderer::show);
        addWand(Wand.<FootRaceAdmin>builder().wandItem(Wand.createWandItem(CHECKPOINT_BLOCK, "Checkpoint", List.of(
                        Component.text("Left Click: push box face away"),
                        Component.text("Right Click: pull box face toward"),
                        Component.text("(Crouch to adjust by 0.5 blocks)")
                )))
                .onLeftClick((event, admin) -> editCheckpoint(admin, 1.0))
                .onLeftSneakClick((event, admin) -> editCheckpoint(admin, 0.5))
                .onRightClick((event, admin) -> editCheckpoint(admin, -1.0))
                .onRightSneakClick((event, admin) -> editCheckpoint(admin, -0.5))
                .build());
        addWand(Wand.<FootRaceAdmin>builder().wandItem(Wand.createWandItem(CHECKPOINT_BLOCK, "Checkpoint Select", List.of(
                        Component.text("Left Click: previous checkpoint"),
                        Component.text("Right Click: next checkpoint"),
                        Component.text("(Crouch to be teleported)")
                )))
                .onLeftClick((event, admin) -> selectCheckpoint(admin, admin.getCurrentCheckpoint() + 1, false))
                .onLeftSneakClick((event, admin) -> selectCheckpoint(admin, admin.getCurrentCheckpoint() + 1, true))
                .onRightClick((event, admin) -> selectCheckpoint(admin, admin.getCurrentCheckpoint() - 1, false))
                .onRightSneakClick((event, admin) -> selectCheckpoint(admin, admin.getCurrentCheckpoint() - 1, true))
                .build());
        addWand(Wand.<FootRaceAdmin>builder().wandItem(Wand.createWandItem(CHECKPOINT_BLOCK, "Add/Remove Checkpoint", List.of(
                        Component.text("Left Click: add checkpoint"),
                        Component.text("Right Click: remove checkpoint")
                )))
                .onLeftClick(((event, admin) -> {
                    int newCheckpointIndex = admin.getCurrentCheckpoint() + 1;
                    BoundingBox newCheckpoint = createCheckpoint(admin.getPlayer().getLocation());
                    BoundingBox nextCheckpoint = config.getCheckpoints().get(newCheckpointIndex); // the checkpoint that will be next is currently at the new index
                    Vector direction = nextCheckpoint.getCenter().subtract(newCheckpoint.getCenter());
                    CheckpointRenderer newCheckpointRenderer = CheckpointRenderer.builder()
                            .world(config.getWorld())
                            .checkpoint(newCheckpoint)
                            .direction(direction)
                            .detectionAreaBlock(CHECKPOINT_BLOCK.createBlockData())
                            .directionBlock(DIRECTION_BLOCK.createBlockData())
                            .type(availableTypes[checkpointType])
                            .glowColor(CHECKPOINT_GLOW)
                            .interpolationDuration(1)
                            .teleportDuration(1)
                            .titleBillboard(Display.Billboard.VERTICAL)
                            .build();
                    newCheckpointRenderer.show();
                    config.getCheckpoints().add(newCheckpointIndex, newCheckpoint);
                    checkpointRenderers.add(newCheckpointIndex, newCheckpointRenderer);
                    
                    // change direction of previous checkpoint to point to newly added one
                    int toAdjustIndex = wrapIndex(newCheckpointIndex - 1);
                    CheckpointRenderer toAdjustRenderer = checkpointRenderers.get(toAdjustIndex);
                    toAdjustRenderer.pointTo(newCheckpoint);
                    
                    CommandResult selectResult = selectCheckpoint(admin, newCheckpointIndex, false);
                    return new CompositeCommandResult(
                            selectResult,
                            CommandResult.success(Component.empty()
                                    .append(Component.text("Add checkpoint index "))
                                    .append(Component.text(newCheckpointIndex))
                                    .append(Component.text("/"))
                                    .append(Component.text(config.getCheckpoints().size() - 1)) //TODO: is this correct numbers?
                                    .append(Component.text(". Max index is now "))
                                    .append(Component.text(config.getCheckpoints().size() - 1)))
                    );
                }))
                .onRightClick((event, admin) -> {
                    if (config.getCheckpoints().size() <= 3) {
                        return CommandResult.failure(Component.text("There must be at least 3 checkpoints"));
                    }
                    int removedIndex = admin.getCurrentCheckpoint();
                    boolean isLast = removedIndex == config.getCheckpoints().size() - 1;
                    config.getCheckpoints().remove(removedIndex);
                    checkpointRenderers.remove(removedIndex).hide();
                    CommandResult selectResult = selectCheckpoint(admin, Math.max(removedIndex - 1, 0), false);
                    
                    // adjust the direction of the checkpoint before the removed one
                    CheckpointRenderer toAdjustRenderer = checkpointRenderers.get(admin.getCurrentCheckpoint());
                    BoundingBox toPointTo = config.getCheckpoints().get(wrapIndex(admin.getCurrentCheckpoint() + 1));
                    toAdjustRenderer.pointTo(toPointTo);
                    
                    if (removedIndex == 0) {
                        checkpointRenderers.getFirst().setTitle(Component.text("Start Line"));
                    } else if (isLast) {
                        checkpointRenderers.getLast().setTitle(Component.text("Finish Line"));
                    }
                    
                    return new CompositeCommandResult(
                            selectResult,
                            CommandResult.success(Component.empty()
                                    .append(Component.text("Remove puzzle index "))
                                    .append(Component.text(removedIndex))
                                    .append(Component.text("/"))
                                    .append(Component.text(config.getCheckpoints().size() - 1))
                                    .append(Component.text(". Max index is now "))
                                    .append(Component.text(config.getCheckpoints().size() - 1)))
                    );
                })
                .build());
        addWand(Wand.<FootRaceAdmin>builder().wandItem(Wand.createWandItem(Material.GLASS, "Toggle Type", List.of(
                        Component.text("Left Click: cycle the display type for checkpoints")
                )))
                .onLeftClick(((event, admin) -> cycleCheckpointType()))
                .build());
        addWand(Wand.<FootRaceAdmin>builder().wandItem(Wand.createWandItem(Material.LIME_WOOL, "Starting Location", List.of(
                        Component.text("Left Click: set to current Location (exact)"),
                        Component.text("Right Click: set to current Location (rounded)"),
                        Component.text("(Crouch to get block position)")
                )))
                .onLeftClick((event, admin) -> editStartingLocation(admin, admin.getPlayer().getLocation()))
                .onLeftSneakClick((event, admin) -> editStartingLocation(admin, admin.getPlayer().getLocation().toBlockLocation()))
                .onRightClick((event, admin) -> editStartingLocation(admin,
                        MathUtils.specialRound(
                                admin.getPlayer().getLocation(),
                                0.5,
                                45F)))
                .onRightSneakClick((event, admin) -> editStartingLocation(admin,
                        MathUtils.specialRound(
                                        admin.getPlayer().getLocation(),
                                        0.5,
                                        45F)
                                .toBlockLocation()))
                .build());
        addWand(Wand.<FootRaceAdmin>builder().wandItem(Wand.createWandItem(Material.BLACK_STAINED_GLASS, "Glass Barrier", List.of(
                        Component.text("Left Click: push box face away"),
                        Component.text("Right Click: pull box face toward")
                )))
                .onLeftClick((event, admin) -> editGlassBarrier(admin, 1.0))
                .onRightClick((event, admin) -> editGlassBarrier(admin, -1.0))
                .build());
        start(newAdmins);
    }
    
    private CommandResult editStartingLocation(FootRaceAdmin admin, Location location) {
        config.setStartingLocation(location);
        startingLocationRenderer.getRenderer().setLocation(location);
        startingLocationRenderer.updateTitleLocation();
        return CommandResult.success(Component.empty()
                .append(Component.text("Set "))
                .append(Component.text("starting location")
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" to "))
                .append(Component.text("[")
                        .append(Component.text(location.getX()))
                        .append(Component.text(", "))
                        .append(Component.text(location.getY()))
                        .append(Component.text(", "))
                        .append(Component.text(location.getZ()))
                        .append(Component.text(", "))
                        .append(Component.text(location.getYaw()))
                        .append(Component.text(", "))
                        .append(Component.text(location.getPitch()))
                        .append(Component.text("]"))));
    }
    
    private CommandResult cycleCheckpointType() {
        checkpointType = MathUtils.wrapIndex(checkpointType + 1, availableTypes.length);
        BoundingBoxRendererImpl.Type typeSelection = availableTypes[checkpointType];
        checkpointRenderers.forEach(checkpointRenderer -> checkpointRenderer.setType(typeSelection));
        return CommandResult.success(Component.empty()
                .append(Component.text("Set checkpoints display type to "))
                .append(Component.text(typeSelection.toString())
                        .decorate(TextDecoration.BOLD)));
    }
    
    private CommandResult editCheckpoint(FootRaceAdmin admin, double increment) {
        BlockFace direction = EntityUtils.getPlayerDirection(admin.getPlayer().getLocation());
        BoundingBox checkpoint = config.getCheckpoints().get(admin.getCurrentCheckpoint());
        
        BoundingBox newCheckpoint = EntityUtils.expandBoundingBox(checkpoint, direction, increment);
        config.getCheckpoints().set(admin.getCurrentCheckpoint(), newCheckpoint);
        BoundingBox toPointTo = config.getCheckpoints().get(wrapIndex(admin.getCurrentCheckpoint() + 1));
        checkpointRenderers.get(admin.getCurrentCheckpoint()).setDetectionArea(newCheckpoint, toPointTo);
        
        // adjust the previous one's direction
        CheckpointRenderer previous = checkpointRenderers.get(wrapIndex(admin.getCurrentCheckpoint() - 1));
        previous.pointTo(newCheckpoint);
        
        return CommandResult.success(Component.text("Expand ")
                .append(Component.text("checkpoint")
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" "))
                .append(Component.text(direction.toString()))
                .append(Component.text(" by "))
                .append(Component.text(increment))
                .append(Component.text(" block(s)")));
    }
    
    private CommandResult editGlassBarrier(FootRaceAdmin admin, double increment) {
        BlockFace direction = EntityUtils.getPlayerDirection(admin.getPlayer().getLocation());
        
        BoundingBox newGlassBarrier = EntityUtils.expandBoundingBox(config.getGlassBarrier(), direction, increment);
        config.setGlassBarrier(newGlassBarrier);
        glassBarrierRenderer.getRenderer().setBoundingBox(newGlassBarrier);
        glassBarrierRenderer.updateTitleLocation();
        
        return CommandResult.success(Component.text("Expand ")
                .append(Component.text("glass barrier")
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" "))
                .append(Component.text(direction.toString()))
                .append(Component.text(" by "))
                .append(Component.text(increment))
                .append(Component.text(" block(s)")));
    }
    
    /**
     * @param from the checkpoint to point from
     * @param to the checkpoint to point to
     * @return a direction from the given checkpoint to the given checkpoint
     */
    public static Vector getDirection(BoundingBox from, BoundingBox to) {
        return to.getCenter().subtract(from.getCenter());
    }
    
    /**
     * Create a new Checkpoint in the given position
     * @param location the position of the min corner of the checkpoint
     * @return a new checkpoint
     */
    private BoundingBox createCheckpoint(Location location) {
        Location p = location.toBlockLocation();
        return new BoundingBox(
                p.getX(),
                p.getY(),
                p.getZ(),
                p.getX() + 2,
                p.getY() + 2,
                p.getZ() + 2
        );
    }
    
    public CommandResult selectCheckpoint(
            FootRaceAdmin admin,
            int checkpointIndex,
            boolean teleport) {
        if (checkpointIndex >= config.getCheckpoints().size()) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Already at checkpoint index "))
                    .append(Component.text(admin.getCurrentCheckpoint()))
                    .append(Component.text("/"))
                    .append(Component.text(config.getCheckpoints().size() - 1)));
        }
        if (checkpointIndex < 0) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Already at checkpoint index 0/"))
                    .append(Component.text(config.getCheckpoints().size() - 1)));
        }
        if (admin.getCurrentCheckpoint() < checkpointRenderers.size()) {
            checkpointRenderers.get(admin.getCurrentCheckpoint()).setGlowing(false);
        }
        checkpointRenderers.get(checkpointIndex).setGlowing(true);
        
        admin.setCurrentCheckpoint(checkpointIndex);
        
        if (teleport) {
            Vector center = config.getCheckpoints().get(checkpointIndex).getCenter();
            admin.getPlayer().teleport(center.toLocation(config.getWorld()));
            BoundingBox nextCheckpoint = config.getCheckpoints()
                    .get(wrapIndex(admin.getCurrentCheckpoint() + 1));
            admin.getPlayer().lookAt(
                    nextCheckpoint.getCenter().toLocation(config.getWorld()),
                    LookAnchor.EYES);
        }
        updateSidebarSelection(admin);
        return CommandResult.success(Component.empty()
                .append(Component.text("Selected puzzle index "))
                .append(Component.text(checkpointIndex))
                .append(Component.text("/"))
                .append(Component.text(config.getCheckpoints().size() - 1)));
    }
    
    public void updateSidebarSelection(FootRaceAdmin admin) {
        sidebar.updateLine(admin.getUniqueId(), "checkpoint", Component.empty()
                .append(Component.text("Checkpoint: "))
                .append(Component.text(admin.getCurrentCheckpoint()))
                .append(Component.text("/"))
                .append(Component.text(config.getCheckpoints().size() - 1)));
    }
    
    private int wrapIndex(int index) {
        return MathUtils.wrapIndex(index, config.getCheckpoints().size());
    }
    
    public List<CheckpointRenderer> createCheckpointRenderers(FootRaceConfig config) {
        BlockData checkpointBlockData = CHECKPOINT_BLOCK.createBlockData();
        BlockData directionBlockData = DIRECTION_BLOCK.createBlockData();
        List<CheckpointRenderer> results = new ArrayList<>(config.getCheckpoints().size());
        for (int i = 0; i < config.getCheckpoints().size(); i++) {
            BoundingBox checkpoint = config.getCheckpoints().get(i);
            int nextCheckpointIndex = wrapIndex(i + 1);
            BoundingBox nextCheckpoint = config.getCheckpoints().get(nextCheckpointIndex);
            Vector direction = getDirection(checkpoint, nextCheckpoint);
            results.add(CheckpointRenderer.builder()
                    .world(config.getWorld())
                    .checkpoint(checkpoint)
                    .direction(direction)
                    .detectionAreaBlock(checkpointBlockData)
                    .directionBlock(directionBlockData)
                    .type(availableTypes[checkpointType])
                    .glowColor(CHECKPOINT_GLOW)
                    .interpolationDuration(1)
                    .teleportDuration(1)
                    .titleBillboard(Display.Billboard.CENTER)
                    .build());
        }
        results.getFirst().setTitle(Component.text("Start Line"));
        results.getLast().setTitle(Component.text("Finish Line"));
        return results;
    }
    
    @Override
    protected @NotNull FootRaceEditorState getStartState() {
        return new EditingState(this);
    }
    
    @Override
    protected void cleanup() {
        startingLocationRenderer.hide();
        checkpointRenderers.forEach(Renderer::hide);
        glassBarrierRenderer.hide();
    }
    
    @Override
    protected void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("checkpoint", Component.empty())
        );
    }
    
    @Override
    protected void resetAdmin(FootRaceAdmin admin) {
        
    }
    
    @Override
    protected void initializeAdmin(FootRaceAdmin admin) {
        
    }
    
    @Override
    protected @NotNull FootRaceAdmin createAdmin(Player admin) {
        return new FootRaceAdmin(admin);
    }
}

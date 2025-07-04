package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.display.boundingbox.BoundingBoxRendererImpl;
import org.braekpo1nt.mctmanager.games.editor.EditorBase;
import org.braekpo1nt.mctmanager.games.editor.wand.Wand;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfigController;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.states.EditingState;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.states.InitialState;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.states.ParkourPathwayEditorState;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.CheckPoint;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ParkourPathwayEditor extends EditorBase<ParkourAdmin, ParkourPathwayEditorState> {
    
    /**
     * Create a new Puzzle at the given position
     * @param respawn the position of the respawn within the first checkpoint of the puzzle
     * @return a new Puzzle with a single checkpoint. inBounds will be 2x3x2.
     * @see ParkourPathwayEditor#createCheckPoint(Location) 
     */
    public static Puzzle createPuzzle(@NotNull Location respawn) {
        Location p = respawn.toBlockLocation();
        BoundingBox inBounds = createInBound(p);
        CheckPoint checkPoint = createCheckPoint(p);
        return new Puzzle(new ArrayList<>(List.of(inBounds)), new ArrayList<>(List.of(checkPoint)));
    }
    
    @Data
    static class PuzzleEdit {
        private final @NotNull Puzzle puzzle;
        private final @NotNull PuzzleRenderer puzzleRenderer;
    }
    
    private final ParkourPathwayConfigController configController;
    private @NotNull ParkourPathwayConfig config;
    private @NotNull List<Puzzle> puzzles;
    private final List<PuzzleRenderer> puzzleRenderers;
    
    public ParkourPathwayEditor(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull ParkourPathwayConfig config,
            @NotNull String configFile,
            @NotNull Collection<Player> newAdmins) {
        super(GameType.PARKOUR_PATHWAY, configFile, plugin, gameManager, new InitialState());
        this.configController = new ParkourPathwayConfigController(plugin.getDataFolder(), getType().getId());
        this.config = config;
        this.puzzles = config.getPuzzles();
        this.puzzleRenderers = createPuzzleRenderers(config);
        puzzleRenderers.forEach(PuzzleRenderer::show);
        addWand(new Wand<>(Material.TRIAL_KEY, "Puzzle Select", List.of(
                Component.text("Left Click: previous puzzle"),
                Component.text("Right Click: next puzzle"),
                Component.text("(Crouch to be teleported)")
        )))
                .onLeftClick((event, admin) -> selectPuzzle(admin, admin.getCurrentPuzzle() + 1, 0, 0, false))
                .onLeftSneakClick((event, admin) -> selectPuzzle(admin, admin.getCurrentPuzzle() + 1, 0, 0, true))
                .onRightClick((event, admin) -> selectPuzzle(admin, admin.getCurrentPuzzle() - 1, 0, 0, false))
                .onRightSneakClick((event, admin) -> selectPuzzle(admin, admin.getCurrentPuzzle() - 1, 0, 0, true));
        addWand(new Wand<>(Material.TRIAL_KEY, "Add/Remove Puzzle", List.of(
                Component.text("Left Click: add puzzle"),
                Component.text("Right Click: remove puzzle")
        )))
                .onLeftClick((event, admin) -> {
                    int newPuzzleIndex = admin.getCurrentPuzzle() + 1;
                    Puzzle newPuzzle = createPuzzle(admin.getPlayer().getLocation());
                    PuzzleRenderer newPuzzleRenderer = new PuzzleRenderer(config.getWorld(), newPuzzle, availableTypes[inBoundsType], availableTypes[checkpointType]);
                    newPuzzleRenderer.show();
                    puzzles.add(newPuzzleIndex, newPuzzle);
                    puzzleRenderers.add(newPuzzleIndex, newPuzzleRenderer);
                    CommandResult selectResult = selectPuzzle(admin, newPuzzleIndex, 0, 0, false);
                    return new CompositeCommandResult(
                            selectResult,
                            CommandResult.success(Component.empty()
                                    .append(Component.text("Add puzzle index "))
                                    .append(Component.text(newPuzzleIndex))
                                    .append(Component.text("/"))
                                    .append(Component.text(puzzles.size()))
                                    .append(Component.text(". Max index is now "))
                                    .append(Component.text(puzzles.size() + 1)))
                    );
                })
                .onRightClick((event, admin) -> {
                    if (puzzles.size() == 3) {
                        return CommandResult.failure(Component.empty()
                                .append(Component.text("There must be at least 3 puzzles")));
                    }
                    puzzles.remove(admin.getCurrentPuzzle());
                    PuzzleRenderer puzzleRenderer = puzzleRenderers.remove(admin.getCurrentPuzzle());
                    puzzleRenderer.hide();
                    CommandResult selectResult = selectPuzzle(admin, Math.max(admin.getCurrentPuzzle() - 1, 0), 0, 0, false);
                    return new CompositeCommandResult(
                            selectResult,
                            CommandResult.success(Component.empty()
                                    .append(Component.text("Remove puzzle index "))
                                    .append(Component.text(admin.getCurrentPuzzle()))
                                    .append(Component.text("/"))
                                    .append(Component.text(puzzles.size()))
                                    .append(Component.text(". Max index is now "))
                                    .append(Component.text(puzzles.size() - 1)))
                    );
                });
        addWand(new Wand<>(PuzzleRenderer.IN_BOUND_BLOCK_HIGHLIGHTED, "inBounds", List.of(
                        Component.text("Left Click: push box face away"),
                        Component.text("Right Click: pull box face toward"),
                        Component.text("(Crouch to adjust by 0.5 blocks)")
                )))
                .onLeftClick((event, admin) -> editInBounds(admin, 1.0))
                .onLeftSneakClick((event, admin) -> editInBounds(admin, 0.5))
                .onRightClick((event, admin) -> editInBounds(admin, -1.0))
                .onRightSneakClick((event, admin) -> editInBounds(admin, -0.5));
        addWand(new Wand<>(PuzzleRenderer.IN_BOUND_BLOCK, "Add/Remove inBound", List.of(
                Component.text("Left Click: add inBound"),
                Component.text("Right Click: remove inBound"))))
                .onLeftClick((event, admin) -> {
                    Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
                    PuzzleRenderer puzzleRenderer = puzzleRenderers.get(admin.getCurrentPuzzle());
                    int numOfInBounds = currentPuzzle.getInBounds().size();
                    BoundingBox newInBound = createInBound(admin.getPlayer().getLocation());
                    currentPuzzle.addInBound(newInBound);
                    puzzleRenderer.addInBound(newInBound);
                    CommandResult selectResult = selectInBound(admin, numOfInBounds);
                    return new CompositeCommandResult(
                            selectResult,
                            CommandResult.success(Component.text("Add inBound index ")
                                    .append(Component.text(numOfInBounds))
                                    .append(Component.text("/"))
                                    .append(Component.text(numOfInBounds))
                            ));
                })
                .onRightClick((event, admin) -> {
                    Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
                    PuzzleRenderer puzzleRenderer = puzzleRenderers.get(admin.getCurrentPuzzle());
                    int numOfInBounds = currentPuzzle.getInBounds().size();
                    if (numOfInBounds == 1) {
                        return CommandResult.failure(Component.text("There must be at least 1 inBound")
                                .color(NamedTextColor.RED));
                    }
                    int removalIndex = admin.getCurrentInBound();
                    currentPuzzle.removeInBound(removalIndex);
                    puzzleRenderer.removeInBound(removalIndex);
                    CommandResult selectResult = selectInBound(admin, 0);
                    return new CompositeCommandResult(
                            selectResult,
                            CommandResult.success(Component.text("Remove inBound index ")
                                    .append(Component.text(removalIndex))
                                    .append(Component.text("/"))
                                    .append(Component.text(numOfInBounds - 1))
                            ));
                });
        addWand(new Wand<>(PuzzleRenderer.IN_BOUND_BLOCK, "inBound select", List.of(
                Component.text("Left Click: previous inBound"),
                Component.text("Right Click: next inBound")
        )))
                .onLeftClick((event, admin) -> {
                    Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
                    int numOfInbounds = currentPuzzle.getInBounds().size();
                    if (admin.getCurrentInBound() == numOfInbounds - 1) {
                        return CommandResult.failure(Component.empty()
                                .append(Component.text("Already at inBound index "))
                                .append(Component.text(admin.getCurrentInBound()))
                                .append(Component.text("/"))
                                .append(Component.text(numOfInbounds - 1)));
                    }
                    return selectInBound(admin, admin.getCurrentInBound() + 1);
                })
                .onRightClick((event, admin) -> {
                    Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
                    int numOfInbounds = currentPuzzle.getInBounds().size();
                    if (admin.getCurrentInBound() == 0) {
                        return CommandResult.failure(Component.text("Already at inBound index 0/")
                                .append(Component.text(numOfInbounds - 1)));
                    }
                    return selectInBound(admin, admin.getCurrentInBound() - 1);
                });
        
        addWand(new Wand<>(PuzzleRenderer.DETECTION_AREA_BLOCK_HIGHLIGHTED, "detectionArea", List.of(
                Component.text("Left Click: push box face away"),
                Component.text("Right Click: pull box face toward"),
                Component.text("(Crouch to adjust by 0.5 blocks)")
        )))
                .onLeftClick((event, admin) -> editCheckpoint(admin, 1.0))
                .onLeftSneakClick((event, admin) -> editCheckpoint(admin, 0.5))
                .onRightClick((event, admin) -> editCheckpoint(admin, -1.0))
                .onRightSneakClick((event, admin) -> editCheckpoint(admin, -0.5));
        addWand(new Wand<>(PuzzleRenderer.DETECTION_AREA_BLOCK, "checkPoint Select", List.of(
                Component.text("Left Click: previous check point"),
                Component.text("Right Click: next check point")
        )))
                .onLeftClick((event, admin) ->  {
                    Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
                    int numOfCheckPoints = currentPuzzle.getCheckPoints().size();
                    if (admin.getCurrentCheckPoint() == numOfCheckPoints - 1) {
                        return CommandResult.failure(Component.text("Already at checkpoint index ")
                                .append(Component.text(admin.getCurrentCheckPoint()))
                                .append(Component.text("/"))
                                .append(Component.text(numOfCheckPoints - 1)));
                    }
                    return selectCheckPoint(admin, admin.getCurrentCheckPoint() + 1);
                })
                .onRightClick((event, admin) -> {
                    Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
                    int numOfCheckPoints = currentPuzzle.getCheckPoints().size();
                    if (admin.getCurrentCheckPoint() == 0) {
                        return CommandResult.failure(Component.text("Already at checkpoint index 0/")
                                .append(Component.text(numOfCheckPoints - 1))
                                .color(NamedTextColor.RED));
                    }
                    return selectCheckPoint(admin, admin.getCurrentCheckPoint() - 1);
                });
        addWand(new Wand<>(PuzzleRenderer.DETECTION_AREA_BLOCK, "Add/Remove checkPoint", List.of(
                Component.text("Left Click: add check point"),
                Component.text("Right Click: remove check point")
        )))
                .onLeftClick((event, admin) -> {
                    Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
                    PuzzleRenderer puzzleRenderer = puzzleRenderers.get(admin.getCurrentPuzzle());
                    int numOfCheckPoints = currentPuzzle.getCheckPoints().size();
                    CheckPoint newCheckPoint = createCheckPoint(admin.getPlayer().getLocation());
                    currentPuzzle.addCheckPoint(newCheckPoint);
                    puzzleRenderer.addCheckPoint(newCheckPoint);
                    CommandResult selectResult = selectCheckPoint(admin, numOfCheckPoints);
                    return new CompositeCommandResult(
                            selectResult,
                            CommandResult.success(Component.text("Add check point index ")
                                    .append(Component.text(numOfCheckPoints))
                                    .append(Component.text("/"))
                                    .append(Component.text(numOfCheckPoints)))
                    );
                })
                .onRightClick((event, admin) -> {
                    Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
                    PuzzleRenderer puzzleRenderer = puzzleRenderers.get(admin.getCurrentPuzzle());
                    int numOfCheckPoints = currentPuzzle.getCheckPoints().size();
                    if (numOfCheckPoints == 1) {
                        return CommandResult.failure(Component.text("There must be at least 1 check point"));
                    }
                    currentPuzzle.removeCheckpoint(admin.getCurrentCheckPoint());
                    puzzleRenderer.removeCheckpoint(admin.getCurrentCheckPoint());
                    CommandResult selectResult = selectCheckPoint(admin, 0);
                    return new CompositeCommandResult(
                            selectResult,
                            CommandResult.success(Component.text("Remove check point index ")
                                    .append(Component.text(admin.getCurrentCheckPoint()))
                                    .append(Component.text("/"))
                                    .append(Component.text(numOfCheckPoints - 1)))
                    );
                });
        addWand(new Wand<>(PuzzleRenderer.RESPAWN_BLOCK_HIGHLIGHTED, "respawn", List.of(
                Component.text("Left Click: set to current Location (exact)"),
                Component.text("Right Click: set to current Location (rounded)"),
                Component.text("(Crouch to get block position)")
        )))
                .onLeftClick((event, admin) -> editRespawn(admin, admin.getPlayer().getLocation()))
                .onLeftSneakClick((event, admin) -> editRespawn(admin, admin.getPlayer().getLocation().toBlockLocation()))
                .onRightClick((event, admin) -> editRespawn(admin, 
                        MathUtils.specialRound(
                                admin.getPlayer().getLocation(), 
                                0.5, 
                                45F)))
                .onRightSneakClick((event, admin) -> editRespawn(admin,
                        MathUtils.specialRound(
                                admin.getPlayer().getLocation(), 
                                0.5, 
                                45F)
                                .toBlockLocation()));
        addWand(new Wand<>(Material.GLASS, "Toggle Type", List.of(
                Component.text("Left Click: cycle the display type for inBounds"),
                Component.text("Right Click: cycle the display type for checkpoints")
        )))
                .onLeftClick(((event, admin) -> cycleInBoundsType()))
                .onRightClick(((event, admin) -> cycleCheckpointType()));
        start(newAdmins);
    }
    
    private int inBoundsType = 0;
    private int checkpointType = 0;
    private BoundingBoxRendererImpl.Type[] availableTypes = new BoundingBoxRendererImpl.Type[]{
            BoundingBoxRendererImpl.Type.EDGE_BLOCK,
            BoundingBoxRendererImpl.Type.EDGE
    };
    
    private CommandResult cycleInBoundsType() {
        inBoundsType++;
        if (inBoundsType >= availableTypes.length) {
            inBoundsType = 0;
        }
        BoundingBoxRendererImpl.Type typeSelection = availableTypes[inBoundsType];
        puzzleRenderers.forEach(puzzleRenderer -> puzzleRenderer.setInBoundsType(typeSelection));
        return CommandResult.success(Component.empty()
                .append(Component.text("Set inBounds display type to "))
                .append(Component.text(typeSelection.toString())
                        .decorate(TextDecoration.BOLD)));
    }
    
    private CommandResult cycleCheckpointType() {
        checkpointType++;
        if (checkpointType >= BoundingBoxRendererImpl.Type.values().length) {
            checkpointType = 0;
        }
        BoundingBoxRendererImpl.Type typeSelection = BoundingBoxRendererImpl.Type.values()[checkpointType];
        puzzleRenderers.forEach(puzzleRenderer -> puzzleRenderer.setCheckpointType(typeSelection));
        return CommandResult.success(Component.empty()
                .append(Component.text("Set checkpoints display type to "))
                .append(Component.text(typeSelection.toString())
                        .decorate(TextDecoration.BOLD)));
    }
    
    private CommandResult editRespawn(ParkourAdmin admin, Location respawn) {
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        PuzzleRenderer puzzleRenderer = puzzleRenderers.get(admin.getCurrentPuzzle());
        
        CheckPoint currentCheckpoint = currentPuzzle.getCheckPoint(admin.getCurrentCheckPoint());
        currentCheckpoint.setRespawn(respawn);
        puzzleRenderer.setCheckPointRespawn(admin.getCurrentCheckPoint(), respawn);
        return CommandResult.success(Component.empty()
                .append(Component.text("Set "))
                .append(Component.text("respawn")
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" to "))
                .append(Component.text("[")
                        .append(Component.text(respawn.getX()))
                        .append(Component.text(", "))
                        .append(Component.text(respawn.getY()))
                        .append(Component.text(", "))
                        .append(Component.text(respawn.getZ()))
                        .append(Component.text(", "))
                        .append(Component.text(respawn.getYaw()))
                        .append(Component.text(", "))
                        .append(Component.text(respawn.getPitch()))
                        .append(Component.text("]"))));
    }
    
    /**
     * Select the given puzzle for the given participant. Update the displays.
     * @param admin the participant
     * @param puzzleIndex the puzzle to pick (must be a valid index)
     * @param inBoundsIndex the inBounds to pick (must be a valid index)
     * @param checkPointIndex the checkPoint to pick (must be a valid index)
     * @param teleport whether to teleport the participant to the selected puzzle's first respawn
     */
    public CommandResult selectPuzzle(
            ParkourAdmin admin, 
            int puzzleIndex, 
            int inBoundsIndex, 
            int checkPointIndex, 
            boolean teleport) {
        if (puzzleIndex >= puzzles.size()) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Already at puzzle index "))
                    .append(Component.text(admin.getCurrentPuzzle()))
                    .append(Component.text("/"))
                    .append(Component.text(puzzles.size() - 1)));
        }
        if (puzzleIndex < 0) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Already at puzzle index 0/"))
                    .append(Component.text(puzzles.size() - 1)));
        }
        
        puzzleRenderers.get(admin.getCurrentPuzzle()).setHighlight(
                admin.getCurrentInBound(), 
                admin.getCurrentCheckPoint(), 
                false);
        
        PuzzleRenderer newPuzzleRenderer = puzzleRenderers.get(puzzleIndex);
        admin.setCurrentPuzzle(puzzleIndex);
        admin.setCurrentInBound(inBoundsIndex);
        admin.setCurrentCheckPoint(checkPointIndex);
        
        newPuzzleRenderer.setHighlight(inBoundsIndex, checkPointIndex, true);
        Puzzle selectedPuzzle = puzzles.get(puzzleIndex);
        if (teleport) {
            Location respawn = selectedPuzzle.getCheckPoints().get(checkPointIndex).getRespawn();
            admin.getPlayer().teleport(respawn);
        }
        updateSidebarSelection(admin);
        return CommandResult.success(Component.empty()
                .append(Component.text("Selected puzzle index "))
                .append(Component.text(puzzleIndex))
                .append(Component.text("/"))
                .append(Component.text(puzzles.size() - 1)));
    }
    
    private void updateSidebarSelection(ParkourAdmin admin) {
        Puzzle selectedPuzzle = puzzles.get(admin.getCurrentPuzzle());
        sidebar.updateLine(admin.getUniqueId(), "puzzle", Component.empty()
                .append(Component.text("Puzzle: "))
                .append(Component.text(admin.getCurrentPuzzle()))
                .append(Component.text("/"))
                .append(Component.text(puzzles.size() - 1)));
        sidebar.updateLine(admin.getUniqueId(), "inBound", Component.empty()
                .append(Component.text("InBound: "))
                .append(Component.text(admin.getCurrentInBound()))
                .append(Component.text("/"))
                .append(Component.text(selectedPuzzle.getInBounds().size() - 1)));
        sidebar.updateLine(admin.getUniqueId(), "checkPoint", Component.empty()
                .append(Component.text("CheckPoint: "))
                .append(Component.text(admin.getCurrentCheckPoint()))
                .append(Component.text("/"))
                .append(Component.text(selectedPuzzle.getCheckPoints().size() - 1)));
    }
    
    public CommandResult selectInBound(ParkourAdmin admin, int inBoundIndex) {
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        if (inBoundIndex < 0 || currentPuzzle.getInBounds().size() <= inBoundIndex) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("index "))
                    .append(Component.text(inBoundIndex))
                    .append(Component.text(" is out of bounds for length "))
                    .append(Component.text(currentPuzzle.getInBounds().size())));
        }
        PuzzleRenderer puzzleRenderer = puzzleRenderers.get(admin.getCurrentPuzzle());
        puzzleRenderer.setHighlightInBounds(admin.getCurrentInBound(), false);
        puzzleRenderer.setHighlightInBounds(inBoundIndex, true);
        admin.setCurrentInBound(inBoundIndex);
        updateSidebarSelection(admin);
        return CommandResult.success(Component.empty()
                .append(Component.text("Selected inBound index "))
                .append(Component.text(inBoundIndex))
                .append(Component.text("/"))
                .append(Component.text(currentPuzzle.getInBounds().size() - 1)));
    }
    
    /**
     * Edits the admin's currently selected inBounds by the given increment
     * @param admin the admin who is performing the edit
     * @param increment what increment to edit the inBounds by
     */
    private CommandResult editInBounds(ParkourAdmin admin, double increment) {
        BlockFace direction = EntityUtils.getPlayerDirection(admin.getPlayer().getLocation());
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        PuzzleRenderer puzzleRenderer = puzzleRenderers.get(admin.getCurrentPuzzle());
        BoundingBox inBounds = currentPuzzle.getInBounds().get(admin.getCurrentInBound());
        inBounds.expand(direction, increment);
        puzzleRenderer.setInBounds(admin.getCurrentInBound(), inBounds);
        return CommandResult.success(Component.text("Expand ")
                .append(Component.text("inBounds")
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" "))
                .append(Component.text(direction.toString()))
                .append(Component.text(" by "))
                .append(Component.text(increment))
                .append(Component.text(" block(s)")));
    }
    
    private CommandResult editCheckpoint(ParkourAdmin admin, double increment) {
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        PuzzleRenderer puzzleRenderer = puzzleRenderers.get(admin.getCurrentPuzzle());
        BlockFace direction = EntityUtils.getPlayerDirection(admin.getPlayer().getLocation());
        CheckPoint currentCheckpoint = currentPuzzle.getCheckPoints().get(admin.getCurrentCheckPoint());
        BoundingBox detectionArea = currentCheckpoint.getDetectionArea();
        detectionArea.expand(direction, increment);
        puzzleRenderer.setCheckPointDetectionArea(admin.getCurrentCheckPoint(), detectionArea);
        return CommandResult.success(Component.text("Expand ")
                .append(Component.text("detectionArea")
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" "))
                .append(Component.text(direction.toString()))
                .append(Component.text(" by "))
                .append(Component.text(increment))
                .append(Component.text(" block(s)")));
    }
    
    private CommandResult selectCheckPoint(ParkourAdmin admin, int checkPointIndex) {
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        PuzzleRenderer puzzleRenderer = puzzleRenderers.get(admin.getCurrentPuzzle());
        if (checkPointIndex < 0 || currentPuzzle.getCheckPoints().size() <= checkPointIndex) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("checkPointIndex "))
                    .append(Component.text(checkPointIndex))
                    .append(Component.text(" is out of bounds for length "))
                    .append(Component.text(currentPuzzle.getCheckPoints().size())));
        }
        puzzleRenderer.setHighlightCheckpoint(admin.getCurrentCheckPoint(), false);
        admin.setCurrentCheckPoint(checkPointIndex);
        puzzleRenderer.setHighlightCheckpoint(checkPointIndex, true);
        updateSidebarSelection(admin);
        return CommandResult.success(Component.text("Selected check point index ")
                .append(Component.text(checkPointIndex))
                .append(Component.text("/"))
                .append(Component.text(currentPuzzle.getCheckPoints().size() - 1)));
    }
    
    public List<PuzzleRenderer> createPuzzleRenderers(ParkourPathwayConfig config) {
        return config.getPuzzles().stream()
                .map(puzzle -> new PuzzleRenderer(config.getWorld(), puzzle, availableTypes[inBoundsType], availableTypes[checkpointType]))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    @Override
    protected @NotNull ParkourPathwayEditorState getStartState() {
        return new EditingState(this);
    }
    
    @Override
    protected void cleanup() {
        puzzleRenderers.forEach(PuzzleRenderer::hide);
    }
    
    @Override
    protected void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("puzzle", Component.empty()),
                new KeyLine("inBound", Component.empty()),
                new KeyLine("checkPoint", Component.empty())
        );
    }
    
    @Override
    protected void resetAdmin(ParkourAdmin admin) {
        
    }
    
    @Override
    protected void initializeAdmin(ParkourAdmin admin) {
        
    }
    
    @Override
    protected @NotNull ParkourAdmin createAdmin(Player admin) {
        return new ParkourAdmin(admin);
    }
    
    public static BoundingBox createInBound(@NotNull Location origin) {
        Location location = origin.toBlockLocation();
        return new BoundingBox(
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getX() + 2,
                location.getY() + 3,
                location.getZ() + 2
        );
    }
    
    /**
     * Creates a basic check point at the given location
     * @param respawn the location of the respawn for the check point
     * @return a new check point with the given respawn and a 1x2x1 detection area
     */
    @NotNull
    public static CheckPoint createCheckPoint(@NotNull Location respawn) {
        Location p = respawn.toBlockLocation();
        BoundingBox newDetectionArea = new BoundingBox(
                p.getX(),
                p.getY(),
                p.getZ(),
                p.getX() + 1,
                p.getY() + 2,
                p.getZ() + 1);
        return new CheckPoint(newDetectionArea, p);
    }
}

package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.editor.EditorBase;
import org.braekpo1nt.mctmanager.games.editor.wand.SpecialWand;
import org.braekpo1nt.mctmanager.games.editor.wand.Wand;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfigController;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.states.EditingState;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.states.InitialState;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.states.ParkourPathwayEditorState;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ParkourPathwayEditor extends EditorBase<ParkourAdmin, ParkourPathwayEditorState> {
    
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
            @NotNull Collection<Player> newAdmins) {
        super(GameType.PARKOUR_PATHWAY, plugin, gameManager, new InitialState());
        this.configController = new ParkourPathwayConfigController(plugin.getDataFolder(), getType().getId());
        this.config = config;
        this.puzzles = config.getPuzzles();
        this.puzzleRenderers = createPuzzleEdits(config);
        puzzleRenderers.forEach(PuzzleRenderer::show);
        addWand(Wand.builder()
                .wandItem(SpecialWand.createWandItem(Material.GLASS, "inBounds", List.of(
                        Component.text("Left Click: push box face away"),
                        Component.text("Right Click: pull box face toward"),
                        Component.text("(Crouch to adjust by 0.5 blocks)")
                )))
                .onLeftClick(event -> {
                    ParkourAdmin admin = admins.get(event.getPlayer().getUniqueId());
                    editInBounds(admin, 1.0);
                })
                .onLeftSneakClick(event -> {
                    ParkourAdmin admin = admins.get(event.getPlayer().getUniqueId());
                    editInBounds(admin, 0.5);
                })
                .onRightClick(event -> {
                    ParkourAdmin admin = admins.get(event.getPlayer().getUniqueId());
                    editInBounds(admin, -1.0);
                })
                .onRightSneakClick(event -> {
                    ParkourAdmin admin = admins.get(event.getPlayer().getUniqueId());
                    editInBounds(admin, -0.5);
                })
                .build());
        addWand(new SpecialWand<>(Material.GLASS_PANE, "Add/Remove inBound", List.of(
                Component.text("Left Click: add inBound"),
                Component.text("Right Click: remove inBound"))))
                .onLeftClick((event, admin) -> {
                    Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
                    PuzzleRenderer puzzleRenderer = puzzleRenderers.get(admin.getCurrentPuzzle());
                    int numOfInBounds = currentPuzzle.getInBounds().size();
                    BoundingBox newInBound = createInBound(admin.getPlayer().getLocation());
                    currentPuzzle.addInBound(newInBound);
                    puzzleRenderer.addInBound(newInBound);
                    selectInBound(admin, numOfInBounds);
                    return CommandResult.success(Component.text("Add inBound index ")
                            .append(Component.text(numOfInBounds))
                            .append(Component.text("/"))
                            .append(Component.text(numOfInBounds + 1))
                    );
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
                    selectInBound(admin, 0);
                    currentPuzzle.removeInBound(removalIndex);
                    puzzleRenderer.removeInBound(removalIndex);
                    return CommandResult.success(Component.text("Remove inBound index ")
                            .append(Component.text(removalIndex))
                            .append(Component.text("/"))
                            .append(Component.text(numOfInBounds - 1))
                    );
                });
        addWand(new SpecialWand<>(SpecialWand.createWandItem(Material.STICK, "test", Collections.emptyList())))
                .onLeftClick((event, admin) -> {
                    return CommandResult.success(Component.text("You have left clicked test"));
                })
                .onRightClick(((event, admin) -> {
                    return CommandResult.success(Component.text("YOu have right clicked test"));
                }));
        /*
        addWand(Wand.builder()
                .build());
        */
        start(newAdmins);
    }
    
    /**
     * Select the given puzzle for the given participant. Update the displays.
     * @param admin the participant
     * @param puzzleIndex the puzzle to pick (must be a valid index)
     * @param inBoundsIndex the inBounds to pick (must be a valid index)
     * @param checkPointIndex the checkPoint to pick (must be a valid index)
     * @param teleport whether to teleport the participant to the selected puzzle's first respawn
     */
    public void selectPuzzle(
            ParkourAdmin admin, 
            int puzzleIndex, 
            int inBoundsIndex, 
            int checkPointIndex, 
            boolean teleport) {
        puzzleRenderers.get(admin.getCurrentPuzzle()).setHighlight(
                admin.getCurrentInBound(), 
                admin.getCurrentCheckPoint(), 
                false);
        
        PuzzleRenderer newPuzzleRenderer = puzzleRenderers.get(puzzleIndex);
        admin.setCurrentPuzzle(puzzleIndex);
        admin.setCurrentInBound(inBoundsIndex);
        admin.setCurrentCheckPoint(checkPointIndex);
        
        newPuzzleRenderer.setHighlight(inBoundsIndex, checkPointIndex, true);
        admin.sendMessage(Component.text("Selected puzzle index ")
                .append(Component.text(puzzleIndex))
                .append(Component.text("/"))
                .append(Component.text(puzzles.size() - 1)));
        if (teleport) {
            Puzzle puzzle = puzzles.get(puzzleIndex);
            Location respawn = puzzle.getCheckPoints().get(checkPointIndex).getRespawn();
            admin.getPlayer().teleport(respawn);
        }
    }
    
    public void selectInBound(ParkourAdmin admin, int inBoundIndex) {
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        if (inBoundIndex < 0 || currentPuzzle.getInBounds().size() <= inBoundIndex) {
            admin.sendMessage(Component.empty()
                    .append(Component.text("index "))
                    .append(Component.text(inBoundIndex))
                    .append(Component.text(" is out of bounds for length "))
                    .append(Component.text(currentPuzzle.getInBounds().size())));
            return;
        }
        PuzzleRenderer puzzleRenderer = puzzleRenderers.get(admin.getCurrentPuzzle());
        puzzleRenderer.setHighlightInBounds(admin.getCurrentInBound(), false);
        puzzleRenderer.setHighlightInBounds(inBoundIndex, true);
        admin.setCurrentInBound(inBoundIndex);
        admin.sendMessage(Component.text("Selected inBound index ")
                .append(Component.text(inBoundIndex))
                .append(Component.text("/"))
                .append(Component.text(currentPuzzle.getInBounds().size() - 1)));
    }
    
    /**
     * Edits the admin's currently selected inBounds by the given increment
     * @param admin the admin who is performing the edit
     * @param increment what increment to edit the inBounds by
     */
    private void editInBounds(ParkourAdmin admin, double increment) {
        BlockFace direction = EntityUtils.getPlayerDirection(admin.getPlayer().getLocation());
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        PuzzleRenderer puzzleRenderer = puzzleRenderers.get(admin.getCurrentPuzzle());
        BoundingBox inBounds = currentPuzzle.getInBounds().get(admin.getCurrentInBound());
        admin.sendMessage(Component.text("Expand ")
                .append(Component.text("inBounds")
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" "))
                .append(Component.text(direction.toString()))
                .append(Component.text(" by "))
                .append(Component.text(increment))
                .append(Component.text(" block(s)")));
        inBounds.expand(direction, increment);
        puzzleRenderer.setInBounds(admin.getCurrentInBound(), inBounds);
    }
    
    private List<PuzzleRenderer> createPuzzleEdits(ParkourPathwayConfig config) {
        return config.getPuzzles().stream()
                .map(puzzle -> new PuzzleRenderer(config.getWorld(), puzzle))
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
        
    }
    
    @Override
    protected void resetAdmin(ParkourAdmin admin) {
        
    }
    
    @Override
    protected void initializeAdmin(ParkourAdmin admin) {
        selectPuzzle(admin, admin.getCurrentPuzzle(), admin.getCurrentInBound(), admin.getCurrentCheckPoint(), true);
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
}

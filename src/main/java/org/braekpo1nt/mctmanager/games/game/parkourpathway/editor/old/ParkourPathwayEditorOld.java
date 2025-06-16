package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.old;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.display.BoxRenderer;
import org.braekpo1nt.mctmanager.display.GroupRenderer;
import org.braekpo1nt.mctmanager.display.LocationRenderer;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.braekpo1nt.mctmanager.games.editor.EditorBase;
import org.braekpo1nt.mctmanager.games.editor.wand.SpecialWand;
import org.braekpo1nt.mctmanager.games.editor.wand.Wand;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfigController;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.ParkourPathwayEditor;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.states.old.EditingStateOld;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.states.old.InitialStateOld;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.states.old.ParkourPathwayEditorStateOld;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.CheckPoint;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ParkourPathwayEditorOld extends EditorBase<ParkourAdminOld, ParkourPathwayEditorStateOld> {
    
    private final ParkourPathwayConfigController configController;
    private @NotNull ParkourPathwayConfig config;
    /**
     * The puzzles that we're editing
     */
    private List<Puzzle> puzzles;
    
    public ParkourPathwayEditorOld(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull ParkourPathwayConfig config,
            @NotNull Collection<Player> newAdmins) {
        super(GameType.PARKOUR_PATHWAY, plugin, gameManager, new InitialStateOld());
        this.configController = new ParkourPathwayConfigController(plugin.getDataFolder(), getType().getId());
        this.config = config;
        addWand(Wand.builder()
                .wandItem(SpecialWand.createWandItem(Material.STICK, "inBounds", List.of(
                        Component.text("Left Click: push box face away"),
                        Component.text("Right Click: pull box face toward"),
                        Component.text("(Crouch to adjust by 0.5 blocks)")
                )))
                .onInteract(event -> {
                    ParkourAdminOld admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useInBoundsWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(SpecialWand.createWandItem(Material.STICK, "Add/Remove inBound", List.of(
                        Component.text("Left Click: add inBound"),
                        Component.text("Right Click: remove inBound")
                )))
                .onInteract(event -> {
                    ParkourAdminOld admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useAddRemoveInBoundWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(SpecialWand.createWandItem(Material.STICK, "detectionArea", List.of(
                        Component.text("Left Click: push box face away"),
                        Component.text("Right Click: pull box face toward"),
                        Component.text("(Crouch to adjust by 0.5 blocks)")
                )))
                .onInteract(event -> {
                    ParkourAdminOld admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useDetectionAreaWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(SpecialWand.createWandItem(Material.STICK, "respawn", List.of(
                        Component.text("Left Click: set to current Location (exact)"),
                        Component.text("Right Click: set to current Location (rounded)"),
                        Component.text("(Crouch to get block position)")
                )))
                .onInteract(event -> {
                    ParkourAdminOld admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useRespawnWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(SpecialWand.createWandItem(Material.STICK, "Puzzle Select", List.of(
                        Component.text("Left Click: previous puzzle"),
                        Component.text("Right Click: next puzzle"),
                        Component.text("(Crouch to be teleported)")
                )))
                .onInteract(event -> {
                    ParkourAdminOld admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    usePuzzleSelectWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(SpecialWand.createWandItem(Material.STICK, "inBound select", List.of(
                        Component.text("Left Click: previous inBound"),
                        Component.text("Right Click: next inBound")
                )))
                .onInteract(event -> {
                    ParkourAdminOld admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useInBoundSelectWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(SpecialWand.createWandItem(Material.STICK, "checkPoint Select", List.of(
                        Component.text("Left Click: previous check point"),
                        Component.text("Right Click: next check point")
                )))
                .onInteract(event -> {
                    ParkourAdminOld admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useCheckpointSelectWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(SpecialWand.createWandItem(Material.STICK, "Add/Remove checkPoint", List.of(
                        Component.text("Left Click: add check point"),
                        Component.text("Right Click: remove check point")
                )))
                .onInteract(event -> {
                    ParkourAdminOld admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useAddRemoveCheckPointWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(SpecialWand.createWandItem(Material.STICK, "Add/Remove Puzzle", List.of(
                        Component.text("Left Click: add puzzle"),
                        Component.text("Right Click: remove puzzle")
                )))
                .onInteract(event -> {
                    ParkourAdminOld admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useAddRemovePuzzleWand(admin, action);
                })
                .build());
        puzzles = config.getPuzzles();
        start(newAdmins);
    }
    
    private void reloadDisplaysForPuzzle(int puzzleIndex) {
        for (ParkourAdminOld admin : admins.values()) {
            if (admin.getCurrentPuzzle() == puzzleIndex) {
                reloadDisplay(admin);
            }
        }
    }
    
    /**
     * Update the display of the given participant's current puzzle
     * @param admin the participant to update the display for
     */
    private void reloadDisplay(ParkourAdminOld admin) {
        Renderer display = puzzlesToDisplay(admin.getCurrentPuzzle(), admin.getCurrentInBound(), admin.getCurrentCheckPoint());
        replaceDisplay(admin, display);
    }
    
    /**
     * Replaces the given admin's current display with the given newDisplay, hiding the old display and showing the new display. 
     * @param admin the admin
     * @param newDisplay the display to replace the old one with
     */
    private void replaceDisplay(@NotNull ParkourAdminOld admin, @NotNull Renderer newDisplay) {
        Renderer oldDisplay = admin.getDisplay();
        admin.setDisplay(newDisplay);
        if (oldDisplay != null) {
            oldDisplay.hide();
        }
        newDisplay.show();
    }
    
    @Override
    public @NotNull GameType getType() {
        return GameType.PARKOUR_PATHWAY;
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        // implement this
        throw new UnsupportedOperationException("not yet implemented");
    }
    
    @Override
    protected @NotNull ParkourAdminOld createAdmin(Player admin) {
        return new ParkourAdminOld(admin);
    }
    
    @Override
    public void onAdminQuit(UUID uuid) {
        // implement this
        throw new UnsupportedOperationException("not yet implemented");
    }
    
    @Override
    protected void initializeAdmin(ParkourAdminOld admin) {
        admin.setCurrentPuzzle(0);
        admin.setCurrentInBound(0);
        admin.setCurrentCheckPoint(0);
        admin.setDisplay(new BoxRenderer(config.getWorld(), new BoundingBox(), Material.GLASS));
        admin.getPlayer().teleport(config.getStartingLocation());
    }
    
    @Override
    protected @NotNull ParkourPathwayEditorStateOld getStartState() {
        return new EditingStateOld(this);
    }
    
    @Override
    public void cleanup() {
    }
    
    @Override
    protected void resetAdmin(ParkourAdminOld admin) {
        if (admin.getDisplay() != null) {
            admin.getDisplay().hide();
        }
    }
    
    private void useInBoundsWand(ParkourAdminOld admin, Action action) {
        BlockFace direction = EntityUtils.getPlayerDirection(admin.getPlayer().getLocation());
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        BoundingBox inBounds = currentPuzzle.getInBounds().get(admin.getCurrentInBound());
        double increment = admin.getPlayer().isSneaking() ? 0.5 : 1.0;
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                admin.sendMessage(Component.text("Expand ")
                        .append(Component.text("inBounds")
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" "))
                        .append(Component.text(direction.toString()))
                        .append(Component.text(" by "))
                        .append(Component.text(increment))
                        .append(Component.text(" block(s)")));
                inBounds.expand(direction, increment);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                admin.sendMessage(Component.text("Expand ")
                        .append(Component.text("inBounds")
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" "))
                        .append(Component.text(direction.toString()))
                        .append(Component.text(" by "))
                        .append(Component.text(-increment))
                        .append(Component.text(" block(s)")));
                inBounds.expand(direction, -increment);
            }
            default -> {
                return;
            }
        }
        reloadDisplaysForPuzzle(admin.getCurrentPuzzle());
    }
    
    private void useDetectionAreaWand(ParkourAdminOld admin, Action action) {
        BlockFace direction = EntityUtils.getPlayerDirection(admin.getPlayer().getLocation());
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        CheckPoint currentCheckpoint = currentPuzzle.getCheckPoints().get(admin.getCurrentCheckPoint());
        BoundingBox detectionArea = currentCheckpoint.getDetectionArea();
        double increment = admin.getPlayer().isSneaking() ? 0.5 : 1.0;
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                admin.sendMessage(Component.text("Expand ")
                        .append(Component.text("detectionArea")
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" "))
                        .append(Component.text(direction.toString()))
                        .append(Component.text(" by "))
                        .append(Component.text(increment))
                        .append(Component.text(" block(s)")));
                detectionArea.expand(direction, increment);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                admin.sendMessage(Component.text("Expand ")
                        .append(Component.text("detectionArea")
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" "))
                        .append(Component.text(direction.toString()))
                        .append(Component.text(" by "))
                        .append(Component.text(-increment))
                        .append(Component.text(" block(s)")));
                detectionArea.expand(direction, -increment);
            }
            default -> {
                return;
            }
        }
        reloadDisplaysForPuzzle(admin.getCurrentPuzzle());
    }
    
    private void useRespawnWand(ParkourAdminOld admin, Action action) {
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        CheckPoint currentCheckpoint = currentPuzzle.getCheckPoints().get(admin.getCurrentCheckPoint());
        Location respawn = currentCheckpoint.getRespawn();
        Location location = admin.getPlayer().getLocation();
        if (action.isRightClick()) {
            location = MathUtils.specialRound(location, 0.5, 45F);
        }
        if (admin.getPlayer().isSneaking()) {
            location = location.toBlockLocation();
        }
        respawn.set(location.getX(), location.getY(), location.getZ());
        respawn.setYaw(location.getYaw());
        respawn.setPitch(location.getPitch());
        reloadDisplaysForPuzzle(admin.getCurrentPuzzle());
        admin.sendMessage(Component.text("Set ")
                .append(Component.text("respawn")
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
                        .append(Component.text("]"))
                )
        );
    }
    
    private void usePuzzleSelectWand(ParkourAdminOld admin, Action action) {
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                if (admin.getCurrentPuzzle() == puzzles.size() - 1) {
                    admin.sendMessage(Component.text("Already at puzzle index ")
                                    .append(Component.text(admin.getCurrentPuzzle()))
                                    .append(Component.text("/"))
                                    .append(Component.text(puzzles.size() - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectPuzzle(admin, admin.getCurrentPuzzle() + 1, admin.getPlayer().isSneaking());
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (admin.getCurrentPuzzle() == 0) {
                    admin.sendMessage(Component.text("Already at puzzle index 0/")
                                    .append(Component.text(puzzles.size() - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectPuzzle(admin, admin.getCurrentPuzzle() - 1, admin.getPlayer().isSneaking());
            }
        }
    }
    
    private void useInBoundSelectWand(ParkourAdminOld admin, Action action) {
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        int numOfInbounds = currentPuzzle.getInBounds().size();
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                if (admin.getCurrentInBound() == numOfInbounds - 1) {
                    admin.sendMessage(Component.text("Already at inBound index ")
                            .append(Component.text(admin.getCurrentInBound()))
                            .append(Component.text("/"))
                            .append(Component.text(numOfInbounds - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectInBound(admin, admin.getCurrentInBound() + 1);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (admin.getCurrentInBound() == 0) {
                    admin.sendMessage(Component.text("Already at inBound index 0/")
                            .append(Component.text(numOfInbounds - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectInBound(admin, admin.getCurrentInBound() - 1);
            }
        }
    }
    
    private void useCheckpointSelectWand(ParkourAdminOld admin, Action action) {
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        int numOfCheckPoints = currentPuzzle.getCheckPoints().size();
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                if (admin.getCurrentCheckPoint() == numOfCheckPoints - 1) {
                    admin.sendMessage(Component.text("Already at checkpoint index ")
                            .append(Component.text(admin.getCurrentCheckPoint()))
                            .append(Component.text("/"))
                            .append(Component.text(numOfCheckPoints - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectCheckPoint(admin, admin.getCurrentCheckPoint() + 1);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (admin.getCurrentCheckPoint() == 0) {
                    admin.sendMessage(Component.text("Already at puzzle 0/")
                                    .append(Component.text(numOfCheckPoints - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectCheckPoint(admin, admin.getCurrentCheckPoint() - 1);
            }
        }
    }
    
    private void useAddRemoveCheckPointWand(ParkourAdminOld admin, Action action) {
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        int numOfCheckPoints = currentPuzzle.getCheckPoints().size();
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                admin.sendMessage(Component.text("Add check point index ")
                        .append(Component.text(numOfCheckPoints))
                        .append(Component.text("/"))
                        .append(Component.text(numOfCheckPoints + 1))
                );
                CheckPoint newCheckPoint = createCheckPoint(admin.getPlayer().getLocation());
                currentPuzzle.getCheckPoints().add(newCheckPoint);
                selectCheckPoint(admin, numOfCheckPoints);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (numOfCheckPoints == 1) {
                    admin.sendMessage(Component.text("There must be at least 1 check point")
                            .color(NamedTextColor.RED));
                    return;
                }
                currentPuzzle.getCheckPoints().remove(admin.getCurrentCheckPoint());
                admin.sendMessage(Component.text("Remove check point index ")
                        .append(Component.text(admin.getCurrentCheckPoint()))
                        .append(Component.text("/"))
                        .append(Component.text(numOfCheckPoints - 1))
                );
                selectCheckPoint(admin, 0);
            }
        }
    }
    
    private void useAddRemoveInBoundWand(ParkourAdminOld admin, Action action) {
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        int numOfInBounds = currentPuzzle.getInBounds().size();
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                admin.sendMessage(Component.text("Add inBound index ")
                        .append(Component.text(numOfInBounds))
                        .append(Component.text("/"))
                        .append(Component.text(numOfInBounds + 1))
                );
                BoundingBox newInBound = ParkourPathwayEditor.createInBound(admin.getPlayer().getLocation());
                currentPuzzle.getInBounds().add(newInBound);
                selectInBound(admin, numOfInBounds);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (numOfInBounds == 1) {
                    admin.sendMessage(Component.text("There must be at least 1 inBound")
                            .color(NamedTextColor.RED));
                    return;
                }
                currentPuzzle.getInBounds().remove(admin.getCurrentInBound());
                admin.sendMessage(Component.text("Remove inBound index ")
                        .append(Component.text(admin.getCurrentInBound()))
                        .append(Component.text("/"))
                        .append(Component.text(numOfInBounds - 1))
                );
                selectInBound(admin, 0);
            }
        }
    }
    
    private void useAddRemovePuzzleWand(ParkourAdminOld admin, Action action) {
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                int newPuzzleIndex = admin.getCurrentPuzzle() + 1;
                admin.sendMessage(Component.text("Add puzzle index ")
                        .append(Component.text(newPuzzleIndex))
                        .append(Component.text("/"))
                        .append(Component.text(puzzles.size()))
                        .append(Component.text(". Max index is now "))
                        .append(Component.text(puzzles.size() + 1))
                );
                Puzzle newPuzzle = createPuzzle(admin.getPlayer().getLocation());
                puzzles.add(newPuzzleIndex, newPuzzle);
                selectPuzzle(admin, newPuzzleIndex, false);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (puzzles.size() == 3) {
                    admin.sendMessage(Component.text("There must be at least 3 puzzles")
                            .color(NamedTextColor.RED));
                    return;
                }
                admin.sendMessage(Component.text("Remove puzzle index ")
                        .append(Component.text(admin.getCurrentPuzzle()))
                        .append(Component.text("/"))
                        .append(Component.text(puzzles.size()))
                        .append(Component.text(". Max index is now "))
                                .append(Component.text(puzzles.size() - 1))
                );
                puzzles.remove(admin.getCurrentPuzzle());
                selectPuzzle(admin, admin.getCurrentPuzzle() - 1, false);
            }
        }
    }
    
    /**
     * Create a new Puzzle at the given position
     * @param respawn the position of the respawn within the first checkpoint of the puzzle
     * @return a new Puzzle with a single checkpoint. inBounds will be 2x3x2.
     * @see ParkourPathwayEditorOld#createCheckPoint(Location) 
     */
    private Puzzle createPuzzle(@NotNull Location respawn) {
        Location p = respawn.toBlockLocation();
        BoundingBox inBounds = ParkourPathwayEditor.createInBound(p);
        CheckPoint checkPoint = createCheckPoint(p);
        return new Puzzle(new ArrayList<>(List.of(inBounds)), new ArrayList<>(List.of(checkPoint)));
    }
    
    /**
     * Creates a basic check point at the given location
     * @param respawn the location of the respawn for the check point
     * @return a new check point with the given respawn and a 1x2x1 detection area
     */
    @NotNull
    private static CheckPoint createCheckPoint(@NotNull Location respawn) {
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
    
    public void selectPuzzle(ParkourAdminOld admin, int puzzleIndex, boolean teleport) {
        selectPuzzle(admin, puzzleIndex, 0, 0, teleport);
    }
    
    /**
     * Select the given puzzle for the given participant. Update the displays.
     * @param admin the participant
     * @param puzzleIndex the puzzle to pick (must be a valid index)
     * @param inBoundsIndex the inBounds to pick (must be a valid index)
     * @param checkPointIndex the checkPoint to pick (must be a valid index)
     * @param teleport whether to teleport the participant to the selected puzzle's first respawn
     */
    public void selectPuzzle(ParkourAdminOld admin, int puzzleIndex, int inBoundsIndex, int checkPointIndex, boolean teleport) {
        Preconditions.checkArgument(0 <= puzzleIndex && puzzleIndex < puzzles.size(), "puzzleIndex %s out of bounds for length %s", puzzleIndex, puzzles.size());
        Puzzle selectedPuzzle = puzzles.get(puzzleIndex);
        Preconditions.checkArgument(0 <= inBoundsIndex && inBoundsIndex < selectedPuzzle.getInBounds().size(), "inBoundsIndex %s out of bounds for length %s", puzzleIndex, selectedPuzzle.getInBounds().size());
        Preconditions.checkArgument(0 <= checkPointIndex && checkPointIndex < selectedPuzzle.getCheckPoints().size(), "checkPointIndex %s out of bounds for length %s", puzzleIndex, selectedPuzzle.getCheckPoints().size());
        admin.setCurrentPuzzle(puzzleIndex);
        admin.setCurrentInBound(inBoundsIndex);
        admin.setCurrentCheckPoint(checkPointIndex);
        reloadDisplay(admin);
        if (teleport) {
            admin.getPlayer().teleport(selectedPuzzle.getCheckPoints().get(checkPointIndex).getRespawn());
        }
        admin.sendMessage(Component.text("Selected puzzle index ")
                .append(Component.text(puzzleIndex))
                .append(Component.text("/"))
                .append(Component.text(puzzles.size() - 1)));
        sidebar.updateLine(admin.getUniqueId(), "puzzle", String.format("Puzzle: %s/%s", puzzleIndex, puzzles.size() - 1));
        sidebar.updateLine(admin.getUniqueId(), "inBound", String.format("InBound: %s/%s", inBoundsIndex, selectedPuzzle.getInBounds().size() - 1));
        sidebar.updateLine(admin.getUniqueId(), "checkPoint", String.format("CheckPoint: %s/%s", checkPointIndex, selectedPuzzle.getCheckPoints().size() - 1));
    }
    
    private void selectInBound(ParkourAdminOld admin, int inBoundIndex) {
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        Preconditions.checkArgument(0 <= inBoundIndex && inBoundIndex < currentPuzzle.getInBounds().size(), "inBoundIndex %s out of bounds for length %s", inBoundIndex, currentPuzzle.getInBounds().size());
        admin.setCurrentInBound(inBoundIndex);
        reloadDisplay(admin);
        admin.sendMessage(Component.text("Selected inBound index ")
                .append(Component.text(inBoundIndex))
                .append(Component.text("/"))
                .append(Component.text(currentPuzzle.getInBounds().size() - 1)));
        sidebar.updateLine(admin.getUniqueId(), "inBound", String.format("InBound: %s/%s", 0, currentPuzzle.getInBounds().size() - 1));
    }
    
    /**
     * Select the given puzzle checkPoint for the given admin. Update the displays.
     * @param admin the admin
     * @param checkPointIndex the checkPoint to pick (must be a valid index)
     */
    private void selectCheckPoint(ParkourAdminOld admin, int checkPointIndex) {
        Puzzle currentPuzzle = puzzles.get(admin.getCurrentPuzzle());
        Preconditions.checkArgument(0 <= checkPointIndex && checkPointIndex < currentPuzzle.getCheckPoints().size(), "checkPointIndex %s out of bounds for length %s", checkPointIndex, currentPuzzle.getCheckPoints().size());
        admin.setCurrentCheckPoint(checkPointIndex);
        reloadDisplay(admin);
        admin.sendMessage(Component.text("Selected check point index ")
                .append(Component.text(checkPointIndex))
                .append(Component.text("/"))
                .append(Component.text(currentPuzzle.getCheckPoints().size() - 1)));
        sidebar.updateLine(admin.getUniqueId(), "inBound", String.format("InBound: %s/%s", 0, currentPuzzle.getInBounds().size() - 1));
        sidebar.updateLine(admin.getUniqueId(), "checkPoint", String.format("CheckPoint: %s/%s", 0, currentPuzzle.getCheckPoints().size() - 1));
    }
    
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        ParkourAdminOld admin = admins.get(event.getWhoClicked().getUniqueId());
        if (admin == null) {
            return;
        }
        if (!GameManagerUtils.INV_REMOVE_ACTIONS.contains(event.getAction())) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        ParkourAdminOld admin = admins.get(event.getPlayer().getUniqueId());
        if (admin == null) {
            return;
        }
        event.setCancelled(true);
    }
    
    // current
    private static final Color IN_BOUNDS_COLOR =  Color.fromRGB(150, 0, 0);
    private static final Color HIGHLIGHT_IN_BOUNDS_COLOR =  Color.fromRGB(255, 0, 0);
    private static final Color DETECTION_AREA_COLOR =  Color.fromRGB(0, 0, 150);
    private static final Color HIGHLIGHT_DETECTION_AREA_COLOR =  Color.fromRGB(0, 0, 255);
    private static final Color RESPAWN_COLOR =  Color.fromRGB(0, 255, 0);
    // next
    private static final Color NEXT_IN_BOUNDS_COLOR =  Color.fromRGB(100, 0, 0);
    private static final Color NEXT_DETECTION_AREA_COLOR =  Color.fromRGB(0, 0, 50);
    private static final Color NEXT_RESPAWN_COLOR =  Color.fromRGB(0, 100, 0);
    
    /**
     * Display the puzzle with the given index, and the next puzzle if there is one. 
     * @param puzzleIndex the index of the first puzzle to display. 
     * @param checkPointIndex the index of the checkPoint to highlight. -1 to highlight no checkPoint.
     * @return The Display of the puzzles
     */
    private @NotNull Renderer puzzlesToDisplay(int puzzleIndex, int inBoundIndex, int checkPointIndex) {
        Preconditions.checkArgument(0 <= puzzleIndex && puzzleIndex < puzzles.size(), "index must be between [0, %s] inclusive", puzzles.size());
        Puzzle puzzle = puzzles.get(puzzleIndex);
        GroupRenderer display = puzzleToDisplay(puzzle, Material.GLASS, Material.GLASS, Material.GLASS, Material.GLASS, Material.GREEN_WOOL, inBoundIndex, checkPointIndex);
        int nextIndex = puzzleIndex + 1;
        if (nextIndex < puzzles.size()) {
            Puzzle nextPuzzle = puzzles.get(nextIndex);
            Renderer nextDisplay = puzzleToDisplay(nextPuzzle, Material.GLASS, null, Material.GLASS, null, Material.GREEN_WOOL, -1, -1);
            display.addChild(nextDisplay);
        }
        return display;
    }
    
    /**
     * Create a display to represent the given puzzle
     * @param puzzle the puzzle to display
     * @param inBoundsMat the Material for the puzzle's inBounds box
     * @param detectionAreaMat the Material for the puzzle's detectionArea
     * @param highLightDetectionAreaMat the Material for the puzzle's detectionArea which should be highlighted
     * @param respawnMat the color for the puzzle's respawn point
     * @param inBoundIndex the index of the inBounds box to highlight. -1 means don't highlight any box.
     * @param checkPointIndex the index of the checkPoint to highlight. -1 means don't highlight any checkPoint.
     * @return a Display of the given puzzle with the given colors
     */
    private @NotNull GroupRenderer puzzleToDisplay(@NotNull Puzzle puzzle, @NotNull Material inBoundsMat, @Nullable Material highlightInBoundsMat, @NotNull Material detectionAreaMat, @Nullable Material highLightDetectionAreaMat, @NotNull Material respawnMat, int inBoundIndex, int checkPointIndex) {
        GroupRenderer display = new GroupRenderer(puzzle.getCheckPoints().getFirst().getRespawn());
        for (int i = 0; i < puzzle.getInBounds().size(); i++) {
            BoundingBox inBound = puzzle.getInBounds().get(i);
            if (i == inBoundIndex && highlightInBoundsMat != null) {
                display.addChild(new BoxRenderer(config.getWorld(), inBound, highlightInBoundsMat));
            } else {
                display.addChild(new BoxRenderer(config.getWorld(), inBound, inBoundsMat));
            }
        }
        for (int i = 0; i < puzzle.getCheckPoints().size(); i++) {
            CheckPoint checkPoint = puzzle.getCheckPoints().get(i);
            if (i == checkPointIndex && highLightDetectionAreaMat != null) {
                display.addChild(new BoxRenderer(config.getWorld(), checkPoint.getDetectionArea(), highLightDetectionAreaMat));
            } else {
                display.addChild(new BoxRenderer(config.getWorld(), checkPoint.getDetectionArea(), detectionAreaMat));
            }
            display.addChild(new LocationRenderer(checkPoint.getRespawn(), respawnMat));
        }
        
        return display;
    }
    
    @Override
    protected void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("puzzle", ""),
                new KeyLine("inBound", ""),
                new KeyLine("checkPoint", "")
        );
    }
}

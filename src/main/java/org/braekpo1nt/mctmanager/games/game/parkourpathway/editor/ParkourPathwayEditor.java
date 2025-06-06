package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.display.BoxDisplay;
import org.braekpo1nt.mctmanager.display.Display;
import org.braekpo1nt.mctmanager.display.GroupDisplay;
import org.braekpo1nt.mctmanager.display.LocationDisplay;
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

import java.util.*;

@Getter
@Setter
public class ParkourPathwayEditor extends EditorBase<ParkourAdmin, ParkourPathwayEditorState> {
    
    private final ParkourPathwayConfigController configController;
    private ParkourPathwayConfig config;
    /**
     * The puzzles that we're editing
     */
    private List<Puzzle> puzzles;
    private final Map<UUID, Display> displays;
    /**
     * the index of the puzzle each participant is editing
     */
    private final Map<UUID, Integer> currentPuzzles;
    /**
     * the index of the inBounds box each participant is editing
     */
    private final Map<UUID, Integer> currentInBounds;
    /**
     * The index of the checkpoint that a participant is editing in their current puzzle (since there can be multiple)
     */
    private final Map<UUID, Integer> currentCheckPoints;
    
    public ParkourPathwayEditor(
            Main plugin, 
            GameManager gameManager, 
            ParkourPathwayConfig config,
            Collection<Player> newAdmins) {
        super(GameType.PARKOUR_PATHWAY, plugin, gameManager, new InitialState());
        this.configController = new ParkourPathwayConfigController(plugin.getDataFolder(), getType().getId());
        this.config = config;
        addWand(Wand.builder()
                .wandItem(Wand.createWandItem(Material.STICK, "inBounds", List.of(
                        Component.text("Left Click: push box face away"),
                        Component.text("Right Click: pull box face toward"),
                        Component.text("(Crouch to adjust by 0.5 blocks)")
                )))
                .onInteract(event -> {
                    ParkourAdmin admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useInBoundsWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(Wand.createWandItem(Material.STICK, "detectionArea", List.of(
                        Component.text("Left Click: push box face away"),
                        Component.text("Right Click: pull box face toward"),
                        Component.text("(Crouch to adjust by 0.5 blocks)")
                )))
                .onInteract(event -> {
                    ParkourAdmin admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useDetectionAreaWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(Wand.createWandItem(Material.STICK, "respawn", List.of(
                        Component.text("Left Click: set to current Location (exact)"),
                        Component.text("Right Click: set to current Location (rounded)"),
                        Component.text("(Crouch to get block position)")
                )))
                .onInteract(event -> {
                    ParkourAdmin admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useRespawnWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(Wand.createWandItem(Material.STICK, "Puzzle Select", List.of(
                        Component.text("Left Click: previous puzzle"),
                        Component.text("Right Click: next puzzle"),
                        Component.text("(Crouch to be teleported)")
                )))
                .onInteract(event -> {
                    ParkourAdmin admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    usePuzzleSelectWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(Wand.createWandItem(Material.STICK, "inBound select", List.of(
                        Component.text("Left Click: previous inBound"),
                        Component.text("Right Click: next inBound")
                )))
                .onInteract(event -> {
                    ParkourAdmin admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useInBoundSelectWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(Wand.createWandItem(Material.STICK, "checkPoint Select", List.of(
                        Component.text("Left Click: previous check point"),
                        Component.text("Right Click: next check point")
                )))
                .onInteract(event -> {
                    ParkourAdmin admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useCheckpointSelectWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(Wand.createWandItem(Material.STICK, "Add/Remove checkPoint", List.of(
                        Component.text("Left Click: add check point"),
                        Component.text("Right Click: remove check point")
                )))
                .onInteract(event -> {
                    ParkourAdmin admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useAddRemoveCheckPointWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(Wand.createWandItem(Material.STICK, "Add/Remove inBound", List.of(
                        Component.text("Left Click: add inBound"),
                        Component.text("Right Click: remove inBound")
                )))
                .onInteract(event -> {
                    ParkourAdmin admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useAddRemoveInBoundWand(admin, action);
                })
                .build());
        addWand(Wand.builder()
                .wandItem(Wand.createWandItem(Material.STICK, "Add/Remove Puzzle", List.of(
                        Component.text("Left Click: add puzzle"),
                        Component.text("Right Click: remove puzzle")
                )))
                .onInteract(event -> {
                    ParkourAdmin admin = admins.get(event.getPlayer().getUniqueId());
                    Action action = event.getAction();
                    useAddRemovePuzzleWand(admin, action);
                })
                .build());
        currentPuzzles = new HashMap<>(newAdmins.size());
        currentInBounds = new HashMap<>(newAdmins.size());
        currentCheckPoints = new HashMap<>(newAdmins.size());
        puzzles = config.getPuzzles();
        displays = new HashMap<>(newAdmins.size());
        start(newAdmins);
    }
    
    private void reloadDisplaysForPuzzle(int puzzleIndex) {
        for (ParkourAdmin admin : admins.values()) {
            int currentPuzzleIndex = currentPuzzles.get(admin.getUniqueId());
            if (currentPuzzleIndex == puzzleIndex) {
                reloadDisplay(admin);
            }
        }
    }
    
    /**
     * Update the display of the given participant's current puzzle
     * @param admin the participant to update the display for
     */
    private void reloadDisplay(ParkourAdmin admin) {
        int currentPuzzle = currentPuzzles.get(admin.getUniqueId());
        int currentBound = currentInBounds.get(admin.getUniqueId());
        int currentCheckPoint = currentCheckPoints.get(admin.getUniqueId());
        Display display = puzzlesToDisplay(currentPuzzle, currentBound, currentCheckPoint);
        replaceDisplay(admin, display);
    }
    
    /**
     * Replaces the given admin's current display with the given newDisplay, hiding the old display and showing the new display. 
     * @param admin the admin
     * @param newDisplay the display to replace the old one with
     */
    private void replaceDisplay(@NotNull ParkourAdmin admin, @NotNull Display newDisplay) {
        Display oldDisplay = displays.put(admin.getUniqueId(), newDisplay);
        if (oldDisplay != null) {
            oldDisplay.hide();
        }
        newDisplay.show(admin.getPlayer().getWorld());
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
    protected @NotNull ParkourAdmin createAdmin(Player admin) {
        return new ParkourAdmin(admin);
    }
    
    @Override
    public void onAdminQuit(UUID uuid) {
        // implement this
        throw new UnsupportedOperationException("not yet implemented");
    }
    
    @Override
    protected void initializeAdmin(ParkourAdmin admin) {
        currentPuzzles.put(admin.getUniqueId(), 0);
        currentInBounds.put(admin.getUniqueId(), 0);
        currentCheckPoints.put(admin.getUniqueId(), 0);
        displays.put(admin.getUniqueId(), new BoxDisplay(new BoundingBox()));
        admin.getPlayer().teleport(config.getStartingLocation());
    }
    
    @Override
    protected @NotNull ParkourPathwayEditorState getStartState() {
        return new EditingState(this);
    }
    
    @Override
    public void cleanup() {
        currentPuzzles.clear();
        currentCheckPoints.clear();
        currentInBounds.clear();
        displays.clear();
    }
    
    @Override
    protected void resetAdmin(ParkourAdmin admin) {
        Display display = displays.get(admin.getUniqueId());
        if (display != null) {
            display.hide();
        }
    }
    
    private void useInBoundsWand(ParkourAdmin admin, Action action) {
        BlockFace direction = EntityUtils.getPlayerDirection(admin.getPlayer().getLocation());
        int currentPuzzleIndex = currentPuzzles.get(admin.getUniqueId());
        int currentInBoundIndex = currentInBounds.get(admin.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        BoundingBox inBounds = currentPuzzle.inBounds().get(currentInBoundIndex);
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
        reloadDisplaysForPuzzle(currentPuzzleIndex);
    }
    
    private void useDetectionAreaWand(ParkourAdmin admin, Action action) {
        BlockFace direction = EntityUtils.getPlayerDirection(admin.getPlayer().getLocation());
        int currentPuzzleIndex = currentPuzzles.get(admin.getUniqueId());
        int currentCheckpointIndex = currentCheckPoints.get(admin.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        CheckPoint currentCheckpoint = currentPuzzle.checkPoints().get(currentCheckpointIndex);
        BoundingBox detectionArea = currentCheckpoint.detectionArea();
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
        reloadDisplaysForPuzzle(currentPuzzleIndex);
    }
    
    private void useRespawnWand(ParkourAdmin admin, Action action) {
        int currentPuzzleIndex = currentPuzzles.get(admin.getUniqueId());
        int currentCheckpointIndex = currentCheckPoints.get(admin.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        CheckPoint currentCheckpoint = currentPuzzle.checkPoints().get(currentCheckpointIndex);
        Location respawn = currentCheckpoint.respawn();
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
        reloadDisplaysForPuzzle(currentPuzzleIndex);
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
    
    private void usePuzzleSelectWand(ParkourAdmin admin, Action action) {
        int currentPuzzle = currentPuzzles.get(admin.getUniqueId());
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                if (currentPuzzle == puzzles.size() - 1) {
                    admin.sendMessage(Component.text("Already at puzzle index ")
                                    .append(Component.text(currentPuzzle))
                                    .append(Component.text("/"))
                                    .append(Component.text(puzzles.size() - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectPuzzle(admin, currentPuzzle + 1, admin.getPlayer().isSneaking());
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (currentPuzzle == 0) {
                    admin.sendMessage(Component.text("Already at puzzle index 0/")
                                    .append(Component.text(puzzles.size() - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectPuzzle(admin, currentPuzzle - 1, admin.getPlayer().isSneaking());
            }
        }
    }
    
    private void useInBoundSelectWand(ParkourAdmin participant, Action action) {
        int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        int currentInBound = currentInBounds.get(participant.getUniqueId());
        int numOfInbounds = currentPuzzle.inBounds().size();
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                if (currentInBound == numOfInbounds - 1) {
                    participant.sendMessage(Component.text("Already at inBound index ")
                            .append(Component.text(currentInBound))
                            .append(Component.text("/"))
                            .append(Component.text(numOfInbounds - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectInBound(participant, currentInBound + 1);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (currentInBound == 0) {
                    participant.sendMessage(Component.text("Already at inBound index 0/")
                            .append(Component.text(numOfInbounds - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectInBound(participant, currentInBound - 1);
            }
        }
    }
    
    private void useCheckpointSelectWand(ParkourAdmin participant, Action action) {
        int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        int currentPuzzleCheckPoint = currentCheckPoints.get(participant.getUniqueId());
        int numOfCheckPoints = currentPuzzle.checkPoints().size();
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                if (currentPuzzleCheckPoint == numOfCheckPoints - 1) {
                    participant.sendMessage(Component.text("Already at checkpoint index ")
                            .append(Component.text(currentPuzzleCheckPoint))
                            .append(Component.text("/"))
                            .append(Component.text(numOfCheckPoints - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectCheckPoint(participant, currentPuzzleCheckPoint + 1);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (currentPuzzleCheckPoint == 0) {
                    participant.sendMessage(Component.text("Already at puzzle 0/")
                                    .append(Component.text(numOfCheckPoints - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectCheckPoint(participant, currentPuzzleCheckPoint - 1);
            }
        }
    }
    
    private void useAddRemoveCheckPointWand(ParkourAdmin admin, Action action) {
        int currentPuzzleIndex = currentPuzzles.get(admin.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        int numOfCheckPoints = currentPuzzle.checkPoints().size();
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                admin.sendMessage(Component.text("Add check point index ")
                        .append(Component.text(numOfCheckPoints))
                        .append(Component.text("/"))
                        .append(Component.text(numOfCheckPoints + 1))
                );
                CheckPoint newCheckPoint = createCheckPoint(admin.getPlayer().getLocation());
                currentPuzzle.checkPoints().add(newCheckPoint);
                selectCheckPoint(admin, numOfCheckPoints);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (numOfCheckPoints == 1) {
                    admin.sendMessage(Component.text("There must be at least 1 check point")
                            .color(NamedTextColor.RED));
                    return;
                }
                int currentPuzzleCheckPoint = currentCheckPoints.get(admin.getUniqueId());
                currentPuzzle.checkPoints().remove(currentPuzzleCheckPoint);
                admin.sendMessage(Component.text("Remove check point index ")
                        .append(Component.text(currentPuzzleCheckPoint))
                        .append(Component.text("/"))
                        .append(Component.text(numOfCheckPoints - 1))
                );
                selectCheckPoint(admin, 0);
            }
        }
    }
    
    private void useAddRemoveInBoundWand(ParkourAdmin admin, Action action) {
        int currentPuzzleIndex = currentPuzzles.get(admin.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        int numOfInBounds = currentPuzzle.inBounds().size();
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                admin.sendMessage(Component.text("Add inBound index ")
                        .append(Component.text(numOfInBounds))
                        .append(Component.text("/"))
                        .append(Component.text(numOfInBounds + 1))
                );
                BoundingBox newInBound = createInBound(admin.getPlayer().getLocation());
                currentPuzzle.inBounds().add(newInBound);
                selectInBound(admin, numOfInBounds);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (numOfInBounds == 1) {
                    admin.sendMessage(Component.text("There must be at least 1 inBound")
                            .color(NamedTextColor.RED));
                    return;
                }
                int currentInBound = currentInBounds.get(admin.getUniqueId());
                currentPuzzle.inBounds().remove(currentInBound);
                admin.sendMessage(Component.text("Remove inBound index ")
                        .append(Component.text(currentInBound))
                        .append(Component.text("/"))
                        .append(Component.text(numOfInBounds - 1))
                );
                selectInBound(admin, 0);
            }
        }
    }
    
    private void useAddRemovePuzzleWand(ParkourAdmin admin, Action action) {
        int currentPuzzleIndex = currentPuzzles.get(admin.getUniqueId());
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                int newPuzzleIndex = currentPuzzleIndex + 1;
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
                        .append(Component.text(currentPuzzleIndex))
                        .append(Component.text("/"))
                        .append(Component.text(puzzles.size()))
                        .append(Component.text(". Max index is now "))
                                .append(Component.text(puzzles.size() - 1))
                );
                puzzles.remove(currentPuzzleIndex);
                selectPuzzle(admin, currentPuzzleIndex - 1, false);
            }
        }
    }
    
    /**
     * Create a new Puzzle at the given position
     * @param respawn the position of the respawn within the first checkpoint of the puzzle
     * @return a new Puzzle with a single checkpoint. inBounds will be 2x3x2.
     * @see ParkourPathwayEditor#createCheckPoint(Location) 
     */
    private Puzzle createPuzzle(@NotNull Location respawn) {
        Location p = respawn.toBlockLocation();
        BoundingBox inBounds = createInBound(p);
        CheckPoint checkPoint = createCheckPoint(p);
        return new Puzzle(new ArrayList<>(List.of(inBounds)), new ArrayList<>(List.of(checkPoint)));
    }
    
    private static BoundingBox createInBound(@NotNull Location origin) {
        Location p = origin.toBlockLocation();
        return new BoundingBox(
                p.getX(),
                p.getY(),
                p.getZ(),
                p.getX() + 2,
                p.getY() + 3,
                p.getZ() + 2
        );
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
    
    public void selectPuzzle(ParkourAdmin admin, int puzzleIndex, boolean teleport) {
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
    public void selectPuzzle(ParkourAdmin admin, int puzzleIndex, int inBoundsIndex, int checkPointIndex, boolean teleport) {
        Preconditions.checkArgument(0 <= puzzleIndex && puzzleIndex < puzzles.size(), "puzzleIndex %s out of bounds for length %s", puzzleIndex, puzzles.size());
        Puzzle selectedPuzzle = puzzles.get(puzzleIndex);
        Preconditions.checkArgument(0 <= inBoundsIndex && inBoundsIndex < selectedPuzzle.inBounds().size(), "inBoundsIndex %s out of bounds for length %s", puzzleIndex, selectedPuzzle.inBounds().size());
        Preconditions.checkArgument(0 <= checkPointIndex && checkPointIndex < selectedPuzzle.checkPoints().size(), "checkPointIndex %s out of bounds for length %s", puzzleIndex, selectedPuzzle.checkPoints().size());
        currentPuzzles.put(admin.getUniqueId(), puzzleIndex);
        currentInBounds.put(admin.getUniqueId(), inBoundsIndex);
        currentCheckPoints.put(admin.getUniqueId(), checkPointIndex);
        reloadDisplay(admin);
        if (teleport) {
            admin.getPlayer().teleport(selectedPuzzle.checkPoints().get(checkPointIndex).respawn());
        }
        admin.sendMessage(Component.text("Selected puzzle index ")
                .append(Component.text(puzzleIndex))
                .append(Component.text("/"))
                .append(Component.text(puzzles.size() - 1)));
        sidebar.updateLine(admin.getUniqueId(), "puzzle", String.format("Puzzle: %s/%s", puzzleIndex, puzzles.size() - 1));
        sidebar.updateLine(admin.getUniqueId(), "inBound", String.format("InBound: %s/%s", inBoundsIndex, selectedPuzzle.inBounds().size() - 1));
        sidebar.updateLine(admin.getUniqueId(), "checkPoint", String.format("CheckPoint: %s/%s", checkPointIndex, selectedPuzzle.checkPoints().size() - 1));
    }
    
    private void selectInBound(ParkourAdmin admin, int inBoundIndex) {
        int currentPuzzleIndex = currentPuzzles.get(admin.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        Preconditions.checkArgument(0 <= inBoundIndex && inBoundIndex < currentPuzzle.inBounds().size(), "inBoundIndex %s out of bounds for length %s", inBoundIndex, currentPuzzle.inBounds().size());
        currentInBounds.put(admin.getUniqueId(), inBoundIndex);
        reloadDisplay(admin);
        admin.sendMessage(Component.text("Selected inBound index ")
                .append(Component.text(inBoundIndex))
                .append(Component.text("/"))
                .append(Component.text(currentPuzzle.inBounds().size() - 1)));
        sidebar.updateLine(admin.getUniqueId(), "inBound", String.format("InBound: %s/%s", 0, currentPuzzle.inBounds().size() - 1));
    }
    
    /**
     * Select the given puzzle checkPoint for the given admin. Update the displays.
     * @param admin the admin
     * @param checkPointIndex the checkPoint to pick (must be a valid index)
     */
    private void selectCheckPoint(ParkourAdmin admin, int checkPointIndex) {
        int currentPuzzleIndex = currentPuzzles.get(admin.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        Preconditions.checkArgument(0 <= checkPointIndex && checkPointIndex < currentPuzzle.checkPoints().size(), "checkPointIndex %s out of bounds for length %s", checkPointIndex, currentPuzzle.checkPoints().size());
        currentCheckPoints.put(admin.getUniqueId(), checkPointIndex);
        reloadDisplay(admin);
        admin.sendMessage(Component.text("Selected check point index ")
                .append(Component.text(checkPointIndex))
                .append(Component.text("/"))
                .append(Component.text(currentPuzzle.checkPoints().size() - 1)));
        sidebar.updateLine(admin.getUniqueId(), "inBound", String.format("InBound: %s/%s", 0, currentPuzzle.inBounds().size() - 1));
        sidebar.updateLine(admin.getUniqueId(), "checkPoint", String.format("CheckPoint: %s/%s", 0, currentPuzzle.checkPoints().size() - 1));
    }
    
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        ParkourAdmin admin = admins.get(event.getWhoClicked().getUniqueId());
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
        ParkourAdmin admin = admins.get(event.getPlayer().getUniqueId());
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
    private @NotNull Display puzzlesToDisplay(int puzzleIndex, int inBoundIndex, int checkPointIndex) {
        Preconditions.checkArgument(0 <= puzzleIndex && puzzleIndex < puzzles.size(), "index must be between [0, %s] inclusive", puzzles.size());
        Puzzle puzzle = puzzles.get(puzzleIndex);
        GroupDisplay display = puzzleToDisplay(puzzle, IN_BOUNDS_COLOR, HIGHLIGHT_IN_BOUNDS_COLOR, DETECTION_AREA_COLOR, HIGHLIGHT_DETECTION_AREA_COLOR, RESPAWN_COLOR, inBoundIndex, checkPointIndex);
        int nextIndex = puzzleIndex + 1;
        if (nextIndex < puzzles.size()) {
            Puzzle nextPuzzle = puzzles.get(nextIndex);
            Display nextDisplay = puzzleToDisplay(nextPuzzle, NEXT_IN_BOUNDS_COLOR, null, NEXT_DETECTION_AREA_COLOR, null, NEXT_RESPAWN_COLOR, -1, -1);
            display.addChild(nextDisplay);
        }
        return display;
    }
    
    /**
     * Create a display to represent the given puzzle
     * @param puzzle the puzzle to display
     * @param inBoundsColor the color for the puzzle's inBounds box
     * @param detectionAreaColor the color for the puzzle's detectionArea
     * @param highLightDetectionAreaColor the color for the puzzle's detectionArea which should be highlighted
     * @param respawnColor the color for the puzzle's respawn point
     * @param inBoundIndex the index of the inBounds box to highlight. -1 means don't highlight any box.
     * @param checkPointIndex the index of the checkPoint to highlight. -1 means don't highlight any checkPoint.
     * @return a Display of the given puzzle with the given colors
     */
    private @NotNull GroupDisplay puzzleToDisplay(@NotNull Puzzle puzzle, @NotNull Color inBoundsColor, @Nullable Color highlightInBoundsColor, @NotNull Color detectionAreaColor, @Nullable Color highLightDetectionAreaColor, @NotNull Color respawnColor, int inBoundIndex, int checkPointIndex) {
        GroupDisplay display = new GroupDisplay();
        for (int i = 0; i < puzzle.inBounds().size(); i++) {
            BoundingBox inBound = puzzle.inBounds().get(i);
            if (i == inBoundIndex && highlightInBoundsColor != null) {
                display.addChild(new BoxDisplay(inBound, highlightInBoundsColor));
            } else {
                display.addChild(new BoxDisplay(inBound, inBoundsColor));
            }
        }
        for (int i = 0; i < puzzle.checkPoints().size(); i++) {
            CheckPoint checkPoint = puzzle.checkPoints().get(i);
            if (i == checkPointIndex && highLightDetectionAreaColor != null) {
                display.addChild(new BoxDisplay(checkPoint.detectionArea(), highLightDetectionAreaColor));
            } else {
                display.addChild(new BoxDisplay(checkPoint.detectionArea(), detectionAreaColor));
            }
            display.addChild(new LocationDisplay(checkPoint.respawn(), respawnColor, Material.GREEN_WOOL));
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

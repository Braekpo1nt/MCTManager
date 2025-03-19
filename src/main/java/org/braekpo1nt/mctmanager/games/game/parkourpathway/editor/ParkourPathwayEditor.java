package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.display.Display;
import org.braekpo1nt.mctmanager.display.geometry.GeometryUtils;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.GameEditor;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfigController;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.CheckPoint;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ParkourPathwayEditor implements GameEditor, Configurable, Listener {
    
    private final Main plugin;
    private final ParkourPathwayConfigController configController;
    private ParkourPathwayConfig config;
    private final GameManager gameManager;
    private List<Player> participants = new ArrayList<>();
    private Sidebar sidebar;
    private Map<UUID, Display> displays;
    // wands
    private final ItemStack inBoundsWand;
    private final ItemStack detectionAreaWand;
    private final ItemStack respawnWand;
    private final ItemStack puzzleSelectWand;
    private final ItemStack inBoundSelectWand;
    private final ItemStack checkPointSelectWand;
    private final ItemStack toggleDisplayWand;
    private final ItemStack addRemovePuzzleWand;
    private final ItemStack addRemoveInBoundWand;
    private final ItemStack addRemoveCheckPointWand;
    private final List<ItemStack> allWands = new ArrayList<>();
    // end wands
    private boolean displayWalls = true;
    /**
     * The puzzles that we're editing
     */
    private List<Puzzle> puzzles;
    /**
     * the index of the puzzle each participant is editing
     */
    private Map<UUID, Integer> currentPuzzles;
    /**
     * the index of the inBounds box each participant is editing
     */
    private Map<UUID, Integer> currentInBounds;
    /**
     * The index of the checkpoint that a participant is editing in their current puzzle (since there can be multiple)
     */
    private Map<UUID, Integer> currentCheckPoints;
    private boolean editorStarted = false;
    
    public ParkourPathwayEditor(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.configController = new ParkourPathwayConfigController(plugin.getDataFolder());
        this.inBoundsWand = addWand("inBounds", List.of(
                Component.text("Left Click: push box face away"),
                Component.text("Right Click: pull box face toward"),
                Component.text("(Crouch to adjust by 0.5 blocks)")
        ));
        this.detectionAreaWand = addWand("detectionArea", List.of(
                Component.text("Left Click: push box face away"),
                Component.text("Right Click: pull box face toward"),
                Component.text("(Crouch to adjust by 0.5 blocks)")
        ));
        this.respawnWand = addWand("respawn", List.of(
                Component.text("Left Click: set to current Location (exact)"),
                Component.text("Right Click: set to current Location (rounded)"),
                Component.text("(Crouch to get block position)")
        ));
        this.puzzleSelectWand = addWand("Puzzle Select", List.of(
                Component.text("Left Click: previous puzzle"),
                Component.text("Right Click: next puzzle"),
                Component.text("(Crouch to be teleported)")
        ));
        this.inBoundSelectWand = addWand("inBound select", List.of(
                Component.text("Left Click: previous inBound"),
                Component.text("Right Click: next inBound")
        ));
        this.checkPointSelectWand = addWand("checkPoint Select", List.of(
                Component.text("Left Click: previous check point"),
                Component.text("Right Click: next check point")
        ));
        this.toggleDisplayWand = addWand("Toggle Display", List.of(
                Component.text("Cycle between all faces and just edges")
        ));
        this.addRemoveCheckPointWand = addWand("Add/Remove checkPoint", List.of(
                Component.text("Left Click: add check point"),
                Component.text("Right Click: remove check point")
        ));
        this.addRemoveInBoundWand = addWand("Add/Remove inBound", List.of(
                Component.text("Left Click: add inBound"),
                Component.text("Right Click: remove inBound")
        ));
        this.addRemovePuzzleWand = addWand("Add/Remove Puzzle", List.of(
                Component.text("Left Click: add puzzle"),
                Component.text("Right Click: remove puzzle")
        ));
    }
    
    /**
     * add a wand to the editor, with the given name and lore. Adds it to the list of all wands.
     * @param name the display name of the wand
     * @param lore the description of the wand
     * @return the new wand
     */
    private ItemStack addWand(String name, List<Component> lore) {
        ItemStack newWand = new ItemStack(Material.STICK);
        newWand.editMeta(meta -> {
            meta.displayName(Component.text(name));
            meta.lore(lore);
        });
        allWands.add(newWand);
        return newWand;
    }
    
    @Override
    public void loadConfig(@NotNull String configFile) throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getConfig();
        if (!editorStarted) {
            return;
        }
        puzzles = config.getPuzzles();
        for (Player participant : participants) {
            selectPuzzle(
                    participant, 
                    currentPuzzles.get(participant.getUniqueId()), 
                    currentInBounds.get(participant.getUniqueId()), 
                    currentCheckPoints.get(participant.getUniqueId()), 
                    false
            );
        }
    }
    
    /**
     * Update the displays of all participant's current puzzles
     */
    private void reloadAllDisplays() {
        for (Player participant : participants) {
            reloadDisplay(participant);
        }
    }
    
    private void reloadDisplaysForPuzzle(int puzzleIndex) {
        for (Player participant : participants) {
            int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
            if (currentPuzzleIndex == puzzleIndex) {
                reloadDisplay(participant);
            }
        }
    }
    
    /**
     * Update the display of the given participant's current puzzle
     * @param participant the participant to update the display for
     */
    private void reloadDisplay(Player participant) {
        int currentPuzzle = currentPuzzles.get(participant.getUniqueId());
        int currentBound = currentInBounds.get(participant.getUniqueId());
        int currentCheckPoint = currentCheckPoints.get(participant.getUniqueId());
        Display display = puzzlesToDisplay(currentPuzzle, currentBound, currentCheckPoint);
        replaceDisplay(participant, display);
    }
    
    /**
     * Replaces the given participant's current display with the given newDisplay, hiding the old display and showing the new display. 
     * @param participant the participant
     * @param newDisplay the display to replace the old one with
     */
    private void replaceDisplay(@NotNull Player participant, @NotNull Display newDisplay) {
        Display oldDisplay = displays.put(participant.getUniqueId(), newDisplay);
        if (oldDisplay != null) {
            oldDisplay.hide();
        }
        newDisplay.show(participant);
    }
    
    @Override
    public boolean configIsValid() {
        config.setPuzzles(puzzles);
        configController.validateConfig(config);
        return true;
    }
    
    @Override
    public void saveConfig() throws ConfigException {
        configController.saveConfig(config);
    }
    
    @Override
    public GameType getType() {
        return GameType.PARKOUR_PATHWAY;
    }
    
    @Override
    public void start(Collection<Player> newParticipants) {
        participants = new ArrayList<>(newParticipants.size());
        currentPuzzles = new HashMap<>(newParticipants.size());
        currentInBounds = new HashMap<>(newParticipants.size());
        currentCheckPoints = new HashMap<>(newParticipants.size());
        puzzles = config.getPuzzles();
        displays = new HashMap<>(newParticipants.size());
        sidebar = gameManager.createSidebar();
        displayWalls = true;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        for (Player participant : newParticipants) {
            selectPuzzle(participant, 0, false);
        }
        editorStarted = true;
        Main.logger().info("Starting Parkour Pathway editor");
    }
    
    public void initializeParticipant(Player participant) {
        participants.add(participant);
        currentPuzzles.put(participant.getUniqueId(), 0);
        currentInBounds.put(participant.getUniqueId(), 0);
        currentCheckPoints.put(participant.getUniqueId(), 0);
        displays.put(participant.getUniqueId(), new Display(plugin));
        sidebar.addPlayer(participant);
        ParticipantInitializer.clearInventory(participant);
        participant.teleport(config.getStartingLocation());
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        giveWands(participant);
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        editorStarted = false;
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        clearSidebar();
        participants.clear();
        currentPuzzles.clear();
        currentCheckPoints.clear();
        currentInBounds.clear();
        displays.clear();
        displayWalls = true;
        Main.logger().info("Stopping Parkour Pathway editor");
    }
    
    private void resetParticipant(Player participant) {
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.clearInventory(participant);
        Display display = displays.get(participant.getUniqueId());
        sidebar.removePlayer(participant);
        if (display != null) {
            display.hide();
        }
    }
    
    public void giveWands(Player participant) {
        participant.getInventory().addItem(allWands.toArray(new ItemStack[0]));
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player participant = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (!allWands.contains(item)) {
            return;
        }
        event.setCancelled(true);
        Action action = event.getAction();
        if (item.equals(inBoundsWand)) {
            useInBoundsWand(participant, action);
        } else if (item.equals(detectionAreaWand)) {
            useDetectionAreaWand(participant, action);
        } else if (item.equals(respawnWand)) {
            useRespawnWand(participant, action);
        }  else if (item.equals(puzzleSelectWand)) {
            usePuzzleSelectWand(participant, action);
        } else if (item.equals(inBoundSelectWand)) {
            useInBoundSelectWand(participant, action);
        } else if (item.equals(checkPointSelectWand)) {
            useCheckpointSelectWand(participant, action);
        } else if (item.equals(toggleDisplayWand)) {
            useToggleDisplayWand(participant);
        } else if (item.equals(addRemoveCheckPointWand)) {
            useAddRemoveCheckPointWand(participant, action);
        } else if (item.equals(addRemoveInBoundWand)) {
            useAddRemoveInBoundWand(participant, action);
        } else if (item.equals(addRemovePuzzleWand)) {
            useAddRemovePuzzleWand(participant, action);
        }
    }
    
    private void useInBoundsWand(Player participant, Action action) {
        BlockFace direction = EntityUtils.getPlayerDirection(participant.getLocation());
        int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
        int currentInBoundIndex = currentInBounds.get(participant.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        BoundingBox inBounds = currentPuzzle.inBounds().get(currentInBoundIndex);
        double increment = participant.isSneaking() ? 0.5 : 1.0;
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                participant.sendMessage(Component.text("Expand ")
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
                participant.sendMessage(Component.text("Expand ")
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
    
    private void useDetectionAreaWand(Player participant, Action action) {
        BlockFace direction = EntityUtils.getPlayerDirection(participant.getLocation());
        int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
        int currentCheckpointIndex = currentCheckPoints.get(participant.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        CheckPoint currentCheckpoint = currentPuzzle.checkPoints().get(currentCheckpointIndex);
        BoundingBox detectionArea = currentCheckpoint.detectionArea();
        double increment = participant.isSneaking() ? 0.5 : 1.0;
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                participant.sendMessage(Component.text("Expand ")
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
                participant.sendMessage(Component.text("Expand ")
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
    
    private void useRespawnWand(Player participant, Action action) {
        int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
        int currentCheckpointIndex = currentCheckPoints.get(participant.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        CheckPoint currentCheckpoint = currentPuzzle.checkPoints().get(currentCheckpointIndex);
        Location respawn = currentCheckpoint.respawn();
        Location location = participant.getLocation();
        if (action.isRightClick()) {
            location = MathUtils.specialRound(location, 0.5, 45F);
        }
        if (participant.isSneaking()) {
            location = location.toBlockLocation();
        }
        respawn.set(location.getX(), location.getY(), location.getZ());
        respawn.setYaw(location.getYaw());
        respawn.setPitch(location.getPitch());
        reloadDisplaysForPuzzle(currentPuzzleIndex);
        participant.sendMessage(Component.text("Set ")
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
    
    private void usePuzzleSelectWand(Player participant, Action action) {
        int currentPuzzle = currentPuzzles.get(participant.getUniqueId());
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                if (currentPuzzle == puzzles.size() - 1) {
                    participant.sendMessage(Component.text("Already at puzzle index ")
                                    .append(Component.text(currentPuzzle))
                                    .append(Component.text("/"))
                                    .append(Component.text(puzzles.size() - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectPuzzle(participant, currentPuzzle + 1, participant.isSneaking());
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (currentPuzzle == 0) {
                    participant.sendMessage(Component.text("Already at puzzle index 0/")
                                    .append(Component.text(puzzles.size() - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectPuzzle(participant, currentPuzzle - 1, participant.isSneaking());
            }
        }
    }
    
    private void useInBoundSelectWand(Player participant, Action action) {
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
    
    private void useCheckpointSelectWand(Player participant, Action action) {
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
    
    private void useToggleDisplayWand(Player participant) {
        displayWalls = !displayWalls;
        if (displayWalls) {
            participant.sendMessage("Displaying walls of inBounds");
        } else {
            participant.sendMessage("Only displaying edges of inBounds");
        }
        reloadAllDisplays();
    }
    
    private void useAddRemoveCheckPointWand(Player participant, Action action) {
        int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        int numOfCheckPoints = currentPuzzle.checkPoints().size();
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                participant.sendMessage(Component.text("Add check point index ")
                        .append(Component.text(numOfCheckPoints))
                        .append(Component.text("/"))
                        .append(Component.text(numOfCheckPoints + 1))
                );
                CheckPoint newCheckPoint = createCheckPoint(participant.getLocation());
                currentPuzzle.checkPoints().add(newCheckPoint);
                selectCheckPoint(participant, numOfCheckPoints);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (numOfCheckPoints == 1) {
                    participant.sendMessage(Component.text("There must be at least 1 check point")
                            .color(NamedTextColor.RED));
                    return;
                }
                int currentPuzzleCheckPoint = currentCheckPoints.get(participant.getUniqueId());
                currentPuzzle.checkPoints().remove(currentPuzzleCheckPoint);
                participant.sendMessage(Component.text("Remove check point index ")
                        .append(Component.text(currentPuzzleCheckPoint))
                        .append(Component.text("/"))
                        .append(Component.text(numOfCheckPoints - 1))
                );
                selectCheckPoint(participant, 0);
            }
        }
    }
    
    private void useAddRemoveInBoundWand(Player participant, Action action) {
        int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        int numOfInBounds = currentPuzzle.inBounds().size();
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                participant.sendMessage(Component.text("Add inBound index ")
                        .append(Component.text(numOfInBounds))
                        .append(Component.text("/"))
                        .append(Component.text(numOfInBounds + 1))
                );
                BoundingBox newInBound = createInBound(participant.getLocation());
                currentPuzzle.inBounds().add(newInBound);
                selectInBound(participant, numOfInBounds);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (numOfInBounds == 1) {
                    participant.sendMessage(Component.text("There must be at least 1 inBound")
                            .color(NamedTextColor.RED));
                    return;
                }
                int currentInBound = currentInBounds.get(participant.getUniqueId());
                currentPuzzle.inBounds().remove(currentInBound);
                participant.sendMessage(Component.text("Remove inBound index ")
                        .append(Component.text(currentInBound))
                        .append(Component.text("/"))
                        .append(Component.text(numOfInBounds - 1))
                );
                selectInBound(participant, 0);
            }
        }
    }
    
    private void useAddRemovePuzzleWand(Player participant, Action action) {
        int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                int newPuzzleIndex = currentPuzzleIndex + 1;
                participant.sendMessage(Component.text("Add puzzle index ")
                        .append(Component.text(newPuzzleIndex))
                        .append(Component.text("/"))
                        .append(Component.text(puzzles.size()))
                        .append(Component.text(". Max index is now "))
                        .append(Component.text(puzzles.size() + 1))
                );
                Puzzle newPuzzle = createPuzzle(participant.getLocation());
                puzzles.add(newPuzzleIndex, newPuzzle);
                selectPuzzle(participant, newPuzzleIndex, false);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (puzzles.size() == 3) {
                    participant.sendMessage(Component.text("There must be at least 3 puzzles")
                            .color(NamedTextColor.RED));
                    return;
                }
                participant.sendMessage(Component.text("Remove puzzle index ")
                        .append(Component.text(currentPuzzleIndex))
                        .append(Component.text("/"))
                        .append(Component.text(puzzles.size()))
                        .append(Component.text(". Max index is now "))
                                .append(Component.text(puzzles.size() - 1))
                );
                puzzles.remove(currentPuzzleIndex);
                selectPuzzle(participant, currentPuzzleIndex - 1, false);
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
        return new Puzzle(List.of(inBounds), new ArrayList<>(List.of(checkPoint)));
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
    
    private void selectPuzzle(Player participant, int puzzleIndex, boolean teleport) {
        selectPuzzle(participant, puzzleIndex, 0, 0, teleport);
    }
    
    /**
     * Select the given puzzle for the given participant. Update the displays.
     * @param participant the participant
     * @param puzzleIndex the puzzle to pick (must be a valid index)
     * @param inBoundsIndex the inBounds to pick (must be a valid index)
     * @param checkPointIndex the checkPoint to pick (must be a valid index)
     * @param teleport whether to teleport the participant to the selected puzzle's first respawn
     */
    private void selectPuzzle(Player participant, int puzzleIndex, int inBoundsIndex, int checkPointIndex, boolean teleport) {
        Preconditions.checkArgument(0 <= puzzleIndex && puzzleIndex < puzzles.size(), "puzzleIndex %s out of bounds for length %s", puzzleIndex, puzzles.size());
        Puzzle selectedPuzzle = puzzles.get(puzzleIndex);
        Preconditions.checkArgument(0 <= inBoundsIndex && inBoundsIndex < selectedPuzzle.inBounds().size(), "inBoundsIndex %s out of bounds for length %s", puzzleIndex, selectedPuzzle.inBounds().size());
        Preconditions.checkArgument(0 <= checkPointIndex && checkPointIndex < selectedPuzzle.checkPoints().size(), "checkPointIndex %s out of bounds for length %s", puzzleIndex, selectedPuzzle.checkPoints().size());
        currentPuzzles.put(participant.getUniqueId(), puzzleIndex);
        currentInBounds.put(participant.getUniqueId(), inBoundsIndex);
        currentCheckPoints.put(participant.getUniqueId(), checkPointIndex);
        reloadDisplay(participant);
        if (teleport) {
            participant.teleport(selectedPuzzle.checkPoints().get(checkPointIndex).respawn());
        }
        participant.sendMessage(Component.text("Selected puzzle index ")
                .append(Component.text(puzzleIndex))
                .append(Component.text("/"))
                .append(Component.text(puzzles.size() - 1)));
        sidebar.updateLine(participant.getUniqueId(), "puzzle", String.format("Puzzle: %s/%s", puzzleIndex, puzzles.size() - 1));
        sidebar.updateLine(participant.getUniqueId(), "inBound", String.format("InBound: %s/%s", inBoundsIndex, selectedPuzzle.inBounds().size() - 1));
        sidebar.updateLine(participant.getUniqueId(), "checkPoint", String.format("CheckPoint: %s/%s", checkPointIndex, selectedPuzzle.checkPoints().size() - 1));
    }
    
    private void selectInBound(Player participant, int inBoundIndex) {
        int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        Preconditions.checkArgument(0 <= inBoundIndex && inBoundIndex < currentPuzzle.inBounds().size(), "inBoundIndex %s out of bounds for length %s", inBoundIndex, currentPuzzle.inBounds().size());
        currentInBounds.put(participant.getUniqueId(), inBoundIndex);
        reloadDisplay(participant);
        participant.sendMessage(Component.text("Selected inBound index ")
                .append(Component.text(inBoundIndex))
                .append(Component.text("/"))
                .append(Component.text(currentPuzzle.inBounds().size() - 1)));
        sidebar.updateLine(participant.getUniqueId(), "inBound", String.format("InBound: %s/%s", 0, currentPuzzle.inBounds().size() - 1));
    }
    
    /**
     * Select the given puzzle checkPoint for the given participant. Update the displays.
     * @param participant the participant
     * @param checkPointIndex the checkPoint to pick (must be a valid index)
     */
    private void selectCheckPoint(Player participant, int checkPointIndex) {
        int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        Preconditions.checkArgument(0 <= checkPointIndex && checkPointIndex < currentPuzzle.checkPoints().size(), "checkPointIndex %s out of bounds for length %s", checkPointIndex, currentPuzzle.checkPoints().size());
        currentCheckPoints.put(participant.getUniqueId(), checkPointIndex);
        reloadDisplay(participant);
        participant.sendMessage(Component.text("Selected check point index ")
                .append(Component.text(checkPointIndex))
                .append(Component.text("/"))
                .append(Component.text(currentPuzzle.checkPoints().size() - 1)));
        sidebar.updateLine(participant.getUniqueId(), "inBound", String.format("InBound: %s/%s", 0, currentPuzzle.inBounds().size() - 1));
        sidebar.updateLine(participant.getUniqueId(), "checkPoint", String.format("CheckPoint: %s/%s", 0, currentPuzzle.checkPoints().size() - 1));
    }
    
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        Player participant = ((Player) event.getWhoClicked());
        if (!participants.contains(participant)) {
            return;
        }
        if (!GameManagerUtils.INV_REMOVE_ACTIONS.contains(event.getAction())) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (!participants.contains(event.getPlayer())) {
            return;
        }
        if (!allWands.contains(event.getItemDrop().getItemStack())) {
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
        Display display = puzzleToDisplay(puzzle, IN_BOUNDS_COLOR, HIGHLIGHT_IN_BOUNDS_COLOR, DETECTION_AREA_COLOR, HIGHLIGHT_DETECTION_AREA_COLOR, RESPAWN_COLOR, inBoundIndex, checkPointIndex);
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
    private @NotNull Display puzzleToDisplay(@NotNull Puzzle puzzle, @NotNull Color inBoundsColor, @Nullable Color highlightInBoundsColor, @NotNull Color detectionAreaColor, @Nullable Color highLightDetectionAreaColor, @NotNull Color respawnColor, int inBoundIndex, int checkPointIndex) {
        Display display = new Display(plugin);
        for (int i = 0; i < puzzle.inBounds().size(); i++) {
            BoundingBox inBound = puzzle.inBounds().get(i);
            List<Vector> inBoundPoints;
            if (displayWalls) {
                inBoundPoints = GeometryUtils.toRectanglePoints(inBound, 2);
            } else {
                inBoundPoints = GeometryUtils.toEdgePoints(inBound, 2);
            }
            if (i == inBoundIndex && highlightInBoundsColor != null) {
                display.addChild(new Display(plugin, inBoundPoints, highlightInBoundsColor));
            } else {
                display.addChild(new Display(plugin, inBoundPoints, inBoundsColor));
            }
        }
        for (int i = 0; i < puzzle.checkPoints().size(); i++) {
            CheckPoint checkPoint = puzzle.checkPoints().get(i);
            if (i == checkPointIndex && highLightDetectionAreaColor != null) {
                display.addChild(new Display(plugin, GeometryUtils.toEdgePoints(checkPoint.detectionArea(), 1), highLightDetectionAreaColor));
            } else {
                display.addChild(new Display(plugin, GeometryUtils.toEdgePoints(checkPoint.detectionArea(), 1), detectionAreaColor));
            }
            display.addChild(new Display(plugin, Collections.singletonList(checkPoint.respawn().toVector()), respawnColor));
        }
        
        return display;
    }
    
    private void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("puzzle", ""),
                new KeyLine("inBound", ""),
                new KeyLine("checkPoint", "")
        );
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
    }
    
}

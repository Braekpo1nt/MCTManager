package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.display.Display;
import org.braekpo1nt.mctmanager.display.geometry.GeometryUtils;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.GameEditor;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.Puzzle;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.Bukkit;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.List;

public class ParkourPathwayEditor implements GameEditor, Configurable, Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final ParkourPathwayStorageUtil storageUtil;
    
    private List<Player> participants = new ArrayList<>();
    private Map<UUID, Display> displays;
    // wands
    private final ItemStack inBoundsWand;
    private final ItemStack detectionAreaWand;
    private final ItemStack respawnWand;
    private final ItemStack puzzleSelectWand;
    private final ItemStack checkPointSelectWand;
    private final List<ItemStack> allWands;
    /**
     * The puzzles that we're editing
     */
    private List<Puzzle> puzzles;
    /**
     * the index of the puzzle each participant is editing
     */
    private Map<UUID, Integer> currentPuzzles;
    /**
     * The index of the checkpoint that a participant is editing in their current puzzle (since there can be multiple)
     */
    private Map<UUID, Integer> currentPuzzleCheckPoints;
    private boolean editorStarted = false;
    
    public ParkourPathwayEditor(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.storageUtil = new ParkourPathwayStorageUtil(plugin.getDataFolder());
        this.inBoundsWand = new ItemStack(Material.STICK);
        inBoundsWand.editMeta(meta -> {
            meta.displayName(Component.text("inBounds"));
            meta.lore(List.of(
                    Component.text("Left Click: push box face away"),
                    Component.text("Right Click: pull box face toward"),
                    Component.text("(Crouch to adjust by 0.5 blocks)")
            ));
        });
        this.detectionAreaWand = new ItemStack(Material.STICK);
        detectionAreaWand.editMeta(meta -> {
            meta.displayName(Component.text("detectionArea"));
            meta.lore(List.of(
                    Component.text("Left Click: push box face away"),
                    Component.text("Right Click: pull box face toward"),
                    Component.text("(Crouch to adjust by 0.5 blocks)")
            ));
        });
        this.respawnWand = new ItemStack(Material.STICK);
        respawnWand.editMeta(meta -> {
            meta.displayName(Component.text("respawn"));
            meta.lore(List.of(
                    Component.text("Left Click: set to current Location (exact)"),
                    Component.text("Right Click: set to current Location (rounded)"),
                    Component.text("(Crouch to get block position)")
            ));
        });
        this.puzzleSelectWand = new ItemStack(Material.STICK);
        puzzleSelectWand.editMeta(meta -> {
            meta.displayName(Component.text("Puzzle Select"));
            meta.lore(List.of(
                    Component.text("Right Click: next puzzle"),
                    Component.text("Left Click: previous puzzle")
            ));
        });
        this.checkPointSelectWand = new ItemStack(Material.STICK);
        checkPointSelectWand.editMeta(meta -> {
            meta.displayName(Component.text("CheckPoint Select"));
            meta.lore(List.of(
                    Component.text("Right Click: next check point"),
                    Component.text("Left Click: previous check point")
            ));
        });
        allWands = List.of(inBoundsWand, detectionAreaWand, respawnWand, checkPointSelectWand, puzzleSelectWand);
    }
    
    @Override
    public boolean loadConfig() throws IllegalArgumentException {
        boolean loaded = storageUtil.loadConfig();
        if (!editorStarted) {
            return loaded;
        }
        puzzles = storageUtil.deepCopyPuzzles();
        for (Player participant : participants) {
            int currentPuzzle = currentPuzzles.get(participant.getUniqueId());
            int currentCheckPoint = currentPuzzleCheckPoints.get(participant.getUniqueId());
            Display display = puzzlesToDisplay(currentPuzzle, currentCheckPoint);
            replaceDisplay(participant, display);
        }
        return loaded;
    }
    
    @Override
    public boolean configIsValid() {
        storageUtil.setPuzzles(puzzles);
        return storageUtil.configIsValid();
    }
    
    @Override
    public void saveConfig() throws IOException {
        storageUtil.saveConfig();
    }
    
    @Override
    public GameType getType() {
        return GameType.PARKOUR_PATHWAY;
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        participants = new ArrayList<>(newParticipants.size());
        currentPuzzles = new HashMap<>(newParticipants.size());
        currentPuzzleCheckPoints = new HashMap<>(newParticipants.size());
        puzzles = storageUtil.deepCopyPuzzles();
        displays = new HashMap<>(newParticipants.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        for (Player participant : newParticipants) {
            selectPuzzle(participant, 0);
        }
        editorStarted = true;
        Bukkit.getLogger().info("Stopping Parkour Pathway editor");
    }
    
    public void initializeParticipant(Player participant) {
        participants.add(participant);
        currentPuzzles.put(participant.getUniqueId(), 0);
        currentPuzzleCheckPoints.put(participant.getUniqueId(), 0);
        displays.put(participant.getUniqueId(), new Display(plugin));
        participant.getInventory().clear();
        participant.teleport(storageUtil.getStartingLocation());
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
        participants.clear();
        currentPuzzles.clear();
        currentPuzzleCheckPoints.clear();
        displays.clear();
        Bukkit.getLogger().info("Stopping Parkour Pathway editor");
    }
    
    private void resetParticipant(Player participant) {
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        participant.getInventory().clear();
        Display display = displays.get(participant.getUniqueId());
        if (display != null) {
            display.hide();
        }
        Bukkit.getLogger().info("Stopping Parkour Pathway editor");
    }
    
    public void giveWands(Player participant) {
        participant.getInventory().addItem(allWands.toArray(new ItemStack[0]));
    }
    
    /**
     * @param item the item which may be a wand
     * @return true if the item is a wand, false otherwise
     */
    public boolean isWand(@NotNull ItemStack item) {
        return allWands.contains(item);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player participant = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (!isWand(item)) {
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
        } else if (item.equals(checkPointSelectWand)) {
            useCheckpointSelectWand(participant, action);
        }
    }
    
    private void useInBoundsWand(Player participant, Action action) {
        BlockFace direction = EntityUtils.getPlayerDirection(participant.getLocation());
        int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        BoundingBox inBounds = currentPuzzle.inBounds();
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
        int currentCheckPointIndex = currentPuzzleCheckPoints.get(participant.getUniqueId());
        Display newDisplay = puzzlesToDisplay(currentPuzzleIndex, currentCheckPointIndex);
        replaceDisplay(participant, newDisplay);
    }
    
    
    
    private void useDetectionAreaWand(Player participant, Action action) {
        BlockFace direction = EntityUtils.getPlayerDirection(participant.getLocation());
        int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
        int currentCheckpointIndex = currentPuzzleCheckPoints.get(participant.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        Puzzle.CheckPoint currentCheckpoint = currentPuzzle.checkPoints().get(currentCheckpointIndex);
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
        Display newDisplay = puzzlesToDisplay(currentPuzzleIndex, currentCheckpointIndex);
        replaceDisplay(participant, newDisplay);
    }
    
    private void useRespawnWand(Player participant, Action action) {
        int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
        int currentCheckpointIndex = currentPuzzleCheckPoints.get(participant.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        Puzzle.CheckPoint currentCheckpoint = currentPuzzle.checkPoints().get(currentCheckpointIndex);
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
        Display newDisplay = puzzlesToDisplay(currentPuzzleIndex, currentCheckpointIndex);
        replaceDisplay(participant, newDisplay);
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
                selectPuzzle(participant, currentPuzzle + 1);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (currentPuzzle == 0) {
                    participant.sendMessage(Component.text("Already at puzzle index 0/")
                                    .append(Component.text(puzzles.size() - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectPuzzle(participant, currentPuzzle - 1);
            }
        }
    }
    
    private void useCheckpointSelectWand(Player participant, Action action) {
        int currentPuzzleIndex = currentPuzzles.get(participant.getUniqueId());
        Puzzle currentPuzzle = puzzles.get(currentPuzzleIndex);
        int currentPuzzleCheckPoint = currentPuzzleCheckPoints.get(participant.getUniqueId());
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
    
    /**
     * Select the given puzzle for the given participant. Update the displays.
     * @param participant the participant
     * @param puzzleIndex the puzzle to pick (must be a valid index)
     */
    private void selectPuzzle(Player participant, int puzzleIndex) {
        Preconditions.checkArgument(0 <= puzzleIndex && puzzleIndex < puzzles.size(), "puzzleIndex %s out of bounds for length %s", puzzleIndex, puzzles.size());
        currentPuzzles.put(participant.getUniqueId(), puzzleIndex);
        currentPuzzleCheckPoints.put(participant.getUniqueId(), 0);
        Display newDisplay = puzzlesToDisplay(puzzleIndex, 0);
        replaceDisplay(participant, newDisplay);
        participant.sendMessage(Component.text("Selected puzzle index ")
                .append(Component.text(puzzleIndex))
                .append(Component.text("/"))
                .append(Component.text(puzzles.size() - 1)));
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
        currentPuzzleCheckPoints.put(participant.getUniqueId(), checkPointIndex);
        Display newDisplay = puzzlesToDisplay(currentPuzzleIndex, checkPointIndex);
        replaceDisplay(participant, newDisplay);
        participant.sendMessage(Component.text("Selected check point index ")
                .append(Component.text(checkPointIndex))
                .append(Component.text("/"))
                .append(Component.text(currentPuzzle.checkPoints().size() - 1)));
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
        if (!isWand(event.getItemDrop().getItemStack())) {
            return;
        }
        event.setCancelled(true);
    }
    
    /**
     * Display the puzzle with the given index, and the next puzzle if there is one. 
     * @param puzzleIndex the index of the first puzzle to display. 
     * @param checkPointIndex the index of the checkPoint to highlight. -1 to highlight no checkPoint.
     * @return The Display of the puzzles
     */
    private @NotNull Display puzzlesToDisplay(int puzzleIndex, int checkPointIndex) {
        Preconditions.checkArgument(0 <= puzzleIndex && puzzleIndex < puzzles.size(), "index must be between [0, %s] inclusive", puzzles.size());
        Puzzle puzzle = puzzles.get(puzzleIndex);
        Display display = puzzleToDisplay(puzzle, Color.fromRGB(255, 0, 0), Color.fromRGB(0, 0, 150), Color.fromRGB(0, 255, 0), checkPointIndex, Color.fromRGB(0, 0, 255));
        int nextIndex = puzzleIndex + 1;
        if (nextIndex < puzzles.size()) {
            Puzzle nextPuzzle = puzzles.get(nextIndex);
            Display nextDisplay = puzzleToDisplay(nextPuzzle, Color.fromRGB(100, 0, 0), Color.fromRGB(0, 0, 50), Color.fromRGB(0, 100, 0), -1, Color.RED);
            display.addChild(nextDisplay);
        }
        return display;
    }
    
    /**
     * Create a display to represent the given puzzle
     * @param puzzle the puzzle to display
     * @param inBoundsColor the color for the puzzle's inBounds box
     * @param detectionAreaColor the color for the puzzle's detectionArea
     * @param respawnColor the color for the puzzle's respawn point
     * @param highlightIndex the index of the checkPoint to highlight. -1 means don't highlight any checkPoint.
     * @param highLightColor the color to highlight 
     * @return a Display of the given puzzle with the given colors
     */
    private @NotNull Display puzzleToDisplay(@NotNull Puzzle puzzle, @NotNull Color inBoundsColor, @NotNull Color detectionAreaColor, @NotNull Color respawnColor, int highlightIndex, @Nullable Color highLightColor) {
        Color highlight = highLightColor != null ? highLightColor : detectionAreaColor;
        Display display = new Display(plugin);
        display.addChild(new Display(plugin, GeometryUtils.toRectanglePoints(puzzle.inBounds(), 2), inBoundsColor));
        for (int i = 0; i < puzzle.checkPoints().size(); i++) {
            Puzzle.CheckPoint checkPoint = puzzle.checkPoints().get(i);
            if (i == highlightIndex) {
                display.addChild(new Display(plugin, GeometryUtils.toEdgePoints(checkPoint.detectionArea(), 1), highlight));
            } else {
                display.addChild(new Display(plugin, GeometryUtils.toEdgePoints(checkPoint.detectionArea(), 1), detectionAreaColor));
            }
            display.addChild(new Display(plugin, Collections.singletonList(checkPoint.respawn().toVector()), respawnColor));
        }
        
        return display;
    }
    
}

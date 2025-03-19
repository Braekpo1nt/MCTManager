package org.braekpo1nt.mctmanager.games.game.footrace.editor;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.display.Display;
import org.braekpo1nt.mctmanager.display.geometry.GeometryUtils;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfig;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfigController;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.GameEditor;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
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

import java.util.*;

public class FootRaceEditor implements GameEditor, Configurable, Listener {
    private final Main plugin;
    private final GameManager gameManager;
    private final FootRaceConfigController controller;
    private FootRaceConfig config;
    private Sidebar sidebar;
    private List<Player> participants;
    private Map<UUID, Display> displays;
    // wands
    private final ItemStack checkpointWand;
    private final ItemStack checkpointSelectWand;
    private final ItemStack addRemoveCheckpointWand;
    private final List<ItemStack> allWands = new ArrayList<>();
    // end wands
    /**
     * the checkpoint that the participant is editing
     */
    private Map<UUID, Integer> currentCheckpoints;
    private boolean editorStarted = false;
    
    public FootRaceEditor(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.controller = new FootRaceConfigController(plugin.getDataFolder());
        this.checkpointWand = addWand(
                Component.text("checkpoint"),
                List.of(
                        Component.text("Left Click: push box face away"),
                        Component.text("Right Click: pull box face toward"),
                        Component.text("(Crouch to adjust by 0.5 blocks)")
                )
        );
        this.checkpointSelectWand = addWand(
                Component.text("checkpoint select"),
                List.of(
                        Component.text("Left Click: previous checkpoint"),
                        Component.text("Right Click: next checkpoint"),
                        Component.text("(Crouch to be teleported)")
                )
        );
        this.addRemoveCheckpointWand = addWand(
                Component.text("Add/Remove Checkpoint"),
                List.of(
                        Component.text("Left Click: add checkpoint"),
                        Component.text("Right Click: remove checkpoint")
                )
        );
    }
    
    /**
     * add a wand to the editor, with the given name and lore. Adds it to the list of all wands.
     * @param name the display name of the wand
     * @param lore the description of the wand
     * @return the new wand
     */
    private ItemStack addWand(Component name, List<Component> lore) {
        ItemStack newWand = new ItemStack(Material.STICK);
        newWand.editMeta(meta -> {
            meta.displayName(name);
            meta.lore(lore);
        });
        allWands.add(newWand);
        return newWand;
    }
    
    @Override
    public void start(Collection<Player> newParticipants) {
        participants = new ArrayList<>(newParticipants.size());
        currentCheckpoints = new HashMap<>(newParticipants.size());
        displays = new HashMap<>(newParticipants.size());
        sidebar = gameManager.createSidebar();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player newParticipant : newParticipants) {
            initializeParticipant(newParticipant);
        }
        initializeSidebar();
        for (Player newParticipant : newParticipants) {
            selectCheckpoint(newParticipant, 0, false);
        }
        editorStarted = true;
        Main.logger().info("Starting Foot Race editor");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        currentCheckpoints.put(participant.getUniqueId(), 0);
        displays.put(participant.getUniqueId(), new Display(plugin));
        sidebar.addPlayer(participant);
        ParticipantInitializer.clearInventory(participant);
        participant.teleport(config.getStartingLocation());
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        giveWands(participant);
    }
    
    /**
     * Select the given puzzle checkpoint for the given participant. Update the displays.
     * @param participant the participant
     * @param checkpointIndex the checkpoint to pick (must be a valid index)
     */
    private void selectCheckpoint(Player participant, int checkpointIndex, boolean teleport) {
        Preconditions.checkArgument(0 <= checkpointIndex && checkpointIndex < config.getCheckpoints().size(), "checkpointIndex %s out of bounds for length %s", checkpointIndex, config.getCheckpoints().size());
        currentCheckpoints.put(participant.getUniqueId(), checkpointIndex);
        reloadDisplay(participant);
        participant.sendMessage(Component.text("Selected check point index ")
                .append(Component.text(checkpointIndex))
                .append(Component.text("/"))
                .append(Component.text(config.getCheckpoints().size() - 1)));
        sidebar.updateLine(participant.getUniqueId(), "checkpoint", String.format("Checkpoint: %s/%s", checkpointIndex, config.getCheckpoints().size() - 1));
        if (teleport) {
            Vector center = config.getCheckpoints().get(checkpointIndex).getCenter();
            participant.teleport(center.toLocation(config.getWorld()));
        }
    }
    
    private void reloadDisplaysForCheckpoint(int checkpointIndex) {
        for (Player participant : participants) {
            int currentCheckpointIndex = currentCheckpoints.get(participant.getUniqueId());
            if (currentCheckpointIndex == checkpointIndex) {
                reloadDisplay(participant);
            }
        }
    }
    
    /**
     * Update the display of the given participant's current puzzle
     * @param participant the participant to update the display for
     */
    private void reloadDisplay(Player participant) {
        int currentCheckpoint = currentCheckpoints.get(participant.getUniqueId());
        Display display = checkpointsToDisplay(currentCheckpoint);
        replaceDisplay(participant, display);
    }
    
    private Display checkpointsToDisplay(int checkpointIndex) {
        Preconditions.checkArgument(0 <= checkpointIndex && checkpointIndex < config.getCheckpoints().size());
        BoundingBox checkpoint = config.getCheckpoints().get(checkpointIndex);
        return new Display(plugin, GeometryUtils.toEdgePoints(checkpoint, 1.0), Color.RED);
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
    
    public void giveWands(Player participant) {
        participant.getInventory().addItem(allWands.toArray(new ItemStack[0]));
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
        currentCheckpoints.clear();
        displays.clear();
        Main.logger().info("Stopping Foot Race editor");
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
    
    @Override
    public GameType getType() {
        return GameType.FOOT_RACE;
    }
    
    @Override
    public boolean configIsValid() {
        controller.validateConfig(config);
        return true;
    }
    
    @Override
    public void saveConfig() throws ConfigIOException, ConfigInvalidException {
        controller.saveConfig(config);
    }
    
    @Override
    public void loadConfig(@NotNull String configFile) throws ConfigIOException, ConfigInvalidException {
        this.config = controller.getConfig();
        if (!editorStarted) {
            return;
        }
        for (Player participant : participants) {
            selectCheckpoint(
                    participant,
                    currentCheckpoints.get(participant.getUniqueId()),
                    false
            );
        }
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
        if (item.equals(checkpointWand)) {
            useCheckpointWand(participant, action);
        } else if (item.equals(checkpointSelectWand)) {
            useCheckpointSelectWand(participant, action);
        } else if (item.equals(addRemoveCheckpointWand)) {
            useInAddRemoveCheckpointWand(participant, action);
        }
    }
    
    private void useCheckpointWand(Player participant, Action action) {
        BlockFace direction = EntityUtils.getPlayerDirection(participant.getLocation());
        int currentCheckpointIndex = currentCheckpoints.get(participant.getUniqueId());
        BoundingBox checkpoint = config.getCheckpoints().get(currentCheckpointIndex);
        double increment = participant.isSneaking() ? 0.5 : 1.0;
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                participant.sendMessage(Component.text("Expand ")
                        .append(Component.text("checkpoint")
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" "))
                        .append(Component.text(direction.toString()))
                        .append(Component.text(" by "))
                        .append(Component.text(increment))
                        .append(Component.text(" block(s)")));
                checkpoint.expand(direction, increment);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                participant.sendMessage(Component.text("Expand ")
                        .append(Component.text("checkpoint")
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" "))
                        .append(Component.text(direction.toString()))
                        .append(Component.text(" by "))
                        .append(Component.text(-increment))
                        .append(Component.text(" block(s)")));
                checkpoint.expand(direction, -increment);
            }
            default -> {
                return;
            }
        }
        reloadDisplaysForCheckpoint(currentCheckpointIndex);
    }
    
    private void useInAddRemoveCheckpointWand(Player participant, Action action) {
        int currentCheckpointIndex = currentCheckpoints.get(participant.getUniqueId());
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                int newCheckpointIndex = currentCheckpointIndex + 1;
                participant.sendMessage(Component.text("Add checkpoint index ")
                        .append(Component.text(newCheckpointIndex))
                        .append(Component.text("/"))
                        .append(Component.text(config.getCheckpoints().size()))
                        .append(Component.text(". Max index is now "))
                        .append(Component.text(config.getCheckpoints().size() + 1))
                );
                BoundingBox checkpoint = createCheckpoint(participant.getLocation());
                config.getCheckpoints().add(newCheckpointIndex, checkpoint);
                selectCheckpoint(participant, newCheckpointIndex, false);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (config.getCheckpoints().size() <= 3) {
                    participant.sendMessage(Component.text("There must be at least 3 checkpoints")
                            .color(NamedTextColor.RED));
                    return;
                }
                participant.sendMessage(Component.text("Remove checkpoint index ")
                        .append(Component.text(currentCheckpointIndex))
                        .append(Component.text("/"))
                        .append(Component.text(config.getCheckpoints().size()))
                        .append(Component.text(". Max index is now "))
                        .append(Component.text(config.getCheckpoints().size() - 1))
                );
                config.getCheckpoints().remove(currentCheckpointIndex);
                selectCheckpoint(participant, currentCheckpointIndex - 1, false);
            }
        }
    }
    
    private void useCheckpointSelectWand(Player participant, Action action) {
        int currentCheckpoint = currentCheckpoints.get(participant.getUniqueId());
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                if (currentCheckpoint == config.getCheckpoints().size() - 1) {
                    participant.sendMessage(Component.text("Already at puzzle index ")
                            .append(Component.text(currentCheckpoint))
                            .append(Component.text("/"))
                            .append(Component.text(config.getCheckpoints().size() - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectCheckpoint(participant, currentCheckpoint + 1, participant.isSneaking());
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (currentCheckpoint == 0) {
                    participant.sendMessage(Component.text("Already at puzzle index 0/")
                            .append(Component.text(config.getCheckpoints().size() - 1))
                            .color(NamedTextColor.RED));
                    return;
                }
                selectCheckpoint(participant, currentCheckpoint - 1, participant.isSneaking());
            }
        }
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
    
    private void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("checkpoint", Component.empty())
        );
    }
    
    private void clearSidebar() {
        sidebar.deleteAllLines();
        sidebar = null;
    }
}

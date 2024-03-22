package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.display.Display;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.GameEditor;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.bukkit.Bukkit;
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
import org.jetbrains.annotations.NotNull;

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
    private final List<ItemStack> allWands;
    
    public ParkourPathwayEditor(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.storageUtil = new ParkourPathwayStorageUtil(plugin.getDataFolder());
        this.inBoundsWand = new ItemStack(Material.STICK);
        inBoundsWand.editMeta(meta -> {
            meta.displayName(Component.text("inBounds"));
            meta.lore(List.of(
                    Component.text("Right Click: pull box face toward"),
                    Component.text("Left Click: push box face away")));
        });
        this.detectionAreaWand = new ItemStack(Material.STICK);
        detectionAreaWand.editMeta(meta -> {
            meta.displayName(Component.text("detectionArea"));
            meta.lore(List.of(
                    Component.text("Right Click: pull box face toward"),
                    Component.text("Left Click: push box face away")));
        });
        this.respawnWand = new ItemStack(Material.STICK);
        respawnWand.editMeta(meta -> {
            meta.displayName(Component.text("respawn"));
            meta.lore(List.of(
                    Component.text("Left Click: set respawn Location to current")));
        });
        this.puzzleSelectWand = new ItemStack(Material.STICK);
        puzzleSelectWand.editMeta(meta -> {
            meta.displayName(Component.text("Puzzle Select"));
            meta.lore(List.of(
                    Component.text("Right Click: next puzzle"),
                    Component.text("Left Click: previous puzzle")));
        });
        allWands = List.of(inBoundsWand, detectionAreaWand, respawnWand, puzzleSelectWand);
    }
    
    @Override
    public boolean loadConfig() throws IllegalArgumentException {
        return storageUtil.loadConfig();
    }
    
    @Override
    public GameType getType() {
        return GameType.PARKOUR_PATHWAY;
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        participants = new ArrayList<>(newParticipants.size());
        displays = new HashMap<>(newParticipants.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        Bukkit.getLogger().info("Stopping Parkour Pathway editor");
    }
    
    public void initializeParticipant(Player participant) {
        participants.add(participant);
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
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
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
        if (item.equals(inBoundsWand)) {
            useInBoundsWand(participant, event.getAction());
        } else if (item.equals(detectionAreaWand)) {
            useDetectionAreaWand(participant, event.getAction());
        } else if (item.equals(respawnWand)) {
            useRespawnWand(participant);
        }  else if (item.equals(puzzleSelectWand)) {
            usePuzzleSelectWand(participant);
        }
    }
    
    private void useInBoundsWand(Player participant, Action action) {
        BlockFace direction = EntityUtils.getPlayerDirection(participant.getLocation());
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                participant.sendMessage(Component.text("Expand bounding box ")
                        .append(Component.text(direction.toString()))
                        .append(Component.text(" by "))
                        .append(Component.text(1))
                        .append(Component.text(" block")));
//                boundingBox.expand(direction, 1);
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                participant.sendMessage(Component.text("Expand bounding box ")
                        .append(Component.text(direction.toString()))
                        .append(Component.text(" by "))
                        .append(Component.text(-1))
                        .append(Component.text(" block")));
//                boundingBox.expand(direction, -1);
            }
        }
    }
    
    private void useDetectionAreaWand(Player participant, Action action) {
        
    }
    
    private void useRespawnWand(Player participant) {
        
    }
    
    private void usePuzzleSelectWand(Player participant) {
        
    }
    
    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        // allow players to move items around their inventory as normal, but if they drop a wand then cancel the event, so they keep the wand in their inventory
    }
    
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        // if a player drops a wand, cancel the event. Otherwise, do nothing. 
        if (!isWand(event.getItemDrop().getItemStack())) {
            return;
        }
        event.setCancelled(true);
    }
    
}

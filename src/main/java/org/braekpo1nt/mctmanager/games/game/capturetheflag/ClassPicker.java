package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClassPicker implements Listener {
    
    private final Component NETHER_STAR_NAME = Component.text("Pick a class");
    
    private final Map<UUID, String> pickedBattleClasses = new HashMap<>();
    private final Main plugin;
    private final Map<UUID, Participant> teamMates;
    private final Map<UUID, ChestGui> guis;
    private final Map<String, Loadout> loadouts;
    private final Map<String, GuiItem> battleClassGuiItems;
    private final Color leatherColor;
    
    public <T extends Participant> ClassPicker(
            Main plugin,
            Collection<T> newTeamMates,
            @NotNull Color leatherColor,
            Map<String, Loadout> loadouts) {
        this.plugin = plugin;
        this.teamMates = new HashMap<>(newTeamMates.size());
        for (T teamMate : newTeamMates) {
            this.teamMates.put(teamMate.getUniqueId(), teamMate);
        }
        this.guis = new HashMap<>(newTeamMates.size());
        this.loadouts = new HashMap<>(loadouts);
        this.leatherColor = leatherColor;
        this.battleClassGuiItems = createGuiItems();
    }
    
    public void start() {
        for (Participant teamMate : teamMates.values()) {
            ChestGui gui = createGui();
            this.guis.put(teamMate.getUniqueId(), gui);
            gui.show(teamMate.getPlayer());
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private Map<String, GuiItem> createGuiItems() {
        Map<String, GuiItem> guiItems = new HashMap<>(loadouts.size());
        for (String battleClass : loadouts.keySet()) {
            Loadout loadout = loadouts.get(battleClass);
            GuiItem menuItem = new GuiItem(loadout.getMenuItem());
            guiItems.put(battleClass, menuItem);
            menuItem.setAction(event -> {
                Participant teamMate = teamMates.get(event.getWhoClicked().getUniqueId());
                if (teamMate == null) {
                    // should not happen
                    event.getWhoClicked().sendMessage(Component.empty()
                            .append(Component.text("You are not on this team's class picker.")));
                    return;
                }
                
                // prevent two teamMates picking same class
                if (pickedBattleClasses.containsValue(battleClass)) {
                    teamMate.sendMessage(Component.empty()
                            .append(Component.text("Someone on your team already selected "))
                            .append(loadout.getName())
                            .color(NamedTextColor.DARK_RED));
                    return;
                }
                
                // deselect old class
                deselectClass(teamMate);
                
                // select new class
                assignClass(battleClass, teamMate, loadout);
                ItemStack newItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                newItem.editMeta(meta -> {
                    meta.displayName(loadout.getName());
                });
                menuItem.setItem(newItem);
                guis.values().forEach(Gui::update);
            });
        }
        return guiItems;
    }
    
    private void assignClass(String battleClass, Participant teamMate, Loadout loadout) {
        pickedBattleClasses.put(teamMate.getUniqueId(), battleClass);
        teamMate.getInventory().clear();
        teamMate.getInventory().setContents(loadout.getContents());
        ColorMap.colorLeatherArmor(teamMate, leatherColor);
        teamMate.sendMessage(Component.empty()
                .append(Component.text("Selected "))
                .append(loadout.getName()));
    }
    
    private void deselectClass(Participant teamMate) {
        String deselectedBattleClass = pickedBattleClasses.remove(teamMate.getUniqueId());
        if (deselectedBattleClass != null) {
            GuiItem deselectedItem = battleClassGuiItems.get(deselectedBattleClass);
            Loadout deselectedLoadout = loadouts.get(deselectedBattleClass);
            deselectedItem.setItem(deselectedLoadout.getMenuItem());
            guis.values().forEach(Gui::update);
        }
    }
    
    private ChestGui createGui() {
        ChestGui gui = new ChestGui(1, ComponentHolder.of(Component.empty()
                .append(Component.text("Pick a class")
                        .color(NamedTextColor.DARK_GRAY))
                .append(Component.text(" (One per team)")
                        .color(NamedTextColor.GRAY))));
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        OutlinePane pane = new OutlinePane(0,0, 9, 1);
        for (GuiItem guiItem : battleClassGuiItems.values()) {
            pane.addItem(guiItem);
        }
        gui.addPane(pane);
        gui.update();
        gui.setOnClose(event -> {
            if (!pickedBattleClasses.containsKey(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(Component.empty()
                        .append(Component.text("You didn't pick a class. Use the nether star to pick."))
                        .color(NamedTextColor.DARK_RED));
            }
            ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
            netherStar.editMeta(meta -> meta.displayName(NETHER_STAR_NAME));
            event.getPlayer().getInventory().addItem(netherStar);
        });
        return gui;
    }
    
    public void stop(boolean assignBattleClasses) {
        HandlerList.unregisterAll(this);
        ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
        netherStar.editMeta(meta -> meta.displayName(NETHER_STAR_NAME));
        for (Participant teamMate : teamMates.values()) {
            ChestGui gui = guis.get(teamMate.getUniqueId());
            gui.setOnClose(event -> {});
            gui.getInventory().close();
            teamMate.getInventory().removeItemAnySlot(netherStar);
        }
        if (assignBattleClasses) {
            for (Participant teamMate : teamMates.values()) {
                if (!pickedBattleClasses.containsKey(teamMate.getUniqueId())) {
                    assignLeftoverClass(teamMate);
                }
            }
        }
        teamMates.clear();
        guis.clear();
        pickedBattleClasses.clear();
        battleClassGuiItems.clear();
        loadouts.clear();
    }
    
    private void assignLeftoverClass(Participant teamMate) {
        for (String battleClass : loadouts.keySet()) {
            if (!pickedBattleClasses.containsValue(battleClass)) {
                assignClass(battleClass, teamMate, loadouts.get(battleClass));
                return;
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Participant teamMate = teamMates.get(event.getPlayer().getUniqueId());
        if (teamMate == null) {
            return;
        }
        ItemStack netherStar = event.getItem();
        if (netherStar == null ||
                !netherStar.getType().equals(Material.NETHER_STAR)) {
            return;
        }
        ItemMeta netherStarMeta = netherStar.getItemMeta();
        if (netherStarMeta == null || !netherStarMeta.hasDisplayName() || !Objects.equals(netherStarMeta.displayName(), NETHER_STAR_NAME)) {
            return;
        }
        event.setCancelled(true);
        teamMate.getInventory().remove(netherStar);
        guis.get(teamMate.getUniqueId()).show(teamMate.getPlayer());
    }
    
    public void addTeamMate(Participant teamMate) {
        teamMates.put(teamMate.getUniqueId(), teamMate);
        teamMate.sendMessage(Component.text("Choose your class"));
        ChestGui gui = createGui();
        this.guis.put(teamMate.getUniqueId(), gui);
        gui.show(teamMate.getPlayer());
    }
    
    public void removeTeamMate(UUID uuid) {
        Participant teamMate = teamMates.remove(uuid);
        if (teamMate == null) {
            return;
        }
        ChestGui gui = guis.get(teamMate.getUniqueId());
        gui.setOnClose(event -> {});
        gui.getInventory().close();
        if (pickedBattleClasses.containsKey(teamMate.getUniqueId())) {
            deselectClass(teamMate);
        }
    }
    
}

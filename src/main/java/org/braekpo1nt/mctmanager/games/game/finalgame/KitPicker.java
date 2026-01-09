package org.braekpo1nt.mctmanager.games.game.finalgame;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.base.states.Kitted;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.Color;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class KitPicker<K extends Participant & Kitted> {
    
    @Data
    private class ParticipantData {
        /**
         * The participant represented by this data
         */
        private final K participant;
        /**
         * The ID of the kit chosen by this participant
         * null indicates no kit chosen
         */
        private @Nullable String kitId;
        /**
         * The kit picker gui that this participant has open.
         * Null if they are not looking at the kit picker gui.
         */
        private @Nullable ChestGui gui;
        
        public ParticipantData(K participant) {
            this.participant = participant;
            this.kitId = null;
        }
        
        /**
         * @return true if this participant has a kit selected
         */
        public boolean hasKit() {
            return kitId != null;
        }
        
        /**
         * @param kitId the kitId to check for
         * @return true if the given kitId matches this participant's {@link #kitId},
         * false otherwise
         */
        public boolean hasKit(String kitId) {
            return Objects.equals(this.kitId, kitId);
        }
        
        public void updateGui() {
            if (gui == null) {
                return;
            }
            gui.update();
        }
        
        /**
         * Show the given gui, save the gui to show later
         * @param gui the gui to show and save
         */
        public void showGui(ChestGui gui) {
            this.gui = gui;
            this.gui.show(participant.getPlayer());
        }
        
        /**
         * Show the saved gui to the participant
         */
        public void showGui() {
            if (this.gui == null) {
                return;
            }
            this.gui.show(participant.getPlayer());
        }
        
        /**
         * Hide (or close) the gui from the participant viewing it
         * Removes the onCloseEvent from the gui
         */
        public void deleteGuiAndHide() {
            if (this.gui == null) {
                return;
            }
            this.gui.setOnClose(event -> {
            });
            this.gui.getInventory().close();
            this.gui = null;
        }
    }
    
    @Data
    private static class KitData {
        /**
         * The internal identifier of this kit
         */
        private final String id;
        /**
         * The kit represented by this data
         */
        private final FinalGameKit kit;
        /**
         * how many participants have chosen this kit
         */
        private int chosen;
        /**
         * The {@link GuiItem} representing this kit in menus
         */
        private GuiItem menuItem;
        
        public KitData(String id, FinalGameKit kit) {
            this.id = id;
            this.kit = kit;
            this.chosen = 0;
            this.menuItem = new GuiItem(kit.getMenuItem(getAvailableCopies()));
        }
        
        /**
         * @return true if someone can choose this class, false if
         * all available copies of this kit have been chosen
         */
        public boolean canBeChosen() {
            return chosen < kit.getCopies();
        }
        
        /**
         * @return How many copies are available to be chosen
         */
        public int getAvailableCopies() {
            return Math.max(0, kit.getCopies() - chosen);
        }
        
        /**
         * Increments the number of chosen copies
         */
        public void choose() {
            this.chosen++;
            menuItem.setItem(kit.getMenuItem(getAvailableCopies()));
        }
        
        /**
         * Decrements the number of chosen copies
         */
        public void unChoose() {
            this.chosen--;
            menuItem.setItem(kit.getMenuItem(getAvailableCopies()));
        }
    }
    
    /**
     * The kits to pick from, mapped to their unique name
     */
    private final Map<String, KitData> kits;
    private final Map<UUID, ParticipantData> participants;
    private final ItemStack netherStar;
    private final Color leatherColor;
    
    public KitPicker(
            Map<String, FinalGameKit> kits,
            Collection<K> participants,
            ItemStack netherStar,
            @NotNull Color leatherColor) {
        this.participants = participants.stream()
                .collect(Collectors.toMap(Participant::getUniqueId, ParticipantData::new));
        this.kits = new HashMap<>(kits.size());
        this.netherStar = netherStar;
        this.leatherColor = leatherColor;
        for (Map.Entry<String, FinalGameKit> entry : kits.entrySet()) {
            FinalGameKit kit = entry.getValue();
            String kitID = entry.getKey();
            KitData kitData = new KitData(kitID, kit);
            this.kits.put(kitID, kitData);
            GuiItem menuItem = kitData.getMenuItem();
            menuItem.setAction(event -> onKitMenuItemClick(event, kitData));
        }
    }
    
    /**
     * When a menu item
     * @param event the event
     * @param kitData the kitData associated with this menu item, which triggers this method on click
     */
    private void onKitMenuItemClick(InventoryClickEvent event, KitData kitData) {
        ParticipantData participantData = participants.get(event.getWhoClicked().getUniqueId());
        if (participantData == null) {
            return;
        }
        if (participantData.hasKit(kitData.getId())) {
            unChooseKit(participantData, true);
            participants.values().forEach(ParticipantData::updateGui);
            return;
        }
        if (!kitData.canBeChosen()) {
            participantData.getParticipant().sendMessage(Component.empty()
                    .append(Component.text("Kit already chosen")));
            return;
        }
        // if they have any kit
        if (participantData.hasKit()) {
            // deselect kit
            unChooseKit(participantData, true);
        }
        // choose the new kit
        chooseKit(participantData, kitData);
        participants.values().forEach(ParticipantData::updateGui);
    }
    
    private ChestGui createGui() {
        int entireHeight = 1;
        ChestGui gui = new ChestGui(
                entireHeight,
                ComponentHolder.of(Component.empty()
                        .append(Component.text("Choose a kit")
                                .color(NamedTextColor.DARK_GRAY)))
        );
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        OutlinePane pane = new OutlinePane(0, 0, 9, entireHeight);
        for (KitData kitData : kits.values()) {
            pane.addItem(kitData.getMenuItem());
        }
        gui.addPane(pane);
        gui.update();
        gui.setOnClose(event -> {
            ParticipantData participantData = participants.get(event.getPlayer().getUniqueId());
            if (participantData == null) {
                return;
            }
            if (!participantData.hasKit()) {
                participantData.getParticipant().sendMessage(Component.empty()
                        .append(Component.text("Please choose a kit. Use nether star to pick."))
                        .color(NamedTextColor.DARK_RED));
            }
            participantData.getParticipant().getInventory().addItem(netherStar);
        });
        return gui;
    }
    
    public void showGuis() {
        for (ParticipantData participantData : participants.values()) {
            participantData.showGui(createGui());
        }
    }
    
    public void stop(boolean assignClasses) {
        // assign kits to participants who didn't pick a kit
        // TODO: make this more efficient by checking if ANY kits are unassigned
        for (ParticipantData participantData : this.participants.values()) {
            if (assignClasses && !participantData.hasKit()) {
                assignKitToUnassigned(participantData);
            }
            participantData.deleteGuiAndHide();
            participantData.getParticipant().setKitId(participantData.getKitId());
        }
        this.participants.clear();
        this.kits.clear();
    }
    
    /**
     * @param participantData a participant without a kit assigned
     */
    private void assignKitToUnassigned(ParticipantData participantData) {
        for (KitData kitData : kits.values()) {
            if (kitData.canBeChosen()) {
                chooseKit(participantData, kitData);
                return;
            }
        }
        chooseKit(participantData, kits.get(0));
    }
    
    public void showGui(Participant participant) {
        ParticipantData participantData = participants.get(participant.getUniqueId());
        if (participantData == null) {
            return;
        }
        participantData.getParticipant().getInventory().removeItemAnySlot(netherStar);
        participantData.showGui();
    }
    
    /**
     * Mark the kit as chosen and message the participant
     * @param participantData the participant who chose
     * @param kitData the kit that was chosen
     */
    private void chooseKit(ParticipantData participantData, KitData kitData) {
        participantData.setKitId(kitData.getId());
        kitData.choose();
        participantData.getParticipant().getInventory().setContents(kitData.getKit().getLoadout());
        ColorMap.colorLeatherArmor(participantData.getParticipant(), leatherColor);
        participantData.getParticipant().sendMessage(Component.empty()
                .append(Component.text("Your kit is "))
                .append(kitData.getKit().getDisplayName()
                        .decorate(TextDecoration.BOLD))
                .color(NamedTextColor.GREEN));
    }
    
    /**
     * Mark the kit as un-chosen and message the participant
     * @param participantData the participant who un-chose
     */
    private void unChooseKit(ParticipantData participantData, boolean message) {
        KitData kitData = kits.get(participantData.getKitId());
        kitData.unChoose();
        participantData.setKitId(null);
        participantData.getParticipant().getInventory().clear();
        if (message) {
            participantData.getParticipant().sendMessage(Component.empty()
                    .append(Component.text("Deselected "))
                    .append(kitData.getKit().getDisplayName()
                            .decorate(TextDecoration.BOLD))
                    .color(NamedTextColor.GREEN));
        }
    }
    
    /**
     * @return each participant's UUID mapped to their assigned kit
     */
    public Map<UUID, FinalGameKit> getAssignedKits() {
        Map<UUID, FinalGameKit> assignedKits = new HashMap<>(participants.size());
        for (ParticipantData participantData : participants.values()) {
            KitData kitData = kits.get(participantData.getKitId());
            assignedKits.put(participantData.getParticipant().getUniqueId(), kitData.getKit());
        }
        return assignedKits;
    }
    
    public void addParticipant(K participant) {
        participants.put(participant.getUniqueId(), new ParticipantData(participant));
    }
    
    public void removeParticipant(UUID uuid) {
        ParticipantData participantData = participants.remove(uuid);
        if (participantData == null) {
            return;
        }
        unChooseKit(participantData, false);
    }
    
}

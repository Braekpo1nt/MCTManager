package org.braekpo1nt.mctmanager.games.game.finalgame;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class KitPicker {
    
    @Data
    private static class ParticipantData {
        /**
         * The participant represented by this data
         */
        private final Participant participant;
        /**
         * The ID of the kit chosen by this participant
         * -1 indicates no kit chosen
         */
        private int kitId;
        /**
         * The kit picker gui that this participant has open.
         * Null if they are not looking at the kit picker gui.
         */
        private @Nullable ChestGui gui;
        
        public ParticipantData(Participant participant) {
            this.participant = participant;
            this.kitId = -1;
        }
        
        /**
         * @return true if this participant has a kit selected
         */
        public boolean hasKit() {
            return kitId > -1;
        }
        
        /**
         * @param kitId the kitId to check for
         * @return true if the given kitId matches this participant's {@link #kitId},
         * false otherwise
         */
        public boolean hasKit(int kitId) {
            return this.kitId == kitId;
        }
        
        public void updateGui() {
            if (gui == null) {
                return;
            }
            gui.update();
        }
        
        public void showGui(ChestGui gui) {
            this.gui = gui;
            gui.show(participant.getPlayer());
        }
    }
    
    @Data
    private static class KitData {
        /**
         * The internal identifier of this kit
         */
        private final int id;
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
        
        public KitData(int id, FinalGameKit kit) {
            this.id = id;
            this.kit = kit;
            this.chosen = 0;
            this.menuItem = new GuiItem(kit.getMenuItem().asQuantity(getAvailableCopies()));
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
            return Math.min(0, kit.getCopies() - chosen);
        }
        
        /**
         * Increments the number of chosen copies
         */
        public void choose() {
            this.chosen++;
            int available = getAvailableCopies();
            menuItem.setItem(kit.getMenuItem().asQuantity(available));
        }
        
        /**
         * Decrements the number of chosen copies
         */
        public void unChoose() {
            this.chosen--;
            int available = getAvailableCopies();
            menuItem.setItem(kit.getMenuItem(available));
        }
    }
    
    /**
     * The kits to pick from, mapped to their ID
     */
    private final Map<Integer, KitData> kits;
    private final Map<Integer, GuiItem> kitGuiItems;
    private final Map<UUID, ParticipantData> participants;
    
    public KitPicker(List<FinalGameKit> kits, Collection<? extends Participant> participants) {
        this.participants = participants.stream()
                .collect(Collectors.toMap(Participant::getUniqueId, ParticipantData::new));
        this.kits = new HashMap<>(kits.size());
        this.kitGuiItems = new HashMap<>(kits.size());
        for (int i = 0; i < kits.size(); i++) {
            FinalGameKit kit = kits.get(i);
            KitData kitData = new KitData(i, kit);
            this.kits.put(i, kitData);
            GuiItem menuItem = kitData.getMenuItem();
            this.kitGuiItems.put(i, menuItem);
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
            // do nothing, they've already picked this kit
            return;
        }
        if (!kitData.canBeChosen()) {
            participantData.getParticipant().sendMessage(Component.empty()
                    .append(Component.text()));
            return;
        }
        if (participantData.hasKit()) {
            // deselect kit
            unChooseKit(participantData, false);
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
        for (GuiItem guiItem : kitGuiItems.values()) {
            pane.addItem(guiItem);
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
        });
        return gui;
    }
    
    public void start() {
        for (ParticipantData participantData : participants.values()) {
            participantData.showGui(createGui());
        }
    }
    
    public void stop(boolean assignClasses) {
        
    }
    
    /**
     * Mark the kit as chosen and message the participant
     * @param participantData the participant who chose
     * @param kitData the kit that was chosen
     */
    private void chooseKit(ParticipantData participantData, KitData kitData) {
        participantData.setKitId(kitData.getId());
        kitData.choose();
        participantData.getParticipant().sendMessage(Component.empty()
                .append(Component.text("Selected "))
                .append(kitData.getKit().getName()
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
        participantData.setKitId(-1);
        if (message) {
            participantData.getParticipant().sendMessage(Component.empty()
                    .append(Component.text("Deselected "))
                    .append(kitData.getKit().getName()
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
    
    public void addParticipant(Participant participant) {
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

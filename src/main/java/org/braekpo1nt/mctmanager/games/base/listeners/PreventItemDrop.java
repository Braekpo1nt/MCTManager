package org.braekpo1nt.mctmanager.games.base.listeners;

import org.braekpo1nt.mctmanager.games.base.GameBase;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Prevent items from being dropped out of participant's inventories, either
 * by pressing the Q button or clicking and dragging out of the inventory edges.
 * Optionally also prevents participants from removing their armor. 
 * @param <P>
 */
public class PreventItemDrop<P extends ParticipantData> extends GameListener<P> {
    
    private final boolean stickyArmor;
    
    /**
     * @param context the context
     * @param stickyArmor true if players should be unable to remove their armor,
     *                    false otherwise.
     */
    public PreventItemDrop(@NotNull GameBase<P, ?, ?, ?, ?> context, boolean stickyArmor) {
        super(context);
        this.stickyArmor = stickyArmor;
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        P participant = context.getParticipant(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        P participant = context.getParticipant(event.getWhoClicked().getUniqueId());
        if (participant == null) {
            return;
        }
        onParticipantClickInventory(event, participant);
    }
    
    /**
     * Called when a participant from {@link #context} triggers an {@link InventoryClickEvent} 
     * @param event the event
     * @param participant the participant who triggered the event
     */
    public void onParticipantClickInventory(@NotNull InventoryClickEvent event, @NotNull P participant) {
        // don't let them drop items from their inventory
        if (GameManagerUtils.INV_REMOVE_ACTIONS.contains(event.getAction())) {
            event.setCancelled(true);
            return;
        }
        // don't let them remove their armor
        if (stickyArmor && (event.getSlotType() == InventoryType.SlotType.ARMOR)) {
            event.setCancelled(true);
        }
    }
    
}

package org.braekpo1nt.mctmanager.games.experimental;

import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
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
public class PreventItemDrop<P> extends GameListener<P> {
    
    private final boolean stickyArmor;
    
    /**
     * @param gameData the gameData containing the participant list
     * @param stickyArmor true if players should be unable to remove their armor,
     *                    false otherwise.
     */
    public PreventItemDrop(@NotNull GameData<P> gameData, boolean stickyArmor) {
        super(gameData);
        this.stickyArmor = stickyArmor;
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        P participant = gameData.getParticipant(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        P participant = gameData.getParticipant(event.getWhoClicked().getUniqueId());
        if (participant == null) {
            return;
        }
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

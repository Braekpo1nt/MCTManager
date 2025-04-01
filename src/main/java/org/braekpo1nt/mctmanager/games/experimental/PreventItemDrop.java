package org.braekpo1nt.mctmanager.games.experimental;

import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;

public class PreventItemDrop<P> extends GameListener<P> {
    
    private final boolean stickyArmor;
    
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

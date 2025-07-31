package org.braekpo1nt.mctmanager.games.base.listeners;

import org.braekpo1nt.mctmanager.games.base.GameBase;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Prevent items from being dropped out of participant's inventories, either
 * by pressing the Q button or clicking and dragging out of the inventory edges.
 * Optionally also prevents participants from removing their armor. 
 * @param <P>
 */
public class PreventItemDrop<P extends ParticipantData> extends GameListener<P> {
    
    private final boolean stickyArmor;
    private final @Nullable Function<ItemStack, Boolean> shouldDrop;
    
    /**
     * A predicate function is used to check on a case-by-case basis whether an item
     * can or can't be dropped.
     * @param context the context
     * @param stickyArmor true if players should be unable to remove their armor,
     *                    false otherwise.
     * @param shouldDrop a function which takes in an item, and returns a boolean indicating whether
     *                   the player is allowed to drop the item. True means they can drop it,
     *                   false means they can't. Can be null (if so, all items are assumed to be
     *                   un-droppable)
     */
    public PreventItemDrop(@NotNull GameBase<P, ?, ?, ?, ?> context, boolean stickyArmor, @Nullable Function<@NotNull ItemStack, @NotNull Boolean> shouldDrop) {
        super(context);
        this.stickyArmor = stickyArmor;
        this.shouldDrop = shouldDrop;
    }
    
    /**
     * All items are prevented from being dropped, either through the UI or through the drop key.
     * @param context the context
     * @param stickyArmor true if players should be unable to remove their armor,
     *                    false otherwise.
     */
    public PreventItemDrop(@NotNull GameBase<P, ?, ?, ?, ?> context, boolean stickyArmor) {
        this(context, stickyArmor, null);
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        P participant = context.getParticipant(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        if (shouldDrop == null) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(shouldDrop.apply(event.getItemDrop().getItemStack()));
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
        // don't let them remove their armor
        if (stickyArmor && (event.getSlotType() == InventoryType.SlotType.ARMOR)) {
            event.setCancelled(true);
            return;
        }
        // don't let them drop items from their inventory
        if (GameManagerUtils.INV_REMOVE_ACTIONS.contains(event.getAction())) {
            if (shouldDrop == null) {
                event.setCancelled(true);
                return;
            }
            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null) {
                return;
            }
            event.setCancelled(shouldDrop.apply(currentItem));
        }
    }
    
}

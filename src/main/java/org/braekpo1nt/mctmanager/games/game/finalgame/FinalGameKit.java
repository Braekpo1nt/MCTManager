package org.braekpo1nt.mctmanager.games.game.finalgame;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
public class FinalGameKit {
    private Component displayName;
    private Material menuItemMaterial;
    private List<Component> menuItemLore;
    /**
     * How many participants may have this kit at once.
     * Note that the first kit will be used for overflow if not enough kits
     * are provided for the number of players. E.g. there are three players and two
     * kits, and both kits only have 1 copie allowed, the 3rd player will get the first kit
     * when kit selection is over.
     */
    private int copies;
    /**
     * List of spawn locations for this kit (will be looped if more
     * pick it than there are spawns, but otherwise cycled) for
     * the north side
     */
    private List<Location> northSpawns;
    /**
     * List of spawn locations for this kit (will be looped if more
     * pick it than there are spawns, but otherwise cycled) for
     * the south side
     */
    private List<Location> southSpawns;
    /**
     * If false, participants using this kit can't melee attack.
     * Defaults to true.
     */
    private boolean melee;
    /**
     * The items in the initial loadout
     */
    private ItemStack[] loadout;
    /**
     * The refills that will be given to the participants using this kit
     * every {@link #refillSeconds} seconds. If empty, no refills will be given.
     */
    private List<Refill> refills;
    /**
     * How many seconds between refills. Less than 1 means no refills will be given.
     * Defaults to 20s.
     */
    private int refillSeconds;
    /**
     * Whether a banner should appear on the head of a participant using this kit.
     * defaults to false.
     */
    private boolean hasBanner;
    
    /**
     * @param amount the amount of the item to be in the stack
     * (less than 1 returns a gray stained-glass pane)
     * @return the menu item associated with this kit in the quantity given, or
     * a {@link Material#GRAY_STAINED_GLASS_PANE} if the amount is less than 1
     */
    public ItemStack getMenuItem(int amount) {
        ItemStack menuItem;
        if (amount >= 1) {
            menuItem = new ItemStack(menuItemMaterial, amount);
        } else {
            menuItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        }
        menuItem.editMeta(meta -> {
            meta.displayName(Component.empty()
                    .append(displayName)
                    .append(Component.empty()
                            .append(Component.text(" ("))
                            .append(Component.text(amount))
                            .append(Component.text(")"))
                    )
            );
            meta.lore(menuItemLore);
        });
        return menuItem;
    }
    
    /**
     * Refill the participants inventory with the items in this refill,
     * up to the max amount of each item.
     * @param participant the participant to give refills to
     */
    public void refill(@NotNull FinalParticipant participant) {
        if (refills.isEmpty()) {
            return;
        }
        Map<Material, Integer> itemCounts = refills.stream()
                .collect(Collectors.toMap(Refill::getMaterial, item -> 0));
        for (@Nullable ItemStack itemStack : participant.getInventory().getContents()) {
            if (itemStack != null) {
                // only need to keep track of items with refills
                itemCounts.computeIfPresent(itemStack.getType(), (material, count) -> count + itemStack.getAmount());
            }
        }
        for (Refill refill : refills) {
            // how many of this item are in the participant's inventory
            int numberInInventory = itemCounts.get(refill.getMaterial());
            refill.refill(numberInInventory, participant.getInventory());
        }
    }
    
    @Data
    @Builder
    public static class Refill {
        /**
         * The material type of the item to be given
         */
        private Material material;
        /**
         * How many of the item to give on refill
         */
        private int amount;
        /**
         * how many of the given item participants are permitted to have at once
         */
        private int max;
        
        /**
         * @param numberInInventory the number of items of {@link #material} in the given inventory
         * @param inventory the inventory to refill
         */
        public void refill(int numberInInventory, @NotNull PlayerInventory inventory) {
            int availableSpace = numberInInventory - max;
            int amountToRefill = Math.min(availableSpace, amount);
            if (amountToRefill > 0) {
                inventory.addItem(new ItemStack(material, amountToRefill));
            }
        }
    }
    
}

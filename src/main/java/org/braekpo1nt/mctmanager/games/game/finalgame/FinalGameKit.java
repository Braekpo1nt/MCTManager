package org.braekpo1nt.mctmanager.games.game.finalgame;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Data
public class FinalGameKit {
    private Component name;
    /**
     * The item representing the kit for gui menus,
     * with custom name and lore
     * Always as a stack of 1, don't change this item's stack count
     */
    private ItemStack menuItem;
    /**
     * How many participants may have this kit at once
     */
    private int copies;
    /**
     * List of spawn locations for this kit (will be looped if more
     * pick it than there are spawns, but otherwise cycled)
     */
    private List<Location> spawns;
    /**
     * If false, participants using this kit can't melee attack
     */
    private boolean melee;
    /**
     * The items in the initial loadout
     */
    private ItemStack[] loadout;
    /**
     * The refills that will be given to the participants using this kit
     * every {@link #refillSeconds} seconds
     */
    private List<Refill> refills;
    /**
     * How many seconds between refills
     */
    private int refillSeconds;
    /**
     * Whether banners should appear on the heads of participants using this kit
     */
    private boolean hasBanners;
    
    /**
     * @param amount the amount of the item to be in the stack
     * (less than 1 returns a gray stained-glass pane)
     * @return the menu item associated with this kit in the quantity given, or
     * a {@link Material#GRAY_STAINED_GLASS_PANE} if the amount is less than 1
     */
    public ItemStack getMenuItem(int amount) {
        if (amount >= 1) {
            return menuItem.asQuantity(amount);
        }
        return menuItem.asQuantity(amount).withType(Material.GRAY_STAINED_GLASS_PANE);
    }
    
    @Data
    public static class Refill {
        /**
         * The item to be given
         */
        private ItemStack item;
        /**
         * How many of the item to give on refill
         */
        private int amount;
        /**
         * how many of the given item participants are permitted to have at once
         */
        private int max;
    }
    
}

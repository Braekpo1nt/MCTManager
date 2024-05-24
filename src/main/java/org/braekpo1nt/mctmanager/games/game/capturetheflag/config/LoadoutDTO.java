package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Loadout;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.PlayerInventoryDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta.ItemMetaDTO;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.kyori.adventure.text.Component;

import java.util.List;

/**
 * Represents a loadout for a BattleClass
 *
 */
@Data
class LoadoutDTO {
    /**
     * The name of this loadout (used in chat messages to communicate to the player which loadout they chose)
     */
    private @Nullable Component name;
    /**
     * the item Material Type to use in the ClassPicker menu to represent the BattleClass
     */
    private @Nullable Material menuItem;
    /**
     * the description of the class in the form of item lore
     */
    private @Nullable List<@Nullable Component> menuLore;
    /**
     * optional item meta for the ClassPicker menu. (for example, if a particular potion is desired). The name and lore will be overwritten by this LoadoutDTO's name and menuLore.
     */
    private @Nullable ItemMetaDTO menuMeta;
    /**
     * the player's inventory when they select this class
     */
    private @Nullable PlayerInventoryDTO inventory;
    
    public void isValid() {
        Preconditions.checkArgument(name != null, "name can't be null");
        Preconditions.checkArgument(menuItem != null, "menuItem can't be null");
        Preconditions.checkArgument(menuLore != null, "lore can't be null");
        if (menuMeta != null) {
            menuMeta.isValid();
        }
        Preconditions.checkArgument(inventory != null, "inventory can't be null");
        inventory.isValid();
    }
    
    @NotNull Loadout toLoadout() {
        ItemStack[] contents;
        if (inventory != null) {
            contents = inventory.toInventoryContents();
        } else {
            contents = new ItemStack[0]; //allowed to be 0
        }
        Preconditions.checkState(menuItem != null, "menuItem can't be null when toLoadout is called");
        ItemStack newMenuItem = new ItemStack(menuItem);
        if (menuMeta != null) {
            newMenuItem.editMeta(meta -> menuMeta.toItemMeta(meta, newMenuItem.getType()));
        }
        newMenuItem.editMeta(meta -> {
            meta.displayName(name);
            meta.lore(menuLore);
        });
        return new Loadout(name, newMenuItem, contents);
    }
}

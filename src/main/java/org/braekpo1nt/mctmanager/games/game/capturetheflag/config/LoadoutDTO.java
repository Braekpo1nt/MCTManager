package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import lombok.Getter;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Loadout;
import org.braekpo1nt.mctmanager.games.game.config.ConfigUtil;
import org.braekpo1nt.mctmanager.games.game.config.inventory.ItemStackDTO;
import org.braekpo1nt.mctmanager.games.game.config.inventory.PlayerInventoryDTO;
import org.braekpo1nt.mctmanager.games.game.config.inventory.meta.ItemMetaDTO;
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
@Getter
class LoadoutDTO {
    /**
     * The name of this loadout (used in chat messages to communicate to the player which loadout they chose)
     */
    private @Nullable JsonElement name;
    /**
     * the item Material Type to use in the ClassPicker menu to represent the BattleClass
     */
    private @Nullable Material menuItem;
    /**
     * the description of the class in the form of item lore
     */
    private @Nullable List<@Nullable JsonElement> menuLore;
    /**
     * the item to use in the ClassPicker menu to represent the BattleClass. The display name will be overwritten to be {@link LoadoutDTO#name} and the amount will be set to 1.
     */
    private @Nullable ItemStackDTO item;
    /**
     * the player's inventory when they select this class
     */
    private @Nullable PlayerInventoryDTO inventory;
    
    public void isValid() {
        Preconditions.checkArgument(name != null, "name can't be null");
        if (item == null) {
            // 0.1.0 backwards compatibility
            if (menuItem == null && menuLore == null) {
                throw new IllegalArgumentException("menuItem and menuLore can't be null if item is null (backwards compatibility with 0.1.0)");
            }
            Preconditions.checkArgument(menuItem != null, "menuItem can't be null");
            Preconditions.checkArgument(menuLore != null, "lore can't be null");
            ItemMetaDTO.toLore(menuLore);
        }
        Preconditions.checkArgument(inventory != null, "inventory can't be null");
        inventory.isValid();
    }
    
    @NotNull Loadout toLoadout() {
        Component menuName = ConfigUtil.toComponent(name);
        ItemStack[] contents;
        if (inventory != null) {
            contents = inventory.toInventoryContents();
        } else {
            contents = new ItemStack[41];
        }
        if (item == null) {
            // 0.1.0 backwards compatibility
            if (menuItem == null && menuLore == null) {
                throw new IllegalArgumentException("menuItem and menuLore can't be null if item is null");
            }
            List<Component> menuDescription = ItemMetaDTO.toLore(menuLore);
            return new Loadout(menuName, menuItem, menuDescription, contents);
        }
        return new Loadout(menuName, item.toItemStack(), contents);
    }
}

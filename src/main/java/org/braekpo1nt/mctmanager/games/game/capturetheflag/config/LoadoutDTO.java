package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import lombok.Getter;
import org.braekpo1nt.mctmanager.games.game.config.inventory.InventoryDTO;
import org.braekpo1nt.mctmanager.games.game.config.inventory.ItemStackDTO;
import org.braekpo1nt.mctmanager.games.game.config.inventory.PlayerInventoryDTO;
import org.braekpo1nt.mctmanager.games.game.config.inventory.meta.ItemMetaDTO;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

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
     * 
     */
    private @Nullable List<@Nullable JsonElement> menuLore;
    /**
     * the item to use in the ClassPicker menu to represent the BattleClass
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
}

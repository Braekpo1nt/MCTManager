package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

import com.google.gson.JsonElement;
import org.braekpo1nt.mctmanager.games.game.config.inventory.InventoryContentsDTO;
import org.bukkit.Material;

import java.util.List;

/**
 * Represents a loadout for a BattleClass
 *
 * @param menuItem  the item Material Type to use in the ClassPicker menu to represent the BattleClass
 * @param name  the name of the battle class. Used as the menu item display name and in chat messages.
 * @param menuLore  the description to show on the menu item when you hover over it (it's just an item lore)
 * @param inventory the player's inventory when they select this class
 */
record LoadoutDTO(Material menuItem, JsonElement name, List<JsonElement> menuLore, InventoryContentsDTO inventory) {
}

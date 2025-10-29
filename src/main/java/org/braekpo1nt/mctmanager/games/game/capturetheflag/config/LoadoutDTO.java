package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.PlayerInventoryDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.meta.ItemMetaDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Loadout;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a loadout for a BattleClass
 */
@Data
class LoadoutDTO implements Validatable {
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
     * optional item meta for the ClassPicker menu. (for example, if a particular potion is desired). The name and lore
     * will be overwritten by this LoadoutDTO's name and menuLore.
     */
    private @Nullable ItemMetaDTO menuMeta;
    /**
     * the player's inventory when they select this class
     */
    private @Nullable PlayerInventoryDTO inventory;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(name, "name");
        validator.notNull(menuItem, "menuItem");
        validator.notNull(menuLore, "lore");
        if (menuMeta != null) {
            menuMeta.validate(validator.path("menuMeta"));
        }
        validator.notNull(inventory, "inventory");
        inventory.validate(validator.path("inventory"));
    }
    
    static Map<String, Loadout> toLoadouts(Map<String, LoadoutDTO> loadoutDTOS) {
        Map<String, Loadout> newLoadouts = new HashMap<>();
        for (Map.Entry<String, LoadoutDTO> entry : loadoutDTOS.entrySet()) {
            String battleClass = entry.getKey();
            LoadoutDTO loadout = entry.getValue();
            newLoadouts.put(battleClass, LoadoutDTO.toLoadout(loadout));
        }
        return newLoadouts;
    }
    
    static @NotNull Loadout toLoadout(LoadoutDTO loadoutDTO) {
        ItemStack[] contents;
        if (loadoutDTO.inventory != null) {
            contents = loadoutDTO.inventory.toInventoryContents();
        } else {
            contents = new ItemStack[0]; //allowed to be 0
        }
        Preconditions.checkState(loadoutDTO.menuItem != null, "menuItem when toLoadout is called");
        ItemStack newMenuItem = new ItemStack(loadoutDTO.menuItem);
        if (loadoutDTO.menuMeta != null) {
            newMenuItem.editMeta(meta -> loadoutDTO.menuMeta.toItemMeta(meta, newMenuItem.getType()));
        }
        newMenuItem.editMeta(meta -> {
            meta.displayName(loadoutDTO.name);
            meta.lore(loadoutDTO.menuLore);
        });
        return new Loadout(loadoutDTO.name, newMenuItem, contents);
    }
}

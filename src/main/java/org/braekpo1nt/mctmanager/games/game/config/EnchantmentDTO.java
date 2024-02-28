package org.braekpo1nt.mctmanager.games.game.config;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;

public record EnchantmentDTO(String name, int level) {
    public Enchantment toEnchantment() {
        return new EnchantmentWrapper(name);
    } 
}

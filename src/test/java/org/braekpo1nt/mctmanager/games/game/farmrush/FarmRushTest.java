package org.braekpo1nt.mctmanager.games.game.farmrush;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FarmRushTest {
    
    static final Map<Material, ItemSale> materialScores;
    
    static {
        materialScores = new HashMap<>();
        materialScores.put(Material.IRON_INGOT, new ItemSale(1, 14));
        materialScores.put(Material.RAW_IRON, new ItemSale(1, 10));
        materialScores.put(Material.DIAMOND, new ItemSale(1, 35));
        materialScores.put(Material.OBSIDIAN, new ItemSale(1, 40));
        materialScores.put(Material.COAL, new ItemSale(1, 4));
        materialScores.put(Material.RAW_COPPER, new ItemSale(1, 2));
        materialScores.put(Material.COPPER_INGOT, new ItemSale(1, 4));
        materialScores.put(Material.GRANITE, new ItemSale(1, 1));
        materialScores.put(Material.DIORITE, new ItemSale(1, 1));
        materialScores.put(Material.WHEAT, new ItemSale(1, 2));
        materialScores.put(Material.BEEF, new ItemSale(1, 4));
        materialScores.put(Material.COOKED_BEEF, new ItemSale(1, 8));
        materialScores.put(Material.LEATHER, new ItemSale(1, 15));
    }
    
    @Test
    void bookTest() {
        List<Component> pages = FarmRushGame.createPages(materialScores, 1);
        Assertions.assertEquals(2, pages.size());
    }
    
}

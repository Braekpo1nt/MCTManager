package org.braekpo1nt.mctmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.braekpo1nt.mctmanager.games.game.config.inventory.InventoryContentsDTO;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

class ItemStackDTOTest {
    
    Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    
    @Test
    void test1() {
        ItemStack leatherBoots = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta = leatherBoots.getItemMeta();
        String json = gson.toJson(itemMeta);
        System.out.println(json);
    }
    
    @Test
    void configuration() {
        ItemStack leatherBoots = new ItemStack(Material.LEATHER_BOOTS);
        System.out.println(leatherBoots.serialize());
    }
    
    
    @Test
    public void testJsonFromFile() throws IOException {
        InputStream inputStream = ItemStackDTOTest.class.getResourceAsStream("exampleInventory.json");
        Assertions.assertNotNull(inputStream, "JSON file not found");
        String jsonString = new String(inputStream.readAllBytes());
        InventoryContentsDTO result = gson.fromJson(jsonString, InventoryContentsDTO.class);
        ItemStack[] contents = result.toInventoryContents();
        Assertions.assertNotNull(contents);
        for (ItemStack itemStack : contents) {
            System.out.println(itemStack.serialize());
        }
    }
    
}

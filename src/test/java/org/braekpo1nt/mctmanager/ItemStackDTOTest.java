package org.braekpo1nt.mctmanager;

import be.seeseemelk.mockbukkit.inventory.ItemFactoryMock;
import be.seeseemelk.mockbukkit.inventory.meta.ItemMetaMock;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class ItemStackDTOTest {
    
    @Test
    void test1() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        Multimap<Attribute, AttributeModifier> attributeModifiers = LinkedHashMultimap.create();
    
        attributeModifiers.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier("modifier1", 5.0, AttributeModifier.Operation.ADD_NUMBER));
        attributeModifiers.put(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier("modifier2", -2.0, AttributeModifier.Operation.ADD_SCALAR));
        attributeModifiers.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier("modifier2", -2.0, AttributeModifier.Operation.ADD_SCALAR));
    
        System.out.println("Modifiers for GENERIC_MAX_HEALTH: " + attributeModifiers.get(Attribute.GENERIC_MAX_HEALTH));
        String json = gson.toJson(attributeModifiers.asMap());
        System.out.println(json);
    }
    
    @Test
    void test2() {
        // Create a Multimap to store Attribute to AttributeModifier mappings
        Multimap<Attribute, AttributeModifier> attributeModifiers = LinkedHashMultimap.create();
    
        // Add some AttributeModifier instances to the Multimap
        AttributeModifier modifier1 = new AttributeModifier("modifier1", 5.0, AttributeModifier.Operation.ADD_NUMBER);
        AttributeModifier modifier2 = new AttributeModifier("modifier2", -2.0, AttributeModifier.Operation.ADD_SCALAR);
    
        attributeModifiers.put(Attribute.GENERIC_MAX_HEALTH, modifier1);
        attributeModifiers.put(Attribute.GENERIC_ATTACK_DAMAGE, modifier2);
        attributeModifiers.put(Attribute.GENERIC_MAX_HEALTH, modifier2); // You can have multiple modifiers for the same attribute
    
        // You can retrieve modifiers for a specific attribute
        System.out.println("Modifiers for GENERIC_MAX_HEALTH: " + attributeModifiers.get(Attribute.GENERIC_MAX_HEALTH));
    
        // You can also retrieve all keys (attributes) in the Multimap
        System.out.println("Attributes in the Multimap: " + attributeModifiers.keySet());
    }
    
}

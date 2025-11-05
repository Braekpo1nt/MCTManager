package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.farmrush.ItemSale;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Data
public class ItemSaleDTO implements Validatable {
    private int requiredAmount;
    private int score;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.validate(requiredAmount > 0, "requiredAmount must be greater than 0");
    }
    
    public static Map<Material, ItemSale> toItemSales(Map<Material, ItemSaleDTO> dtos) {
        Map<Material, ItemSale> result = new HashMap<>(dtos.size());
        for (Map.Entry<Material, ItemSaleDTO> entry : dtos.entrySet()) {
            Material material = entry.getKey();
            result.put(material, entry.getValue().toItemSale(material));
        }
        return result;
    }
    
    public ItemSale toItemSale(@NotNull Material material) {
        return new ItemSale(
                material,
                requiredAmount,
                score
        );
    }
}

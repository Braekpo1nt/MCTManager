package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.farmrush.ItemSale;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Data
class ItemSaleDTO implements Validatable {
    /**
     * the amount of this item required to sell in order to earn one {@link #score}
     * (defaults to 1)
     */
    private int requiredAmount = 1;
    /**
     * the number of points earned for selling the {@link #requiredAmount} of this item
     */
    private int score;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.validate(requiredAmount > 0, "requiredAmount must be greater than 0");
    }
    
    public ItemSale toItemSale() {
        return new ItemSale(requiredAmount, score);
    }
    
    @Contract("null -> null; !null -> !null")
    public static <T> Map<T, ItemSale> toItemSales(Map<T, ItemSaleDTO> itemSaleDTOMap) {
        if (itemSaleDTOMap == null) {
            return null;
        }
        Map<T, ItemSale> result = new HashMap<>(itemSaleDTOMap.size());
        for (Map.Entry<T, ItemSaleDTO> entry : itemSaleDTOMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toItemSale());
        }
        return result;
    }
}

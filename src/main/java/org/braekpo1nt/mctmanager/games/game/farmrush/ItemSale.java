package org.braekpo1nt.mctmanager.games.game.farmrush;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

@Data
public class ItemSale implements Validatable {
    private final int requiredAmount;
    private final int score;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.validate(requiredAmount > 0, "requiredAmount must be greater than 0");
    }
}

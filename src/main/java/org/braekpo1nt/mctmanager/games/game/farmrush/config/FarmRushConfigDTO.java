package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

@Data
public class FarmRushConfigDTO implements Validatable {
    
    @Override
    public void validate(@NotNull Validator validator) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public FarmRushConfig toConfig() {
        return FarmRushConfig.builder()
                .build();
    }
}

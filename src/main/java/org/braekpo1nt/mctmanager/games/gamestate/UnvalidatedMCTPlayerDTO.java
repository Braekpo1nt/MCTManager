package org.braekpo1nt.mctmanager.games.gamestate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a player who has never logged on before
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class UnvalidatedMCTPlayerDTO implements Validatable {
    /**
     * The in-game-name of the unvalidated participant
     */
    private String ign;
    private @Nullable UUID offlineUniqueId;
    private String teamName;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(ign, "ign");
        validator.notNull(teamName, "teamName");
    }
}

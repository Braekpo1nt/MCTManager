package org.braekpo1nt.mctmanager.games.game.spleef.config;

import lombok.Getter;
import org.braekpo1nt.mctmanager.games.game.config.SoundDTO;
import org.jetbrains.annotations.Nullable;

@Getter
class PowerupDTO {
    int wieght = 1;
    @Nullable SoundDTO userSound;
    @Nullable SoundDTO affectedSound;
    
    void isValid() {
        throw new UnsupportedOperationException("not yet implemented");
    }
    
}

package org.braekpo1nt.mctmanager.games.game.spleef.config;

import lombok.Getter;
import org.braekpo1nt.mctmanager.games.game.config.SoundDTO;
import org.jetbrains.annotations.Nullable;

@Getter
class PowerupDTO {
    private int wieght = 1;
    private @Nullable SoundDTO userSound;
    private @Nullable SoundDTO affectedSound;
    
    void isValid() {
        if (userSound != null) {
            userSound.isValid();
        }
        if (affectedSound != null) {
            affectedSound.isValid();
        }
    }
    
}

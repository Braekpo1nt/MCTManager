package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.braekpo1nt.mctmanager.games.game.config.SoundDTO;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.Powerup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Getter
class PowerupDTO {
    private int weight = 1;
    private @Nullable SoundDTO userSound;
    private @Nullable SoundDTO affectedSound;
    /**
     * the valid sources which this powerup can be received from. If all sources are valid, make this null. If no sources are valid, make this empty. Null elements will be ignored.
     */
    private @Nullable List<Powerup.Source> sources;
    
    void isValid() {
        if (userSound != null) {
            userSound.isValid();
        }
        if (affectedSound != null) {
            affectedSound.isValid();
        }
        if (sources != null) {
            Preconditions.checkArgument(!sources.contains(null), "sources can't contain null elements");
        }
    }
    
}

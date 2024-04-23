package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.braekpo1nt.mctmanager.games.game.config.SoundDTO;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.Powerup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
class PowerupDTO {
    private int weight = 1;
    private @Nullable SoundDTO userSound;
    private @Nullable SoundDTO affectedSound;
    /**
     * the valid {@link Powerup.Source}s which this powerup can be received from. If all sources are valid, make this null. If no sources are valid, make this empty. Must not contain null elements
     */
    private @Nullable List<Powerup.@Nullable Source> sources;
    
    void isValid() {
        if (userSound != null) {
            userSound.isValid();
        }
        if (affectedSound != null) {
            affectedSound.isValid();
        }
    }
    
    /**
     * @return a list of the sources which this powerup can come from
     */
    @NotNull List<Powerup. @NotNull Source> getSources() {
        if (sources == null) {
            return Arrays.asList(Powerup.Source.values());
        }
        return sources.stream().filter(Objects::nonNull).toList();
    }
}

package org.braekpo1nt.mctmanager.games.game.spleef.config;

import lombok.Getter;
import org.braekpo1nt.mctmanager.config.dto.SoundDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.Powerup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
class PowerupDTO implements Validatable {
    private @Nullable SoundDTO userSound;
    private @Nullable SoundDTO affectedSound;
    /**
     * the {@link Powerup.Source}s which this powerup is restricted to. If all sources are valid, make this null. Otherwise, the powerup will only be given to players from the specified sources. If no sources are valid, make this empty.
     */
    private @Nullable List<Powerup.@Nullable Source> sources;
    
    @Override
    public void validate(Validator validator) {
        if (userSound != null) {
            userSound.isValid();
        }
        if (affectedSound != null) {
            affectedSound.isValid();
        }
    }
    
    /**
     * a convenience method to filter out null entries from {@link PowerupDTO#sources}. 
     * If sources is null, returns a list of all {@link Powerup.Source}s, because on the user's
     * side, unspecified sources indicates all sources are valid. 
     * @return a list of the sources which this powerup can come from
     */
    @NotNull List<Powerup. @NotNull Source> getSources() {
        if (sources == null) {
            return Arrays.asList(Powerup.Source.values());
        }
        return sources.stream().filter(Objects::nonNull).toList();
    }
}

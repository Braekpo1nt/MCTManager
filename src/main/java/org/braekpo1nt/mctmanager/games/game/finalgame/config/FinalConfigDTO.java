package org.braekpo1nt.mctmanager.games.game.finalgame.config;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@Data
public class FinalConfigDTO implements Validatable {
    
    private String version;
    private String world;
    private @Nullable BoundingBox spectatorArena;
    private LocationDTO spectatorSpawn;
    private Durations durations;
    private Component description;
    
    @Override
    public void validate(@NotNull Validator validator) {
        
    }
    
    public FinalConfig toConfig() {
        throw new UnsupportedOperationException("not implemented yet");
    }
    
    @Data
    static class Durations {
        private int description;
        private int roundStarting;
        private int classSelection;
        private int roundOver;
        private int gameOver;
    }
}

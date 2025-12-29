package org.braekpo1nt.mctmanager.games.game.finalgame.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
public class FinalConfigDTO implements Validatable {
    
    private String version;
    private String world;
    private @Nullable BoundingBox spectatorArea;
    private LocationDTO spectatorSpawn;
    /**
     * Maps the unique kit name to the kit details
     */
    private Map<String, FinalGameKitDTO> kits;
    private MapHalfDto northMap;
    private MapHalfDto southMap;
    private int requiredWins;
    private List<Material> preventInteractions;
    private @Nullable Material replacementType;
    
    private Durations durations;
    private Component description;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.version, "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version), "invalid config version (%s)", this.version);
        validator.notNull(this.world, "world");
        validator.notNull(Bukkit.getWorld(this.world), "Could not find world \"%s\"", this.world);
        if (spectatorArea != null) {
            BoundingBox spectatorArea = this.spectatorArea;
            validator.validate(spectatorArea.getVolume() >= 1.0, "spectatorArea (%s) volume (%s) must be at least 1.0", spectatorArea, spectatorArea.getVolume());
        }
        
        validator.notNull(this.spectatorSpawn, "spectatorSpawn");
        
        validator.notNull(kits, "kits");
        validator.validateMap(kits, "kits");
        
        validator.notNull(northMap, "northMap");
        validator.notNull(southMap, "southMap");
        northMap.validate(validator.path("northMap"));
        southMap.validate(validator.path("southMap"));
        
        validator.notNull(this.description, "description");
    }
    
    public FinalConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        
        return FinalConfig.builder()
                .world(newWorld)
                .spectatorBoundary(this.spectatorArea == null ? null :
                        new SpectatorBoundary(this.spectatorArea, this.spectatorSpawn.toLocation(newWorld)))
                .replacementType(this.replacementType != null ? this.replacementType : Material.WHITE_CONCRETE)
                .northMap(this.northMap.toMapHalf(Affiliation.NORTH, newWorld))
                .southMap(this.southMap.toMapHalf(Affiliation.SOUTH, newWorld))
                .kits(FinalGameKitDTO.toFinalGameKits(this.kits, newWorld))
                .spectatorSpawn(this.spectatorSpawn.toLocation(newWorld))
                .requiredWins(this.requiredWins)
                .preventInteractions(this.preventInteractions != null ? this.preventInteractions : Collections.emptyList())
                .descriptionDuration(this.durations.description)
                .roundStartingDuration(this.durations.roundStarting)
                .classSelectionDuration(this.durations.classSelection)
                .roundOverDuration(this.durations.roundOver)
                .gameOverDuration(this.durations.gameOver)
                .description(this.description)
                .build();
    }
    
    @Data
    static class MapHalfDto implements Validatable {
        private LocationDTO spawn;
        private BoundingBox replacementArea;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(spawn, "spawn");
            validator.notNull(replacementArea, "replacementArea");
        }
        
        public FinalConfig.MapHalf toMapHalf(@NotNull Affiliation affiliation, @NotNull World world) {
            return new FinalConfig.MapHalf(
                    affiliation,
                    this.spawn.toLocation(world),
                    replacementArea
            );
        }
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

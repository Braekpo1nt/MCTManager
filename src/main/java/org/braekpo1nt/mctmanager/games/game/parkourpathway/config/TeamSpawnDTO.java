package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.TeamSpawn;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@AllArgsConstructor
class TeamSpawnDTO implements Validatable {
    /**
     * the area which will be used to place a glass barrier. Air blocks will be replaced with stained-glass of the appropriate team color. 
     */
    private BoundingBoxDTO barrierArea;
    /**
     * the location where a team should spawn
     */
    private LocationDTO spawn;
    
    @Override
    public void validate(Validator validator) {
        validator.notNull(barrierArea, "barrierArea");
        validator.notNull(spawn, "spawn");
    }
    
    @NotNull TeamSpawn toTeamSpawn(@NotNull World world) {
        return new TeamSpawn(world, barrierArea.toBoundingBox(), spawn.toLocation(world));
    }
    
    public static @NotNull List<TeamSpawn> toTeamSpawns(@NotNull World world, @NotNull List<@NotNull TeamSpawnDTO> teamSpawnDTOS) {
        return teamSpawnDTOS.stream().map(teamSpawnDTO -> teamSpawnDTO.toTeamSpawn(world)).toList();
    }
    
    @NotNull static TeamSpawnDTO fromTeamSpawn(TeamSpawn teamSpawn) {
        return new TeamSpawnDTO(BoundingBoxDTO.from(teamSpawn.getBarrierArea()), LocationDTO.from(teamSpawn.getSpawnLocation()));
    }
    
    public static List<TeamSpawnDTO> fromTeamSpawns(List<TeamSpawn> teamSpawns) {
        return teamSpawns.stream().map(TeamSpawnDTO::fromTeamSpawn).toList();
    }
}

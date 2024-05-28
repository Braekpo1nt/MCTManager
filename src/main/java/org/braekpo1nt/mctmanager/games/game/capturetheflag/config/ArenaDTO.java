package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Arena;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

record ArenaDTO(
        Vector northSpawn, 
        Vector southSpawn, 
        Vector northFlag, 
        Vector southFlag, 
        Vector northBarrier,
        Vector southBarrier, 
        Arena.BarrierSize barrierSize, 
        BoundingBoxDTO boundingBox) implements Validatable {
    
    static Arena toArena(ArenaDTO arenaDTO, World arenaWorld) {
        return new Arena(
                arenaDTO.northSpawn().toLocation(arenaWorld),
                arenaDTO.southSpawn().toLocation(arenaWorld),
                arenaDTO.northFlag().toLocation(arenaWorld),
                arenaDTO.southFlag().toLocation(arenaWorld),
                arenaDTO.northBarrier().toLocation(arenaWorld),
                arenaDTO.southBarrier().toLocation(arenaWorld),
                arenaDTO.barrierSize(),
                arenaDTO.boundingBox().toBoundingBox()
        );
    }
    
    static List<Arena> toArenas(List<ArenaDTO> arenaDTOS, World arenaWorld) {
        return arenaDTOS.stream().map(arenaDTO -> ArenaDTO.toArena(arenaDTO, arenaWorld)).toList();
    }
    
    @Override
    public void validate(Validator validator) {
        validator.notNull(this.northSpawn, "northSpawn");
        validator.notNull(this.southSpawn, "southSpawn");
        validator.notNull(this.northFlag, "northFlag");
        validator.notNull(this.southFlag, "southFlag");
        validator.validate(!this.northFlag.equals(this.southFlag), "northFlag and southFlag can't be identical (%s)", this.northFlag);
        validator.notNull(this.northBarrier, "northBarrier");
        validator.notNull(this.southBarrier, "southBarrier");
        validator.notNull(this.barrierSize, "barrierSize");
        validator.validate(this.barrierSize.xSize() >= 1
                && this.barrierSize.ySize() >= 1
                && this.barrierSize.zSize() >= 1, "barrierSize can't have a dimension less than 1 (%s)", this.barrierSize);
        validator.notNull(this.boundingBox, "boundingBox");
        BoundingBox arenaBoundingBox = this.boundingBox.toBoundingBox();
        validator.validate(arenaBoundingBox.getVolume() >= 2.0, "boundingBox (%s) volume (%s) must be at least 2.0", arenaBoundingBox, arenaBoundingBox.getVolume());
        validator.validate(arenaBoundingBox.contains(this.northFlag), "boundingBox (%s) must contain northFlag (%s)", arenaBoundingBox, this.northFlag);
        validator.validate(arenaBoundingBox.contains(this.southFlag), "boundingBox (%s) must contain southFlag (%s)", arenaBoundingBox, this.southFlag);
    }
}

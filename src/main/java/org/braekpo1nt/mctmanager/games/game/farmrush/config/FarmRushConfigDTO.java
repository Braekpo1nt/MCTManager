package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class FarmRushConfigDTO implements Validatable {
    
    private String version;
    private String world;
    private LocationDTO adminLocation;
    private @Nullable BoundingBox spectatorArea;
    /**
     * the file location of the arena, relative to the MCTManager plugin's data folder 
     * ({@code <server>/plugins/MCTManager/}). Example: {@code "farmRushArena.schem"}.
     */
    private String arenaFile;
    /**
     * The location in the world where the first arena will be placed. Will be the
     * minimum position used in WorldEdit's placement of the schematic file.
     * All positions in the {@link #arena} will have this vector added to them.<br>
     * Defaults to {@code (0, 0, 0)}
     */
    private @Nullable Vector firstArenaOrigin;
    /**
     * The details of the arena. When generating arenas for the game, the first one placed down will have its origin at {@link #firstArenaOrigin}, and successive ones will be placed on a grid according to the defined size. Please ensure that the {@link ArenaDTO#getSize()} attribute is the correct dimensions of the {@link #arenaFile} schematic file's dimensions. 
     */
    private ArenaDTO arena;
    private @Nullable List<Material> preventInteractions;
    private Component description;
    
    @Override
    public void validate(@NotNull Validator validator) {
        // TODO: implement this
    }
    
    public FarmRushConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        return FarmRushConfig.builder()
                .world(newWorld)
                .adminLocation(this.adminLocation.toLocation(newWorld))
                .descriptionDuration(10)
                .description(this.description)
                .arenaFile(this.arenaFile)
                .firstArena(this.arena.toArena(newWorld).offset(firstArenaOrigin != null ? firstArenaOrigin : new Vector(0, 0, 0)))
                .build();
    }
}

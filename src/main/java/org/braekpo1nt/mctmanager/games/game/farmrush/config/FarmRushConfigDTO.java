package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ChestInventoryDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.PlayerInventoryDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@Data
public class FarmRushConfigDTO implements Validatable {
    
    private String version;
    private String world;
    private LocationDTO adminLocation;
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
     * The items in the starter chest. Can contain no more than 27 entries. 
     */
    private @Nullable ChestInventoryDTO starterChestContents;
    /**
     * The details of the arena. When generating arenas for the game, the first one placed down will have its origin at {@link #firstArenaOrigin}, and successive ones will be placed on a grid according to the defined size. Please ensure that the {@link ArenaDTO#getSize()} attribute is the correct dimensions of the {@link #arenaFile} schematic file's dimensions. 
     */
    private ArenaDTO arena;
    private @Nullable List<Material> preventInteractions;
    /**
     * each participant's starting inventory. 
     */
    private @Nullable PlayerInventoryDTO loadout;
    private Component description;
    private Durations durations;
    
    @Data
    static class Durations {
        private int description = 10;
        private int starting = 10;
        /**
         * how long the players have to farm rush, in seconds
         */
        private int gameDuration = 180;
        private int gameOver = 10;
    }
    
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
                .description(this.description)
                .arenaFile(this.arenaFile)
                .firstArena(this.arena.toArena(newWorld).offset(firstArenaOrigin != null ? firstArenaOrigin : new Vector(0, 0, 0)))
                .loadout(this.loadout != null ? this.loadout.toInventoryContents() : null)
                .starterChestContents(this.starterChestContents != null ? this.starterChestContents.toInventoryContents() : null)
                .preventInteractions(this.preventInteractions != null ? this.preventInteractions : Collections.emptyList())
                .descriptionDuration(this.durations.description)
                .startingDuration(this.durations.starting)
                .gameDuration(this.durations.gameDuration)
                .gameOverDuration(this.durations.gameOver)
                .build();
    }
}

package org.braekpo1nt.mctmanager.games.game.survivalgames.config;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.PlayerInventoryDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.survivalgames.Border;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@Data
class BorderDTO implements Validatable {
    /** the center of the world border */
    private Center center;
    /** The size that the border should start at */
    private double initialBorderSize;
    /** the amount of damage a player takes when outside the border plus the border buffer. */
    private double damageAmount;
    /** the amount of blocks a player may safely be outside the border before taking damage. */
    private double damageBuffer;
    /** the warning distance that causes the screen to be tinted red when the player is within the specified number of blocks from the border. */
    private int warningDistance;
    /** the warning time that causes the screen to be tinted red when a contracting border will reach the player within the specified time. */
    private int warningTime;
    /**
     * The locations where players can respawn
     * If none exist, then the first platform spawn will be used
     */
    private @Nullable List<LocationDTO> respawnLocations;
    /**
     * The number of stages that respawning is allowed. 
     * After this many stages, respawning will be disabled.
     * Can't be more than the total number of {@link #borderStages}. 
     * If this is less than 1, no respawning will occur through the duration of the game.
     * Defaults to 0.
     */
    private int respawnStages;
    /**
     * The amount of time it takes for a dead participant to respawn (in seconds).
     * Can't be negative.
     * Defaults to 10s
     */
    private @Nullable Integer respawnTime;
    /**
     * The time (in seconds) that a participant who just respawned is invincible for
     * (includes time spent in the air, so choose accordingly). Set it to 0
     * to have no respawn time. Can't be negative.
     * Defaults to 10s
     */
    private @Nullable Integer respawnGracePeriodTime;
    /**
     * The loadout to be given to a participant upon respawning.
     * Defaults to empty.
     */
    private @Nullable PlayerInventoryDTO respawnLoadout;
    /** The stages the border should progress through */
    private List<BorderStageDTO> borderStages;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.validate(this.center != null, "center can't be null");
        validator.validate(this.initialBorderSize >= 1.0,
                "initialBorderSize can't be less than 1.0: %s", this.initialBorderSize);
        validator.notEmpty(this.borderStages, "borderStages");
        validator.validateList(this.borderStages, "borderStages");
        validator.validate(this.respawnStages <= borderStages.size(), "respawnStages must be less than or equal to the total number of borderStages (%d)", borderStages.size());
        if (this.respawnTime != null) {
            validator.validate(this.respawnTime >= 0, "respawnTime (%s) can't be negative", this.respawnTime);
        }
        if (this.respawnGracePeriodTime != null) {
            validator.validate(this.respawnGracePeriodTime >= 0, "respawnGracePeriodTime (%s) can't be negative", this.respawnGracePeriodTime);
        }
        if (this.respawnLoadout != null) {
            this.respawnLoadout.validate(validator.path("respawnLoadout"));
        }
    }
    
    record Center(double x, double z) {
    }
    
    public Border toBorder(@NotNull World world) {
        return Border.builder()
                .centerX(center.x())
                .centerZ(center.z())
                .initialBorderSize(initialBorderSize)
                .damageAmount(damageAmount)
                .damageBuffer(damageBuffer)
                .warningDistance(warningDistance)
                .warningTime(warningTime)
                .respawnStages(respawnStages)
                .respawnTime(this.respawnTime != null ? this.respawnTime : 10)
                .respawnGracePeriodTime(this.respawnGracePeriodTime != null ? this.respawnGracePeriodTime : 10)
                .respawnLocations(respawnLocations != null ? LocationDTO.toLocations(respawnLocations, world) : Collections.emptyList())
                .respawnLoadout(this.respawnLoadout != null ? this.respawnLoadout.toInventoryContents() : new ItemStack[0])
                .stages(BorderStageDTO.toBorderStages(borderStages))
                .build();
    }
}

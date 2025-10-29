package org.braekpo1nt.mctmanager.games.game.survivalgames;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@Builder
public class Border {
    /** the center X coord of the world border */
    private double centerX;
    /** the center Z coord of the world border */
    private double centerZ;
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
    private List<Location> respawnLocations;
    /**
     * The number of stages that respawning is allowed. 
     * After this many stages, respawning will be disabled.
     * Can't be more than the total number of {@link #stages}. 
     * If this is less than 1, no respawning will occur through the duration of the game.
     * Defaults to 0.
     */
    private int respawnStages;
    /**
     * The amount of time it takes for a dead participant to respawn (in seconds)
     * Defaults to 10s
     */
    private int respawnTime;
    /**
     * The time (in seconds) that a participant who just respawned is invincible for
     * (includes time spent in the air, so choose accordingly)
     * Defaults to 10s
     */
    private int respawnGracePeriodTime;
    /**
     * The loadout to be given to a participant upon respawning.
     * Can't be null, can be empty
     */
    private @NotNull ItemStack[] respawnLoadout;
    /**
     * The number of deaths that grant kill points. E.g. if 2, then the first two times
     * a participant is killed, the killer gets points. 
     * But from the third death on, no killers get points for killing that participant.
     * Negative number indicates no limit (all kills grant points). 
     * Defaults to -1
     */
    private int deathPointsThreshold;
    /**
     * If true, a participant in their {@link #respawnGracePeriodTime} can attack other participants.
     * If false, a participant can't deal damage when in grace period.
     * Defaults to true.
     */
    private boolean canAttackWhenRespawning;
    private List<BorderStage> stages;
    
    /**
     * If true, a participant in their {@link #respawnGracePeriodTime} can attack other participants.
     * If false, a participant can't deal damage when in grace period.
     * Defaults to true.
     */
    public boolean canAttackWhenRespawning() {
        return canAttackWhenRespawning;
    }
    
    /**
     * Assign the values of the given world boarder to this class's values
     * @param worldBorder the world border to initialize
     */
    public void initializeWorldBorder(@NotNull WorldBorder worldBorder) {
        worldBorder.setCenter(centerX, centerZ);
        worldBorder.setSize(initialBorderSize);
        worldBorder.setDamageAmount(damageAmount);
        worldBorder.setDamageBuffer(damageBuffer);
        worldBorder.setWarningDistance(warningDistance);
        worldBorder.setWarningTime(warningTime);
    }
    
    public int getTimeBetweenStages(int from, int to) {
        int totalTime = 0;
        for (int i = from; i < Math.min(to, stages.size()); i++) {
            totalTime += stages.get(i).getTotalTime();
        }
        return totalTime;
    }
    
    public Component getRespawnLine(int index) {
        if (index + 1 <= respawnStages) {
            return Component.empty()
                    .append(Component.text("Respawning Enabled")
                            .color(NamedTextColor.GREEN));
        } else {
            return getRespawnDisabledLine();
        }
    }
    
    /**
     * @param index the current stage index to check if respawning is enabled for
     * @return true if respawning is enabled for the given index, false otherwise
     */
    public boolean allowRespawn(int index) {
        return index + 1 <= respawnStages;
    }
    
    public Component getRespawnDisabledLine() {
        return Component.empty()
                .append(Component.text("Respawning Disabled")
                        .color(NamedTextColor.RED));
    }
    
    /**
     * @return true if the previous stage's respawn state is different from that of the given stage,
     * true if the given border stage is the very first, false otherwise.
     */
    public boolean didRespawnStateChange(int index) {
        if (index == 0) {
            return true;
        }
        return allowRespawn(index - 1) != allowRespawn(index);
    }
    
    /**
     * @return true if the next stage's respawn state is different from that of the given stage.
     * If the given border stage is the very last, true if the given stage allows respawning. 
     * False otherwise.
     */
    public boolean willRespawnStateChange(int index) {
        if (index + 1 >= stages.size()) {
            return allowRespawn(stages.size() - 1);
        }
        return allowRespawn(index) != allowRespawn(index + 1);
    }
    
    public boolean neverRespawn() {
        return respawnStages <= 0;
    }
}

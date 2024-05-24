package org.braekpo1nt.mctmanager.games.game.mecha.config;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.config.dto.YawPitch;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * @param version
 * @param world the world
 * @param spectatorArea
 * @param removeArea The area to empty containers and remove floor items
 * @param border the information about the world border
 * @param spawnLootTable The loot table for the spawn chests
 * @param weightedMechaLootTables The loot tables for the chests, with weights for the weighted random selection
 * @param spawnChestCoords The coordinates of the spawn chests
 * @param mapChestCoords the coordinates of the map chests
 * @param platformCenter the place where players will be looking when they spawn in at the start of the game. If this is null, then the Platforms.facingDirection() will be used. If Platforms.facingDirection is null, then they will face yaw=0,pitch=0.
 * @param platforms
 * @param scores
 * @param durations
 * @param description
 */
record MechaConfig (String version, String world, BoundingBoxDTO spectatorArea, BoundingBoxDTO removeArea, BorderDTO border, NamespacedKeyDTO spawnLootTable, List<WeightedNamespacedKey> weightedMechaLootTables, List<Vector> spawnChestCoords, List<Vector> mapChestCoords, Vector platformCenter, List<Platform> platforms, Scores scores, Durations durations, Component description) {
    
    /**
     * 
     * @param center the center of the world border
     * @param initialBorderSize The size that the border should start at
     * @param damageAmount the amount of damage a player takes when outside the border plus the border buffer.
     * @param damageBuffer the amount of blocks a player may safely be outside the border before taking damage.
     * @param warningDistance the warning distance that causes the screen to be tinted red when the player is within the specified number of blocks from the border.
     * @param warningTime the warning time that causes the screen to be tinted red when a contracting border will reach the player within the specified time.
     * @param borderStages The stages the border should progress through
     */
    record BorderDTO(Center center, double initialBorderSize, double damageAmount, double damageBuffer, int warningDistance, int warningTime, List<BorderStage> borderStages) {
        record Center(double x, double z) {
        }
        
        /**
         * 
         * @param size The size (in blocks) the border will be at this stage. The border will shrink from the previous stage's size to this stage's size over this stage's duration
         * @param delay the border will stay at the previous stage's size for this many seconds
         * @param duration the border will take this many seconds to transition from the previous stage's size to this stage's size
         */
        record BorderStage (int size, int delay, int duration){
        }
    }
    
    static class WeightedNamespacedKey extends NamespacedKeyDTO {
        private int weight;
        
        int weight() {
            return weight; 
        }
        
        @Override
        public void isValid() {
            super.isValid();
            Preconditions.checkArgument(weight >= 1,
                    "weightedNamespacedKey can't have a weight (%s) less than 1", weight);
        }
    }
    
    /**
     * @param barrier the BoundingBox of the spawn platform. A hollow box of Barrier blocks will be formed, with the bottom layer of blocks made of Concrete which matches the color of the appropriate team. Players will be spawned in the center of the box, standing on the Concrete blocks.
     * @param facingDirection if this is not null, the players will be looking this direction when they spawn in at the start of the game (this overrides platformCenter). If this is null, then the players will be looking in the direction of platformCenter. If platformCenter is also null, the players will be looking at yaw=0,pitch=0.
     */
    record Platform(BoundingBoxDTO barrier, YawPitch facingDirection) {
    }
    
    record Scores(int kill, int surviveTeam, int firstPlace, int secondPlace, int thirdPlace) {
    }
    
    /**
     * 
     * @param start the delay before the game starts, the time spent on the platforms before they disappear
     * @param invulnerability the duration of the invulnerability once the platforms disappear
     * @param end the delay after the game ends, allows for some celebration time before armor and items are taken away and the teleport back to the hub starts
     */
    record Durations(int start, int invulnerability, int end) {
    }
}

package org.braekpo1nt.mctmanager.games.game.mecha.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.YawPitch;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.bukkit.*;
import org.bukkit.loot.LootTable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Data
class MechaConfigDTO implements Validatable {
    
    private String version;
    private String world;
    private @Nullable BoundingBoxDTO spectatorArea;
    /**
     * The area to empty containers and remove floor items
     */
    private BoundingBoxDTO removeArea;
    /**
     * the information about the world border
     */
    private BorderDTO border;
    /**
     * The loot table for the spawn chests
     */
    private NamespacedKeyDTO spawnLootTable;
    /**
     * The loot tables for the chests, with weights for the weighted random selection
     */
    private List<WeightedNamespacedKey> weightedMechaLootTables;
    /**
     * The coordinates of the spawn chests
     */
    private List<Vector> spawnChestCoords;
    /**
     * the coordinates of the map chests
     */
    private List<Vector> mapChestCoords;
    /**
     * the place where players will be looking when they spawn in at the start of the game. If this is null, then the Platforms.facingDirection() will be used. If Platforms.facingDirection is null, then they will face yaw=0,pitch=0.
     */
    private Vector platformCenter;
    private List<Platform> platforms;
    /** 
     * If true, players will be unable to open any block inventories that aren't spawn chests or map chests. 
     * Defaults to true. 
     */
    private boolean lockOtherInventories = true;
    /**
     * If true, containers will be cleared in the {@link MechaConfigDTO#removeArea} area. 
     * All chunks found in that area will be searched for blocks with inventories, and those will be cleared.
     * If false, this phase will be skipped. Useful in combination with {@link MechaConfigDTO#lockOtherInventories} 
     * set to true, because players shouldn't be able to put anything in those inventories anyway. 
     * Defaults to true.
     */
    private boolean shouldClearContainers = true;
    private @Nullable List<Material> preventInteractions;
    private Scores scores;
    private Durations durations;
    private Component description;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.version,
                "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version), "invalid config version (%s)", this.version);
        validator.notNull(Bukkit.getWorld(this.world),
                "Could not find world \"%s\"", this.world);
        if (spectatorArea != null) {
            BoundingBox spectatorArea = this.spectatorArea.toBoundingBox();
            validator.validate(spectatorArea.getVolume() >= 1.0, "spectatorArea (%s) volume (%s) must be at least 1.0", spectatorArea, spectatorArea.getVolume());
        }
        validator.notNull(this.removeArea,
                "removeArea");
        validator.validate(this.removeArea.toBoundingBox().getVolume() >= 1.0,
                "removeArea (%s) volume (%s) can't be less than 1.0", this.removeArea.toBoundingBox(), this.removeArea.toBoundingBox().getVolume());
        validator.notNull(this.border, "border");
        this.border.validate(validator.path("border"));
        validator.notNull(this.spawnLootTable, "spawnLootTable");
        validator.validate(lootTableExists(this.spawnLootTable.toNamespacedKey()),
                "spawnLootTable: Could not find spawn loot table \"%s\"", this.spawnLootTable);
        validator.notNull(this.weightedMechaLootTables,
                "weightedMechaLootTables");
        validator.validate(!this.weightedMechaLootTables.isEmpty(),
                "weightedMechaLootTables must have at least 1 entry");
        for (int i = 0; i < this.weightedMechaLootTables.size(); i++) {
            MechaConfigDTO.WeightedNamespacedKey weightedNamespacedKey = this.weightedMechaLootTables.get(i);
            weightedNamespacedKey.validate(validator.path("weightedMechaLootTables[%d]", i));
            NamespacedKey namespacedKey = weightedNamespacedKey.toNamespacedKey();
            validator.validate(lootTableExists(namespacedKey),
                    "weightedMechaLootTables[%d]: Could not find loot table \"%s\"", i, namespacedKey);
        }
        validator.notNull(this.spawnChestCoords,
                "spawnChestCoords");
        validator.validate(!this.spawnChestCoords.contains(null),
                "spawnChestCoords can't contain a null position");
        for (Vector pos : this.spawnChestCoords) {
            validator.validate(this.removeArea.toBoundingBox().contains(pos),
                    "spawnChestCoord (%s) is not inside removeArea (%s)", pos, this.removeArea.toBoundingBox());
        }
        validator.notNull(this.mapChestCoords,
                "mapChestCoords");
        validator.validate(!this.mapChestCoords.contains(null),
                "mapChestCoords can't contain a null position");
        for (Vector pos : this.mapChestCoords) {
            validator.validate(this.removeArea.toBoundingBox().contains(pos),
                    "mapChestCoord (%s) is not inside removeArea (%s)", pos, this.removeArea.toBoundingBox());
        }
        validator.notNull(this.platforms, "platforms");
        validator.validate(!this.platforms.isEmpty(), "platforms must have at least one element");
        for (MechaConfigDTO.Platform platform : this.platforms) {
            validator.notNull(platform.barrier(), "platforms.barrier");
            BoundingBox barrier = platform.barrier().toBoundingBox();
            validator.validate(barrier.getHeight() >= 3, "platforms.barrier must have a height of at least 3");
            validator.validate(barrier.getWidthX() >= 2, "platforms.barrier must have an x width of at least 2");
            validator.validate(barrier.getWidthZ() >= 2, "platforms.barrier must have an z width of at least 2");
        }
        for (int i = 0; i < this.platforms.size()-1; i++) {
            BoundingBox boxA = this.platforms.get(i).barrier().toBoundingBox();
            for (int j = i+1; j < this.platforms.size(); j++) {
                BoundingBox boxB = this.platforms.get(j).barrier().toBoundingBox();
                validator.validate(!boxA.contains(boxB), "barrier \"%s\" overlaps barrier \"%s\"", boxA, boxB);
            }
        }
        validator.notNull(this.scores,
                "scores");
        validator.notNull(this.durations,
                "durations");
        validator.validate(this.durations.start() >= 0,
                "durations.start (%s) can't be negative", this.durations.start());
        validator.validate(this.durations.invulnerability() >= 0,
                "durations.invulnerability (%s) can't be negative", this.durations.invulnerability());
        validator.validate(this.durations.end() >= 0,
                "durations.end (%s) can't be negative", this.durations.end());
        validator.notNull(this.description, "description");
    }
    
    MechaConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        HashMap<LootTable, Integer> newWeightedMechaLootTables  = new HashMap<>(this.weightedMechaLootTables.size());
        for (MechaConfigDTO.WeightedNamespacedKey weightedNamespacedKey : this.weightedMechaLootTables) {
            LootTable lootTable = Bukkit.getLootTable(weightedNamespacedKey.toNamespacedKey());
            int weight = weightedNamespacedKey.weight();
            newWeightedMechaLootTables.put(lootTable, weight);
        }
    
        List<BoundingBox> newPlatformBarriers = new ArrayList<>();
        List<Location> newPlatformSpawns = new ArrayList<>();
        for (int i = 0; i < this.platforms.size(); i++) {
            MechaConfigDTO.Platform platform = this.platforms.get(i);
            BoundingBox barrierArea = platform.barrier().toBoundingBox();
            newPlatformBarriers.add(barrierArea);
            double spawnX = barrierArea.getCenterX() + 0.5;
            double spawnY = barrierArea.getMin().getBlockY() + 1;
            double spawnZ = barrierArea.getCenterZ() + 0.5;
            float spawnYaw = 0;
            float spawnPitch = 0;
            if (platform.facingDirection() != null) {
                spawnYaw = platform.facingDirection().yaw();
                spawnPitch = platform.facingDirection().pitch();
            } else if (this.platformCenter != null) {
                YawPitch direction = EntityUtils.getPlayerLookAtYawPitch(new Vector(spawnX, spawnY, spawnZ), this.platformCenter);
                spawnYaw = direction.yaw();
                spawnPitch = direction.pitch();
            }
            newPlatformSpawns.add(new Location(newWorld, spawnX, spawnY, spawnZ, spawnYaw, spawnPitch));
        }
        Location newAdminSpawn = newPlatformBarriers.get(0).getCenter().toLocation(newWorld);
        if (this.platformCenter != null) {
            YawPitch direction = EntityUtils.getPlayerLookAtYawPitch(newAdminSpawn.toVector(), this.platformCenter);
            newAdminSpawn.setYaw(direction.yaw());
            newAdminSpawn.setPitch(direction.pitch());
        }
        
        List<BorderDTO.BorderStage> borderStages = this.border.borderStages();
        int[] sizes = new int[borderStages.size()];
        int[] delays = new int[borderStages.size()];
        int[] durations = new int[borderStages.size()];
        for (int i = 0; i < borderStages.size(); i++) {
            sizes[i] = borderStages.get(i).size();
            delays[i] = borderStages.get(i).delay();
            durations[i] = borderStages.get(i).duration();
        }
        return MechaConfig.builder()
                .world(newWorld)
                .spectatorArea(this.spectatorArea != null ? this.spectatorArea.toBoundingBox() : null)
                .spawnChestCoords(this.spawnChestCoords)
                .mapChestCoords(this.mapChestCoords)
                .spawnLootTable(Bukkit.getLootTable(this.spawnLootTable.toNamespacedKey()))
                .weightedMechaLootTables(newWeightedMechaLootTables)
                .removeArea(this.removeArea.toBoundingBox())
                .platformBarriers(newPlatformBarriers)
                .platformSpawns(newPlatformSpawns)
                .adminSpawn(newAdminSpawn)
                .startDuration(this.durations.start)
                .endDuration(this.durations.end)
                .gracePeriodDuration(this.durations.invulnerability)
                .killScore(this.scores.kill)
                .surviveTeamScore(this.scores.surviveTeam)
                .firstPlaceScore(this.scores.firstPlace)
                .secondPlaceScore(this.scores.secondPlace)
                .thirdPlaceScore(this.scores.thirdPlace)
                .lockOtherInventories(this.lockOtherInventories)
                .shouldClearContainers(this.shouldClearContainers)
                .initialBorderSize(this.border.initialBorderSize())
                .worldBorderCenterX(this.border.center().x())
                .worldBorderCenterZ(this.border.center().z())
                .worldBorderDamageAmount(this.border.damageAmount())
                .worldBorderDamageBuffer(this.border.damageBuffer())
                .worldBorderWarningDistance(this.border.warningDistance())
                .worldBorderWarningTime(this.border.warningTime())
                .sizes(sizes)
                .delays(delays)
                .durations(durations)
                .preventInteractions(this.preventInteractions != null ? this.preventInteractions : Collections.emptyList())
                .descriptionDuration(this.durations.description)
                .description(this.description)
                .build();
    }
    
    private boolean lootTableExists(@Nullable NamespacedKey lootTable) {
        return lootTable == null || Bukkit.getLootTable(lootTable) != null;
    }
    
    @Data
    @EqualsAndHashCode(callSuper = true)
    static class WeightedNamespacedKey extends NamespacedKeyDTO {
        private int weight;
        
        int weight() {
            return weight; 
        }
        
        @Override
        public void validate(@NotNull Validator validator) {
            super.validate(validator);
            validator.validate(weight >= 1,
                    "weight can't be less than 1 (got %s)", weight);
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
    record Durations(int start, int invulnerability, int end, int description) {
    }
}

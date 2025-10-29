package org.braekpo1nt.mctmanager.games.game.survivalgames.config;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.config.dto.YawPitch;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.PlayerInventoryDTO;
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
class SurvivalGamesConfigDTO implements Validatable {
    
    private String version;
    private String world;
    private @Nullable BoundingBox spectatorArea;
    /**
     * The area to empty containers and remove floor items
     */
    private BoundingBox removeArea;
    /**
     * the information about the world border
     */
    private BorderDTO border;
    /**
     * The items a player starts out with at the beginning of the round (this can be empty or left out)
     */
    private @Nullable PlayerInventoryDTO starterLoadout;
    /**
     * The loot table for the spawn chests
     */
    private NamespacedKeyDTO spawnLootTable;
    /**
     * The loot tables for the chests, with weights for the weighted random selection
     */
    @SerializedName(value = "weightedLootTables", alternate = {"weightedMechaLootTables"})
    private List<WeightedNamespacedKey> weightedLootTables;
    /**
     * The coordinates of the spawn chests
     */
    private List<Vector> spawnChestCoords;
    /**
     * the coordinates of the map chests
     */
    private List<Vector> mapChestCoords;
    /**
     * the place where players will be looking when they spawn in at the start of the game. If this is null, then the
     * Platforms.facingDirection() will be used. If Platforms.facingDirection is null, then they will face
     * yaw=0,pitch=0.
     */
    private Vector platformCenter;
    private List<Platform> platforms;
    /**
     * The number of rounds.
     */
    private int rounds;
    /**
     * If true, players will be unable to open any block inventories that aren't spawn chests or map chests.
     * Defaults to true.
     */
    private @Nullable Boolean lockOtherInventories;
    /**
     * If true, containers will be cleared in the {@link SurvivalGamesConfigDTO#removeArea} area.
     * All chunks found in that area will be searched for blocks with inventories, and those will be cleared.
     * If false, this phase will be skipped. Useful in combination with
     * {@link SurvivalGamesConfigDTO#lockOtherInventories}
     * set to true, because players shouldn't be able to put anything in those inventories anyway.
     * Defaults to true.
     */
    private @Nullable Boolean shouldClearContainers;
    /**
     * Whether the Topbar should show the death count in the top right corner or not.
     * Defaults to false.
     */
    private @Nullable Boolean showDeathCount;
    private @Nullable List<Material> preventInteractions;
    private Scores scores;
    private Durations durations;
    private Component description;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.version,
                "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version), "invalid config version (%s)", this.version);
        validator.notNull(this.world, "world");
        validator.notNull(Bukkit.getWorld(this.world),
                "Could not find world \"%s\"", this.world);
        if (spectatorArea != null) {
            validator.validate(spectatorArea.getVolume() >= 1.0, "spectatorArea (%s) volume (%s) must be at least 1.0", spectatorArea, spectatorArea.getVolume());
        }
        if (starterLoadout != null) {
            starterLoadout.validate(validator.path("starterLoadout"));
        }
        validator.notNull(this.removeArea,
                "removeArea");
        validator.validate(this.removeArea.getVolume() >= 1.0,
                "removeArea (%s) volume (%s) can't be less than 1.0", this.removeArea, this.removeArea.getVolume());
        validator.notNull(this.border, "border");
        this.border.validate(validator.path("border"));
        validator.notNull(this.spawnLootTable, "spawnLootTable");
        validator.validate(lootTableExists(this.spawnLootTable.toNamespacedKey()),
                "spawnLootTable: Could not find spawn loot table \"%s\"", this.spawnLootTable);
        validator.notNull(this.weightedLootTables,
                "weightedLootTables");
        validator.validate(!this.weightedLootTables.isEmpty(),
                "weightedLootTables must have at least 1 entry");
        for (int i = 0; i < this.weightedLootTables.size(); i++) {
            SurvivalGamesConfigDTO.WeightedNamespacedKey weightedNamespacedKey = this.weightedLootTables.get(i);
            weightedNamespacedKey.validate(validator.path("weightedLootTables[%d]", i));
            NamespacedKey namespacedKey = weightedNamespacedKey.toNamespacedKey();
            validator.validate(lootTableExists(namespacedKey),
                    "weightedLootTables[%d]: Could not find loot table \"%s\"", i, namespacedKey);
        }
        validator.notNull(this.spawnChestCoords,
                "spawnChestCoords");
        validator.validate(!this.spawnChestCoords.contains(null),
                "spawnChestCoords can't contain a null position");
        for (Vector pos : this.spawnChestCoords) {
            validator.validate(this.removeArea.contains(pos),
                    "spawnChestCoord (%s) is not inside removeArea (%s)", pos, this.removeArea);
        }
        validator.notNull(this.mapChestCoords,
                "mapChestCoords");
        validator.validate(!this.mapChestCoords.contains(null),
                "mapChestCoords can't contain a null position");
        for (Vector pos : this.mapChestCoords) {
            validator.validate(this.removeArea.contains(pos),
                    "mapChestCoord (%s) is not inside removeArea (%s)", pos, this.removeArea);
        }
        validator.validate(this.rounds >= 1, "rounds must be greater than 0");
        validator.notNull(this.platforms, "platforms");
        validator.validate(!this.platforms.isEmpty(), "platforms must have at least one element");
        for (SurvivalGamesConfigDTO.Platform platform : this.platforms) {
            validator.notNull(platform.barrier(), "platforms.barrier");
            BoundingBox barrier = platform.barrier();
            validator.validate(barrier.getHeight() >= 3, "platforms.barrier must have a height of at least 3");
            validator.validate(barrier.getWidthX() >= 2, "platforms.barrier must have an x width of at least 2");
            validator.validate(barrier.getWidthZ() >= 2, "platforms.barrier must have an z width of at least 2");
        }
        for (int i = 0; i < this.platforms.size() - 1; i++) {
            BoundingBox boxA = this.platforms.get(i).barrier();
            for (int j = i + 1; j < this.platforms.size(); j++) {
                BoundingBox boxB = this.platforms.get(j).barrier();
                validator.validate(!boxA.contains(boxB), "barrier \"%s\" overlaps barrier \"%s\"", boxA, boxB);
            }
        }
        validator.notNull(this.scores,
                "scores");
        validator.notNull(this.durations,
                "durations");
        validator.validate(this.durations.roundStarting >= 0,
                "durations.start (%s) can't be negative", this.durations.roundStarting);
        validator.validate(this.durations.invulnerability >= 0,
                "durations.invulnerability (%s) can't be negative", this.durations.invulnerability);
        validator.validate(this.durations.gameOver >= 0,
                "durations.end (%s) can't be negative", this.durations.gameOver);
        validator.notNull(this.description, "description");
    }
    
    SurvivalGamesConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        HashMap<LootTable, Integer> newWeightedLootTables = new HashMap<>(this.weightedLootTables.size());
        for (SurvivalGamesConfigDTO.WeightedNamespacedKey weightedNamespacedKey : this.weightedLootTables) {
            LootTable lootTable = Bukkit.getLootTable(weightedNamespacedKey.toNamespacedKey());
            int weight = weightedNamespacedKey.weight();
            newWeightedLootTables.put(lootTable, weight);
        }
        
        List<BoundingBox> newPlatformBarriers = new ArrayList<>();
        List<Location> newPlatformSpawns = new ArrayList<>();
        for (Platform platform : this.platforms) {
            BoundingBox barrierArea = platform.barrier();
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
        Location newAdminSpawn = newPlatformBarriers.getFirst().getCenter().toLocation(newWorld);
        if (this.platformCenter != null) {
            YawPitch direction = EntityUtils.getPlayerLookAtYawPitch(newAdminSpawn.toVector(), this.platformCenter);
            newAdminSpawn.setYaw(direction.yaw());
            newAdminSpawn.setPitch(direction.pitch());
        }
        
        return SurvivalGamesConfig.builder()
                .world(newWorld)
                .spectatorBoundary(this.spectatorArea == null ? null :
                        new SpectatorBoundary(this.spectatorArea,
                                this.platforms.getFirst()
                                        .barrier()
                                        .getCenter()
                                        .toLocation(newWorld)))
                .spawnChestCoords(this.spawnChestCoords)
                .mapChestCoords(this.mapChestCoords)
                .spawnLootTable(Bukkit.getLootTable(this.spawnLootTable.toNamespacedKey()))
                .weightedLootTables(newWeightedLootTables)
                .removeArea(this.removeArea)
                .platformBarriers(newPlatformBarriers)
                .platformSpawns(newPlatformSpawns)
                .adminSpawn(newAdminSpawn)
                .rounds(this.rounds)
                .roundStartingDuration(this.durations.roundStarting)
                .gameOverDuration(this.durations.gameOver)
                .roundOverDuration(this.durations.roundOver)
                .gracePeriodDuration(this.durations.invulnerability)
                .killScore(this.scores.kill)
                .starterLoadout(this.starterLoadout != null ? this.starterLoadout.toInventoryContents() : null)
                .surviveTeamScore(this.scores.surviveTeam)
                .firstPlaceScore(this.scores.firstPlace)
                .secondPlaceScore(this.scores.secondPlace)
                .thirdPlaceScore(this.scores.thirdPlace)
                .lockOtherInventories(this.lockOtherInventories != null ? this.lockOtherInventories : true)
                .shouldClearContainers(this.shouldClearContainers != null ? this.shouldClearContainers : true)
                .showDeathCount(this.showDeathCount != null ? this.showDeathCount : false)
                .initialBorderSize(this.border.getInitialBorderSize())
                .border(border.toBorder(newWorld))
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
     * @param barrier the BoundingBox of the spawn platform. A hollow box of Barrier blocks will be formed, with the
     * bottom layer of blocks made of Concrete which matches the color of the appropriate team. Players will be spawned
     * in the center of the box, standing on the Concrete blocks.
     * @param facingDirection if this is not null, the players will be looking this direction when they spawn in at the
     * start of the game (this overrides platformCenter). If this is null, then the players will be looking in the
     * direction of platformCenter. If platformCenter is also null, the players will be looking at yaw=0,pitch=0.
     */
    record Platform(BoundingBox barrier, YawPitch facingDirection) {
    }
    
    record Scores(int kill, int surviveTeam, int firstPlace, int secondPlace, int thirdPlace) {
    }
    
    @Data
    static class Durations {
        /**
         * the delay before the game starts, the time spent on the platforms before they disappear
         */
        @SerializedName(value = "roundStarting", alternate = {"start"})
        private int roundStarting;
        private int roundOver;
        /**
         * the duration of the invulnerability once the platforms disappear
         */
        private int invulnerability;
        /**
         * the delay after the game ends, allows for some celebration time before armor and items are taken away and the
         * teleport back to the hub starts
         */
        @SerializedName(value = "gameOver", alternate = {"end"})
        private int gameOver;
        /**
         * How long the description should show
         */
        private int description;
    }
}

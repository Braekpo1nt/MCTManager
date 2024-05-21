package org.braekpo1nt.mctmanager.games.game.mecha.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.GameConfigStorageUtil;
import org.braekpo1nt.mctmanager.config.YawPitch;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.loot.LootTable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class MechaStorageUtil extends GameConfigStorageUtil<MechaConfig> {
    
    protected MechaConfig mechaConfig = null;
    private BoundingBox removeArea;
    private Map<LootTable, Integer> weightedMechaLootTables;
    private World world;
    private List<BoundingBox> platformBarriers;
    private List<Location> platformSpawns;
    private Location adminSpawn;
    private Component description;
    private NamespacedKey spawnLootTable;
    
    public MechaStorageUtil(File configDirectory) {
        super(configDirectory, "mechaConfig.json", MechaConfig.class);
    }
    
    @Override
    protected MechaConfig getConfig() {
        return mechaConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable MechaConfig config) {
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(config.version() != null, 
                "version can't be null");
        Preconditions.checkArgument(Main.VALID_CONFIG_VERSIONS.contains(config.version()), "invalid config version (%s)", config.version());
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, 
                "Could not find world \"%s\"", config.world());
        Preconditions.checkArgument(config.spectatorArea() != null, 
                "spectatorArea can't be null");
        Preconditions.checkArgument(config.spectatorArea().toBoundingBox().getVolume() >= 1.0, 
                "getSpectatorArea's volume (%s) can't be less than 1. %s", config.spectatorArea().toBoundingBox().getVolume(), config.spectatorArea().toBoundingBox());
        Preconditions.checkArgument(config.removeArea() != null, 
                "removeArea can't be null");
        Preconditions.checkArgument(config.removeArea().toBoundingBox().getVolume() >= 1.0, 
                "removeArea (%s) volume (%s) can't be less than 1.0", config.removeArea().toBoundingBox(), config.removeArea().toBoundingBox().getVolume());
        borderIsValid(config.border());
        Preconditions.checkArgument(config.spawnLootTable() != null, "spawnLootTable can't be null");
        Preconditions.checkArgument(lootTableExists(config.spawnLootTable().toNamespacedKey()), 
                "Could not find spawn loot table \"%s\"", config.spawnLootTable());
        Preconditions.checkArgument(config.weightedMechaLootTables() != null, 
                "weightedMechaLootTables can't be null");
        Preconditions.checkArgument(config.weightedMechaLootTables().size() >= 1, 
                "weightedMechaLootTables must have at least 1 entry");
        for (MechaConfig.WeightedNamespacedKey weightedNamespacedKey : config.weightedMechaLootTables()) {
            Preconditions.checkArgument(weightedNamespacedKey.namespace() != null, 
                    "weightedNamespacedKey.namespace can't be null");
            Preconditions.checkArgument(weightedNamespacedKey.key() != null, 
                    "weightedNamespacedKey.key can't be null");
            NamespacedKey namespacedKey = weightedNamespacedKey.toNamespacedKey();
            Preconditions.checkArgument(lootTableExists(namespacedKey), 
                    "Could not find loot table \"%s\"", namespacedKey);
            weightedNamespacedKey.isValid();
        }
        Preconditions.checkArgument(config.spawnChestCoords() != null, 
                "spawnChestCoords can't be null");
        Preconditions.checkArgument(!config.spawnChestCoords().contains(null), 
                "spawnChestCoords can't contain a null position");
        for (Vector pos : config.spawnChestCoords()) {
            Preconditions.checkArgument(config.removeArea().toBoundingBox().contains(pos), 
                    "spawnChestCoord (%s) is not inside removeArea (%s)", pos, config.removeArea().toBoundingBox());
        }
        Preconditions.checkArgument(config.mapChestCoords() != null, 
                "mapChestCoords can't be null");
        Preconditions.checkArgument(!config.mapChestCoords().contains(null), 
                "mapChestCoords can't contain a null position");
        for (Vector pos : config.mapChestCoords()) {
            Preconditions.checkArgument(config.removeArea().toBoundingBox().contains(pos), 
                    "mapChestCoord (%s) is not inside removeArea (%s)", pos, config.removeArea().toBoundingBox());
        }
        Preconditions.checkArgument(config.platforms() != null, "platforms can't be null");
        Preconditions.checkArgument(config.platforms().size() > 0, "platforms must have at least one element");
        for (MechaConfig.Platform platform : config.platforms()) {
            Preconditions.checkArgument(platform.barrier() != null, "platforms.barrier can't be null");
            BoundingBox barrier = platform.barrier().toBoundingBox();
            Preconditions.checkArgument(barrier.getHeight() >= 3, "platforms.barrier must have a height of at least 3");
            Preconditions.checkArgument(barrier.getWidthX() >= 2, "platforms.barrier must have an x width of at least 2");
            Preconditions.checkArgument(barrier.getWidthZ() >= 2, "platforms.barrier must have an z width of at least 2");
        }
        for (int i = 0; i < config.platforms().size()-1; i++) {
            BoundingBox boxA = config.platforms().get(i).barrier().toBoundingBox();
            for (int j = i+1; j < config.platforms().size(); j++) {
                BoundingBox boxB = config.platforms().get(j).barrier().toBoundingBox();
                Preconditions.checkArgument(!boxA.contains(boxB), "barrier \"%s\" overlaps barrier \"%s\"", boxA, boxB);
            }
        }
        Preconditions.checkArgument(config.scores() != null, 
                "scores can't be null");
        Preconditions.checkArgument(config.durations() != null, 
                "durations can't be null");
        Preconditions.checkArgument(config.durations().start() >= 0, 
                "durations.start (%s) can't be negative", config.durations().start());
        Preconditions.checkArgument(config.durations().invulnerability() >= 0, 
                "durations.invulnerability (%s) can't be negative", config.durations().invulnerability());
        Preconditions.checkArgument(config.durations().end() >= 0, 
                "durations.end (%s) can't be negative", config.durations().end());
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.description());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
        return true;
    }
    
    private static void borderIsValid(MechaConfig.BorderDTO border) {
        Preconditions.checkArgument(border != null, "border can't be null");
        Preconditions.checkArgument(border.center() != null, "border.center can't be null");
        Preconditions.checkArgument(border.initialBorderSize() >= 1.0, 
                "border.initialBorderSize can't be less than 1.0: %s", border.initialBorderSize());
        Preconditions.checkArgument(border.borderStages() != null, 
                "border.borderStages can't be null");
        Preconditions.checkArgument(border.borderStages().size() >= 1, 
                "border.borderStages must have at least one stage");
        for (MechaConfig.BorderDTO.BorderStage borderStage : border.borderStages()) {
            Preconditions.checkArgument(borderStage.size() >= 1.0, 
                    "borderStage size (%s) can't be less than 1.0", borderStage.size());
            Preconditions.checkArgument(borderStage.delay() >= 0, 
                    "borderStage.delay (%S) can't be negative", borderStage.delay());
            Preconditions.checkArgument(borderStage.duration() >= 0, 
                    "borderStage.duration (%S) can't be negative", borderStage.duration());
        }
    }
    
    @Override
    protected void setConfig(MechaConfig config) {
        World newWorld = Bukkit.getWorld(config.world());
        Preconditions.checkArgument(newWorld != null, "Could not find world \"%s\"", config.world());
        NamespacedKey newSpawnLootTable = config.spawnLootTable().toNamespacedKey();
        List<MechaConfig.WeightedNamespacedKey> weightedNamespacedKeys = config.weightedMechaLootTables();
        HashMap<LootTable, Integer> newWeightedMechaLootTables  = new HashMap<>(weightedNamespacedKeys.size());
        for (MechaConfig.WeightedNamespacedKey weightedNamespacedKey : weightedNamespacedKeys) {
            LootTable lootTable = Bukkit.getLootTable(weightedNamespacedKey.toNamespacedKey());
            int weight = weightedNamespacedKey.weight();
            newWeightedMechaLootTables.put(lootTable, weight);
        }
        List<BoundingBox> newPlatformBarriers = new ArrayList<>();
        List<Location> newPlatformSpawns = new ArrayList<>();
        for (int i = 0; i < config.platforms().size(); i++) {
            MechaConfig.Platform platform = config.platforms().get(i);
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
            } else if (config.platformCenter() != null) {
                YawPitch direction = EntityUtils.getPlayerLookAtYawPitch(new Vector(spawnX, spawnY, spawnZ), config.platformCenter());
                spawnYaw = direction.yaw();
                spawnPitch = direction.pitch();
                Bukkit.getLogger().info(String.format("%s: yaw=%s, pitch=%s", i+1, spawnYaw, spawnPitch));
            }
            newPlatformSpawns.add(new Location(newWorld, spawnX, spawnY, spawnZ, spawnYaw, spawnPitch));
        }
        Location newAdminSpawn = newPlatformBarriers.get(0).getCenter().toLocation(newWorld);
        if (config.platformCenter() != null) {
            YawPitch direction = EntityUtils.getPlayerLookAtYawPitch(newAdminSpawn.toVector(), config.platformCenter());
            newAdminSpawn.setYaw(direction.yaw());
            newAdminSpawn.setPitch(direction.pitch());
        }
        Component newDescription = GsonComponentSerializer.gson().deserializeFromTree(config.description());
        // now it's confirmed everything works, so set the actual fields
        this.world = newWorld;
        this.weightedMechaLootTables = newWeightedMechaLootTables;
        this.platformBarriers = newPlatformBarriers;
        this.platformSpawns = newPlatformSpawns;
        this.description = newDescription;
        this.removeArea = config.removeArea().toBoundingBox();
        this.adminSpawn = newAdminSpawn;
        this.spawnLootTable = newSpawnLootTable;
        this.mechaConfig = config;
    }
    
    private boolean lootTableExists(@Nullable NamespacedKey lootTable) {
        return lootTable == null || Bukkit.getLootTable(lootTable) != null;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return MechaStorageUtil.class.getResourceAsStream("exampleMechaConfig.json");
    }
    
    /**
     * The coordinates of all the spawn chests
     */
    public List<Vector> getSpawnChestCoords() {
        return mechaConfig.spawnChestCoords();
    }
    
    /**
     * The coordinates of all the chests in the open world, not including spawn chests
     */
    public List<Vector> getMapChestCoords() {
        return mechaConfig.mapChestCoords();
    }
    
    /**
     * The mecha spawn loot table from the mctdatapack
     */
    public LootTable getSpawnLootTable() {
        return Bukkit.getLootTable(spawnLootTable);
    }
    
    /**
     * The mecha loot tables from the mctdatapack, not including the spawn loot.
     * Each loot table is paired with a weight for random selection. 
     */
    public Map<LootTable, Integer> getWeightedMechaLootTables() {
        return this.weightedMechaLootTables;
    }
    
    public int[] getSizes() {
        List<MechaConfig.BorderDTO.BorderStage> borderStages = mechaConfig.border().borderStages();
        int[] sizes = new int[borderStages.size()];
        for (int i = 0; i < borderStages.size(); i++) {
            sizes[i] = borderStages.get(i).size();
        }
        return sizes;
    }
    
    public int[] getDelays() {
        List<MechaConfig.BorderDTO.BorderStage> borderStages = mechaConfig.border().borderStages();
        int[] delays = new int[borderStages.size()];
        for (int i = 0; i < borderStages.size(); i++) {
            delays[i] = borderStages.get(i).delay();
        }
        return delays;
    }
    
    public int[] getDurations() {
        List<MechaConfig.BorderDTO.BorderStage> borderStages = mechaConfig.border().borderStages();
        int[] durations = new int[borderStages.size()];
        for (int i = 0; i < borderStages.size(); i++) {
            durations[i] = borderStages.get(i).duration();
        }
        return durations;
    }
    
    public World getWorld() {
        return world;
    }
    
    public double getWorldBorderCenterX() {
        return mechaConfig.border().center().x();
    }
    
    public double getWorldBorderCenterZ() {
        return mechaConfig.border().center().z();
    }
    
    public double getInitialBorderSize() {
        return mechaConfig.border().initialBorderSize();
    }

    public BoundingBox getRemoveArea() {
        return removeArea;
    }
    
    public List<BoundingBox> getPlatformBarriers() {
        return platformBarriers;
    }
    
    public List<Location> getPlatformSpawns() {
        return platformSpawns;
    }
    
    public Component getDescription() {
        return description;
    }
    
    public int getStartDuration() {
        return mechaConfig.durations().start();
    }
    
    public int getInvulnerabilityDuration() {
        return mechaConfig.durations().invulnerability();
    }
    
    public int getEndDuration() {
        return mechaConfig.durations().end();
    }
    
    public int getKillScore() {
        return mechaConfig.scores().kill();
    }
    
    public int getSurviveTeamScore() {
        return mechaConfig.scores().surviveTeam();
    }
    
    public Location getAdminSpawn() {
        return adminSpawn;
    }
    
    public double getWorldBorderDamageAmount() {
        return mechaConfig.border().damageAmount();
    }
    
    public double getWorldBorderDamageBuffer() {
        return mechaConfig.border().damageBuffer();
    }
    
    public int getWorldBorderWarningDistance() {
        return mechaConfig.border().warningDistance();
    }
    
    public int getWorldBorderWarningTime() {
        return mechaConfig.border().warningTime();
    }
}

package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.BattleClass;
import org.braekpo1nt.mctmanager.games.game.config.ConfigUtil;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.braekpo1nt.mctmanager.games.game.config.inventory.InventoryContentsDTO;
import org.braekpo1nt.mctmanager.games.game.config.inventory.meta.ItemMetaDTO;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class CaptureTheFlagStorageUtil extends GameConfigStorageUtil<CaptureTheFlagConfig> {
    private CaptureTheFlagConfig captureTheFlagConfig = null;
    private World world;
    private Location spawnObservatory;
    private List<Arena> arenas;
    private Component description;
    private Map<BattleClass, ItemStack[]> loadouts;
    private Map<BattleClass, Material> menuItems;
    private Map<BattleClass, List<Component>> menuLores;
    
    public CaptureTheFlagStorageUtil(File configDirectory) {
        super(configDirectory, "captureTheFlagConfig.json", CaptureTheFlagConfig.class);
    }
    
    @Override
    protected CaptureTheFlagConfig getConfig() {
        return captureTheFlagConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable CaptureTheFlagConfig config) throws IllegalArgumentException {
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(config.version() != null, "version can't be null");
        Preconditions.checkArgument(config.version().equals(Main.CONFIG_VERSION), "Config version %s not supported. %s required.", config.version(), Main.CONFIG_VERSION);
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, "Could not find world \"%s\"", config.world());
        Preconditions.checkArgument(config.arenas() != null, "arenas can't be null");
        Preconditions.checkArgument(config.arenas().size() >= 1, "there must be at least 1 arena");
        for (CaptureTheFlagConfig.ArenaDTO arena : config.arenas()) {
            Preconditions.checkArgument(arena.northSpawn() != null, "northSpawn can't be null");
            Preconditions.checkArgument(arena.southSpawn() != null, "southSpawn can't be null");
            Preconditions.checkArgument(arena.northFlag() != null, "northFlag can't be null");
            Preconditions.checkArgument(arena.southFlag() != null, "southFlag can't be null");
            Preconditions.checkArgument(!arena.northFlag().equals(arena.southFlag()), "northFlag and southFlag can't be identical (%s)", arena.northFlag());
            Preconditions.checkArgument(arena.northBarrier() != null, "northBarrier can't be null");
            Preconditions.checkArgument(arena.southBarrier() != null, "southBarrier can't be null");
            Preconditions.checkArgument(arena.barrierSize() != null, "barrierSize can't be null");
            Preconditions.checkArgument(arena.barrierSize().xSize() >= 1
                    && arena.barrierSize().ySize() >= 1
                    && arena.barrierSize().zSize() >= 1, "barrierSize can't have a dimension less than 1 (%s)", arena.barrierSize());
            Preconditions.checkArgument(arena.boundingBox() != null, "boundingBox can't be null");
            BoundingBox arenaBoundingBox = arena.boundingBox().toBoundingBox();
            Preconditions.checkArgument(arenaBoundingBox.getVolume() >= 2.0, "boundingBox (%s) volume (%s) must be at least 2.0", arenaBoundingBox, arenaBoundingBox.getVolume());
            Preconditions.checkArgument(arenaBoundingBox.contains(arena.northFlag()), "boundingBox (%s) must contain northFlag (%s)", arenaBoundingBox, arena.northFlag());
            Preconditions.checkArgument(arenaBoundingBox.contains(arena.southFlag()), "boundingBox (%s) must contain southFlag (%s)", arenaBoundingBox, arena.southFlag());
        }
        Preconditions.checkArgument(config.spectatorArea() != null, "spectatorArea can't be null");
        BoundingBox spectatorArea = config.spectatorArea().toBoundingBox();
        Preconditions.checkArgument(spectatorArea.getVolume() >= 1.0, "spectatorArea (%s) volume (%s) must be at least 1.0", spectatorArea, spectatorArea.getVolume());
        Preconditions.checkArgument(config.scores() != null, "scores can't be null");
        Preconditions.checkArgument(config.durations() != null, "durations can't be null");
        Preconditions.checkArgument(config.durations().matchesStarting() >= 0, "durations.matchesStarting (%s) can't be negative", config.durations().matchesStarting());
        Preconditions.checkArgument(config.durations().classSelection() >= 0, "durations.classSelection (%s) can't be negative", config.durations().classSelection());
        Preconditions.checkArgument(config.durations().roundTimer() >= 0, "durations.roundTimer (%s) can't be negative", config.durations().roundTimer());
        Preconditions.checkArgument(config.loadouts() != null, "loadouts can't be null");
        Preconditions.checkArgument(config.loadouts().keySet().containsAll(List.of(BattleClass.values())), "loadouts must contain an entry for each BattleClass");
        Set<Material> uniqueMenuItems = new HashSet<>();
        for (BattleClass battleClass : BattleClass.values()) {
            CaptureTheFlagConfig.Loadout loadout = config.loadouts().get(battleClass);
            Preconditions.checkArgument(!uniqueMenuItems.contains(loadout.menuItem()), "loadout.menuItem %s for BattleClass %s is not unique", loadout.menuItem(), battleClass);
            uniqueMenuItems.add(loadout.menuItem());
            loadoutIsValid(loadout);
        }
        try {
            GsonComponentSerializer.gson().deserializeFromTree(config.description());
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("description is invalid", e);
        }
        return true;
    }
    
    private void loadoutIsValid(CaptureTheFlagConfig.Loadout loadout) {
        Preconditions.checkArgument(loadout.menuItem() != null, "loadout.menuItem can't be null");
        Preconditions.checkArgument(loadout.menuLore() != null, "loadout.menuLore can't be null");
        for (JsonElement line : loadout.menuLore()) {
            Preconditions.checkArgument(line != null, "loadout.menuLore can't have any null entries");
        }
        ItemMetaDTO.toLore(loadout.menuLore());
        inventoryIsValid(loadout.inventory());
    }
    
    private void inventoryIsValid(InventoryContentsDTO inventory) {
        Preconditions.checkArgument(inventory != null, "loadout.inventory can't be null");
        Preconditions.checkArgument(inventory.contents() != null, "loadout.inventory.contents can't be null");
        Preconditions.checkArgument(inventory.contents().size() <= 41, "loadout.inventory.contents can't contain more than 41 entries");
        Preconditions.checkArgument(inventory.contents().keySet().stream().max(Integer::compareTo).orElse(0) <= 40, "loadout.inventory.contents indexes can't be greater than 40");
        Preconditions.checkArgument(0 <= inventory.contents().keySet().stream().min(Integer::compareTo).orElse(0), "loadout.inventory.contents indexes can't be less than 0");
    }
    
    @Override
    protected void setConfig(CaptureTheFlagConfig config) {
        World newWorld = Bukkit.getWorld(config.world());
        Preconditions.checkArgument(newWorld != null, "Could not find world \"%s\"", config.world());
        Location newSpawnObservatory = config.spawnObservatory().toLocation(newWorld);
        Component newDescription = GsonComponentSerializer.gson().deserializeFromTree(config.description());
        Map<BattleClass, ItemStack[]> newLoadouts = new HashMap<>();
        Map<BattleClass, Material> newMenuItems = new HashMap<>();
        Map<BattleClass, List<Component>> newMenuLores = new HashMap<>();
        for (Map.Entry<BattleClass, CaptureTheFlagConfig.Loadout> entry : config.loadouts().entrySet()) {
            BattleClass battleClass = entry.getKey();
            CaptureTheFlagConfig.Loadout loadout = entry.getValue();
            newLoadouts.put(battleClass, loadout.inventory().toInventoryContents());
            newMenuItems.put(battleClass, loadout.menuItem());
            List<Component> menuLore = ItemMetaDTO.toLore(loadout.menuLore());
            newMenuLores.put(battleClass, menuLore);
        }
        // now it's confirmed everything works, so set the actual fields
        this.world = newWorld;
        this.spawnObservatory = newSpawnObservatory;
        this.arenas = toArenas(config.arenas(), world);
        this.description = newDescription;
        this.captureTheFlagConfig = config;
        this.loadouts = newLoadouts;
        this.menuItems = newMenuItems;
        this.menuLores = newMenuLores;
    }
    
    private List<Arena> toArenas(List<CaptureTheFlagConfig.ArenaDTO> arenaDTOS, World arenaWorld) {
        List<Arena> newArenas = new ArrayList<>(arenaDTOS.size());
        for (CaptureTheFlagConfig.ArenaDTO arenaDTO : arenaDTOS) {
            newArenas.add(new Arena(
                    arenaDTO.northSpawn().toLocation(arenaWorld),
                    arenaDTO.southSpawn().toLocation(arenaWorld),
                    arenaDTO.northFlag().toLocation(arenaWorld),
                    arenaDTO.southFlag().toLocation(arenaWorld),
                    arenaDTO.northBarrier().toLocation(arenaWorld),
                    arenaDTO.southBarrier().toLocation(arenaWorld),
                    arenaDTO.barrierSize(),
                    arenaDTO.boundingBox().toBoundingBox()
            ));
        }
        return newArenas;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return CaptureTheFlagStorageUtil.class.getResourceAsStream("exampleCaptureTheFlagConfig.json");
    }
    
    public World getWorld() {
        return world;
    }
    
    public Location getSpawnObservatory() {
        return spawnObservatory;
    }
    
    public CaptureTheFlagConfig.Scores getScores() {
        return captureTheFlagConfig.scores();
    }
    
    public CaptureTheFlagConfig.Durations getDurations() {
        return captureTheFlagConfig.durations();
    }
    
    public List<Arena> getArenas() {
        return arenas;
    }
    
    public int getMatchesStartingDuration() {
        return captureTheFlagConfig.durations().matchesStarting();
    }
    
    public int getRoundTimerDuration() {
        return captureTheFlagConfig.durations().roundTimer();
    }
    
    public int getClassSelectionDuration() {
        return captureTheFlagConfig.durations().classSelection();
    }
    
    public int getWinScore() {
        return captureTheFlagConfig.scores().win();
    }
    
    public int getKillScore() {
        return captureTheFlagConfig.scores().kill();
    }
    
    public Component getDescription() {
        return description;
    }
    
    public Map<BattleClass, ItemStack[]> getLoadouts() {
        return loadouts;
    }
    
    public Map<BattleClass, Material> getMenuItems() {
        return menuItems;
    }
    
    public Map<BattleClass, List<Component>> getMenuLores() {
        return menuLores;
    }
}

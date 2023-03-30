package org.braekpo1nt.mctmanager.games.mecha;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import fr.mrmicky.fastboard.FastBoard;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.MCTGame;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.structure.Structure;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class MechaGame implements MCTGame, Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    private boolean gameActive = false;
    private boolean mechaHasStarted = false;
    private List<Player> participants;
    private Map<UUID, Integer> killCounts;
    private final World mechaWorld;
    private final MultiverseWorld mvMechaWorld;
    /**
     * Stores the default game mode for the multiverse world
     */
    private GameMode oldMVGameMode;
    private Map<UUID, FastBoard> boards = new HashMap<>();
    private int startMechaTaskId;
    /**
     * The coordinates of all the chests in the open world, not including spawn chests
     */
    private List<Vector> mapChestCoords;
    /**
     * The coordinates of all the spawn chests
     */
    private List<Vector> spawnChestCoords;
    /**
     * Holds the mecha loot tables from the mctdatapack, not including the spawn loot.
     * Each loot table is paired with a weight for random selection. 
     */
    private List<LootTable> mechaLootTables;
    private List<Integer> mechaLootTableWeights;
    /**
     * Holds the mecha spawn loot table from the mctdatapack
     */
    private LootTable spawnLootTable;
    private final WorldBorder worldBorder;
    private int borderShrinkingTaskId;
    private Map<String, List<UUID>> livingTeams;
    
    public MechaGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setChestCoordsAndLootTables();
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.mvMechaWorld = worldManager.getMVWorld("FT");
        this.mechaWorld = mvMechaWorld.getCBWorld();
        this.worldBorder = mechaWorld.getWorldBorder();
    }
    
    @Override
    public void start(List<Player> participants) {
        this.participants = participants;
        initializeLivingTeams();
        this.killCounts = participants.stream().collect(Collectors.toMap(Entity::getUniqueId, value -> 0));
        placePlatforms();
        fillAllChests();
        teleportPlayersToStartingPositions();
        this.oldMVGameMode = mvMechaWorld.getGameMode();
        mvMechaWorld.setGameMode(GameMode.ADVENTURE);
        setPlayersToAdventure();
        clearInventories();
        resetHealthAndHunger();
        clearStatusEffects();
        initializeFastboards();
        startStartMechaCountdownTask();
        initializeWorldBorder();
        gameActive = true;
        Bukkit.getLogger().info("Started mecha");
    }
    
    @Override
    public void stop() {
        hideFastBoards();
        cancelAllTasks();
        clearInventories();
        placePlatforms();
        clearAllChests();
        worldBorder.reset();
        gameActive = false;
        mechaHasStarted = false;
        gameManager.gameIsOver();
        this.mvMechaWorld.setGameMode(oldMVGameMode);
        Bukkit.getLogger().info("Stopped mecha");
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(startMechaTaskId);
        Bukkit.getScheduler().cancelTask(borderShrinkingTaskId);
    }
    
    private void startStartMechaCountdownTask() {
        startMechaTaskId = new BukkitRunnable() {
            int count = 10;
            
            @Override
            public void run() {
                if (count <= 0) {
                    startMecha();
                    this.cancel();
                    return;
                }
                for (Player participant : participants) {
                    participant.sendMessage(Component.text(count));
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void initializeLivingTeams() {
        livingTeams = new HashMap<>();
        for (Player participant : participants) {
            String teamName = gameManager.getTeamName(participant.getUniqueId());
            if (!livingTeams.containsKey(teamName)) {
                livingTeams.put(teamName, new ArrayList<>());
            }
            List<UUID> teamMates = livingTeams.get(teamName);
            teamMates.add(participant.getUniqueId());
        }
    }
    
    /**
     * Removes the player with the given UUID from the living teams map.
     * If that player was the last to be removed from their team, this
     * also removes the team from the map.
     * @param playerUniqueId The UUID of the player to remove from the team
     * @throws NullPointerException if the team of the player with the given UUID is not in the
     * list of living teams
     */
    private void removePlayerFromLivingTeams(UUID playerUniqueId) {
        String teamName = gameManager.getTeamName(playerUniqueId);
        List<UUID> teamMates = livingTeams.get(teamName);
        teamMates.remove(playerUniqueId);
        if (teamMates.size() == 0) {
            livingTeams.remove(teamName);
        }
    }
    
    private void clearInventories() {
        for (Player participant : participants) {
            participant.getInventory().clear();
        }
    }
    
    private void resetHealthAndHunger() {
        for (Player participant : participants) {
            participant.setHealth(participant.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
            participant.setSaturation(20);
        }
    }
    
    private void clearStatusEffects() {
        for (Player participant : participants) {
            for (PotionEffect effect : participant.getActivePotionEffects()) {
                participant.removePotionEffect(effect.getType());
            }
        }
    }
    
    private void startMecha() {
        this.mechaHasStarted = true;
        kickOffBorderShrinking();
        removePlatforms();
        for (Player participant : participants) {
            participant.sendMessage(Component.text("Go!"));
        }
    }
    
    private void onTeamWin(String winningTeamName) {
        Component displayNameComponent = gameManager.getFormattedTeamDisplayName(winningTeamName);
        Bukkit.getServer().sendMessage(Component.text("Team ")
                .append(displayNameComponent)
                .append(Component.text(" wins!")));
        stop();
    }
    
    private void startSuddenDeath() {
        displaySuddenDeath();
        for (Player participant : participants) {
            participant.sendMessage(Component.text("Sudden death!"));
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!gameActive) {
            return;
        }
        if (!mechaHasStarted) {
            return;
        }
        Player killed = event.getPlayer();
        if (!participants.contains(killed)) {
            return;
        }
        killed.setGameMode(GameMode.SPECTATOR);
        removePlayerFromLivingTeams(killed.getUniqueId());
        event.setCancelled(true);
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            Bukkit.getServer().sendMessage(deathMessage);
        }
        if (killed.getKiller() != null) {
            onPlayerGetKill(killed);
        }
        String winningTeam = getWinningTeam();
        if (winningTeam != null) {
            onTeamWin(winningTeam);
        }
    }
    
    private void onPlayerGetKill(Player killed) {
        Player killer = killed.getKiller();
        if (!participants.contains(killer)) {
            return;
        }
        addKill(killer.getUniqueId());
        gameManager.awardPointsToPlayer(killer, 40);
    }
    
    /**
     * Returns the winning team if there is one
     * @return The team name of the winning team, or null if there is no winning team
     */
    public String getWinningTeam() {
        if (livingTeams.size() == 1) {
            String winningTeam = livingTeams.keySet().iterator().next();
            return winningTeam;
        }
        return null;
    }
    
    private void addKill(UUID killerUniqueId) {
        FastBoard board = boards.get(killerUniqueId);
        int oldKillCount = killCounts.get(killerUniqueId);
        int newKillCount = oldKillCount + 1;
        killCounts.put(killerUniqueId, newKillCount);
//        board.updateLine(1, Component.text("Kills: ")
//                .append(Component.text(newKillCount))
//                .color(NamedTextColor.RED)
//                .toString());
        board.updateLine(1, "Kills: " + newKillCount);
    }
    
    private void initializeWorldBorder() {
        worldBorder.setCenter(0, 0);
        worldBorder.setSize(248);
    }
    
    private void kickOffBorderShrinking() {
        int[] sizes = new int[]{180, 150, 100, 50, 25, 2};
        int[] delays = new int[]{90, 70, 60, 80, 60, 30};
        int[] durations = new int[]{25, 20, 20 , 15, 15, 30};
        this.borderShrinkingTaskId = new BukkitRunnable() {
            int delay = 0;
            int duration = 0;
            boolean onDelay = false;
            boolean onDuration = false;
            int sceneIndex = 0;
            @Override
            public void run() {
                if (onDelay) {
                    displayBorderDelayFor(delay);
                    if (delay <= 1) {
                        onDelay = false;
                        onDuration = true;
                        duration = durations[sceneIndex];
                        int size = sizes[sceneIndex];
                        worldBorder.setSize(size, duration);
                        sendBorderShrinkAnouncement(duration, size);
                        return;
                    }
                    delay--;
                } else if (onDuration) {
                    displayBorderShrinkingFor(duration);
                    if (duration <= 1) {
                        onDuration = false;
                        onDelay = true;
                        sceneIndex++;
                        if (sceneIndex >= delays.length) {
                            startSuddenDeath();
                            Bukkit.getLogger().info("Border is in final position.");
                            this.cancel();
                            return;
                        }
                        delay = delays[sceneIndex];
                        sendBorderDelayAnouncement(delay);
                        return;
                    }
                    duration--;
                } else {
                    //initialize
                    onDelay = true;
                    delay = delays[0];
                }
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void initializeFastboards() {
        for (Player participant : participants) {
            FastBoard board = new FastBoard(participant);
            board.updateTitle(ChatColor.BLUE+"MECHA");
            board.updateLines(
                    "",
                    ChatColor.RED+"Kills: 0",
                    "",
                    ChatColor.LIGHT_PURPLE+"Border",
                    ChatColor.LIGHT_PURPLE+"0:00"
            );
            boards.put(participant.getUniqueId(), board);
        }
    }
    
    private void hideFastBoards() {
        for (FastBoard board : boards.values()) {
            if (!board.isDeleted()) {
                board.delete();
            }
        }
    }
    
    /**
     * Sends a chat message to all participants saying the border is delaying
     * @param delay The delay in seconds
     */
    private void sendBorderDelayAnouncement(int delay) {
        String timeString = getTimeString(delay);
        String message = "Border will not shrink for "+timeString;
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
    /**
     * Sends a chat message to all participants saying the border is shrinking
     * @param duration The duration of the shrink in seconds
     * @param size The size of the border in blocks
     */
    private void sendBorderShrinkAnouncement(int duration, int size) {
        String timeString = getTimeString(duration);
        String message = String.format("Border shrinking to %d for %s", size, timeString);
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
    /**
     * Displays the time left for the border shrink to the participants on the FastBoards
     * @param duration The seconds left in the border shrink
     */
    private void displayBorderShrinkingFor(int duration) {
        String timeString = getTimeString(duration);
        String line3 = ChatColor.RED+"Shrinking";
        String line4 = ChatColor.RED+timeString;
        for (Player participant : participants) {
            FastBoard board = boards.get(participant.getUniqueId());
            board.updateLine(3, line3);
            board.updateLine(4, line4);
        }
    }
    
    /**
     * Displays the time left till the border shrinks again on the FastBoards
     * @param delay The seconds left till the border shrinks
     */
    private void displayBorderDelayFor(int delay) {
        String timeString = getTimeString(delay);
        String line3 = ChatColor.LIGHT_PURPLE+"Border";
        String line4 = ChatColor.LIGHT_PURPLE+timeString;
        for (Player participant : participants) {
            FastBoard board = boards.get(participant.getUniqueId());
            board.updateLine(3, line3);
            board.updateLine(4, line4);
        }
    }
    
    /**
     * Displays the sudden death message on the FastBoards
     */
    private void displaySuddenDeath() {
        String line3 = ChatColor.RED+"Sudden death";
        String line4 = "";
        for (Player participant : participants) {
            FastBoard board = boards.get(participant.getUniqueId());
            board.updateLine(3, line3);
            board.updateLine(4, line4);
        }
    }
    
    /**
     * Returns the given seconds as a string representing time in the format
     * MM:ss (or minutes:seconds)
     * @param timeSeconds The time in seconds
     * @return Time string MM:ss
     */
    private String getTimeString(long timeSeconds) {
        Duration duration = Duration.ofSeconds(timeSeconds);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%d:%02d", minutes, seconds);
    }
    
    private void teleportPlayersToStartingPositions() {
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        Map<String, Location> teamLocations = new HashMap<>();
        teamLocations.put("orange", anchorManager.getAnchorLocation("mecha-orange"));
        teamLocations.put("yellow", anchorManager.getAnchorLocation("mecha-yellow"));
        teamLocations.put("green", anchorManager.getAnchorLocation("mecha-green"));
        teamLocations.put("dark-green", anchorManager.getAnchorLocation("mecha-dark-green"));
        teamLocations.put("cyan", anchorManager.getAnchorLocation("mecha-cyan"));
        teamLocations.put("blue", anchorManager.getAnchorLocation("mecha-blue"));
        teamLocations.put("purple", anchorManager.getAnchorLocation("mecha-purple"));
        teamLocations.put("red", anchorManager.getAnchorLocation("mecha-red"));
        for (Player participant : participants) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            Location teamLocation = teamLocations.getOrDefault(team, teamLocations.get("yellow"));
            participant.teleport(teamLocation);
        }
    }
    
    private void setPlayersToAdventure() {
        for (Player participant : participants) {
            participant.setGameMode(GameMode.ADVENTURE);
        }
    }
    
    private void placePlatforms() {
        Structure structure = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctdatapack", "mecha/platforms"));
        structure.place(new Location(this.mechaWorld, -13, -43, -13), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
    }
    
    private void removePlatforms() {
        Structure structure = Bukkit.getStructureManager().loadStructure(new NamespacedKey("mctdatapack", "mecha/platforms_removed"));
        structure.place(new Location(this.mechaWorld, -13, -43, -13), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
    }
    
    /**
     * Fill all chests in the mecha world, map chests and spawn chests
     */
    private void fillAllChests() {
        fillSpawnChests();
        fillMapChests();
    }
    
    private void clearAllChests() {
        List<Vector> allChestCoords = new ArrayList<>(spawnChestCoords);
        allChestCoords.addAll(mapChestCoords);
        for (Vector coords : allChestCoords) {
            Block block = mechaWorld.getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();
            chest.getBlockInventory().clear();
        }
    }
    
    private void fillSpawnChests() {
        for (Vector coords : spawnChestCoords) {
            Block block = mechaWorld.getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();
            chest.setLootTable(spawnLootTable);
            chest.update();
        }
    }
    
    private void fillMapChests() {
        for (Vector coords : mapChestCoords) {
            Block block = mechaWorld.getBlockAt(coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
            block.setType(Material.CHEST);
            fillMapChest(((Chest) block.getState()));
        }
    }
    
    /**
     * Fills the given chest with a random loot table
     * @param chest The chest to fill
     */
    private void fillMapChest(Chest chest) {
        LootTable lootTable = getRandomLootTable(mechaLootTableWeights, mechaLootTables);
        chest.setLootTable(lootTable);
        chest.update();
    }
    
    /**
     * Gets a random loot table from the MECHA loot table selection
     * @return A loot table for a chest
     */
    private LootTable getRandomLootTable(List<Integer> weights, List<LootTable> loots) {
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }
        int randomIndex = (int) (Math.random() * totalWeight);
        int weightSum = 0;
        for (int i = 0; i < weights.size(); i++) {
            weightSum += weights.get(i);
            if (randomIndex < weightSum) {
                return loots.get(i);
            }
        }
        return null;
    }
    
    private void setChestCoordsAndLootTables() {
        this.spawnChestCoords = new ArrayList<>(12);
        spawnChestCoords.add(new Vector(-1, -45, 1));
        spawnChestCoords.add(new Vector(0, -45, 1));
        spawnChestCoords.add(new Vector(-2, -45, 0));
        spawnChestCoords.add(new Vector(-1, -44, 0));
        spawnChestCoords.add(new Vector(0, -44, 0));
        spawnChestCoords.add(new Vector(1, -45, 0));
        spawnChestCoords.add(new Vector(-2, -45, -1));
        spawnChestCoords.add(new Vector(-1, -44, -1));
        spawnChestCoords.add(new Vector(0, -44, -1));
        spawnChestCoords.add(new Vector(1, -45, -1));
        spawnChestCoords.add(new Vector(-1, -45, -2));
        spawnChestCoords.add(new Vector(0, -45, -2));
        
        this.mapChestCoords = new ArrayList<>(62);
        mapChestCoords.add(new Vector(-18, -45, -15));
        mapChestCoords.add(new Vector(-10, -37, -17));
        mapChestCoords.add(new Vector(-10, -31, -18));
        mapChestCoords.add(new Vector(-15, -28, -28));
        mapChestCoords.add(new Vector(-13, -28, -28));
        mapChestCoords.add(new Vector(-13, -34, -36));
        mapChestCoords.add(new Vector(-21, -34, -30));
        mapChestCoords.add(new Vector(-21, -40, -27));
        mapChestCoords.add(new Vector(-23, -45, -33));
        mapChestCoords.add(new Vector(-23, -45, 20));
        mapChestCoords.add(new Vector(-25, -44, 9));
        mapChestCoords.add(new Vector(-10, -45, 41));
        mapChestCoords.add(new Vector(-26, -44, 52));
        mapChestCoords.add(new Vector(-10, -38, 43));
        mapChestCoords.add(new Vector(-22, -30, 56));
        mapChestCoords.add(new Vector(-9, -31, 34));
        mapChestCoords.add(new Vector(35, -44, 19));
        mapChestCoords.add(new Vector(24, -51, 3));
        mapChestCoords.add(new Vector(38, -51, 23));
        mapChestCoords.add(new Vector(23, -51, 58));
        mapChestCoords.add(new Vector(-52, -51, 65));
        mapChestCoords.add(new Vector(-58, -51, -11));
        mapChestCoords.add(new Vector(-27, -45, -12));
        mapChestCoords.add(new Vector(-38, -39, -10));
        mapChestCoords.add(new Vector(-31, -33, -10));
        mapChestCoords.add(new Vector(-46, -43, 17));
        mapChestCoords.add(new Vector(-65, -42, 19));
        mapChestCoords.add(new Vector(-60, -43, 30));
        mapChestCoords.add(new Vector(-83, -43, 63));
        mapChestCoords.add(new Vector(-61, -43, 64));
        mapChestCoords.add(new Vector(-50, -43, 33));
        mapChestCoords.add(new Vector(22, -45, -23));
        mapChestCoords.add(new Vector(16, -45, -10));
        mapChestCoords.add(new Vector(30, -45, -44));
        mapChestCoords.add(new Vector(34, -43, -31));
        mapChestCoords.add(new Vector(22, -37, -45));
        mapChestCoords.add(new Vector(9, -27, -44));
        mapChestCoords.add(new Vector(16, -28, -13));
        mapChestCoords.add(new Vector(22, -40, -70));
        mapChestCoords.add(new Vector(8, -40, -81));
        mapChestCoords.add(new Vector(26, -45, 24));
        mapChestCoords.add(new Vector(-14, -45, -57));
        mapChestCoords.add(new Vector(-29, -45, -56));
        mapChestCoords.add(new Vector(-10, -51, -52));
        mapChestCoords.add(new Vector(-36, -51, -66));
        mapChestCoords.add(new Vector(-16, -39, -57));
        mapChestCoords.add(new Vector(-12, -33, -68));
        mapChestCoords.add(new Vector(-66, -45, -26));
        mapChestCoords.add(new Vector(-52, -48, -30));
        mapChestCoords.add(new Vector(-70, -27, -40));
        mapChestCoords.add(new Vector(-74, -44, -37));
        mapChestCoords.add(new Vector(-98, -45, -43));
        mapChestCoords.add(new Vector(-94, -40, -50));
        mapChestCoords.add(new Vector(-93, -44, -27));
        mapChestCoords.add(new Vector(-93, -39, -30));
        mapChestCoords.add(new Vector(-93, -34, -34));
        mapChestCoords.add(new Vector(-42, -45, -58));
        mapChestCoords.add(new Vector(-36, -39, -61));
        mapChestCoords.add(new Vector(-52, -33, -69));
        mapChestCoords.add(new Vector(-52, -27, -71));
        mapChestCoords.add(new Vector(-67, -51, -83));
        mapChestCoords.add(new Vector(-89, -50, -113));
    
        this.spawnLootTable = Bukkit.getLootTable(new NamespacedKey("mctdatapack", "mecha/spawn-chest"));
        
        this.mechaLootTables = new ArrayList<>(4);
        this.mechaLootTableWeights = new ArrayList<>();
        LootTable poor = Bukkit.getLootTable(new NamespacedKey("mctdatapack", "mecha/poor-chest"));
        mechaLootTables.add(poor);
        mechaLootTableWeights.add(3);
        
        LootTable good = Bukkit.getLootTable(new NamespacedKey("mctdatapack", "mecha/good-chest"));
        mechaLootTables.add(good);
        mechaLootTableWeights.add(3);
        
        LootTable better = Bukkit.getLootTable(new NamespacedKey("mctdatapack", "mecha/better-chest"));
        mechaLootTables.add(better);
        mechaLootTableWeights.add(2);
        
        LootTable excellent = Bukkit.getLootTable(new NamespacedKey("mctdatapack", "mecha/excellent-chest"));
        mechaLootTables.add(excellent);
        mechaLootTableWeights.add(1);
        
    }
}

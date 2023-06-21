package org.braekpo1nt.mctmanager.games.mecha;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.AnchorManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.mecha.io.MechaStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.structure.Structure;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

public class MechaGame implements MCTGame, Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    private boolean gameActive = false;
    private boolean mechaHasStarted = false;
    private List<Player> participants;
    private final World mechaWorld;
    private final MultiverseWorld mvMechaWorld;
    private int startMechaTaskId;
    private int stopMechaCountdownTaskId;
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
    private Map<LootTable, Integer> weightedMechaLootTables;
    /**
     * Holds the mecha spawn loot table from the mctdatapack
     */
    private LootTable spawnLootTable;
    private final WorldBorder worldBorder;
    private int borderShrinkingTaskId;
    private String lastKilledTeam;
    private List<UUID> livingPlayers;
    private List<UUID> deadPlayers;
    private Map<UUID, Integer> killCounts;
    private final String title = ChatColor.BLUE+"MECHA";
    private Map<String, Location> teamLocations;
    private final PotionEffect RESISTANCE = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 200, true, false, true);
    
    public MechaGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        setChestCoordsAndLootTables();
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        this.mvMechaWorld = worldManager.getMVWorld("FT");
        this.mechaWorld = mvMechaWorld.getCBWorld();
        this.worldBorder = mechaWorld.getWorldBorder();
    }
    
    @Override
    public MCTGames getType() {
        return MCTGames.MECHA;
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>(newParticipants.size());
        livingPlayers = new ArrayList<>(newParticipants.size());
        deadPlayers = new ArrayList<>();
        lastKilledTeam = null;
        this.killCounts = new HashMap<>(newParticipants.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        placePlatforms();
        fillAllChests();
        initializeTeamLocations();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        setUpTeamOptions();
        initializeWorldBorder();
        startStartMechaCountdownTask();
        gameActive = true;
        Bukkit.getLogger().info("Started mecha");
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        UUID participantUniqueId = participant.getUniqueId();
        livingPlayers.add(participantUniqueId);
        killCounts.put(participantUniqueId, 0);
        teleportParticipantToStartingPosition(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.getInventory().clear();
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        initializeFastBoard(participant);
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        clearFloorItems();
        placePlatforms();
        clearAllChests();
        lastKilledTeam = null;
        worldBorder.reset();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        mechaHasStarted = false;
        gameActive = false;
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopped mecha");
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        hideFastBoard(participant);
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        if (participantShouldRejoin(participant)) {
            messageAllParticipants(Component.text(participant.getName())
                    .append(Component.text(" is rejoining MECHA!"))
                    .color(NamedTextColor.YELLOW));
            rejoinParticipant(participant);
            return;
        }
        messageAllParticipants(Component.text(participant.getName())
                .append(Component.text(" is joining MECHA!"))
                .color(NamedTextColor.YELLOW));
        initializeParticipant(participant);
    }
    
    private void rejoinParticipant(Player participant) {
        participant.sendMessage(ChatColor.YELLOW + "You have rejoined MECHA");
        participants.add(participant);
        initializeFastBoard(participant);
        participant.setGameMode(GameMode.SPECTATOR);
    }
    
    /**
     * Checks if the participant was previously in the game, and should thus rejoin
     * @param participant The participant to check
     * @return True if the participant was in the game before, and should rejoin. False
     * if the participant wasn't in the game before. 
     */
    private boolean participantShouldRejoin(Player participant) {
        if (!mechaHasStarted) {
            return false;
        }
        return deadPlayers.contains(participant.getUniqueId());
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        if (!gameActive) {
            return;
        }
        if (!mechaHasStarted) {
            participants.remove(participant);
            UUID participantUniqueId = participant.getUniqueId();
            livingPlayers.remove(participantUniqueId);
            killCounts.remove(participantUniqueId);
            return;
        }
        List<ItemStack> drops = Arrays.stream(participant.getInventory().getContents())
                .filter(Objects::nonNull)
                .toList();
        int droppedExp = calculateExpPoints(participant.getLevel());
        Component deathMessage = Component.text(participant.getName())
                .append(Component.text(" left early. Their life is forfeit."));
        PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant, drops, droppedExp, deathMessage);
        Bukkit.getServer().getPluginManager().callEvent(fakeDeathEvent);
        resetParticipant(participant);
        participants.remove(participant);
    }
    
    private void setUpTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        }
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(startMechaTaskId);
        Bukkit.getScheduler().cancelTask(borderShrinkingTaskId);
        Bukkit.getScheduler().cancelTask(stopMechaCountdownTaskId);
    }
    
    private void startStartMechaCountdownTask() {
        this.startMechaTaskId = new BukkitRunnable() {
            int count = 10;
            @Override
            public void run() {
                if (count <= 0) {
                    startMecha();
                    this.cancel();
                    return;
                }
                messageAllParticipants(Component.text(count));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startStopMechaCountdownTask() {
        messageAllParticipants(Component.text("Game ending in 10 seconds..."));
        stopMechaCountdownTaskId = new BukkitRunnable() {
            int count = 10;
            @Override
            public void run() {
                if (count <= 0) {
                    stop();
                    this.cancel();
                    return;
                }
                if (count <= 3) {
                    messageAllParticipants(Component.text("Game ending in ")
                            .append(Component.text(count)));
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void switchPlayerFromLivingToDead(UUID playerUniqueId) {
        livingPlayers.remove(playerUniqueId);
        deadPlayers.add(playerUniqueId);
    }
    
    private void clearFloorItems() {
        Location min = new Location(mechaWorld, -130, -64, -130);
        Location max = new Location(mechaWorld, 130, 325, 130);
        BoundingBox removeArea = BoundingBox.of(min, max);
        for (Item item : mechaWorld.getEntitiesByClass(Item.class)) {
            if (removeArea.contains(item.getLocation().toVector())) {
                item.remove();
            }
        }
    }
    
    private void startMecha() {
        this.mechaHasStarted = true;
        kickOffBorderShrinking();
        removePlatforms();
        messageAllParticipants(Component.text("Go!"));
        giveInvulnerabilityForTenSeconds();
    }
    
    private void giveInvulnerabilityForTenSeconds() {
        for (Player participant : participants) {
            participant.addPotionEffect(RESISTANCE);
        }
        messageAllParticipants(Component.text("Invulnerable for 10 seconds!"));
    }
    
    private void onTeamWin(String winningTeamName) {
        Component displayNameComponent = gameManager.getFormattedTeamDisplayName(winningTeamName);
        Bukkit.getServer().sendMessage(Component.text("Team ")
                .append(displayNameComponent)
                .append(Component.text(" wins!")));
        startStopMechaCountdownTask();
    }
    
    private void startSuddenDeath() {
        displaySuddenDeath();
        messageAllParticipants(Component.text("Sudden death!"));
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
        dropInventory(killed, event.getDrops());
        event.setCancelled(true);
        if (event.getDeathSound() != null && event.getDeathSoundCategory() != null) {
            killed.getWorld().playSound(killed.getLocation(), event.getDeathSound(), event.getDeathSoundCategory(), event.getDeathSoundVolume(), event.getDeathSoundPitch());
        }
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            Bukkit.getServer().sendMessage(deathMessage);
        }
        onParticipantDeath(killed);
        if (killed.getKiller() != null) {
            onParticipantGetKill(killed);
        }
        String winningTeam = getWinningTeam();
        if (winningTeam != null) {
            onTeamWin(winningTeam);
        }
    }
    
    private void onParticipantDeath(Player killed) {
        UUID killedUniqueId = killed.getUniqueId();
        switchPlayerFromLivingToDead(killedUniqueId);
        String teamName = gameManager.getTeamName(killedUniqueId);
        if (teamIsAllDead(teamName)) {
            onTeamDeath(teamName);
        }
        lastKilledTeam = teamName;
    }
    
    private void onTeamDeath(String teamName) {
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(teamName);
        messageAllParticipants(Component.empty()
                .append(formattedTeamDisplayName)
                .append(Component.text(" has been eliminated.")));
        for (Player participant : participants) {
            if (livingPlayers.contains(participant.getUniqueId())) {
                gameManager.awardPointsToPlayer(participant, 40);
            }
        }
    }
    
    /**
     * Checks if the given team name is all dead
     * @param teamName The team to check
     * @return True if the given team is entirely dead (no living players left), false otherwise
     */
    private boolean teamIsAllDead(String teamName) {
        for (UUID livingPlayerUniqueId : livingPlayers) {
            String livingTeam = gameManager.getTeamName(livingPlayerUniqueId);
            if (teamName.equals(livingTeam)) {
                return false;
            }
        }
        return true;
    }
    
    private int calculateExpPoints(int level) {
        int maxExpPoints = level > 7 ? 100 : level * 7;
        return maxExpPoints / 10;
    }
    
    private void dropInventory(Player killed, List<ItemStack> drops) {
        for (ItemStack item : drops) {
            mechaWorld.dropItemNaturally(killed.getLocation(), item);
        }
        killed.getInventory().clear();
    }
    
    private void onParticipantGetKill(Player killed) {
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
        if (allLivingPlayersAreOnOneTeam()) {
            UUID winningPlayerUniqueId = livingPlayers.get(0);
            return gameManager.getTeamName(winningPlayerUniqueId);
        }
        if (allPlayersAreDead()) {
            return lastKilledTeam;
        }
        return null;
    }
    
    /**
     * Checks if there are no living players anymore
     * @return True if all players are dead, false if not
     */
    private boolean allPlayersAreDead() {
        return livingPlayers.isEmpty();
    }
    
    /**
     * Check if all the living players belong to a single team.
     * @return True if all living players are on a single team. False if there
     * are at least two players alive on different teams, or there are no
     * living players.
     */
    private boolean allLivingPlayersAreOnOneTeam() {
        if (livingPlayers.size() == 0) {
            return false;
        }
        if (livingPlayers.size() == 1) {
            return true;
        }
        UUID firstPlayerUUID = livingPlayers.get(0);
        String firstTeam = gameManager.getTeamName(firstPlayerUUID);
        for (int i = 1; i < livingPlayers.size(); i++) {
            UUID nextPlayerUUID = livingPlayers.get(i);
            String nextTeam = gameManager.getTeamName(nextPlayerUUID);
            if (!nextTeam.equals(firstTeam)) {
                return false;
            }
        }
        return true;
    }
    
    private void addKill(UUID killerUniqueId) {
        int oldKillCount = killCounts.get(killerUniqueId);
        int newKillCount = oldKillCount + 1;
        killCounts.put(killerUniqueId, newKillCount);
        gameManager.getFastBoardManager().updateLine(
                killerUniqueId,
                2, 
                ChatColor.RED+"Kills: " + newKillCount);
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
    
    private void initializeFastBoard(Player participant) {
        int killCount = killCounts.get(participant.getUniqueId());
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId(),
                title,
                "",
                ChatColor.RED+"Kills: "+killCount,
                "",
                ChatColor.LIGHT_PURPLE+"Border",
                ChatColor.LIGHT_PURPLE+"0:00"
        );
    }
    
    private void hideFastBoard(Player participant) {
        gameManager.getFastBoardManager().updateLines(
                participant.getUniqueId()
        );
    }
    /**
     * Sends a chat message to all participants saying the border is delaying
     * @param delay The delay in seconds
     */
    private void sendBorderDelayAnouncement(int delay) {
        String timeString = TimeStringUtils.getTimeString(delay);
        messageAllParticipants(Component.text("Border will not shrink for "+timeString));
    }
    
    /**
     * Sends a chat message to all participants saying the border is shrinking
     * @param duration The duration of the shrink in seconds
     * @param size The size of the border in blocks
     */
    private void sendBorderShrinkAnouncement(int duration, int size) {
        String timeString = TimeStringUtils.getTimeString(duration);
        messageAllParticipants(Component.text("Border shrinking to ")
                .append(Component.text(size))
                .append(Component.text(" for "))
                .append(Component.text(timeString)));
    }
    
    /**
     * Displays the time left for the border shrink to the participants on the FastBoards
     * @param duration The seconds left in the border shrink
     */
    private void displayBorderShrinkingFor(int duration) {
        String timeString = TimeStringUtils.getTimeString(duration);
        String borderPhase = ChatColor.RED+"Shrinking";
        String shrinkDuration = ChatColor.RED+timeString;
        for (Player participant : participants) {
            UUID playerUniqueId = participant.getUniqueId();
            gameManager.getFastBoardManager().updateLine(playerUniqueId, 
                    4, borderPhase);
            gameManager.getFastBoardManager().updateLine(playerUniqueId, 
                    5, shrinkDuration);
        }
    }
    
    /**
     * Displays the time left till the border shrinks again on the FastBoards
     * @param delay The seconds left till the border shrinks
     */
    private void displayBorderDelayFor(int delay) {
        String timeString = TimeStringUtils.getTimeString(delay);
        String borderPhase = ChatColor.LIGHT_PURPLE+"Border";
        String boardDelay = ChatColor.LIGHT_PURPLE+timeString;
        for (Player participant : participants) {
            UUID playerUniqueId = participant.getUniqueId();
            gameManager.getFastBoardManager().updateLine(playerUniqueId,
                    4, borderPhase);
            gameManager.getFastBoardManager().updateLine(playerUniqueId, 
                    5, boardDelay);
        }
    }
    
    /**
     * Displays the sudden death message on the FastBoards
     */
    private void displaySuddenDeath() {
        String borderPhase = ChatColor.RED+"Sudden death";
        for (Player participant : participants) {
            UUID playerUniqueId = participant.getUniqueId();
            gameManager.getFastBoardManager().updateLine(playerUniqueId,
                    4, borderPhase);
            gameManager.getFastBoardManager().updateLine(playerUniqueId,
                    5, "");
        }
    }
    
    private void teleportParticipantToStartingPosition(Player participant) {
        String team = gameManager.getTeamName(participant.getUniqueId());
        Location teamLocation = teamLocations.getOrDefault(team, teamLocations.get("yellow"));
        participant.teleport(teamLocation);
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
        LootTable lootTable = getRandomLootTable(weightedMechaLootTables);
        chest.setLootTable(lootTable);
        chest.update();
    }
    
    /**
     * Gets a random loot table from loots, using the provided weights
     * @return A loot table for a chest
     */
    private LootTable getRandomLootTable(Map<LootTable, Integer> weightedLootTables) {
        int totalWeight = 0;
        Collection<Integer> weights = weightedLootTables.values();
        for (int weight : weights) {
            totalWeight += weight;
        }
        int randomIndex = (int) (Math.random() * totalWeight);
        int weightSum = 0;
        for (Map.Entry<LootTable, Integer> entry : weightedLootTables.entrySet()) {
            int weight = entry.getValue();
            weightSum += weight;
            if (randomIndex < weightSum) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    private void messageAllParticipants(Component message) {
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
    private void initializeTeamLocations() {
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        teamLocations = new HashMap<>();
        teamLocations.put("orange", anchorManager.getAnchorLocation("mecha-orange"));
        teamLocations.put("yellow", anchorManager.getAnchorLocation("mecha-yellow"));
        teamLocations.put("green", anchorManager.getAnchorLocation("mecha-green"));
        teamLocations.put("dark-green", anchorManager.getAnchorLocation("mecha-dark-green"));
        teamLocations.put("cyan", anchorManager.getAnchorLocation("mecha-cyan"));
        teamLocations.put("blue", anchorManager.getAnchorLocation("mecha-blue"));
        teamLocations.put("purple", anchorManager.getAnchorLocation("mecha-purple"));
        teamLocations.put("red", anchorManager.getAnchorLocation("mecha-red"));
    }
    
    private void setChestCoordsAndLootTables() {
        MechaStorageUtil mechaStorageUtil = new MechaStorageUtil(plugin);
        this.spawnChestCoords = mechaStorageUtil.getSpawnChestCoords();
        this.mapChestCoords = mechaStorageUtil.getMapChestCoords();
        this.mapChestCoords = mechaStorageUtil.getMapChestCoords();
        this.spawnLootTable = mechaStorageUtil.getSpawnLootTable();
        this.weightedMechaLootTables = mechaStorageUtil.getWeightedMechaLootTables();
    }
    
    private void setChestCoordsAndLootTables2() {
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
//        mapChestCoords.add(new Vector(-18, -45, -15));
//        mapChestCoords.add(new Vector(-10, -37, -17));
//        mapChestCoords.add(new Vector(-10, -31, -18));
//        mapChestCoords.add(new Vector(-15, -28, -28));
//        mapChestCoords.add(new Vector(-13, -28, -28));
//        mapChestCoords.add(new Vector(-13, -34, -36));
//        mapChestCoords.add(new Vector(-21, -34, -30));
//        mapChestCoords.add(new Vector(-21, -40, -27));
//        mapChestCoords.add(new Vector(-23, -45, -33));
//        mapChestCoords.add(new Vector(-23, -45, 20));
//        mapChestCoords.add(new Vector(-25, -44, 9));
//        mapChestCoords.add(new Vector(-10, -45, 41));
//        mapChestCoords.add(new Vector(-26, -44, 52));
//        mapChestCoords.add(new Vector(-10, -38, 43));
//        mapChestCoords.add(new Vector(-22, -30, 56));
//        mapChestCoords.add(new Vector(-9, -31, 34));
//        mapChestCoords.add(new Vector(35, -44, 19));
//        mapChestCoords.add(new Vector(24, -51, 3));
//        mapChestCoords.add(new Vector(38, -51, 23));
//        mapChestCoords.add(new Vector(23, -51, 58));
//        mapChestCoords.add(new Vector(-52, -51, 65));
//        mapChestCoords.add(new Vector(-58, -51, -11));
//        mapChestCoords.add(new Vector(-27, -45, -12));
//        mapChestCoords.add(new Vector(-38, -39, -10));
//        mapChestCoords.add(new Vector(-31, -33, -10));
//        mapChestCoords.add(new Vector(-46, -43, 17));
//        mapChestCoords.add(new Vector(-65, -42, 19));
//        mapChestCoords.add(new Vector(-60, -43, 30));
//        mapChestCoords.add(new Vector(-83, -43, 63));
//        mapChestCoords.add(new Vector(-61, -43, 64));
//        mapChestCoords.add(new Vector(-50, -43, 33));
//        mapChestCoords.add(new Vector(22, -45, -23));
//        mapChestCoords.add(new Vector(16, -45, -10));
//        mapChestCoords.add(new Vector(30, -45, -44));
//        mapChestCoords.add(new Vector(34, -43, -31));
//        mapChestCoords.add(new Vector(22, -37, -45));
//        mapChestCoords.add(new Vector(9, -27, -44));
//        mapChestCoords.add(new Vector(16, -28, -13));
//        mapChestCoords.add(new Vector(22, -40, -70));
//        mapChestCoords.add(new Vector(8, -40, -81));
//        mapChestCoords.add(new Vector(26, -45, 24));
//        mapChestCoords.add(new Vector(-14, -45, -57));
//        mapChestCoords.add(new Vector(-29, -45, -56));
//        mapChestCoords.add(new Vector(-10, -51, -52));
//        mapChestCoords.add(new Vector(-36, -51, -66));
//        mapChestCoords.add(new Vector(-16, -39, -57));
//        mapChestCoords.add(new Vector(-12, -33, -68));
//        mapChestCoords.add(new Vector(-66, -45, -26));
//        mapChestCoords.add(new Vector(-52, -48, -30));
//        mapChestCoords.add(new Vector(-70, -27, -40));
//        mapChestCoords.add(new Vector(-74, -44, -37));
//        mapChestCoords.add(new Vector(-98, -45, -43));
//        mapChestCoords.add(new Vector(-94, -40, -50));
//        mapChestCoords.add(new Vector(-93, -44, -27));
//        mapChestCoords.add(new Vector(-93, -39, -30));
//        mapChestCoords.add(new Vector(-93, -34, -34));
//        mapChestCoords.add(new Vector(-42, -45, -58));
//        mapChestCoords.add(new Vector(-36, -39, -61));
//        mapChestCoords.add(new Vector(-52, -33, -69));
//        mapChestCoords.add(new Vector(-52, -27, -71));
//        mapChestCoords.add(new Vector(-67, -51, -83));
//        mapChestCoords.add(new Vector(-89, -50, -113));
        mapChestCoords.add(new Vector(65, -36, -20));
        mapChestCoords.add(new Vector(81, -41, -14));
        mapChestCoords.add(new Vector(94, -41, 19));
        mapChestCoords.add(new Vector(92, -35, 14));
        mapChestCoords.add(new Vector(75, -41, 37));
        mapChestCoords.add(new Vector(68, -41, 27));
        mapChestCoords.add(new Vector(63, -41, 66));
        mapChestCoords.add(new Vector(89, -41, 72));
        mapChestCoords.add(new Vector(89, -41, 65));
        mapChestCoords.add(new Vector(76, -36, 20));
        mapChestCoords.add(new Vector(112, -49, -9));
        mapChestCoords.add(new Vector(104, -41, -13));
        mapChestCoords.add(new Vector(135, -45, -23));
        mapChestCoords.add(new Vector(129, -35, -13));
        mapChestCoords.add(new Vector(132, -35, -23));
        mapChestCoords.add(new Vector(94, -45, -41));
        mapChestCoords.add(new Vector(53, -51, -39));
        mapChestCoords.add(new Vector(53, -58, -88));
        mapChestCoords.add(new Vector(89, -51, 10));
        mapChestCoords.add(new Vector(84, -39, -102));
        mapChestCoords.add(new Vector(84, -37, -91));
        mapChestCoords.add(new Vector(120, -47, -97));
        mapChestCoords.add(new Vector(134, -42, -114));
        mapChestCoords.add(new Vector(118, -45, -49));
        mapChestCoords.add(new Vector(129, -41, -64));
        mapChestCoords.add(new Vector(129, -37, -55));
        mapChestCoords.add(new Vector(154, -45, -40));
        mapChestCoords.add(new Vector(160, -41, -45));
        mapChestCoords.add(new Vector(149, -37, -52));
        mapChestCoords.add(new Vector(-26, -40, -132));
        mapChestCoords.add(new Vector(-48, -44, -121));
        mapChestCoords.add(new Vector(-33, -37, -121));
        mapChestCoords.add(new Vector(33, -33, -145));
        mapChestCoords.add(new Vector(21, -27, -134));
        mapChestCoords.add(new Vector(150, -45, -143));
        mapChestCoords.add(new Vector(142, -37, -132));
        mapChestCoords.add(new Vector(83, -45, -146));
        mapChestCoords.add(new Vector(97, -40, -146));
        mapChestCoords.add(new Vector(101, -35, -132));
        mapChestCoords.add(new Vector(117, -51, 83));
        mapChestCoords.add(new Vector(100, -45, 124));
        mapChestCoords.add(new Vector(87, -40, 130));
        mapChestCoords.add(new Vector(48, -45, 128));
        mapChestCoords.add(new Vector(53, -40, 128));
        mapChestCoords.add(new Vector(78, -45, 29));
        mapChestCoords.add(new Vector(51, -46, 45));
        mapChestCoords.add(new Vector(78, -45, -52));
        mapChestCoords.add(new Vector(49, -46, -101));
        mapChestCoords.add(new Vector(146, -45, -107));
        mapChestCoords.add(new Vector(172, -45, -91));
        mapChestCoords.add(new Vector(102, -45, 126));
        mapChestCoords.add(new Vector(117, -45, 100));
        mapChestCoords.add(new Vector(157, -45, 76));
        mapChestCoords.add(new Vector(39, -46, 93));
        mapChestCoords.add(new Vector(55, -45, 85));
        mapChestCoords.add(new Vector(-43, -29, -119));
        mapChestCoords.add(new Vector(12, -51, -41));
        mapChestCoords.add(new Vector(-33, -51, -44));
        mapChestCoords.add(new Vector(29, -45, -27));
        mapChestCoords.add(new Vector(29, -37, -27));
        mapChestCoords.add(new Vector(131, -45, 61));
        
        this.spawnLootTable = Bukkit.getLootTable(new NamespacedKey("mctdatapack", "mecha/spawn-chest"));
        
        this.weightedMechaLootTables = new HashMap<>(4);
        LootTable poor = Bukkit.getLootTable(new NamespacedKey("mctdatapack", "mecha/poor-chest"));
        weightedMechaLootTables.put(poor, 2);
        
        LootTable good = Bukkit.getLootTable(new NamespacedKey("mctdatapack", "mecha/good-chest"));
        weightedMechaLootTables.put(good, 3);
        
        LootTable better = Bukkit.getLootTable(new NamespacedKey("mctdatapack", "mecha/better-chest"));
        weightedMechaLootTables.put(better, 2);
        
        LootTable excellent = Bukkit.getLootTable(new NamespacedKey("mctdatapack", "mecha/excellent-chest"));
        weightedMechaLootTables.put(excellent, 1);
        
    }
}

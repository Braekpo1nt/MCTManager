package org.braekpo1nt.mctmanager.games.game.spleef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.structure.Structure;
import org.bukkit.util.BoundingBox;

import java.util.*;

public class SpleefRound implements Listener {
    private final Main plugin;
    private final GameManager gameManager;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final SpleefStorageUtil storageUtil;
    private List<Player> participants = new ArrayList<>();
    private Map<UUID, Boolean> participantsAlive;
    private boolean spleefHasStarted = false;
    private boolean roundActive = false;
    private final SpleefGame spleefGame;
    private final DecayManager decayManager;
    private final PowerupManager powerupManager;
    private int startCountDownTaskID;
    
    public SpleefRound(Main plugin, GameManager gameManager, SpleefGame spleefGame, SpleefStorageUtil spleefStorageUtil, Sidebar sidebar, Sidebar adminSidebar) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.spleefGame = spleefGame;
        this.storageUtil = spleefStorageUtil;
        this.sidebar = sidebar;
        this.adminSidebar = adminSidebar;
        this.decayManager = new DecayManager(plugin, storageUtil, this);
        this.powerupManager = new PowerupManager(plugin, storageUtil);
    }
    
    public void start(List<Player> newParticipants) {
        this.participants = new ArrayList<>(newParticipants.size());
        participantsAlive = new HashMap<>(newParticipants.size());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        placeLayers(true);
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        initializeSidebar();
        initializeAdminSidebar();
        setupTeamOptions();
        startRoundStartingCountDown();
        decayManager.setAliveCount(newParticipants.size());
        spleefHasStarted = false;
        roundActive = true;
        Bukkit.getLogger().info("Starting Spleef round");
    }
    
    private void initializeParticipant(Player participant) {
        UUID participantUniqueId = participant.getUniqueId();
        participants.add(participant);
        participantsAlive.put(participantUniqueId, true);
        teleportPlayerToRandomStartingPosition(participant);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void rejoinParticipant(Player participant) {
        participant.sendMessage(ChatColor.YELLOW + "You have rejoined Spleef");
        participants.add(participant);
        participant.setGameMode(GameMode.SPECTATOR);
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void roundIsOver() {
        stop();
        spleefGame.roundIsOver();
    }
    
    public void stop() {
        spleefHasStarted = false;
        roundActive = false;
        HandlerList.unregisterAll(this);
        decayManager.stop();
        powerupManager.stop();
        placeLayers(false);
        cancelAllTasks();
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        clearSidebar();
        clearAdminSidebar();
        participants.clear();
        participantsAlive.clear();
        Bukkit.getLogger().info("Stopping Spleef round");
    }
    
    public boolean isActive() {
        return roundActive;
    }
    
    public void onParticipantJoin(Player participant) {
        if (!roundActive) {
            return;
        }
        if (participantShouldRejoin(participant)) {
            rejoinParticipant(participant);
            messageAllParticipants(Component.text(participant.getName())
                    .append(Component.text(" is rejoining Spleef!"))
                    .color(NamedTextColor.YELLOW));
        } else {
            initializeParticipant(participant);
            if (spleefHasStarted) {
                giveTool(participant);
                participant.setGameMode(GameMode.SURVIVAL);
            }
            messageAllParticipants(Component.text(participant.getName())
                    .append(Component.text(" is joining Spleef!"))
                    .color(NamedTextColor.YELLOW));
        }
        powerupManager.addParticipant(participant);
        long aliveCount = getAliveCount();
        String alive = String.format("Alive: %s", aliveCount);
        sidebar.updateLine("alive", alive);
        adminSidebar.updateLine("alive", alive);
        decayManager.setAliveCount(aliveCount);
    }
    
    private boolean participantShouldRejoin(Player participant) {
        if (!roundActive) {
            return false;
        }
        return participantsAlive.containsKey(participant.getUniqueId());
    }
    
    public void onParticipantQuit(Player participant) {
        if (!roundActive) {
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
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!roundActive) {
            return;
        }
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (!cause.equals(EntityDamageEvent.DamageCause.LAVA)
                && !cause.equals(EntityDamageEvent.DamageCause.FIRE)) {
            event.setCancelled(true);
            return;
        }
        if (!spleefHasStarted) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        participant.setFoodLevel(20);
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!roundActive) {
            return;
        }
        Player killed = event.getPlayer();
        if (!participants.contains(killed)) {
            return;
        }
        killed.setGameMode(GameMode.SPECTATOR);
        killed.getInventory().clear();
        event.setCancelled(true);
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            Bukkit.getServer().sendMessage(deathMessage);
        }
        onParticipantDeath(killed);
        if (lessThanTwoPlayersAlive() || exactlyOneTeamIsAlive()) {
            roundIsOver();
        }
    }
    
    /**
     * @return true if exactly one team is alive, false otherwise
     */
    private boolean exactlyOneTeamIsAlive() {
        String onlyTeam = null;
        for (Player participant : participants) {
            if (participantsAlive.get(participant.getUniqueId())) {
                String livingTeam = gameManager.getTeamName(participant.getUniqueId());
                if (onlyTeam == null) {
                    onlyTeam = livingTeam;
                } else if (!onlyTeam.equals(livingTeam)) {
                    return false;
                }
            }
        }
        return onlyTeam != null;
    }
    
    private boolean lessThanTwoPlayersAlive() {
        return getAliveCount() < 2;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!roundActive) {
            return;
        }
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (powerupManager.isPowerup(event.getItem())) {
                return;
            }
            event.setCancelled(true);
        }
    }
    
    private void onParticipantDeath(Player killed) {
        ParticipantInitializer.clearStatusEffects(killed);
        ParticipantInitializer.resetHealthAndHunger(killed);
        killed.getInventory().clear();
        participantsAlive.put(killed.getUniqueId(), false);
        powerupManager.removeParticipant(killed);
        String killedTeam = gameManager.getTeamName(killed.getUniqueId());
        int count = participants.size();
        for (Player participant : participants) {
            if (participantsAlive.get(participant.getUniqueId())) {
                String teamName = gameManager.getTeamName(participant.getUniqueId());
                if (!teamName.equals(killedTeam)) {
                    gameManager.awardPointsToParticipant(participant, storageUtil.getSurviveScore());
                }
            } else {
                count--;
            }
        }
        String alive = String.format("Alive: %s", count);
        sidebar.updateLine("alive", alive);
        adminSidebar.updateLine("alive", alive);
        decayManager.setAliveCount(getAliveCount());
    }
    
    private void startSpleef() {
        String alive = String.format("Alive: %s", participants.size());
        sidebar.updateLine("alive", alive);
        adminSidebar.updateLine("alive", alive);
        giveTools();
        for (Player participant : participants) {
            participant.setGameMode(GameMode.SURVIVAL);
        }
        spleefHasStarted = true;
        decayManager.start();
        powerupManager.start(participants);
    }
    
    private void giveTools() {
        for (Player participant : participants) {
            participant.getInventory().addItem(storageUtil.getTool());
        }
    }
    
    private void giveTool(Player participant) {
        participant.getInventory().addItem(storageUtil.getTool());
    }
    
    private void startRoundStartingCountDown() {
        this.startCountDownTaskID = new BukkitRunnable() {
            private int count = storageUtil.getRoundStartingDuration();

            @Override
            public void run() {
                if (count <= 0) {
                    sidebar.updateLine("timer", "");
                    adminSidebar.updateLine("timer", "");
                    startSpleef();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                String timer = String.format("Starting in: %s", timeLeft);
                sidebar.updateLine("timer", timer);
                adminSidebar.updateLine("timer", timer);
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void placeLayers(boolean replaceStencil) {
        for (int i = 0; i < storageUtil.getStructures().size(); i++) {
            Structure layer = storageUtil.getStructures().get(i);
            layer.place(storageUtil.getStructureOrigins().get(i), true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
        }
        if (replaceStencil && storageUtil.getStencilBlock() != null) {
            for (BoundingBox layerArea : storageUtil.getDecayLayers()) {
                BlockPlacementUtils.createCubeReplace(storageUtil.getWorld(), layerArea, storageUtil.getStencilBlock(), storageUtil.getLayerBlock());
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!roundActive) {
            return;
        }
        Player participant = event.getPlayer();
        if (!participants.contains(participant)) {
            return;
        }
        powerupManager.onParticipantBreakBlock(participant);
        event.setDropItems(false);
    }
    
    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
    }
    
    private void initializeAdminSidebar() {
        adminSidebar.addLine("alive", "Alive: ");
    }
    
    private void clearAdminSidebar() {
        adminSidebar.deleteLine("alive");
    }
    
    private void initializeSidebar() {
        sidebar.addLine("alive", "Alive: ");
    }
    
    private void clearSidebar() {
        sidebar.deleteLine("alive");
    }
    
    private void teleportPlayerToRandomStartingPosition(Player player) {
        player.sendMessage("Teleporting to Spleef");
        int index = new Random().nextInt(storageUtil.getStartingLocations().size());
        player.teleport(storageUtil.getStartingLocations().get(index));
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(startCountDownTaskID);
    }
    
    void messageAllParticipants(Component message) {
        gameManager.messageAdmins(message);
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
    private int calculateExpPoints(int level) {
        int maxExpPoints = level > 7 ? 100 : level * 7;
        return maxExpPoints / 10;
    }
    
    /**
     * @return the number of participants who are alive in this round
     */
    private long getAliveCount() {
        return participantsAlive.values().stream().filter(value -> value).count();
    }
}

package org.braekpo1nt.mctmanager.games.clockwork;

import com.google.gson.internal.bind.JsonTreeReader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;

import java.util.*;
import java.util.List;

public class ClockworkRound implements Listener {

    private final Main plugin;
    private final GameManager gameManager;
    private final ClockworkGame clockworkGame;
    private final Location startingPosition;
    private Map<UUID, Boolean> participantsAreAlive;
    private Map<String, Boolean> teamsAreAlive;
    private List<Player> participants;
    private static final String title = ChatColor.BLUE+"Clockwork";
    private boolean roundActive;
    private int roundStartingCountDownTaskId;
    private int bellCountDownTaskId;
    private int bellCountDown = 8;
    private int numberOfBellRings = 1;
    private long bellRingCycleDuration = 20L;
    private final List<Wedge> wedges;
    private int ringBellTaskId;
    private final Random random = new Random();
    private String lastKilledTeam = null;
    private boolean skeletonCycle = false;

    public ClockworkRound(Main plugin, GameManager gameManager, ClockworkGame clockworkGame, Location startingPosition) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.clockworkGame = clockworkGame;
        this.startingPosition = startingPosition;
        this.wedges = createWedges();
    }
    
    public void start(List<Player> newParticipants) {
        participants = new ArrayList<>(newParticipants.size());
        participantsAreAlive = new HashMap<>(newParticipants.size());
        teamsAreAlive = new HashMap<>();
        lastKilledTeam = null;
        numberOfBellRings = 1;
        bellCountDown = 8;
        bellRingCycleDuration = 20L;
        skeletonCycle = false;
        List<String> teams = gameManager.getTeamNames(newParticipants);
        for (String team : teams) {
            teamsAreAlive.put(team, true);
        }
        String livingTeams = ""+teamsAreAlive.size();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : newParticipants) {
            initializeParticipant(participant, livingTeams);
        }
        setupTeamOptions();
        startRoundStartingCountDown();
        roundActive = true;
        Bukkit.getLogger().info("Starting capture the flag round");
    }

    private void roundIsOver() {
        stop();
        clockworkGame.roundIsOver();
    }

    public void stop() {
        roundActive = false;
        killAllSkeletons();
        HandlerList.unregisterAll(this);
        cancelAllTasks();
        lastKilledTeam = null;
        numberOfBellRings = 1;
        bellCountDown = 8;
        bellRingCycleDuration = 20L;
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        participants.clear();
        participantsAreAlive.clear();
        Bukkit.getLogger().info("Stopping clockwork round");
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
        if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
            event.setDamage(0);
            ParticipantInitializer.resetHealthAndHunger(participant);
            return;
        }
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
        if (event.getDeathSound() != null && event.getDeathSoundCategory() != null) {
            killed.getWorld().playSound(killed.getLocation(), event.getDeathSound(), event.getDeathSoundCategory(), event.getDeathSoundVolume(), event.getDeathSoundPitch());
        }
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            Bukkit.getServer().sendMessage(deathMessage);
        }
        onParticipantDeath(killed);
        String winningTeam = getWinningTeam();
        if (winningTeam != null) {
            onTeamWin(winningTeam);
        }
    }

    private void onTeamWin(String winningTeam) {
        gameManager.awardPointsToTeam(winningTeam, 50);
        Component displayName = gameManager.getFormattedTeamDisplayName(winningTeam);
        messageAllParticipants(Component.text("Team ")
                .append(displayName)
                .append(Component.text(" wins this round!")));
        roundIsOver();
    }

    private void onParticipantDeath(Player killed) {
        UUID killedUniqueId = killed.getUniqueId();
        participantsAreAlive.put(killedUniqueId, false);
        String teamName = gameManager.getTeamName(killedUniqueId);
        if (teamIsAllDead(teamName)) {
            onTeamDeath(teamName);
        }
        for (Player participant : participants) {
            if (participantsAreAlive.get(participant.getUniqueId())) {
                gameManager.awardPointsToPlayer(participant, 5);
            }
        }
        lastKilledTeam = teamName;
    }
    
    private void onTeamDeath(String teamName) {
        Component displayName = gameManager.getFormattedTeamDisplayName(teamName);
        messageAllParticipants(Component.empty()
                .append(displayName)
                .append(Component.text(" has been eliminated.")));
        teamsAreAlive.put(teamName, false);
        int count = 0;
        for (boolean alive : teamsAreAlive.values()) {
            if (alive) {
                count++;
            }
        }
        String teamsAlive = ""+count;
        for (Player participant : participants) {
            if (participantsAreAlive.get(participant.getUniqueId())) {
                gameManager.awardPointsToPlayer(participant, 20);
            }
            updateTeamsAliveFastBoard(participant, teamsAlive);
        }
    }

    private boolean teamIsAllDead(String teamName) {
        for (Map.Entry<UUID, Boolean> participantIsAlive : participantsAreAlive.entrySet()) {
            if (participantIsAlive.getValue()) {
                String aliveTeamName = gameManager.getTeamName(participantIsAlive.getKey());
                if (teamName.equals(aliveTeamName)) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getWinningTeam() {
        int count = 0;
        for (boolean isAlive : teamsAreAlive.values()) {
            if (isAlive) {
                count++;
            }
        }
        if (count > 1) {
            return null;
        }
        for (Map.Entry<String, Boolean> teamIsAlive : teamsAreAlive.entrySet()) {
            if (teamIsAlive.getValue()) {
                return teamIsAlive.getKey();
            }
        }
        if (count == 0) {
            return lastKilledTeam;
        }
        return null;
    }

    private void startRoundStartingCountDown() {
        this.roundStartingCountDownTaskId = new BukkitRunnable() {
            int count = 10;
            @Override
            public void run() {
                if (count <= 0) {
                    startClockwork();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                for (Player participant : participants){
                    updateRoundStartingCountDown(participant, timeLeft);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startClockwork() {
        int count = 0;
        for (boolean alive : teamsAreAlive.values()) {
            if (alive) {
                count++;
            }
        }
        String teamsAlive = ""+count;
        for (Player participant : participants) {
            updateTeamsAliveFastBoard(participant, teamsAlive);
        }
        ringBell();
    }
    
    private void ringBell() {
        numberOfBellRings = random.nextInt(1, 13);
        this.ringBellTaskId = new BukkitRunnable() {
            private int count = numberOfBellRings;
            @Override
            public void run() {
                if (count <= 0) {
                    startBellCountDown();
                    if (skeletonCycle) {
                        summonSkeleton();
                    }
                    skeletonCycle = !skeletonCycle;
                    if (bellRingCycleDuration > 2) {
                        bellRingCycleDuration = bellRingCycleDuration - 2;
                    }
                    if (bellRingCycleDuration < 10) {
                        bellCountDown = 5;
                    }
                    this.cancel();
                    return;
                }
                for (Player participant : participants) {
                    participant.playSound(participant.getLocation(), Sound.BLOCK_BELL_USE, 100, 2);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, bellRingCycleDuration).getTaskId();
    }

    private void startBellCountDown() {
        this.bellCountDownTaskId = new BukkitRunnable() {
            int count = bellCountDown;
            @Override
            public void run() {
                if (count <= 0) {
                    onBellCountDownRunOut();
                    this.cancel();
                    return;
                }
                String timeLeft = ChatColor.RED+""+count;
                for (Player participant : participants) {
                    updateBellCountDownFastBoard(participant, timeLeft);
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void onBellCountDownRunOut() {
        for (Player participant : participants) {
            updateBellCountDownFastBoard(participant, "");
        }
        killPlayersNotOnWedge();
        this.bellCountDownTaskId = new BukkitRunnable() {
            int count = 5;
            @Override
            public void run() {
                if (count <= 0) {
                    ringBell();
                    this.cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    private void killPlayersNotOnWedge() {
        Wedge wedge = wedges.get(numberOfBellRings-1);
        for (Player participant : participants) {
            Component deathMessage = Component.text(participant.getName())
                    .append(Component.text(" was eliminated"));
            if (!wedge.isInside(participant)) {
                PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant, Collections.emptyList(), 0, deathMessage);
                Bukkit.getPluginManager().callEvent(fakeDeathEvent);
            }
        }
    }

    private void setupTeamOptions() {
        Scoreboard mctScoreboard = gameManager.getMctScoreboard();
        for (Team team : mctScoreboard.getTeams()) {
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
    }

    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }

    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(roundStartingCountDownTaskId);
        Bukkit.getScheduler().cancelTask(bellCountDownTaskId);
        Bukkit.getScheduler().cancelTask(ringBellTaskId);
    }

    private void updateBellCountDownFastBoard(Player participant, String timeLeft) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                6,
                timeLeft
        );
    }
    
    private void updateRoundStartingCountDown(Player participant, String timeLeft) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                3,
                "Starting:"
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                timeLeft
        );
    }

    private void initializeParticipant(Player participant, String livingTeams) {
        participants.add(participant);
        UUID participantUniqueId = participant.getUniqueId();
        participantsAreAlive.put(participantUniqueId, true);
        initializeFastBoard(participant, livingTeams);
        participant.teleport(startingPosition);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }

    private void initializeFastBoard(Player participant, String livingTeams) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                3,
                "Teams:"// teams alive
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                livingTeams// teams alive
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                6,
                ""
        );
    }
    
    private void updateTeamsAliveFastBoard(Player participant, String livingTeams) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                3,
                "Teams:" // teams alive
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                livingTeams // teams alive
        );
    }
    
    private void messageAllParticipants(Component message) {
        for (Player participant : participants) {
            participant.sendMessage(message);
        }
    }
    
    private List<Wedge> createWedges() {
        List<Wedge> newWedges = new ArrayList<>();
        newWedges.add(createWedge(-1013, -1, -984)); //1
        newWedges.add(createWedge(-1018, -1, -989)); //2
        newWedges.add(createWedge(-1020, -1, -1001)); //3
        newWedges.add(createWedge(-1018, -1, -1013)); //4
        newWedges.add(createWedge(-1013, -1, -1018)); //5
        newWedges.add(createWedge(-1001, -1, -1020)); //6
        newWedges.add(createWedge(-989, -1, -1018)); //7
        newWedges.add(createWedge(-984, -1, -1013)); //8
        newWedges.add(createWedge(-982, -1, -1001)); //9
        newWedges.add(createWedge(-984, -1, -989)); //10
        newWedges.add(createWedge(-989, -1, -984)); //11
        newWedges.add(createWedge(-1001, -1, -982)); //12
        return newWedges;
    }

    private void summonSkeleton() {
        World world = startingPosition.getWorld();
        if (world != null) {
            Skeleton skeleton = (Skeleton) world.spawnEntity(startingPosition, EntityType.SKELETON);
            skeleton.setLootTable(null);
            // Additional configuration for the summoned skeleton can be done here
        }
    }
    
    private void killAllSkeletons() {
        for (Entity entity : startingPosition.getWorld().getEntities()) {
            if (entity.getType() == EntityType.SKELETON) {
                entity.remove();
            }
        }
    }

    public Wedge createWedge(int x, int y, int z) {
        int sizeX = 3; // Size along X-axis
        int sizeY = 10; // Size along Y-axis
        int sizeZ = 3; // Size along Z-axis
        
        // Calculate the minimum and maximum coordinates
        int maxX = x + sizeX;
        int maxY = y + sizeY;
        int maxZ = z + sizeZ;
        
        // Create and return the BoundingBox
        return new Wedge(new BoundingBox(x, y, z, maxX, maxY, maxZ));
    }
}

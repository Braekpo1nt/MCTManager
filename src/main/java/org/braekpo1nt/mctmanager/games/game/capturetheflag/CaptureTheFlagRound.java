package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfig;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A round is made up of multiple matches. It kicks off the matches it contains, and ends
 * when all the matches are over.
 */
public class CaptureTheFlagRound {
    
    private final CaptureTheFlagGame captureTheFlagGame;
    private final Main plugin;
    private final GameManager gameManager;
    private final CaptureTheFlagConfig config;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final BattleTopbar topbar;
    private final List<CaptureTheFlagMatch> matches;
    private List<Player> participants = new ArrayList<>();
    private List<Player> onDeckParticipants;
    private int matchesStartingCountDownTaskId;
    private int onDeckClassSelectionTimerTaskId;
    private int onDeckMatchTimerTaskId;
    private boolean roundActive = false;
    private boolean firstRound = false;
    private int descriptionPeriodTaskId;
    private boolean descriptionShowing = false;
    /**
     * false if the countdown timer is still going and the matches haven't started yet for this round. False otherwise. 
     */
    private boolean matchesStarted = false;
    
    public CaptureTheFlagRound(CaptureTheFlagGame captureTheFlagGame, Main plugin, GameManager gameManager, CaptureTheFlagConfig config, List<MatchPairing> matchPairings, Sidebar sidebar, Sidebar adminSidebar, BattleTopbar topbar) {
        this.captureTheFlagGame = captureTheFlagGame;
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.config = config;
        this.sidebar = sidebar;
        this.adminSidebar = adminSidebar;
        this.topbar = topbar;
        this.matches = createMatches(matchPairings, config.getArenas());
    }
    
    /**
     * Creates the matches for this round from the provided matchPairings and arenas. matchPairings.size() 
     * must be less than or equal to arenas.size(), or you will get null pointer exceptions. 
     * @param matchPairings The MatchPairings to create {@link CaptureTheFlagMatch}s from
     * @param arenas The arenas to assign to each {@link CaptureTheFlagMatch}
     * @throws NullPointerException if matchPairings.size() is greater than arenas.size()
     */
    public List<CaptureTheFlagMatch> createMatches(List<MatchPairing> matchPairings, List<Arena> arenas) {
        List<CaptureTheFlagMatch> newMatches = new ArrayList<>();
        for (int i = 0; i < matchPairings.size(); i++) {
            MatchPairing matchPairing = matchPairings.get(i);
            Arena arena = arenas.get(i);
            CaptureTheFlagMatch match = new CaptureTheFlagMatch(this, plugin, gameManager, 
                    matchPairing, arena, config, sidebar, adminSidebar, topbar);
            newMatches.add(match);
        }
        return newMatches;
    }
    
    public boolean isActive() {
        return roundActive;
    }
    
    public void start(List<Player> newParticipants, List<Player> newOnDeckParticipants) {
        participants = new ArrayList<>();
        onDeckParticipants = new ArrayList<>();
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        for (Player onDeck : newOnDeckParticipants) {
            initializeOnDeckParticipant(onDeck);
        }
        initializeSidebar();
        roundActive = true;
        matchesStarted = false;
        if (firstRound) {
            startDescriptionPeriod();
        } else {
            startMatchesStartingCountDown();
        }
    }
    
    private void initializeParticipant(Player participant) {
        participants.add(participant);
        participant.teleport(config.getSpawnObservatory());
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void initializeOnDeckParticipant(Player onDeckParticipant) {
        onDeckParticipants.add(onDeckParticipant);
        onDeckParticipant.teleport(config.getSpawnObservatory());
        onDeckParticipant.getInventory().clear();
        onDeckParticipant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(onDeckParticipant);
        ParticipantInitializer.resetHealthAndHunger(onDeckParticipant);
    }
    
    private void roundIsOver() {
        stop();
        captureTheFlagGame.roundIsOver();
    }
    public void stop() {
        cancelAllTasks();
        roundActive = false;
        descriptionShowing = false;
        for (CaptureTheFlagMatch match : matches) {
            if (match.isActive()) {
                match.stop();
            }
        }
        for (Player participant : participants) {
            resetParticipant(participant);
        }
        for (Player onDeckParticipant : onDeckParticipants) {
            resetOnDeckParticipant(onDeckParticipant);
        }
        participants.clear();
    }
    
    private void resetParticipant(Player participant) {
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private void resetOnDeckParticipant(Player onDeckParticipant) {
        onDeckParticipant.getInventory().clear();
        onDeckParticipant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(onDeckParticipant);
        ParticipantInitializer.resetHealthAndHunger(onDeckParticipant);
    }
    
    public void onParticipantJoin(Player participant) {
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        Component teamDisplayName = gameManager.getFormattedTeamDisplayName(teamId);
        CaptureTheFlagMatch match = getMatch(teamId);
        if (match == null) {
            initializeOnDeckParticipant(participant);
            participant.sendMessage(Component.empty()
                    .append(teamDisplayName)
                    .append(Component.text(" is on-deck this round."))
                    .color(NamedTextColor.YELLOW));
            sidebar.updateLine(participant.getUniqueId(), "enemy", "On Deck");
            topbar.setLeft(Component.text("On Deck"));
            return;
        }
        initializeParticipant(participant);
        String enemyTeamId = getOppositeTeam(teamId);
        ChatColor enemyColor = gameManager.getTeamChatColor(enemyTeamId);
        String enemyDisplayName = gameManager.getTeamDisplayName(enemyTeamId);
        sidebar.updateLine(participant.getUniqueId(), "enemy", String.format("%svs: %s%s", ChatColor.BOLD, enemyColor, enemyDisplayName));
        if (match.isActive()) {
            match.onParticipantJoin(participant);
        }
    }
    
    public void onParticipantQuit(Player participant) {
        if (onDeckParticipants.contains(participant)) {
            onOnDeckParticipantQuit(participant);
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        String teamName = gameManager.getTeamName(participant.getUniqueId());
        CaptureTheFlagMatch match = getMatch(teamName);
        if (match == null) {
            resetParticipant(participant);
            participants.remove(participant);
            return;
        }
        match.onParticipantQuit(participant);
        resetParticipant(participant);
        participants.remove(participant);
    }
    
    /**
     * Code to handle when an on-deck participant leaves
     * @param onDeckParticipant the on-deck participant
     */
    private void onOnDeckParticipantQuit(Player onDeckParticipant) {
        resetOnDeckParticipant(onDeckParticipant);
        onDeckParticipants.remove(onDeckParticipant);
    }
    
    /**
     * Tells the round that the given match is over. If all matches are over, stops the round. If not all matches are over, teleports the players who were in the passed-in match to the spawn observatory.
     * @param match The match that is over. Must be one of the matches in {@link CaptureTheFlagRound#matches}.
     */
    public void matchIsOver(CaptureTheFlagMatch match) {
        if (allMatchesAreOver()) {
            roundIsOver();
            return;
        }
        MatchPairing matchPairing = match.getMatchPairing();
        for (Player participant : participants) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            if (matchPairing.containsTeam(team)) {
                participant.teleport(config.getSpawnObservatory());
            }
        }
    }
    
    /**
     * Check if all the matches are over
     * @return True if all matches are over, false if even one match isn't over
     */
    private boolean allMatchesAreOver() {
        for (CaptureTheFlagMatch match : matches) {
            if (match.isActive()) {
                return false;
            }
        }
        return true;
    }
    
    private void startOnDeckClassSelectionTimer() {
        this.onDeckClassSelectionTimerTaskId = new BukkitRunnable() {
            private int count = config.getClassSelectionDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    startOnDeckMatchTimer();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                String timer = String.format("Class selection: %s", timeLeft);
                sidebar.updateLine("timer", timer);
                adminSidebar.updateLine("timer", timer);
                topbar.setMiddle(Component.text(timeLeft));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startOnDeckMatchTimer() {
        this.onDeckMatchTimerTaskId = new BukkitRunnable() {
            int count = config.getRoundTimerDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                String timer = String.format("Round: %s", timeLeft);
                sidebar.updateLine("timer", timer);
                adminSidebar.updateLine("timer", timer);
                topbar.setMiddle(Component.text(timeLeft));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    /**
     * An alternate version of {@link CaptureTheFlagRound#startMatchesStartingCountDown()}
     * reserved for the first round in a game, which uses the 
     */
    private void startDescriptionPeriod() {
        descriptionShowing = true;
        this.descriptionPeriodTaskId = new BukkitRunnable() {
            private int count = config.getDescriptionDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    sidebar.updateLine("timer", "");
                    adminSidebar.updateLine("timer", "");
                    topbar.setMiddle(Component.empty());
                    descriptionShowing = false;
                    startMatchesStartingCountDown();
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                String timerString = String.format("Starting soon: %s", timeLeft);
                sidebar.updateLine("timer", timerString);
                adminSidebar.updateLine("timer", timerString);
                topbar.setMiddle(Component.text(timeLeft));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startMatchesStartingCountDown() {
        this.matchesStartingCountDownTaskId = new BukkitRunnable() {
            int count = config.getMatchesStartingDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    startMatches();
                    matchesStarted = true;
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                String timer = String.format("Starting: %s", timeLeft);
                sidebar.updateLine("timer", timer);
                adminSidebar.updateLine("timer", timer);
                topbar.setMiddle(Component.text(timeLeft));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    /**
     * Starts the matches that are in this round
     */
    private void startMatches() {
        for (CaptureTheFlagMatch match : matches) {
            MatchPairing matchPairing = match.getMatchPairing();
            List<Player> northParticipants = getParticipantsOnTeam(matchPairing.northTeam());
            List<Player> southParticipants = getParticipantsOnTeam(matchPairing.southTeam());
            match.start(northParticipants, southParticipants);
        }
        startOnDeckClassSelectionTimer();
    }
    
    public void onPlayerDamage(Player participant, EntityDamageEvent event) {
        if (!roundActive) {
            return;
        }
        if (onDeckParticipants.contains(participant)) {
            event.setCancelled(true);
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        if (descriptionShowing) {
            event.setCancelled(true);
            return;
        }
        if (!matchesStarted) {
            event.setCancelled(true);
            return;
        }
        for (CaptureTheFlagMatch match : matches) {
            match.onPlayerDamage(participant, event);
        }
    }
    
    public void onPlayerLoseHunger(Player participant, FoodLevelChangeEvent event) {
        if (!roundActive) {
            return;
        }
        if (onDeckParticipants.contains(participant)) {
            participant.setFoodLevel(20);
            event.setCancelled(true);
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        if (descriptionShowing) {
            participant.setFoodLevel(20);
            event.setCancelled(true);
            return;
        }
        if (matchesStarted) {
            return;
        }
        participant.setFoodLevel(20);
        event.setCancelled(true);
    }
    
    public void onClickInventory(Player participant, InventoryClickEvent event) {
        if (!roundActive) {
            return;
        }
        if (onDeckParticipants.contains(participant)) {
            event.setCancelled(true);
            return;
        }
        if (!participants.contains(participant)) {
            return;
        }
        if (descriptionShowing) {
            event.setCancelled(true);
            return;
        }
        if (!matchesStarted) {
            event.setCancelled(true);
            return;
        }
        for (CaptureTheFlagMatch match : matches) {
            match.onClickInventory(participant, event);
        }
    }

    private List<Player> getParticipantsOnTeam(String teamName) {
        List<Player> onTeam = new ArrayList<>();
        for (Player participant : participants) {
            String participantTeam = gameManager.getTeamName(participant.getUniqueId());
            if (participantTeam.equals(teamName)) {
                onTeam.add(participant);
            }
        }
        return onTeam;
    }
    
    private void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(matchesStartingCountDownTaskId);
        Bukkit.getScheduler().cancelTask(onDeckClassSelectionTimerTaskId);
        Bukkit.getScheduler().cancelTask(onDeckMatchTimerTaskId);
        Bukkit.getScheduler().cancelTask(descriptionPeriodTaskId);
    }

    private void initializeSidebar() {
        for (Player participant : participants) {
            String teamId = gameManager.getTeamName(participant.getUniqueId());
            String enemyTeamId = getOppositeTeam(teamId);
            ChatColor enemyColor = gameManager.getTeamChatColor(enemyTeamId);
            String enemyDisplayName = gameManager.getTeamDisplayName(enemyTeamId);
            sidebar.updateLine(participant.getUniqueId(), "enemy", String.format("%svs: %s%s", ChatColor.BOLD, enemyColor, enemyDisplayName));
        }
        sidebar.updateLine("timer", "");
        adminSidebar.updateLine("timer", "");
        topbar.setMiddle(Component.empty());
        for (Player onDeckParticipant : onDeckParticipants) {
            sidebar.updateLine(onDeckParticipant.getUniqueId(), "enemy", "On Deck");
        }
    }
    
    /**
     * Messages all participants of the entire game, whether they are in this round or not
     * @param message The message to send
     */
    public void messageAllGameParticipants(Component message) {
        captureTheFlagGame.messageAllParticipants(message);
    }
    
    /**
     * Checks if the participant is alive and in a match
     * @param participant The participant
     * @return True if the participant is alive and in a match, false otherwise
     */
    public boolean isAliveInMatch(Player participant) {
        for (CaptureTheFlagMatch match : matches) {
            if (match.isActive()) {
                if (match.isAliveInMatch(participant)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the opposite team of the given teamName, if the teamName is contained in one of this round's
     * {@link CaptureTheFlagRound#matches}.
     * @param teamName The teamName to check for
     * @return The opposite team in the {@link CaptureTheFlagMatch} which contains the given teamName. Null if
     * the given teamName is not contained in any of the matches for this round.
     */
    public @Nullable String getOppositeTeam(@NotNull String teamName) {
        for (CaptureTheFlagMatch match : matches) {
            String oppositeTeam = match.getMatchPairing().oppositeTeam(teamName);
            if (oppositeTeam != null) {
                return oppositeTeam;
            }
        }
        return null;
    }
    
    /**
     * Get the match that the team is in.
     * @param teamName The team to find the match for. 
     * @return The match that the team is in. Null if the given team is not in a match.
     */
    private @Nullable CaptureTheFlagMatch getMatch(@NotNull String teamName) {
        for (CaptureTheFlagMatch match : matches) {
            if (match.getMatchPairing().containsTeam(teamName)) {
                return match;
            }
        }
        return null;
    }
    
    /**
     * @return a copy of this round's matches.
     */
    public @NotNull List<CaptureTheFlagMatch> getMatches() {
        return new ArrayList<>(matches);
    }
    
    // Test methods
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (CaptureTheFlagMatch match : matches) {
            sb.append(match);
            sb.append(",");
        }
        return sb.toString();
    }
    
    /**
     * @return a copy of the list of participants
     */
    public @NotNull List<Player> getParticipants() {
        return new ArrayList<>(participants);
    }
    
    /**
     * @return a copy of the list of on-deck participants
     */
    public @NotNull List<Player> getOnDeckParticipants() {
        return new ArrayList<>(onDeckParticipants);
    }
    
    /**
     * @param teamName The team name to check for
     * @return true if the teamName is in one of this round's matches, false otherwise
     */
    public boolean containsTeam(String teamName) {
        for (CaptureTheFlagMatch match : matches) {
            if (match.getMatchPairing().containsTeam(teamName)) {
                return true;
            }
        }
        return false;
    }
    
    public void setFirstRound(boolean firstRound) {
        this.firstRound = firstRound;
    }
    
}

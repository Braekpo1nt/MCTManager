package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.*;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.participant.TeamData;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoundActiveState implements CaptureTheFlagState {
    
    private final CaptureTheFlagGame context;
    private final RoundManager roundManger;
    private final Map<MatchPairing, CaptureTheFlagMatch> matches;
    private final Timer classSelectionTimer;
    private int numOfEndedMatches = 0;
    private Timer roundTimer;
    
    public RoundActiveState(CaptureTheFlagGame context) {
        this.context = context;
        this.roundManger = context.getRoundManager();
        
        List<MatchPairing> currentRound = roundManger.getCurrentRound();
        matches = new HashMap<>(currentRound.size());
        Map<MatchPairing, List<CTFParticipant>> matchParticipants = new HashMap<>();
        for (int i = 0; i < currentRound.size(); i++) {
            MatchPairing matchPairing = currentRound.get(i);
            CaptureTheFlagMatch match = new CaptureTheFlagMatch(
                    context, 
                    this::matchIsOver, 
                    matchPairing, 
                    context.getConfig().getArenas().get(i));
            matches.put(matchPairing, match);
            matchParticipants.put(matchPairing, new ArrayList<>());
        }
        
        
        for (CTFParticipant participant : context.getParticipants().values()) {
            MatchPairing matchPairing = RoundManager.getMatchPairing(participant.getTeamId(), currentRound);
            if (matchPairing == null) {
                initializeOnDeckParticipant(participant);
            } else {
                matchParticipants.get(matchPairing).add(participant);
            }
        }
        
        for (MatchPairing matchPairing : currentRound) {
            CaptureTheFlagMatch match = matches.get(matchPairing);
            List<CTFParticipant> newParticipants = matchParticipants.get(matchPairing);
            CTFTeam northTeam;
            if (context.getTeams().containsKey(matchPairing.northTeam())) {
                northTeam = context.getTeams().get(matchPairing.northTeam());
            } else {
                northTeam = context.getQuitTeams().get(matchPairing.northTeam());
            }
            CTFTeam southTeam;
            if (context.getTeams().containsKey(matchPairing.southTeam())) {
                southTeam = context.getTeams().get(matchPairing.southTeam());
            } else {
                southTeam = context.getQuitTeams().get(matchPairing.southTeam());
            }
            match.start(northTeam, southTeam, newParticipants);
        }
        
        classSelectionTimer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getClassSelectionDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .topbarPrefix(Component.text("Class selection: "))
                .sidebarPrefix(Component.text("Class selection: "))
                .titleAudience(Audience.audience(context.getParticipants().values()))
                .onCompletion(() -> {
                    startRoundTimer();
                    for (CaptureTheFlagMatch match : matches.values()) {
                        match.nextState();
                    }
                })
                .build());
    }
    
    private void startRoundTimer() {
        roundTimer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundTimerDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .sidebarPrefix(Component.text("Round: "))
                .onCompletion(() -> {
                    for (CaptureTheFlagMatch match : matches.values()) {
                        match.nextState();
                    }
                })
                .build());
    }
    
    private void initializeOnDeckParticipant(Participant participant) {
        participant.teleport(context.getConfig().getSpawnObservatory());
        participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
        ParticipantInitializer.clearInventory(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    /**
     * Called by each match when it ends, so that we know each one has ended
     */
    public void matchIsOver() {
        numOfEndedMatches++;
        if (numOfEndedMatches >= matches.size()) {
            roundIsOver();
        }
    }
    
    /**
     * Called when all matches are over and the round is over
     */
    private void roundIsOver() {
        stop();
        if (context.getRoundManager().hasNextRound()) {
            context.setState(new RoundOverState(context));
        } else {
            context.setState(new GameOverState(context));
        }
    }
    
    @Override
    public void stop() {
        cancelAllTasks();
        for (CaptureTheFlagMatch match : matches.values()) {
            match.stop();
        }
        matches.clear();
        for (Participant participant : context.getParticipants().values()) {
            resetParticipant(participant);
        }
    }
    
    private void cancelAllTasks() {
        if (classSelectionTimer != null) {
            classSelectionTimer.cancel();
        }
        if (roundTimer != null) {
            roundTimer.cancel();
        }
    }
    
    @Override
    public void onParticipantJoin(Participant participant, Team team) {
        context.onTeamJoin(team);
        CTFParticipant.QuitData quitData = context.getQuitDatas().remove(participant.getUniqueId());
        if (quitData == null) {
            context.initializeParticipant(participant);
        } else {
            context.initializeParticipant(participant, quitData.getKills(), quitData.getDeaths(), quitData.getScore());
        }
        participant.setGameMode(GameMode.ADVENTURE);
        participant.teleport(context.getConfig().getSpawnObservatory());
        participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
        Team ctfTeam = context.getTeams().get(participant.getTeamId());
        CaptureTheFlagMatch match = getMatch(ctfTeam.getTeamId());
        if (match == null) {
            initializeOnDeckParticipant(participant);
            participant.sendMessage(Component.empty()
                    .append(ctfTeam.getFormattedDisplayName())
                    .append(Component.text(" is on-deck this round."))
                    .color(NamedTextColor.YELLOW));
            Component roundDisplay = Component.empty()
                    .append(Component.text("Round "))
                    .append(Component.text(context.getRoundManager().getPlayedRounds() + 1))
                    .append(Component.text(":"));
            participant.showTitle(UIUtils.defaultTitle(
                    roundDisplay,
                    Component.empty()
                            .append(ctfTeam.getFormattedDisplayName())
                            .append(Component.text(" is on-deck"))));
        } else {
            CTFParticipant ctfParticipant = context.getParticipants().get(participant.getUniqueId());
            match.onParticipantJoin(ctfParticipant);
        }
    }
    
    public @Nullable CaptureTheFlagMatch getMatch(String teamId) {
        List<MatchPairing> currentRound = roundManger.getCurrentRound();
        MatchPairing matchPairing = RoundManager.getMatchPairing(teamId, currentRound);
        return matches.get(matchPairing);
    }
    
    @Override
    public void onParticipantQuit(CTFParticipant participant) {
        CaptureTheFlagMatch match = getMatch(participant.getTeamId());
        if (match == null) {
            participant.setGameMode(GameMode.ADVENTURE);
        } else {
            match.onParticipantQuit(participant);
        }
        context.getQuitDatas().put(participant.getUniqueId(), participant.getQuitData());
        context.resetParticipant(participant);
        context.getParticipants().remove(participant.getUniqueId());
        context.onTeamQuit(context.getTeams().get(participant.getTeamId()));
    }
    
    public void resetParticipant(Participant participant) {
        ParticipantInitializer.clearInventory(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        Participant participant = context.getParticipants().get(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        CaptureTheFlagMatch match = getMatch(participant.getTeamId());
        if (match == null) {
            Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "CTF.RoundActiveState.onPlayerDamage() -> isOnDeck cancelled");
            event.setCancelled(true);
        } else {
            match.onPlayerDamage(event);
        }
    }
    
    @Override
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        Participant participant = context.getParticipants().get(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        CaptureTheFlagMatch match = getMatch(participant.getTeamId());
        if (match == null) {
            event.setCancelled(true);
        } else {
            match.onPlayerLoseHunger(event);
        }
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        Participant participant = context.getParticipants().get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        CaptureTheFlagMatch match = getMatch(participant.getTeamId());
        if (match != null) {
            match.onPlayerMove(event);
        }
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event) {
        Participant participant = context.getParticipants().get(event.getWhoClicked().getUniqueId());
        if (participant == null) {
            return;
        }
        CaptureTheFlagMatch match = getMatch(participant.getTeamId());
        if (match != null) {
            match.onClickInventory(event);
        }
    }
    
    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        Participant participant = context.getParticipants().get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        CaptureTheFlagMatch match = getMatch(participant.getTeamId());
        if (match != null) {
            match.onPlayerDeath(event);
        }
    }
}

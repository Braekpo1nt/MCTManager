package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFTeam;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.*;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RoundActiveState extends CaptureTheFlagStateBase {
    
    private final RoundManager roundManger;
    private final Map<MatchPairing, CaptureTheFlagMatch> matches;
    private @Nullable Timer classSelectionTimer;
    private int numOfEndedMatches = 0;
    private @Nullable Timer roundTimer;
    
    public RoundActiveState(CaptureTheFlagGame context) {
        super(context);
        this.roundManger = context.getRoundManager();
        this.matches = new HashMap<>(roundManger.getCurrentRound().size());
    }
    
    @Override
    public void enter() {
        
        List<MatchPairing> currentRound = roundManger.getCurrentRound();
        Map<MatchPairing, List<CTFParticipant>> matchParticipants = currentRound.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        pairing -> new ArrayList<>()
                ));
        
        for (CTFParticipant participant : context.getParticipants().values()) {
            MatchPairing matchPairing = RoundManager.getMatchPairing(participant.getTeamId(), currentRound);
            if (matchPairing == null) {
                initializeOnDeckParticipant(participant);
            } else {
                matchParticipants.get(matchPairing).add(participant);
            }
        }
        
        for (int i = 0; i < currentRound.size(); i++) {
            MatchPairing matchPairing = currentRound.get(i);
            List<CTFParticipant> newParticipants = matchParticipants.get(matchPairing);
            CTFTeam northTeam = context.getTeamOrQuitTeam(matchPairing.northTeam());
            CTFTeam southTeam = context.getTeamOrQuitTeam(matchPairing.southTeam());
            CaptureTheFlagMatch match = new CaptureTheFlagMatch(
                    context,
                    this::matchIsOver,
                    matchPairing,
                    context.getConfig().getArenas().get(i),
                    northTeam,
                    southTeam,
                    newParticipants);
            matches.put(matchPairing, match);
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
    
    @Override
    public void exit() {
        Timer.cancel(classSelectionTimer);
        Timer.cancel(roundTimer);
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
        cleanup();
        if (context.getRoundManager().hasNextRound()) {
            context.setState(new RoundOverState(context));
        } else {
            context.setState(new GameOverState(context));
        }
    }
    
    @Override
    public void cleanup() {
        Timer.cancel(classSelectionTimer);
        Timer.cancel(roundTimer);
        for (CaptureTheFlagMatch match : matches.values()) {
            match.cleanup();
        }
        matches.clear();
    }
    
    @Override
    public void onTeamRejoin(CTFTeam team) {
        CaptureTheFlagMatch match = getMatch(team.getTeamId());
        if (match == null) {
            return;
        }
        match.onTeamRejoin(team);
    }
    
    @Override
    public void onNewTeamJoin(CTFTeam team) {
        CaptureTheFlagMatch match = getMatch(team.getTeamId());
        if (match == null) {
            return;
        }
        match.onNewTeamJoin(team);
    }
    
    @Override
    public void onParticipantRejoin(CTFParticipant participant, CTFTeam team) {
        super.onParticipantRejoin(participant, team);
        CaptureTheFlagMatch match = getMatch(team.getTeamId());
        if (match == null) {
            joinOnDeckParticipant(participant, team);
        } else {
            match.onParticipantRejoin(participant, team);
        }
    }
    
    @Override
    public void onNewParticipantJoin(CTFParticipant participant, CTFTeam team) {
        super.onNewParticipantJoin(participant, team);
        CaptureTheFlagMatch match = getMatch(team.getTeamId());
        if (match == null) {
            joinOnDeckParticipant(participant, team);
        } else {
            match.onNewParticipantJoin(participant, team);
        }
    }
    
    private void joinOnDeckParticipant(CTFParticipant participant, CTFTeam team) {
        initializeOnDeckParticipant(participant);
        participant.sendMessage(Component.empty()
                .append(team.getFormattedDisplayName())
                .append(Component.text(" is on-deck this round."))
                .color(NamedTextColor.YELLOW));
        Component roundDisplay = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(context.getRoundManager().getPlayedRounds() + 1))
                .append(Component.text(":"));
        participant.showTitle(UIUtils.defaultTitle(
                roundDisplay,
                Component.empty()
                        .append(team.getFormattedDisplayName())
                        .append(Component.text(" is on-deck"))));
    }
    
    public @Nullable CaptureTheFlagMatch getMatch(String teamId) {
        // TODO: create a faster way of getting the current matchPairing of a given teamId
        List<MatchPairing> currentRound = roundManger.getCurrentRound();
        MatchPairing matchPairing = RoundManager.getMatchPairing(teamId, currentRound);
        return matches.get(matchPairing);
    }
    
    @Override
    public void onParticipantQuit(CTFParticipant participant, CTFTeam team) {
        CaptureTheFlagMatch match = getMatch(team.getTeamId());
        if (match == null) {
            return;
        }
        match.onParticipantQuit(participant, team);
    }
    
    @Override
    public void onTeamQuit(CTFTeam team) {
        CaptureTheFlagMatch match = getMatch(team.getTeamId());
        if (match == null) {
            return;
        }
        match.onTeamQuit(team);
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull CTFParticipant participant) {
        CaptureTheFlagMatch match = getMatch(participant.getTeamId());
        if (match == null) {
            return;
        }
        match.onParticipantMove(event, participant);
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull CTFParticipant participant) {
        CaptureTheFlagMatch match = getMatch(participant.getTeamId());
        if (match == null) {
            return;
        }
        match.onParticipantTeleport(event, participant);
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull CTFParticipant participant) {
        CaptureTheFlagMatch match = getMatch(participant.getTeamId());
        if (match == null) {
            return;
        }
        match.onParticipantInteract(event, participant);
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull CTFParticipant participant) {
        CaptureTheFlagMatch match = getMatch(participant.getTeamId());
        if (match == null) {
            Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "CTF.RoundActiveState.onPlayerDamage() -> isOnDeck cancelled");
            event.setCancelled(true);
        } else {
            match.onParticipantDamage(event, participant);
        }
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull CTFParticipant participant) {
        CaptureTheFlagMatch match = getMatch(participant.getTeamId());
        if (match != null) {
            match.onParticipantDeath(event, participant);
        }
    }
    
    @Override
    public void onParticipantRespawn(PlayerRespawnEvent event, CTFParticipant participant) {
        CaptureTheFlagMatch match = getMatch(participant.getTeamId());
        if (match == null) {
            return;
        }
        match.onParticipantRespawn(event, participant);
    }
    
    @Override
    public void onParticipantPostRespawn(PlayerPostRespawnEvent event, CTFParticipant participant) {
        CaptureTheFlagMatch match = getMatch(participant.getTeamId());
        if (match == null) {
            return;
        }
        match.onParticipantPostRespawn(event, participant);
    }
    
    @Override
    public void onParticipantFoodLevelChange(@NotNull FoodLevelChangeEvent event, @NotNull CTFParticipant participant) {
        CaptureTheFlagMatch match = getMatch(participant.getTeamId());
        if (match == null) {
            event.setCancelled(true);
        } else {
            match.onParticipantFoodLevelChange(event, participant);
        }
    }
}

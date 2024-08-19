package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.MatchPairing;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.RoundManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoundActiveState implements CaptureTheFlagState {
    
    private final CaptureTheFlagGame context;
    private final GameManager gameManager;
    private final RoundManager roundManger;
    private final List<Player> onDeckParticipants = new ArrayList<>();
    private final Map<MatchPairing, CaptureTheFlagMatch> matches;
    private int numOfEndedMatches = 0;
    
    public RoundActiveState(CaptureTheFlagGame context) {
        this.context = context;
        this.gameManager = context.getGameManager();
        this.roundManger = context.getRoundManager();
        
        List<MatchPairing> currentRound = roundManger.getCurrentRound();
        matches = new HashMap<>(currentRound.size());
        Map<MatchPairing, List<Player>> matchParticipants = new HashMap<>();
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
        
        
        for (Player participant : context.getParticipants()) {
            String teamId = gameManager.getTeamName(participant.getUniqueId());
            MatchPairing matchPairing = RoundManager.getMatchPairing(teamId, currentRound);
            if (matchPairing == null) {
                initializeOnDeckParticipant(participant);
            } else {
                matchParticipants.get(matchPairing).add(participant);
            }
        }
        
        for (MatchPairing matchPairing : currentRound) {
            CaptureTheFlagMatch match = matches.get(matchPairing);
            List<Player> newParticipants = matchParticipants.get(matchPairing);
            match.start(newParticipants);
        }
    }
    
    private void initializeOnDeckParticipant(Player participant) {
        onDeckParticipants.add(participant);
        participant.teleport(context.getConfig().getSpawnObservatory());
        participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    /**
     * Called by each match when it ends, so that we know each one has ended
     * @param endedMatch the match that ended
     */
    //TODO: remove this parameter if not used
    public void matchIsOver(CaptureTheFlagMatch endedMatch) {
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
        context.setState(new RoundOverState(context));
    }
    
    @Override
    public void stop() {
        cancelAllTasks();
        for (CaptureTheFlagMatch match : matches.values()) {
            match.stop();
        }
        matches.clear();
        for (Player participant : context.getParticipants()) {
            resetParticipant(participant);
        }
        onDeckParticipants.clear();
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        context.initializeParticipant(participant);
        context.getSidebar().updateLine(participant.getUniqueId(), "title", context.getTitle());
        Component roundLine = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(context.getRoundManager().getCurrentRoundIndex() + 1))
                .append(Component.text("/"))
                .append(Component.text(context.getRoundManager().getMaxRounds()))
                ;
        context.getSidebar().updateLine("round", roundLine);
        context.getAdminSidebar().updateLine("round", roundLine);
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        CaptureTheFlagMatch match = getMatch(teamId);
        if (match == null) {
            Component teamDisplayName = gameManager.getFormattedTeamDisplayName(teamId);
            initializeOnDeckParticipant(participant);
            participant.sendMessage(Component.empty()
                    .append(teamDisplayName)
                    .append(Component.text(" is on-deck this round."))
                    .color(NamedTextColor.YELLOW));
        } else {
            participant.setGameMode(GameMode.ADVENTURE);
            participant.teleport(context.getConfig().getSpawnObservatory());
            participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
            match.onParticipantJoin(participant);
        }
    }
    
    public @Nullable CaptureTheFlagMatch getMatch(String teamId) {
        List<MatchPairing> currentRound = roundManger.getCurrentRound();
        MatchPairing matchPairing = RoundManager.getMatchPairing(teamId, currentRound);
        return matches.get(matchPairing);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        CaptureTheFlagMatch match = getMatch(teamId);
        if (match == null) {
            resetOnDeckParticipant(participant);
            onDeckParticipants.remove(participant);
            return;
        }
        match.onParticipantQuit(participant);
        context.resetParticipant(participant);
        context.getParticipants().remove(participant);
    }
    
    private void resetOnDeckParticipant(Player participant) {
        participant.setGameMode(GameMode.ADVENTURE);
        context.resetParticipant(participant);
    }
    
    public void resetParticipant(Player participant) {
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        CaptureTheFlagMatch match = getMatch(teamId);
        if (match == null) {
            Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "CTF.RoundActiveState.onPlayerDamage() -> isOnDeck cancelled");
            event.setCancelled(true);
        } else {
            match.onPlayerDamage(event);
        }
    }
    
    @Override
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        CaptureTheFlagMatch match = getMatch(teamId);
        if (match == null) {
            event.setCancelled(true);
        } else {
            match.onPlayerLoseHunger(event);
        }
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        Player participant = event.getPlayer();
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        CaptureTheFlagMatch match = getMatch(teamId);
        if (match != null) {
            match.onPlayerMove(event);
        }
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event) {
        
    }
}

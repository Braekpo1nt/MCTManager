package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.MatchPairing;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.RoundManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

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
                onDeckParticipants.add(participant);
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
    
    /**
     * Called by each match when it ends, so that we know each one has ended
     * @param endedMatch the match that ended
     */
    public void matchIsOver(CaptureTheFlagMatch endedMatch) {
        numOfEndedMatches++;
        if (numOfEndedMatches >= matches.size()) {
            allMatchesAreOver();
        }
    }
    
    private void allMatchesAreOver() {
        context.setState(new RoundOverState(context));
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    @Override
    public void initializeParticipant(Player participant) {
        
    }
    
    @Override
    public void resetParticipant(Player participant) {
        
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        
    }
    
    @Override
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event) {
        
    }
}

package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class MatchActiveState implements CaptureTheFlagMatchState {
    
    private final CaptureTheFlagMatch context;
    
    public MatchActiveState(CaptureTheFlagMatch context) {
        this.context = context;
        for (Player participant : context.getAllParticipants()) {
            participant.closeInventory();
        }
        context.messageAllParticipants(Component.text("Begin!"));
        context.openGlassBarriers();
    }
    
    @Override
    public void nextState() {
        onBothTeamsLose(Component.text("Time ran out."));
    }
    
    private void onBothTeamsLose(Component reason) {
        context.messageAllParticipants(Component.empty()
                .append(Component.text("Game over. "))
                .append(reason));
        context.setState(new MatchOverState(context));
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        context.initializeParticipant(participant);
        String teamId = context.getGameManager().getTeamName(participant.getUniqueId());
        context.getTopbar().linkToTeam(participant.getUniqueId(), teamId);
        context.getParticipantsAreAlive().put(participant.getUniqueId(), false);
        participant.teleport(context.getConfig().getSpawnObservatory());
        participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
        Location lookLocation;
        if (context.getMatchPairing().northTeam().equals(teamId)) {
            lookLocation = context.getArena().northFlag();
        } else {
            lookLocation = context.getArena().southFlag();
        }
        participant.lookAt(lookLocation.getX(), lookLocation.getY(), lookLocation.getZ(), LookAnchor.EYES);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
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
    
    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        
    }
}

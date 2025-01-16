package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import io.papermc.paper.entity.LookAnchor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class MatchOverState implements CaptureTheFlagMatchState {
    
    private final CaptureTheFlagMatch context;
    
    public MatchOverState(CaptureTheFlagMatch context) {
        this.context = context;
        for (Participant participant : context.getAllParticipants().values()) {
            if (context.getParticipantsAreAlive().get(participant.getUniqueId())) {
                participant.teleport(context.getConfig().getSpawnObservatory());
                participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
                ParticipantInitializer.clearInventory(participant);
                participant.closeInventory();
                ParticipantInitializer.resetHealthAndHunger(participant);
                ParticipantInitializer.clearStatusEffects(participant);
            }
        }
        context.getMatchIsOver().run();
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        context.getParticipantsAreAlive().put(participant.getUniqueId(), false);
        context.initializeParticipant(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        context.getTopbar().linkToTeam(participant.getUniqueId(), participant.getTeamId());
        participant.teleport(context.getConfig().getSpawnObservatory());
        participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
        Location lookLocation;
        if (context.getMatchPairing().northTeam().equals(participant.getTeamId())) {
            lookLocation = context.getArena().northFlag();
        } else {
            lookLocation = context.getArena().southFlag();
        }
        participant.lookAt(lookLocation.getX(), lookLocation.getY(), lookLocation.getZ(), LookAnchor.EYES);
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        context.resetParticipant(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        if (context.getMatchPairing().northTeam().equals(participant.getTeamId())) {
            context.getNorthParticipants().remove(participant.getUniqueId());
        } else if (context.getMatchPairing().southTeam().equals(participant.getTeamId())) {
            context.getSouthParticipants().remove(participant.getUniqueId());
        }
        context.getAllParticipants().remove(participant.getUniqueId());
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "CaptureTheFlagMatch.MatchOverState.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        // do nothing
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event) {
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        // do nothing
    }
    
    @Override
    public void nextState() {
        // do nothing
    }
}

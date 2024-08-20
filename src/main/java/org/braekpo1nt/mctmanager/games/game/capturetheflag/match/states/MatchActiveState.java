package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collections;

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
        String teamId = context.getGameManager().getTeamName(participant.getUniqueId());
        if (context.getMatchPairing().northTeam().equals(teamId)) {
            onNorthParticipantQuit(participant);
        } else {
            onSouthParticipantQuit(participant);
        }
    }
    
    private void onNorthParticipantQuit(Player participant) {
        if (context.getParticipantsAreAlive().get(participant.getUniqueId())) {
            Component deathMessage = Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" left early. Their life is forfeit."));
            PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant,
                    DamageSource.builder(DamageType.GENERIC).build(), Collections.emptyList(), 0, deathMessage);
            this.onPlayerDeath(fakeDeathEvent);
        }
        context.resetParticipant(participant);
        context.getNorthParticipants().remove(participant);
        context.getAllParticipants().remove(participant);
    }
    
    private void onSouthParticipantQuit(Player participant) {
        if (context.getParticipantsAreAlive().get(participant.getUniqueId())) {
            Component deathMessage = Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" left early. Their life is forfeit."));
            PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant,
                    DamageSource.builder(DamageType.GENERIC).build(), Collections.emptyList(), 0, deathMessage);
            this.onPlayerDeath(fakeDeathEvent);
        }
        context.resetParticipant(participant);
        context.getSouthParticipants().remove(participant);
        context.getAllParticipants().remove(participant);
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        // do nothing
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

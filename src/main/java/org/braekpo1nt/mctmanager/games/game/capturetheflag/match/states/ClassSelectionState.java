package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.ClassPicker;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class ClassSelectionState implements CaptureTheFlagMatchState {
    
    private final CaptureTheFlagMatch context;
    private final ClassPicker northClassPicker;
    private final ClassPicker southClassPicker;
    
    public ClassSelectionState(CaptureTheFlagMatch context) {
        this.context = context;
        this.northClassPicker = context.getNorthClassPicker();
        this.southClassPicker = context.getSouthClassPicker();
        northClassPicker.start(
                context.getPlugin(), 
                Participant.toPlayersList(context.getNorthParticipants().values()), 
                context.getConfig().getLoadouts());
        southClassPicker.start(
                context.getPlugin(),
                Participant.toPlayersList(context.getSouthParticipants().values()), 
                context.getConfig().getLoadouts());
    }
    
    @Override
    public void nextState() {
        northClassPicker.stop(true);
        southClassPicker.stop(true);
        context.setState(new MatchActiveState(context));
    }
    
    @Override
    public void stop() {
        northClassPicker.stop(false);
        southClassPicker.stop(false);
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        context.initializeParticipant(participant);
        String teamId = context.getGameManager().getTeamId(participant.getUniqueId());
        context.getTopbar().linkToTeam(participant.getUniqueId(), teamId);
        if (context.getMatchPairing().northTeam().equals(teamId)) {
            northClassPicker.addTeamMate(participant.getPlayer());
        } else {
            southClassPicker.addTeamMate(participant.getPlayer());
        }
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        context.resetParticipant(participant);
        context.getAllParticipants().remove(participant.getUniqueId());
        String teamId = context.getGameManager().getTeamId(participant.getUniqueId());
        int alive;
        int dead;
        if (context.getMatchPairing().northTeam().equals(teamId)) {
            context.getNorthParticipants().remove(participant.getUniqueId());
            context.getNorthClassPicker().removeTeamMate(participant.getPlayer());
            alive = context.countAlive(context.getNorthParticipants().values());
            dead = context.getNorthParticipants().size() - alive;
        } else {
            context.getSouthParticipants().remove(participant.getUniqueId());
            context.getSouthClassPicker().removeTeamMate(participant.getPlayer());
            alive = context.countAlive(context.getSouthParticipants().values());
            dead = context.getSouthParticipants().size() - alive;
        }
        context.getTopbar().setMembers(teamId, alive, dead);
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "CaptureTheFlagMatch.ClassSelectionState.onPlayerDamage() cancelled");
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
        // do nothing
    }
    
    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        // do nothing
    }
}

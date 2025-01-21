package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

public class DescriptionState implements CaptureTheFlagState {
    
    private final CaptureTheFlagGame context;
    
    public DescriptionState(CaptureTheFlagGame context) {
        this.context = context;
        startTimer();
    }
    
    protected void startTimer() {
        context.messageAllParticipants(context.getConfig().getDescription());
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withTopbar(context.getTopbar())
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .topbarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> {
                    context.setState(new PreRoundState(context));
                })
                .build());
    }
    
    @Override
    public void onTeamJoin(Team team) {
        if (!context.getTeams().containsKey(team.getTeamId())) {
            context.getRoundManager().regenerateRounds(Team.toTeamIds(context.getTeams()), 
                    context.getConfig().getArenas().size());
        }
        context.updateRoundLine();
        context.getTeams().put(team.getTeamId(), team);
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        initializeParticipant(participant);
    }
    
    private void initializeParticipant(Participant participant) {
        context.initializeParticipant(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.teleport(context.getConfig().getSpawnObservatory());
        participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        context.resetParticipant(participant);
        context.getParticipants().remove(participant.getUniqueId());
        List<String> teamIds = Participant.getTeamIds(context.getParticipants().values());
        if (!teamIds.contains(participant.getTeamId())) {
            context.getRoundManager().regenerateRounds(teamIds, context.getConfig().getArenas().size());
            context.updateRoundLine();
        }
    }
    
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "CaptureTheFlagGame.DescriptionState.onPlayerDamage() cancelled");
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
}

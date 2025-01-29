package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.participant.TeamData;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class RoundOverState implements CaptureTheFlagState {
    
    private final CaptureTheFlagGame context;
    
    public RoundOverState(CaptureTheFlagGame context) {
        this.context = context;
        Audience.audience(context.getParticipants().values()).showTitle(UIUtils.roundOverTitle());
        Main.logger().info("Starting RoundOverState timer");
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundOverDuration())
                .withTopbar(context.getTopbar())
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Round Over: "))
                .onCompletion(() -> {
                    context.getRoundManager().nextRound();
                    context.setState(new PreRoundState(context));
                })
                .build());
    }
    
    @Override
    public void onTeamJoin(Team team) {
        context.getTeams().put(team.getTeamId(), new TeamData<>(team));
        context.getRoundManager().regenerateRounds(Team.getTeamIds(context.getTeams()),
                context.getConfig().getArenas().size());
        context.updateRoundLine();
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        context.initializeParticipant(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.teleport(context.getConfig().getSpawnObservatory());
        participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        context.resetParticipant(participant);
        context.getParticipants().remove(participant.getUniqueId());
    }
    
    @Override
    public void onTeamQuit(Team team) {
        context.getRoundManager().regenerateRounds(Team.getTeamIds(context.getTeams()), context.getConfig().getArenas().size());
        context.updateRoundLine();
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "CaptureTheFlagGame.RoundOverState.onPlayerDamage() cancelled");
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

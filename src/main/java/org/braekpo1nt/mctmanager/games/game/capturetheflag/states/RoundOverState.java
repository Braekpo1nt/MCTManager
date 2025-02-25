package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFQuitData;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
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
    public void onParticipantJoin(Participant participant, Team team) {
        context.onTeamJoin(team);
        CTFQuitData quitData = context.getQuitDatas().remove(participant.getUniqueId());
        if (quitData == null) {
            context.initializeParticipant(participant);
        } else {
            context.initializeParticipant(participant, quitData.getKills(), quitData.getDeaths());
        }
        participant.setGameMode(GameMode.ADVENTURE);
        participant.teleport(context.getConfig().getSpawnObservatory());
        participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
    }
    
    @Override
    public void onParticipantQuit(CTFParticipant participant) {
        context.resetParticipant(participant);
        context.getParticipants().remove(participant.getUniqueId());
        context.getQuitDatas().put(participant.getUniqueId(), participant.getQuitData());
        context.onTeamQuit(context.getTeams().get(participant.getTeamId()));
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

package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

public class RoundOverState implements CaptureTheFlagState {
    
    private final CaptureTheFlagGame context;
    
    public RoundOverState(CaptureTheFlagGame context) {
        this.context = context;
        Audience.audience(context.getParticipants()).showTitle(UIUtils.roundOverTitle());
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
    public void onParticipantJoin(Player participant) {
        context.initializeParticipant(participant);
        String teamId = context.getGameManager().getTeamId(participant.getUniqueId());
        if (!context.getRoundManager().containsTeamId(teamId)) {
            List<String> teamIds = context.getGameManager().getTeamIds(context.getParticipants());
            context.getRoundManager().regenerateRounds(teamIds, context.getConfig().getArenas().size());
        }
        context.updateRoundLine();
        participant.setGameMode(GameMode.ADVENTURE);
        participant.teleport(context.getConfig().getSpawnObservatory());
        participant.setRespawnLocation(context.getConfig().getSpawnObservatory(), true);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        context.resetParticipant(participant);
        context.getParticipants().remove(participant);
        String quitTeamId = context.getGameManager().getTeamId(participant.getUniqueId());
        List<String> teamIds = context.getGameManager().getTeamIds(context.getParticipants());
        if (!teamIds.contains(quitTeamId)) {
            context.getRoundManager().regenerateRounds(teamIds, context.getConfig().getArenas().size());
            context.updateRoundLine();
        }
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

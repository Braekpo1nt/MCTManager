package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFTeam;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class RoundOverState extends CaptureTheFlagStateBase {
    
    public RoundOverState(CaptureTheFlagGame context) {
        super(context);
        Audience.audience(context.getParticipants().values()).showTitle(UIUtils.roundOverTitle());
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
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull CTFParticipant participant) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "RoundOverState.onParticipantDamage() cancelled");
        event.setCancelled(true);
    }
}

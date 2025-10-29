package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DescriptionState extends CaptureTheFlagStateBase {
    
    private @Nullable Timer timer;
    
    public DescriptionState(CaptureTheFlagGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        context.messageAllParticipants(context.getConfig().getDescription());
        timer = context.getTimerManager().start(Timer.builder()
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
    public void exit() {
        Timer.cancel(timer);
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull CTFParticipant participant) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "DescriptionState.onParticipantDamage() cancelled");
        event.setCancelled(true);
    }
}

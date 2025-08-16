package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DescriptionState extends ColossalCombatStateBase {
    
    private @Nullable Timer timer;
    
    public DescriptionState(@NotNull ColossalCombatGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        context.closeGates();
        context.messageAllParticipants(context.getConfig().getDescription());
        timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withTopbar(context.getTopbar())
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> {
                    context.setState(new PreRoundState(context));
                })
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
}

package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class FightingState extends GameplayState {
    
    private final Timer suddenDeathTimer;
    
    public FightingState(@NotNull ColossalCombatGame context) {
        super(context);
        suddenDeathTimer = context.getTimerManager().start(Timer.builder()
                .duration(config.getCaptureTheFlagDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Sudden Death: "))
                .onCompletion(() -> context.setState(new SuddenDeathState(context)))
                .build());
    }
    
    @Override
    public void cleanup() {
        suddenDeathTimer.cancel();
    }
}

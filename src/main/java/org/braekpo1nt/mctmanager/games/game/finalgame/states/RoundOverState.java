package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoundOverState extends FinalStateBase {
    
    private @Nullable Timer timer;
    
    public RoundOverState(@NotNull FinalGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        for (FinalParticipant participant : context.getParticipants().values()) {
            if (!participant.getAffiliation().equals(Affiliation.SPECTATOR)) {
                participant.setGameMode(GameMode.SPECTATOR);
                ParticipantInitializer.clearInventory(participant);
                ParticipantInitializer.clearStatusEffects(participant);
                ParticipantInitializer.resetHealthAndHunger(participant);
                participant.setAlive(true);
            }
            context.titleAllParticipants(UIUtils.roundOverTitle());
            timer = context.getTimerManager().start(Timer.builder()
                    .duration(context.getConfig().getRoundOverDuration())
                    .withTopbar(context.getTopbar())
                    .withSidebar(context.getAdminSidebar(), "timer")
                    .sidebarPrefix(Component.text("Round Over: "))
                    .onCompletion(() -> {
                        context.setState(new PreRoundState(context));
                    })
                    .build());
        }
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
}

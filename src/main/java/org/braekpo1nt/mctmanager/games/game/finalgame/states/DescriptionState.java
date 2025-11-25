package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalTeam;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DescriptionState extends FinalStateBase {
    
    private @Nullable Timer timer;
    
    public DescriptionState(@NotNull FinalGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        context.messageAllParticipants(context.getConfig().getDescription());
        this.timer = context.getTimerManager().start(Timer.builder()
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
    
    @Override
    public void onParticipantRejoin(FinalParticipant participant, FinalTeam team) {
        super.onParticipantRejoin(participant, team);
        participant.sendMessage(context.getConfig().getDescription());
    }
    
    @Override
    public void onNewParticipantJoin(FinalParticipant participant, FinalTeam team) {
        super.onNewParticipantJoin(participant, team);
        participant.sendMessage(context.getConfig().getDescription());
    }
}

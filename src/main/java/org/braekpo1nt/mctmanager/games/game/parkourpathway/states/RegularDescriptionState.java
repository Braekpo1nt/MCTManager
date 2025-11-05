package org.braekpo1nt.mctmanager.games.game.parkourpathway.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourParticipant;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourTeam;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegularDescriptionState extends ParkourPathwayStateBase {
    
    private @Nullable Timer timer;
    
    public RegularDescriptionState(@NotNull ParkourPathwayGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        for (ParkourParticipant participant : context.getParticipants().values()) {
            participant.teleport(context.getConfig().getStartingLocation());
            participant.getInventory().addItem(context.getWandItems());
        }
        context.messageAllParticipants(context.getConfig().getDescription());
        timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> {
                    context.setState(new CountDownState(context));
                })
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
    
    @Override
    public void onParticipantRejoin(ParkourParticipant participant, ParkourTeam team) {
        super.onParticipantRejoin(participant, team);
        participant.teleport(context.getConfig().getStartingLocation());
    }
    
    @Override
    public void onNewParticipantJoin(ParkourParticipant participant, ParkourTeam team) {
        super.onNewParticipantJoin(participant, team);
        participant.teleport(context.getConfig().getStartingLocation());
    }
}

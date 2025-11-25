package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PreRoundState extends FinalStateBase {
    
    private @Nullable Timer timer;
    
    public PreRoundState(@NotNull FinalGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        context.getTabList().setParticipantGreys(context.getParticipants().values(), false);
        for (FinalParticipant participant : context.getParticipants().values()) {
            switch (participant.getAffiliation()) {
                case NORTH -> {
                    participant.teleport(context.getConfig().getNorthMap().getSpawn());
                    ParticipantInitializer.clearStatusEffects(participant);
                    ParticipantInitializer.resetHealthAndHunger(participant);
                    participant.setGameMode(GameMode.ADVENTURE);
                }
                case SOUTH -> {
                    participant.teleport(context.getConfig().getSouthMap().getSpawn());
                    ParticipantInitializer.clearStatusEffects(participant);
                    ParticipantInitializer.resetHealthAndHunger(participant);
                    participant.setGameMode(GameMode.ADVENTURE);
                }
            }
        }
        context.updateRoundSidebar();
        context.updateAliveStatus(Affiliation.NORTH);
        context.updateAliveStatus(Affiliation.SOUTH);
        this.timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundStartingDuration())
                .withTopbar(context.getTopbar())
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .topbarPrefix(Component.text("Starting soon: "))
                .titleAudience(Audience.audience(
                        Audience.audience(context.getNorthTeam()),
                        Audience.audience(context.getSouthTeam())
                ))
                .onCompletion(() -> {
                    context.setState(new KitSelectionState(context));
                })
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
}

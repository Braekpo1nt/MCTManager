package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

public class PreRoundState extends ColossalCombatStateBase {
    public PreRoundState(@NotNull ColossalCombatGame context) {
        super(context);
        for (ColossalParticipant participant : context.getParticipants().values()) {
            switch (participant.getAffiliation()) {
                case NORTH -> {
                    participant.teleport(context.getConfig().getNorthSpawn());
                    context.giveLoadout(participant);
                    ParticipantInitializer.clearStatusEffects(participant);
                    ParticipantInitializer.resetHealthAndHunger(participant);
                    participant.setArrowsInBody(0);
                    participant.setGameMode(GameMode.ADVENTURE);
                }
                case SOUTH -> {
                    participant.teleport(context.getConfig().getSouthSpawn());
                    context.giveLoadout(participant);
                    ParticipantInitializer.clearStatusEffects(participant);
                    ParticipantInitializer.resetHealthAndHunger(participant);
                    participant.setArrowsInBody(0);
                    participant.setGameMode(GameMode.ADVENTURE);
                }
                case SPECTATOR -> {}
            }
        }
        // TODO: update round sidebar
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundStartingDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Round Starting: "))
                .onCompletion(() -> context.setState(new RoundActiveState(context)))
                .build());
    }
    
}

package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalTeam;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PreRoundState extends ColossalCombatStateBase {
    
    private @Nullable Timer timer;
    
    public PreRoundState(@NotNull ColossalCombatGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        context.closeGates();
        for (ColossalParticipant participant : context.getParticipants().values()) {
            switch (participant.getAffiliation()) {
                case NORTH -> {
                    participant.teleport(context.getConfig().getNorthGate().getSpawn());
                    context.giveLoadout(participant);
                    ParticipantInitializer.clearStatusEffects(participant);
                    ParticipantInitializer.resetHealthAndHunger(participant);
                    participant.setArrowsInBody(0);
                    participant.setGameMode(GameMode.ADVENTURE);
                }
                case SOUTH -> {
                    participant.teleport(context.getConfig().getSouthGate().getSpawn());
                    context.giveLoadout(participant);
                    ParticipantInitializer.clearStatusEffects(participant);
                    ParticipantInitializer.resetHealthAndHunger(participant);
                    participant.setArrowsInBody(0);
                    participant.setGameMode(GameMode.ADVENTURE);
                }
                case SPECTATOR -> {}
            }
        }
        context.updateRoundSidebar();
        context.updateAliveStatus(Affiliation.NORTH);
        context.updateAliveStatus(Affiliation.SOUTH);
        timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundStartingDuration())
                .withTopbar(context.getTopbar())
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Gates Opening: "))
                .topbarPrefix(Component.text("Gates Opening: "))
                .titleAudience(Audience.audience(context.getParticipants().values()))
                .onCompletion(() -> context.setState(new GatesOpeningState(context)))
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
    
    @Override
    public void onParticipantRejoin(ColossalParticipant participant, ColossalTeam team) {
        super.onParticipantRejoin(participant, team);
        context.giveLoadout(participant);
    }
    
    @Override
    public void onNewParticipantJoin(ColossalParticipant participant, ColossalTeam team) {
        super.onNewParticipantJoin(participant, team);
        context.giveLoadout(participant);
    }
}

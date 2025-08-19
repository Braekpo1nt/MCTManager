package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoundOverState extends ColossalCombatStateBase {
    
    private @Nullable Timer timer;
    
    public RoundOverState(@NotNull ColossalCombatGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        for (ColossalParticipant participant : context.getParticipants().values()) {
            if (participant.getAffiliation() != Affiliation.SPECTATOR) {
                participant.setGameMode(GameMode.SPECTATOR);
                ParticipantInitializer.clearInventory(participant);
                ParticipantInitializer.clearStatusEffects(participant);
                ParticipantInitializer.resetHealthAndHunger(participant);
                participant.setAlive(true);
            }
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
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
    
    @Override
    public void onParticipantRespawn(PlayerRespawnEvent event, ColossalParticipant participant) {
        super.onParticipantRespawn(event, participant);
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        event.setRespawnLocation(participant.getLocation());
    }
    
    @Override
    public void onParticipantPostRespawn(PlayerPostRespawnEvent event, ColossalParticipant participant) {
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        participant.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.clearInventory(participant);
    }
}

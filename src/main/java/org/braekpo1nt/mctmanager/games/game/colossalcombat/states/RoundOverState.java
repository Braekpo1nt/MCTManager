package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.experimental.Affiliation;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

public class RoundOverState extends ColossalCombatStateBase {
    public RoundOverState(@NotNull ColossalCombatGame context) {
        super(context);
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
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundOverDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Round Over: "))
                .onCompletion(() -> {
                    // TODO: check if there is another round
                    if () {
                        context.setState(new PreRoundState(context));
                    } else {
                        context.setState(new GameOverState(context));
                    }
                })
                .build());
    }
}

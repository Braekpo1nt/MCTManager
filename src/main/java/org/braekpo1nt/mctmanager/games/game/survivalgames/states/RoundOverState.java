package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class RoundOverState extends SurvivalGamesStateBase {
    
    public RoundOverState(@NotNull SurvivalGamesGame context) {
        super(context);
        for (SurvivalGamesParticipant participant : context.getParticipants().values()) {
            participant.setGameMode(GameMode.SPECTATOR);
            ParticipantInitializer.clearInventory(participant);
            participant.setAlive(true);
        }
        context.titleAllParticipants(UIUtils.roundOverTitle());
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundOverDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .topbarPrefix(Component.text("Round Over: "))
                .sidebarPrefix(Component.text("Round Starting: "))
                .onCompletion(() -> {
                    if (context.getCurrentRound() < context.getConfig().getRounds()) {
                        context.clearFloorItems();
                        context.clearAllChests();
                        context.fillAllChests();
                        context.clearContainers();
                        context.setCurrentRound(context.getCurrentRound() + 1);
                        context.setState(new PreRoundState(context));
                    } else {
                        context.setState(new GameOverState(context));
                    }
                })
                .build());
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull SurvivalGamesParticipant participant) {
        event.setCancelled(true);
    }
}

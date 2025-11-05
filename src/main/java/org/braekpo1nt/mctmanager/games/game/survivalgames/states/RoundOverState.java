package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.glow.GlowManager;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class RoundOverState extends SurvivalGamesStateBase {
    
    private Timer timer;
    
    public RoundOverState(@NotNull SurvivalGamesGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        resetGlowing(); // must happen before all are set to alive=true
        for (SurvivalGamesParticipant participant : context.getParticipants().values()) {
            participant.setGameMode(GameMode.SPECTATOR);
            ParticipantInitializer.clearInventory(participant);
            participant.setAlive(true);
        }
        context.titleAllParticipants(UIUtils.roundOverTitle());
        timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundOverDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .topbarPrefix(Component.text("Round Over: "))
                .sidebarPrefix(Component.text("Round Over: "))
                .onCompletion(() -> {
                    if (context.getCurrentRound() < context.getConfig().getRounds()) {
                        context.clearFloorItems();
                        context.clearAllChests();
                        context.fillAllChests();
                        context.clearContainers();
                        context.setCurrentRound(context.getCurrentRound() + 1);
                        context.setState(new MultiPreRoundState(context));
                    } else {
                        context.setState(new GameOverState(context));
                    }
                })
                .build());
    }
    
    private void resetGlowing() {
        GlowManager glowManager = context.getGlowManager();
        for (SurvivalGamesTeam team : context.getTeams().values()) {
            for (SurvivalGamesParticipant viewer : team.getParticipants()) {
                for (SurvivalGamesParticipant target : team.getParticipants()) {
                    if (!target.equals(viewer) && target.isAlive()) {
                        glowManager.hideGlowing(viewer, target);
                    }
                }
            }
        }
        for (Player viewer : context.getAdmins()) {
            for (SurvivalGamesParticipant target : context.getParticipants().values()) {
                if (target.isAlive()) {
                    glowManager.hideGlowing(viewer, target);
                }
            }
        }
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull SurvivalGamesParticipant participant) {
        event.setCancelled(true);
    }
}

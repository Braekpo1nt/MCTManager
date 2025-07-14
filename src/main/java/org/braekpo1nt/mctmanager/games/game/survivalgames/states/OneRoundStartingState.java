package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

/**
 * Used when there is only one round in the game
 */
public class OneRoundStartingState extends OnPlatformsState {
    public OneRoundStartingState(@NotNull SurvivalGamesGame context) {
        super(context);
        for (SurvivalGamesTeam team : context.getTeams().values()) {
            context.updateAliveCount(team);
        }
        context.initializeWorldBorder();
        context.createPlatformsAndTeleportTeams();
        for (SurvivalGamesParticipant participant : context.getParticipants().values()) {
            ParticipantInitializer.clearInventory(participant);
            participant.setGameMode(GameMode.ADVENTURE);
            participant.setAlive(true);
        }
        context.updateRoundLine();
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundStartingDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .topbarPrefix(Component.text("Starting: "))
                .sidebarPrefix(Component.text("Starting: "))
                .onCompletion(() -> context.setState(new RoundActiveState(context)))
                .build());
    }
}

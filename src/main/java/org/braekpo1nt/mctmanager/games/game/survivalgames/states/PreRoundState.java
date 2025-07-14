package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

public class PreRoundState extends OnPlatformsState {
    public PreRoundState(@NotNull SurvivalGamesGame context) {
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
        context.titleAllParticipants(UIUtils.roundXTitle(context.getCurrentRound()));
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundStartingDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .topbarPrefix(Component.text("Round Starting: "))
                .sidebarPrefix(Component.text("Round Starting: "))
                .onCompletion(() -> context.setState(new RoundActiveState(context)))
                .build());
    }
}

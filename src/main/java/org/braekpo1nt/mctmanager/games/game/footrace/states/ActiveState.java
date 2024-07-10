package org.braekpo1nt.mctmanager.games.game.footrace.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ActiveState implements FootRaceState {
    
    private final @NotNull FootRaceGame context;
    
    public ActiveState(@NotNull FootRaceGame context) {
        this.context = context;
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        if (participantShouldRejoin(participant)) {
            rejoinParticipant(participant);
        } else {
            initializeParticipant(participant);
        }
        context.getSidebar().updateLine(participant.getUniqueId(), "title", context.getTitle());
        
        Integer currentLap = context.getLaps().get(participant.getUniqueId());
        if (currentLap > FootRaceGame.MAX_LAPS) {
            showRaceCompleteFastBoard(participant.getUniqueId());
        } else {
            context.getSidebar().updateLine(participant.getUniqueId(), "lap", String.format("Lap: %d/%d", currentLap, FootRaceGame.MAX_LAPS));
        }
    }
    
    /**
     * Checks if the participant was previously in the game, and should thus rejoin
     * @param participant The participant to check
     * @return True if the participant was in the game before, and should rejoin. False
     * if the participant wasn't in the game before. 
     */
    private boolean participantShouldRejoin(Player participant) {
        return completedRace(participant) 
                || context.getLaps().containsKey(participant.getUniqueId());
    }
    
    private void rejoinParticipant(Player participant) {
        context.getParticipants().add(participant);
        context.getSidebar().addPlayer(participant);
        if (completedRace(participant)) {
            showRaceCompleteFastBoard(participant.getUniqueId());
        }
        context.giveBoots(participant);
    }
    
    /**
     * @param participant the participant
     * @return true if the given participant has already completed the race
     */
    private boolean completedRace(Player participant) {
        return context.getPlacements().contains(participant.getUniqueId());
    }
    
    private void showRaceCompleteFastBoard(UUID playerUUID) {
        long elapsedTime = System.currentTimeMillis() - context.getRaceStartTime();
        context.getSidebar().updateLines(playerUUID,
                new KeyLine("elapsedTime", TimeStringUtils.getTimeComponentMillis(elapsedTime)),
                new KeyLine("lap", Component.empty()
                        .append(Component.text("Finished "))
                        .append(GameManagerUtils.getPlacementTitle(
                                context.getPlacements().indexOf(playerUUID) + 1))
                        .append(Component.text("!")))
        );
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        resetParticipant(participant);
        context.getParticipants().remove(participant);
    }
    
    @Override
    public void initializeParticipant(Player participant) {
        context.initializeParticipant(participant);
    }
    
    @Override
    public void resetParticipant(Player participant) {
        context.resetParticipant(participant);
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        
    }
}

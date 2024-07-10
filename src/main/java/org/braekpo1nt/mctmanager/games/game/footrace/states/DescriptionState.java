package org.braekpo1nt.mctmanager.games.game.footrace.states;

import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class DescriptionState implements FootRaceState {
    
    private final @NotNull FootRaceGame context;
    
    public DescriptionState(@NotNull FootRaceGame context) {
        this.context = context;
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        initializeParticipant(participant);
        context.getSidebar().updateLine(participant.getUniqueId(), "title", context.getTitle());
        Integer currentLap = context.getLaps().get(participant.getUniqueId());
        context.getSidebar().updateLine(participant.getUniqueId(), "lap", String.format("Lap: %d/%d", currentLap, FootRaceGame.MAX_LAPS));
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        resetParticipant(participant);
        context.getParticipants().remove(participant);
        context.getLapCooldowns().remove(participant.getUniqueId());
        context.getLaps().remove(participant.getUniqueId());
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
        // do nothing
    }
}

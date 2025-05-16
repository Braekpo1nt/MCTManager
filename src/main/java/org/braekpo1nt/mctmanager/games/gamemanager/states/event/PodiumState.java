package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class PodiumState extends EventState {
    public PodiumState(@NotNull GameManager context, @NotNull ContextReference contextReference, @NotNull EventData eventData) {
        super(context, contextReference, eventData);
        sidebar.updateLine("currentGame", getCurrentGameLine());
        if (eventData.getWinningTeam() != null) {
            sidebar.addLine("winner", Component.empty()
                    .append(Component.text("Winner: "))
                    .append(eventData.getWinningTeam().getFormattedDisplayName()));
            for (MCTParticipant participant : onlineParticipants.values()) {
                if (participant.getTeamId().equals(eventData.getWinningTeam().getTeamId())) {
                    eventData.giveCrown(participant);
                    returnParticipantToHub(participant, config.getPodium());
                } else {
                    returnParticipantToHub(participant, config.getPodiumObservation());
                }
            }
        } else {
            sidebar.addLine("winner", Component.empty());
        }
    }
    
    @Override
    public CommandResult startGame(@NotNull Set<String> teamIds, @NotNull List<Player> gameAdmins, @NotNull GameType gameType, @NotNull String configFile) {
        return CommandResult.failure("Can't start a game, the event is over.");
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        if (eventData.getWinningTeam() != null) {
            for (MCTParticipant participant : eventData.getWinningTeam().getOnlineMembers()) {
                eventData.removeCrown(participant);
            }
        }
        sidebar.deleteLine("winner");
    }
    
    @Override
    public void onSwitchMode() {
        if (eventData.getWinningTeam() != null) {
            for (MCTParticipant participant : eventData.getWinningTeam().getOnlineMembers()) {
                eventData.removeCrown(participant);
            }
        }
        sidebar.deleteLine("winner");
    }
    
    @Override
    public Component getCurrentGameLine() {
        return Component.text("Thanks for playing!");
    }
    
    @Override
    public CommandResult modifyMaxGames(int newMaxGames) {
        return CommandResult.failure(Component.text("The event is over, can't change the max games."));
    }
    
    @Override
    public CommandResult addGameToVotingPool(@NotNull GameType gameToAdd) {
        return CommandResult.failure("The event is over, can't change the voting pool.");
    }
    
    @Override
    public CommandResult removeGameFromVotingPool(@NotNull GameType gameToRemove) {
        return CommandResult.failure("The event is over, can't change the voting pool.");
    }
    
    @Override
    public void onParticipantJoin(@NotNull MCTParticipant participant) {
        super.onParticipantJoin(participant);
        if (eventData.getWinningTeam() == null) {
            return;
        }
        if (participant.getTeamId().equals(eventData.getWinningTeam().getTeamId())) {
            participant.teleport(config.getPodium());
            eventData.giveCrown(participant);
        }
    }
    
    @Override
    public void onParticipantQuit(@NotNull MCTParticipant participant) {
        if (eventData.getWinningTeam() == null) {
            return;
        }
        if (participant.getTeamId().equals(eventData.getWinningTeam().getTeamId())) {
            eventData.removeCrown(participant);
        }
        super.onParticipantQuit(participant);
    }
}

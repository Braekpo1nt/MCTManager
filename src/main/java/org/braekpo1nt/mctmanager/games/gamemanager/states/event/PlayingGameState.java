package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.SuccessCommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayingGameState extends EventState {
    
    private final GameType activeGameType;
    
    public PlayingGameState(
            @NotNull GameManager context,
            @NotNull ContextReference contextReference,
            @NotNull EventData eventData,
            @NotNull GameType gameType,
            @NotNull String gameConfigFile) {
        super(context, contextReference, eventData);
        CommandResult commandResult = this.startGame(teams.keySet(), gameType, gameConfigFile);
        this.activeGameType = gameType;
        if (!(commandResult instanceof SuccessCommandResult)) {
            context.messageAdmins(commandResult.getMessage());
            context.messageOnlineParticipants(Component.empty()
                    .append(Component.text("An error occurred starting the selected game. The admins are handling it.")
                            .color(NamedTextColor.DARK_RED)));
            context.setState(new WaitingInHubState(context, contextReference, eventData));
        }
    }
    
    @Override
    public void onParticipantJoin(@NotNull MCTParticipant participant) {
        super.onParticipantJoin(participant);
        CommandResult commandResult = joinParticipantToGame(activeGameType, participant);
        if (!(commandResult instanceof SuccessCommandResult)) {
            CompositeCommandResult adminResult = new CompositeCommandResult(CommandResult.failure(Component.empty()
                    .append(Component.text("An error occurred joining "))
                    .append(participant.displayName())
                    .append(Component.text(" to "))
                    .append(Component.text(activeGameType.getTitle()))
                    .append(Component.text(":"))),
                    commandResult);
            context.messageAdmins(adminResult.getMessage());
        }
    }
    
    @Override
    public void onSwitchMode() {
        // do nothing
    }
    
    @Override
    public CommandResult startGame(Set<String> teamIds, @NotNull GameType gameType, @NotNull String configFile) {
        if (!activeGames.isEmpty()) {
            return CommandResult.failure("Only one game can be run at a time during an event");
        }
        return super.startGame(teamIds, gameType, configFile);
    }
    
    @Override
    public void gameIsOver(@NotNull GameType gameType, Map<String, Integer> teamScores, Map<UUID, Integer> participantScores, @NotNull Collection<UUID> gameParticipants) {
        super.gameIsOver(gameType, teamScores, participantScores, gameParticipants);
        postGame();
    }
    
    protected void postGame() {
        eventData.getPlayedGames().add(activeGameType);
        eventData.setCurrentGameNumber(eventData.getCurrentGameNumber() + 1);
        if (eventData.isItHalfTime()) {
            context.setState(new HalfTimeBreakState(context, contextReference, eventData));
        } else {
            context.setState(new WaitingInHubState(context, contextReference, eventData));
        }
    }
    
    @Override
    public CommandResult undoGame(@NotNull GameType gameType, int iterationIndex) {
        if (gameType == activeGameType) {
            return CommandResult.failure(Component.text("Can't undo ")
                    .append(Component.text(gameType.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" because it is in progress")));
        }
        return super.undoGame(gameType, iterationIndex);
    }
}

package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.SuccessCommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.gamemanager.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayingGameState extends EventState {
    
    private final GameInstanceId activeGameId;
    
    public PlayingGameState(
            @NotNull GameManager context,
            @NotNull ContextReference contextReference,
            @NotNull EventData eventData,
            @NotNull GameType gameType,
            @NotNull String gameConfigFile) {
        super(context, contextReference, eventData);
        CommandResult commandResult = this.startGame(teams.keySet(), onlineAdmins, gameType, gameConfigFile);
        this.activeGameId = new GameInstanceId(gameType, gameConfigFile);
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
        CommandResult commandResult = joinParticipantToGame(activeGameId, participant);
        if (!(commandResult instanceof SuccessCommandResult)) {
            CompositeCommandResult adminResult = new CompositeCommandResult(CommandResult.failure(Component.empty()
                    .append(Component.text("An error occurred joining "))
                    .append(participant.displayName())
                    .append(Component.text(" to "))
                    .append(Component.text(activeGameId.getTitle()))
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
    public CommandResult startGame(@NotNull Set<String> teamIds, @NotNull List<Player> gameAdmins, @NotNull GameType gameType, @NotNull String configFile) {
        if (!activeGames.isEmpty()) {
            return CommandResult.failure("Only one game can be run at a time during an event");
        }
        return super.startGame(teamIds, gameAdmins, gameType, configFile);
    }
    
    @Override
    public void onParticipantJoin(@NotNull PlayerJoinEvent event, @NotNull MCTParticipant participant) {
        super.onParticipantJoin(event, participant);
        MCTGame game = activeGames.get(activeGameId);
        MCTTeam team = teams.get(participant.getTeamId());
        game.onTeamJoin(team);
        game.onParticipantJoin(participant);
    }
    
    @Override
    public void gameIsOver(@NotNull GameInstanceId id, Map<String, Integer> teamScores, Map<UUID, Integer> participantScores, @NotNull Collection<UUID> gameParticipants, @NotNull List<Player> gameAdmins) {
        super.gameIsOver(id, teamScores, participantScores, gameParticipants, gameAdmins);
        postGame();
    }
    
    protected void postGame() {
        eventData.getPlayedGames().add(activeGameId.getGameType());
        eventData.setCurrentGameNumber(eventData.getCurrentGameNumber() + 1);
        if (eventData.isItHalfTime()) {
            context.setState(new HalfTimeBreakState(context, contextReference, eventData));
        } else {
            context.setState(new WaitingInHubState(context, contextReference, eventData));
        }
    }
    
    @Override
    public CommandResult undoGame(@NotNull GameInstanceId id, int iterationIndex) {
        if (activeGameId.equals(id)) {
            return CommandResult.failure(Component.text("Can't undo ")
                    .append(Component.text(id.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" because it is in progress")));
        }
        return super.undoGame(id, iterationIndex);
    }
}

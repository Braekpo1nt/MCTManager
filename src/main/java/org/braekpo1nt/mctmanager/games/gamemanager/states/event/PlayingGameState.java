package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PlayingGameState extends EventState {
    
    private final GameInstanceId activeGameId;
    
    public PlayingGameState(
            @NotNull GameManager context,
            @NotNull ContextReference contextReference,
            @NotNull EventData eventData,
            @NotNull GameType gameType,
            @NotNull String gameConfigFile) {
        super(context, contextReference, eventData);
        this.activeGameId = new GameInstanceId(gameType, gameConfigFile);
    }
    
    @Override
    public void enter() {
        this.startGame(teams.keySet(), onlineAdmins, activeGameId.getGameType(), activeGameId.getConfigFile())
                .thenAccept(commandResult -> {
                    CommandResult.showResult(context.getOnlineAdmins(), commandResult);
                })
                .exceptionally(e -> {
                    Main.logger().log(Level.SEVERE, "An error occurred starting the selected game. Returning to WaitingInHubState.", e);
                    context.messageAdmins(Component.empty()
                            .append(Component.text("An error occurred starting the selected game. See console for details."))
                            .append(Component.newline())
                            .append(Component.text(e.getMessage()))
                            .color(NamedTextColor.DARK_RED)
                    );
                    context.messageOnlineParticipants(Component.empty()
                            .append(Component.text("An error occurred starting the selected game. The admins are handling it.")
                                    .color(NamedTextColor.DARK_RED)));
                    context.setState(new WaitingInHubState(context, contextReference, eventData));
                    return null;
                });
    }
    
    @Override
    public @NotNull String getSystemStateDescription() {
        return "PLAYING_GAME";
    }
    
    @Override
    public void exit() {
        // do nothing
    }
    
    @Override
    public CompletableFuture<Void> onParticipantJoin(@NotNull MCTParticipant participant) {
        return super.onParticipantJoin(participant)
                .thenComposeAsync(
                        v -> joinParticipantToGame(activeGameId.getGameType(), activeGameId.getConfigFile(), participant),
                        context.getMainThreadExecutor()
                )
                .thenAccept(result -> CommandResult.showResult(plugin.getServer().getConsoleSender(), result))
                .exceptionally(e -> {
                    CommandResult adminResult = CommandResult.throwable(String.format("join %s to game %s", participant.displayName(), activeGameId.getTitle()), e);
                    CommandResult.showResult(context.getOnlineAdmins(), adminResult);
                    return null;
                })
                ;
    }
    
    @Override
    public void onAdminJoin(@NotNull Player admin) {
        super.onAdminJoin(admin);
        CommandResult commandResult = joinAdminToGame(activeGameId.getGameType(), activeGameId.getConfigFile(), admin);
        admin.sendMessage(commandResult.getMessageOrEmpty());
    }
    
    @Override
    public CompletableFuture<CommandResult> startGame(@NotNull Set<String> teamIds, @NotNull List<Player> gameAdmins, @NotNull GameType gameType, @NotNull String configFile) {
        if (!activeGames.isEmpty()) {
            return CommandResult.failure("Only one game can be run at a time during an event").asFuture();
        }
        return super.startGame(teamIds, gameAdmins, gameType, configFile);
    }
    
    @Override
    public CompletableFuture<Void> gameIsOver(int gameSessionId, @NotNull GameInstanceId id, Map<String, Integer> teamScores, Map<UUID, Integer> participantScores, @NotNull Collection<UUID> gameParticipants, @NotNull List<Player> gameAdmins) {
        CompletableFuture<Void> gameIsOverFuture = super.gameIsOver(gameSessionId, id, teamScores, participantScores, gameParticipants, gameAdmins);
        postGame();
        return gameIsOverFuture;
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
    public CommandResult undoGame(int gameSessionId) {
        // TODO: implement this operation differently
        return CommandResult.failure("Can't undo games while a game is running");
    }
    
    @Override
    public CommandResult redoGame(int gameSessionId) {
        // TODO: implement this operation differently
        return CommandResult.failure("Can't redo games while a game is running");
    }
}

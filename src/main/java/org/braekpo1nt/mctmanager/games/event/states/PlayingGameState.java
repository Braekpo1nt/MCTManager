package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.SuccessCommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.ScoreKeeper;
import org.braekpo1nt.mctmanager.games.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.event.states.delay.BackToHubDelayState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlayingGameState implements EventState {
    
    protected final EventManager context;
    protected final GameManager gameManager;
    
    /**
     * Start the given game with the given config file
     * @param context the context
     * @param gameType the game type to start
     * @param configFile the config file to use
     */
    public PlayingGameState(EventManager context, @NotNull GameType gameType, @NotNull String configFile) {
        this.context = context;
        this.gameManager = context.getGameManager();
        startGame(gameType, configFile);
    }
    
    /**
     * Don't start the game, used by {@link PlayingFinalGameState}
     * @param context the context
     */
    PlayingGameState(EventManager context) {
        this.context = context;
        this.gameManager = context.getGameManager();
    }
    
    protected void startGame(@NotNull GameType gameType, @NotNull String configFile) {
        createScoreKeeperForGame(gameType);
        context.getSidebar().removeAllPlayers();
        context.getAdminSidebar().removeAllPlayers();
        context.getAdmins().clear();
        CommandResult result = gameManager.startGame(gameType, configFile);
        if (result instanceof SuccessCommandResult) {
            context.messageAllAdmins(
                    result.and(CommandResult.failure(
                            Component.text("Unable to start the game ")
                                    .append(Component.text(gameType.getTitle()))
                                    .append(Component.text(". Returning to the hub to try again."))
                            )
                    ).getMessage());
            context.initializeParticipantsAndAdmins();
            context.setState(new WaitingInHubState(context));
        }
    }
    
    /**
     * Adds a ScoreKeeper to track the game's points. If no ScoreKeepers exist for gameType, creates a new list of iterations for the game.
     * @param gameType the game to add a ScoreKeeper for
     */
    private void createScoreKeeperForGame(GameType gameType) {
        if (!context.getScoreKeepers().containsKey(gameType)) {
            List<ScoreKeeper> iterationScoreKeepers = new ArrayList<>(List.of(new ScoreKeeper()));
            context.getScoreKeepers().put(gameType, iterationScoreKeepers);
        } else {
            List<ScoreKeeper> iterationScoreKeepers = context.getScoreKeepers().get(gameType);
            iterationScoreKeepers.add(new ScoreKeeper());
        }
    }
    
    @Override
    public void updatePersonalScores(Collection<Participant> updateParticipants) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "PlayingGameState updatePersonalScores()----");
    }
    
    @Override
    public <T extends Team> void updateTeamScores(Collection<T> updateTeams) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "PlayingGameState updateTeamScores()----");
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        // do nothing
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        // do nothing
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        // do nothing
    }
    
    @Override
    public CommandResult startEvent(int numberOfGames, int currentGameNumber, @NotNull EventConfig config) {
        return CommandResult.failure(Component.text("An event is already running."));
    }
    
    @Override
    public void onParticipantDamage(EntityDamageEvent event) {
        // do nothing
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event, Participant participant) {
        // do nothing
    }
    
    @Override
    public void onDropItem(PlayerDropItemEvent event, @NotNull Participant participant) {
        // do nothing
    }
    
    @Override
    public void gameIsOver(@NotNull GameType finishedGameType) {
        context.getPlayedGames().add(finishedGameType);
        context.setCurrentGameNumber(context.getCurrentGameNumber() + 1);
        context.initializeParticipantsAndAdmins();
        context.setState(new BackToHubDelayState(context));
        context.updateTeamScores();
        context.updatePersonalScores();
    }
    
    @Override
    public CommandResult setMaxGames(int newMaxGames) {
        if (newMaxGames < context.getCurrentGameNumber()) {
            return CommandResult.failure(Component.text("Can't set the max games for this event to less than ")
                    .append(Component.text(context.getCurrentGameNumber())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" because "))
                    .append(Component.text(context.getCurrentGameNumber()))
                    .append(Component.text(" game(s) have been played.")));
        }
        context.setMaxGames(newMaxGames);
        context.getSidebar().updateLine("currentGame", context.getCurrentGameLine());
        context.getAdminSidebar().updateLine("currentGame", context.getCurrentGameLine());
        // TODO: update the title of the active game to reflect the new max games
        return CommandResult.success(Component.text("Max games has been set to ")
                .append(Component.text(newMaxGames)));
    }
}

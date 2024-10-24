package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.ScoreKeeper;
import org.braekpo1nt.mctmanager.games.event.states.delay.BackToHubDelayState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlayingGameState implements EventState {
    
    protected final EventManager context;
    protected final GameManager gameManager;
    
    public PlayingGameState(EventManager context, GameType gameType) {
        this.context = context;
        this.gameManager = context.getGameManager();
        startGame(context, gameType);
    }
    
    protected void startGame(EventManager context, GameType gameType) {
        createScoreKeeperForGame(gameType);
        context.getSidebar().removeAllPlayers();
        context.getAdminSidebar().removeAllPlayers();
        context.getParticipants().clear();
        context.getAdmins().clear();
        boolean gameStarted = gameManager.startGame(gameType, Bukkit.getConsoleSender());
        if (!gameStarted) {
            context.messageAllAdmins(Component.text("Unable to start the game ")
                    .append(Component.text(gameType.getTitle()))
                    .append(Component.text(". Returning to the hub to try again."))
                    .color(NamedTextColor.RED));
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
    public void onParticipantJoin(Player participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
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
    public void startEvent(@NotNull CommandSender sender, int numberOfGames, int currentGameNumber) {
        sender.sendMessage(Component.text("An event is already running.")
                .color(NamedTextColor.RED));
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        // do nothing
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event) {
        // do nothing
    }
    
    @Override
    public void onDropItem(PlayerDropItemEvent event) {
        // do nothing
    }
    
    @Override
    public void gameIsOver(@NotNull GameType finishedGameType) {
        context.getPlayedGames().add(finishedGameType);
        context.setCurrentGameNumber(context.getCurrentGameNumber() + 1);
        context.setState(new BackToHubDelayState(context));
    }
    
    @Override
    public void colossalCombatIsOver(@Nullable String winningTeam) {
        // do nothing
    }
    
    @Override
    public void setMaxGames(@NotNull CommandSender sender, int newMaxGames) {
        if (newMaxGames < context.getCurrentGameNumber()) {
            sender.sendMessage(Component.text("Can't set the max games for this event to less than ")
                    .append(Component.text(context.getCurrentGameNumber())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" because "))
                    .append(Component.text(context.getCurrentGameNumber()))
                    .append(Component.text(" game(s) have been played."))
                    .color(NamedTextColor.RED));
            return;
        }
        context.setMaxGames(newMaxGames);
        context.getSidebar().updateLine("currentGame", context.getCurrentGameLine());
        context.getAdminSidebar().updateLine("currentGame", context.getCurrentGameLine());
        gameManager.updateGameTitle();
        sender.sendMessage(Component.text("Max games has been set to ")
                .append(Component.text(newMaxGames)));
    }
    
    @Override
    public void stopColossalCombat(@NotNull CommandSender sender) {
        sender.sendMessage(Component.text("Colossal Combat is not running")
                .color(NamedTextColor.RED));
    }
    
    @Override
    public void startColossalCombat(@NotNull CommandSender sender, @NotNull String firstTeam, @NotNull String secondTeam) {
        sender.sendMessage(Component.text("Can't start Colossal Combat while a game is running")
                .color(NamedTextColor.RED));
    }
}

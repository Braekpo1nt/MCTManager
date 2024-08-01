package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.states.delay.BackToHubDelayState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayingGameState implements EventState {
    
    protected final EventManager context;
    protected final GameManager gameManager;
    
    public PlayingGameState(EventManager context) {
        this.context = context;
        this.gameManager = context.getGameManager();
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
    public void startEvent(@NotNull CommandSender sender, int numberOfGames) {
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
        context.initializeParticipantsAndAdmins();
        context.getPlayedGames().add(finishedGameType);
        context.setCurrentGameNumber(context.getCurrentGameNumber() + 1);
        context.getSidebar().updateLine("currentGame", context.getCurrentGameLine());
        context.getAdminSidebar().updateLine("currentGame", context.getCurrentGameLine());
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getBackToHubDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Back to Hub: "))
                .onCompletion(() -> {
                    context.setState(new BackToHubDelayState(context, finishedGameType));
                })
                .build());
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
        
    }
    
    @Override
    public void startColossalCombat(@NotNull CommandSender sender, @NotNull String firstTeam, @NotNull String secondTeam) {
        
    }
}

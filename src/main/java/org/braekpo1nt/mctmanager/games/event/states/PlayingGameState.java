package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
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
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        
    }
    
    @Override
    public void startEvent(@NotNull CommandSender sender, int numberOfGames) {
        sender.sendMessage(Component.text("An event is already running.")
                .color(NamedTextColor.RED));
    }
    
    @Override
    public void stopEvent(@NotNull CommandSender sender) {
        
    }
    
    @Override
    public void undoGame(@NotNull CommandSender sender, @NotNull GameType gameType, int iterationIndex) {
        
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event) {
        
    }
    
    @Override
    public void onDropItem(PlayerDropItemEvent event) {
        
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
        
    }
    
    @Override
    public void setMaxGames(@NotNull CommandSender sender, int newMaxGames) {
        
    }
}

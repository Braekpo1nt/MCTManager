package org.braekpo1nt.mctmanager.games.event.states;

import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReadyUpState implements EventState {
    
    private final EventManager context;
    
    public ReadyUpState(EventManager context) {
        this.context = context;
    }
    
    public void readyUpParticipant(Player participant) {
        
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
        
    }
    
    @Override
    public void colossalCombatIsOver(@Nullable String winningTeam) {
        
    }
    
    @Override
    public void setMaxGames(@NotNull CommandSender sender, int newMaxGames) {
        
    }
    
    @Override
    public void stopColossalCombat(@NotNull CommandSender sender) {
        
    }
    
    @Override
    public void startColossalCombat(@NotNull CommandSender sender, @NotNull String firstTeam, @NotNull String secondTeam) {
        
    }
}

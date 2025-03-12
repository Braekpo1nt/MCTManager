package org.braekpo1nt.mctmanager.games.event.states.delay;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.states.EventState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DelayState implements EventState {
    
    protected final EventManager context;
    
    public DelayState(EventManager context) {
        this.context = context;
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        context.getParticipants().put(participant.getUniqueId(), participant);
        if (context.getSidebar() != null) {
            context.getSidebar().addPlayer(participant);
            context.updateTeamScores();
            context.getSidebar().updateLine(participant.getUniqueId(), "currentGame", context.getCurrentGameLine());
        }
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        context.getParticipants().remove(participant.getUniqueId());
        if (context.getSidebar() != null) {
            context.getSidebar().removePlayer(participant);
        }
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        context.getAdmins().add(admin);
        if (context.getAdminSidebar() != null) {
            context.getAdminSidebar().addPlayer(admin);
            context.updateTeamScores();
            context.getAdminSidebar().updateLine(admin.getUniqueId(), "currentGame", 
                    context.getCurrentGameLine());
        }
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        if (context.getAdminSidebar() != null) {
            context.getAdminSidebar().removePlayer(admin);
        }
    }
    
    @Override
    public void startEvent(@NotNull CommandSender sender, int numberOfGames, int currentGameNumber) {
        sender.sendMessage(Component.text("An event is already running.")
                .color(NamedTextColor.RED));
    }
    
    @Override
    public void onParticipantDamage(EntityDamageEvent event) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "EventManager.DelayState.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null) {
            return;
        }
        event.setCancelled(true);
    }
    
    @Override
    public void onDropItem(PlayerDropItemEvent event, @NotNull Participant participant) {
        event.setCancelled(true);
    }
    
    @Override
    public void gameIsOver(@NotNull GameType finishedGameType) {
        // do nothing
    }
    
    @Override
    public void colossalCombatIsOver(@Nullable Team winningTeam) {
        
    }
    
    @Override
    public void startColossalCombat(@NotNull CommandSender sender, @NotNull Team firstTeam, @NotNull Team secondTeam) {
        
    }
    
    @Override
    public void stopColossalCombat(@NotNull CommandSender sender) {
        sender.sendMessage(Component.text("Colossal Combat is not running")
                .color(NamedTextColor.RED));
    }
    
    @Override
    public void setMaxGames(@NotNull CommandSender sender, int newMaxGames) {
        sender.sendMessage(Component.text("Can't change the max games during transition period.")
                .color(NamedTextColor.RED));
    }
}

package org.braekpo1nt.mctmanager.games.event.states.delay;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.event.states.EventState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class DelayState implements EventState {
    
    protected final EventManager context;
    
    public DelayState(EventManager context) {
        this.context = context;
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        if (context.getSidebar() != null) {
            context.getSidebar().addPlayer(participant);
            context.updateTeamScores();
            context.getSidebar().updateLine(participant.getUniqueId(), "currentGame", context.getCurrentGameLine());
        }
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
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
    public CommandResult startEvent(int numberOfGames, int currentGameNumber, @NotNull EventConfig config) {
        return CommandResult.failure(Component.text("An event is already running."));
    }
    
    @Override
    public void onParticipantDamage(EntityDamageEvent event) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "EventManager.DelayState.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event, Participant participant) {
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
    public CommandResult setMaxGames(int newMaxGames) {
        return CommandResult.failure(Component.text("Can't change the max games during transition period."));
    }
}

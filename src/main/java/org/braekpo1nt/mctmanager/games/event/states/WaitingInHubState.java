package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;

public class WaitingInHubState implements EventState {
    
    private final EventManager context;
    private final GameManager gameManager;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    
    public WaitingInHubState(EventManager context) {
        this.context = context;
        this.gameManager = context.getGameManager();
        this.sidebar = context.getSidebar();
        this.adminSidebar = context.getAdminSidebar();
        gameManager.returnAllParticipantsToHub();
        double scoreMultiplier = context.matchProgressPointMultiplier();
        gameManager.messageOnlineParticipants(Component.text("Score multiplier: ")
                .append(Component.text(scoreMultiplier))
                .color(NamedTextColor.GOLD));
        Component prefix;
        if (context.allGamesHaveBeenPlayed()) {
            prefix = Component.text("Final round: ");
        } else {
            prefix = Component.text("Vote starts in: ");
        }
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getWaitingInHubDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(prefix)
                .onCompletion(() -> {
                    if (context.allGamesHaveBeenPlayed()) {
                        context.setState(new ColossalCombatDelay(context));
                    } else {
                        context.setState(new VotingState(context));
                    }
                })
                .build());
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        gameManager.returnParticipantToHubInstantly(participant);
        context.getParticipants().add(participant);
        if (sidebar != null) {
            sidebar.addPlayer(participant);
            context.updateTeamScores();
            sidebar.updateLine(participant.getUniqueId(), "currentGame", context.getCurrentGameLine());
        }
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        context.getParticipants().remove(participant);
        if (sidebar != null) {
            sidebar.removePlayer(participant);
        }
        if (context.getWinningTeam() != null) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            if (team.equals(context.getWinningTeam())) {
                context.removeCrown(participant);
            }
        }
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        if (adminSidebar != null) {
            adminSidebar.addPlayer(admin);
            context.updateTeamScores();
            adminSidebar.updateLine(admin.getUniqueId(), "currentGame", context.getCurrentGameLine());
        }
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        context.getAdmins().remove(admin);
        if (adminSidebar != null) {
            adminSidebar.removePlayer(admin);
        }
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
        event.setCancelled(true);
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
    public void setMaxGames(@NotNull CommandSender sender, int newMaxGames) {
        if (newMaxGames < context.getCurrentGameNumber() - 1) {
            sender.sendMessage(Component.text("Can't set the max games for this event to less than ")
                    .append(Component.text(context.getCurrentGameNumber() - 1)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" because "))
                    .append(Component.text(context.getCurrentGameNumber() - 1))
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
}

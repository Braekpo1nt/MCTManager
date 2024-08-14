package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PodiumState implements EventState {
    
    private final EventManager context;
    private final Sidebar sidebar;
    private final GameManager gameManager;
    private final Sidebar adminSidebar;
    
    public PodiumState(EventManager context) {
        this.context = context;
        gameManager = context.getGameManager();
        sidebar = context.getSidebar();
        adminSidebar = context.getAdminSidebar();
        Component winner;
        if (context.getWinningTeam() != null) {
            Component teamDisplayName = gameManager.getFormattedTeamDisplayName(context.getWinningTeam());
            winner = Component.empty()
                    .append(Component.text("Winner: "))
                    .append(teamDisplayName);
        } else {
            winner = Component.empty();
        }
        sidebar.addLine("winner", winner);
        adminSidebar.addLine("winner", winner);
        sidebar.updateLine("currentGame", context.getCurrentGameLine());
        adminSidebar.updateLine("currentGame", context.getCurrentGameLine());
        gameManager.returnAllParticipantsToPodium(context.getWinningTeam());
        for (Player participant : context.getParticipants()) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            if (team.equals(context.getWinningTeam())) {
                context.giveCrown(participant);
            }
        }
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        if (context.getWinningTeam() != null) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            boolean winner = team.equals(context.getWinningTeam());
            gameManager.returnParticipantToPodium(participant, winner);
            if (winner) {
                context.giveCrown(participant);
            }
        } else {
            gameManager.returnParticipantToPodium(participant, false);
        }
        context.getParticipants().add(participant);
        if (sidebar != null) {
            sidebar.addPlayer(participant);
            context.updateTeamScores();
            sidebar.updateLine(participant.getUniqueId(), "currentGame", context.getCurrentGameLine());
            if (context.getWinningTeam() != null) {
                Component teamDisplayName = gameManager.getFormattedTeamDisplayName(context.getWinningTeam());
                sidebar.updateLine("winner", Component.empty()
                        .append(Component.text("Winner: "))
                        .append(teamDisplayName));
            }
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
        context.getAdmins().add(admin);
        if (adminSidebar != null) {
            adminSidebar.addPlayer(admin);
            context.updateTeamScores();
            adminSidebar.updateLine(admin.getUniqueId(), "currentGame", context.getCurrentGameLine());
            if (context.getWinningTeam() != null) {
                Component teamDisplayName = gameManager.getFormattedTeamDisplayName(context.getWinningTeam());
                adminSidebar.updateLine("winner", Component.empty()
                        .append(Component.text("Winner: "))
                        .append(teamDisplayName));
            }
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
    public void onPlayerDamage(EntityDamageEvent event) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "PodiumState.onPlayerDamage() cancelled");
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
        if (context.getWinningTeam() == null) {
            return;
        }
        Player participant = ((Player) event.getWhoClicked());
        String team = gameManager.getTeamName(participant.getUniqueId());
        if (!team.equals(context.getWinningTeam())) {
            return;
        }
        if (!currentItem.equals(context.getCrown())) {
            return;
        }
        event.setCancelled(true);
    }
    
    @Override
    public void onDropItem(PlayerDropItemEvent event) {
        Player participant = event.getPlayer();
        if (context.getWinningTeam() == null) {
            return;
        }
        String team = gameManager.getTeamName(participant.getUniqueId());
        if (!team.equals(context.getWinningTeam())) {
            return;
        }
        if (!event.getItemDrop().getItemStack().equals(context.getCrown())) {
            return;
        }
        event.setCancelled(true);
    }
    
    @Override
    public void gameIsOver(@NotNull GameType finishedGameType) {
        // do nothing
    }
    
    @Override
    public void colossalCombatIsOver(@Nullable String winningTeam) {
        // do nothing
    }
    
    @Override
    public void setMaxGames(@NotNull CommandSender sender, int newMaxGames) {
        sender.sendMessage(Component.text("The event is over, can't change the max games.")
                .color(NamedTextColor.RED));
    }
    
    @Override
    public void stopColossalCombat(@NotNull CommandSender sender) {
        sender.sendMessage(Component.text("Colossal Combat is not running")
                .color(NamedTextColor.RED));
    }
    
    @Override
    public void startColossalCombat(@NotNull CommandSender sender, @NotNull String firstTeam, @NotNull String secondTeam) {
        sender.sendMessage(Component.text("The event is over, can't start Colossal Combat")
                .color(NamedTextColor.RED));
    }
}

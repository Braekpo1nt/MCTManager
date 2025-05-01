package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

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
            winner = Component.empty()
                    .append(Component.text("Winner: "))
                    .append(context.getWinningTeam().getFormattedDisplayName());
        } else {
            winner = Component.empty();
        }
        sidebar.addLine("winner", winner);
        adminSidebar.addLine("winner", winner);
        sidebar.updateLine("currentGame", context.getCurrentGameLine());
        adminSidebar.updateLine("currentGame", context.getCurrentGameLine());
//        gameManager.returnAllParticipantsToPodium(context.getWinningTeam());
        for (Participant participant : context.getParticipants()) {
            if (participant.getTeamId().equals(context.getWinningTeam().getTeamId())) {
                context.giveCrown(participant);
            }
        }
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        if (context.getWinningTeam() != null) {
            boolean winner = participant.getTeamId().equals(context.getWinningTeam().getTeamId());
//            gameManager.returnParticipantToPodium(participant, winner);
            if (winner) {
                context.giveCrown(participant);
            }
        } else {
//            gameManager.returnParticipantToPodium(participant, false);
        }
        if (sidebar != null) {
            sidebar.addPlayer(participant);
            context.updateTeamScores();
            sidebar.updateLine(participant.getUniqueId(), "currentGame", context.getCurrentGameLine());
            if (context.getWinningTeam() != null) {
                sidebar.updateLine("winner", Component.empty()
                        .append(Component.text("Winner: "))
                        .append(context.getWinningTeam().getFormattedDisplayName()));
            }
        }
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        if (sidebar != null) {
            sidebar.removePlayer(participant);
        }
        if (context.getWinningTeam() != null) {
            if (participant.getTeamId().equals(context.getWinningTeam().getTeamId())) {
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
                adminSidebar.updateLine("winner", Component.empty()
                        .append(Component.text("Winner: "))
                        .append(context.getWinningTeam().getFormattedDisplayName()));
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
    public CommandResult startEvent(int numberOfGames, int currentGameNumber, @NotNull EventConfig config) {
        return CommandResult.failure(Component.text("An event is already running."));
    }
    
    @Override
    public void onParticipantDamage(EntityDamageEvent event) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "EventManager.PodiumState.onPlayerDamage() cancelled");
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
        if (context.getWinningTeam() == null) {
            return;
        }
        if (!participant.getTeamId().equals(context.getWinningTeam().getTeamId())) {
            return;
        }
        if (!currentItem.equals(context.getCrown())) {
            return;
        }
        event.setCancelled(true);
    }
    
    @Override
    public void onDropItem(PlayerDropItemEvent event, @NotNull Participant participant) {
        if (context.getWinningTeam() == null) {
            return;
        }
        if (!participant.getTeamId().equals(context.getWinningTeam().getTeamId())) {
            return;
        }
        if (!event.getItemDrop().getItemStack().equals(context.getCrown())) {
            return;
        }
        event.setCancelled(true);
    }
    
    @Override
    public void updatePersonalScores(Collection<Participant> updateParticipants) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "PodiumState updatePersonalScores()");
        if (context.getSidebar() == null) {
            return;
        }
        for (Participant participant : updateParticipants) {
            context.getSidebar().updateLine(participant.getUniqueId(), "personalScore",
                    Component.empty()
                            .append(Component.text("Personal: "))
                            .append(Component.text(participant.getScore()))
                            .color(NamedTextColor.GOLD));
        }
    }
    
    @Override
    public <T extends Team> void updateTeamScores(Collection<T> updateTeams) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "PodiumState updateTeamScores()");
        if (context.getSidebar() == null) {
            return;
        }
        List<Team> sortedTeams = GameManagerUtils.sortTeams(updateTeams);
        if (context.getNumberOfTeams() != sortedTeams.size()) {
            EventState.reorderTeamLines(sortedTeams, context);
            return;
        }
        KeyLine[] teamLines = new KeyLine[context.getNumberOfTeams()];
        for (int i = 0; i < context.getNumberOfTeams(); i++) {
            Team team = sortedTeams.get(i);
            teamLines[i] = new KeyLine("team"+i, Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(team.getScore())
                            .color(NamedTextColor.GOLD))
            );
        }
        context.getSidebar().updateLines(teamLines);
        if (context.getAdminSidebar() == null) {
            return;
        }
        context.getAdminSidebar().updateLines(teamLines);
    }
    
    @Override
    public void gameIsOver(@NotNull GameType finishedGameType) {
        // do nothing
    }
    
    @Override
    public CommandResult setMaxGames(int newMaxGames) {
        return CommandResult.failure(Component.text("The event is over, can't change the max games."));
    }
}

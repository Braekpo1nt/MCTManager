package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.ReadyUpManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.topbar.ReadyUpTopbar;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReadyUpState implements EventState {
    
    private final EventManager context;
    private final GameManager gameManager;
    private final ReadyUpManager readyUpManager = new ReadyUpManager();
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final ReadyUpTopbar topbar = new ReadyUpTopbar();
    
    public ReadyUpState(EventManager context) {
        this.context = context;
        this.gameManager = context.getGameManager();
        this.sidebar = context.getSidebar();
        this.adminSidebar = context.getAdminSidebar();
        gameManager.returnAllParticipantsToHub();
        readyUpManager.clear();
        for (OfflinePlayer offlineParticipant : gameManager.getOfflineParticipants()) {
            String teamId = gameManager.getTeamName(offlineParticipant.getUniqueId());
            readyUpManager.unReadyParticipant(offlineParticipant.getUniqueId(), teamId);
        }
        Audience.audience(context.getParticipants()).sendMessage(Component.empty()
                .append(Component.text("Please ready up with "))
                .append(Component.text("/readyup")
                        .clickEvent(ClickEvent.runCommand("/readyup"))
                        .decorate(TextDecoration.UNDERLINED))
                .color(NamedTextColor.GREEN)
        );
        context.messageAllAdmins(Component.text("Ready Up has begun"));
    }
    
    @Override
    public void readyUpParticipant(Player participant) {
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        List<OfflinePlayer> teamMembers = gameManager.getOfflineParticipants(teamId);
        boolean wasReady = readyUpManager.readyUpParticipant(participant.getUniqueId(), teamId);
        if (!wasReady) {
            long readyCount = readyUpManager.readyCount(teamId);
            context.messageAllAdmins(Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" is ready. ("))
                    .append(Component.text(readyCount))
                    .append(Component.text("/"))
                    .append(Component.text(teamMembers.size()))
                    .append(Component.text(")"))
                    .color(NamedTextColor.GREEN)
            );
            int teamIdCount = gameManager.getTeamNames().size();
            if (readyUpManager.teamIsReady(teamId)) {
                Component teamDisplayName = gameManager.getFormattedTeamDisplayName(teamId);
                context.messageAllAdmins(Component.empty()
                        .append(teamDisplayName)
                        .append(Component.text(" is ready. ("))
                        .append(Component.text(readyUpManager.readyTeamCount()))
                        .append(Component.text("/"))
                        .append(Component.text(teamIdCount))
                        .append(Component.text(" teams ready)"))
                        .color(NamedTextColor.GREEN)
                );
            }
            if (readyUpManager.allTeamsAreReady()) {
                context.messageAllAdmins(Component.empty()
                        .append(Component.text("All "))
                        .append(Component.text(teamIdCount))
                        .append(Component.text(" teams are ready. Use"))
                        .append(Component.text()
                                .append(Component.text("/mct event start "))
                                .append(Component.text(context.getMaxGames()))
                                .clickEvent(ClickEvent.suggestCommand(
                                        String.format("/mct event start %d", context.getMaxGames())))
                        )
                        .append(Component.text(" to start the event"))
                        .color(NamedTextColor.GREEN)
                );
            }
        }
    }
    
    @Override
    public void unReadyParticipant(Player participant) {
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        List<OfflinePlayer> teamMembers = gameManager.getOfflineParticipants(teamId);
        boolean teamWasReady = readyUpManager.teamIsReady(teamId);
        boolean wasReady = readyUpManager.unReadyParticipant(participant.getUniqueId(), teamId);
        long readyCount = readyUpManager.readyCount(teamId);
        if (wasReady) {
            context.messageAllAdmins(Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" is not ready. ("))
                    .append(Component.text(readyCount))
                    .append(Component.text("/"))
                    .append(Component.text(teamMembers.size()))
                    .append(Component.text(")"))
                    .color(NamedTextColor.DARK_RED)
            );
            if (teamWasReady) {
                Component teamDisplayName = gameManager.getFormattedTeamDisplayName(teamId);
                context.messageAllAdmins(Component.empty()
                        .append(teamDisplayName)
                        .append(Component.text(" is not ready. ("))
                        .append(Component.text(readyUpManager.readyTeamCount()))
                        .append(Component.text("/"))
                        .append(Component.text(gameManager.getTeamNames().size()))
                        .append(Component.text(" teams ready)"))
                        .color(NamedTextColor.DARK_RED)
                );
            }
        }
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
        this.unReadyParticipant(participant);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        this.unReadyParticipant(participant);
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
        if (numberOfGames != context.getMaxGames()) {
            this.setMaxGames(sender, numberOfGames);
        }
        context.messageAllAdmins(Component.text("Starting event with ")
                .append(Component.text(context.getMaxGames()))
                .append(Component.text(" games.")));
        Audience.audience(
                Audience.audience(context.getAdmins()),
                Audience.audience(context.getParticipants())
        ).showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("Event Starting"))
                        .color(NamedTextColor.GOLD)
        ));
        context.setState(new WaitingInHubState(context));
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        event.setCancelled(true);
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
        // do nothing
    }
    
    @Override
    public void colossalCombatIsOver(@Nullable String winningTeam) {
        // do nothing
    }
    
    @Override
    public void setMaxGames(@NotNull CommandSender sender, int newMaxGames) {
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
        sender.sendMessage(Component.text("Can't start Colossal Combat during Ready Up state")
                .color(NamedTextColor.RED));
    }
}

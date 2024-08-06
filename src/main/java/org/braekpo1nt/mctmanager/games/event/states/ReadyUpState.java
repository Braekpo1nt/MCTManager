package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.ReadyUpManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.topbar.ReadyUpTopbar;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class ReadyUpState implements EventState {
    
    private final EventManager context;
    private final GameManager gameManager;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final ReadyUpManager readyUpManager;
    private final ReadyUpTopbar topbar;
    
    private final int readyUpPromptTaskId;
    private static final Title READYUP_TITLE = Title.title(
            Component.empty(), 
            Component.empty()
                    .append(Component.text("/readyup"))
                    .color(NamedTextColor.GREEN), 
            Title.Times.times(Duration.ZERO, Duration.ofMillis(2500), Duration.ZERO));
    private static final Component READY = Component.empty()
            .append(Component.text("ready")
                    .color(NamedTextColor.GREEN));
    private static final Component NOT_READY = Component.empty()
            .append(Component.text("not")
                    .color(NamedTextColor.DARK_RED));
    
    public ReadyUpState(EventManager context) {
        this.context = context;
        this.gameManager = context.getGameManager();
        this.sidebar = context.getSidebar();
        this.adminSidebar = context.getAdminSidebar();
        this.readyUpManager = context.getReadyUpManager();
        this.topbar = context.getTopbar();
        gameManager.returnAllParticipantsToHub();
        readyUpManager.clear();
        Set<String> teamIds = gameManager.getTeamNames();
        for (String teamId : teamIds) {
            readyUpManager.addTeam(teamId);
        }
        for (OfflinePlayer offlineParticipant : gameManager.getOfflineParticipants()) {
            String teamId = gameManager.getTeamName(offlineParticipant.getUniqueId());
            readyUpManager.unReadyParticipant(offlineParticipant.getUniqueId(), teamId);
        }
    
        for (String teamId : teamIds) {
            NamedTextColor teamColor = gameManager.getTeamNamedTextColor(teamId);
            topbar.addTeam(teamId, teamColor);
            if (readyUpManager.teamIsReady(teamId)) {
                topbar.setReadyCount(teamId, -1);
            } else {
                topbar.setReadyCount(teamId, 0);
            }
        }
        for (Player participant : context.getParticipants()) {
            topbar.showPlayer(participant);
            topbar.setReady(participant.getUniqueId(), false);
        }
        for (Player admin : context.getAdmins()) {
            topbar.showPlayer(admin);
        }
        
        promptToReadyUp(Audience.audience(context.getParticipants()));
        context.messageAllAdmins(Component.text("Ready Up has begun"));
        
        readyUpPromptTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player participant : context.getParticipants()) {
                    String teamId = gameManager.getTeamName(participant.getUniqueId());
                    boolean ready = readyUpManager.participantIsReady(participant.getUniqueId(), teamId);
                    if (!ready) {
                        participant.showTitle(READYUP_TITLE);
                    }
                }
            }
        }.runTaskTimer(context.getPlugin(), 0L, 2*20L).getTaskId();
    }
    
    private void promptToReadyUp(Audience audience) {
        audience.sendMessage(Component.empty()
                .append(Component.text("Please ready up with "))
                .append(Component.text("/readyup")
                        .clickEvent(ClickEvent.runCommand("/readyup"))
                        .decorate(TextDecoration.UNDERLINED))
                .color(NamedTextColor.GREEN));
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
            participant.showTitle(Title.title(Component.empty(), Component.empty()));
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
                topbar.setReadyCount(teamId, -1);
            } else {
                topbar.setReadyCount(teamId, readyCount);
            }
            if (readyUpManager.allTeamsAreReady()) {
                context.messageAllAdmins(Component.empty()
                        .append(Component.text("All "))
                        .append(Component.text(teamIdCount))
                        .append(Component.text(" teams are ready"))
                        .append(Component.newline())
                        .append(Component.text("Use "))
                        .append(Component.text()
                                .append(Component.text("/mct event start "))
                                .append(Component.text(context.getMaxGames()))
                                .clickEvent(ClickEvent.suggestCommand(
                                        String.format("/mct event start %d", context.getMaxGames())))
                                .decorate(TextDecoration.UNDERLINED)
                        )
                        .append(Component.text(" to start the event"))
                        .color(NamedTextColor.GREEN)
                );
            }
        }
        topbar.setReady(participant.getUniqueId(), true);
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
            topbar.setReadyCount(teamId, readyCount);
        }
        topbar.setReady(participant.getUniqueId(), false);
        promptToReadyUp(participant);
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
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        if (!readyUpManager.containsTeam(teamId)) {
            readyUpManager.addTeam(teamId);
            topbar.addTeam(teamId, gameManager.getTeamNamedTextColor(teamId));
        }
        topbar.showPlayer(participant);
        this.unReadyParticipant(participant);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        context.getParticipants().remove(participant);
        if (sidebar != null) {
            sidebar.removePlayer(participant);
        }
        this.unReadyParticipant(participant);
        topbar.hidePlayer(participant.getUniqueId());
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        if (adminSidebar != null) {
            adminSidebar.addPlayer(admin);
            context.updateTeamScores();
            adminSidebar.updateLine(admin.getUniqueId(), "currentGame", context.getCurrentGameLine());
        }
        topbar.showPlayer(admin);
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        context.getAdmins().remove(admin);
        if (adminSidebar != null) {
            adminSidebar.removePlayer(admin);
        }
        topbar.hidePlayer(admin.getUniqueId());
    }
    
    @Override
    public void startEvent(@NotNull CommandSender sender, int numberOfGames) {
        if (numberOfGames != context.getMaxGames()) {
            this.setMaxGames(sender, numberOfGames);
        }
        cancelAllTasks();
        topbar.hideAllPlayers();
        readyUpManager.clear();
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
        gameManager.removeParticipantsFromHub(context.getParticipants());
        context.setState(new WaitingInHubState(context));
    }
    
    @Override
    public void listReady(@NotNull CommandSender sender, @Nullable String teamId) {
        TextComponent.Builder builder = Component.text()
                .append(Component.text("Readiness:")
                        .decorate(TextDecoration.BOLD));
        List<OfflinePlayer> sortedOfflineParticipants = getSortedOfflineParticipants(teamId);
        for (OfflinePlayer participant : sortedOfflineParticipants) {
            Component displayName = gameManager.getDisplayName(participant);
            String participantTeamId = gameManager.getTeamName(participant.getUniqueId());
            boolean ready = readyUpManager.participantIsReady(participant.getUniqueId(), participantTeamId);
            builder.append(Component.empty()
                    .append(Component.newline())
                    .append(displayName)
                    .append(Component.text(": "))
                    .append(ready ? READY : NOT_READY));
        }
        sender.sendMessage(builder.build());
    }
    
    /**
     * @param teamId if null, all players are listed. If not null, players on the given team are listed.
     * @return the players sorted by readiness
     */
    public List<OfflinePlayer> getSortedOfflineParticipants(@Nullable String teamId) {
        List<OfflinePlayer> sortedOfflinePlayers;
        if (teamId == null) {
            sortedOfflinePlayers = gameManager.getOfflineParticipants();
        } else {
            sortedOfflinePlayers = gameManager.getOfflineParticipants(teamId);
        }
        sortedOfflinePlayers.sort((p1, p2) -> {
            String teamId1 = gameManager.getTeamName(p1.getUniqueId());
            String teamId2 = gameManager.getTeamName(p2.getUniqueId());
            int readyComparison = Boolean.compare(
                    readyUpManager.participantIsReady(p2.getUniqueId(), teamId2),
                    readyUpManager.participantIsReady(p1.getUniqueId(), teamId1));
            if (readyComparison != 0) {
                return readyComparison;
            }
            
            String p1Name = p1.getName();
            if (p1Name == null) {
                p1Name = gameManager.getOfflineIGN(p1.getUniqueId());
                if (p1Name == null) {
                    p1Name = p1.getUniqueId().toString();
                }
            }
            String p2Name = p2.getName();
            if (p2Name == null) {
                p2Name = gameManager.getOfflineIGN(p2.getUniqueId());
                if (p2Name == null) {
                    p2Name = p2.getUniqueId().toString();
                }
            }
            return p1Name.compareToIgnoreCase(p2Name);
        });
        return sortedOfflinePlayers;
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        Bukkit.getLogger().info("ReadyUpState");
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
    
    @Override
    public void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(readyUpPromptTaskId);
    }
}

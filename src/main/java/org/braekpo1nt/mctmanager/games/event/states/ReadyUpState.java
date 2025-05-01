package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.ReadyUpManager;
import org.braekpo1nt.mctmanager.games.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.topbar.ReadyUpTopbar;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
//        gameManager.returnAllParticipantsToHub();
        readyUpManager.clear();
        Collection<Team> teams = gameManager.getTeams();
        for (Team team : teams) {
            readyUpManager.addTeam(team.getTeamId());
        }
        for (OfflineParticipant offlineParticipant : gameManager.getOfflineParticipants()) {
            readyUpManager.unReadyParticipant(offlineParticipant.getUniqueId(), offlineParticipant.getTeamId());
        }
    
        for (Team team : teams) {
            topbar.addTeam(team.getTeamId(), team.getColor());
            if (readyUpManager.teamIsReady(team.getTeamId())) {
                topbar.setReadyCount(team.getTeamId(), -1);
            } else {
                topbar.setReadyCount(team.getTeamId(), 0);
            }
        }
        for (Participant participant : context.getParticipants()) {
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
                for (Participant participant : context.getParticipants()) {
                    boolean ready = readyUpManager.participantIsReady(participant.getUniqueId(), participant.getTeamId());
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
                        .hoverEvent(HoverEvent.showText(Component.text("Ready Up")))
                        .decorate(TextDecoration.UNDERLINED))
                .color(NamedTextColor.GREEN));
    }
    
    @Override
    public void readyUpParticipant(@NotNull Participant participant) {
        String teamId = participant.getTeamId();
        Team team = gameManager.getTeam(teamId);
        if (team == null) {
            return;
        }
        Collection<OfflineParticipant> teamMembers = gameManager.getOfflineParticipants(teamId);
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
            int teamIdCount = gameManager.getTeamIds().size();
            if (readyUpManager.teamIsReady(teamId)) {
                context.messageAllAdmins(Component.empty()
                        .append(team.getFormattedDisplayName())
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
        participant.sendMessage(Component.empty()
                .append(Component.text("You are ready. Unready with "))
                .append(Component.text("/unready")));
    }
    
    @Override
    public void unReadyParticipant(@NotNull Participant participant) {
        String teamId = participant.getTeamId();
        Team team = gameManager.getTeam(teamId);
        if (team == null) {
            return;
        }
        Collection<OfflineParticipant> teamMembers = gameManager.getOfflineParticipants(teamId);
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
                context.messageAllAdmins(Component.empty()
                        .append(team.getFormattedDisplayName())
                        .append(Component.text(" is not ready. ("))
                        .append(Component.text(readyUpManager.readyTeamCount()))
                        .append(Component.text("/"))
                        .append(Component.text(gameManager.getTeamIds().size()))
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
    public void onParticipantJoin(Participant participant) {
//        gameManager.returnParticipantToHub(participant);
        if (sidebar != null) {
            sidebar.addPlayer(participant);
            context.updateTeamScores();
            sidebar.updateLine(participant.getUniqueId(), "currentGame", context.getCurrentGameLine());
        }
        String teamId = participant.getTeamId();
        Team team = gameManager.getTeam(teamId);
        if (!readyUpManager.containsTeam(teamId) && team != null) {
            readyUpManager.addTeam(teamId);
            topbar.addTeam(teamId, team.getColor());
        }
        topbar.showPlayer(participant);
        this.unReadyParticipant(participant);
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
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
    public CommandResult startEvent(int numberOfGames, int currentGameNumber, @NotNull EventConfig config) {
        List<CommandResult> results = new ArrayList<>();
        if (numberOfGames != context.getMaxGames() || currentGameNumber != context.getCurrentGameNumber()) {
            context.setCurrentGameNumber(currentGameNumber);
            results.add(this.setMaxGames(numberOfGames));
        }
        cancelAllTasks();
        topbar.hideAllPlayers();
        readyUpManager.clear();
        Component message = Component.text("Starting event with ")
                .append(Component.text(context.getMaxGames()))
                .append(Component.text(" games."));
        results.add(CommandResult.success());
        context.messageAllAdmins(message);
        Audience.audience(
                Audience.audience(context.getAdmins()),
                Audience.audience(context.getParticipants())
        ).showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("Event Starting"))
                        .color(NamedTextColor.GOLD)
        ));
//        gameManager.removeParticipantsFromHub(context.getParticipants());
        context.setState(new WaitingInHubState(context));
        return CompositeCommandResult.all(results);
    }
    
    @Override
    public void listReady(@NotNull CommandSender sender, @Nullable String teamId) {
        TextComponent.Builder builder = Component.text()
                .append(Component.text("Readiness:")
                        .decorate(TextDecoration.BOLD));
        List<OfflineParticipant> sortedOfflineParticipants = getSortedOfflineParticipants(teamId);
        for (OfflineParticipant participant : sortedOfflineParticipants) {
            boolean ready = readyUpManager.participantIsReady(participant.getUniqueId(), participant.getTeamId());
            builder.append(Component.empty()
                    .append(Component.newline())
                    .append(participant.displayName())
                    .append(Component.text(": "))
                    .append(ready ? READY : NOT_READY));
        }
        sender.sendMessage(builder.build());
    }
    
    /**
     * @param teamId if null, all players are listed. If not null, players on the given team are listed.
     * @return the players sorted by readiness
     */
    public List<OfflineParticipant> getSortedOfflineParticipants(@Nullable String teamId) {
        Collection<OfflineParticipant> unsortedOfflineParticipants;
        if (teamId == null) {
            unsortedOfflineParticipants = gameManager.getOfflineParticipants();
        } else {
            unsortedOfflineParticipants = gameManager.getOfflineParticipants(teamId);
        }
        return unsortedOfflineParticipants.stream().sorted((p1, p2) -> {
            int readyComparison = Boolean.compare(
                    readyUpManager.participantIsReady(p2.getUniqueId(), p2.getTeamId()),
                    readyUpManager.participantIsReady(p1.getUniqueId(), p1.getTeamId()));
            if (readyComparison != 0) {
                return readyComparison;
            }
            
            return p1.getName().compareToIgnoreCase(p2.getName());
        }).toList();
    }
    
    @Override
    public void onParticipantDamage(EntityDamageEvent event) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "EventManager.ReadyUpState.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    @Override
    public void updatePersonalScores(Collection<Participant> updateParticipants) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "ReadyUpState updatePersonalScores()");
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
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "ReadyUpState updateTeamScores()");
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
    public void onClickInventory(InventoryClickEvent event, Participant participant) {
        // do nothing
    }
    
    @Override
    public void onDropItem(PlayerDropItemEvent event, @NotNull Participant participant) {
        // do nothing
    }
    
    @Override
    public void gameIsOver(@NotNull GameType finishedGameType) {
        // do nothing
    }
    
    @Override
    public CommandResult setMaxGames(int newMaxGames) {
        context.setMaxGames(newMaxGames);
        Component currentGameLine = context.getCurrentGameLine();
        context.getSidebar().updateLine("currentGame", currentGameLine);
        context.getAdminSidebar().updateLine("currentGame", currentGameLine);
        // TODO: update the title of the active game to reflect the new max games
        return CommandResult.success(Component.text("Max games has been set to ")
                .append(Component.text(newMaxGames)));
    }
    
    @Override
    public void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(readyUpPromptTaskId);
    }
}

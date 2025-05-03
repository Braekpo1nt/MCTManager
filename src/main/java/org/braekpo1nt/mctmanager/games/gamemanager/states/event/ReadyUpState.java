package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.event.ReadyUpManager;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.topbar.ReadyUpTopbar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ReadyUpState extends EventState {
    
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
    
    private final ReadyUpTopbar topbar;
    private final ReadyUpManager readyUpManager;
    private final int readyUpPromptTaskId;
    
    public ReadyUpState(
            @NotNull GameManager context, 
            @NotNull ContextReference contextReference, 
            @NotNull EventConfig eventConfig, 
            int maxGames,
            int startingGameNumber) {
        super(context, contextReference, eventConfig, startingGameNumber, maxGames);
        context.stopAllGames();
        for (MCTParticipant participant : onlineParticipants.values()) {
            returnParticipantToHub(participant);
        }
        setupSidebar();
        sidebar.updateLine("currentGame", getCurrentGameLine());
        this.readyUpManager = new ReadyUpManager();
        for (String teamId : teams.keySet()) {
            readyUpManager.addTeam(teamId);
        }
        for (OfflineParticipant offlineParticipant : allParticipants.values()) {
            readyUpManager.unReadyParticipant(offlineParticipant.getUniqueId(), offlineParticipant.getTeamId());
        }
        
        this.topbar = new ReadyUpTopbar();
        for (MCTTeam team : teams.values()) {
            topbar.addTeam(team.getTeamId(), team.getColor());
            if (readyUpManager.teamIsReady(team.getTeamId())) {
                topbar.setReadyCount(team.getTeamId(), -1);
            } else {
                topbar.setReadyCount(team.getTeamId(), 0);
            }
        }
        for (MCTParticipant participant : onlineParticipants.values()) {
            topbar.showPlayer(participant);
            topbar.setReady(participant.getUniqueId(), false);
        }
        for (Player admin : onlineAdmins) {
            topbar.showPlayer(admin);
        }
        
        context.messageOnlineParticipants(Component.empty()
                .append(Component.text("Please ready up with "))
                .append(Component.text("/readyup")
                        .clickEvent(ClickEvent.runCommand("/readyup"))
                        .hoverEvent(HoverEvent.showText(Component.text("Ready Up")))
                        .decorate(TextDecoration.UNDERLINED))
                .color(NamedTextColor.GREEN));
        context.messageAdmins(Component.text("Ready Up has begun"));
        
        readyUpPromptTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                for (MCTParticipant participant : onlineParticipants.values()) {
                    boolean ready = readyUpManager.participantIsReady(participant.getUniqueId(), participant.getTeamId());
                    if (!ready) {
                        participant.showTitle(READYUP_TITLE);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2*20L).getTaskId();
    }
    
    protected void setupSidebar() {
        sidebar.deleteAllLines();
        this.sidebar.updateTitle(eventData.getConfig().getTitle());
        sidebar.addLines(
                new KeyLine("currentGame", Component.empty()),
                new KeyLine("team0", Component.empty()),
                new KeyLine("team1", Component.empty()),
                new KeyLine("team2", Component.empty()),
                new KeyLine("team3", Component.empty()),
                new KeyLine("team4", Component.empty()),
                new KeyLine("team5", Component.empty()),
                new KeyLine("team6", Component.empty()),
                new KeyLine("team7", Component.empty()),
                new KeyLine("team8", Component.empty()),
                new KeyLine("team9", Component.empty()),
                new KeyLine("personalScore", Component.empty()),
                new KeyLine("timer", Component.empty())
        );
        updateSidebarTeamScores();
        updateSidebarPersonalScores(onlineParticipants.values());
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        topbar.cleanup();
        readyUpManager.cleanup();
        plugin.getServer().getScheduler().cancelTask(readyUpPromptTaskId);
    }
    
    @Override
    public void onSwitchMode() {
        topbar.cleanup();
        readyUpManager.cleanup();
        plugin.getServer().getScheduler().cancelTask(readyUpPromptTaskId);
    }
    
    @Override
    public CommandResult startEvent(int maxGames, int currentGameNumber) {
        topbar.cleanup();
        plugin.getServer().getScheduler().cancelTask(readyUpPromptTaskId);
        readyUpManager.cleanup();
        List<CommandResult> results = new ArrayList<>();
        if (maxGames != eventData.getMaxGames() || currentGameNumber != eventData.getCurrentGameNumber()) {
            eventData.setCurrentGameNumber(currentGameNumber);
            results.add(this.modifyMaxGames(maxGames));
        }
        Component message = Component.text("Starting event with ")
                .append(Component.text(eventData.getMaxGames()))
                .append(Component.text(" games."));
        results.add(CommandResult.success(message));
        context.messageAdmins(message);
        Audience.audience(
                onlineParticipants.values()
        ).showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("Event Starting"))
                        .color(NamedTextColor.GOLD)
        ));
        context.setState(new WaitingInHubState(context, contextReference, eventData));
        return CompositeCommandResult.all(results);
    }
    
    @Override
    public CommandResult startGame(Set<String> teamIds, @NotNull GameType gameType, @NotNull String configFile) {
        return CommandResult.failure("Can't start a game during this state.");
    }
    
    // readyup start
    @Override
    public CommandResult listReady(@Nullable String teamId) {
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
        return CommandResult.success(builder.build());
    }
    
    /**
     * @param teamId if null, all players are listed. If not null, players on the given team are listed.
     * @return the players sorted by readiness
     */
    public List<OfflineParticipant> getSortedOfflineParticipants(@Nullable String teamId) {
        Collection<OfflineParticipant> unsortedOfflineParticipants;
        if (teamId == null) {
            unsortedOfflineParticipants = context.getOfflineParticipants();
        } else {
            unsortedOfflineParticipants = context.getOfflineParticipants(teamId);
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
    public CommandResult readyUpParticipant(@NotNull MCTParticipant participant) {
        String teamId = participant.getTeamId();
        MCTTeam team = teams.get(teamId);
        Collection<OfflineParticipant> teamMembers = context.getOfflineParticipants(teamId);
        boolean wasReady = readyUpManager.readyUpParticipant(participant.getUniqueId(), teamId);
        if (!wasReady) {
            long readyCount = readyUpManager.readyCount(teamId);
            context.messageAdmins(Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" is ready. ("))
                    .append(Component.text(readyCount))
                    .append(Component.text("/"))
                    .append(Component.text(teamMembers.size()))
                    .append(Component.text(")"))
                    .color(NamedTextColor.GREEN)
            );
            participant.showTitle(Title.title(Component.empty(), Component.empty()));
            int teamIdCount = context.getTeamIds().size();
            if (readyUpManager.teamIsReady(teamId)) {
                context.messageAdmins(Component.empty()
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
                context.messageAdmins(Component.empty()
                        .append(Component.text("All "))
                        .append(Component.text(teamIdCount))
                        .append(Component.text(" teams are ready"))
                        .append(Component.newline())
                        .append(Component.text("Use "))
                        .append(Component.text()
                                .append(Component.text("/mct event start "))
                                .append(Component.text(eventData.getMaxGames()))
                                .clickEvent(ClickEvent.suggestCommand(
                                        String.format("/mct event start %d", eventData.getMaxGames())))
                                .decorate(TextDecoration.UNDERLINED)
                        )
                        .append(Component.text(" to start the event"))
                        .color(NamedTextColor.GREEN)
                );
            }
        }
        topbar.setReady(participant.getUniqueId(), true);
        return CommandResult.success(Component.empty()
                .append(Component.text("You are ready. Unready with "))
                .append(Component.text("/unready")));
    }
    
    @Override
    public CommandResult unReadyParticipant(@NotNull MCTParticipant participant) {
        String teamId = participant.getTeamId();
        MCTTeam team = teams.get(teamId);
        boolean teamWasReady = readyUpManager.teamIsReady(teamId);
        boolean wasReady = readyUpManager.unReadyParticipant(participant.getUniqueId(), teamId);
        long readyCount = readyUpManager.readyCount(teamId);
        if (wasReady) {
            context.messageAdmins(Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" is not ready. ("))
                    .append(Component.text(readyCount))
                    .append(Component.text("/"))
                    .append(Component.text(team.getMemberUUIDs().size()))
                    .append(Component.text(")"))
                    .color(NamedTextColor.DARK_RED)
            );
            if (teamWasReady) {
                context.messageAdmins(Component.empty()
                        .append(team.getFormattedDisplayName())
                        .append(Component.text(" is not ready. ("))
                        .append(Component.text(readyUpManager.readyTeamCount()))
                        .append(Component.text("/"))
                        .append(Component.text(teams.size()))
                        .append(Component.text(" teams ready)"))
                        .color(NamedTextColor.DARK_RED)
                );
            }
            topbar.setReadyCount(teamId, readyCount);
        }
        topbar.setReady(participant.getUniqueId(), false);
        return CommandResult.success(Component.empty()
                .append(Component.text("Please ready up with "))
                .append(Component.text("/readyup")
                        .clickEvent(ClickEvent.runCommand("/readyup"))
                        .hoverEvent(HoverEvent.showText(Component.text("Ready Up")))
                        .decorate(TextDecoration.UNDERLINED))
                .color(NamedTextColor.GREEN));
    }
    // readyup end
    
    
    /**
     * @return a line for sidebars saying what the current game is
     */
    @Override
    public Component getCurrentGameLine() {
        return Component.empty()
                .append(Component.text("/readyup")
                        .color(NamedTextColor.GREEN));
    }
    
    // leave/join start
    
    @Override
    public void onParticipantJoin(@NotNull MCTParticipant participant) {
        super.onParticipantJoin(participant);
        String teamId = participant.getTeamId();
        MCTTeam team = teams.get(teamId);
        if (!readyUpManager.containsTeam(teamId) && team != null) {
            readyUpManager.addTeam(teamId);
            topbar.addTeam(teamId, team.getColor());
        }
        topbar.showPlayer(participant);
        unReadyParticipant(participant);
    }
    
    @Override
    public void onParticipantQuit(@NotNull MCTParticipant participant) {
        unReadyParticipant(participant);
        topbar.hidePlayer(participant);
        super.onParticipantQuit(participant);
    }
    
    @Override
    public void onAdminJoin(@NotNull Player admin) {
        super.onAdminJoin(admin);
        topbar.showPlayer(admin);
    }
    
    @Override
    public void onAdminQuit(@NotNull Player admin) {
        topbar.hidePlayer(admin);
        super.onAdminQuit(admin);
    }
    // leave/join end
}

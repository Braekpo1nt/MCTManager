package org.braekpo1nt.mctmanager.games.gamemanager.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfigController;
import org.braekpo1nt.mctmanager.games.gamemanager.practice.PracticeManager;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.ReadyUpState;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PracticeState extends GameManagerState {
    
    private final PracticeManager practiceManager;
    
    public PracticeState(@NotNull GameManager context, @NotNull ContextReference contextReference) {
        super(context, contextReference);
        // TODO: since enter() loads a new game state, assigning any participants to this at this time is redundant because they are instantly removed and re-added
        this.practiceManager = new PracticeManager(
                context,
                config.getPractice(),
                teams.values(),
                onlineParticipants.values().stream()
                        .filter(participant -> !isParticipantInGame(participant.getUniqueId()))
                        .toList());
    }
    
    @Override
    public void enter() {
        setupSidebar();
        contextReference.getGameStateStorageUtil().practiceMode();
        CompletableFuture<CommandResult> futureResult = context.loadGameState();
        CommandResult.showResult(contextReference.getPlugin().getServer().getConsoleSender(), futureResult);
    }
    
    @Override
    public @NotNull String getSystemStateDescription() {
        return "PRACTICE";
    }
    
    @Override
    public void exit() {
        practiceManager.cleanup();
    }
    
    @Override
    protected @NotNull CompletableFuture<CommandResult> rebuildFromScores() {
        return context.getGameStateService().rebuildPracticeMode()
                .thenApply(v -> CommandResult.success(Component.text("Rebuilt game state from practice mode")))
                .exceptionally(e -> CommandResult.throwable("rebuild the practice game state", e))
                ;
    }
    
    @Override
    public void postLoadGameState() {
        practiceManager.setConfig(config.getPractice());
        for (MCTParticipant participant : onlineParticipants.values()) {
            participant.getInventory().close();
        }
        practiceManager.cleanup();
        practiceManager.setTeamsAndParticipants(
                teams.values(),
                onlineParticipants.values().stream()
                        .filter(participant -> !isParticipantInGame(participant.getUniqueId()))
                        .toList()
        );
    }
    
    /**
     * Set up the sidebar for this state. Called once in the constructor.
     */
    protected void setupSidebar() {
        sidebar.deleteAllLines();
        this.sidebar.updateTitle(Component.empty()
                .append(Sidebar.DEFAULT_TITLE)
                .append(Component.text(" - "))
                .append(Mode.PRACTICE.getTitle()));
        sidebar.addLines(
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
                new KeyLine("personalScore", Component.empty())
        );
        updateSidebarTeamScores();
        updateSidebarPersonalScores(onlineParticipants.values());
    }
    
    @Override
    public CommandResult switchMode(@NotNull Mode mode) {
        switch (mode) {
            case MAINTENANCE -> {
                practiceManager.cleanup();
                context.setState(new MaintenanceState(context, contextReference));
                return CommandResult.success(Component.text("Switched to maintenance mode"));
            }
            case PRACTICE -> {
                return CommandResult.success(Component.text("Already in practice mode"));
            }
            case EVENT -> {
//                practiceManager.cleanup();
                return CommandResult.success(Component.empty()
                        .append(Component.text("At this time, you must switch to event mode using the \"/mct event start\" command"))
                );
//                return startEvent(EventInfo.getDebugEvent(), 7, 0);
            }
            default -> {
                return CommandResult.failure(Component.empty()
                        .append(mode.getTitle()
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a valid mode")));
            }
        }
    }
    
    @Override
    public @NotNull Mode getMode() {
        return Mode.PRACTICE;
    }
    
    @Override
    public CompletableFuture<CommandResult> startGame(@NotNull Set<String> teamIds, @NotNull List<Player> gameAdmins, @NotNull GameType gameType, @NotNull String configFile) {
        if (gameType == GameType.FARM_RUSH) {
            if (teamIds.size() > 1) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("Only one team can play "))
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(" at a time during practice mode."))
                ).asFuture();
            }
        }
        return super.startGame(teamIds, gameAdmins, gameType, configFile);
    }
    
    @Override
    public CompletableFuture<CommandResult> joinParticipantToGame(@NotNull GameType gameType, @Nullable String configFile, @NotNull MCTParticipant participant) {
        if (configFile == null) {
            return CommandResult.failure(Component.text("Please provide a valid config file")).asFuture();
        }
        GameInstanceId id = new GameInstanceId(gameType, configFile);
        if (config.getPractice().isRestrictGameJoining()) {
            GameInstanceId teamGameId = context.getTeamActiveGame(participant.getTeamId());
            if (id.equals(teamGameId)) { // if you're trying to join your team's game
                return super.joinParticipantToGame(gameType, configFile, participant);
            } else { // you're trying to join another team's game
                return CommandResult.failure(Component.empty()
                        .append(Component.text("Can't join another group's game"))
                ).asFuture();
            }
        } else {
            if (id.getGameType() == GameType.FARM_RUSH) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("Only one team can play "))
                        .append(Component.text(id.getTitle()))
                        .append(Component.text(" at a time."))
                ).asFuture();
            }
            return super.joinParticipantToGame(gameType, configFile, participant);
        }
    }
    
    @Override
    public CompletableFuture<CommandResult> startEvent(@NotNull EventInfo eventInfo, int maxGames, int currentGameNumber) {
        try {
            EventConfig eventConfig = new EventConfigController(plugin.getDataFolder()).getConfig();
            practiceManager.cleanup();
            context.setState(new ReadyUpState(context, contextReference, eventInfo, eventConfig, maxGames, currentGameNumber));
            return CommandResult.success(Component.text("Switched to event mode")).asFuture();
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, e.getMessage(), e);
            return CommandResult.failure(Component.text("Can't switch to event mode. Error loading config file. See console for details:\n")
                    .append(Component.text(e.getMessage()))
            ).asFuture();
        }
    }
    
    // leave/join start
    
    @Override
    public CompletableFuture<Void> onParticipantJoin(@NotNull MCTParticipant participant) {
        CompletableFuture<Void> joinFuture = super.onParticipantJoin(participant);
        if (!participant.getWorld().equals(config.getWorld())) {
            participant.teleport(config.getSpawn());
        }
        practiceManager.addParticipant(participant);
        return joinFuture;
    }
    
    @Override
    public void onNonJoin(@NotNull Player player) {
        Optional<MCTTeam> first = contextReference.getTeams().values().stream().findFirst();
        if (first.isEmpty()) {
            return;
        }
        MCTTeam team = first.get();
        context.joinOnlineParticipant(player, team.getTeamId());
    }
    
    @Override
    public CompletableFuture<Void> onParticipantQuit(@NotNull MCTParticipant participant) {
        practiceManager.removeParticipant(participant.getUniqueId());
        return super.onParticipantQuit(participant);
    }
    
    // leave/join stop
    
    // team/participants management start
    @Override
    public CompletableFuture<Team> addTeam(String teamId, String teamDisplayName, String colorString) {
        return super.addTeam(teamId, teamDisplayName, colorString)
                .thenApplyAsync(team -> {
                    if (team != null) {
                        MCTTeam mctTeam = teams.get(teamId);
                        practiceManager.addTeam(mctTeam);
                    }
                    return team;
                }, context.getMainThreadExecutor())
                ;
    }
    
    @Override
    public CompletableFuture<CommandResult> removeTeam(String teamId) {
        practiceManager.removeTeam(teamId);
        return super.removeTeam(teamId);
    }
    
    // team/participants management stop
    
    @Override
    protected void onParticipantJoinGame(@NotNull GameInstanceId id, Participant participant) {
        super.onParticipantJoinGame(id, participant);
        practiceManager.removeParticipant(participant.getUniqueId());
    }
    
    @Override
    protected void onParticipantReturnToHub(@NotNull Participant participant) {
        super.onParticipantReturnToHub(participant);
        practiceManager.addParticipant(participant);
    }
    
    @Override
    public CommandResult openHubMenu(@NotNull MCTParticipant participant) {
        return practiceManager.openMenu(participant.getUniqueId());
    }
    
    @Override
    public void onParticipantDropItem(@NotNull PlayerDropItemEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        super.onParticipantInteract(event, participant);
        practiceManager.onParticipantInteract(event);
    }
}

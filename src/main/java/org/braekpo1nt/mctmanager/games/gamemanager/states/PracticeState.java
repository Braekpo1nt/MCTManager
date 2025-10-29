package org.braekpo1nt.mctmanager.games.gamemanager.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfigController;
import org.braekpo1nt.mctmanager.games.gamemanager.practice.PracticeManager;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.ReadyUpState;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetConfig;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PracticeState extends GameManagerState {
    
    private final PracticeManager practiceManager;
    
    public PracticeState(@NotNull GameManager context, @NotNull ContextReference contextReference) {
        super(context, contextReference);
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
        PresetConfig presetConfig = config.getPractice().getPreset();
        if (presetConfig != null) {
            CommandResult commandResult = GameManagerUtils.applyPreset(
                    plugin,
                    context,
                    new PresetStorageUtil(plugin.getDataFolder()),
                    presetConfig.getFile(),
                    presetConfig.isOverride(),
                    presetConfig.isResetScores(),
                    presetConfig.isWhitelist(),
                    presetConfig.isUnWhitelist(),
                    presetConfig.isKickUnWhitelisted());
            context.messageAdmins(commandResult.getMessageOrEmpty());
        }
    }
    
    @Override
    public void exit() {
        practiceManager.cleanup();
    }
    
    @Override
    public void onLoadGameState() {
        practiceManager.setConfig(config.getPractice());
        for (MCTParticipant participant : onlineParticipants.values()) {
            participant.getInventory().close();
        }
    }
    
    /**
     * Set up the sidebar for this state. Called once in the constructor.
     */
    protected void setupSidebar() {
        sidebar.deleteAllLines();
        this.sidebar.updateTitle(Component.empty()
                .append(Sidebar.DEFAULT_TITLE)
                .append(Component.text(" - "))
                .append(Component.text("Practice")));
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
    public CommandResult switchMode(@NotNull String mode) {
        switch (mode) {
            case "maintenance" -> {
                practiceManager.cleanup();
                context.setState(new MaintenanceState(context, contextReference));
                return CommandResult.success(Component.text("Switched to maintenance mode"));
            }
            case "practice" -> {
                return CommandResult.success(Component.text("Already in practice mode"));
            }
            case "event" -> {
                practiceManager.cleanup();
                return startEvent(7, 0);
            }
            default -> {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(mode)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a valid mode")));
            }
        }
    }
    
    @Override
    public CommandResult startGame(@NotNull Set<String> teamIds, @NotNull List<Player> gameAdmins, @NotNull GameType gameType, @NotNull String configFile) {
        if (gameType == GameType.FARM_RUSH) {
            if (teamIds.size() > 1) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("Only one team can play "))
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(" at a time during practice mode.")));
            }
        }
        return super.startGame(teamIds, gameAdmins, gameType, configFile);
    }
    
    @Override
    public CommandResult joinParticipantToGame(@NotNull GameType gameType, @Nullable String configFile, @NotNull MCTParticipant participant) {
        if (configFile == null) {
            return CommandResult.failure(Component.text("Please provide a valid config file"));
        }
        GameInstanceId id = new GameInstanceId(gameType, configFile);
        if (config.getPractice().isRestrictGameJoining()) {
            GameInstanceId teamGameId = context.getTeamActiveGame(participant.getTeamId());
            if (id.equals(teamGameId)) { // if you're trying to join your team's game
                return super.joinParticipantToGame(gameType, configFile, participant);
            } else { // you're trying to join another team's game
                return CommandResult.failure(Component.empty()
                        .append(Component.text("Can't join another group's game")));
            }
        } else {
            if (id.getGameType() == GameType.FARM_RUSH) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("Only one team can play "))
                        .append(Component.text(id.getTitle()))
                        .append(Component.text(" at a time.")));
            }
            return super.joinParticipantToGame(gameType, configFile, participant);
        }
    }
    
    @Override
    public CommandResult startEvent(int maxGames, int currentGameNumber) {
        try {
            EventConfig eventConfig = new EventConfigController(plugin.getDataFolder()).getConfig();
            practiceManager.cleanup();
            context.setState(new ReadyUpState(context, contextReference, eventConfig, maxGames, currentGameNumber));
            return CommandResult.success(Component.text("Switched to event mode"));
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, e.getMessage(), e);
            return CommandResult.failure(Component.text("Can't switch to event mode. Error loading config file. See console for details:\n")
                    .append(Component.text(e.getMessage())));
        }
    }
    
    // leave/join start
    
    @Override
    public void onParticipantJoin(@NotNull MCTParticipant participant) {
        super.onParticipantJoin(participant);
        if (!participant.getWorld().equals(config.getWorld())) {
            participant.teleport(config.getSpawn());
        }
        practiceManager.addParticipant(participant);
    }
    
    @Override
    public void onParticipantQuit(@NotNull MCTParticipant participant) {
        practiceManager.removeParticipant(participant.getUniqueId());
        super.onParticipantQuit(participant);
    }
    
    // leave/join stop
    
    
    @Override
    protected void addScores(Map<String, Integer> newTeamScores, Map<UUID, Integer> newParticipantScores, @NotNull GameInstanceId id) {
        Map<String, Integer> teamScores = newTeamScores.entrySet().stream()
                .filter(e -> teams.containsKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<UUID, Integer> participantScores = newParticipantScores.entrySet().stream()
                .filter(e -> allParticipants.containsKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // TODO: save scores for practice to special practice log, so Shotgun can use stats
        displayStats(teamScores, participantScores, id);
    }
    
    @Override
    public Team addTeam(String teamId, String teamDisplayName, String colorString) {
        Team team = super.addTeam(teamId, teamDisplayName, colorString);
        if (team != null) {
            MCTTeam mctTeam = teams.get(teamId);
            practiceManager.addTeam(mctTeam);
        }
        return team;
    }
    
    @Override
    public CommandResult removeTeam(String teamId) {
        practiceManager.removeTeam(teamId);
        return super.removeTeam(teamId);
    }
    
    @Override
    protected void onParticipantJoinGame(@NotNull GameInstanceId id, MCTParticipant participant) {
        super.onParticipantJoinGame(id, participant);
        practiceManager.removeParticipant(participant.getUniqueId());
    }
    
    @Override
    protected void onParticipantReturnToHub(@NotNull MCTParticipant participant) {
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

package org.braekpo1nt.mctmanager.games.gamemanager.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.participants.MaintenanceParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.teams.MaintenanceTeam;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfigController;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.ReadyUpState;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;

public class MaintenanceState extends GameManagerState {
    
    public MaintenanceState(
            @NotNull GameManager context,
            @NotNull ContextReference contextReference) {
        super(context, contextReference);
    }
    
    @Override
    public void enter() {
        setupSidebar();
    }
    
    @Override
    public void exit() {
        // do nothing
    }
    
    /**
     * Set up the sidebar for this state. Called once in the constructor.
     */
    protected void setupSidebar() {
        sidebar.deleteAllLines();
        this.sidebar.updateTitle(Component.empty()
                .append(Sidebar.DEFAULT_TITLE)
                .append(Component.text(" - "))
                .append(Mode.MAINTENANCE.getTitle()));
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
                return CommandResult.success(Component.text("Already in maintenance mode"));
            }
            case PRACTICE -> {
                context.setState(new PracticeState(context, contextReference));
                return CommandResult.success(Component.text("Switched to practice mode"));
            }
            case EVENT -> {
                // TODO: use the active event from SystemState
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
    
    // team/participants management start
    @Override
    public Team addTeam(String teamId, String teamDisplayName, String colorString) {
        try {
            context.getGameStateService().addTeam(MaintenanceTeam.builder()
                    .teamId(teamId)
                    .displayName(teamDisplayName)
                    .color(colorString)
                    .modifiedAt(new Date())
                    .build());
        } catch (SQLException e) {
            context.reportGameStateException("adding team to maintenance database", e);
        }
        return super.addTeam(teamId, teamDisplayName, colorString);
    }
    
    @Override
    public CommandResult removeTeam(String teamId) {
        try {
            context.getGameStateService().deleteMaintenanceTeam(teamId);
        } catch (SQLException e) {
            context.reportGameStateException("removing team from maintenance database", e);
        }
        return super.removeTeam(teamId);
    }
    
    /**
     * Same as super method but adds the participant to the maintenance_participants database as well
     */
    @Override
    public CommandResult joinParticipantToTeam(@NotNull OfflinePlayer offlinePlayer, @NotNull String ign, @NotNull MCTTeam team) {
        try {
            context.getGameStateService().addParticipant(
                    MaintenanceParticipantEntity.builder()
                            .teamId(team.getTeamId())
                            .participantUUID(offlinePlayer.getUniqueId().toString())
                            .build(),
                    ign
            );
        } catch (SQLException e) {
            context.reportGameStateException("adding participant to maintenance database", e);
        }
        return super.joinParticipantToTeam(offlinePlayer, ign, team);
    }
    
    @Override
    public CommandResult leaveParticipant(@NotNull OfflineParticipant offlineParticipant) {
        try {
            context.getGameStateService().deleteMaintenanceParticipant(offlineParticipant.getUniqueId().toString());
        } catch (SQLException e) {
            context.reportGameStateException("removing participant from maintenance database", e);
        }
        return super.leaveParticipant(offlineParticipant);
    }
    
    // team/participants management stop
    
    @Override
    public @NotNull Mode getMode() {
        return Mode.MAINTENANCE;
    }
    
    @Override
    public CommandResult startEvent(@NotNull EventInfo eventInfo, int maxGames, int currentGameNumber) {
        try {
            EventConfig eventConfig = new EventConfigController(plugin.getDataFolder()).getConfig();
            context.setState(new ReadyUpState(context, contextReference, eventInfo, eventConfig, maxGames, currentGameNumber));
            return CommandResult.success(Component.text("Switched to event mode"));
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, e.getMessage(), e);
            return CommandResult.failure(Component.text("Can't switch to event mode. Error loading config file. See console for details:\n")
                    .append(Component.text(e.getMessage())));
        }
    }
}

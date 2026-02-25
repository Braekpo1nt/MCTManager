package org.braekpo1nt.mctmanager.games.gamemanager.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfigController;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.ReadyUpState;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.jetbrains.annotations.NotNull;

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
                        .append(Component.text("At this time, you must switch to event mode using the \"/event start\" command"))
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

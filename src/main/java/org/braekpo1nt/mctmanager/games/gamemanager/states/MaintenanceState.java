package org.braekpo1nt.mctmanager.games.gamemanager.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.event.config.EventConfigController;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class MaintenanceState extends GameManagerState {
    
    public MaintenanceState(
            @NotNull GameManager context,
            @NotNull ContextReference contextReference) {
        super(context, contextReference);
        this.sidebar.updateTitle(Component.empty()
                .append(Sidebar.DEFAULT_TITLE)
                .append(Component.text(" - "))
                .append(Component.text("Maintenance")));
    }
    
    @Override
    public CommandResult switchMode(@NotNull String mode) {
        switch (mode) {
            case "maintenance" -> {
                return CommandResult.success(Component.text("Already in maintenance mode"));
            }
            case "practice" -> {
                context.setState(new PracticeState(context, contextReference));
                return CommandResult.success(Component.text("Switched to practice mode"));
            }
            case "event" -> {
                try {
                    EventConfig eventConfig = new EventConfigController(plugin.getDataFolder()).getConfig();
                    context.setState(new EventState(context, contextReference, eventConfig));
                    return CommandResult.success(Component.text("Switched to event mode"));
                } catch (ConfigException e) {
                    Main.logger().log(Level.SEVERE, e.getMessage(), e);
                    return CommandResult.failure(Component.text("Can't switch to event mode. Error loading config file. See console for details:\n")
                            .append(Component.text(e.getMessage())));
                }
            }
            default -> {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(mode)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a valid mode")));
            }
        }
    }
}

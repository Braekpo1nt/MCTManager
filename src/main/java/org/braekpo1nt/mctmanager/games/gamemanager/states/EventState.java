package org.braekpo1nt.mctmanager.games.gamemanager.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public class EventState extends GameManagerState {
    
    protected final @NotNull EventConfig config;
    protected final @NotNull EventManager eventManager;
    
    public EventState(
            @NotNull GameManager context,
            @NotNull ContextReference contextReference,
            @NotNull EventConfig config) {
        super(context, contextReference);
        this.config = config;
        this.eventManager = context.getEventManager();
    }
    
    @Override
    public Sidebar createSidebar() {
        return context.getSidebarFactory().createSidebar(config.getTitle());
    }
    
    // game start
    
    @Override
    protected @NotNull Component createNewTitle(String baseTitle) {
        int currentGameNumber = eventManager.getCurrentGameNumber();
        int maxGames = eventManager.getMaxGames();
        return Component.empty()
                .append(Component.text(baseTitle)
                        .color(NamedTextColor.BLUE))
                .append(Component.space())
                .append(Component.empty()
                        .append(Component.text("["))
                        .append(Component.text(currentGameNumber))
                        .append(Component.text("/"))
                        .append(Component.text(maxGames))
                        .append(Component.text("]"))
                        .color(NamedTextColor.GRAY));
    }
    
    @Override
    public void gameIsOver(@NotNull GameType gameType, @NotNull Collection<UUID> gameParticipants) {
        super.gameIsOver(gameType, gameParticipants);
        eventManager.gameIsOver(gameType);
    }
    
    @Override
    public CommandResult removeTeam(String teamId) {
        CommandResult commandResult = super.removeTeam(teamId);
        eventManager.updateTeamScores();
        return commandResult;
    }
    
    // game stop
}

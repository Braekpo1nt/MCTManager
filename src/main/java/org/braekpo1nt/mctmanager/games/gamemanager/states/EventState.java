package org.braekpo1nt.mctmanager.games.gamemanager.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class EventState extends GameManagerState {
    
    protected final @NotNull EventConfig eventConfig;
    protected final @NotNull EventManager eventManager;
    
    public EventState(
            @NotNull GameManager context,
            @NotNull ContextReference contextReference,
            @NotNull EventConfig eventConfig) {
        super(context, contextReference);
        this.eventConfig = eventConfig;
        this.eventManager = context.getEventManager();
        this.sidebar.updateTitle(eventConfig.getTitle());
    }
    
    @Override
    public CommandResult switchMode(@NotNull String mode) {
        switch (mode) {
            case "maintenance" -> {
                context.setState(new MaintenanceState(context, contextReference));
                return CommandResult.success(Component.text("Switched to maintenance mode"));
            }
            case "practice" -> {
                context.setState(new PracticeState(context, contextReference));
                return CommandResult.success(Component.text("Switched to practice mode"));
            }
            case "event" -> {
                return CommandResult.success(Component.text("Already in event mode"));
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
    public Sidebar createSidebar() {
        return context.getSidebarFactory().createSidebar(eventConfig.getTitle());
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
    public void gameIsOver(@NotNull GameType gameType, Map<String, Integer> teamScores, Map<UUID, Integer> participantScores, @NotNull Collection<UUID> gameParticipants) {
        super.gameIsOver(gameType, teamScores, participantScores, gameParticipants);
        eventManager.gameIsOver(gameType);
    }
    
    @Override
    public void addScores(Map<String, Integer> teamScores, Map<UUID, Integer> participantScores, GameType gameType) {
        super.addScores(teamScores, participantScores, gameType);
        eventManager.trackScores(teamScores, participantScores, gameType);
    }
    
    @Override
    public CommandResult removeTeam(String teamId) {
        CommandResult commandResult = super.removeTeam(teamId);
        eventManager.updateTeamScores();
        return commandResult;
    }
    
    // game stop
    
    // event handlers start
    @Override
    public void onParticipantInventoryClick(@NotNull InventoryClickEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantDropItem(@NotNull PlayerDropItemEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        event.setCancelled(true);
    }
    // event handlers stop
    
    @Override
    public double getMultiplier() {
        return eventManager.matchProgressPointMultiplier();
    }
}

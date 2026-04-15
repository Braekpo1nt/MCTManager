package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.ScoreEvent;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
import org.braekpo1nt.mctmanager.games.gamemanager.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.games.gamemanager.states.GameManagerState;
import org.braekpo1nt.mctmanager.games.gamemanager.states.MaintenanceState;
import org.braekpo1nt.mctmanager.games.gamemanager.states.PracticeState;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public abstract class EventState extends GameManagerState {
    
    protected final @NotNull EventData eventData;
    
    public EventState(
            @NotNull GameManager context,
            @NotNull ContextReference contextReference,
            @NotNull EventData eventData) {
        super(context, contextReference);
        this.eventData = eventData;
    }
    
    public EventState(
            @NotNull GameManager context,
            @NotNull ContextReference contextReference,
            @NotNull EventInfo eventInfo,
            @NotNull EventConfig eventConfig,
            int startingGameNumber,
            int maxGames) {
        super(context, contextReference);
        this.eventData = new EventData(eventConfig, eventInfo, startingGameNumber, maxGames);
    }
    
    @Override
    public CommandResult switchMode(@NotNull Mode mode) {
        switch (mode) {
            case MAINTENANCE -> {
                context.setState(new MaintenanceState(context, contextReference));
                return CommandResult.success(Component.text("Switched to maintenance mode"));
            }
            case PRACTICE -> {
                context.setState(new PracticeState(context, contextReference));
                return CommandResult.success(Component.text("Switched to practice mode"));
            }
            case EVENT -> {
                return CommandResult.success(Component.text("Already in event mode"));
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
    protected @NotNull CommandResult rebuildFromScores() throws SQLException {
        try {
            context.getGameStateService().rebuildEventMode(eventData.getEventInfo().getEventId());
            return CommandResult.success(Component.text("Rebuilt game state from event mode"));
        } catch (SQLException e) {
            throw new SQLException("Unable to rebuild event mode", e);
        }
    }
    
    
    @Override
    public @NotNull Mode getMode() {
        return Mode.EVENT;
    }
    
    @Override
    public Sidebar createSidebar() {
        return context.getSidebarFactory().createSidebar(eventData.getConfig().getTitle());
    }
    
    // event start
    @Override
    public CommandResult startEvent(@NotNull EventInfo eventInfo, int maxGames, int currentGameNumber) {
        return CommandResult.failure("Event is started");
    }
    
    @Override
    public @NotNull CommandResult stopEvent() {
        if (plugin.isEnabled()) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                    this::markEventAsStoppedInDatabase
            );
        } else {
            markEventAsStoppedInDatabase();
        }
        return switchMode(Mode.MAINTENANCE);
    }
    
    private void markEventAsStoppedInDatabase() {
        try {
            context.getEventService().updateActiveEvent(
                    null,
                    1,
                    7
            );
        } catch (SQLException e) {
            Main.logger().log(Level.WARNING, "Could not update active event ID in system_state table", e);
        }
        try {
            context.getEventService().setEventEndTime(
                    eventData.getEventInfo().getEventId(),
                    new Date()
            );
        } catch (SQLException e) {
            Main.logger().log(Level.WARNING, "Could set event end time", e);
        }
    }
    
    @Override
    public CommandResult undoGame(int gameSessionId) {
        // TODO: implement this operation
        return CommandResult.failure(Component.text("This operation is not yet implemented. Speak to the developers for more details."));
    }
    
    /**
     * Creates a report describing the scores associated with the given ScoreKeeper
     * @param id The game instance id the ScoreKeeper is associated with
     * @param scoreEvents The {@link ScoreEvent}s that were undone
     * @return A component with a report of the ScoreKeeper's scores
     */
    @NotNull
    private Component createUndoReport(@NotNull GameInstanceId id, @NotNull List<ScoreEvent> scoreEvents) {
        // TODO: implement this
        throw new UnsupportedOperationException("not yet implemented");
        //        TextComponent.Builder reportBuilder = Component.text()
//                .append(Component.text("|Scores for ("))
//                .append(Component.text(id.getTitle())
//                        .decorate(TextDecoration.BOLD))
//                .append(Component.text("):\n"))
//                .color(NamedTextColor.YELLOW);
//        for (MCTTeam team : teams.values()) {
//            int teamScoreToSubtract = scoreKeeper.getScore(team.getTeamId());
//            reportBuilder.append(Component.text("|  - "))
//                    .append(team.getFormattedDisplayName())
//                    .append(Component.text(": "))
//                    .append(Component.text(teamScoreToSubtract)
//                            .color(NamedTextColor.GOLD)
//                            .decorate(TextDecoration.BOLD))
//                    .append(Component.text("\n"));
//            
//            Collection<OfflineParticipant> participantsOnTeam = context.getParticipantsOnTeam(team.getTeamId());
//            for (OfflineParticipant participant : participantsOnTeam) {
//                int participantScoreToSubtract = scoreKeeper.getScore(participant.getUniqueId());
//                reportBuilder.append(Component.text("|    - "))
//                        .append(participant.displayName())
//                        .append(Component.text(": "))
//                        .append(Component.text(participantScoreToSubtract)
//                                .color(NamedTextColor.GOLD)
//                                .decorate(TextDecoration.BOLD))
//                        .append(Component.text("\n"));
//            }
//        }
//        return reportBuilder.build();
    }
    
    @Override
    public boolean eventIsActive() {
        return true;
    }
    
    @Override
    public CommandResult modifyMaxGames(int newMaxGames) {
        if (newMaxGames < eventData.getCurrentGameNumber()) {
            return CommandResult.failure(Component.text("Can't set the max games for this event to less than ")
                    .append(Component.text(eventData.getCurrentGameNumber())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" because "))
                    .append(Component.text(eventData.getCurrentGameNumber()))
                    .append(Component.text(" game(s) have been played.")));
        }
        eventData.setMaxGames(newMaxGames);
        sidebar.updateLine("currentGame", getCurrentGameLine());
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                context.getEventService().updateActiveEvent(
                        eventData.getEventInfo().getEventId(),
                        eventData.getCurrentGameNumber(),
                        eventData.getMaxGames()
                );
            } catch (SQLException e) {
                Main.logger().log(Level.SEVERE, "Could not update active currentGameNumber", e);
            }
        });
        return CommandResult.success(Component.text("Max games has been set to ")
                .append(Component.text(newMaxGames)));
    }
    
    /**
     * @param whitelist true if all players in the event should be whitelisted, false if they should be un-whitelisted
     * @return the result
     */
    @Override
    public CommandResult whitelist(boolean whitelist) {
        
        for (OfflineParticipant offlineParticipant : context.getOfflineParticipants()) {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(offlineParticipant.getUniqueId());
            offlinePlayer.setWhitelisted(whitelist);
        }
        
        return whitelist ?
                CommandResult.success(Component.text("Whitelisted all participants in this event"))
                :
                CommandResult.success(Component.text("De-whitelisted all participants in this event"))
                ;
    }
    
    /**
     * @return all the games in the voting pool (available to vote for in the event)
     */
    @Override
    public List<GameType> getVotingPool() {
        List<GameType> votingPool = new ArrayList<>(VoteManager.votableGames());
        votingPool.removeAll(eventData.getPlayedGames());
        return votingPool;
    }
    
    @Override
    public CommandResult addGameToVotingPool(@NotNull GameType gameToAdd) {
        if (!VoteManager.votableGames().contains(gameToAdd)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(gameToAdd.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a votable game")));
        }
        if (!eventData.getPlayedGames().contains(gameToAdd)) {
            return CommandResult.failure("This game is already in the voting pool");
        }
        eventData.getPlayedGames().remove(gameToAdd);
        return CommandResult.success(Component.empty()
                .append(Component.text(gameToAdd.getTitle()))
                .append(Component.text(" has been added to the voting pool.")));
    }
    
    @Override
    public CommandResult removeGameFromVotingPool(@NotNull GameType gameToRemove) {
        if (eventData.getPlayedGames().contains(gameToRemove)) {
            return CommandResult.failure("This game is not in the voting pool");
        }
        eventData.getPlayedGames().add(gameToRemove);
        return CommandResult.success(Component.empty()
                .append(Component.text(gameToRemove.getTitle()))
                .append(Component.text(" has been removed from the voting pool.")));
    }
    
    @Override
    public CommandResult listReady(@Nullable String teamId) {
        return CommandResult.failure("Not in ready-up state");
    }
    // event stop
    
    // game start
    @Override
    protected @NotNull Component createNewTitle(String baseTitle) {
        int currentGameNumber = eventData.getCurrentGameNumber();
        int maxGames = eventData.getMaxGames();
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
    @Nullable public String getEventId() {
        return eventData.getEventInfo().getEventId();
    }
    // game stop
    
    // leave/join start
    @Override
    public void onParticipantJoin(@NotNull MCTParticipant participant) {
        super.onParticipantJoin(participant);
        sidebar.updateLine(participant.getUniqueId(), "currentGame", getCurrentGameLine());
    }
    // leave/join stop
    
    // team/participants management start
    // team/participants management stop
    
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
        return eventData.getPointMultiplier();
    }
    
    // progression start
    
    /**
     * @return a line for sidebars saying what the current game is
     */
    public Component getCurrentGameLine() {
        if (eventData.getCurrentGameNumber() > eventData.getMaxGames()) {
            return Component.empty()
                    .append(Component.text("Final Game"));
        }
        return Component.empty()
                .append(Component.text("Game ["))
                .append(Component.text(eventData.getCurrentGameNumber()))
                .append(Component.text("/"))
                .append(Component.text(eventData.getMaxGames()))
                .append(Component.text("] "))
                .append(Component.empty()
                        .append(Component.text("(x"))
                        .append(Component.text(eventData.getPointMultiplier())
                                .color(NamedTextColor.GOLD))
                        .append(Component.text(")")));
    }
    // progression end
}

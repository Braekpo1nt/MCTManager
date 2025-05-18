package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.event.ScoreKeeper;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.games.gamemanager.states.GameManagerState;
import org.braekpo1nt.mctmanager.games.gamemanager.states.MaintenanceState;
import org.braekpo1nt.mctmanager.games.gamemanager.states.PracticeState;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
            @NotNull EventConfig eventConfig,
            int startingGameNumber,
            int maxGames) {
        super(context, contextReference);
        this.eventData = new EventData(eventConfig, startingGameNumber, maxGames);
    }
    
    @Override
    public CommandResult switchMode(@NotNull String mode) {
        switch (mode) {
            case "maintenance" -> {
                onSwitchMode();
                context.setState(new MaintenanceState(context, contextReference));
                return CommandResult.success(Component.text("Switched to maintenance mode"));
            }
            case "practice" -> {
                onSwitchMode();
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
    
    /**
     * Code to be run when the mode is switched
     */
    public abstract void onSwitchMode();
    
    @Override
    public Sidebar createSidebar() {
        return context.getSidebarFactory().createSidebar(eventData.getConfig().getTitle());
    }
    
    // event start
    @Override
    public CommandResult startEvent(int maxGames, int currentGameNumber) {
        return CommandResult.failure("Event is started");
    }
    
    @Override
    public CommandResult stopEvent() {
        return switchMode("practice");
    }
    
    @Override
    public int getGameIterations(@NotNull GameType gameType) {
        return eventData.getGameIterations(gameType);
    }
    
    @Override
    public CommandResult undoGame(@NotNull GameType gameType, int iterationIndex) {
        if (!eventData.getScoreKeepers().containsKey(gameType)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("No points were tracked for "))
                    .append(Component.text(gameType.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(".")));
        }
        List<ScoreKeeper> gameScoreKeepers = eventData.getScoreKeepers().get(gameType);
        if (iterationIndex < 0) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(iterationIndex+1)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a valid play-through")));
        }
        if (iterationIndex >= gameScoreKeepers.size()) {
            return CommandResult.failure(Component.text(gameType.getTitle())
                    .append(Component.text(" has only been played "))
                    .append(Component.text(gameScoreKeepers.size()))
                    .append(Component.text(" time(s). Can't undo play-through "))
                    .append(Component.text(iterationIndex + 1)));
        }
        ScoreKeeper iterationScoreKeeper = gameScoreKeepers.get(iterationIndex);
        if (iterationScoreKeeper == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("No points were tracked for play-through "))
                    .append(Component.text(iterationIndex + 1)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" of "))
                    .append(Component.text(gameType.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(".")));
        }
        undoScores(iterationScoreKeeper);
        gameScoreKeepers.set(iterationIndex, null); // remove tracked points for this iteration
        Component report = createScoreKeeperReport(gameType, iterationScoreKeeper);
        plugin.getServer().getConsoleSender().sendMessage(report);
        return CommandResult.success(report);
    }
    
    /**
     * Removes the scores that were tracked by the given ScoreKeeper
     * @param scoreKeeper holds the tracked scores to be removed
     */
    private void undoScores(ScoreKeeper scoreKeeper) {
        for (MCTTeam team : teams.values()) {
            int teamScoreToSubtract = scoreKeeper.getScore(team.getTeamId());
            int teamCurrentScore = team.getScore();
            if (teamCurrentScore - teamScoreToSubtract < 0) {
                teamScoreToSubtract = teamCurrentScore;
            }
            context.addScore(team, -teamScoreToSubtract);
            
            Collection<OfflineParticipant> participantsOnTeam = context.getParticipantsOnTeam(team.getTeamId());
            for (OfflineParticipant participant : participantsOnTeam) {
                int participantScoreToSubtract = scoreKeeper.getScore(participant.getUniqueId());
                int participantCurrentScore = participant.getScore();
                if (participantCurrentScore - participantScoreToSubtract < 0) {
                    participantScoreToSubtract = participantCurrentScore;
                }
                context.addScore(participant, -participantScoreToSubtract);
            }
        }
    }
    
    /**
     * Creates a report describing the scores associated with the given ScoreKeeper
     * @param gameType The gameType the ScoreKeeper is associated with
     * @param scoreKeeper The scorekeeper describing the given scores
     * @return A component with a report of the ScoreKeeper's scores
     */
    @NotNull
    private Component createScoreKeeperReport(@NotNull GameType gameType, @NotNull ScoreKeeper scoreKeeper) {
        TextComponent.Builder reportBuilder = Component.text()
                .append(Component.text("|Scores for ("))
                .append(Component.text(gameType.getTitle())
                        .decorate(TextDecoration.BOLD))
                .append(Component.text("):\n"))
                .color(NamedTextColor.YELLOW);
        for (MCTTeam team : teams.values()) {
            int teamScoreToSubtract = scoreKeeper.getScore(team.getTeamId());
            reportBuilder.append(Component.text("|  - "))
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(teamScoreToSubtract)
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text("\n"));
            
            Collection<OfflineParticipant> participantsOnTeam = context.getParticipantsOnTeam(team.getTeamId());
            for (OfflineParticipant participant : participantsOnTeam) {
                int participantScoreToSubtract = scoreKeeper.getScore(participant.getUniqueId());
                reportBuilder.append(Component.text("|    - "))
                        .append(participant.displayName())
                        .append(Component.text(": "))
                        .append(Component.text(participantScoreToSubtract)
                                .color(NamedTextColor.GOLD)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text("\n"));
            }
        }
        return reportBuilder.build();
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
        return CommandResult.success(Component.text("Max games has been set to ")
                .append(Component.text(newMaxGames)));
    }
    
    @Override
    public CommandResult addGameToVotingPool(@NotNull GameType gameToAdd) {
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
    public void addScores(Map<String, Integer> teamScores, Map<UUID, Integer> participantScores, GameType gameType) {
        super.addScores(teamScores, participantScores, gameType);
        eventData.trackScores(teamScores, participantScores, gameType);
    }
    // game stop
    
    // leave/join start
    @Override
    public void onParticipantJoin(@NotNull MCTParticipant participant) {
        super.onParticipantJoin(participant);
        sidebar.updateLine(participant.getUniqueId(), "currentGame", getCurrentGameLine());
    }
    // leave/join stop
    
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

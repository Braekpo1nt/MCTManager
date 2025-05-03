package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.event.states.delay.StartingGameDelayState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VotingState implements EventState {
    
    private final EventManager context;
    private final GameManager gameManager;
    
    public VotingState(EventManager context) {
        this.context = context;
        this.gameManager = context.getGameManager();
        context.getSidebar().removeAllPlayers();
        context.getAdminSidebar().removeAllPlayers();
        context.getAdmins().clear();
    }
    
    /**
     * @param gameType the game to play
     * @param ignore unused at this time
     */
    private void startingGameDelay(@NotNull GameType gameType, @NotNull String ignore) {
        context.initializeParticipantsAndAdmins();
        String configFile = context.getConfig().getGameConfigs().getOrDefault(gameType, "default.json");
        context.setState(new StartingGameDelayState(context, gameType, configFile));
    }
    
    @Override
    public void updatePersonalScores(Collection<Participant> updateParticipants) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "VotingState updatePersonalScores()");
        if (context.getSidebar() == null) {
            return;
        }
        for (Participant participant : updateParticipants) {
            context.getSidebar().updateLine(participant.getUniqueId(), "personalScore",
                    Component.empty()
                            .append(Component.text("Personal: "))
                            .append(Component.text(participant.getScore()))
                            .color(NamedTextColor.GOLD));
        }
    }
    
    @Override
    public <T extends Team> void updateTeamScores(Collection<T> updateTeams) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "VotingState updateTeamScores()");
        if (context.getSidebar() == null) {
            return;
        }
        List<Team> sortedTeams = GameManagerUtils.sortTeams(updateTeams);
        if (context.getNumberOfTeams() != sortedTeams.size()) {
            EventState.reorderTeamLines(sortedTeams, context);
            return;
        }
        KeyLine[] teamLines = new KeyLine[context.getNumberOfTeams()];
        for (int i = 0; i < context.getNumberOfTeams(); i++) {
            Team team = sortedTeams.get(i);
            teamLines[i] = new KeyLine("team"+i, Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(team.getScore())
                            .color(NamedTextColor.GOLD))
            );
        }
        context.getSidebar().updateLines(teamLines);
        if (context.getAdminSidebar() == null) {
            return;
        }
        context.getAdminSidebar().updateLines(teamLines);
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        
    }
    
    @Override
    public CommandResult startEvent(int numberOfGames, int currentGameNumber, @NotNull EventConfig config) {
        return CommandResult.failure(Component.text("An event is already running."));
    }
    
    @Override
    public void onParticipantDamage(EntityDamageEvent event) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "EventManager.VotingState.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event, Participant participant) {
        // do nothing
    }
    
    @Override
    public void onDropItem(PlayerDropItemEvent event, @NotNull Participant participant) {
        // do nothing
    }
    
    @Override
    public void gameIsOver(@NotNull GameType finishedGameType) {
        // do nothing
    }
    
    @Override
    public CommandResult setMaxGames(int newMaxGames) {
        if (newMaxGames < context.getCurrentGameNumber()) {
            return CommandResult.failure(Component.text("Can't set the max games for this event to less than ")
                    .append(Component.text(context.getCurrentGameNumber())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" because "))
                    .append(Component.text(context.getCurrentGameNumber() - 1))
                    .append(Component.text(" game(s) have been played and voting is in progress.")));
        }
        context.setMaxGames(newMaxGames);
        context.getSidebar().updateLine("currentGame", context.getCurrentGameLine());
        context.getAdminSidebar().updateLine("currentGame", context.getCurrentGameLine());
        // TODO: update the title of the active game to reflect the new max games
        return CommandResult.success(Component.text("Max games has been set to ")
                .append(Component.text(newMaxGames)));
    }
    
    @Override
    public Component getTitle() {
        return context.getConfig().getTitle();
    }
}

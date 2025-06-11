package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.delay.StartingGameDelayState;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VotingState extends EventState {
    
    private final VoteManager voteManager;
    private final Timer timer;
    
    public VotingState(@NotNull GameManager context, @NotNull ContextReference contextReference, @NotNull EventData eventData) {
        super(context, contextReference, eventData);
        List<GameType> votingPool = new ArrayList<>(VoteManager.votableGames());
        votingPool.removeAll(eventData.getPlayedGames());
        this.voteManager = new VoteManager(plugin, this::onVoteExecuted, votingPool, new HashSet<>(onlineParticipants.values()));
        this.timer = Timer.builder()
                .duration(eventData.getConfig().getVotingDuration())
                .withSidebar(sidebar, "timer")
                .sidebarPrefix(Component.text("Voting: "))
                .onCompletion(voteManager::executeVote)
                .onTogglePause((paused) -> {
                    if (paused) {
                        voteManager.pauseVote();
                    } else {
                        voteManager.resumeVote();
                    }
                })
                .build();
    }
    
    @Override
    public void enter() {
        context.getTimerManager().start(timer);
    }
    
    @Override
    public void exit() {
        voteManager.cancelVote();
        timer.cancel();
    }
    
    protected void onVoteExecuted(GameType gameType, String configFile) {
        timer.cancel();
        String chosenConfigFile = eventData.getConfig().getGameConfigs().getOrDefault(gameType, configFile);
        context.setState(new StartingGameDelayState(
                context, contextReference, eventData,
                gameType, chosenConfigFile));
    }
    
    @Override
    public CommandResult startGame(@NotNull Set<String> teamIds, @NotNull List<Player> gameAdmins, @NotNull GameType gameType, @NotNull String configFile) {
        voteManager.cancelVote();
        timer.cancel();
        context.setState(new StartingGameDelayState(context, contextReference, eventData, gameType, configFile));
        return CommandResult.success(Component.empty()
                .append(Component.text("Manually starting "))
                .append(Component.text(gameType.getTitle()))
                .append(Component.text(". Cancelling vote.")));
    }
    
    @Override
    public CommandResult addGameToVotingPool(@NotNull GameType gameToAdd) {
        return CommandResult.failure("Can't change the voting pool while voting.");
    }
    
    @Override
    public CommandResult removeGameFromVotingPool(@NotNull GameType gameToRemove) {
        return CommandResult.failure("Can't change the voting pool while voting.");
    }
    
    @Override
    public void onParticipantJoin(@NotNull MCTParticipant participant) {
        super.onParticipantJoin(participant);
        participant.teleport(config.getSpawn());
        voteManager.onParticipantJoin(participant);
    }
    
    @Override
    public void onParticipantQuit(@NotNull MCTParticipant participant) {
        voteManager.onParticipantQuit(participant);
        super.onParticipantQuit(participant);
    }
    
    @Override
    public void onParticipantInventoryClick(@NotNull InventoryClickEvent event, MCTParticipant participant) {
        if (voteManager.isInVoteGui(participant)) {
            return;
        }
        super.onParticipantInventoryClick(event, participant);
    }
}

package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.delay.StartingGameDelayState;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class VotingState extends EventState {
    
    private final VoteManager voteManager;
    private final Timer timer;
    
    public VotingState(@NotNull GameManager context, @NotNull ContextReference contextReference, @NotNull EventData eventData) {
        super(context, contextReference, eventData);
        List<GameType> votingPool = new ArrayList<>(VoteManager.votableGames());
        votingPool.removeAll(eventData.getPlayedGames());
        this.voteManager = new VoteManager(plugin, this::onVoteExecuted, votingPool, new HashSet<>(onlineParticipants.values()));
        this.timer = context.getTimerManager().start(Timer.builder()
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
                .build());
    }
    
    protected void onVoteExecuted(GameType gameType, String configFile) {
        String chosenConfigFile = eventData.getConfig().getGameConfigs().getOrDefault(gameType, configFile);
        context.setState(new StartingGameDelayState(
                context, contextReference, eventData,
                gameType, chosenConfigFile));
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        voteManager.cancelVote();
        timer.cancel();
    }
    
    @Override
    public void onSwitchMode() {
        voteManager.cancelVote();
        timer.cancel();
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
        voteManager.onParticipantJoin(participant);
    }
    
    @Override
    public void onParticipantQuit(@NotNull MCTParticipant participant) {
        voteManager.onParticipantQuit(participant);
        super.onParticipantQuit(participant);
    }
}

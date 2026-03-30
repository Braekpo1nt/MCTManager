package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.delay.StartingGameDelayState;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class VotingState extends EventState {
    
    private static final String VOTING_SOUND = "block.note_block.bit";
    private static final int VOTING_VOLUME = 50;
    private static final int VOTING_PITCH = 30;
    
    private final VoteManager voteManager;
    private final Timer timer;
    private @Nullable BukkitTask display;
    
    public VotingState(@NotNull GameManager context, @NotNull ContextReference contextReference, @NotNull EventData eventData) {
        super(context, contextReference, eventData);
        List<GameType> votingPool = getVotingPool();
        this.voteManager = new VoteManager(
                plugin,
                this::onVoteExecuted,
                votingPool,
                new HashSet<>(onlineParticipants.values()),
                eventData.getConfig().isWeightedVoting());
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
        if (display != null) {
            display.cancel();
        }
    }
    
    protected void onVoteExecuted(List<GameType> gameTypes, String configFile) {
        timer.cancel();
        if (eventData.getConfig().isWeightedVoting()) {
            Random random = new Random();
            scheduleNextDisplay(gameTypes, configFile, 5, random, true);
        } else {
            GameType gameType = gameTypes.getFirst();
            acceptVote(configFile, gameType);
        }
    }
    
    private void acceptVote(String configFile, GameType gameType) {
        String chosenConfigFile = eventData.getConfig().getGameConfigs().getOrDefault(gameType, configFile);
        context.setState(new StartingGameDelayState(
                context, contextReference, eventData,
                gameType, chosenConfigFile));
    }
    
    private void playVotingSound() {
        for (Participant participant : context.getOnlineParticipants()) {
            participant.playSound(participant.getLocation(), VOTING_SOUND, VOTING_VOLUME, VOTING_PITCH);
        }
        for (Player admin : onlineAdmins) {
            admin.playSound(admin.getLocation(), VOTING_SOUND, VOTING_VOLUME, VOTING_PITCH);
            
        }
    }
    
    public void scheduleNextDisplay(List<GameType> votes, String configFile, final long numberOfTicks, Random random, final boolean displayIsRed) {
        
        this.display = new BukkitRunnable() {
            @Override
            public void run() {
                boolean redTitle;
                int selectedInt;
                GameType gameType;
                long nextNumberOfTicks;
                if (votes.size() > 1) {
                    selectedInt = random.nextInt(votes.size());
                    gameType = votes.get(selectedInt);
                    votes.remove(selectedInt);
                    if (votes.size() > 6) {
                        nextNumberOfTicks = 5;
                    } else {
                        nextNumberOfTicks = switch (votes.size()) {
                            case 6 -> 7;
                            case 5 -> 9;
                            case 4 -> 13;
                            case 3 -> 16;
                            default -> 20;
                        };
                    }
                    if (displayIsRed) {
                        Audience.audience( // Use this for display, modify color
                                Audience.audience(onlineParticipants.values()),
                                Audience.audience(onlineAdmins)
                        ).showTitle(UIUtils.defaultTitle(
                                Component.empty()
                                        .append(Component.text(gameType.getTitle()))
                                        .color(NamedTextColor.RED),
                                Component.empty()
                        ));
                        playVotingSound();
                        redTitle = false;
                    } else {
                        Audience.audience( // Use this for display, modify color
                                Audience.audience(onlineParticipants.values()),
                                Audience.audience(onlineAdmins)
                        ).showTitle(UIUtils.defaultTitle(
                                Component.empty()
                                        .append(Component.text(gameType.getTitle()))
                                        .color(NamedTextColor.YELLOW),
                                Component.empty()
                        ));
                        playVotingSound();
                        redTitle = true;
                    }
                } else {
                    gameType = votes.getFirst();
                    Audience.audience( // Use this for display, modify color
                            Audience.audience(onlineParticipants.values()),
                            Audience.audience(onlineAdmins)
                    ).showTitle(UIUtils.defaultTitle(
                            Component.empty()
                                    .append(Component.text(gameType.getTitle()))
                                    .color(NamedTextColor.BLUE),
                            Component.empty()
                    ));
                    playVotingSound();
                    this.cancel();
                    acceptVote(configFile, gameType);
                    return;
                }
                if (!votes.isEmpty()) {
                    scheduleNextDisplay(votes, configFile, nextNumberOfTicks, random, redTitle);
                }
            }
        }.runTaskLater(plugin, numberOfTicks);
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

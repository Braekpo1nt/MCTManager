package org.braekpo1nt.mctmanager.games.event;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.enums.GameType;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class EventManager {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final VoteManager voteManager;
    private EventState currentState;
    private int maxGames = 6;
    private int currentGameNumber = 0;
    // Config stuff
    // Durations in seconds
    private final int WAITING_IN_HUB_DURATION = 20;
    private final int HALFTIME_BREAK_DURATION = 60;
    private final int VOTING_DURATION = 20;
    private final int STARTING_GAME_COUNT_DOWN_DURATION = 5;
    private final int BACK_TO_HUB_DELAY_DURATION = 10;
    // Task IDs
    private int waitingInHubTaskId;
    private int toColossalColosseumDelayTaskId;
    private int backToHubDelayTaskId;
    private int startingGameCountdownTaskId;
    private int halftimeBreakTaskId;
    private int votingTaskId;
    
    
    public EventManager(Main plugin, GameManager gameManager, VoteManager voteManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.voteManager = voteManager;
    }
    
    /**
     * Start a new event with the given number of games
     * @param sender the place to send errors and confirmations to
     * @param numberOfGames the number of games to be played in this event
     */
    public void startEvent(CommandSender sender, int numberOfGames) {
        maxGames = numberOfGames;
        currentGameNumber = 0;
        startWaitingInHub();
    }
    
    public void cancelAllTasks() {
        Bukkit.getScheduler().cancelTask(waitingInHubTaskId);
        Bukkit.getScheduler().cancelTask(toColossalColosseumDelayTaskId);
        Bukkit.getScheduler().cancelTask(backToHubDelayTaskId);
        Bukkit.getScheduler().cancelTask(startingGameCountdownTaskId);
        Bukkit.getScheduler().cancelTask(halftimeBreakTaskId);
        Bukkit.getScheduler().cancelTask(votingTaskId);
    }
    
    private void startWaitingInHub() {
        currentState = EventState.WAITING_IN_HUB;
        this.waitingInHubTaskId = new BukkitRunnable() {
            int count = WAITING_IN_HUB_DURATION;
            @Override
            public void run() {
                if (count <= 0) {
                    if (allGamesHaveBeenPlayed()) {
                        toColossalColosseumDelay();
                    } else {
                        startVoting();
                    }
                    this.cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startHalftimeBreak() {
        currentState = EventState.WAITING_IN_HUB;
        this.halftimeBreakTaskId = new BukkitRunnable() {
            int count = HALFTIME_BREAK_DURATION;
            @Override
            public void run() {
                if (count <= 0) {
                    startVoting();
                    this.cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startVoting() {
        currentState = EventState.VOTING;
        voteManager.startVote(gameManager.getOnlineParticipants(), null, this::startingGameDelay);
//        this.votingTaskId = new BukkitRunnable() {
//            int count = VOTING_DURATION;
//            @Override
//            public void run() {
//                if (count <= 0) {
//                    // get voted game
//                    startingGameDelay();
//                    this.cancel();
//                    return;
//                }
//                count--;
//            }
//        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startingGameDelay(GameType gameType) {
        currentState = EventState.DELAY;
        this.startingGameCountdownTaskId = new BukkitRunnable() {
            int count = STARTING_GAME_COUNT_DOWN_DURATION;
            @Override
            public void run() {
                if (count <= 0) {
                    currentState = EventState.PLAYING_GAME;
                    gameManager.startGame(gameType, Bukkit.getConsoleSender());
                    this.cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    public void gameIsOver() {
        currentState = EventState.DELAY;
        this.backToHubDelayTaskId = new BukkitRunnable() {
            int count = BACK_TO_HUB_DELAY_DURATION;
            @Override
            public void run() {
                if (count <= 0) {
                    if (isItHalfTime()) {
                        startHalftimeBreak();
                    } else {
                        startWaitingInHub();
                    }
                    this.cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void toColossalColosseumDelay() {
        currentState = EventState.DELAY;
        this.toColossalColosseumDelayTaskId = new BukkitRunnable() {
            int count = STARTING_GAME_COUNT_DOWN_DURATION;
            @Override
            public void run() {
                if (count <= 0) {
                    // start selected game
                    currentState = EventState.PLAYING_GAME;
                    startColossalColoseum();
                    this.cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    public boolean eventIsActive() {
        return currentState == null;
    }
    
    /**
     * Check if half the games have been played
     * @return true if the currentGameNumber-1 is half of the maxGames. False if it is lower or higher. 
     * If maxGames is odd, it must be the greater half (i.e. 2 is half of 3, 1 is not). 
     */
    public boolean isItHalfTime() {
        double half = maxGames / 2.0;
        return half <= currentGameNumber-1 && currentGameNumber-1 <= Math.ceil(half);
    }
    
    public boolean allGamesHaveBeenPlayed() {
        return currentGameNumber == maxGames - 1;
    }
}

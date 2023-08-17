package org.braekpo1nt.mctmanager.games.event;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class EventManager {
    
    private final Main plugin;
    private final GameManager gameManager;
    private EventState currentState;
    // Config stuff
    // Durations in seconds
    private final int WAITING_IN_HUB_DURATION = 20;
    private final int HALFTIME_BREAK_DURATION = 60;
    private final int VOTING_DURATION = 20;
    private final int STARTING_GAME_COUNT_DOWN_DURATION = 5;
    private final int BACK_TO_HUB_DELAY_DURATION = 10;
    
    
    public EventManager(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    /**
     * Start a new event with the given number of games
     * @param sender the place to send errors and confirmations to
     * @param numberOfGames the number of games to be played in this event
     */
    public void startEvent(CommandSender sender, int numberOfGames) {
        startWaitingInHub();
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
        // initialize vote
        this.votingTaskId = new BukkitRunnable() {
            int count = VOTING_DURATION;
            @Override
            public void run() {
                if (count <= 0) {
                    // get voted game
                    startingGameDelay();
                    this.cancel();
                    return;
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
    
    private void startingGameDelay() {
        currentState = EventState.DELAY;
        this.startingGameCountdownTaskId = new BukkitRunnable() {
            int count = STARTING_GAME_COUNT_DOWN_DURATION;
            @Override
            public void run() {
                if (count <= 0) {
                    // start selected game
                    currentState = EventState.PLAYING_GAME;
                    gameManager.startGame();
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
        
    }
}

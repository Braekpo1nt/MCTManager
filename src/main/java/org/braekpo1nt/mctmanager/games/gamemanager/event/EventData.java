package org.braekpo1nt.mctmanager.games.gamemanager.event;

import lombok.Getter;
import lombok.Setter;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * handles event management
 */
public class EventData {
    @Getter
    private final EventConfig config;
    @Getter
    @Setter
    private int currentGameNumber;
    @Getter
    @Setter
    private int maxGames;
    @Getter
    private final List<GameType> playedGames = new ArrayList<>();
    /**
     * contains the ScoreKeepers for the games played during the event. Cleared on start and end of event. 
     * <p>
     * If a given key doesn't exist, no score was kept for that game. 
     * <p>
     * If a given key does exist, it is pared with a list of ScoreKeepers which contain the scores
     * tracked for a given iteration of the game. Iterations are in order of play, first to last.
     * If a given iteration is null, then no points were tracked for that iteration. 
     * Otherwise, it contains the scores tracked for the given iteration. 
     */
    @Getter
    private final Map<GameType, List<ScoreKeeper>> scoreKeepers = new HashMap<>();
    private static final ItemStack CROWN = new ItemStack(Material.CARVED_PUMPKIN);
    @Getter
    @Setter
    private @Nullable MCTTeam winningTeam;
    
    static {
        CROWN.editMeta(meta -> meta.setCustomModelData(1));
    }
    
    public EventData(@NotNull EventConfig config, int startingGameNumber, int maxGames) {
        this.config = config;
        this.currentGameNumber = startingGameNumber;
        this.maxGames = maxGames;
    }
    
    public void cleanup() {
        playedGames.clear();
        scoreKeepers.clear();
        winningTeam = null;
    }
    
    // score tracking start
    public void trackScores(Map<String, Integer> teamScores, Map<UUID, Integer> participantScores, GameType gameType) {
        List<ScoreKeeper> gameScoreKeepers = scoreKeepers.getOrDefault(gameType, new ArrayList<>());
        gameScoreKeepers.add(new ScoreKeeper(teamScores, participantScores));
        scoreKeepers.put(gameType, gameScoreKeepers);
    }
    
    // score tracking stop
    
    public void giveCrown(Participant participant) {
        participant.getInventory().setHelmet(CROWN);
    }
    
    public void removeCrown(Participant participant) {
        ItemStack helmet = participant.getInventory().getHelmet();
        if (helmet != null && helmet.equals(CROWN)) {
            participant.getInventory().setHelmet(null);
        }
    }
    
    /**
     * The nth multiplier is used on the nth game in the event. If there are x multipliers, and we're on game z where z is greater than x, the xth multiplier is used.
     * @return a multiplier for the score based on the progression in the match.
     */
    public double getPointMultiplier() {
        if (currentGameNumber <= 0) {
            return 1;
        }
        double[] multipliers = config.getMultipliers();
        if (currentGameNumber > multipliers.length) {
            return multipliers[multipliers.length - 1];
        }
        return multipliers[currentGameNumber - 1];
    }
    
    /**
     * Check if half the games have been played
     * @return true if the currentGameNumber-1 is half of the maxGames. False if it is lower or higher. 
     * If maxGames is odd, it must be the greater half (i.e. 2 is half of 3, 1 is not). 
     */
    public boolean isItHalfTime() {
        if (maxGames == 1) {
            return false;
        }
        double half = maxGames / 2.0;
        return half <= currentGameNumber-1 && currentGameNumber-1 <= Math.ceil(half);
    }
    
    public boolean allGamesHaveBeenPlayed() {
        return currentGameNumber >= maxGames + 1;
    }
    
    public int getGameIterations(@NotNull GameType gameType) {
        List<ScoreKeeper> gameScoreKeepers = scoreKeepers.get(gameType);
        if (gameScoreKeepers == null) {
            return 0;
        }
        return gameScoreKeepers.size();
    }
}

package org.braekpo1nt.mctmanager.games.game.survivalgames;

import lombok.*;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
@Getter
@Setter
public class SurvivalGamesParticipant extends ParticipantData {
    
    private boolean alive;
    private int kills;
    private int deaths;
    /**
     * Used for when a participant is gliding from the sky after a respawn
     */
    private boolean shouldGlide;
    /**
     * True if the participant is in the process of respawning, false otherwise (dead or alive)
     */
    private boolean respawning;
    /**
     * The number of seconds left until the participant respawns
     */
    private int respawnCountdown;
    
    public SurvivalGamesParticipant(@NotNull Participant participant, int score) {
        super(participant, score);
        this.alive = true;
        this.kills = 0;
        this.deaths = 0;
        this.shouldGlide = false;
        this.respawning = false;
        this.respawnCountdown = 0;
    }
    
    public SurvivalGamesParticipant(@NotNull Participant participant, @NotNull SurvivalGamesParticipant.QuitData quitData) {
        super(participant, quitData.getScore());
        this.alive = quitData.isAlive();
        this.kills = quitData.getKills();
        this.deaths = quitData.getDeaths();
        this.shouldGlide = false;
        this.respawning = false;
        this.respawnCountdown = 0;
    }
    
    public QuitData getQuitData() {
        return new QuitData(
                getScore(),
                alive,
                kills,
                deaths
        );
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
        private final boolean alive;
        private final int kills;
        private final int deaths;
    }
}

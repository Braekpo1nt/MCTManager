package org.braekpo1nt.mctmanager.games.game.capturetheflag.match;

import io.papermc.paper.entity.LookAnchor;
import lombok.Data;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.MatchPairing;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

@Data
public class CaptureTheFlagMatch {
    
    
    private final CaptureTheFlagGame parentContext;
    /**
     * To be called when this match is over
     */
    private final Consumer<CaptureTheFlagMatch> matchIsOver;
    private final MatchPairing matchPairing;
    private final Arena arena;
    private final List<Player> northParticipants = new ArrayList<>();
    private final List<Player> southParticipants = new ArrayList<>();
    private final List<Player> allParticipants = new ArrayList<>();
    private final Map<UUID, Boolean> participantsAreAlive = new HashMap<>();
    private final GameManager gameManager;
    private final BattleTopbar topbar;
    
    public CaptureTheFlagMatch(CaptureTheFlagGame parentContext, Consumer<CaptureTheFlagMatch> matchIsOver, MatchPairing matchPairing, Arena arena) {
        this.parentContext = parentContext;
        this.matchIsOver = matchIsOver;
        this.matchPairing = matchPairing;
        this.arena = arena;
        this.gameManager = parentContext.getGameManager();
        this.topbar = parentContext.getTopbar();
    }
    
    public void start(List<Player> newParticipants) {
        for (Player participant : newParticipants) {
            initializeParticipant(participant);
        }
        throw new UnsupportedOperationException("not yet implemented");
    }
    
    private void initializeParticipant(Player participant) {
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        allParticipants.add(participant);
        UUID participantUniqueId = participant.getUniqueId();
        participantsAreAlive.put(participantUniqueId, true);
        int alive = 0;
        int dead = 0;
        if (matchPairing.northTeam().equals(teamId)) {
            northParticipants.add(participant);
            participant.teleport(arena.northSpawn());
            participant.setBedSpawnLocation(arena.northSpawn(), true);
            participant.lookAt(arena.southSpawn().getX(), arena.southSpawn().getY(), arena.southSpawn().getZ(), LookAnchor.EYES);
            alive = countAlive(northParticipants);
            dead = northParticipants.size() - alive;
        } else {
            southParticipants.add(participant);
            participant.teleport(arena.southSpawn());
            participant.setBedSpawnLocation(arena.southSpawn(), true);
            participant.lookAt(arena.northSpawn().getX(), arena.northSpawn().getY(), arena.northSpawn().getZ(), LookAnchor.EYES);
            alive = countAlive(southParticipants);
            dead = southParticipants.size() - alive;
        }
        topbar.setMembers(teamId, alive, dead);
        allParticipants.add(participant);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    private int countAlive(List<Player> participants) {
        int living = 0;
        for (Player participant : participants) {
            if (participantsAreAlive.get(participant.getUniqueId())) {
                living++;
            }
        }
        return living;
    }
    
}

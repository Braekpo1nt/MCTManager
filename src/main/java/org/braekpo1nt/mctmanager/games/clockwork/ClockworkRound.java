package org.braekpo1nt.mctmanager.games.clockwork;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.*;

public class ClockworkRound implements Listener {

    private final Main plugin;
    private final GameManager gameManager;
    private final Location startingPosition;
    private Map<UUID, Boolean> participantsAreAlive;
    private Map<String, Boolean> teamsAreAlive;
    private List<Player> participants;
    private static final String title = ChatColor.BLUE+"Clockwork";

    public ClockworkRound(Main plugin, GameManager gameManager, Location startingPosition) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.startingPosition = startingPosition;
    }
    
    public void start(List<Player> newParticipants) {
        participants = new ArrayList<>(newParticipants.size());
        participantsAreAlive = new HashMap<>(newParticipants.size());
        teamsAreAlive = new HashMap<>();
        List<String> teams = gameManager.getTeamNames(newParticipants);
        for (String team : teams) {
            teamsAreAlive.put(team, true);
        }
        String livingTeams = ""+teamsAreAlive.size();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player participant : newParticipants) {
            initializeParticipant(participant, livingTeams);
        }
    }

    private void initializeParticipant(Player participant, String livingTeams) {
        participants.add(participant);
        UUID participantUniqueId = participant.getUniqueId();
        participantsAreAlive.put(participantUniqueId, true);
        initializeFastBoard(participant, livingTeams);
        participant.teleport(startingPosition);
        participant.getInventory().clear();
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }

    private void initializeFastBoard(Player participant, String livingTeams) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                livingTeams// teams alive
        );
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                6,
                ""
        );
    }
    
    private void updateTeamsAliveFastBoard(Player participant, String livingTeams) {
        gameManager.getFastBoardManager().updateLine(
                participant.getUniqueId(),
                4,
                ""+livingTeams // teams alive
        );
    }

}

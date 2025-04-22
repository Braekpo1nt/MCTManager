package org.braekpo1nt.mctmanager.games.gamemanager.states;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MaintenanceState extends GameManagerState {
    
    
    public MaintenanceState(
            @NotNull GameManager context, 
            @NotNull Map<String, MCTTeam> teams, 
            @NotNull Map<UUID, MCTParticipant> onlineParticipants,
            @NotNull List<Player> onlineAdmins) {
        super(context, teams, onlineParticipants, onlineAdmins);
    }
}

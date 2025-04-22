package org.braekpo1nt.mctmanager.games.gamemanager.states;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventState extends GameManagerState {
    
    protected final @NotNull EventConfig config;
    
    public EventState(
            @NotNull GameManager context, 
            @NotNull Map<String, MCTTeam> teams, 
            @NotNull Map<UUID, MCTParticipant> onlineParticipants,
            @NotNull List<Player> onlineAdmins,
            @NotNull EventConfig config) {
        super(context, teams, onlineParticipants, onlineAdmins);
        this.config = config;
    }
    
    @Override
    public Sidebar createSidebar() {
        return context.getSidebarFactory().createSidebar(config.getTitle());
    }
}

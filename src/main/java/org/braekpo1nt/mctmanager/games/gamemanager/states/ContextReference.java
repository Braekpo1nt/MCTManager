package org.braekpo1nt.mctmanager.games.gamemanager.states;

import lombok.Builder;
import lombok.Data;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.hub.leaderboard.LeaderboardManager;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.braekpo1nt.mctmanager.ui.tablist.TabList;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Used to pass info from {@link org.braekpo1nt.mctmanager.games.GameManager} to {@link GameManagerState}
 * for convenience and safety. 
 */
@Data
@Builder
public class ContextReference {
    private final @NotNull TabList tabList;
    private final @NotNull Scoreboard mctScoreboard;
    private final @NotNull Map<GameType, MCTGame> activeGames;
    private final @NotNull Map<String, MCTTeam> teams;
    private final @NotNull Map<UUID, OfflineParticipant> allParticipants;
    private final @NotNull Map<UUID, MCTParticipant> onlineParticipants;
    private final @NotNull List<Player> onlineAdmins;
    private final @NotNull Main plugin;
    private final @NotNull GameStateStorageUtil gameStateStorageUtil;
    private final @NotNull SidebarFactory sidebarFactory;
    private final @NotNull HubConfig config;
    private final @NotNull List<LeaderboardManager> leaderboardManagers;
}

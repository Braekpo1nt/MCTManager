package org.braekpo1nt.mctmanager.games.gamemanager.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.tablist.TabList;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GameManagerState {
    
    protected final @NotNull GameManager context;
    protected final @NotNull TabList tabList;
    protected final Scoreboard mctScoreboard;
    
    protected final Map<String, MCTTeam> teams;
    protected final Map<UUID, MCTParticipant> onlineParticipants;
    protected final List<Player> onlineAdmins;
    
    public GameManagerState(
            @NotNull GameManager context,
            @NotNull Map<String, MCTTeam> teams,
            @NotNull Map<UUID, MCTParticipant> onlineParticipants,
            @NotNull List<Player> onlineAdmins) {
        this.context = context;
        this.tabList = context.getTabList();
        this.mctScoreboard = context.getMctScoreboard();
        
        this.teams = teams;
        this.onlineParticipants = onlineParticipants;
        this.onlineAdmins = onlineAdmins;
    }
    
    // leave/join start
    public void onAdminJoin(@NotNull PlayerJoinEvent event, @NotNull Player admin) {
        event.joinMessage(GameManagerUtils.replaceWithDisplayName(admin, event.joinMessage()));
        onAdminJoin(admin);
    }
    
    public void onAdminJoin(@NotNull Player admin) {
        context.getOnlineAdmins().add(admin);
        admin.setScoreboard(context.getMctScoreboard());
        admin.addPotionEffect(Main.NIGHT_VISION);
        Component displayName = Component.empty()
                .append(Component.text("[Admin]")
                        .color(GameManager.ADMIN_COLOR))
                .append(Component.text(admin.getName()));
        admin.displayName(displayName);
        admin.playerListName(displayName);
        tabList.showPlayer(admin);
    }
    
    public void onParticipantJoin(@NotNull PlayerJoinEvent event, @NotNull MCTParticipant participant) {
        event.joinMessage(GameManagerUtils.replaceWithDisplayName(participant, event.joinMessage()));
        onParticipantJoin(participant);
    }
    
    /**
     * Handles when a participant joins
     * @param participant the participant who joined
     */
    public void onParticipantJoin(@NotNull MCTParticipant participant) {
        onlineParticipants.put(participant.getUniqueId(), participant);
        MCTTeam team = teams.get(participant.getTeamId());
        team.joinOnlineMember(participant);
        participant.getPlayer().setScoreboard(mctScoreboard);
        participant.addPotionEffect(Main.NIGHT_VISION);
        Component displayName = Component.text(participant.getName(), team.getColor());
        participant.getPlayer().displayName(displayName);
        participant.getPlayer().playerListName(displayName);
        tabList.showPlayer(participant);
        tabList.setParticipantGrey(participant.getParticipantID(), false);
        ColorMap.colorLeatherArmor(participant, team.getBukkitColor());
        updateScoreVisuals(Collections.singletonList(team), Collections.singletonList(participant));
    }
    
    public void onAdminQuit(@NotNull PlayerQuitEvent event, @NotNull Player admin) {
        event.quitMessage(GameManagerUtils.replaceWithDisplayName(admin, event.quitMessage()));
        onAdminQuit(admin);
    }
    
    public void onAdminQuit(@NotNull Player admin) {
        onlineAdmins.remove(admin);
        tabList.hidePlayer(admin.getUniqueId());
        Component displayName = Component.text(admin.getName(), NamedTextColor.WHITE);
        admin.displayName(displayName);
        admin.playerListName(displayName);
    }
    
    public void onParticipantQuit(@NotNull PlayerQuitEvent event, @NotNull MCTParticipant participant) {
        event.quitMessage(GameManagerUtils.replaceWithDisplayName(participant, event.quitMessage()));
        tabList.hidePlayer(participant);
    }
    
    /**
     * Handles when a participant leaves the event.
     * Should be called when a participant disconnects (quits/leaves) from the server 
     * (see {@link GameManager#onPlayerQuit(PlayerQuitEvent)}),
     * or when they are removed from the participants list
     * @param participant The participant who left the event
     * @see GameManager#leaveParticipant(CommandSender, OfflineParticipant)
     */
    public void onParticipantQuit(@NotNull MCTParticipant participant) {
        if (participant.getCurrentGame() != null) {
            MCTGame activeGame = context.getActiveGame(participant.getCurrentGame());
            if (activeGame != null) {
                activeGame.onParticipantQuit(participant.getUniqueId());
            }
        }
        MCTTeam team = teams.get(participant.getTeamId());
        team.quitOnlineMember(participant.getUniqueId());
        onlineParticipants.remove(participant.getUniqueId());
        tabList.hidePlayer(participant.getUniqueId());
        Component displayName = Component.text(participant.getName(),
                NamedTextColor.WHITE);
        participant.getPlayer().displayName(displayName);
        participant.getPlayer().playerListName(displayName);
        GameManagerUtils.deColorLeatherArmor(participant.getInventory());
        tabList.setParticipantGrey(participant.getParticipantID(), true);
    }
    // leave/join end
    
    // ui start
    public void updateScoreVisuals(Collection<MCTTeam> mctTeams, Collection<MCTParticipant> mctParticipants) {
        tabList.setScores(mctTeams);
    }
    
    /**
     * @return a new sidebar
     */
    public Sidebar createSidebar() {
        return context.getSidebarFactory().createSidebar();
    }
    // ui end
}

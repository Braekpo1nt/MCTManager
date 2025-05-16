package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.base.GameBase;
import org.braekpo1nt.mctmanager.games.base.listeners.PreventItemDrop;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfig;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.states.CaptureTheFlagState;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.states.InitialState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
public class CaptureTheFlagGame extends GameBase<CTFParticipant, CTFTeam, CTFParticipant.QuitData, CTFTeam.QuitData, CaptureTheFlagState> {
    
    private final BattleTopbar topbar;
    private final RoundManager roundManager;
    private final CaptureTheFlagConfig config;
    
    private final Map<String, CTFTeam> quitTeams = new HashMap<>();
    
    public CaptureTheFlagGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager, 
            @NotNull Component title, 
            @NotNull CaptureTheFlagConfig config, 
            @NotNull Collection<Team> newTeams, 
            @NotNull Collection<Participant> newParticipants, 
            @NotNull List<Player> newAdmins) {
        super(GameType.CAPTURE_THE_FLAG, plugin, gameManager, title, new InitialState());
        this.config = config;
        this.topbar = addUIManager(new BattleTopbar());
        Set<String> teamIds = Participant.getTeamIds(newParticipants);
        roundManager = new RoundManager(teamIds, config.getArenas().size());
        addListener(new PreventItemDrop<>(this, true));
        setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        start(newTeams, newParticipants, newAdmins);
        updateRoundLine();
        Main.logger().info("Starting Capture the Flag");
    }
    
    @Override
    protected @NotNull World getWorld() {
        return config.getWorld();
    }
    
    @Override
    protected @NotNull CaptureTheFlagState getStartState() {
        return new DescriptionState(this);
    }
    
    @Override
    protected @NotNull CTFParticipant createParticipant(Participant participant) {
        return new CTFParticipant(participant, 0, 0, 0);
    }
    
    @Override
    protected @NotNull CTFParticipant createParticipant(Participant participant, CTFParticipant.QuitData quitData) {
        return new CTFParticipant(participant, quitData);
    }
    
    @Override
    protected @NotNull CTFParticipant.QuitData getQuitData(CTFParticipant participant) {
        return participant.getQuitData();
    }
    
    @Override
    protected void initializeParticipant(CTFParticipant participant, CTFTeam team) {
        topbar.setKillsAndDeaths(participant.getUniqueId(), 0, 0);
        participant.teleport(config.getSpawnObservatory());
    }
    
    @Override
    protected void initializeTeam(CTFTeam team) {
        
    }
    
    @Override
    protected @NotNull CTFTeam createTeam(Team team) {
        return new CTFTeam(team, 0);
    }
    
    @Override
    protected @NotNull CTFTeam createTeam(Team team, CTFTeam.QuitData quitData) {
        return new CTFTeam(team, quitData);
    }
    
    @Override
    protected @NotNull CTFTeam.QuitData getQuitData(CTFTeam team) {
        return team.getQuitData();
    }
    
    /**
     * @param teamId the teamId of the team which might have quit, or might be online
     * @return the team with the given id if they are online or if they quit
     * @throws IllegalStateException if no team with the given teamId ever joined
     */
    public @NotNull CTFTeam getTeamOrQuitTeam(@NotNull String teamId) {
        CTFTeam ctfTeam = teams.get(teamId);
        if (ctfTeam != null) {
            return ctfTeam;
        }
        CTFTeam quitTeam = quitTeams.get(teamId);
        if (quitTeam != null) {
            return quitTeam;
        }
        throw new IllegalStateException(String.format("Attempted to access a team which never joined (id %s", teamId));
    }
    
    @Override
    protected void resetParticipant(CTFParticipant participant, CTFTeam team) {
        
    }
    
    @Override
    protected void setupTeamOptions(org.bukkit.scoreboard.@NotNull Team scoreboardTeam, @NotNull CTFTeam team) {
        scoreboardTeam.setAllowFriendlyFire(false);
        scoreboardTeam.setCanSeeFriendlyInvisibles(true);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
    }
    
    @Override
    protected void initializeAdmin(Player admin) {
        admin.teleport(config.getSpawnObservatory());
    }
    
    @Override
    protected void cleanup() {
        
    }
    
    @Override
    protected void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("round", "")
        );
    }
    
    @Override
    protected @Nullable SpectatorBoundary getSpectatorBoundary() {
        return config.getSpectatorBoundary();
    }
    
    @Override
    protected boolean shouldPreventInteractions(@NotNull Material type) {
        return config.getPreventInteractions().contains(type);
    }
    
    /**
     * Updates the round line of the sidebar for the given player
     * to reflect the current round and number of total rounds
     * @param uuid the UUID of the participant/admin to update the round line for
     */
    public void updateRoundLine(UUID uuid) {
        Component roundLine = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(roundManager.getPlayedRounds() + 1))
                .append(Component.text("/"))
                .append(Component.text(roundManager.getMaxRounds()))
                ;
        sidebar.updateLine(uuid, "round", roundLine);
        adminSidebar.updateLine("round", roundLine);
    }
    
    /**
     * Updates the round line of the sidebar for all participants and admins
     * to reflect the current round and number of total rounds
     */
    public void updateRoundLine() {
        Component roundLine = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(roundManager.getPlayedRounds() + 1))
                .append(Component.text("/"))
                .append(Component.text(roundManager.getMaxRounds()))
                ;
        sidebar.updateLine("round", roundLine);
        adminSidebar.updateLine("round", roundLine);
    }
    
    @Override
    protected void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("round", ""),
                new KeyLine("timer", "")
        );
    }
    
    @Override
    protected void resetAdmin(Player admin) {
        
    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        CTFParticipant participant = participants.get(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantFoodLevelChange(event, participant);
    }
    
}

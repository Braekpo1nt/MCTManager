package org.braekpo1nt.mctmanager.games.game.spleef;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.base.GameBase;
import org.braekpo1nt.mctmanager.games.base.listeners.PreventHungerLoss;
import org.braekpo1nt.mctmanager.games.base.listeners.PreventItemDrop;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.spleef.config.SpleefConfig;
import org.braekpo1nt.mctmanager.games.game.spleef.state.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.spleef.state.InitialState;
import org.braekpo1nt.mctmanager.games.game.spleef.state.SpleefState;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.structure.Structure;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Random;

@Getter
@Setter
public class SpleefGame extends GameBase<SpleefParticipant, SpleefTeam, SpleefParticipant.QuitData, SpleefTeam.QuitData, SpleefState> {
    
    private final @NotNull SpleefConfig config;
    private final Random random = new Random();
    
    private int currentRound;
    
    public SpleefGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            int gameSessionId,
            @NotNull SpleefConfig config,
            @NotNull String configFile,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(gameSessionId, new GameInstanceId(GameType.SPLEEF, configFile), plugin, gameManager, title, new InitialState());
        this.config = config;
        this.currentRound = 1;
        placeLayers(true);
        addListener(new PreventHungerLoss<>(this));
        addListener(new PreventItemDrop<>(this, true));
        setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        start(newTeams, newParticipants, newAdmins);
        Main.logger().info("Started Spleef");
    }
    
    @Override
    protected @NotNull SpleefState getStartState() {
        return new DescriptionState(this);
    }
    
    @Override
    protected @NotNull World getWorld() {
        return config.getWorld();
    }
    
    public void placeLayers(boolean replaceStencil) {
        for (int i = 0; i < config.getStructures().size(); i++) {
            Structure layer = config.getStructures().get(i);
            layer.place(config.getStructureOrigins().get(i), true, StructureRotation.NONE, Mirror.NONE, 0, 1, random);
        }
        if (replaceStencil && config.getStencilBlock() != null) {
            for (BoundingBox layerArea : config.getDecayLayers()) {
                BlockPlacementUtils.createCubeReplace(config.getWorld(), layerArea, config.getStencilBlock(), config.getLayerBlock());
            }
        }
    }
    
    @Override
    protected void cleanup() {
        placeLayers(false);
    }
    
    @Override
    protected @NotNull SpleefParticipant createParticipant(Participant participant) {
        return new SpleefParticipant(participant, 0, true);
    }
    
    @Override
    protected @NotNull SpleefParticipant createParticipant(Participant participant, SpleefParticipant.QuitData quitData) {
        return new SpleefParticipant(participant, quitData.getScore(), true);
    }
    
    @Override
    protected @NotNull SpleefParticipant.QuitData getQuitData(SpleefParticipant participant) {
        return participant.getQuitData();
    }
    
    @Override
    protected void initializeParticipant(SpleefParticipant participant, SpleefTeam team) {
        teleportToRandomStartingPosition(participant);
    }
    
    public void teleportToRandomStartingPosition(Participant participant) {
        Location location = getRandomStartingPosition();
        participant.teleport(location);
        participant.setRespawnLocation(location, true);
    }
    
    public Location getRandomStartingPosition() {
        int index = random.nextInt(config.getStartingLocations().size());
        return config.getStartingLocations().get(index);
    }
    
    @Override
    protected void initializeTeam(SpleefTeam team) {
        
    }
    
    @Override
    protected @NotNull SpleefTeam createTeam(Team team) {
        return new SpleefTeam(team, 0);
    }
    
    @Override
    protected @NotNull SpleefTeam createTeam(Team team, SpleefTeam.QuitData quitData) {
        return new SpleefTeam(team, quitData.getScore());
    }
    
    @Override
    protected @NotNull SpleefTeam.QuitData getQuitData(SpleefTeam team) {
        return team.getQuitData();
    }
    
    @Override
    protected void resetParticipant(SpleefParticipant participant, SpleefTeam team) {
        
    }
    
    @Override
    protected void initializeAdmin(Player admin) {
        admin.teleport(config.getStartingLocations().getFirst());
    }
    
    @Override
    protected void resetAdmin(Player admin) {
        
    }
    
    @Override
    protected void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("round", Component.empty()
                        .append(Component.text("Round 1/"))
                        .append(Component.text(config.getRounds()))),
                new KeyLine("timer", ""),
                new KeyLine("alive", Component.empty())
        );
    }
    
    @Override
    protected void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("round", Component.empty()
                        .append(Component.text("Round 1/"))
                        .append(Component.text(config.getRounds()))),
                new KeyLine("timer", ""),
                new KeyLine("alive", Component.empty())
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
    
    @Override
    protected void setupTeamOptions(org.bukkit.scoreboard.@NotNull Team scoreboardTeam, @NotNull SpleefTeam team) {
        scoreboardTeam.setAllowFriendlyFire(false);
        scoreboardTeam.setCanSeeFriendlyInvisibles(true);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        SpleefParticipant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        state.onParticipantBreakBlock(event, participant);
    }
}

package org.braekpo1nt.mctmanager.games.game.finalgame;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.base.DuoGameBase;
import org.braekpo1nt.mctmanager.games.base.WandsDuoGameBase;
import org.braekpo1nt.mctmanager.games.base.listeners.PreventHungerLossSpecific;
import org.braekpo1nt.mctmanager.games.base.listeners.PreventItemDrop;
import org.braekpo1nt.mctmanager.games.base.listeners.PreventPickupArrow;
import org.braekpo1nt.mctmanager.games.editor.wand.Wand;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.finalgame.config.FinalConfig;
import org.braekpo1nt.mctmanager.games.game.finalgame.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.finalgame.states.FinalState;
import org.braekpo1nt.mctmanager.games.game.finalgame.states.InitialState;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class FinalGame extends WandsDuoGameBase<FinalParticipant, FinalTeam, FinalParticipant.QuitData, FinalTeam.QuitData, FinalState> {
    
    private final @NotNull FinalConfig config;
    private final @NotNull BattleTopbar topbar;
    private int currentRound;
    
    public FinalGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull FinalConfig config,
            @NotNull String configFile,
            @NotNull Team newNorth,
            @NotNull Team newSouth,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins
    ) {
        super(
                new GameInstanceId(GameType.COLOSSAL_COMBAT, configFile),
                plugin,
                gameManager,
                title,
                new InitialState(),
                new FinalTeam(newNorth, Affiliation.NORTH, 0),
                new FinalTeam(newSouth, Affiliation.SOUTH, 0)
        );
        this.config = config;
        this.topbar = addUIManager(new BattleTopbar());
        this.currentRound = 1;
        setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        addListener(new PreventItemDrop<>(this, true));
        addListener(new PreventPickupArrow<>(this));
        addListener(new PreventHungerLossSpecific<>(this, participant -> participant.getAffiliation().equals(Affiliation.SPECTATOR)));
        topbar.addTeam(this.northTeam.getTeamId(), this.northTeam.getColor());
        topbar.addTeam(this.southTeam.getTeamId(), this.southTeam.getColor());
        topbar.linkTeamPair(this.northTeam.getTeamId(), this.northTeam.getTeamId());
        addWand(Wand.<FinalParticipant>builder()
                .wandItem(Wand.createWandItem(
                        Material.NETHER_STAR,
                        "Kit Picker",
                        List.of(Component.text("Click to select a kit"))
                ))
                .onRightClick((event, participant) -> {
                    state.onOpenKitPicker(participant);
                    return CommandResult.success();
                })
                .shouldNotDrop(true)
                .build());
        start(newTeams, newParticipants, newAdmins);
        updateAliveStatus(Affiliation.NORTH);
        updateAliveStatus(Affiliation.SOUTH);
    }
    
    /**
     * Update the {@link #topbar} with the alive status of the given affiliation
     * @param affiliation the affiliation to update the alive status for
     */
    public void updateAliveStatus(Affiliation affiliation) {
        switch (affiliation) {
            case NORTH -> {
                int alive = northTeam.getAlive();
                int dead = northTeam.size() - alive;
                topbar.setMembers(northTeam.getTeamId(), alive, dead);
            }
            case SOUTH -> {
                int alive = southTeam.getAlive();
                int dead = southTeam.size() - alive;
                topbar.setMembers(southTeam.getTeamId(), alive, dead);
            }
        }
    }
    
    public void addKill(@NotNull FinalParticipant participant) {
        int oldKillCount = participant.getKills();
        int newKillCount = oldKillCount + 1;
        participant.setKills(newKillCount);
        topbar.setKills(participant.getUniqueId(), newKillCount);
    }
    
    public void addDeath(@NotNull FinalParticipant participant) {
        int oldDeathCount = participant.getDeaths();
        int newDeathCount = oldDeathCount + 1;
        participant.setDeaths(newDeathCount);
        topbar.setDeaths(participant.getUniqueId(), newDeathCount);
    }
    
    @Override
    protected @NotNull World getWorld() {
        return config.getWorld();
    }
    
    @Override
    protected @NotNull FinalState getStartState() {
        return new DescriptionState(this);
    }
    
    @Override
    protected void cleanup() {
        
    }
    
    @Override
    protected @NotNull FinalParticipant createParticipant(Participant participant) {
        Affiliation affiliation = getAffiliation(participant.getTeamId());
        return new FinalParticipant(participant, affiliation, true, 0, 0, 0);
    }
    
    @Override
    protected @NotNull FinalParticipant createParticipant(Participant participant, FinalParticipant.QuitData quitData) {
        return new FinalParticipant(participant, true, quitData);
    }
    
    @Override
    protected @NotNull FinalParticipant.QuitData getQuitData(FinalParticipant participant) {
        return participant.getQuitData();
    }
    
    @Override
    protected void initializeParticipant(FinalParticipant participant, FinalTeam team) {
        switch (participant.getAffiliation()) {
            case NORTH -> {
                topbar.setKillsAndDeaths(participant.getUniqueId(), 0, 0);
                topbar.linkToTeam(participant.getUniqueId(), participant.getTeamId());
                participant.teleport(config.getNorthMap().getSpawn());
            }
            case SOUTH -> {
                topbar.setKillsAndDeaths(participant.getUniqueId(), 0, 0);
                topbar.linkToTeam(participant.getUniqueId(), participant.getTeamId());
                participant.teleport(config.getSouthMap().getSpawn());
            }
            case SPECTATOR -> participant.teleport(config.getSpectatorSpawn());
        }
    }
    
    @Override
    protected void initializeTeam(FinalTeam team) {
        
    }
    
    @Override
    protected @NotNull FinalTeam createTeam(Team team) {
        Affiliation affiliation = getAffiliation(team.getTeamId());
        return new FinalTeam(team, affiliation, 0);
    }
    
    @Override
    protected @NotNull FinalTeam createTeam(Team team, FinalTeam.QuitData quitData) {
        return new FinalTeam(team, quitData);
    }
    
    @Override
    protected @NotNull FinalTeam.QuitData getQuitData(FinalTeam team) {
        return team.getQuitData();
    }
    
    @Override
    protected void resetParticipant(FinalParticipant participant, FinalTeam team) {
        
    }
    
    @Override
    protected void setupTeamOptions(org.bukkit.scoreboard.@NotNull Team scoreboardTeam, @NotNull FinalTeam team) {
        scoreboardTeam.setAllowFriendlyFire(false);
        scoreboardTeam.setCanSeeFriendlyInvisibles(true);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
    }
    
    @Override
    protected void initializeAdmin(Player admin) {
        admin.teleport(config.getSpectatorSpawn());
    }
    
    @Override
    protected void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("timer", Component.empty()),
                new KeyLine("northWinCount", Component.empty()
                        .append(northTeam.getFormattedDisplayName())
                        .append(Component.text(": 0/"))
                        .append(Component.text(config.getRequiredWins()))),
                new KeyLine("southWinCount", Component.empty()
                        .append(southTeam.getFormattedDisplayName())
                        .append(Component.text(": 0/"))
                        .append(Component.text(config.getRequiredWins()))),
                new KeyLine("round", Component.text("Round: 1"))
        );
    }
    
    public void updateRoundSidebar(@NotNull FinalParticipant participant) {
        sidebar.updateLines(participant.getUniqueId(),
                new KeyLine("northWinCount", toWinCountLine(northTeam)),
                new KeyLine("southWinCount", toWinCountLine(southTeam)),
                new KeyLine("round", Component.empty()
                        .append(Component.text("Round: "))
                        .append(Component.text(this.currentRound)))
        );
    }
    
    public void updateRoundSidebar() {
        Component northLine = toWinCountLine(northTeam);
        Component southLine = toWinCountLine(southTeam);
        Component roundLine = Component.empty()
                .append(Component.text("Round: "))
                .append(Component.text(currentRound));
        sidebar.updateLines(
                new KeyLine("northWinCount", northLine),
                new KeyLine("southWinCount", southLine),
                new KeyLine("round", roundLine)
        );
        adminSidebar.updateLines(
                new KeyLine("northWinCount", northLine),
                new KeyLine("southWinCount", southLine),
                new KeyLine("round", roundLine)
        );
    }
    
    private @NotNull Component toWinCountLine(FinalTeam team) {
        return Component.empty()
                .append(team.getFormattedDisplayName())
                .append(Component.text(": "))
                .append(Component.text(team.getWins()))
                .append(Component.text("/"))
                .append(Component.text(config.getRequiredWins()));
    }
    
    @Override
    protected void resetAdmin(Player admin) {
        
    }
    
    @Override
    protected void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("northWinCount", Component.empty()
                        .append(northTeam.getFormattedDisplayName())
                        .append(Component.text(": 0/"))
                        .append(Component.text(config.getRequiredWins()))),
                new KeyLine("southWinCount", Component.empty()
                        .append(southTeam.getFormattedDisplayName())
                        .append(Component.text(": 0/"))
                        .append(Component.text(config.getRequiredWins()))),
                new KeyLine("round", Component.text("Round: 1"))
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
}

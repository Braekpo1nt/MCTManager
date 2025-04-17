package org.braekpo1nt.mctmanager.games.game.colossalcombat;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.experimental.Affiliation;
import org.braekpo1nt.mctmanager.games.experimental.DuoGameBase;
import org.braekpo1nt.mctmanager.games.experimental.PreventItemDrop;
import org.braekpo1nt.mctmanager.games.experimental.PreventPickupArrow;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.config.ColossalCombatConfig;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.states.ColossalCombatState;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.states.InitialState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.topbar.BattleTopbar;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class ColossalCombatGame extends DuoGameBase<ColossalParticipant, ColossalTeam, ColossalParticipant.QuitData, ColossalTeam.QuitData, ColossalCombatState> {
    
    private final @NotNull ColossalCombatConfig config;
    private final @NotNull BattleTopbar topbar;
    
    private int currentRound;
    
    public ColossalCombatGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull ColossalCombatConfig config,
            @NotNull Team newNorth,
            @NotNull Team newSouth,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(
                GameType.FINAL, 
                plugin, 
                gameManager, 
                title, 
                new InitialState(), 
                new ColossalTeam(newNorth, 0, Affiliation.NORTH), 
                new ColossalTeam(newSouth, 0, Affiliation.SOUTH));
        this.config = config;
        this.topbar = addUIManager(new BattleTopbar());
        this.currentRound = 1;
        setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        addListener(new PreventItemDrop<>(this, true));
        addListener(new PreventPickupArrow<>(this));
        topbar.addTeam(northTeam.getTeamId(), northTeam.getColor());
        topbar.addTeam(southTeam.getTeamId(), southTeam.getColor());
        topbar.linkTeamPair(northTeam.getTeamId(), southTeam.getTeamId());
        start(newTeams, newParticipants, newAdmins);
        updateAliveStatus(Affiliation.NORTH);
        updateAliveStatus(Affiliation.SOUTH);
    }
    
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
    
    public void addKill(@NotNull ColossalParticipant participant) {
        int oldKillCount = participant.getKills();
        int newKillCount = oldKillCount + 1;
        participant.setKills(newKillCount);
        topbar.setKills(participant.getUniqueId(), newKillCount);
    }
    
    public void addDeath(@NotNull ColossalParticipant participant) {
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
    protected @NotNull ColossalCombatState getStartState() {
        return new DescriptionState(this);
    }
    
    @Override
    protected void cleanup() {
        
    }
    
    @Override
    protected @NotNull ColossalParticipant createParticipant(Participant participant) {
        Affiliation affiliation = getAffiliation(participant.getTeamId());
        return new ColossalParticipant(participant, affiliation, true, 0, 0, 0);
    }
    
    @Override
    protected @NotNull ColossalParticipant createParticipant(Participant participant, ColossalParticipant.QuitData quitData) {
        return new ColossalParticipant(participant, true, quitData);
    }
    
    @Override
    protected @NotNull ColossalParticipant.QuitData getQuitData(ColossalParticipant participant) {
        return participant.getQuitData();
    }
    
    @Override
    protected void initializeParticipant(ColossalParticipant participant, ColossalTeam team) {
        switch (participant.getAffiliation()) {
            case NORTH -> {
                topbar.setKillsAndDeaths(participant.getUniqueId(), 0, 0);
                topbar.linkToTeam(participant.getUniqueId(), participant.getTeamId());
                participant.teleport(config.getNorthGate().getSpawn());
            }
            case SOUTH -> {
                topbar.setKillsAndDeaths(participant.getUniqueId(), 0, 0);
                topbar.linkToTeam(participant.getUniqueId(), participant.getTeamId());
                participant.teleport(config.getSouthGate().getSpawn());
            }
            case SPECTATOR -> participant.teleport(config.getSpectatorSpawn());
        }
    }
    
    @Override
    protected void initializeTeam(ColossalTeam team) {
        
    }
    
    @Override
    protected @NotNull ColossalTeam createTeam(Team team) {
        Affiliation affiliation = getAffiliation(team.getTeamId());
        return new ColossalTeam(team, 0, affiliation);
    }
    
    @Override
    protected @NotNull ColossalTeam createTeam(Team team, ColossalTeam.QuitData quitData) {
        return new ColossalTeam(team, quitData);
    }
    
    @Override
    protected @NotNull ColossalTeam.QuitData getQuitData(ColossalTeam team) {
        return team.getQuitData();
    }
    
    @Override
    protected void resetParticipant(ColossalParticipant participant, ColossalTeam team) {
        
    }
    
    @Override
    protected void setupTeamOptions(org.bukkit.scoreboard.@NotNull Team scoreboardTeam, @NotNull ColossalTeam team) {
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
    
    @Override
    protected void resetAdmin(Player admin) {
        
    }
    
    public void updateRoundSidebar(@NotNull Player admin) {
        adminSidebar.updateLines(admin.getUniqueId(),
                new KeyLine("northWinCount", toWinCountLine(northTeam)),
                new KeyLine("southWinCount", toWinCountLine(southTeam)),
                new KeyLine("round", Component.empty()
                        .append(Component.text("Round: "))
                        .append(Component.text(currentRound)))
        );
    }
    
    public void updateRoundSidebar(@NotNull ColossalParticipant participant) {
        sidebar.updateLines(participant.getUniqueId(), 
                new KeyLine("northWinCount", toWinCountLine(northTeam)),
                new KeyLine("southWinCount", toWinCountLine(southTeam)),
                new KeyLine("round", Component.empty()
                        .append(Component.text("Round: "))
                        .append(Component.text(currentRound)))
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
    
    private @NotNull TextComponent toWinCountLine(ColossalTeam team) {
        return Component.empty()
                .append(team.getFormattedDisplayName())
                .append(Component.text(": "))
                .append(Component.text(team.getWins()))
                .append(Component.text("/"))
                .append(Component.text(config.getRequiredWins()));
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
    
    public void giveLoadout(ColossalParticipant participant) {
        participant.getInventory().setContents(config.getLoadout());
        GameManagerUtils.colorLeatherArmor(gameManager, participant);
    }
    
    public void closeGates() {
        closeGate(config.getNorthGate(), gameManager.getTeamPowderColor(northTeam.getTeamId()));
        closeGate(config.getSouthGate(), gameManager.getTeamPowderColor(southTeam.getTeamId()));
        placeConcrete();
    }
    
    private void placeConcrete() {
        if (config.shouldReplaceWithConcrete()) {
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getNorthFlagReplaceArea(),
                    config.getReplaceBlock(),
                    gameManager.getTeamConcreteColor(northTeam.getTeamId()));
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getSouthFlagReplaceArea(),
                    config.getReplaceBlock(),
                    gameManager.getTeamConcreteColor(southTeam.getTeamId()));
        }
    }
    
    public void resetArena() {
        // remove items/arrows on the ground
        BoundingBox removeArea = config.getRemoveArea();
        for (Arrow arrow : config.getWorld().getEntitiesByClass(Arrow.class)) {
            if (removeArea.contains(arrow.getLocation().toVector())) {
                arrow.remove();
            }
        }
        for (Item item : config.getWorld().getEntitiesByClass(Item.class)) {
            if (removeArea.contains(item.getLocation().toVector())) {
                item.remove();
            }
        }
        removeConcrete();
    }
    
    public void removeConcrete() {
        if (config.shouldReplaceWithConcrete()) {
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getNorthFlagReplaceArea(),
                    gameManager.getTeamConcreteColor(northTeam.getTeamId()),
                    config.getReplaceBlock());
            BlockPlacementUtils.createCubeReplace(
                    config.getWorld(),
                    config.getSouthFlagReplaceArea(),
                    gameManager.getTeamConcreteColor(southTeam.getTeamId()),
                    config.getReplaceBlock());
        }
    }
    
    private void closeGate(Gate gate, Material teamPowderColor) {
        //replace powder with air
        for (Material powderColor : ColorMap.getAllConcretePowderColors()) {
            BlockPlacementUtils.createCubeReplace(config.getWorld(), gate.getClearArea(), powderColor, Material.AIR);
        }
        //place stone under the powder area
        BlockPlacementUtils.createCube(config.getWorld(), gate.getStone(), Material.STONE);
        //replace air with team powder color
        BlockPlacementUtils.createCubeReplace(config.getWorld(), gate.getPlaceArea(), Material.AIR, teamPowderColor);
    }
    
    public void openGates() {
        BlockPlacementUtils.createCube(config.getWorld(), config.getNorthGate().getStone(), Material.AIR);
        BlockPlacementUtils.createCube(config.getWorld(), config.getSouthGate().getStone(), Material.AIR);
    }
}

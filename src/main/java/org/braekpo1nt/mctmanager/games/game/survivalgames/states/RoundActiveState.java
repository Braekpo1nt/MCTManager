package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.survivalgames.BorderStage;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.games.game.survivalgames.config.SurvivalGamesConfig;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.ui.topbar.ManyBattleTopbar;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public abstract class RoundActiveState extends SurvivalGamesStateBase {
    
    protected final Main plugin;
    protected final TimerManager timerManager;
    protected final SurvivalGamesConfig config;
    protected final Sidebar adminSidebar;
    protected final ManyBattleTopbar topbar;
    protected final WorldBorder worldBorder;
    private int respawnTaskId;
    
    public RoundActiveState(@NotNull SurvivalGamesGame context) {
        super(context);
        this.plugin = context.getPlugin();
        this.timerManager = context.getTimerManager();
        this.config = context.getConfig();
        this.adminSidebar = context.getAdminSidebar();
        this.topbar = context.getTopbar();
        this.worldBorder = context.getWorldBorder();
    }
    
    @Override
    public void enter() {
        this.respawnTaskId = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            context.getParticipants().values()
                    .forEach(p -> {
                        handleRespawning(p);
                        handleRespawnGracePeriod(p);
                    });
        }, 0L, 20L).getTaskId();
    }
    
    public void handleRespawning(SurvivalGamesParticipant p) {
        if (!p.isRespawning()) {
            return;
        }
        if (p.isAlive()) {
            return;
        }
        if (p.getRespawnCountdown() == 0) {
            p.showTitle(UIUtils.EMPTY_TITLE);
            respawnParticipant(p);
            return;
        }
        p.showTitle(UIUtils.defaultTitle(
                Component.empty()
                        .append(Component.text("Respawning in")),
                Component.empty()
                        .append(Component.text(p.getRespawnCountdown()))
                        .color(TimeStringUtils.getColorForTime(p.getRespawnCountdown()))
        ));
        p.setRespawnCountdown(p.getRespawnCountdown() - 1);
    }
    
    public void handleRespawnGracePeriod(SurvivalGamesParticipant p) {
        if (p.isRespawning()) {
            return;
        }
        if (!p.isAlive()) {
            return;
        }
        if (p.getRespawnGracePeriodCountdown() == 0) {
            p.showTitle(UIUtils.EMPTY_TITLE);
            // end the grace period
            return;
        }
        p.showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.empty()
                        .append(Component.text("Grace Period: "))
                        .append(Component.empty()
                                .append(Component.text(p
                                        .getRespawnGracePeriodCountdown()))
                                .color(TimeStringUtils.getColorForTime(p
                                        .getRespawnGracePeriodCountdown())))
        ));
        p.setRespawnGracePeriodCountdown(p.getRespawnGracePeriodCountdown() - 1);
    }
    
    @Override
    public void exit() {
        plugin.getServer().getScheduler().cancelTask(respawnTaskId);
    }
    
    @Override
    public void cleanup() {
        for (int flyTaskId : context.getGlideTaskIds()) {
            plugin.getServer().getScheduler().cancelTask(flyTaskId);
        }
        context.getParticipants().values().forEach(p -> p.setShouldGlide(false));
    }
    
    @Override
    public void onParticipantRejoin(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        participant.setAlive(false); // participants are dead when joining
        super.onParticipantRejoin(participant, team);
        participant.setGameMode(GameMode.SPECTATOR);
        updateRespawnLine();
        participant.teleport(config.getPlatformSpawns().getFirst());
        if (allowRespawn()) {
            initiateParticipantRespawnCountdown(participant);
        }
    }
    
    @Override
    public void onNewParticipantJoin(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        participant.setAlive(false); // participants are dead when joining
        super.onNewParticipantJoin(participant, team);
        participant.setGameMode(GameMode.SPECTATOR);
        updateRespawnLine();
        participant.teleport(config.getPlatformSpawns().getFirst());
        if (allowRespawn()) {
            initiateParticipantRespawnCountdown(participant);
        }
    }
    
    @Override
    public void onParticipantQuit(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        if (participant.isAlive()) {
            List<ItemStack> drops = Arrays.stream(participant.getInventory().getContents())
                    .filter(Objects::nonNull)
                    .toList();
            int droppedExp = GameManagerUtils.calculateExpPoints(participant.getLevel());
            Component deathMessage = Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" left early. Their life is forfeit."));
            PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant.getPlayer(),
                    DamageSource.builder(DamageType.GENERIC).build(), drops, droppedExp, 0, 0, 0, deathMessage, true);
            this.onParticipantDeath(fakeDeathEvent, participant);
        }
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull SurvivalGamesParticipant participant) {
        if (participant.isInRespawnGracePeriod()) {
            onRespawningParticipantDamage(event, participant);
        } else {
            onLivingParticipantDamage(event);
        }
    }
    
    private void onRespawningParticipantDamage(@NotNull EntityDamageEvent event, @NotNull SurvivalGamesParticipant participant) {
        event.setCancelled(true);
        Entity causingEntity = event.getDamageSource().getCausingEntity();
        if (causingEntity == null) {
            return;
        }
        SurvivalGamesParticipant damagerParticipant = context.getParticipants().get(causingEntity.getUniqueId());
        if (damagerParticipant == null) {
            return;
        }
        damagerParticipant.sendMessage(Component.empty()
                .append(participant.displayName())
                .append(Component.text(" just respawned"))
                .color(NamedTextColor.RED)
        );
    }
    
    private void onLivingParticipantDamage(@NotNull EntityDamageEvent event) {
        if (config.getBorder().canAttackWhenRespawning()) {
            return;
        }
        Entity causingEntity = event.getDamageSource().getCausingEntity();
        if (causingEntity == null) {
            return;
        }
        SurvivalGamesParticipant damagerParticipant = context.getParticipants().get(causingEntity.getUniqueId());
        if (damagerParticipant == null) {
            return;
        }
        if (!damagerParticipant.isInRespawnGracePeriod()) {
            return;
        }
        event.setCancelled(true);
        damagerParticipant.sendMessage(Component.empty()
                .append(Component.text("You just respawned"))
                .color(NamedTextColor.RED)
        );
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull SurvivalGamesParticipant killed) {
        event.setDroppedExp(0);
        if (killed.getKiller() != null) {
            SurvivalGamesParticipant killer = context.getParticipants().get(killed.getKiller().getUniqueId());
            if (killer != null) {
                onParticipantGetKill(killer, killed);
            }
        }
        onParticipantDeath(killed);
    }
    
    /**
     * Used to respawn a participant mid-game and send them into glide mode
     * @param participant the participant to respawn
     */
    public void respawnParticipant(SurvivalGamesParticipant participant) {
        participant.setAlive(true);
        participant.setRespawning(false);
        // grace period start
        participant.setRespawnGracePeriodCountdown(config.getBorder().getRespawnGracePeriodTime());
        handleRespawnGracePeriod(participant);
        // grace period end
        participant.getInventory().setContents(config.getBorder().getRespawnLoadout());
        int index = selectRespawnLocation(participant.getUsedRespawns());
        // save this as a used respawn for this participant
        participant.getUsedRespawns().add(index);
        Location respawn = index == -1 ?
                config.getPlatformSpawns().getFirst() :
                config.getRespawnLocations().get(index);
        participant.teleport(respawn);
        participant.setGameMode(GameMode.ADVENTURE);
        SurvivalGamesTeam team = context.getTeams().get(participant.getTeamId());
        context.updateAliveCount(team);
        int flyTaskId = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            participant.getPlayer().setGliding(true);
            participant.setShouldGlide(true);
        }, 10L).getTaskId();
        context.getGlideTaskIds().add(flyTaskId);
        for (SurvivalGamesParticipant viewer : team.getParticipants()) {
            if (!viewer.equals(participant)) {
                context.getGlowManager().showGlowing(viewer, participant);
            }
        }
        for (Player admin : context.getAdmins()) {
            context.getGlowManager().showGlowing(admin, participant);
        }
    }
    
    /**
     * @param usedRespawns a set of respawns that are already used, and should be excluded if
     * possible
     * @return a random respawn location within the current border stage that hasn't
     * been used by the given participant. If no suitable location can be found,
     * the first center platform spawn is returned.
     */
    private int selectRespawnLocation(Set<Integer> usedRespawns) {
        BorderStage currentBorderStage = context.getCurrentBorderStage();
        double centerX = config.getBorder().getCenterX();
        double centerZ = config.getBorder().getCenterZ();
        return selectRespawnLocationIndex(centerX, centerZ, currentBorderStage, config.getRespawnLocations(), usedRespawns, context.getRandom());
    }
    
    /**
     * Chooses a random location from the given list that is in the given border stage and hasn't already been used
     * @param centerX the center x coord of the border
     * @param centerZ the center z coord of the border
     * @param currentBorderStage the border stage to select a location within
     * @param respawnLocations the locations to choose from. The resulting index will be in range
     * of this list, or -1 if an appropriate location could not be found
     * @param usedRespawnIndexes a collection of indexes of respawn points in the respawnLocations
     * list that has been used, and shouldn't be chosen if an unused
     * one can be found
     * @param random a random provider
     * @return an index in the bounds of the given respawnLocations corresponding to the chosen
     * respawn location, or -1 if an appropriate location could not be found
     */
    public static int selectRespawnLocationIndex(double centerX, double centerZ, BorderStage currentBorderStage, List<Location> respawnLocations, Set<Integer> usedRespawnIndexes, Random random) {
        List<Integer> indexesInsideBorder = currentBorderStage.getLocationIndexesInside(centerX, centerZ, respawnLocations);
        if (indexesInsideBorder.isEmpty()) {
            // failsafe, we shouldn't get to this point from a real-world gameplay perspective
            return -1;
        }
        
        List<Integer> unusedIndexesInsideBorder = new ArrayList<>(indexesInsideBorder);
        unusedIndexesInsideBorder.removeAll(usedRespawnIndexes);
        if (unusedIndexesInsideBorder.isEmpty()) {
            return indexesInsideBorder.get(
                    random.nextInt(indexesInsideBorder.size()));
        } else {
            return unusedIndexesInsideBorder.get(
                    random.nextInt(unusedIndexesInsideBorder.size()));
        }
    }
    
    public void initiateParticipantRespawnCountdown(SurvivalGamesParticipant participant) {
        participant.setRespawning(true);
        SurvivalGamesTeam team = context.getTeams().get(participant.getTeamId());
        team.sendMessage(Component.empty()
                .append(participant.displayName())
                .append(Component.text(" will respawn in "))
                .append(Component.text(config.getBorder().getRespawnTime()))
                .append(Component.text(" seconds"))
                .color(team.getColor()));
        participant.setRespawnCountdown(config.getBorder().getRespawnTime());
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull SurvivalGamesParticipant participant) {
        if (participant.isShouldGlide()) {
            handleGliding(participant);
        }
    }
    
    private void handleGliding(@NotNull SurvivalGamesParticipant participant) {
        if (EntityUtils.isOnGround(participant.getLocation(), 1)) {
            participant.setShouldGlide(false);
            participant.getPlayer().setGliding(false); // this has to come first, or the setGliding will trigger the canceled event
        }
    }
    
    @Override
    public void onParticipantToggleGlide(@NotNull EntityToggleGlideEvent event, SurvivalGamesParticipant participant) {
        if (!participant.isShouldGlide()) {
            return;
        }
        event.setCancelled(true);
    }
    
    private void onParticipantGetKill(@NotNull SurvivalGamesParticipant killer, @NotNull SurvivalGamesParticipant killed) {
        addKill(killer);
        UIUtils.showKillTitle(killer, killed);
        if (!killer.getTeamId().equals(killed.getTeamId())) {
            // if all deaths grant points, or this specific death for this killed participant should grant points
            int deathPointsThreshold = config.getBorder().getDeathPointsThreshold();
            if (deathPointsThreshold < 0 || killed.getDeaths() < deathPointsThreshold) {
                context.awardPoints(killer, config.getKillScore());
            }
        }
    }
    
    /**
     * @param participant the participant to add a kill to
     */
    private void addKill(@NotNull SurvivalGamesParticipant participant) {
        int oldKillCount = participant.getKills();
        int newKillCount = oldKillCount + 1;
        participant.setKills(newKillCount);
        topbar.setKills(participant.getUniqueId(), newKillCount);
    }
    
    /**
     * @param participant the participant to add a death to
     */
    private void addDeath(@NotNull SurvivalGamesParticipant participant) {
        int oldDeathCount = participant.getDeaths();
        int newDeathCount = oldDeathCount + 1;
        participant.setDeaths(newDeathCount);
        if (config.showDeathCount()) {
            topbar.setDeaths(participant.getUniqueId(), newDeathCount);
        }
    }
    
    private void onParticipantDeath(SurvivalGamesParticipant participant) {
        participant.setAlive(false);
        String teamId = participant.getTeamId();
        addDeath(participant);
        SurvivalGamesTeam team = context.getTeams().get(teamId);
        context.updateAliveCount(team);
        for (SurvivalGamesParticipant viewer : team.getParticipants()) {
            if (!viewer.equals(participant)) {
                context.getGlowManager().hideGlowing(viewer, participant);
            }
        }
        for (Player viewer : context.getAdmins()) {
            context.getGlowManager().hideGlowing(viewer, participant);
        }
        if (allowRespawn()) {
            initiateParticipantRespawnCountdown(participant);
        }
        if (!team.isAlive()) {
            onTeamDeath(context.getTeams().get(teamId));
        }
    }
    
    /**
     * Call when all of a team's members are dead.
     * @param deadTeam the team who just died
     */
    private void onTeamDeath(SurvivalGamesTeam deadTeam) {
        if (allowRespawn()) {
            onTeamTempDeath(deadTeam);
        } else {
            onTeamPermadeath(deadTeam);
        }
    }
    
    private void onTeamTempDeath(SurvivalGamesTeam deadTeam) {
        context.messageAllParticipants(Component.empty()
                .append(deadTeam.getFormattedDisplayName())
                .append(Component.text(" has been eliminated, but will respawn.")));
    }
    
    private void onTeamPermadeath(SurvivalGamesTeam deadTeam) {
        context.messageAllParticipants(Component.empty()
                .append(deadTeam.getFormattedDisplayName())
                .append(Component.text(" has been eliminated.")));
        List<SurvivalGamesTeam> livingTeams = getLivingTeams();
        for (SurvivalGamesTeam livingTeam : livingTeams) {
            context.awardPoints(livingTeam, config.getSurviveTeamScore());
        }
        if (!getRespawningTeams().isEmpty()) {
            // there is still battle to be had
            return;
        }
        switch (livingTeams.size()) {
            case 2 -> {
                plugin.getServer().sendMessage(Component.empty()
                        .append(deadTeam.getFormattedDisplayName())
                        .append(Component.text(" got third place!")));
                context.awardPoints(deadTeam, config.getThirdPlaceScore());
            }
            case 1 -> {
                plugin.getServer().sendMessage(Component.empty()
                        .append(deadTeam.getFormattedDisplayName())
                        .append(Component.text(" got second place!")));
                context.awardPoints(deadTeam, config.getSecondPlaceScore());
                onTeamWin(livingTeams.getFirst());
            }
            case 0 -> {
                // this is a provision for when there is only 1 team at the beginning, for testing purposes
                onTeamWin(deadTeam);
            }
        }
    }
    
    /**
     * @return a list of the teams which are still alive (have at least 1 living member)
     */
    private @NotNull List<SurvivalGamesTeam> getLivingTeams() {
        return context.getTeams().values().stream()
                .filter(SurvivalGamesTeam::isAlive)
                .toList();
    }
    
    /**
     * @return a list of the teams which are dead, but have at least 1 team member who is respawning
     */
    private @NotNull List<SurvivalGamesTeam> getRespawningTeams() {
        return context.getTeams().values().stream()
                .filter(SurvivalGamesTeam::isRespawning)
                .toList();
    }
    
    private void onTeamWin(SurvivalGamesTeam winningTeam) {
        plugin.getServer().sendMessage(Component.text("Team ")
                .append(winningTeam.getFormattedDisplayName())
                .append(Component.text(" wins!")));
        context.awardPoints(winningTeam, config.getFirstPlaceScore());
        if (context.getCurrentRound() < context.getConfig().getRounds()) {
            context.setState(new RoundOverState(context));
        } else {
            context.setState(new GameOverState(context));
        }
    }
    
    protected boolean allowRespawn() {
        return config.getBorder().allowRespawn(context.getBorderStageIndex());
    }
    
    /**
     * Update the sidebars to reflect the current respawn status
     */
    protected void updateRespawnLine() {
        if (config.getBorder().neverRespawn()) {
            return;
        }
        Component respawnLine = config.getBorder().getRespawnLine(context.getBorderStageIndex());
        context.getAdminSidebar().updateLine("respawn", respawnLine);
        context.getSidebar().updateLine("respawn", respawnLine);
    }
}

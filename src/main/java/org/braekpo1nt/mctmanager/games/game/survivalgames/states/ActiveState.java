package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.games.game.survivalgames.config.SurvivalGamesConfig;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.participant.TeamData;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.ui.topbar.ManyBattleTopbar;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.WorldBorder;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ActiveState implements SurvivalGamesState {
    
    private final @NotNull SurvivalGamesGame context;
    private final Main plugin;
    private final GameManager gameManager;
    private final TimerManager timerManager;
    private final SurvivalGamesConfig config;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final ManyBattleTopbar topbar;
    private final WorldBorder worldBorder;
    /**
     * the index of the border stage
     */
    private int borderStageIndex = 0;
    private boolean gracePeriod = false;
    private @Nullable Timer borderDelay;
    private @Nullable Timer borderShrinking;
    private @Nullable Timer gracePeriodTimer;
    
    public ActiveState(@NotNull SurvivalGamesGame context) {
        this.context = context;
        this.plugin = context.getPlugin();
        this.gameManager = context.getGameManager();
        this.timerManager = context.getTimerManager();
        this.config = context.getConfig();
        this.sidebar = context.getSidebar();
        this.adminSidebar = context.getAdminSidebar();
        this.topbar = context.getTopbar();
        this.worldBorder = context.getWorldBorder();
        context.removePlatforms();
        startGracePeriodTimer();
    }
    
    private void startGracePeriodTimer() {
        gracePeriod = true;
        Component gracePeriodDuration = TimeStringUtils.getTimeComponent(config.getGracePeriodDuration());
        Component gracePeriodStarted = Component.empty()
                .append(gracePeriodDuration)
                .append(Component.text(" grace period"))
                .color(NamedTextColor.GREEN);
        context.messageAllParticipants(gracePeriodStarted);
        Audience.audience(context.getParticipants().values()).showTitle(UIUtils.defaultTitle(
                Component.empty(),
                gracePeriodStarted
        ));
        gracePeriodTimer = timerManager.start(Timer.builder()
                .duration(config.getGracePeriodDuration())
                .withTopbar(topbar)
                .topbarPrefix(Component.text("Grace Period: "))
                .onCompletion(() -> {
                    gracePeriod = false;
                    Component gracePeriodEnded = Component.empty()
                            .append(Component.text("Grace period ended"))
                            .color(NamedTextColor.RED);
                    context.messageAllParticipants(gracePeriodEnded);
                    Audience.audience(context.getParticipants().values()).showTitle(UIUtils.defaultTitle(
                            Component.empty(),
                            gracePeriodEnded
                    ));
                    startBorderDelay();
                })
                .build());
    }
    
    private void startBorderDelay() {
        borderDelay = timerManager.start(Timer.builder()
                .duration(config.getDelays()[borderStageIndex])
                .withSidebar(adminSidebar, "timer")
                .withTopbar(topbar)
                .sidebarPrefix(Component.text("Border: ")
                        .color(NamedTextColor.LIGHT_PURPLE))
                .topbarPrefix(Component.text("Border: ")
                        .color(NamedTextColor.LIGHT_PURPLE))
                .timerColor(NamedTextColor.LIGHT_PURPLE)
                .onCompletion(() -> {
                    int size = config.getSizes()[borderStageIndex];
                    int duration = config.getDurations()[borderStageIndex];
                    worldBorder.setSize(size, duration);
                    sendBorderShrinkAnnouncement(duration, size);
                    startBorderShrinking();
                })
                .build());
    }
    
    private void startBorderShrinking() {
        borderShrinking = timerManager.start(Timer.builder()
                .duration(config.getDurations()[borderStageIndex])
                .withSidebar(adminSidebar, "timer")
                .withTopbar(topbar)
                .sidebarPrefix(Component.text("Border shrinking: ")
                        .color(NamedTextColor.RED))
                .topbarPrefix(Component.text("Border shrinking: ")
                        .color(NamedTextColor.RED))
                .timerColor(NamedTextColor.RED)
                .onCompletion(() -> {
                    borderStageIndex++;
                    if (borderStageIndex >= config.getDelays().length) {
                        startSuddenDeath();
                        return;
                    }
                    int delay = config.getDelays()[borderStageIndex];
                    sendBorderDelayAnnouncement(delay);
                    startBorderDelay();
                })
                .build());
    }
    
    /**
     * Sends a chat message to all participants saying the border is delaying
     * @param delay The delay in seconds
     */
    private void sendBorderDelayAnnouncement(int delay) {
        String timeString = TimeStringUtils.getTimeString(delay);
        context.messageAllParticipants(Component.text("Border will not shrink for "+timeString));
    }
    
    private void startSuddenDeath() {
        Component message = Component.empty()
                        .append(Component.text("Sudden Death")
                                .color(NamedTextColor.RED));
        topbar.setMiddle(message);
        adminSidebar.updateLine("timer", message);
        context.messageAllParticipants(Component.empty()
                .append(Component.text("Sudden death!")
                    .color(NamedTextColor.RED)));
    }
    
    /**
     * Sends a chat message to all participants saying the border is shrinking
     * @param duration The duration of the shrink in seconds
     * @param size The size of the border in blocks
     */
    private void sendBorderShrinkAnnouncement(int duration, int size) {
        String timeString = TimeStringUtils.getTimeString(duration);
        context.messageAllParticipants(Component.empty()
                .append(Component.text("Border shrinking to "))
                .append(Component.text(size))
                .append(Component.text(" for "))
                .append(Component.text(timeString))
                .color(NamedTextColor.RED)
        );
        Audience.audience(
                Audience.audience(context.getAdmins()),
                Audience.audience(context.getParticipants().values())
        ).showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.text("Border shrinking")
                        .color(NamedTextColor.RED)
        ));
    }
    
    @Override
    public void onParticipantJoin(Participant participant, Team team) {
        context.onTeamJoin(team);
        if (participantShouldRejoin(participant)) {
            rejoinParticipant(participant);
        } else {
            context.getDeadPlayers().remove(participant.getUniqueId());
            String teamId = participant.getTeamId();
            if (!context.getLivingMembers().containsKey(teamId)) {
                context.getTopbar().addTeam(teamId, team.getColor());
            }
            initializeParticipant(participant);
            participant.teleport(config.getPlatformSpawns().getFirst());
            participant.setRespawnLocation(config.getPlatformSpawns().getFirst(), true);
        }
        sidebar.updateLine(participant.getUniqueId(), "title", context.getTitle());
        context.initializeGlowing(participant);
    }
    
    private boolean participantShouldRejoin(Participant participant) {
        return context.getLivingMembers().containsKey(participant.getTeamId()) && 
                context.getDeadPlayers().contains(participant.getUniqueId());
    }
    
    private void rejoinParticipant(Participant participant) {
        context.getParticipants().put(participant.getUniqueId(), participant);
        context.getTeams().get(participant.getTeamId()).addParticipant(participant);
        participant.setGameMode(GameMode.SPECTATOR);
        sidebar.addPlayer(participant);
        topbar.showPlayer(participant);
        context.initializeKillCount(participant);
        topbar.linkToTeam(participant.getUniqueId(), participant.getTeamId());
        context.getGlowManager().addPlayer(participant);
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        if (context.getLivingPlayers().contains(participant.getUniqueId())) {
            List<ItemStack> drops = Arrays.stream(participant.getInventory().getContents())
                    .filter(Objects::nonNull)
                    .toList();
            int droppedExp = GameManagerUtils.calculateExpPoints(participant.getLevel());
            Component deathMessage = Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" left early. Their life is forfeit."));
            PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant.getPlayer(), 
                    DamageSource.builder(DamageType.GENERIC).build(), drops, droppedExp, deathMessage);
            this.onParticipantDeath(fakeDeathEvent);
        }
        resetParticipant(participant);
        context.getParticipants().remove(participant.getUniqueId());
    }
    
    @Override
    public void initializeParticipant(Participant participant) {
        context.getParticipants().put(participant.getUniqueId(), participant);
        context.getTeams().get(participant.getTeamId()).addParticipant(participant);
        context.getLivingPlayers().add(participant.getUniqueId());
        String teamId = participant.getTeamId();
        context.getLivingMembers().putIfAbsent(teamId, 0);
        int oldAliveCount = context.getLivingMembers().get(teamId);
        context.getLivingMembers().put(teamId, oldAliveCount + 1);
        sidebar.addPlayer(participant);
        topbar.showPlayer(participant);
        topbar.linkToTeam(participant.getUniqueId(), teamId);
        context.updateAliveCount(teamId);
        context.initializeKillCount(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
    }
    
    @Override
    public void resetParticipant(Participant participant) {
        context.getTeams().get(participant.getTeamId()).removeParticipant(participant.getUniqueId());
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        context.getSidebar().removePlayer(participant.getUniqueId());
        context.getTopbar().hidePlayer(participant.getUniqueId());
        context.getGlowManager().removePlayer(participant);
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        if (gracePeriod) {
            Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "SurvivalGames.ActiveState.onPlayerDamage()->invulnerable cancelled");
            event.setCancelled(true);
        }
    }
    
    @Override
    public void onParticipantDeath(PlayerDeathEvent event) {
        Participant killed = context.getParticipants().get(event.getPlayer().getUniqueId());
        if (killed == null) {
            return;
        }
        killed.setGameMode(GameMode.SPECTATOR);
        dropInventory(killed, event.getDrops());
        Main.debugLog(LogType.CANCEL_PLAYER_DEATH_EVENT, "SurvivalGamesGame.ActiveState.onPlayerDeath() cancelled");
        event.setCancelled(true);
        if (event.getDeathSound() != null && event.getDeathSoundCategory() != null) {
            killed.getWorld().playSound(killed.getLocation(), event.getDeathSound(), event.getDeathSoundCategory(), event.getDeathSoundVolume(), event.getDeathSoundPitch());
        }
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            plugin.getServer().sendMessage(deathMessage);
        }
        if (killed.getKiller() != null) {
            Participant killer = context.getParticipants().get(killed.getKiller().getUniqueId());
            if (killer != null) {
                onParticipantGetKill(killer, killed);
            }
        }
        onParticipantDeath(killed);
    }
    
    private void dropInventory(Participant killed, List<ItemStack> drops) {
        for (ItemStack item : drops) {
            config.getWorld().dropItemNaturally(killed.getLocation(), item);
        }
    }
    
    private void onParticipantGetKill(@NotNull Participant killer, @NotNull Participant killed) {
        if (!context.getParticipants().containsKey(killer.getUniqueId())) {
            return;
        }
        addKill(killer.getUniqueId());
        UIUtils.showKillTitle(killer, killed);
        gameManager.awardPointsToParticipant(killer, config.getKillScore());
    }
    
    /**
     * @param playerUUID the player to add a kill to
     */
    private void addKill(@NotNull UUID playerUUID) {
        int oldKillCount = context.getKillCounts().get(playerUUID);
        int newKillCount = oldKillCount + 1;
        context.getKillCounts().put(playerUUID, newKillCount);
        topbar.setKills(playerUUID, newKillCount);
    }
    
    /**
     * @param playerUUID the player to add a death to
     */
    private void addDeath(@NotNull UUID playerUUID) {
        int oldDeathCount = context.getDeathCounts().get(playerUUID);
        int newDeathCount = oldDeathCount + 1;
        context.getDeathCounts().put(playerUUID, newDeathCount);
        topbar.setDeaths(playerUUID, newDeathCount);
    }
    
    private void onParticipantDeath(Participant killed) {
        UUID killedUUID = killed.getUniqueId();
        killed.getInventory().clear();
        switchPlayerFromLivingToDead(killedUUID);
        String teamId = killed.getTeamId();
        int oldLivingMembers = context.getLivingMembers().get(teamId);
        context.getLivingMembers().put(teamId, oldLivingMembers - 1);
        addDeath(killedUUID);
        context.updateAliveCount(teamId);
        if (context.getLivingMembers().get(teamId) <= 0) {
            onTeamDeath(context.getTeams().get(teamId));
        }
    }
    
    private void switchPlayerFromLivingToDead(UUID playerUniqueId) {
        context.getLivingPlayers().remove(playerUniqueId);
        context.getDeadPlayers().add(playerUniqueId);
    }
    
    /**
     * Call when all of a team's members are dead. 
     * @param deadTeam the team who just died
     */
    private void onTeamDeath(Team deadTeam) {
        context.messageAllParticipants(Component.empty()
                .append(deadTeam.getFormattedDisplayName())
                .append(Component.text(" has been eliminated.")));
        // TODO: does getLivingTeams need to return teams, or just ids?
        List<TeamData<Participant>> livingTeams = getLivingTeams();
        gameManager.awardPointsToTeams(Team.getTeamIds(livingTeams), config.getSurviveTeamScore());
        switch (livingTeams.size()) {
            case 2 -> {
                plugin.getServer().sendMessage(Component.empty()
                        .append(deadTeam.getFormattedDisplayName())
                        .append(Component.text(" got third place!")));
                gameManager.awardPointsToTeam(deadTeam, config.getThirdPlaceScore());
            }
            case 1 -> {
                plugin.getServer().sendMessage(Component.empty()
                        .append(deadTeam.getFormattedDisplayName())
                        .append(Component.text(" got second place!")));
                gameManager.awardPointsToTeam(deadTeam, config.getSecondPlaceScore());
                onTeamWin(livingTeams.getFirst());
            }
            case 0 -> {
                // this is a provision for when there is only 1 team at the beginning, for testing purposes
                onTeamWin(deadTeam);
            }
        }
    }
    
    /**
     * @return a list of the teamIds of the teams which are still alive (have at least 1 living member)
     */
    private @NotNull List<SurvivalGamesTeam> getLivingTeams() {
        // TODO: Teams remove this in favor of teams storing their living members
        return context.getLivingMembers().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .map(context.getTeams()::get).toList();
    }
    
    private void onTeamWin(Team winningTeam) {
        plugin.getServer().sendMessage(Component.text("Team ")
                .append(winningTeam.getFormattedDisplayName())
                .append(Component.text(" wins!")));
        gameManager.awardPointsToTeam(winningTeam, config.getFirstPlaceScore());
        if (borderDelay != null) {
            borderDelay.cancel();
        }
        if (borderShrinking != null) {
            borderShrinking.cancel();
        }
        if (gracePeriodTimer != null) {
            gracePeriodTimer.cancel();
        }
        context.setState(new GameOverState(context));
    }
}

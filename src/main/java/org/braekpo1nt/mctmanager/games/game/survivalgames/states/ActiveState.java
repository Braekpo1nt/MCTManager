package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.games.game.survivalgames.config.SurvivalGamesConfig;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
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
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ActiveState extends SurvivalGamesStateBase {
    
    private final Main plugin;
    private final TimerManager timerManager;
    private final SurvivalGamesConfig config;
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
        super(context);
        this.plugin = context.getPlugin();
        this.timerManager = context.getTimerManager();
        this.config = context.getConfig();
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
    public void onParticipantRejoin(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        super.onParticipantRejoin(participant, team);
        participant.setGameMode(GameMode.SPECTATOR);
    }
    
    @Override
    public void onNewParticipantJoin(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        super.onNewParticipantJoin(participant, team);
        participant.teleport(config.getPlatformSpawns().getFirst());
        participant.setRespawnLocation(config.getPlatformSpawns().getFirst(), true);
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
                    DamageSource.builder(DamageType.GENERIC).build(), drops, droppedExp, deathMessage);
            this.onParticipantDeath(fakeDeathEvent, participant);
        }
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull SurvivalGamesParticipant participant) {
        if (gracePeriod) {
            Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "SurvivalGames.ActiveState.onPlayerDamage()->invulnerable cancelled");
            event.setCancelled(true);
        }
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
    
    private void onParticipantGetKill(@NotNull SurvivalGamesParticipant killer, @NotNull SurvivalGamesParticipant killed) {
        if (!context.getParticipants().containsKey(killer.getUniqueId())) {
            return;
        }
        addKill(killer);
        UIUtils.showKillTitle(killer, killed);
        context.awardPoints(killer, config.getKillScore());
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
        if (!team.isAlive()) {
            onTeamDeath(context.getTeams().get(teamId));
        }
    }
    
    /**
     * Call when all of a team's members are dead. 
     * @param deadTeam the team who just died
     */
    private void onTeamDeath(SurvivalGamesTeam deadTeam) {
        context.messageAllParticipants(Component.empty()
                .append(deadTeam.getFormattedDisplayName())
                .append(Component.text(" has been eliminated.")));
        List<SurvivalGamesTeam> livingTeams = getLivingTeams();
        for (SurvivalGamesTeam livingTeam : livingTeams) {
            context.awardPoints(livingTeam, config.getSurviveTeamScore());
            context.displayScore(livingTeam);
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
    
    private void onTeamWin(SurvivalGamesTeam winningTeam) {
        plugin.getServer().sendMessage(Component.text("Team ")
                .append(winningTeam.getFormattedDisplayName())
                .append(Component.text(" wins!")));
        context.awardPoints(winningTeam, config.getFirstPlaceScore());
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

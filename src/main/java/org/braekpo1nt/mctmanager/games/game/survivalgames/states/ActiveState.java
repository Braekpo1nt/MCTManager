package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.config.SurvivalGamesConfig;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.ui.topbar.ManyBattleTopbar;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.WorldBorder;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
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
    private boolean invulnerable = false;
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
        startInvulnerableTimer();
    }
    
    private void startInvulnerableTimer() {
        invulnerable = true;
        Component gracePeriodDuration = TimeStringUtils.getTimeComponent(config.getGracePeriodDuration());
        Component gracePeriodStarted = Component.empty()
                .append(gracePeriodDuration)
                .append(Component.text(" grace period"))
                .color(NamedTextColor.GREEN);
        context.messageAllParticipants(gracePeriodStarted);
        Audience.audience(context.getParticipants()).showTitle(UIUtils.defaultTitle(
                Component.empty(),
                gracePeriodStarted
        ));
        Component initialTimer = Component.empty()
                .append(Component.text("Grace period: "))
                .append(gracePeriodDuration);
        sidebar.addLine("grace", initialTimer);
        adminSidebar.addLine("grace", initialTimer);
        gracePeriodTimer = timerManager.start(Timer.builder()
                .duration(config.getGracePeriodDuration())
                .withSidebar(sidebar, "grace")
                .withSidebar(adminSidebar, "grace")
                .sidebarPrefix(Component.text("Grace Period: "))
                .onCompletion(() -> {
                    sidebar.deleteLine("grace");
                    adminSidebar.deleteLine("grace");
                    invulnerable = false;
                    Component gracePeriodEnded = Component.empty()
                            .append(Component.text("Grace period ended"))
                            .color(NamedTextColor.RED);
                    context.messageAllParticipants(gracePeriodEnded);
                    Audience.audience(context.getParticipants()).showTitle(UIUtils.defaultTitle(
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
        String message = String.format("%sSudden death", ChatColor.RED);
        topbar.setMiddle(Component.text("Sudden death")
                .color(NamedTextColor.RED));
        adminSidebar.updateLine("timer", message);
        context.messageAllParticipants(Component.text("Sudden death!"));
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
                Audience.audience(context.getParticipants())
        ).showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.text("Border shrinking")
                        .color(NamedTextColor.RED)
        ));
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        if (participantShouldRejoin(participant)) {
            rejoinParticipant(participant);
        } else {
            context.getDeadPlayers().remove(participant.getUniqueId());
            String teamId = context.getGameManager().getTeamName(participant.getUniqueId());
            if (!context.getLivingMembers().containsKey(teamId)) {
                NamedTextColor color = context.getGameManager().getTeamNamedTextColor(teamId);
                context.getTopbar().addTeam(teamId, color);
            }
            initializeParticipant(participant);
            participant.teleport(config.getPlatformSpawns().get(0));
            participant.setBedSpawnLocation(config.getPlatformSpawns().get(0), true);
        }
        sidebar.updateLine(participant.getUniqueId(), "title", context.getTitle());
    }
    
    private boolean participantShouldRejoin(Player participant) {
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        return context.getLivingMembers().containsKey(teamId) && 
                context.getDeadPlayers().contains(participant.getUniqueId());
    }
    
    private void rejoinParticipant(Player participant) {
        context.getParticipants().add(participant);
        participant.setGameMode(GameMode.SPECTATOR);
        sidebar.addPlayer(participant);
        topbar.showPlayer(participant);
        context.initializeKillCount(participant);
        String teamId = gameManager.getTeamName(participant.getUniqueId());
        topbar.linkToTeam(participant.getUniqueId(), teamId);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        if (context.getLivingPlayers().contains(participant.getUniqueId())) {
            List<ItemStack> drops = Arrays.stream(participant.getInventory().getContents())
                    .filter(Objects::nonNull)
                    .toList();
            int droppedExp = GameManagerUtils.calculateExpPoints(participant.getLevel());
            Component deathMessage = Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" left early. Their life is forfeit."));
            PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant, 
                    DamageSource.builder(DamageType.GENERIC).build(), drops, droppedExp, deathMessage);
            this.onPlayerDeath(fakeDeathEvent);
        }
        resetParticipant(participant);
        context.getParticipants().remove(participant);
    }
    
    @Override
    public void initializeParticipant(Player participant) {
        context.getParticipants().add(participant);
        context.getLivingPlayers().add(participant.getUniqueId());
        String teamId = context.getGameManager().getTeamName(participant.getUniqueId());
        context.getLivingMembers().putIfAbsent(teamId, 0);
        int oldAliveCount = context.getLivingMembers().get(teamId);
        context.getLivingMembers().put(teamId, oldAliveCount + 1);
        sidebar.addPlayer(participant);
        topbar.showPlayer(participant);
        topbar.linkToTeam(participant.getUniqueId(), teamId);
        context.updateAliveCount(teamId);
        context.initializeKillCount(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.getInventory().clear();
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
    }
    
    @Override
    public void resetParticipant(Player participant) {
        participant.getInventory().clear();
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        context.getSidebar().removePlayer(participant.getUniqueId());
        context.getTopbar().hidePlayer(participant.getUniqueId());
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        if (invulnerable) {
            Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "SurvivalGames.ActiveState.onPlayerDamage()->invulnerable cancelled");
            event.setCancelled(true);
        }
    }
    
    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killed = event.getPlayer();
        if (!context.getParticipants().contains(killed)) {
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
            onParticipantGetKill(killed.getKiller(), killed);
        }
        onParticipantDeath(killed);
    }
    
    private void dropInventory(Player killed, List<ItemStack> drops) {
        for (ItemStack item : drops) {
            config.getWorld().dropItemNaturally(killed.getLocation(), item);
        }
        killed.getInventory().clear();
    }
    
    private void onParticipantGetKill(@NotNull Player killer, @NotNull Player killed) {
        if (!context.getParticipants().contains(killer)) {
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
    
    private void onParticipantDeath(Player killed) {
        UUID killedUUID = killed.getUniqueId();
        switchPlayerFromLivingToDead(killedUUID);
        String teamId = gameManager.getTeamName(killedUUID);
        int oldLivingMembers = context.getLivingMembers().get(teamId);
        context.getLivingMembers().put(teamId, oldLivingMembers - 1);
        addDeath(killedUUID);
        context.updateAliveCount(teamId);
        if (context.getLivingMembers().get(teamId) <= 0) {
            onTeamDeath(teamId);
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
    private void onTeamDeath(String deadTeam) {
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(deadTeam);
        context.messageAllParticipants(Component.empty()
                .append(formattedTeamDisplayName)
                .append(Component.text(" has been eliminated.")));
        Component displayName = gameManager.getFormattedTeamDisplayName(deadTeam);
        List<String> livingTeams = getLivingTeamIds();
        for (String teamId : livingTeams) {
            gameManager.awardPointsToTeam(teamId, config.getSurviveTeamScore());
        }
        switch (livingTeams.size()) {
            case 2 -> {
                plugin.getServer().sendMessage(Component.empty()
                        .append(displayName)
                        .append(Component.text(" got third place!")));
                gameManager.awardPointsToTeam(deadTeam, config.getThirdPlaceScore());
            }
            case 1 -> {
                plugin.getServer().sendMessage(Component.empty()
                        .append(displayName)
                        .append(Component.text(" got second place!")));
                gameManager.awardPointsToTeam(deadTeam, config.getSecondPlaceScore());
                onTeamWin(livingTeams.get(0));
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
    private @NotNull List<String> getLivingTeamIds() {
        return context.getLivingMembers().entrySet().stream()
                .filter(entry -> entry.getValue() > 0).map(Map.Entry::getKey).toList();
    }
    
    private void onTeamWin(String winningTeam) {
        Component displayName = gameManager.getFormattedTeamDisplayName(winningTeam);
        plugin.getServer().sendMessage(Component.text("Team ")
                .append(displayName)
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

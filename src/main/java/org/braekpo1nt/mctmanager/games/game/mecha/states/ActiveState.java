package org.braekpo1nt.mctmanager.games.game.mecha.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.mecha.MechaGame;
import org.braekpo1nt.mctmanager.games.game.mecha.config.MechaConfig;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.ui.topbar.ManyBattleTopbar;
import org.bukkit.ChatColor;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;

public class ActiveState implements MechaState {
    
    private final @NotNull MechaGame context;
    private final TimerManager timerManager;
    private final MechaConfig config;
    private final Sidebar sidebar;
    private final Sidebar adminSidebar;
    private final ManyBattleTopbar topbar;
    private final WorldBorder worldBorder;
    /**
     * the index of the border stage
     */
    private int borderStageIndex = 0;
    private boolean invulnerable = false;
    
    public ActiveState(@NotNull MechaGame context) {
        this.context = context;
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
        timerManager.start(Timer.builder()
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
        timerManager.start(Timer.builder()
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
        timerManager.start(Timer.builder()
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
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    @Override
    public void initializeParticipant(Player participant) {
        
    }
    
    @Override
    public void resetParticipant(Player participant) {
        
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        
    }
    
    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        
    }
    
    @Override
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        
    }
    
    @Override
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        
    }
    
    
}

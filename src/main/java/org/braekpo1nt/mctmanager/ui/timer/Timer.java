package org.braekpo1nt.mctmanager.ui.timer;

import lombok.Data;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.topbar.Topbar;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Timer extends BukkitRunnable {
    
    @Data
    private static class SidebarData {
        private final @NotNull Sidebar sidebar;
        private final @NotNull String key;
    }
    
    private boolean paused = false;
    private int secondsLeft;
    private final int titleThreshold;
    private final @NotNull List<SidebarData> sidebarDatas;
    private final @NotNull List<Topbar> topbars;
    private final @Nullable Audience titleAudience;
    private final @Nullable Runnable completion;
    
    private Timer(int secondsLeft,
                  @NotNull List<SidebarData> sidebarDatas,
                  @NotNull List<Topbar> topbars,
                  int titleThreshold,
                  @Nullable Audience titleAudience, @Nullable Runnable completion) {
        this.secondsLeft = secondsLeft;
        this.sidebarDatas = sidebarDatas;
        this.topbars = topbars;
        this.titleAudience = titleAudience;
        this.titleThreshold = titleThreshold;
        this.completion = completion;
    }
    
    public int pause() {
        this.paused = true;
        return secondsLeft;
    }
    
    public int resume() {
        this.paused = false;
        return secondsLeft;
    }
    
    public int getSecondsLeft() {
        return this.secondsLeft;
    }
    
    public int setSecondsLeft(int secondsLeft) {
        int oldSecondsLeft = this.secondsLeft;
        this.secondsLeft = secondsLeft;
        return oldSecondsLeft;
    }
    
    /**
     * Called every iteration
     */
    @Override
    public void run() {
        if (paused) {
            return;
        }
        if (secondsLeft <= 0) {
            for (Topbar topbar : topbars) {
                topbar.setMiddle(Component.empty());
            }
            for (SidebarData sidebarData : sidebarDatas) {
                sidebarData.getSidebar().updateLine(sidebarData.getKey(), "");
            }
            if (titleAudience != null) {
                titleAudience.showTitle(Title.title(Component.empty(), Component.empty()));
            }
            onComplete();
            return;
        }
        String timeString = TimeStringUtils.getTimeString(secondsLeft);
        for (Topbar topbar : topbars) {
            topbar.setMiddle(Component.text(timeString));
        }
        for (SidebarData sidebarData : sidebarDatas) {
            sidebarData.getSidebar().updateLine(sidebarData.getKey(), timeString);
        }
        if (titleAudience != null && secondsLeft <= titleThreshold) {
            Title title = Title.title(
                    Component.text("Starting in"), 
                    Component.text(secondsLeft)
                            .color(TimeStringUtils.getColorForTime(secondsLeft)), 
                    Title.Times.times(
                            Duration.ZERO,
                            Duration.ofMillis(1500), 
                            Duration.ZERO
                    )
            );
            titleAudience.showTitle(title);
        }
        secondsLeft--;
    }
    
    /**
     * The method to be called when the timer hits zero. Calls the specified competion method if it's not null, and always cancels this Timer. 
     */
    private void onComplete() {
        if (completion != null) {
            completion.run();
        }
        this.cancel();
    }
    
    public Timer start(Plugin plugin) {
        this.runTaskTimer(plugin, 0L, 20L);
        return this;
    }
    
    public Timer start(Plugin plugin, int secondsLeft) {
        this.secondsLeft = secondsLeft;
        return this.start(plugin);
    }
    
    public int skip() {
        this.onComplete();
        return secondsLeft;
    }
    
    public static class Builder {
        private int count = 0;
        private @Nullable List<SidebarData> sidebarDatas;
        private @Nullable List<Topbar> topbars;
        private int titleThreshold = 10;
        private @Nullable Audience titleAudience;
        private @Nullable Runnable completion;
        
        public Timer build() {
            return new Timer(
                    count,
                    sidebarDatas != null ? sidebarDatas : Collections.emptyList(),
                    topbars != null ? topbars : Collections.emptyList(),
                    titleThreshold,
                    titleAudience,
                    completion
            );
        }
        
        /**
         * Set the duration of the timer, in seconds
         * @param seconds the number of seconds this timer should last
         * @return this
         */
        public Builder duration(int seconds) {
            this.count = seconds;
            return this;
        }
        
        /**
         * Add a sidebar to display this timer. Use this multiple times to specify multiple sidebars.
         * @param sidebar the sidebar to display the timer on
         * @param key the key of the line to display the timer
         * @return this
         */
        public Builder withSidebar(@NotNull Sidebar sidebar, @NotNull String key) {
            SidebarData sidebarData = new SidebarData(sidebar, key);
            if (sidebarDatas == null) {
                sidebarDatas = new ArrayList<>(Collections.singletonList(sidebarData));
            } else {
                sidebarDatas.add(sidebarData);
            }
            return this;
        }
        
        /**
         * A sidebar to display this timer on. Will be displayed in the middle of
         * the given Topbar. Use this multiple times to specify multiple Topbars
         * @param topbar the Topbar to display the timer in the middle of
         * @return this
         */
        public Builder withTopbar(@NotNull Topbar topbar) {
            if (topbars == null) {
                topbars = new ArrayList<>(Collections.singletonList(topbar));
            } else {
                topbars.add(topbar);
            }
            return this;
        }
        
        /**
         * Defaults to 10 if not specified. 
         * If the titleAudience is not null, then the Title countdown will be displayed
         * to the audience when this timer has {@code titleThreshold} seconds left.
         * @param titleThreshold the number of seconds meant to be left in this timer before the title begins displaying to the given audience, if that audience is not null. 
         * @return this
         */
        public Builder titleThreshold(int titleThreshold) {
            this.titleThreshold = titleThreshold;
            return this;
        }
        
        /**
         * @param titleAudience the audience to display the title to. If null, no title will be displayed.
         * @return this
         */
        public Builder titleAudience(@Nullable Audience titleAudience) {
            this.titleAudience = titleAudience;
            return this;
        }
        
        /**
         * @param completion the {@link Runnable} to be called when the timer reaches 0
         * @return this
         */
        public Builder onCompletion(@Nullable Runnable completion) {
            this.completion = completion;
            return this;
        }
    }
}

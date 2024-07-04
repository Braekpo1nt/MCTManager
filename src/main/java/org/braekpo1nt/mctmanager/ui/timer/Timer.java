package org.braekpo1nt.mctmanager.ui.timer;

import lombok.Data;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.topbar.Topbar;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Timer extends BukkitRunnable {
    
    @Override
    public String toString() {
        return "Timer{" +
                "manager=" + manager +
                ", paused=" + paused +
                ", started=" + started +
                ", secondsLeft=" + secondsLeft +
                ", name='" + name + '\'' +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Data
    private static class SidebarData {
        private final @NotNull Sidebar sidebar;
        private final @NotNull String key;
    }
    
    private int secondsLeft;
    private int completionSeconds;
    private boolean started = false;
    private boolean paused = false;
    /**
     * the consumer to execute on pause and resume. Will be passed true on pause, and false on resume. 
     */
    private final @Nullable Consumer<Boolean> onTogglePause;
    
    /**
     * if this Timer belongs to a TimerManager, this will retain a reference to that.
     * Upon cancellation, this Timer will remove itself from this manager.
     */
    private @Nullable TimerManager manager;
    
    private final @NotNull Component sidebarPrefix;
    private final @NotNull List<SidebarData> sidebarDatas;
    
    private final @NotNull Component topbarPrefix;
    private final @NotNull List<Topbar> topbars;
    
    private final @NotNull TextColor timerColor;
    
    private final int titleThreshold;
    private @Nullable Audience titleAudience;
    
    private final @Nullable Runnable completion;
    
    //debug
    private final @Nullable String name;
    //debug
    
    private Timer(int secondsLeft,
                  int completionSeconds,
                  @NotNull Component sidebarPrefix,
                  @NotNull List<SidebarData> sidebarDatas,
                  @NotNull Component topbarPrefix,
                  @NotNull List<Topbar> topbars,
                  @NotNull TextColor timerColor,
                  int titleThreshold,
                  @Nullable Audience titleAudience, @Nullable Runnable completion,
                  @Nullable Consumer<Boolean> onTogglePause,
                  @Nullable String name
    ) {
        this.secondsLeft = secondsLeft;
        this.completionSeconds = completionSeconds;
        this.sidebarPrefix = sidebarPrefix;
        this.sidebarDatas = sidebarDatas;
        this.topbarPrefix = topbarPrefix;
        this.topbars = topbars;
        this.timerColor = timerColor;
        this.titleAudience = titleAudience;
        this.titleThreshold = titleThreshold;
        this.completion = completion;
        this.onTogglePause = onTogglePause;
        this.name = name;
    }
    
    public void setTimerManager(@NotNull TimerManager timerManager) {
        this.manager = timerManager;
    }
    
    /**
     * @return how many seconds are left in this timer.
     */
    public int getSecondsLeft() {
        return this.secondsLeft;
    }
    
    /**
     * Set the number of seconds left in this timer.
     * @param secondsLeft the number of seconds left desired
     */
    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }
    
    /**
     * Add a new Audience to this timer's titleAudience. They will see the Title countdown, if there is one, when it happens. 
     * @param newTitleAudience the Audience implementation to add to this timer's titleAudience
     */
    public void addTitleAudience(@NotNull Audience newTitleAudience) {
        if (titleAudience == null) {
            titleAudience = newTitleAudience;
        } else {
            titleAudience = Audience.audience(titleAudience, newTitleAudience);
        }
    }
    
    /**
     * Remove the given audience from this timer's titleAudience. They will not see the Title countdown when it happens (if it happens). Clears the title of the given audience if this timer is displaying titles. 
     * @param removeTitleAudience the Audience implementation to remove (could be a player, could be the console, etc.) 
     */
    public void removeTitleAudience(@NotNull Audience removeTitleAudience) {
        if (titleAudience == null) {
            return;
        }
        titleAudience = titleAudience.filterAudience(audience -> !audience.equals(removeTitleAudience));
        if (secondsLeft <= titleThreshold) {
            removeTitleAudience.clearTitle();
        }
    }
    
    /**
     * Called every iteration
     */
    @Override
    public void run() {
        if (paused) {
            return;
        }
        if (secondsLeft <= completionSeconds) {
            onComplete();
            return;
        }
        Component timeString = TimeStringUtils.getTimeComponent(secondsLeft)
                .color(timerColor);
        for (Topbar topbar : topbars) {
            topbar.setMiddle(
                    Component.empty()
                            .append(topbarPrefix)
                            .append(timeString)
            );
        }
        for (SidebarData sidebarData : sidebarDatas) {
            sidebarData.getSidebar().updateLine(sidebarData.getKey(), 
                    Component.empty()
                            .append(sidebarPrefix)
                            .append(timeString)
            );
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
        clear();
        if (completion != null) {
            completion.run();
        }
        this.cancel();
    }
    
    /**
     * Pauses this timer. The timer will not proceed until {@link Timer#resume()} is called. If this is paused already,
     * nothing happens. If this is not paused, then {@link #onTogglePause} is called, passing true.
     */
    public void pause() {
        if (paused) {
            return;
        }
        this.paused = true;
        if (onTogglePause != null) {
            onTogglePause.accept(true);
        }
    }
    
    /**
     * Resumes this timer. The timer will proceed iterating. If this is not paused, nothing happens. 
     * If this is paused, then {@link #onTogglePause} is called, passing false.
     */
    public void resume() {
        if (!paused) {
            return;
        }
        this.paused = false;
        if (onTogglePause != null) {
            onTogglePause.accept(false);
        }
    }
    
    /**
     * skip to the end, as if the timer had reached zero. Still performs any actions and cleanup, same as if the timer reached zero. 
     */
    public void skip() {
        this.onComplete();
    }
    
    /**
     * set all topbar middles to empty, set all assigned sidebar lines to empty, and clear the titleAudience's title. Note that this does not stop this timer on its own, so values might be reset on the next iteration of {@link Timer#run}. 
     */
    private void clear() {
        for (Topbar topbar : topbars) {
            topbar.setMiddle(Component.empty());
        }
        for (SidebarData sidebarData : sidebarDatas) {
            sidebarData.getSidebar().updateLine(sidebarData.getKey(), Component.empty());
        }
        if (titleAudience != null) {
            titleAudience.clearTitle();
        }
    }
    
    /**
     * {@inheritDoc}
     * This will be called when this timer is over or stopped.
     * If this is paused, sets the state to no longer be paused. If this is registered in a timerManager, also removes itself from that timerManager.
     * @throws IllegalStateException
     */
    @Override
    public synchronized void cancel() throws IllegalStateException {
        started = false;
        paused = false;
        if (manager != null) {
            manager.unregister(this);
            manager = null;
        }
        super.cancel();
    }
    
    /**
     * start this timer. If this timer was already started, nothing happens. 
     * @param plugin the plugin to register this BukkitRunnable with
     * @return this
     */
    public Timer start(Plugin plugin) {
        if (!started) {
            this.runTaskTimer(plugin, 0L, 20L);
        }
        return this;
    }
    
    /**
     * {@inheritDoc}
     * also marks this Timer as having started, so that you can't start it again with {@link Timer#start(Plugin)}
     */
    @Override
    public synchronized @NotNull BukkitTask runTaskTimer(@NotNull Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        if (started) {
            throw new IllegalStateException("can't start a timer which is already started");
        }
        started = true;
        return super.runTaskTimer(plugin, delay, period);
    }
    
    public static class Builder {
        private int duration = 0;
        private int completionSeconds = 0;
        
        private @Nullable Component sidebarPrefix;
        private @Nullable List<SidebarData> sidebarDatas;
        
        
        private @Nullable Component topbarPrefix;
        private @Nullable List<Topbar> topbars;
        
        private @Nullable TextColor timerColor;
        
        private int titleThreshold = 10;
        private @Nullable Audience titleAudience;
        
        private @Nullable Runnable completion;
        private @Nullable Consumer<Boolean> onTogglePause;
        
        private @Nullable String name;
        
        public Timer build() {
            return new Timer(
                    duration,
                    completionSeconds,
                    sidebarPrefix != null ? sidebarPrefix : Component.empty(),
                    sidebarDatas != null ? sidebarDatas : Collections.emptyList(),
                    topbarPrefix != null ? topbarPrefix : Component.empty(),
                    topbars != null ? topbars : Collections.emptyList(),
                    timerColor != null ? timerColor : NamedTextColor.WHITE,
                    titleThreshold,
                    titleAudience,
                    completion,
                    onTogglePause,
                    name
            );
        }
        
        /**
         * A method to call when the pause state is toggled. 
         * You can rely this Timer to only call this consumer when pause is actually toggled. For example,
         * if the pause command is run twice in a row, this will only be called once. 
         * @param onTogglePause the consumer to execute on pause and resume. 
         *                      Will be passed true on pause, and false on resume. 
         * @return this
         */
        public Builder onTogglePause(@Nullable Consumer<Boolean> onTogglePause) {
            this.onTogglePause = onTogglePause;
            return this;
        }
        
        /**
         * @param name the name of this timer. Useful for debugging, not much else. 
         * @return this
         */
        public Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }
        
        /**
         * Set the duration of the timer, in seconds
         * @param seconds the number of seconds this timer should last
         * @return this
         */
        public Builder duration(int seconds) {
            this.duration = seconds;
            return this;
        }
    
        /**
         * Set the number of seconds to be left for the timer to finish.
         * For example, This allows for you to have a timer that goes from
         * 30 seconds to 10 seconds, then start another timer with different
         * colors/attributes with 10 seconds left, if you want. 
         * @param completionSeconds Defaults to 0. The number of seconds left for the timer to complete. 
         * @return this
         */
        public Builder completionSeconds(int completionSeconds) {
            this.completionSeconds = completionSeconds;
            return this;
        }
        
        /**
         * Set the prefix to display before the sidebar's timer
         * @param sidebarPrefix the prefix to display before the timer
         * @return this
         */
        public Builder sidebarPrefix(@Nullable Component sidebarPrefix) {
            this.sidebarPrefix = sidebarPrefix;
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
         * Set the prefix to display before the Topbar's timer
         * @param topbarPrefix the prefix to display before the timer
         * @return this
         */
        public Builder topbarPrefix(@Nullable Component topbarPrefix) {
            this.topbarPrefix = topbarPrefix;
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
         * @param timerColor the color the time left component should be 
         *                   (in both the Topbars and Sidebars, but not in the Title)
         *                   Defaults to {@link NamedTextColor#WHITE}.
         * @return this
         */
        public Builder timerColor(@Nullable TextColor timerColor) {
            this.timerColor = timerColor;
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

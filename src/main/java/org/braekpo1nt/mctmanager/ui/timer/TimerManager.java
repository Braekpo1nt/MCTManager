package org.braekpo1nt.mctmanager.ui.timer;

import lombok.Setter;
import org.braekpo1nt.mctmanager.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TimerManager {
    
    // debug
    private final @Nullable String name;
    // debug
    private final Main plugin;
    @Setter
    private @Nullable TimerManager parent;
    private final List<@NotNull TimerManager> managers = new ArrayList<>();
    private final List<@NotNull Timer> timers = new ArrayList<>();
    
    public TimerManager(@NotNull Main plugin, @Nullable String name) {
        this.plugin = plugin;
        this.name = name;
    }
    
    public TimerManager(@NotNull Main plugin) {
        this(plugin, null);
    }
    
    @Override
    public String toString() {
        return "TimerManager{" +
                "name='" + name + '\'' +
                ", plugin=" + plugin +
                ", parent=" + parent +
                ", nManagers=" + managers.size() +
                ", nTimers=" + timers.size() +
                '}';
    }
    
    /**
     * @return a new TimerManager instance which is already
     * registered with this TimerManager as its parent
     */
    public TimerManager createManager() {
        TimerManager manager = new TimerManager(plugin);
        return register(manager);
    }
    
    /**
     * @param manager the manager to register
     * @return the given manager
     */
    public TimerManager register(@NotNull TimerManager manager) {
        managers.add(manager);
        manager.setParent(this);
        return manager;
    }
    
    public Timer register(@NotNull Timer timer) {
        timers.add(timer);
        timer.setTimerManager(this);
        return timer;
    }
    
    private void unregister(@NotNull TimerManager manager) {
        managers.remove(manager);
    }
    
    public Timer start(@NotNull Timer timer) {
        return register(timer.start(plugin));
    }
    
    public void unregister(@NotNull Timer timer) {
        timers.remove(timer);
    }
    
    public void pause() {
        managers.forEach(TimerManager::pause);
        timers.forEach(Timer::pause);
    }
    
    public void resume() {
        managers.forEach(TimerManager::resume);
        timers.forEach(Timer::resume);
    }
    
    public void skip() {
        List<TimerManager> managersCopy = new ArrayList<>(managers);
        List<Timer> timersCopy = new ArrayList<>(timers);
        // this line ordering is important. Skipping manager A might create timer B,
        // which is added to the timers list and would be skipped, if we weren't iterating
        // through a copy of both lists.
        for (TimerManager manager : managersCopy) {
            manager.skip();
        }
        for (Timer timer : timersCopy) {
            timer.skip();
        }
    }
    
    /**
     * Cancel all sub TimerManagers and Timers handled by this TimerManager.
     * This also removes itself from its parent, if it has one.
     */
    public void cancel() {
        List<TimerManager> managersCopy = new ArrayList<>(managers);
        for (TimerManager manager : managersCopy) {
            manager.cancel();
        }
        List<Timer> timersCopy = new ArrayList<>(timers);
        for (Timer timer : timersCopy) {
            timer.cancel();
        }
        if (parent != null) {
            parent.unregister(this);
            parent = null;
        }
    }
}

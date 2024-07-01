package org.braekpo1nt.mctmanager.ui.timer;

import org.braekpo1nt.mctmanager.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TimerManager {
    
    private final Main plugin;
    
    private final List<@NotNull TimerManager> managers = new ArrayList<>();
    private final List<@NotNull Timer> timers = new ArrayList<>();
    
    public TimerManager(@NotNull Main plugin) {
        this.plugin = plugin;
    }
    
    public TimerManager createManager() {
        TimerManager manager = new TimerManager(plugin);
        managers.add(manager);
        return manager;
    }
    
    public Timer start(@NotNull Timer timer) {
        timer.start(plugin);
        timers.add(timer);
        return timer;
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
        managers.forEach(TimerManager::skip);
        managers.clear();
        timers.forEach(Timer::skip);
        timers.clear();
    }
    
    public void cancel() {
        managers.forEach(TimerManager::cancel);
        managers.clear();
        timers.forEach(Timer::cancel);
        timers.clear();
    }
    
}

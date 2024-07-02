package org.braekpo1nt.mctmanager.ui.timer;

import lombok.Setter;
import org.braekpo1nt.mctmanager.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TimerManager {
    
    private final Main plugin;
    @Setter
    private @Nullable TimerManager parent;
    private final List<@NotNull TimerManager> managers = new ArrayList<>();
    private final List<@NotNull Timer> timers = new ArrayList<>();
    
    public TimerManager(@NotNull Main plugin) {
        this.plugin = plugin;
    }
    
    public TimerManager createManager() {
        TimerManager manager = new TimerManager(plugin);
        managers.add(manager);
        manager.setParent(this);
        return manager;
    }
    
    private void remove(@NotNull TimerManager manager) {
        managers.remove(manager);
    }
    
    public Timer start(@NotNull Timer timer) {
        timer.start(plugin);
        timers.add(timer);
        timer.setManager(this);
        return timer;
    }
    
    public void remove(@NotNull Timer timer) {
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
        for (TimerManager manager : managers) {
            manager.setParent(null);
            manager.skip();
        }
        managers.clear();
        for (Timer timer : timers) {
            timer.setManager(null);
            timer.skip();
        }
        timers.clear();
    }
    
    public void cancel() {
        managers.forEach(manager -> {
            manager.setParent(null);
            manager.cancel();
        });
        managers.clear();
        timers.forEach(timer -> {
            timer.setManager(null);
            timer.cancel();
        });
        timers.clear();
        if (parent != null) {
            parent.remove(this);
            parent = null;
        }
    }
}

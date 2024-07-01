package org.braekpo1nt.mctmanager.ui.timer;

import org.braekpo1nt.mctmanager.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TimerManager {
    
    private final Main plugin;
    
    private final @Nullable TimerManager parentManager;
    private final List<@NotNull TimerManager> managers = new ArrayList<>();
    private final List<@NotNull Timer> timers = new ArrayList<>();
    
    public TimerManager(@NotNull Main plugin, @Nullable TimerManager parentManager) {
        this.plugin = plugin;
        this.parentManager = parentManager;
    }
    
    public TimerManager(@NotNull Main plugin) {
        this(plugin, null);
    }
    
    public TimerManager createManager() {
        TimerManager manager = new TimerManager(plugin);
        register(manager);
        return manager;
    }
    
    private void register(TimerManager manager) {
        managers.add(manager);
    }
    
    public void register(@NotNull Timer timer) {
        timers.add(timer);
    }
    
    public void remove(@NotNull Timer timer) {
        timers.remove(timer);
    }
    
    public void remove(@NotNull TimerManager manager) {
        managers.remove(manager);
    }
    
    public Timer start(@NotNull Timer timer) {
        timer.start(plugin);
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
    
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void skip() {
        for (Iterator<@NotNull TimerManager> iterator = managers.iterator(); iterator.hasNext();) {
            iterator.next().skip();
        }
        for (Iterator<@NotNull Timer> iterator = timers.iterator(); iterator.hasNext();) {
            iterator.next().skip();
        }
    }
    
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void cancel() {
        for (Iterator<@NotNull TimerManager> iterator = managers.iterator(); iterator.hasNext();) {
            iterator.next().cancel();
        }
        for (Iterator<@NotNull Timer> iterator = timers.iterator(); iterator.hasNext();) {
            iterator.next().cancel();
        }
    }
    
}

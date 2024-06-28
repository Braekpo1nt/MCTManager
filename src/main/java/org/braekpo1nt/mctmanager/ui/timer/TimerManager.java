package org.braekpo1nt.mctmanager.ui.timer;

import org.braekpo1nt.mctmanager.Main;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TimerManager {
    
    private final Main plugin;
    
    private final List<@NotNull Timer> timers = new ArrayList<>();
    
    public TimerManager(@NotNull Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Start the given timer and register it in this TimerManager. It will be included in all bulk operations this TimerManager performs. 
     * @param timer the timer to register and start
     * @return the timer that was passed in, for convenience
     */
    public Timer start(@NotNull Timer timer) {
        timers.add(timer);
        timer.start(plugin);
        return timer;
    }
    
    public void remove(@NotNull Timer timer) {
        timers.remove(timer);
    }
    
    public void pause() {
        timers.forEach(Timer::pause);
    }
    
    public void resume() {
        timers.forEach(Timer::resume);
    }
    
    public void skip() {
        timers.forEach(Timer::skip);
    }
    
    public void cancel() {
        timers.forEach(Timer::cancel);
    }
    
    
    
}

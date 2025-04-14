package org.braekpo1nt.mctmanager.games.experimental;

import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * The basic requirements for a game listener. Used in {@link GameBase} to
 * reduce boilerplate.
 * @param <P> the type of the participant used in the {@link GameBase}
 */
public class GameListener<P extends ParticipantData> implements Listener {
    
    protected final @NotNull GameBase<P, ?, ?, ?, ?> context;
    
    protected GameListener(@NotNull GameBase<P, ?, ?, ?, ?> context) {
        this.context = context;
    }
    
    public void register(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void unregister() {
        HandlerList.unregisterAll(this);
    }
    
}

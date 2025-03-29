package org.braekpo1nt.mctmanager.games.experimental;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * The basic requirements for a game listener. Used in {@link GameBase} to
 * reduce boilerplate.
 * @param <P> the type of the participant used in the {@link GameBase}
 */
public abstract class GameListener<P> implements Listener {
    
    protected final @NotNull GameData<P> gameData;
    
    public GameListener(@NotNull GameData<P> gameData) {
        this.gameData = gameData;
    }
    
    public void register(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void unregister() {
        HandlerList.unregisterAll(this);
    }
    
}

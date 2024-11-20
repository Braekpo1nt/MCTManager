package org.braekpo1nt.mctmanager.packetevents;

import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.injector.ChannelInjector;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.netty.NettyManager;
import org.braekpo1nt.mctmanager.MockMain;
import org.bukkit.plugin.Plugin;

/**
 * Mock the {@link PacketEventsAPI} for testing purposes. This should fake any actual
 * functionality and do essentially nothing. 
 */
public class PacketEventsAPIMock extends PacketEventsAPI<Plugin> {
    private final MockMain plugin;
    boolean loaded = false;
    boolean initialized = false;
    boolean terminated = false;
    
    public PacketEventsAPIMock(MockMain plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void load() {
        loaded = true;
    }
    
    @Override
    public boolean isLoaded() {
        return loaded;
    }
    
    @Override
    public void init() {
        initialized = true;
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    @Override
    public void terminate() {
        terminated = true;
    }
    
    @Override
    public boolean isTerminated() {
        return terminated;
    }
    
    @Override
    public Plugin getPlugin() {
        return plugin;
    }
    
    @Override
    public ServerManager getServerManager() {
        return null;
    }
    
    @Override
    public ProtocolManager getProtocolManager() {
        return null;
    }
    
    @Override
    public PlayerManager getPlayerManager() {
        return null;
    }
    
    @Override
    public NettyManager getNettyManager() {
        return null;
    }
    
    @Override
    public ChannelInjector getInjector() {
        return null;
    }
}

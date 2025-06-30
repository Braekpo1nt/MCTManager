package org.braekpo1nt.mctmanager.games.editor.states;

import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.editor.Admin;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public interface EditorStateBase<A extends Admin> {
    void cleanup();
    void onAdminJoin(A admin);
    void onAdminQuit(A admin);
    
    @NotNull CommandResult validateConfig(@NotNull String configFile) throws ConfigException;
    
    @NotNull CommandResult saveConfig(@NotNull String configFile) throws ConfigException;
    
    @NotNull CommandResult loadConfig(@NotNull String configFile) throws ConfigException;
    
    void onAdminInteract(PlayerInteractEvent event, A admin);
    void onAdminDropItem(PlayerDropItemEvent event, A admin);
}

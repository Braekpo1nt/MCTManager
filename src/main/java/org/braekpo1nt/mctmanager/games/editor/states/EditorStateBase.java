package org.braekpo1nt.mctmanager.games.editor.states;

import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.editor.Admin;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public interface EditorStateBase<A extends Admin> {
    void cleanup();
    void onAdminJoin(A admin);
    void onAdminQuit(A admin);
    
    void validateConfig(@NotNull String configFile) throws ConfigException;
    
    void saveConfig(@NotNull String configFile) throws ConfigException;
    
    void loadConfig(@NotNull String configFile) throws ConfigException;
    
    void onAdminInteract(PlayerInteractEvent event, A admin);
}

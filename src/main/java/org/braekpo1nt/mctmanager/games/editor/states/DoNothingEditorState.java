package org.braekpo1nt.mctmanager.games.editor.states;

import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.editor.Admin;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public interface DoNothingEditorState<A extends Admin> extends EditorStateBase<A> {
    @Override
    default void cleanup() {
        
    }
    
    @Override
    default void onAdminJoin(A admin) {
        
    }
    
    @Override
    default void onAdminQuit(A admin) {
        
    }
    
    @Override
    default @NotNull CommandResult validateConfig(@NotNull String configFile) throws ConfigException {
        return CommandResult.success();
    }
    
    @Override
    default @NotNull CommandResult saveConfig(@NotNull String configFile) throws ConfigException {
        return CommandResult.success();
    }
    
    @Override
    default @NotNull CommandResult loadConfig(@NotNull String configFile) throws ConfigException {
        return CommandResult.success();
    }
    
    @Override
    default void onAdminInteract(PlayerInteractEvent event, A admin) {
        
    }
}

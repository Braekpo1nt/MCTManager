package org.braekpo1nt.mctmanager.games.game.interfaces;

import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface GameEditor extends Configurable {
    GameType getType();
    void stop();
    
    void onAdminJoin(Player admin);
    void onAdminQuit(UUID uuid);
    
    CommandResult configIsValid(@NotNull String configFile);
    
    CommandResult saveConfig(@NotNull String configFile, boolean skipValidation) throws ConfigIOException, ConfigInvalidException;
}

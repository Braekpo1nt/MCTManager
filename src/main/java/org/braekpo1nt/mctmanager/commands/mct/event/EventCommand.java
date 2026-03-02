package org.braekpo1nt.mctmanager.commands.mct.event;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.Usage;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

public class EventCommand extends CommandManager {
    
    public EventCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new EventUndoSubCommand(gameManager, "undo"));
        addSubCommand(new ModifyCommand(gameManager, "modify"));
    }
    
    @Override
    protected @NotNull Usage getSubCommandUsageArg(Permissible permissible) {
        return new Usage("<options>");
    }
}

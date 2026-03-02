package org.braekpo1nt.mctmanager.commands.mct.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.Usage;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.mct.event.vote.VoteCommand;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class EventCommand extends CommandManager {
    
    public EventCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new EventUndoSubCommand(gameManager, "undo"));
        addSubCommand(new VoteCommand(gameManager, "vote"));
        addSubCommand(new ModifyCommand(gameManager, "modify"));
    }
    
    @Override
    protected @NotNull Usage getSubCommandUsageArg(Permissible permissible) {
        return new Usage("<options>");
    }
}

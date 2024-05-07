package org.braekpo1nt.mctmanager.commands.utils;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.Usage;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

public class UtilsCommand extends CommandManager {
    
    public UtilsCommand(Main plugin) {
        super("utils");
        PluginCommand command = plugin.getCommand(getName());
        Preconditions.checkArgument(command != null, "Can't find command %s", getName());
        command.setExecutor(this);
        addSubCommand(new DistSubCommand(plugin, "dist"));
        addSubCommand(new BoundingBoxSubCommand(plugin, "boundingbox"));
        addSubCommand(new LocationSubCommand("location"));
        addSubCommand(new VectorSubCommand("vector"));
        addSubCommand(new YawPitchSubCommand("yawpitch"));
    }
    
    @Override
    protected @NotNull Usage getSubCommandUsageArg() {
        return new Usage("<options>");
    }
    
    
}

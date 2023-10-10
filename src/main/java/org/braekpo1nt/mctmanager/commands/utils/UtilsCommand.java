package org.braekpo1nt.mctmanager.commands.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandManager;

public class UtilsCommand extends CommandManager {
    
    public UtilsCommand(Main plugin) {
        plugin.getCommand("utils").setExecutor(this);
        subCommands.put("dist", new DistSubCommand());
        subCommands.put("boundingbox", new BoundingBoxSubCommand());
        subCommands.put("location", new LocationSubCommand());
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /utils <options>", NamedTextColor.RED);
    }
    
    
}

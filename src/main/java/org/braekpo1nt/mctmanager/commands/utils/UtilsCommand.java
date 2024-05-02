package org.braekpo1nt.mctmanager.commands.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.commandmanager.OldCommandManager;

public class UtilsCommand extends OldCommandManager {
    
    public UtilsCommand(Main plugin) {
        plugin.getCommand("utils").setExecutor(this);
        subCommands.put("dist", new DistSubCommand());
        subCommands.put("boundingbox", new BoundingBoxSubCommand(plugin));
        subCommands.put("location", new LocationSubCommand());
        subCommands.put("vector", new VectorSubCommand());
        subCommands.put("yawpitch", new YawPitchSubCommand());
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /utils <options>", NamedTextColor.RED);
    }
    
    
}

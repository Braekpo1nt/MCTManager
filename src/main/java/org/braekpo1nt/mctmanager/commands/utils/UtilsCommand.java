package org.braekpo1nt.mctmanager.commands.utils;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.MasterCommandManager;
import org.jetbrains.annotations.NotNull;

public class UtilsCommand extends MasterCommandManager {
    
    public UtilsCommand(@NotNull Main plugin) {
        super(plugin, "utils");
        addSubCommand(new DistSubCommand(plugin, "dist"));
        addSubCommand(new BoundingBoxSubCommand(plugin, "boundingbox"));
        addSubCommand(new LocationSubCommand("location"));
        addSubCommand(new VectorSubCommand("vector"));
        addSubCommand(new YawPitchSubCommand("yawpitch"));
    }
}

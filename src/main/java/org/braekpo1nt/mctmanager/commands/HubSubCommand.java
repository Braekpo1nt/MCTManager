package org.braekpo1nt.mctmanager.commands;

import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.braekpo1nt.mctmanager.listeners.HubBoundaryListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class HubSubCommand implements TabExecutor {
    
    private final HubBoundaryListener hubBoundaryListener;
    private final BlockEffectsListener blockEffectsListener;

    public HubSubCommand(HubBoundaryListener hubBoundaryListener, BlockEffectsListener blockEffectsListener) {
        this.hubBoundaryListener = hubBoundaryListener;
        this.blockEffectsListener = blockEffectsListener;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /mct hub <options>");
        }
        
        switch (args[0]) {
            case "disablehubboundary":
                hubBoundaryListener.disableBoundary();
                break;
            case "enablehubboundary":
                hubBoundaryListener.enableBoundary();
                break;
            case "disableblockeffects":
                blockEffectsListener.disableBlockEffects();
            case "enableblockeffects":
                blockEffectsListener.enableBlockEffects();
            default:
                sender.sendMessage(String.format("Unrecognized option %s", args[0]));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Arrays.asList("disablehubboundary", "enablehubboundary", "disableblockeffects", "enableblockeffects");
    }
}

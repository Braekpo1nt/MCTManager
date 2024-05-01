package org.braekpo1nt.mctmanager.commands.mct;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Handles toggling various options for MCT. Sort of like /gamerule. Mostly used for debugging.
 */
public class OptionSubCommand implements TabExecutor {
    
    private final GameManager gameManager;
    private final BlockEffectsListener blockEffectsListener;
    
    public OptionSubCommand(GameManager gameManager, BlockEffectsListener blockEffectsListener) {
        this.gameManager = gameManager;
        this.blockEffectsListener = blockEffectsListener;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /mct option <options>");
            return false;
        }
        
        switch (args[0]) {
            case "disablehubboundary":
                gameManager.setBoundaryEnabled(false);
                break;
            case "enablehubboundary":
                gameManager.setBoundaryEnabled(true);
                break;
            case "disableblockeffects":
                blockEffectsListener.disableBlockEffects();
                break;
            case "enableblockeffects":
                blockEffectsListener.enableBlockEffects();
                break;
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

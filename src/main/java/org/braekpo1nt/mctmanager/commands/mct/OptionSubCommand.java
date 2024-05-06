package org.braekpo1nt.mctmanager.commands.mct;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.commandmanager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Handles toggling various options for MCT. Sort of like /gamerule. Mostly used for debugging.
 */
public class OptionSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    private final BlockEffectsListener blockEffectsListener;
    
    public OptionSubCommand(GameManager gameManager, BlockEffectsListener blockEffectsListener, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
        this.blockEffectsListener = blockEffectsListener;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            return CommandResult.failure(getUsage().of("<options>"));
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
                return CommandResult.failure(Component.empty()
                        .append(Component.text("Unrecognized option "))
                        .append(Component.text(args[0])
                                .decorate(TextDecoration.BOLD)));
        }
        return CommandResult.success();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Arrays.asList("disablehubboundary", "enablehubboundary", "disableblockeffects", "enableblockeffects");
    }
}

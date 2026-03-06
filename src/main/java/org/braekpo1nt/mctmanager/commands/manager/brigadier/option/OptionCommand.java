package org.braekpo1nt.mctmanager.commands.manager.brigadier.option;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.jetbrains.annotations.NotNull;

public class OptionCommand implements BrigadierSubCommand {
    
    private final @NotNull BlockEffectsListener blockEffectsListener;
    
    public OptionCommand(@NotNull BlockEffectsListener blockEffectsListener) {
        this.blockEffectsListener = blockEffectsListener;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("option")
                .then(Commands.literal("disableblockeffects")
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            blockEffectsListener.disableBlockEffects();
                            return CommandResult.success();
                        }))
                )
                .then(Commands.literal("enableblockeffects")
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            blockEffectsListener.enableBlockEffects();
                            return CommandResult.success();
                        }))
                )
                ;
    }
}

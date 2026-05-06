package org.braekpo1nt.mctmanager.commands.mct.option;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
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
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("option")
                .then(Permissioned.literal("disableblockeffects")
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            blockEffectsListener.disableBlockEffects();
                            return CommandResult.success();
                        }))
                )
                .then(Permissioned.literal("enableblockeffects")
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            blockEffectsListener.enableBlockEffects();
                            return CommandResult.success();
                        }))
                )
                ;
    }
}

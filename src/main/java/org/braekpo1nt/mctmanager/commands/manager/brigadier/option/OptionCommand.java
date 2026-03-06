package org.braekpo1nt.mctmanager.commands.manager.brigadier.option;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.jetbrains.annotations.NotNull;

public class OptionCommand implements BrigadierSubCommand {
    
    private final @NotNull BlockEffectsListener blockEffectsListener;
    
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return null;
    }
}

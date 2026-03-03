package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public interface BrigadierSubCommand {
    @NotNull LiteralArgumentBuilder<CommandSourceStack> create();
}

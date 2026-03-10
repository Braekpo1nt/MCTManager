package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.jetbrains.annotations.NotNull;

public interface BrigadierSubCommand {
    @NotNull Permissioned<CommandSourceStack> create();
}

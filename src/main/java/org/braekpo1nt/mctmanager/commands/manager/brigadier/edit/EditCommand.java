package org.braekpo1nt.mctmanager.commands.manager.brigadier.edit;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class EditCommand implements BrigadierSubCommand {
    
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    
    public EditCommand(@NotNull Main plugin, @NotNull GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("edit")
                .then(new StartEditSubCommand(gameManager).create())
                .then(Commands.literal("stop")
                        .executes(BrigadierAdapters.wraps(ctx -> gameManager.stopEditor()))
                )
                
                ;
    }
}

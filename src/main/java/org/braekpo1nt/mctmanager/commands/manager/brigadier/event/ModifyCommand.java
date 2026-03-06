package org.braekpo1nt.mctmanager.commands.manager.brigadier.event;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class ModifyCommand implements BrigadierSubCommand {
    
    public final @NotNull GameManager gameManager;
    
    public ModifyCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("modify")
                .then(Commands.literal("maxgames")
                        .then(Commands.argument("newCount", IntegerArgumentType.integer())
                                .executes(BrigadierAdapters.wraps(this::executeMaxGames))
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeMaxGames(CommandContext<CommandSourceStack> ctx) {
        int newCount = ctx.getArgument("newCount", Integer.class);
        return gameManager.modifyMaxGames(newCount);
    }
}

package org.braekpo1nt.mctmanager.commands.mct.event;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
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
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("modify")
                .then(Permissioned.literal("maxgames")
                        .then(Permissioned.argument("newCount", IntegerArgumentType.integer())
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

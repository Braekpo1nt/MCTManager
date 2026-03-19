package org.braekpo1nt.mctmanager.commands.mct.mode;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EnumArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
import org.jetbrains.annotations.NotNull;

public class ModeCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public ModeCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("mode")
                .then(Permissioned.argument("mode", new EnumArgumentType<>(Mode.class, Mode.values()))
                        .executes(BrigadierAdapters.wraps(this::executeMode))
                        .then(Permissioned.argument("load", BoolArgumentType.bool())
                                .executes(BrigadierAdapters.wraps(this::executeModeLoad))
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeMode(@NotNull CommandContext<CommandSourceStack> ctx) {
        Mode mode = ctx.getArgument("mode", Mode.class);
        return gameManager.switchMode(mode, false);
    }
    
    private @NotNull CommandResult executeModeLoad(@NotNull CommandContext<CommandSourceStack> ctx) {
        Mode mode = ctx.getArgument("mode", Mode.class);
        boolean load = ctx.getArgument("load", Boolean.class);
        return gameManager.switchMode(mode, load);
    }
}

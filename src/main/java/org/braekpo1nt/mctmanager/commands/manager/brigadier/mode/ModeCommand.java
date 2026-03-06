package org.braekpo1nt.mctmanager.commands.manager.brigadier.mode;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
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
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("mode")
                .then(Commands.argument("mode", new EnumArgumentType<>(Mode.class, Mode.values()))
                        .executes(BrigadierAdapters.wraps(this::executeMode))
                )
                ;
    }
    
    private @NotNull CommandResult executeMode(@NotNull CommandContext<CommandSourceStack> ctx) {
        Mode mode = ctx.getArgument("mode", Mode.class);
        return gameManager.switchMode(mode);
    }
}

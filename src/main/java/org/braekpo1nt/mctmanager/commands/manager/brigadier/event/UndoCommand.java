package org.braekpo1nt.mctmanager.commands.manager.brigadier.event;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EnumArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class UndoCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public UndoCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("undo")
                .then(Commands.argument("game", new EnumArgumentType<>(GameType.class, GameType.values()))
                        .then(Commands.argument("configFile", StringArgumentType.string())
                                .then(Commands.argument("iteration", IntegerArgumentType.integer())
                                        .executes(BrigadierAdapters.wraps(this::executeUndo))
                                )
                        )
                )
                ;
    }
    
    private CommandResult executeUndo(CommandContext<CommandSourceStack> ctx) {
        GameType gameType = ctx.getArgument("game", GameType.class);
        String configFile = ctx.getArgument("configFile", String.class);
        int iterationNumber = ctx.getArgument("iteration", Integer.class);
        return gameManager.undoGame(new GameInstanceId(gameType, configFile), iterationNumber - 1);
    }
}

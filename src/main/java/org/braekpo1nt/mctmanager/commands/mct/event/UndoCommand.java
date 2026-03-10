package org.braekpo1nt.mctmanager.commands.mct.event;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
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
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("undo")
                .then(Permissioned.argument("game", new EnumArgumentType<>(GameType.class, GameType.values()))
                        .then(Permissioned.argument("configFile", StringArgumentType.word())
                                .then(Permissioned.argument("iteration", IntegerArgumentType.integer())
                                        .executes(BrigadierAdapters.wraps(this::executeUndo))
                                )
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeUndo(CommandContext<CommandSourceStack> ctx) {
        GameType gameType = ctx.getArgument("game", GameType.class);
        String configFile = ctx.getArgument("configFile", String.class);
        int iterationNumber = ctx.getArgument("iteration", Integer.class);
        return gameManager.undoGame(new GameInstanceId(gameType, configFile), iterationNumber - 1);
    }
}

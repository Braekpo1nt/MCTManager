package org.braekpo1nt.mctmanager.commands.manager.brigadier.timer;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class TimerCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public TimerCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("timer")
                .then(Commands.literal("pause")
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            gameManager.getTimerManager().pause();
                            return CommandResult.success();
                        }))
                )
                .then(Commands.literal("resume")
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            gameManager.getTimerManager().resume();
                            return CommandResult.success();
                        }))
                )
                .then(Commands.literal("skip")
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            gameManager.getTimerManager().skip();
                            return CommandResult.success();
                        }))
                )
                ;
    }
}

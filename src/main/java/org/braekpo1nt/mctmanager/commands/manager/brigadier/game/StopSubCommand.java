package org.braekpo1nt.mctmanager.commands.manager.brigadier.game;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.argumenttypes.ConfigFileArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.GameIdArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class StopSubCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public StopSubCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("stop")
                .then(Commands.argument("gameId", new GameIdArgumentType(gameManager, false))
                        .then(Commands.argument("configFile", new ConfigFileArgumentType(gameManager, false))
                                .executes(BrigadierAdapters.wraps(ctx -> {
                                    GameType gameType = ctx.getArgument("gameId", GameType.class);
                                    String configFile = ctx.getArgument("configFile", String.class);
                                    return CommandResult.success(Component.empty()
                                            .append(Component.text(gameType.getTitle()))
                                            .append(Component.text(":"))
                                            .append(Component.text(configFile))
                                    );
                                }))
                        )
                )
                ;
    }
}

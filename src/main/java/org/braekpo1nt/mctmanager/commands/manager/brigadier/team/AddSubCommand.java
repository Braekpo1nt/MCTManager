package org.braekpo1nt.mctmanager.commands.manager.brigadier.team;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.jetbrains.annotations.NotNull;

public class AddSubCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public AddSubCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("add")
                .then(Commands.argument("teamId", StringArgumentType.word())
                        .then(Commands.argument("displayName", StringArgumentType.string())
                                .then(Commands.argument("color", StringArgumentType.word())
                                        .suggests(CommandUtils::suggestColor)
                                        .executes(BrigadierAdapters.wraps(this::executeAdd))
                                )
                        )
                )
                ;
    }
    
    private CommandResult executeAdd(CommandContext<CommandSourceStack> ctx) {
        String teamId = ctx.getArgument("teamId", String.class);
        String displayName = ctx.getArgument("displayName", String.class);
        String colorString = ctx.getArgument("color", String.class);
        return GameManagerUtils.addTeam(gameManager, teamId, displayName, colorString);
    }
}

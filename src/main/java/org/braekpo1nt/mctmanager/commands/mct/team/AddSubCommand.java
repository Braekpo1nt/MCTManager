package org.braekpo1nt.mctmanager.commands.mct.team;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AddSubCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public AddSubCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("add")
                .then(Permissioned.argument("teamId", StringArgumentType.word())
                        .then(Permissioned.argument("displayName", StringArgumentType.string())
                                .then(Permissioned.argument("color", StringArgumentType.word())
                                        .suggests(CommandUtils::suggestColor)
                                        .executes(BrigadierAdapters.wrapsFuture(this::executeAdd))
                                )
                        )
                )
                ;
    }
    
    private @NotNull CompletableFuture<CommandResult> executeAdd(CommandContext<CommandSourceStack> ctx) {
        String teamId = ctx.getArgument("teamId", String.class);
        String displayName = ctx.getArgument("displayName", String.class);
        String colorString = ctx.getArgument("color", String.class);
        return GameManagerUtils.addTeam(gameManager, teamId, displayName, colorString);
    }
}

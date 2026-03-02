package org.braekpo1nt.mctmanager.commands.manager.brigadier.team;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AddSubCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public AddSubCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("add")
                .then(Commands.argument("teamId", StringArgumentType.word())
                        .then(Commands.argument("displayName", StringArgumentType.greedyString())
                                .then(Commands.argument("color", ArgumentTypes.namedColor())
                                        .suggests(this::suggestColor)
                                        .executes(BrigadierAdapters.wraps(this::executeAdd))
                                )
                        )
                )
                ;
    }
    
    private CompletableFuture<Suggestions> suggestColor(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        List<String> suggestions = ColorMap.getPartiallyMatchingColorStrings(builder.getRemainingLowerCase());
        for (String suggestion : suggestions) {
            builder.suggest(suggestion);
        }
        return builder.buildFuture();
    }
    
    private CommandResult executeAdd(CommandContext<CommandSourceStack> ctx) {
        String teamId = ctx.getArgument("teamId", String.class);
        String displayName = ctx.getArgument("displayName", String.class);
        String colorString = ctx.getArgument("color", String.class);
        return GameManagerUtils.addTeam(gameManager, teamId, displayName, colorString);
    }
}

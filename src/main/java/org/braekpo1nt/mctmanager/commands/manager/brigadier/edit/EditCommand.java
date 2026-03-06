package org.braekpo1nt.mctmanager.commands.manager.brigadier.edit;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class EditCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public EditCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("edit")
                .then(new StartEditSubCommand(gameManager).create())
                .then(Commands.literal("stop")
                        .executes(BrigadierAdapters.wraps(ctx -> gameManager.stopEditor()))
                )
                .then(Commands.literal("validate")
                        .then(Commands.argument("configFile", StringArgumentType.word())
                                .suggests((this::suggestEditorConfig))
                                .executes(BrigadierAdapters.wraps(this::executeValidate))
                        )
                )
                .then(Commands.literal("save")
                        .then(Commands.argument("configFile", StringArgumentType.word())
                                .suggests((this::suggestEditorConfig))
                                .executes(BrigadierAdapters.wraps(ctx -> executeSave(ctx, false)))
                                .then(Commands.argument("forceSave", BoolArgumentType.bool())
                                        .executes(BrigadierAdapters.wraps(ctx -> {
                                            boolean forceSave = ctx.getArgument("forceSave", Boolean.class);
                                            return executeSave(ctx, forceSave);
                                        }))
                                )
                        )
                )
                .then(Commands.literal("load")
                        .then(Commands.argument("configFile", StringArgumentType.word())
                                .suggests((this::suggestEditorConfig))
                                .executes(BrigadierAdapters.wraps(this::executeLoad))
                        )
                )
                ;
    }
    
    private CompletableFuture<Suggestions> suggestEditorConfig(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            GameType gameType = gameManager.getEditorType();
            if (gameType == null) {
                return builder.build();
            }
            gameManager.getConfigFiles(gameType)
                    .forEach(builder::suggest);
            return builder.build();
        });
    }
    
    private @NotNull CommandResult executeValidate(@NotNull CommandContext<CommandSourceStack> ctx) {
        if (!gameManager.editorIsRunning()) {
            return CommandResult.failure(Component.text("No editor is running."));
        }
        String configFile = ctx.getArgument("configFile", String.class);
        return gameManager.validateEditor(configFile);
    }
    
    private @NotNull CommandResult executeSave(CommandContext<CommandSourceStack> ctx, boolean forceSave) {
        String configFile = ctx.getArgument("configFile", String.class);
        return gameManager.saveEditor(configFile, forceSave);
    }
    
    private @NotNull CommandResult executeLoad(@NotNull CommandContext<CommandSourceStack> ctx) {
        String configFile = ctx.getArgument("configFile", String.class);
        if (!gameManager.editorIsRunning()) {
            return CommandResult.failure(Component.text("No editor is running."));
        }
        return gameManager.loadEditor(configFile);
    }
}

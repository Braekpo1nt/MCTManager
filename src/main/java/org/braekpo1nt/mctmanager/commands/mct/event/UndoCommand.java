package org.braekpo1nt.mctmanager.commands.mct.event;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EnumArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class UndoCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public UndoCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("gameSessions")
                .then(Permissioned.literal("list")
                        .then(Permissioned.argument("eventId", new EventInfoArgumentType(gameManager.getEventService()))
                        )
                        .then(Permissioned.argument("gameType", new EnumArgumentType<>(GameType.class, GameType.values()))
                                .then(Permissioned.argument("configFile", StringArgumentType.word())
                                        .then(Permissioned.argument("mode", new EnumArgumentType<>(Mode.class, Mode.values()))
                                                .executes(BrigadierAdapters.wraps(this::executeList))
                                        )
                                )
                        )
                )
                .then(Permissioned.literal("undo")
                        .then(Permissioned.argument("sessionId", IntegerArgumentType.integer())
                                .executes(BrigadierAdapters.wraps(this::executeUndo))
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeList(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        EventInfoResolver eventInfoResolver = ctx.getArgument("eventId", EventInfoResolver.class);
        String eventId;
        GameType gameType = ctx.getArgument("gameType", GameType.class);
        String configFile = ctx.getArgument("configFile", String.class);
        Mode mode = ctx.getArgument("iteration", Mode.class);
        try {
            eventId = eventInfoResolver.resolve().getEventId();
            List<Integer> gameSessionIds = gameManager.getGameSessionId(eventId, gameType, configFile, mode);
            TextComponent.Builder builder = Component.text();
            for (int i = 0; i < gameSessionIds.size(); i++) {
                int gameSessionId = gameSessionIds.get(i);
                builder
                        .append(Component.text(gameSessionId)
                                .clickEvent(ClickEvent.copyToClipboard(gameSessionId + ""))
                                .hoverEvent(HoverEvent.showText(Component.text("Copy")))
                        )
                ;
                if (i < gameSessionIds.size() - 1) {
                    builder.append(Component.text(", "));
                }
            }
            return CommandResult.success(builder.build());
        } catch (SQLException e) {
            return CommandResult.sqlException("resolving eventId", e);
        }
    }
    
    private @NotNull CommandResult executeUndo(CommandContext<CommandSourceStack> ctx) {
        int sessionId = ctx.getArgument("sessionId", Integer.class);
        return gameManager.undoGame(sessionId);
    }
}

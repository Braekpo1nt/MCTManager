package org.braekpo1nt.mctmanager.commands.mct.event;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class EventSubCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public EventSubCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("event")
                .then(buildStart())
                .then(buildStop())
                .then(buildCreate())
                .then(buildDelete())
                .then(new VoteCommand(gameManager).create())
                .then(new UndoCommand(gameManager).create())
                .then(new ModifyCommand(gameManager).create())
                .then(buildMaxGames())
                ;
    }
    
    private Permissioned<CommandSourceStack> buildStart() {
        return Permissioned.literal("start")
                .then(Permissioned.argument("eventId", new EventInfoArgumentType(gameManager.getEventService()))
                        .then(Permissioned.argument("numberOfGames", IntegerArgumentType.integer())
                                .executes(BrigadierAdapters.wraps(ctx -> {
                                    try {
                                        EventInfoResolver eventInfoResolver = ctx.getArgument("eventId", EventInfoResolver.class);
                                        EventInfo eventInfo = eventInfoResolver.resolve();
                                        int maxGames = ctx.getArgument("numberOfGames", Integer.class);
                                        return gameManager.startEvent(eventInfo, maxGames, 1);
                                    } catch (SQLException e) {
                                        return handleSQLException("get EventInfo", e);
                                    }
                                }))
                                .then(Permissioned.argument("currentGameNumber", IntegerArgumentType.integer())
                                        .executes(BrigadierAdapters.wraps(ctx -> {
                                            try {
                                                EventInfoResolver eventInfoResolver = ctx.getArgument("eventId", EventInfoResolver.class);
                                                EventInfo eventInfo = eventInfoResolver.resolve();
                                                int maxGames = ctx.getArgument("numberOfGames", Integer.class);
                                                int currentGameNumber = ctx.getArgument("currentGameNumber", Integer.class);
                                                return gameManager.startEvent(eventInfo, maxGames, currentGameNumber);
                                            } catch (SQLException e) {
                                                return handleSQLException("get EventInfo", e);
                                            }
                                        }))
                                )
                        )
                );
    }
    
    private Permissioned<CommandSourceStack> buildStop() {
        return Permissioned.literal("stop")
                .executes(BrigadierAdapters.wraps(ctx ->
                        CommandResult.success(Component.text("Are you sure? Type ")
                                .append(Component.empty()
                                        .append(Component.text("/mct event stop "))
                                        .append(Component.text("confirm")
                                                .decorate(TextDecoration.BOLD))
                                        .decorate(TextDecoration.ITALIC))
                                .append(Component.text(" to confirm."))
                                .color(NamedTextColor.YELLOW))
                ))
                .then(Permissioned.literal("confirm")
                        .executes(BrigadierAdapters.wraps(ctx -> gameManager.stopEvent()))
                )
                ;
    }
    
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    
    private Permissioned<CommandSourceStack> buildCreate() {
        return Permissioned.literal("create")
                .then(Permissioned.argument("eventId", new EventInfoArgumentType(gameManager.getEventService()))
                        .then(Permissioned.argument("eventDate", StringArgumentType.word())
                                .suggests((ctx, builder) -> CompletableFuture.supplyAsync(() -> {
                                    String remaining = builder.getRemaining();
                                    if (remaining.isBlank() || remaining.isEmpty()) {
                                        builder.suggest(DATE_FORMAT);
                                        return builder.build();
                                    }
                                    if (remaining.length() > DATE_FORMAT.length()) {
                                        return builder.build();
                                    }
                                    builder.suggest(remaining + DATE_FORMAT.substring(remaining.length()));
                                    return builder.build();
                                }))
                                .then(Permissioned.argument("plainTextName", StringArgumentType.string())
                                        .then(Permissioned.argument("componentName", gameManager.getComponentArgumentType())
                                                .executes(BrigadierAdapters.wraps(this::executeCreate))
                                        )
                                )
                        )
                );
    }
    
    private @NotNull CommandResult executeCreate(CommandContext<CommandSourceStack> ctx) {
        String eventId = ctx.getArgument("eventId", String.class);
        String eventDateString = ctx.getArgument("eventDate", String.class);
        Date eventDate;
        try {
            eventDate = TimeStringUtils.parseDate(eventDateString);
        } catch (DateTimeParseException e) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Could not parse date string "))
                    .append(Component.text(eventDateString)
                            .decorate(TextDecoration.BOLD)));
        }
        String plainTextName = ctx.getArgument("plainTextName", String.class);
        Component componentName = ctx.getArgument("componentName", Component.class);
        return gameManager.createEvent(eventId, eventDate, plainTextName, componentName);
    }
    
    private Permissioned<CommandSourceStack> buildDelete() {
        return Permissioned.literal("delete")
                .then(Permissioned.argument("eventId", new EventInfoArgumentType(gameManager.getEventService()))
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            EventInfoResolver eventInfoResolver = ctx.getArgument("eventId", EventInfoResolver.class);
                            EventInfo eventInfo;
                            try {
                                eventInfo = eventInfoResolver.resolve();
                            } catch (SQLException e) {
                                return handleSQLException("Get EventInfo", e);
                            }
                            return gameManager.deleteEvent(eventInfo.getEventId());
                        }))
                );
    }
    
    private @NotNull CommandResult handleSQLException(String attemptedAction, SQLException e) {
        Main.logger().log(Level.WARNING, String.format("A database error occurred trying to %s", attemptedAction), e);
        return CommandResult.failure(Component.empty()
                .append(Component.text("A database error occurred. See console for details."))
                .append(Component.newline())
                .append(Component.text(e.getMessage()))
        );
    }
    
    private Permissioned<CommandSourceStack> buildMaxGames() {
        return Permissioned.literal("setMaxGames")
                .then(Permissioned.argument("newCount", IntegerArgumentType.integer())
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            int newCount = ctx.getArgument("newCount", Integer.class);
                            return gameManager.modifyMaxGames(newCount);
                        }))
                );
    }
    
}

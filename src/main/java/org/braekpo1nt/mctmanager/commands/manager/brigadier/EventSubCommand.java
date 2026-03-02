package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class EventSubCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public EventSubCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("event")
                .then(buildStart())
                .then(buildStop())
                .then(buildCreate())
                .then(buildDelete())
                .then(new VoteCommand(gameManager).create())
                ;
    }
    
    private ArgumentBuilder<CommandSourceStack, ?> buildStart() {
        return Commands.literal("start")
                .then(Commands.argument("eventId", StringArgumentType.word())
                        .then(Commands.argument("numberOfGames", IntegerArgumentType.integer())
                                .executes(BrigadierAdapters.wraps(ctx -> {
                                    String eventId = ctx.getArgument("eventId", String.class);
                                    int maxGames = ctx.getArgument("numberOfGames", Integer.class);
                                    return executeStart(eventId, maxGames, 1);
                                }))
                                .then(Commands.argument("currentGameNumber", IntegerArgumentType.integer())
                                        .executes(BrigadierAdapters.wraps(ctx -> {
                                            String eventId = ctx.getArgument("eventId", String.class);
                                            int maxGames = ctx.getArgument("numberOfGames", Integer.class);
                                            int currentGameNumber = ctx.getArgument("currentGameNumber", Integer.class);
                                            return executeStart(eventId, maxGames, currentGameNumber);
                                        }))
                                )
                        )
                );
    }
    
    private CommandResult executeStart(String eventId, int maxGames, int currentGameNumber) {
        EventInfo eventInfo;
        try {
            eventInfo = gameManager.getEventService().getEventInfo(eventId);
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, String.format("A database error occurred retrieving EventInfo with eventId \"%s\"", eventId), e);
            return CommandResult.failure(Component.empty()
                    .append(Component.text("A database error occurred getting the info for eventId"))
                    .append(Component.text(eventId)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". See console for more details."))
                    .append(Component.newline())
                    .append(Component.text(e.getMessage()))
            );
        }
        if (eventInfo == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(eventId)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a valid eventId."))
            );
        }
        return gameManager.startEvent(eventInfo, maxGames, currentGameNumber);
    }
    
    private ArgumentBuilder<CommandSourceStack, ?> buildStop() {
        return Commands.literal("stop")
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
                .then(Commands.literal("confirm")
                        .executes(BrigadierAdapters.wraps(ctx -> gameManager.stopEvent()))
                )
                ;
    }
    
    private ArgumentBuilder<CommandSourceStack, ?> buildCreate() {
        return Commands.literal("create")
                .then(Commands.argument("eventId", StringArgumentType.word())
                        .then(Commands.argument("eventDate", StringArgumentType.word())
                                .then(Commands.argument("plainTextName", StringArgumentType.string())
                                        .then(Commands.argument("componentName", ArgumentTypes.component())
                                                .executes(BrigadierAdapters.wraps(this::executeCreate))
                                        )
                                )
                        )
                );
    }
    
    private CommandResult executeCreate(CommandContext<CommandSourceStack> ctx) {
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
    
    private ArgumentBuilder<CommandSourceStack, ?> buildDelete() {
        return Commands.literal("delete")
                .then(Commands.argument("eventId", StringArgumentType.word())
                        .suggests((ctx, builder) -> CompletableFuture.supplyAsync(() -> {
                            try {
                                List<String> eventIds = gameManager.getEventIds();
                                for (String eventId : eventIds) {
                                    builder.suggest(eventId);
                                }
                            } catch (SQLException e) {
                                Main.logger().log(Level.WARNING, "Can't get eventIds from the database", e);
                            }
                            return builder.build();
                        }))
                        .executes(ctx -> {
                            String eventId = ctx.getArgument("eventId", String.class);
                            CommandResult commandResult = gameManager.deleteEvent(eventId);
                            CommandResult.showResult(ctx.getSource().getSender(), commandResult);
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
    
}

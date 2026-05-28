package org.braekpo1nt.mctmanager.commands.mct.event;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

public class EventCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public EventCommand(@NotNull GameManager gameManager) {
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
                // TODO: fix this command
//                .then(new EventApplyPresetCommand(gameManager, plugin).create())
                .then(buildMaxGames())
                .then(buildWhitelist())
                ;
    }
    
    private Permissioned<CommandSourceStack> buildStart() {
        return Permissioned.literal("start")
                .then(Permissioned.argument("eventId", new EventInfoArgumentType(gameManager.getEventService()))
                        .then(Permissioned.argument("numberOfGames", IntegerArgumentType.integer())
                                .executes(BrigadierAdapters.wrapsFuture(ctx -> {
                                    try {
                                        EventInfoResolver eventInfoResolver = ctx.getArgument("eventId", EventInfoResolver.class);
                                        EventInfo eventInfo = eventInfoResolver.resolve();
                                        int maxGames = ctx.getArgument("numberOfGames", Integer.class);
                                        return gameManager.startEvent(eventInfo, maxGames, 1);
                                    } catch (SQLException e) {
                                        return CommandResult.sqlException("get EventInfo", e).asFuture();
                                    }
                                }))
                                .then(Permissioned.argument("currentGameNumber", IntegerArgumentType.integer())
                                        .executes(BrigadierAdapters.wrapsFuture(ctx -> {
                                            try {
                                                EventInfoResolver eventInfoResolver = ctx.getArgument("eventId", EventInfoResolver.class);
                                                EventInfo eventInfo = eventInfoResolver.resolve();
                                                int maxGames = ctx.getArgument("numberOfGames", Integer.class);
                                                int currentGameNumber = ctx.getArgument("currentGameNumber", Integer.class);
                                                return gameManager.startEvent(eventInfo, maxGames, currentGameNumber);
                                            } catch (SQLException e) {
                                                return CommandResult.sqlException("get EventInfo", e).asFuture();
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
                        .executes(BrigadierAdapters.wrapsFuture(ctx -> gameManager.stopEvent()))
                )
                ;
    }
    
    private Permissioned<CommandSourceStack> buildCreate() {
        return Permissioned.literal("create")
                .then(Permissioned.argument("eventId", StringArgumentType.word())
                        .then(Permissioned.argument("eventDate", StringArgumentType.word())
                                .suggests(TimeStringUtils::suggestDate)
                                .then(Permissioned.argument("plainTextName", StringArgumentType.string())
                                        .then(Permissioned.argument("componentName", gameManager.getComponentArgumentType())
                                                .executes(BrigadierAdapters.wrapsFuture(this::executeCreate))
                                                .then(Permissioned.argument("canonical", BoolArgumentType.bool())
                                                        .executes(BrigadierAdapters.wrapsFuture(this::executeCreateCanonical))
                                                )
                                        )
                                )
                        )
                );
    }
    
    private @NotNull CompletableFuture<CommandResult> executeCreate(CommandContext<CommandSourceStack> ctx) {
        return createEvent(ctx, true);
    }
    
    private @NotNull CompletableFuture<CommandResult> executeCreateCanonical(CommandContext<CommandSourceStack> ctx) {
        boolean canonical = ctx.getArgument("canonical", Boolean.class);
        return createEvent(ctx, canonical);
    }
    
    private CompletableFuture<CommandResult> createEvent(CommandContext<CommandSourceStack> ctx, boolean canonical) {
        String eventId = ctx.getArgument("eventId", String.class);
        String eventDateString = ctx.getArgument("eventDate", String.class);
        Date eventDate;
        try {
            eventDate = TimeStringUtils.parseDate(eventDateString);
        } catch (DateTimeParseException e) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Could not parse date string "))
                    .append(Component.text(eventDateString)
                            .decorate(TextDecoration.BOLD))
            ).asFuture();
        }
        String plainTextName = ctx.getArgument("plainTextName", String.class);
        Component componentName = ctx.getArgument("componentName", Component.class);
        return gameManager.createEvent(eventId, eventDate, plainTextName, componentName, canonical);
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
                                return CommandResult.sqlException("Get EventInfo", e);
                            }
                            return gameManager.deleteEvent(eventInfo.getEventId());
                        }))
                );
    }
    
    private Permissioned<CommandSourceStack> buildMaxGames() {
        return Permissioned.literal("setMaxGames")
                .then(Permissioned.argument("newCount", IntegerArgumentType.integer())
                        .executes(BrigadierAdapters.wrapsFuture(ctx -> {
                            int newCount = ctx.getArgument("newCount", Integer.class);
                            return gameManager.modifyMaxGames(newCount);
                        }))
                );
    }
    
    private Permissioned<CommandSourceStack> buildWhitelist() {
        return Permissioned.literal("whitelist")
                .then(Permissioned.literal("addAll")
                        .executes(BrigadierAdapters.wraps(ctx -> gameManager.whitelist(true)))
                )
                .then(Permissioned.literal("removeAll")
                        .executes(BrigadierAdapters.wraps(ctx -> gameManager.whitelist(false)))
                )
                ;
    }
    
}

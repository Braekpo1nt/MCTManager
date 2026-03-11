package org.braekpo1nt.mctmanager.commands.mct.event;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
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

// TODO: if the command doesn't change anything, don't perform the operation (e.g. setting the date to the same date shouldn't update the date
public class ModifyCommand implements BrigadierSubCommand {
    
    public final @NotNull GameManager gameManager;
    
    public ModifyCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("modify")
                .then(Permissioned.argument("eventId", new EventInfoArgumentType(gameManager.getEventService()))
                        .then(Permissioned.literal("eventDate")
                                .then(Permissioned.argument("date", StringArgumentType.word())
                                        .suggests(TimeStringUtils::suggestDate)
                                        .executes(BrigadierAdapters.wraps(this::executeModifyEventDate))
                                )
                        )
                        .then(Permissioned.literal("componentName")
                                .then(Permissioned.argument("component", gameManager.getComponentArgumentType())
                                        .executes(BrigadierAdapters.wraps(this::executeModifyComponentName))
                                )
                        )
                        .then(Permissioned.literal("canonical")
                                .then(Permissioned.argument("isCanon", BoolArgumentType.bool())
                                        .executes(BrigadierAdapters.wraps(this::executeModifyCanonical))
                                )
                        )
                        .then(Permissioned.literal("plainTextName")
                                .then(Permissioned.argument("name", StringArgumentType.string())
                                        .executes(BrigadierAdapters.wraps(this::executeModifyPlainName))
                                )
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeModifyEventDate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        
        EventInfoResolver eventInfoResolver = ctx.getArgument("eventId", EventInfoResolver.class);
        String eventDateString = ctx.getArgument("date", String.class);
        java.util.Date eventDate;
        try {
            eventDate = TimeStringUtils.parseDate(eventDateString);
        } catch (DateTimeParseException e) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Could not parse date string "))
                    .append(Component.text(eventDateString)
                            .decorate(TextDecoration.BOLD)));
        }
        try {
            EventInfo eventInfo = eventInfoResolver.resolve();
            Date oldDate = eventInfo.getEventDate();
            eventInfo.setEventDate(eventDate);
            gameManager.getEventService().update(eventInfo);
            return CommandResult.success(Component.empty()
                    .append(Component.text("Set date for "))
                    .append(Component.text(eventInfo.getEventId())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to "))
                    .append(Component.text(TimeStringUtils.toString(eventDate)))
                    .append(Component.text(" (was "))
                    .append(Component.text(TimeStringUtils.toString(oldDate)))
                    .append(Component.text(")"))
            );
        } catch (SQLException e) {
            return EventSubCommand.handleSQLException("change event date", e);
        }
    }
    
    private @NotNull CommandResult executeModifyComponentName(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        EventInfoResolver eventInfoResolver = ctx.getArgument("eventId", EventInfoResolver.class);
        Component componentName = ctx.getArgument("component", Component.class);
        try {
            EventInfo eventInfo = eventInfoResolver.resolve();
            Component oldComponentName = eventInfo.getComponentName();
            eventInfo.setComponentName(componentName);
            gameManager.getEventService().update(eventInfo);
            return CommandResult.success(Component.empty()
                    .append(Component.text("Set component name for "))
                    .append(Component.text(eventInfo.getEventId())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to "))
                    .append(componentName)
                    .append(Component.text(" (was "))
                    .append(oldComponentName)
                    .append(Component.text(")"))
            );
        } catch (SQLException e) {
            return EventSubCommand.handleSQLException("change component name", e);
        }
    }
    
    private @NotNull CommandResult executeModifyCanonical(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        EventInfoResolver eventInfoResolver = ctx.getArgument("eventId", EventInfoResolver.class);
        boolean isCanon = ctx.getArgument("isCanon", Boolean.class);
        try {
            EventInfo eventInfo = eventInfoResolver.resolve();
            boolean oldValue = eventInfo.isCanonical();
            eventInfo.setCanonical(isCanon);
            gameManager.getEventService().update(eventInfo);
            return CommandResult.success(Component.empty()
                    .append(Component.text("Set canonical for "))
                    .append(Component.text(eventInfo.getEventId())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to "))
                    .append(Component.text(isCanon))
                    .append(Component.text(" (was "))
                    .append(Component.text(oldValue))
                    .append(Component.text(")"))
            );
        } catch (SQLException e) {
            return EventSubCommand.handleSQLException("change canonical value", e);
        }
    }
    
    private @NotNull CommandResult executeModifyPlainName(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        EventInfoResolver eventInfoResolver = ctx.getArgument("eventId", EventInfoResolver.class);
        String plainTextName = ctx.getArgument("name", String.class);
        try {
            EventInfo eventInfo = eventInfoResolver.resolve();
            String oldName = eventInfo.getPlainTextName();
            eventInfo.setPlainTextName(plainTextName);
            gameManager.getEventService().update(eventInfo);
            return CommandResult.success(Component.empty()
                    .append(Component.text("Set plaintext name for "))
                    .append(Component.text(eventInfo.getEventId())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to "))
                    .append(Component.text(plainTextName))
                    .append(Component.text(" (was "))
                    .append(Component.text(oldName))
                    .append(Component.text(")"))
            );
        } catch (SQLException e) {
            return EventSubCommand.handleSQLException("change eventId", e);
        }
    }
}

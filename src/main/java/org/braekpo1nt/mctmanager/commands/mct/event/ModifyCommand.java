package org.braekpo1nt.mctmanager.commands.mct.event;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.Date;

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
                                        .suggests(EventSubCommand::suggestDate)
                                        .executes(BrigadierAdapters.wraps(this::executeModifyEventDate))
                                )
                        )
                        .then(Permissioned.literal("componentName")
                                .then(Permissioned.argument("component", gameManager.getComponentArgumentType())
                                        .executes(BrigadierAdapters.wraps(this::executeModifyComponentName))
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
                    .append(Component.text(oldDate))
            );
        } catch (SQLException e) {
            return EventSubCommand.handleSQLException("Get EventInfo", e);
        }
    }
    
    private @NotNull CommandResult executeModifyComponentName(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        EventInfoResolver eventInfoResolver = ctx.getArgument("eventId", EventInfoResolver.class);
        String eventDateString = ctx.getArgument("date", String.class);
        try {
            EventInfo eventInfo = eventInfoResolver.resolve();
            
        } catch (SQLException e) {
            return EventSubCommand.handleSQLException("Get EventInfo", e);
        }
        return null;
    }
}

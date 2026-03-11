package org.braekpo1nt.mctmanager.commands.argumenttypes;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.service.EventService;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class EventInfoResolver {
    
    private static final DynamicCommandExceptionType ERROR_EVENT_DOES_NOT_EXIST = new DynamicCommandExceptionType(eventId -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("No event with id "))
            .append(Component.text(eventId.toString())
                    .decorate(TextDecoration.BOLD))
    ));
    
    private final @NotNull EventService eventService;
    private final @NotNull String eventId;
    
    public EventInfoResolver(@NotNull EventService eventService, @NotNull String eventId) {
        this.eventService = eventService;
        this.eventId = eventId;
    }
    
    /**
     * @return the {@link EventInfo} with the eventId given to the argument type
     * @throws SQLException if there is an issue communicating with the database
     * @throws CommandSyntaxException if the event does not exist
     */
    public @NotNull EventInfo resolve() throws SQLException, CommandSyntaxException {
        EventInfo eventInfo = eventService.getEventInfo(eventId);
        if (eventInfo == null) {
            throw ERROR_EVENT_DOES_NOT_EXIST.create(eventId);
        }
        return eventInfo;
    }
    
}

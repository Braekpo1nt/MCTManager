package org.braekpo1nt.mctmanager.commands.argumenttypes;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.jetbrains.annotations.NotNull;

/**
 * Argument resolvers allow the more intense work of resolving an argument to
 * be performed only when the command is executed. Also allows for certain tasks
 * to be performed asynchronously, as long as they don't access the bukkit api.
 * <br>
 * This one resolves an {@link OfflineParticipant} from a name
 */
public class OfflineParticipantResolver {
    
    private static final DynamicCommandExceptionType ERROR_PARTICIPANT_DOES_NOT_EXIST = new DynamicCommandExceptionType(name -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("Player "))
            .append(Component.text(name.toString())
                    .decorate(TextDecoration.BOLD))
            .append(Component.text(" is not a on a team."))
    ));
    
    private final @NotNull GameManager gameManager;
    private final String ign;
    
    public OfflineParticipantResolver(@NotNull GameManager gameManager, @NotNull String ign) {
        this.gameManager = gameManager;
        this.ign = ign;
    }
    
    public @NotNull OfflineParticipant resolve() throws CommandSyntaxException {
        OfflineParticipant offlineParticipant = gameManager.getOfflineParticipant(ign);
        if (offlineParticipant == null) {
            throw ERROR_PARTICIPANT_DOES_NOT_EXIST.create(ign);
        }
        return offlineParticipant;
    }
    
}

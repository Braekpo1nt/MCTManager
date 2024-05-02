package org.braekpo1nt.mctmanager.commands.commandmanager;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandResult {
    
    /**
     * @return the message to send to the command sender(s)
     */
    @Nullable Component getMessage();
    
    static CommandResult succeeded(@Nullable Component message) {
        return new SuccessCommandResult(message);
    }
    
    static CommandResult succeeded() {
        return new SuccessCommandResult(null);
    }
    
    static CommandResult failed(@Nullable Component message) {
        return new FailureCommandResult(message);
    }
    
    static CommandResult failed(@NotNull String message) {
        return new FailureCommandResult(Component.text(message));
    }
}

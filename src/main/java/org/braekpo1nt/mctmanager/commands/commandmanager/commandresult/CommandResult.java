package org.braekpo1nt.mctmanager.commands.commandmanager.commandresult;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandResult {
    
    /**
     * @return the message to send to the command sender(s)
     */
    @Nullable Component getMessage();
    
    @NotNull CommandResult and(CommandResult other);
    
    static CommandResult success(@Nullable Component message) {
        return new SuccessCommandResult(message);
    }
    
    static CommandResult success() {
        return new SuccessCommandResult(null);
    }
    
    static CommandResult failure(@Nullable Component message) {
        return new FailureCommandResult(message);
    }
    
    static CommandResult failure(@NotNull String message) {
        return new FailureCommandResult(Component.text(message));
    }
}

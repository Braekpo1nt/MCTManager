package org.braekpo1nt.mctmanager.commands.commandmanager;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public interface CommandResult {
    
    /**
     * @return the message to send to the command sender(s)
     */
    Component getMessage();
    
    static CommandResult succeeded(@Nullable Component message) {
        return new SuccessCommandResult(message);
    }
    
    static CommandResult failed(@Nullable Component message) {
        return new FailureCommandResult(message);
    }
}

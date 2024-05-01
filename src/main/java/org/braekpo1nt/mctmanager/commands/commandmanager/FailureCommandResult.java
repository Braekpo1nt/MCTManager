package org.braekpo1nt.mctmanager.commands.commandmanager;

import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;

@AllArgsConstructor
public class FailureCommandResult implements CommandResult {
    private Component message;
    
    @Override
    public Component getMessage() {
        return message;
    }
}

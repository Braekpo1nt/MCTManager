package org.braekpo1nt.mctmanager.commands.commandmanager.commandresult;

import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;

@AllArgsConstructor
public class SuccessCommandResult implements CommandResult {
    protected Component message;
    
    @Override
    public Component getMessage() {
        return message;
    }
}

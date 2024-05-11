package org.braekpo1nt.mctmanager.commands.manager.commandresult;

import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public class SuccessCommandResult implements CommandResult {
    protected Component message;
    
    @Override
    public @Nullable Component getMessage() {
        return message;
    }
    
    @Override
    public @NotNull CommandResult and(CommandResult other) {
        return new CompositeCommandResult(this, other);
    }
}

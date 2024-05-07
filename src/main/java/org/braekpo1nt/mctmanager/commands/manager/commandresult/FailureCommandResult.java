package org.braekpo1nt.mctmanager.commands.manager.commandresult;

import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public class FailureCommandResult implements CommandResult {
    private Component message;
    
    @Override
    public @Nullable Component getMessage() {
        return message.color(NamedTextColor.RED);
    }
    
    @Override
    public @NotNull CommandResult and(CommandResult other) {
        return new CompositeCommandResult(this, other);
    }
}

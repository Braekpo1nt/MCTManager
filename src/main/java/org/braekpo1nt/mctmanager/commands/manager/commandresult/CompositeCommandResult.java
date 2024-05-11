package org.braekpo1nt.mctmanager.commands.manager.commandresult;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CompositeCommandResult implements CommandResult {
    
    protected final List<CommandResult> commandResults = new ArrayList<>();
    
    public CompositeCommandResult(CommandResult first, CommandResult second) {
        commandResults.add(first);
        commandResults.add(second);
    }
    
    @Override
    public @Nullable Component getMessage() {
        TextComponent.Builder result = Component.text();
        for (int i = 0; i < commandResults.size(); i++) {
            CommandResult commandResult = commandResults.get(i);
            if (commandResult.getMessage() != null) {
                result.append(commandResult.getMessage());
                if (i < commandResults.size() - 1) {
                    result.append(Component.newline());
                }
            }
        }
        return result.asComponent();
    }
    
    @Override
    public @NotNull CommandResult and(CommandResult other) {
        commandResults.add(other);
        return this;
    }
}

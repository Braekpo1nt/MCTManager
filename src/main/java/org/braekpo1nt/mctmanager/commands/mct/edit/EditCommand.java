package org.braekpo1nt.mctmanager.commands.mct.edit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.games.GameManager;

public class EditCommand extends CommandManager {
    
    public EditCommand(GameManager gameManager) {
        subCommands.put("start", new StartSubCommand(gameManager));
        subCommands.put("stop", new StopSubCommand(gameManager));
        subCommands.put("validate", (sender, command, label, args) -> {
            
            if (!gameManager.editorIsRunning()) {
                sender.sendMessage(Component.text("No editor is running.")
                        .color(NamedTextColor.RED));
                return true;
            }
            
            gameManager.validateEditor(sender);
            
            return true;
        });
        subCommands.put("save", (sender, command, label, args) -> {
            if (!gameManager.editorIsRunning()) {
                sender.sendMessage(Component.text("No editor is running.")
                        .color(NamedTextColor.RED));
                return true;
            }
            
            boolean force = false;
            if (args.length == 1) {
                String forceString = args[0];
                Boolean forceBoolean = CommandUtils.toBoolean(forceString);
                if (forceBoolean == null) {
                    sender.sendMessage(Component.empty()
                                    .append(Component.text(forceString)
                                            .decorate(TextDecoration.BOLD))
                                    .append(Component.text(" is not a valid boolean"))
                            .color(NamedTextColor.RED));
                    return true;
                }
                force = forceBoolean;
            }
            
            gameManager.saveEditor(sender, force);
            
            return true;
        });
        subCommands.put("load", (sender, command, label, args) -> {
            if (!gameManager.editorIsRunning()) {
                sender.sendMessage(Component.text("No editor is running.")
                        .color(NamedTextColor.RED));
                return true;
            }
            gameManager.loadEditor(sender);
            return true;
        });
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct edit <options>");
    }
}

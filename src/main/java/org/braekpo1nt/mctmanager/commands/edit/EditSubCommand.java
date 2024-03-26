package org.braekpo1nt.mctmanager.commands.edit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;

public class EditSubCommand extends CommandManager {
    
    public EditSubCommand(GameManager gameManager) {
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
            
            gameManager.saveEditor(sender, false);
            
            return true;
        });
//        subCommands.put("load", new LoadSubCommand(gameManager));
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct edit <options>");
    }
}

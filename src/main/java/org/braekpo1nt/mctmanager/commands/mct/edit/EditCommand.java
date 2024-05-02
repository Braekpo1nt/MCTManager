package org.braekpo1nt.mctmanager.commands.mct.edit;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.commands.commandmanager.SubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class EditCommand extends CommandManager {
    
    public EditCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new StartSubCommand(gameManager, "start"));
        addSubCommand(new StopSubCommand(gameManager, "stop"));
        addSubCommand(new SubCommand() {
            @Override
            public @NotNull String getName() {
                return "validate";
            }
    
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (!gameManager.editorIsRunning()) {
                    return CommandResult.failure(Component.text("No editor is running."));
                }
                gameManager.validateEditor(sender);
                return CommandResult.success();
            }
        });
//        subCommands.put("save", (sender, command, label, args) -> {
//            if (!gameManager.editorIsRunning()) {
//                sender.sendMessage(Component.text("No editor is running.")
//                        .color(NamedTextColor.RED));
//                return true;
//            }
//            
//            boolean force = false;
//            if (args.length == 1) {
//                String forceString = args[0];
//                Boolean forceBoolean = CommandUtils.toBoolean(forceString);
//                if (forceBoolean == null) {
//                    sender.sendMessage(Component.empty()
//                                    .append(Component.text(forceString)
//                                            .decorate(TextDecoration.BOLD))
//                                    .append(Component.text(" is not a valid boolean"))
//                            .color(NamedTextColor.RED));
//                    return true;
//                }
//                force = forceBoolean;
//            }
//            
//            gameManager.saveEditor(sender, force);
//            
//            return true;
//        });
//        subCommands.put("load", (sender, command, label, args) -> {
//            if (!gameManager.editorIsRunning()) {
//                sender.sendMessage(Component.text("No editor is running.")
//                        .color(NamedTextColor.RED));
//                return true;
//            }
//            gameManager.loadEditor(sender);
//            return true;
//        });
    }
}

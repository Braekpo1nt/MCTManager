package org.braekpo1nt.mctmanager.commands.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;

public class EventSubCommand extends CommandManager {
    
    public EventSubCommand(GameManager gameManager) {
        subCommands.put("start", (sender, command, label, args) -> {
            int maxGames = 6;
            if (args.length != 1) {
                sender.sendMessage(Component.text("Usage: /mct event start <number of games>")
                        .color(NamedTextColor.RED));
                return true;
            }
            String maxGamesString = args[0];
            if (!CommandUtils.isInteger(maxGamesString)) {
                sender.sendMessage(Component.empty()
                        .append(Component.text(maxGamesString)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not an integer"))
                        .color(NamedTextColor.RED));
                return true;
            }
            maxGames = Integer.parseInt(maxGamesString);
            gameManager.getEventManager().startEvent(sender, maxGames);
            return true;
        });
        subCommands.put("stop", (sender, command, label, args) -> {
            if (!gameManager.getEventManager().eventIsActive()) {
                sender.sendMessage(Component.text("There is no event running.")
                        .color(NamedTextColor.RED));
                return true;
            }
            if (args.length != 1) {
                sender.sendMessage(Component.text("Are you sure? Type ")
                        .append(Component.empty()
                                .append(Component.text("/mct event stop "))
                                .append(Component.text("confirm")
                                        .decorate(TextDecoration.BOLD))
                                .decorate(TextDecoration.ITALIC))
                        .append(Component.text(" to confirm."))
                        .color(NamedTextColor.YELLOW));
                return true;
            }
            String confirmString = args[0];
            if (!confirmString.equals("confirm")) {
                sender.sendMessage(Component.empty()
                        .append(Component.text(confirmString))
                        .append(Component.text(" is not a recognized option."))
                        .color(NamedTextColor.RED));
                return true;
            }
            gameManager.getEventManager().stopEvent(sender);
            return true;
        });
        subCommands.put("pause", (sender, command, label, args) -> {
            gameManager.getEventManager().pauseEvent(sender);
            return true;
        });
        subCommands.put("resume", (sender, command, label, args) -> {
            gameManager.getEventManager().resumeEvent(sender);
            return true;
        });
        subCommands.put("finalgame", new FinalGameSubCommand(gameManager));
        subCommands.put("undo", (sender, command, label, args) -> {
            if (args.length != 1) {
                sender.sendMessage(Component.text("Usage: /mct event undo <game>")
                        .color(NamedTextColor.RED));
                return true;
            }
            String gameID = args[0];
            GameType gameType = GameType.fromID(gameID);
            if (gameType == null) {
                sender.sendMessage(Component.text(gameID)
                        .append(Component.text(" is not a valid game"))
                        .color(NamedTextColor.RED));
                return true;
            }
            gameManager.getEventManager().undoGame(sender, gameType);
            return true;
        });
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct event <options>");
    }
       
}

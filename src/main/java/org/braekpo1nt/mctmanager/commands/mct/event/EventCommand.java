package org.braekpo1nt.mctmanager.commands.mct.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.commands.commandmanager.OldCommandManager;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.commandmanager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.Usage;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventCommand extends CommandManager {
    
    public EventCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new TabSubCommand("start") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                int maxGames = 6;
                if (args.length != 1) {
                    return CommandResult.failure(getUsage().of("<numberOfGame>"));
                }
                String maxGamesString = args[0];
                if (!CommandUtils.isInteger(maxGamesString)) {
                    return CommandResult.failure(Component.empty()
                            .append(Component.text(maxGamesString)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not an integer")));
                }
                maxGames = Integer.parseInt(maxGamesString);
                gameManager.getEventManager().startEvent(sender, maxGames);
                return CommandResult.success();
            }
            
            @Override
            public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                return Collections.emptyList();
            }
        });
//        subCommands.put("stop", new TabExecutor() {
//            @Override
//            public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//                if (!gameManager.getEventManager().eventIsActive()) {
//                    sender.sendMessage(Component.text("There is no event running.")
//                            .color(NamedTextColor.RED));
//                    return true;
//                }
//                if (args.length != 1) {
//                    sender.sendMessage(Component.text("Are you sure? Type ")
//                            .append(Component.empty()
//                                    .append(Component.text("/mct event stop "))
//                                    .append(Component.text("confirm")
//                                            .decorate(TextDecoration.BOLD))
//                                    .decorate(TextDecoration.ITALIC))
//                            .append(Component.text(" to confirm."))
//                            .color(NamedTextColor.YELLOW));
//                    return true;
//                }
//                String confirmString = args[0];
//                if (!confirmString.equals("confirm")) {
//                    sender.sendMessage(Component.empty()
//                            .append(Component.text(confirmString))
//                            .append(Component.text(" is not a recognized option."))
//                            .color(NamedTextColor.RED));
//                    return true;
//                }
//                gameManager.getEventManager().stopEvent(sender);
//                return true;
//            }
//            
//            @Override
//            public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//                    return Collections.emptyList();
//            }
//        });
//        subCommands.put("pause", (sender, command, label, args) -> {
//            gameManager.getEventManager().pauseEvent(sender);
//            return true;
//        });
//        subCommands.put("resume", (sender, command, label, args) -> {
//            gameManager.getEventManager().resumeEvent(sender);
//            return true;
//        });
//        subCommands.put("finalgame", new FinalGameSubCommand(gameManager));
//        subCommands.put("undo", new TabExecutor() {
//            @Override
//            public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//                if (args.length != 2) {
//                    sender.sendMessage(Component.text("Usage: /mct event undo <game> <iteration>")
//                            .color(NamedTextColor.RED));
//                    return true;
//                }
//                String gameID = args[0];
//                GameType gameType = GameType.fromID(gameID);
//                if (gameType == null) {
//                    sender.sendMessage(Component.text(gameID)
//                            .append(Component.text(" is not a valid game"))
//                            .color(NamedTextColor.RED));
//                    return true;
//                }
//                int iterationNumber = -1;
//                try {
//                    iterationNumber = Integer.parseInt(args[1]);
//                } catch (NumberFormatException e) {
//                    sender.sendMessage(Component.text(args[1])
//                            .append(Component.text(" is not a valid integer"))
//                            .color(NamedTextColor.RED));
//                    return true;
//                }
//                gameManager.getEventManager().undoGame(sender, gameType, iterationNumber - 1);
//                return true;
//            }
//            
//            @Override
//            public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//                if (args.length != 2) {
//                    return Collections.emptyList();
//                }
//                String gameID = args[0];
//                GameType gameType = GameType.fromID(gameID);
//                if (gameType == null) {
//                    return Collections.emptyList();
//                }
//                int iterations = gameManager.getEventManager().getGameIterations(gameType);
//                if (iterations <= 0) {
//                    return Collections.emptyList();
//                }
//                return generateNumberList(iterations);
//            }
//        });
//        subCommands.put("vote", new VoteSubCommand(gameManager));
//        subCommands.put("modify", new ModifySubCommand(gameManager));
    }
    
    /**
     * @param n the number to generate numbers up to (must be at least 1 to get any entries)
     * @return a list containing the numbers 1 through n inclusive as Strings, in increasing order. If n is less than 1, an empty list is produced. 
     */
    public static List<String> generateNumberList(int n) {
        List<String> numbers = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            numbers.add(Integer.toString(i));
        }
        return numbers;
    }
    
    @Override
    protected @NotNull Usage getUsageOptions() {
        return new Usage("<options>");
    }
}

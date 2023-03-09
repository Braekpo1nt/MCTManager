package org.braekpo1nt.mctmanager.commands;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class MCTStartGameCommand implements TabExecutor {
    
    private final GameManager gameManager;
    
    public MCTStartGameCommand(Main plugin, GameManager gameManager) {
        this.gameManager = gameManager;
        plugin.getCommand("mctstartgame").setExecutor(this);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            printGameNumber(args[0], ((Player) sender));
        }
        return true;
    }
    
    public void printGameNumber(String gameName, Player player) {
        switch (gameName) {
            case "foot-race":
                gameManager.startFootRace();
                break;
//            case "mecha":
//                player.sendMessage("2");
//                break;
//            case "bedwars":
//                player.sendMessage("3");
//                break;
//            case "capture-the-flag":
//                player.sendMessage("4");
//                break;
//            case "dodgeball":
//                player.sendMessage("5");
//                break;
//            case "spleef":
//                player.sendMessage("6");
//                break;
//            case "parkour-pathway":
//                player.sendMessage("7");
//                break;
            default:
                player.sendMessage("Unknown game: " + gameName);
                break;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
//            return Arrays.asList("foot-race", "mecha", "bedwars", "dodgeball", "capture-the-flag", "spleef", "parkour-pathway").stream().sorted().toList();
            return Arrays.asList("foot-race");
        }
        return null;
    }
}

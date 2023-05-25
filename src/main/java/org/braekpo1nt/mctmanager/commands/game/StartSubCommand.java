package org.braekpo1nt.mctmanager.commands.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles starting games
 */
public class StartSubCommand implements TabExecutor {
    
    private final GameManager gameManager;
    private final Map<String, MCTGames> mctGames = new HashMap<>();
    
    public StartSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
        mctGames.put("foot-race", MCTGames.FOOT_RACE);
        mctGames.put("mecha", MCTGames.MECHA);
        mctGames.put("capture-the-flag", MCTGames.CAPTURE_THE_FLAG);
        mctGames.put("spleef", MCTGames.SPLEEF);
        mctGames.put("parkour-pathway", MCTGames.PARKOUR_PATHWAY);
        mctGames.put("clockwork", MCTGames.CLOCKWORK);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /mct game start <game>");
            return true;
        }
        if (args.length == 1) {
            String gameName = args[0];
            if (!mctGames.containsKey(gameName)) {
                sender.sendMessage(Component.text(gameName)
                        .append(Component.text(" is not a valid game name."))
                        .color(NamedTextColor.RED));
                return true;
            }
            MCTGames mctGame = mctGames.get(gameName);
            gameManager.startGame(mctGame, sender);
        }
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
//            return Arrays.asList("foot-race", "mecha", "bedwars", "dodgeball", "capture-the-flag", "spleef", "parkour-pathway").stream().sorted().toList();
            return mctGames.keySet().stream().sorted().toList();
        }
        return null;
    }
}

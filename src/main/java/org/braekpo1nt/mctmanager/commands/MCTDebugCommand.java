package org.braekpo1nt.mctmanager.commands;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements CommandExecutor {
    
    private final Main plugin;
    private int boarderShrinkingTaskId;
    private final WorldBorder worldBorder;
    
    public MCTDebugCommand(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("mctdebug").setExecutor(this);
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        World mechaWorld = worldManager.getMVWorld("FT").getCBWorld();
        this.worldBorder = mechaWorld.getWorldBorder();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
    
        int startIndex = -1;
        int endIndex = -1;
    
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("\"") && startIndex == -1) {
                startIndex = i;
                continue;
            }
        
            if (args[i].endsWith("\"") && endIndex == -1) {
                endIndex = i;
            }
        }
    
        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
            sender.sendMessage("Usage: /mctdebug [args] <\"message text\"> [more args]");
            return false;
        }
    
        StringBuilder beforeBuilder = new StringBuilder();
        for (int i = 0; i < startIndex; i++) {
            beforeBuilder.append(args[i]);
            beforeBuilder.append(" ");
        }
    
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = startIndex; i <= endIndex; i++) {
            messageBuilder.append(args[i]);
            messageBuilder.append(" ");
        }
    
        StringBuilder afterBuilder = new StringBuilder();
        for (int i = endIndex + 1; i < args.length; i++) {
            afterBuilder.append(args[i]);
            afterBuilder.append(" ");
        }
        
        sender.sendMessage("Before substring: " + beforeBuilder.toString().trim());
        String messageWithQuotes = messageBuilder.toString().trim();
        String message = messageWithQuotes.substring(1, messageWithQuotes.length() - 1).trim();
        sender.sendMessage("Substring: " + message);
        sender.sendMessage("After substring: " + afterBuilder.toString().trim());
    
        return true;
        
//        Player player = ((Player) sender).getPlayer();
//        Component mainTitle = Component.text("Main title");
//        Component subTitle = Component.text("Subtitle");
//
//        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
//        Title title = Title.title(mainTitle, subTitle, times);
//        sender.showTitle(title);
        
    }
    
    
    
    
    
}

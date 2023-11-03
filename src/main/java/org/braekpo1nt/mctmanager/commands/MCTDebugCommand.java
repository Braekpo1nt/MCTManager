package org.braekpo1nt.mctmanager.commands;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.xezard.glow.data.glow.Glow;
import ru.xezard.glow.data.glow.IGlow;
import ru.xezard.glow.data.glow.manager.GlowsManager;

import java.util.Collections;
import java.util.List;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements TabExecutor {
    
    private final Main plugin;
    private Glow yellow;
    private Glow cyan;
    private Glow white;
    
    public MCTDebugCommand(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("mctdebug").setExecutor(this);
    }
    
    
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return false;
        }
        String arg = args[0];
        Player braekpo1nt = Bukkit.getPlayer("Braekpo1nt");
        Player rstln = Bukkit.getPlayer("rstln");
        Player kbelleflash = Bukkit.getPlayer("Kbelleflash");
        if (braekpo1nt == null || rstln == null || kbelleflash == null) {
            return true;
        }
        switch (arg) {
            case "show" -> {
                if (yellow == null) {
                    yellow = Glow.builder()
                            .color(ChatColor.YELLOW)
                            .name("yellowGlow")
                            .build();
                }
                yellow.addHolders(rstln);
                yellow.display(rstln);
                yellow.display(braekpo1nt);
                
                if (cyan == null) {
                    cyan = Glow.builder()
                            .color(ChatColor.AQUA)
                            .name("cyanGlow")
                            .build();
                }
                cyan.addHolders(kbelleflash);
                cyan.display(kbelleflash);
                cyan.display(braekpo1nt);
                
                if (white == null) {
                    white = Glow.builder()
                            .color(ChatColor.WHITE)
                            .name("adminGlow")
                            .build();
                }
                white.addHolders(braekpo1nt);
                white.display(kbelleflash);
                white.display(braekpo1nt);
                white.display(rstln);
            }
            case "hide" -> {
                GlowsManager.getInstance().clear();
                yellow = null;
                cyan = null;
                white = null;
            }
            case "hideyellow" -> {
                if (yellow != null) {
                    yellow.hideFrom(rstln);
                }
            }
            case "showyellow" -> {
                if (yellow != null) {
                    yellow.display(rstln);
                }
            }
        }
    
//        Player player = ((Player) sender).getPlayer();
        
//        Component mainTitle = Component.text("Main title");
//        Component subTitle = Component.text("Subtitle");
//
//        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
//        Title title = Title.title(mainTitle, subTitle, times);
//        sender.showTitle(title);
        return true;
    }
    
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}

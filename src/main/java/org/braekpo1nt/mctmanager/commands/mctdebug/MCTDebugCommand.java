package org.braekpo1nt.mctmanager.commands.mctdebug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.sidebar.SidebarFactory;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.ui.timer.TimerManager;
import org.braekpo1nt.mctmanager.ui.topbar.BasicTopbar;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements TabExecutor, Listener {
    
    private final BasicTopbar topbar = new BasicTopbar();
    private final TimerManager timerManager;
    private final Sidebar sidebar;
    private @Nullable Timer timer;
    
    public MCTDebugCommand(Main plugin) {
        plugin.getCommand("mctdebug").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.timerManager = new TimerManager(plugin);
        this.sidebar = new SidebarFactory().createSidebar();
        sidebar.addLine("timer", "");
        sidebar.updateTitle("Title");
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Must be a player to run this command")
                    .color(NamedTextColor.RED));
            return true;
        }
        
//        if (args.length != 0) {
//            sender.sendMessage(Component.text("Usage: /mctdebug <arg> [options]")
//                    .color(NamedTextColor.RED));
//            return true;
//        }
        
        if (args.length > 0) {
            String arg = args[0];
            if (timer == null) {
                sender.sendMessage("no timer yet");
                return true;
            }
            switch (arg) {
                case "pause" -> {
                    timer.pause();
                }
                case "resume" -> {
                    timer.resume();
                }
                case "cancel" -> {
                    timer.cancel();
                }
                case "clear" -> {
                    timer.clear();
                }
                case "skip" -> {
                    timer.skip();
                }
                case "addplayer" -> {
                    if (args.length < 2) {
                        sender.sendMessage("specify player");
                        return true;
                    }
                    Player secondPlayer = Bukkit.getPlayer(args[1]);
                    if (secondPlayer == null) {
                        sender.sendMessage("player doesn't exist");
                        return true;
                    }
                    timer.addTitleAudience(secondPlayer);
                    sidebar.addPlayer(secondPlayer);
                    topbar.showPlayer(secondPlayer);
                }
                case "removeplayer" -> {
                    if (args.length < 2) {
                        sender.sendMessage("specify player");
                        return true;
                    }
                    Player secondPlayer = Bukkit.getPlayer(args[1]);
                    if (secondPlayer == null) {
                        sender.sendMessage("player doesn't exist");
                        return true;
                    }
                    timer.removeTitleAudience(secondPlayer);
                    sidebar.removePlayer(secondPlayer);
                    topbar.hidePlayer(secondPlayer.getUniqueId());
                }
            }
        } else {
            if (timer != null) {
                sidebar.removeAllPlayers();
                topbar.hideAllPlayers();
                timer.cancel();
                timer.clear();
            }
            sidebar.addPlayer(player);
            topbar.showPlayer(player);
            timer = Timer.builder()
                    .onCompletion(() -> {
                        sender.sendMessage("done!");
                        sidebar.removeAllPlayers();
                        topbar.hideAllPlayers();
                    })
                    .duration(20)
                    .titleAudience(player)
                    .withTopbar(topbar)
                    .withSidebar(sidebar, "timer")
                    .build().start(timerManager);
        }
        
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
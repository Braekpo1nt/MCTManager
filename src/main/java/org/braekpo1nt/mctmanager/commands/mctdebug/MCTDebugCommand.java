package org.braekpo1nt.mctmanager.commands.mctdebug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.topbar.BasicTopbar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements TabExecutor, Listener {
    
    private final BasicTopbar topbar = new BasicTopbar();
    private final Main plugin;
    
    public MCTDebugCommand(Main plugin) {
        plugin.getCommand("mctdebug").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Must be a player to run this command")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length != 0) {
            sender.sendMessage(Component.text("Usage: /mctdebug <arg> [options]")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        topbar.showPlayer(player);
        new BukkitRunnable() {
            int count = 15;
            @Override
            public void run() {
                if (count <= 0) {
                    topbar.hidePlayer(player.getUniqueId());
                    player.showTitle(Title.title(Component.empty(), Component.empty()));
                    this.cancel();
                    return;
                }
                String timeString = TimeStringUtils.getTimeString(count);
                if (count <= 10) {
                    Title title = Title.title(Component.text("Starting in"), Component.text(count).color(getColorForTime(count)), Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1500), Duration.ofMillis(0)));
                    player.showTitle(title);
                }
                topbar.setMiddle(Component.text(timeString));
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
//        Component mainTitle = Component.text("Main title");
//        Component subTitle = Component.text("Subtitle");
//
//        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
//        Title title = Title.title(mainTitle, subTitle, times);
//        sender.showTitle(title);
        return true;
    }
    
    private static TextColor getColorForTime(int seconds) {
        switch (seconds) {
            case 3 -> {
                return NamedTextColor.RED;
            }
            case 2 -> {
                return NamedTextColor.YELLOW;
            }
            case 1 -> {
                return NamedTextColor.GREEN;
            }
            default -> {
                return NamedTextColor.WHITE;
            }
        }
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}

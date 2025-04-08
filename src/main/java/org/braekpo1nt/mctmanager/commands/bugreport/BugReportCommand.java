package org.braekpo1nt.mctmanager.commands.bugreport;

import java.util.Collections;
import java.util.List;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class BugReportCommand implements TabExecutor {
    private final GameManager gameManager;

    public BugReportCommand(Main plugin, GameManager gameManager) {
        PluginCommand command = plugin.getCommand("bugreport");
        this.gameManager = gameManager;
        if (command == null) {
            Main.logger().severe("Unable to find the /bugreport command in plugin.yml");
            return;
        }
        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /bugreport <description>", NamedTextColor.RED));
            return true;
        }

        String description = String.join(" ", args);

        gameManager.playSoundForAdmins(Sound.BLOCK_NOTE_BLOCK_PLING.name().toLowerCase(), 1, 1);
        gameManager.getOnlineAdmins().forEach(admin -> {
            admin.showTitle(Title.title(
                    Component.text(""),
                    Component.text(sender.getName() + " reported a bug")));
        });

        // TODO: Logs the bug and the timestamp to a file
        Main.logger().warning(sender.getName() + "reported a bug: " + description);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}

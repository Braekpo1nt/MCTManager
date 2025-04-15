package org.braekpo1nt.mctmanager.commands.notready;

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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class NotReadyCommand implements TabExecutor {
    private final GameManager gameManager;

    public NotReadyCommand(Main plugin, GameManager gameManager) {
        PluginCommand command = plugin.getCommand("notready");
        this.gameManager = gameManager;
        if (command == null) {
            Main.logger().severe("Unable to find the /notready command in plugin.yml");
            return;
        }
        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /notready <reason>", NamedTextColor.RED));
            return true;
        }

        String reason = String.join(" ", args);

        gameManager.playSoundForAdmins(Sound.BLOCK_NOTE_BLOCK_PLING.key().value(), 1, 1);
        Audience.audience(gameManager.getOnlineAdmins()).showTitle(Title.title(
            Component.text(""),
            Component.text(sender.getName() + " is not ready to continue the event")));
        gameManager.messageAdmins(
                Component.text(sender.getName() + " is not ready to continue the event. Reason: " + reason));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}

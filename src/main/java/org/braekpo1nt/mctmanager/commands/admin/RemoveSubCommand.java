package org.braekpo1nt.mctmanager.commands.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class RemoveSubCommand implements TabExecutor {
    
    private final GameManager gameManager;
    
    public RemoveSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            sender.sendMessage(Component.text("Usage: /mct admin remove <admin>")
                    .color(NamedTextColor.RED));
            return true;
        }
        String name = args[0];
        OfflinePlayer admin = Bukkit.getOfflinePlayer(name);
        if (!gameManager.isAdmin(admin.getUniqueId())) {
            sender.sendMessage(Component.empty()
                    .append(Component.text(name)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not an admin"))
                    .color(NamedTextColor.RED));
            return true;
        }
        gameManager.removeAdmin(admin);
        sender.sendMessage(Component.empty()
                .append(Component.text(name)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" is no longer an admin"))
                .color(NamedTextColor.YELLOW));
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return null;
        }
        return Collections.emptyList();
    }
}

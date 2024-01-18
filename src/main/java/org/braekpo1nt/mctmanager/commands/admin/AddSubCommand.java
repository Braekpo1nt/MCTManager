package org.braekpo1nt.mctmanager.commands.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class AddSubCommand implements TabExecutor {
    
    private final GameManager gameManager;
    
    public AddSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            sender.sendMessage(Component.text("Usage: /mct admin add <player>")
                    .color(NamedTextColor.RED));
            return true;
        }
        String name = args[0];
        Player newAdmin = Bukkit.getPlayer(name);
        if (newAdmin == null || !newAdmin.isOnline()) {
            sender.sendMessage(Component.empty()
                    .append(Component.text(name)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not online"))
                    .color(NamedTextColor.RED));
            return true;
        }
        if (gameManager.isAdmin(newAdmin.getUniqueId())) {
            sender.sendMessage(Component.empty()
                    .append(Component.text(name)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is already an admin"))
                    .color(NamedTextColor.YELLOW));
            return true;
        }
        if (gameManager.isParticipant(newAdmin.getUniqueId())) {
            sender.sendMessage(Component.empty()
                    .append(Component.text(name)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is a participant, and can't be an admin"))
                    .color(NamedTextColor.YELLOW));
            return true;
        }
        gameManager.addAdmin(newAdmin);
        sender.sendMessage(Component.empty()
                .append(Component.text("Added "))
                .append(Component.text(name)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" as an admin"))
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

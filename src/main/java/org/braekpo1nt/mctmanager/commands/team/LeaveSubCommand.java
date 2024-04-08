package org.braekpo1nt.mctmanager.commands.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class LeaveSubCommand implements CommandExecutor {
    private final GameManager gameManager;
    
    public LeaveSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {
            sender.sendMessage(Component.text("Usage: /mct team leave <member>")
                    .color(NamedTextColor.RED));
            return true;
        }
        String playerName = args[0];
        OfflinePlayer playerToLeave = Bukkit.getOfflinePlayer(playerName);
        if (!gameManager.isParticipant(playerToLeave.getUniqueId())) {
            sender.sendMessage(String.format("Player %s is not on a team.", playerName));
            sender.sendMessage(Component.text("Player ")
                    .append(Component.text(playerName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not on a team."))
                    .color(NamedTextColor.RED));
            return true;
        }
        gameManager.leavePlayer(sender, playerToLeave, playerName);
        return true;
    }
}

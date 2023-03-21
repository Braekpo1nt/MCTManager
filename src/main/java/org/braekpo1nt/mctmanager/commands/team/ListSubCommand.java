package org.braekpo1nt.mctmanager.commands.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListSubCommand implements CommandExecutor {
    private final GameManager gameManager;
    
    public ListSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            sender.sendMessage("Usage: /mct team list");
            return true;
        }
        displayTeams(sender);
        return true;
    }
    
    private void displayTeams(CommandSender sender) {
        TextComponent.Builder messageBuilder = Component.text().append(Component.text("TEAMS\n")
                    .decorate(TextDecoration.BOLD));
        
        for (String teamName : gameManager.getTeamNames()) {
            TextComponent.Builder teamBuilder = Component.text().append(Component.text("- " + teamName + ": "));
            List<String> playersInTeam = gameManager.getPlayerNamesOnTeam(teamName);
            if (playersInTeam.isEmpty()) {
                teamBuilder.append(Component.text("(none)\n")
                        .color(NamedTextColor.GRAY));
            } else {
                teamBuilder.append(Component.text(String.join(", ", playersInTeam) + "\n"));
            }
            messageBuilder.append(teamBuilder.build());
        }
        Component message = messageBuilder.build();
        sender.sendMessage(message);
    }
}

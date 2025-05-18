package org.braekpo1nt.mctmanager.commands.teammsg;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class TeamMsgCommand implements TabExecutor {
    
    private final GameManager gameManager;
    
    public TeamMsgCommand(Main plugin, GameManager gameManager) {
        PluginCommand command = plugin.getCommand("t");
        this.gameManager = gameManager;
        if (command == null) {
            Main.logger().severe("Unable to find the /t command in plugin.yml");
            return;
        }
        command.setExecutor(this);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
            return true;
        }
        Participant participant = gameManager.getOnlineParticipant(player.getUniqueId());
        if (participant == null) {
            sender.sendMessage(Component.text("Only participants can use this command", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /t <message>", NamedTextColor.RED));
            return true;
        }
        String message = String.join(" ", args);
        Team team = gameManager.getTeam(participant);
        if (team instanceof Audience audience) {
            audience.sendMessage(
                    Component.empty()
                            .append(Component.text("<"))
                            .append(team.getFormattedDisplayName())
                            .append(Component.text(" "))
                            .append(player.displayName())
                            .append(Component.text("> "))
                            .append(Component.text(message)
                                    .decorate(TextDecoration.ITALIC)
                                    .color(team.getColor()))
            );
        }
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}

package org.braekpo1nt.mctmanager.commands.mctdebug;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements TabExecutor, Listener {
    
    
    public MCTDebugCommand(Main plugin) {
        Objects.requireNonNull(plugin.getCommand("mctdebug")).setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @Data
    @AllArgsConstructor
    public static class Participant {
        private @NotNull Component name;
        private boolean alive;
        public Component toTabListEntry(TextColor color) {
            if (alive) {
                return name.color(color);
            }
            return name.color(NamedTextColor.DARK_GRAY);
        }
    }
    
    @Data
    @AllArgsConstructor
    public static class Team {
        private @NotNull TextColor color;
        private @NotNull Component name;
        private @NotNull List<Participant> participants;
        private int score;
        
        public Component toTabListLine(int index) {
            TextComponent.Builder builder = Component.text();
            builder
                    .append(Component.text(index))
                    .append(Component.text(". "))
                    .append(name.color(color))
                    .append(Component.text("                          "))
                    .append(Component.text(score))
                    .append(Component.newline())
                    .append(Component.text("   "))
            ;
            
            for (Participant participant : participants) {
                builder.append(participant.toTabListEntry(color));
                builder.append(Component.text("  "));
            }
            return builder.asComponent();
        }
    }
    
    private static final List<Team> allTeams;
    static {
        allTeams = List.of(
                new Team(
                        NamedTextColor.YELLOW,
                        Component.text("Yellow Yaks"),
                        List.of(
                                new Participant(Component.text("Purpled"),true),
                                new Participant(Component.text("Antfrost"),true),
                                new Participant(Component.text("vGumiho"),false),
                                new Participant(Component.text("RedVelvetCake"),true)
                        ),
                        2112
                ),
                new Team(
                        NamedTextColor.DARK_PURPLE,
                        Component.text("Purple Pandas"),
                        List.of(
                                new Participant(Component.text("SolidarityGaming"),false),
                                new Participant(Component.text("InTheLittleWood"),false),
                                new Participant(Component.text("FireBreathMan"),false),
                                new Participant(Component.text("Smajor1"),false)
                        ),
                        1299
                ),
                new Team(
                        NamedTextColor.BLUE,
                        Component.text("Blue Bats"),
                        List.of(
                                new Participant(Component.text("ShubbleYT"),false),
                                new Participant(Component.text("Krtzy"),false),
                                new Participant(Component.text("falsesymmetry"),false),
                                new Participant(Component.text("fruitberries"),false)
                        ),
                        1177
                )
        );
    }
    
    public static Component toTabList(List<Team> teams) {
        TextComponent.Builder builder = Component.text();
        builder.append(Component.newline());
        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            builder
                    .append(team.toTabListLine(i))
                    .append(Component.newline())
                    .append(Component.newline())
            ;
        }
        return builder.asComponent();
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
        
        TextComponent.Builder builder = Component.text();
        builder
                .append(toTabList(allTeams))
        ;
        player.sendPlayerListHeader(
                builder.asComponent()
        );
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}

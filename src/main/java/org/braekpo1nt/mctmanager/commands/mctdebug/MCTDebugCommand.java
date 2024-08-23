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

import java.util.ArrayList;
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
        private final @NotNull String name;
        private final boolean alive;
        
        public Component toTabListEntry(TextColor color, int maxLength) {
            String resultName;
            if (name.length() > maxLength) {
                resultName = name.substring(0, maxLength);
            } else {
                resultName = name;
            }
            if (alive) {
                return Component.text(resultName).color(color);
            }
            return Component.text(resultName).color(NamedTextColor.DARK_GRAY);
        }
    }
    
    @Data
    @AllArgsConstructor
    public static class Team {
        private final @NotNull TextColor color;
        private final @NotNull String name;
        private final @NotNull List<Participant> participants;
        private final int score;
        
        public Component toTabListLine(int index) {
            return Component.empty()
                    .append(Component.text(index))
                    .append(Component.text(". "))
                    .append(Component.text(name)
                            .color(color))
                    .append(Component.text("                          "))
                    .append(Component.text(score))
                    .append(Component.newline())
                    .append(Component.text("   "))
                    .append(getParticipantNamesLine())
            ;
        }
        
        private Component getParticipantNamesLine() {
            List<Integer> nameLengths = participants.stream().map(participant -> participant.getName().length()).toList();
            int maxNameLength = calculateMaxNameLength(nameLengths, 45);
            TextComponent.Builder builder = Component.text();
            for (int i = 0; i < participants.size(); i++) {
                Participant participant = participants.get(i);
                builder.append(participant.toTabListEntry(color, maxNameLength));
                if (i < participants.size() - 1) {
                    builder.append(Component.space());
                }
            }
            return builder.build();
        }
        
        public static int calculateMaxNameLength(List<Integer> nameLengths, int maxLineLength) {
            // Calculate the total initial length with spaces between names
            int totalLength = nameLengths.stream().mapToInt(Integer::intValue).sum() + (nameLengths.size() - 1);
            
            if (totalLength <= maxLineLength) {
                // If total length is already within the limit, no trimming is needed
                return Collections.max(nameLengths);
            }
            
            List<Integer> lengths = new ArrayList<>(nameLengths);
            // Sort the lengths in descending order
            lengths.sort(Collections.reverseOrder());
            
            // Iteratively reduce the length of the longest names
            int index = 0;
            while (totalLength > maxLineLength && index < lengths.size()) {
                int maxLength = lengths.get(index);
                
                for (int i = 0; i < lengths.size(); i++) {
                    if (lengths.get(i) == maxLength) {
                        lengths.set(i, lengths.get(i) - 1);
                        totalLength--;
                        
                        if (totalLength <= maxLineLength) {
                            break;
                        }
                    }
                }
            }
            
            // The maximum length that any name can have after trimming
            return Collections.min(lengths);
        }
        
    }
    
    private static final List<Team> allTeams;
    static {
        allTeams = List.of(
                new Team(
                        NamedTextColor.YELLOW,
                        "Yellow Yaks",
                        List.of(
                                new Participant("Purpled",true),
                                new Participant("Antfrost",true),
                                new Participant("vGumiho",false),
                                new Participant("RedVelvetCake",true)
                        ),
                        2112
                ),
                new Team(
                        NamedTextColor.DARK_PURPLE,
                        "Purple Pandas",
                        List.of(
                                new Participant("SolidarityGaming1111111111111111",false),
                                new Participant("InTheLittleWood",false),
                                new Participant("FireBreathMan",false),
                                new Participant("Smajor1",false)
                        ),
                        1299
                ),
                new Team(
                        NamedTextColor.BLUE,
                        "Blue Bats",
                        List.of(
                                new Participant("ShubbleYT",false),
                                new Participant("Krtzy",false),
                                new Participant("falsesymmetry",false),
                                new Participant("fruitberries",false)
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

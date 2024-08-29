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
    public static class PlayerData {
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
    public static class TeamData {
        private final @NotNull TextColor color;
        private final @NotNull String name;
        private final @NotNull List<PlayerData> participants;
        private final int score;
        
        public Component toTabListLine(int index, int chars, int pChars) {
            int paddingLength = Math.max(chars - (4 + name.length() + Integer.toString(score).length()), 0);
            return Component.empty()
                    .append(Component.empty()
                        .append(Component.text(String.format("%2d", index)))
                        .append(Component.text(". "))
                        .append(Component.text(name)
                            .color(color))
                        .append(Component.text(" ".repeat(paddingLength)))
                        .append(Component.text(score))
                    )
                    
                    .append(Component.newline())
                    
                    .append(Component.empty()
                        .append(Component.text("     "))
                        .append(getParticipantNamesLine(pChars))
                    )
                    ;
        }
        
        private Component getParticipantNamesLine(int pChars) {
            List<Integer> nameLengths = participants.stream().map(participant -> participant.getName().length()).toList();
            List<Integer> trimLengths = getTrimLengths(nameLengths, pChars - (nameLengths.size() - 1));
            TextComponent.Builder builder = Component.text();
            for (int i = 0; i < participants.size(); i++) {
                PlayerData participant = participants.get(i);
                builder.append(participant.toTabListEntry(color, trimLengths.get(i)));
                if (i < participants.size() - 1) {
                    builder.append(Component.space());
                }
            }
            int totalLength = trimLengths.stream().mapToInt(Integer::intValue).sum() + nameLengths.size() - 1;
            int paddingLength = pChars - totalLength;
            builder.append(Component.text(" ".repeat(paddingLength)));
            return builder.build();
        }
        
        public static @NotNull List<Integer> getTrimLengths(@NotNull List<Integer> nameLengths, int maxLineLength) {
            List<Integer> trimmedLengths = new ArrayList<>(nameLengths);
            if (trimmedLengths.isEmpty()) {
                return trimmedLengths;
            }
            int totalLength = trimmedLengths.stream().mapToInt(Integer::intValue).sum();
            int numOfNames = trimmedLengths.size();
            while (totalLength > maxLineLength) {
                int maxIndex = 0;
                int maxValue = trimmedLengths.getFirst();
                for (int i = 1; i < numOfNames; i++) {
                    int value = trimmedLengths.get(i);
                    if (value > maxValue) {
                        maxValue = value;
                        maxIndex = i;
                    }
                }
                trimmedLengths.set(maxIndex, maxValue - 1);
                totalLength--;
            }
            return trimmedLengths;
        }
    }
    
    private static final List<TeamData> allTeams;
    static {
        allTeams = List.of(
                new TeamData(
                        NamedTextColor.YELLOW,
                        "Yellow Yaks",
                        List.of(
                                new PlayerData("Purpled",true),
                                new PlayerData("Antfrost",true),
                                new PlayerData("vGumiho",false),
                                new PlayerData("RedVelvetCake",true)
                        ),
                        2112
                ),
                new TeamData(
                        NamedTextColor.DARK_AQUA,
                        "Teal Turkeys",
                        List.of(
                                new PlayerData("SolidarityGaming",true),
                                new PlayerData("cubfan13",true),
                                new PlayerData("jojosolo",false),
                                new PlayerData("smajor199",true)
                        ),
                        1299
                ),
                new TeamData(
                        NamedTextColor.GOLD,
                        "Giner Breadmen",
                        List.of(
                                new PlayerData("GoodTimesWithScar",true),
                                new PlayerData("FireBreathMan",true),
                                new PlayerData("Owengejuice",true),
                                new PlayerData("bekyamon",true)
                        ),
                        1177
                )
        );
    }
    
    /**
     * 
     * @param chars 55
     * @param pChars 43
     */
    public static Component toTabList(List<TeamData> teams, int chars, int pChars) {
        TextComponent.Builder builder = Component.text();
        builder.append(Component.newline());
        for (int i = 0; i < teams.size(); i++) {
            TeamData team = teams.get(i);
            builder
                    .append(team.toTabListLine(i, chars, pChars))
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
        
        if (args.length != 2) {
            sender.sendMessage(Component.text("Usage: /mctdebug <arg> [options]")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        int chars = Integer.parseInt(args[0]);
        int pChars = Integer.parseInt(args[1]);
        
        TextComponent.Builder builder = Component.text();
        builder
                .append(toTabList(allTeams, chars, pChars))
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

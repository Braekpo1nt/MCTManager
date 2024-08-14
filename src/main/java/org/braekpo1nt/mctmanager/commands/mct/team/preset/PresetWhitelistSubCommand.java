package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PresetWhitelistSubCommand extends TabSubCommand {
    
    private final PresetStorageUtil storageUtil;
    private final GameManager gameManager;
    
    public PresetWhitelistSubCommand(GameManager gameManager, PresetStorageUtil storageUtil, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
        this.storageUtil = storageUtil;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return CommandResult.failure(getUsage().of("<true|false>"));
        }
        String shouldWhitelistString = args[0];
        Boolean shouldWhitelist = CommandUtils.toBoolean(shouldWhitelistString);
        if (shouldWhitelist == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(shouldWhitelistString)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a valid boolean"))
            );
        }
        
        Preset preset;
        try {
            storageUtil.loadPreset();
            preset = storageUtil.getPreset();
        } catch (ConfigException e) {
            Main.logger().severe(String.format("Could not load preset. %s", e.getMessage()));
            e.printStackTrace();
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Error occurred loading preset. See console for details: "))
                    .append(Component.text(e.getMessage())));
        }
        
        int changedCount = 0;
        int unchangedCount = 0;
        TextComponent.Builder builder = Component.text();
        if (shouldWhitelist) {
            
            for (Preset.PresetTeam team : preset.getTeams()) {
                for (String ign : team.getMembers()) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ign);
                    if (!offlinePlayer.isWhitelisted()) {
                        offlinePlayer.setWhitelisted(true);
                        changedCount++;
                    } else {
                        unchangedCount++;
                    }
                }
            }
            
            builder
                    .append(Component.text("Whitelisted ")
                            .append(Component.text(changedCount))
                            .append(Component.text(" participant(s).")));
            if (unchangedCount > 0) {
                builder
                        .append(Component.text(" "))
                        .append(Component.text(unchangedCount))
                        .append(Component.text(" participant(s) were already whitelisted."));
            }
        } else {
            
            for (Preset.PresetTeam team : preset.getTeams()) {
                for (String ign : team.getMembers()) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ign);
                    if (offlinePlayer.isWhitelisted()) {
                        offlinePlayer.setWhitelisted(false);
                        changedCount++;
                    } else {
                        unchangedCount++;
                    }
                }
            }
            
            builder
                    .append(Component.text("Un-whitelisted ")
                            .append(Component.text(changedCount))
                            .append(Component.text(" participant(s).")));
            if (unchangedCount > 0) {
                builder
                        .append(Component.text(" "))
                        .append(Component.text(unchangedCount))
                        .append(Component.text(" participant(s) were already un-whitelisted."));
            }
        }
        return CommandResult.success(builder.asComponent());
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of("true", "false");
    }
}

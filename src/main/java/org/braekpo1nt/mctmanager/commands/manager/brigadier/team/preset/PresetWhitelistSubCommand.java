package org.braekpo1nt.mctmanager.commands.manager.brigadier.team.preset;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Level;

public class PresetWhitelistSubCommand implements BrigadierSubCommand {
    
    private final @NotNull PresetStorageUtil storageUtil;
    
    public PresetWhitelistSubCommand(@NotNull PresetStorageUtil storageUtil) {
        this.storageUtil = storageUtil;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("whitelist")
                .then(Commands.argument("whitelist", BoolArgumentType.bool())
                        .executes(BrigadierAdapters.wraps(this::executeWhitelist))
                )
                ;
    }
    
    private @NotNull CommandResult executeWhitelist(CommandContext<CommandSourceStack> ctx) {
        File presetFile = ctx.getArgument(PresetCommand.PRESET_FILE_ARG, File.class);
        boolean shouldWhitelist = ctx.getArgument("whitelist", Boolean.class);
        Preset preset;
        try {
            preset = storageUtil.loadPreset(presetFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Could not load preset. %s", e.getMessage()), e);
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
}

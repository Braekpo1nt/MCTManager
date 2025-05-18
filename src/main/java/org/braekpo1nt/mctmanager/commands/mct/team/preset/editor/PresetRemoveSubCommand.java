package org.braekpo1nt.mctmanager.commands.mct.team.preset.editor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * lets you remove a team from the preset
 */
public class PresetRemoveSubCommand extends SubCommand {
    
    
    private final PresetStorageUtil storageUtil;
    
    public PresetRemoveSubCommand(PresetStorageUtil storageUtil, @NotNull String name) {
        super(name);
        this.storageUtil = storageUtil;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            return CommandResult.failure(getUsage().of("<team>"));
        }
        String presetFile = args[0];
        String removeTeamId = args[1];
        return removeTeam(presetFile, removeTeamId);
    }
    
    private @NotNull CommandResult removeTeam(@NotNull String presetFile, @NotNull String teamId) {
        return storageUtil.modifyPreset(presetFile, preset -> {
            if (!preset.hasTeamId(teamId)) {
                return CommandResult.failure(Component.text("Team ")
                        .append(Component.text(teamId)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" does not exist in the preset")));
            }
            
            preset.removeTeam(teamId);
            return CommandResult.success(Component.empty()
                    .append(Component.text("Removed "))
                    .append(Component.text(teamId)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" from preset"))
            );
        });
    }
    
}

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
 * lets you leave players from their team by ign
 */
public class PresetLeaveSubCommand extends SubCommand {
    
    
    private final PresetStorageUtil storageUtil;
    
    public PresetLeaveSubCommand(PresetStorageUtil storageUtil, @NotNull String name) {
        super(name);
        this.storageUtil = storageUtil;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            return CommandResult.failure(getUsage().of("<member>"));
        }
        String presetFile = args[0];
        String ign = args[1];
        return leavePlayer(presetFile, ign);
    }
    
    private @NotNull CommandResult leavePlayer(@NotNull String presetFile, @NotNull String ign) {
        if (ign.isEmpty()) {
            return CommandResult.failure("Player name can't be blank");
        }
        
        return storageUtil.modifyPreset(presetFile, preset -> {
            if (!preset.hasMember(ign)) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(ign)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not in the preset"))
                );
            }
            preset.leaveMember(ign);
            return CommandResult.success(Component.empty()
                    .append(Component.text("Left "))
                    .append(Component.text(ign)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" from the preset"))
            );
        });
    }
    
}

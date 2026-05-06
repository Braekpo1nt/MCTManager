package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.argumenttypes.FileResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetController;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetDTO;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PresetAddMissingUUIDsCommand implements BrigadierSubCommand {
    
    private final Main plugin;
    private final File presetDirectory;
    
    public PresetAddMissingUUIDsCommand(Main plugin) {
        this.plugin = plugin;
        this.presetDirectory = new File(plugin.getDataFolder(), "presets");
    }
    
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("addMissingUUIDs")
                .executes(BrigadierAdapters.wraps(this::executePresetResolveMissingUUIDs))
                ;
    }
    
    private @NotNull CommandResult executePresetResolveMissingUUIDs(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        FileResolver fileResolver = ctx.getArgument(PresetCommand.PRESET_FILE_ARG, FileResolver.class);
        File presetFile = fileResolver.resolve();
        PresetController presetController = new PresetController(presetDirectory);
        PresetDTO dto = presetController.getPresetDTOWithoutValidation(presetFile);
        if (dto == null) {
            return CommandResult.failure("Could not parse. dto is null.");
        }
        List<String> unFindableIGNs = new ArrayList<>();
        List<CommandResult> results = new ArrayList<>();
        boolean changesWereMade = false;
        if (dto.getTeams() == null) {
            return CommandResult.failure("teams list is null");
        }
        for (PresetDTO.PresetTeamDTO teamDTO : dto.getTeams()) {
            for (PresetDTO.PresetParticipantDTO memberDTO : teamDTO.getMembers()) {
                if (memberDTO.getUuid() == null) {
                    String ign = memberDTO.getIgn();
                    if (ign == null) {
                        return CommandResult.failure("the ign of a player can't be null for this operation");
                    }
                    UUID uuid = plugin.getServer().getPlayerUniqueId(ign);
                    if (uuid == null) {
                        // add to list and fail out later for a full list of IGNs that couldn't be resolved
                        unFindableIGNs.add(ign);
                    } else {
                        changesWereMade = true;
                        memberDTO.setUuid(uuid);
                        results.add(CommandResult.success(Component.empty()
                                .append(Component.text("Resolved "))
                                .append(CommandUtils.copiable(ign))
                                .append(Component.text(" to "))
                                .append(CommandUtils.copiable(uuid.toString()))
                        ));
                    }
                }
            }
        }
        if (!unFindableIGNs.isEmpty()) {
            TextComponent.Builder builder = Component.text();
            builder.append(Component.text("Could not find UUIDs for the following IGNs: "));
            for (String unFindableIGN : unFindableIGNs) {
                builder
                        .append(CommandUtils.copiable(unFindableIGN))
                        .append(Component.text(", "))
                ;
            }
            return CommandResult.failure(builder.build());
        }
        if (changesWereMade) {
            presetController.savePreset(dto, presetFile);
            results.add(CommandResult.success(Component.text("Saved UUIDs to preset")));
            return new CompositeCommandResult(results);
        } else {
            return CommandResult.success(Component.text("No changes needed to be made to this preset."));
        }
        
    }
}

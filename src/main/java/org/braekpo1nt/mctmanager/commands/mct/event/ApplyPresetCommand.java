package org.braekpo1nt.mctmanager.commands.mct.event;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.teams.EventTeam;
import org.braekpo1nt.mctmanager.database.service.EventService;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ApplyPresetCommand implements BrigadierSubCommand {
    
    private final DynamicCommandExceptionType ERROR_TEAM_ID_EXISTS = new DynamicCommandExceptionType(teamId -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("A team with ID "))
            .append(Component.text(teamId.toString())
                    .decorate(TextDecoration.BOLD))
            .append(Component.text(" already exists for this event."))
    ));
    
    private final @NotNull EventService eventService;
    
    public ApplyPresetCommand(@NotNull EventService eventService) {
        this.eventService = eventService;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("applyPreset");
    }
    
    private @NotNull CommandResult applyPreset(@NotNull EventInfo eventInfo, @NotNull Preset preset) throws SQLException, CommandSyntaxException {
        addTeams(eventInfo, preset);
        return CommandResult.success();
    }
    
    private void addTeams(EventInfo eventInfo, Preset preset) throws SQLException, CommandSyntaxException {
        Map<String, EventTeam> oldTeams = eventService.getTeams(eventInfo.getEventId()).stream()
                .collect(Collectors.toMap(EventTeam::getTeamId, Function.identity()));
        Map<String, EventTeam> newTeams = new HashMap<>(preset.getTeamCount());
        for (Preset.PresetTeam team : preset.getTeams()) {
            if (oldTeams.containsKey(team.getTeamId())) {
                throw ERROR_TEAM_ID_EXISTS.create(team.getTeamId());
            }
            EventTeam newTeam = EventTeam.builder()
                    .eventId(eventInfo.getEventId())
                    .teamId(team.getTeamId())
                    .displayName(team.getDisplayName())
                    .color(team.getColor())
                    .build();
            newTeams.put(newTeam.getTeamId(), newTeam);
        }
    }
}

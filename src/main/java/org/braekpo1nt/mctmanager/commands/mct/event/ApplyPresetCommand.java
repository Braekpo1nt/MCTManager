package org.braekpo1nt.mctmanager.commands.mct.event;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoResolver;
import org.braekpo1nt.mctmanager.commands.argumenttypes.FileArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.FileResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.participants.EventParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.teams.EventTeam;
import org.braekpo1nt.mctmanager.database.service.EventService;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ApplyPresetCommand implements BrigadierSubCommand {
    
    public static final String PRESET_FILE_ARG = "presetFile";
    
    private final DynamicCommandExceptionType ERROR_TEAM_ID_EXISTS = new DynamicCommandExceptionType(teamId -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("A team with ID "))
            .append(Component.text(teamId.toString())
                    .decorate(TextDecoration.BOLD))
            .append(Component.text(" already exists for this event."))
    ));
    
    private final DynamicCommandExceptionType ERROR_PARTICIPANT_EXISTS = new DynamicCommandExceptionType(ign -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("A participant "))
            .append(Component.text(ign.toString())
                    .decorate(TextDecoration.BOLD))
            .append(Component.text(" already exists for this event."))
    ));
    
    private final @NotNull PresetStorageUtil storageUtil;
    private final @NotNull EventService eventService;
    private final @NotNull Main plugin;
    
    public ApplyPresetCommand(@NotNull PresetStorageUtil storageUtil, @NotNull EventService eventService, @NotNull Main plugin) {
        this.storageUtil = storageUtil;
        this.eventService = eventService;
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("applyPreset")
                .then(Permissioned.argument("eventId", new EventInfoArgumentType(eventService))
                        .then(Permissioned.argument(PRESET_FILE_ARG,
                                        new FileArgumentType(new File(plugin.getDataFolder(), "presets"), ".json"))
                                .executes(BrigadierAdapters.wraps(this::executeApplyPreset))
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeApplyPreset(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        EventInfoResolver eventInfoResolver = ctx.getArgument("eventId", EventInfoResolver.class);
        FileResolver fileResolver = ctx.getArgument(PRESET_FILE_ARG, FileResolver.class);
        try {
            EventInfo eventInfo = eventInfoResolver.resolve();
            File presetFile = fileResolver.resolve();
            Preset preset = storageUtil.loadPreset(presetFile);
            return applyPreset(eventInfo, preset);
        } catch (SQLException e) {
            return EventSubCommand.handleSQLException("applying preset", e);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Could not load preset. %s", e.getMessage()), e);
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Error occurred loading preset. See console for details: "))
                    .append(Component.text(e.getMessage())));
        }
    }
    
    private @NotNull CommandResult applyPreset(@NotNull EventInfo eventInfo, @NotNull Preset preset) {
        return CommandResult.async(plugin, Component.text("Applying preset"), () -> {
            List<EventTeam> teams = getTeams(eventInfo, preset);
            List<EventParticipantEntity> participants = getParticipants(eventInfo, preset);
            try {
                eventService.replaceEventTeamsAndParticipants(teams, participants, eventInfo.getEventId());
            } catch (SQLException e) {
                return EventSubCommand.handleSQLException("apply preset to event", e);
            }
            return CommandResult.success(Component.text("Preset applied."));
        });
    }
    
    private List<EventTeam> getTeams(EventInfo eventInfo, Preset preset) {
        List<EventTeam> newTeams = new ArrayList<>(preset.getTeamCount());
        for (Preset.PresetTeam team : preset.getTeams()) {
            EventTeam newTeam = EventTeam.builder()
                    .eventId(eventInfo.getEventId())
                    .teamId(team.getTeamId())
                    .displayName(team.getDisplayName())
                    .color(team.getColor())
                    .build();
            newTeams.add(newTeam);
        }
        return newTeams;
    }
    
    private List<EventParticipantEntity> getParticipants(EventInfo eventInfo, Preset preset) {
        List<EventParticipantEntity> newParticipants = new ArrayList<>();
        for (Preset.PresetTeam team : preset.getTeams()) {
            for (String ign : team.getMembers()) {
                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(ign);
                EventParticipantEntity newParticipant = EventParticipantEntity.builder()
                        .eventId(eventInfo.getEventId())
                        .participantUUID(offlinePlayer.getUniqueId().toString())
                        .teamId(team.getTeamId())
                        .build();
                newParticipants.add(newParticipant);
            }
        }
        return newParticipants;
    }
    
    /**
     * Ensures there are no duplicates
     */
    private Map<String, EventTeam> getTeamsNoDuplicates(EventInfo eventInfo, Preset preset) throws SQLException, CommandSyntaxException {
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
        return newTeams;
    }
    
    /**
     * Ensures there are no duplicates
     */
    private List<EventParticipantEntity> getParticipantsNoDuplicates(EventInfo eventInfo, Preset preset) throws SQLException, CommandSyntaxException {
        Map<String, EventParticipantEntity> oldParticipants = eventService.getParticipants(eventInfo.getEventId()).stream()
                .collect(Collectors.toMap(EventParticipantEntity::getParticipantUUID, Function.identity()));
        List<EventParticipantEntity> newParticipants = new ArrayList<>();
        for (Preset.PresetTeam team : preset.getTeams()) {
            for (String ign : team.getMembers()) {
                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(ign);
                if (oldParticipants.containsKey(offlinePlayer.getUniqueId().toString())) {
                    throw ERROR_PARTICIPANT_EXISTS.create(ign);
                }
                EventParticipantEntity newParticipant = EventParticipantEntity.builder()
                        .eventId(eventInfo.getEventId())
                        .participantUUID(offlinePlayer.getUniqueId().toString())
                        .teamId(team.getTeamId())
                        .build();
                newParticipants.add(newParticipant);
            }
        }
        return newParticipants;
    }
}

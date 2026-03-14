package org.braekpo1nt.mctmanager.commands.mct.event;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import org.braekpo1nt.mctmanager.database.entities.AllPlayersEntity;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.participants.EventParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.teams.EventTeam;
import org.braekpo1nt.mctmanager.database.service.EventService;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
    
    private final DynamicCommandExceptionType ERROR_PLAYER_DOES_NOT_EXIST = new DynamicCommandExceptionType(ign -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("A player with the name "))
            .append(Component.text(ign.toString())
                    .decorate(TextDecoration.BOLD))
            .append(Component.text(" could not be found in the all_players database. Have they logged in yet? Resolve players in the preset with the \"/mct team preset <preset.json> resolve\" command"))
    ));
    
    private final DynamicCommandExceptionType ERROR_MULTIPLE_PLAYERS_WITH_NAME = new DynamicCommandExceptionType(ign -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("A multiple players with the name "))
            .append(Component.text(ign.toString())
                    .decorate(TextDecoration.BOLD))
            .append(Component.text(" exist in the all_players database. Can't use IGN for preset"))
    ));
    
    private final @NotNull PresetStorageUtil storageUtil;
    private final @NotNull EventService eventService;
    private final @NotNull GameStateService gameStateService;
    private final @NotNull Main plugin;
    
    public ApplyPresetCommand(@NotNull EventService eventService, @NotNull Main plugin, @NotNull GameStateService gameStateService) {
        this.storageUtil = new PresetStorageUtil(plugin.getDataFolder());
        this.eventService = eventService;
        this.plugin = plugin;
        this.gameStateService = gameStateService;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("applyPreset")
                .then(Permissioned.argument("eventId", new EventInfoArgumentType(eventService))
                        .then(Permissioned.argument(PRESET_FILE_ARG,
                                        new FileArgumentType(new File(plugin.getDataFolder(), "presets"), ".json"))
                                .executes(BrigadierAdapters.wraps(ctx -> CommandResult.success(Component.empty()
                                        .append(Component.text("Are you sure you want to overwrite the players and teams associated with this event? This operation can't be undone. If you're sure, add \"confirm\" to the end of this command"))
                                        .color(NamedTextColor.YELLOW)
                                )))
                                .then(Permissioned.literal("confirm")
                                        .executes(BrigadierAdapters.wraps(this::executeApplyPreset))
                                )
                        )
                )
                ;
    }
    
    /**
     * Resolves the command arguments and applies the preset to the eventId
     * @param ctx the context
     * @return the result
     * @throws CommandSyntaxException if there's a syntax error
     */
    private @NotNull CommandResult executeApplyPreset(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        EventInfoResolver eventInfoResolver = ctx.getArgument("eventId", EventInfoResolver.class);
        FileResolver fileResolver = ctx.getArgument(PRESET_FILE_ARG, FileResolver.class);
        try {
            EventInfo eventInfo = eventInfoResolver.resolve();
            File presetFile = fileResolver.resolve();
            Preset preset = storageUtil.loadPreset(presetFile);
            return applyPreset(eventInfo, preset);
        } catch (SQLException e) {
            return CommandResult.sqlException("applying preset", e);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Could not load preset. %s", e.getMessage()), e);
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Error occurred loading preset. See console for details: "))
                    .append(Component.text(e.getMessage())));
        }
    }
    
    /**
     * Applies the given preset to the given event. This is a replacement operation, it removes all participants and
     * teams that were previously associated with the event and adds all those from the given preset
     * @param eventInfo the event to apply the preset to
     * @param preset the preset to apply
     * @return a result detailing the operation's success
     */
    private @NotNull CommandResult applyPreset(@NotNull EventInfo eventInfo, @NotNull Preset preset) {
        return CommandResult.async(plugin,
                Component.empty()
                        .append(Component.text("Applying preset "))
                        .append(Component.text(preset.getFileName())
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" to "))
                        .append(Component.text(eventInfo.getEventId())
                                .decorate(TextDecoration.BOLD))
                ,
                () -> {
                    List<EventTeam> teams = getTeams(eventInfo, preset);
                    try {
                        List<EventParticipantEntity> participants = getParticipants(eventInfo, preset);
                        eventService.replaceEventTeamsAndParticipants(teams, participants, eventInfo.getEventId());
                    } catch (SQLException e) {
                        return CommandResult.sqlException("apply preset to event", e);
                    }
                    return CommandResult.success(Component.text("Preset applied."));
                });
    }
    
    /**
     * @param eventInfo the event with the eventId to give to the created {@link EventTeam}s
     * @param preset the preset to get the teams from
     * @return the preset teams as {@link EventTeam} objects
     */
    private List<EventTeam> getTeams(EventInfo eventInfo, Preset preset) {
        Date now = new Date();
        List<EventTeam> newTeams = new ArrayList<>(preset.getTeamCount());
        for (Preset.PresetTeam team : preset.getTeams()) {
            EventTeam newTeam = EventTeam.builder()
                    .eventId(eventInfo.getEventId())
                    .teamId(team.getTeamId())
                    .displayName(team.getDisplayName())
                    .color(team.getColor())
                    .modifiedAt(now)
                    .build();
            newTeams.add(newTeam);
        }
        return newTeams;
    }
    
    /**
     * @param eventInfo the event with the eventId to give to the created {@link EventParticipantEntity}s
     * @param preset the preset to get the participants from
     * @return the preset participants as {@link EventParticipantEntity} objects
     */
    private List<EventParticipantEntity> getParticipants(EventInfo eventInfo, Preset preset) throws CommandSyntaxException, SQLException {
        List<EventParticipantEntity> newParticipants = new ArrayList<>();
        for (Preset.PresetTeam team : preset.getTeams()) {
            for (String ign : team.getMembers()) {
                AllPlayersEntity player;
                try {
                    player = gameStateService.getPlayer(ign);
                } catch (GameStateService.MultiplePlayersWithNameException e) {
                    throw ERROR_MULTIPLE_PLAYERS_WITH_NAME.create(ign);
                }
                if (player == null) {
                    throw ERROR_PLAYER_DOES_NOT_EXIST.create(ign);
                }
                String playerUUID = player.getUuid();
                EventParticipantEntity newParticipant = EventParticipantEntity.builder()
                        .eventId(eventInfo.getEventId())
                        .participantUUID(playerUUID)
                        .teamId(team.getTeamId())
                        .substitute(false)
                        .build();
                newParticipants.add(newParticipant);
            }
        }
        return newParticipants;
    }
    
    /**
     * @param eventInfo the event with the eventId to give to the created {@link EventParticipantEntity}s
     * @param preset the preset to get the participants from
     * @return the preset participants as {@link EventParticipantEntity} objects
     */
    private List<EventParticipantEntity> getParticipantsBukkit(EventInfo eventInfo, Preset preset) {
        List<EventParticipantEntity> newParticipants = new ArrayList<>();
        for (Preset.PresetTeam team : preset.getTeams()) {
            for (String ign : team.getMembers()) {
                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(ign);
                EventParticipantEntity newParticipant = EventParticipantEntity.builder()
                        .eventId(eventInfo.getEventId())
                        .participantUUID(offlinePlayer.getUniqueId().toString())
                        .teamId(team.getTeamId())
                        .substitute(false)
                        .build();
                newParticipants.add(newParticipant);
            }
        }
        return newParticipants;
    }
    
    /**
     * Ensures there are no duplicates already present in the event
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
     * Ensures there are no duplicates already present in the event
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
                        .substitute(false)
                        .build();
                newParticipants.add(newParticipant);
            }
        }
        return newParticipants;
    }
}

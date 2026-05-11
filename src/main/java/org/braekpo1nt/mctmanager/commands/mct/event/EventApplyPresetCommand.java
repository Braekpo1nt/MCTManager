package org.braekpo1nt.mctmanager.commands.mct.event;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
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
import org.braekpo1nt.mctmanager.commands.mct.team.preset.PresetCommand;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.database.entities.EventInfo;
import org.braekpo1nt.mctmanager.database.entities.participants.EventParticipantEntity;
import org.braekpo1nt.mctmanager.database.entities.teams.EventTeam;
import org.braekpo1nt.mctmanager.database.service.EventService;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * @deprecated This results in UUID mismatches between the in-memory state and the database.
 * Is not used right now.
 */
@Deprecated
public class EventApplyPresetCommand implements BrigadierSubCommand {
    
    private final Dynamic2CommandExceptionType ERROR_ONLINE_UUID_MISMATCH = new Dynamic2CommandExceptionType((ign, uuid) -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("A player with the name "))
            .append(Component.text(ign.toString())
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.copyToClipboard(ign.toString())))
            .append(Component.text(" is online and does not have the IGN "))
            .append(Component.text(uuid.toString())
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.copyToClipboard(uuid.toString())))
            .append(Component.text(", which means the preset UUID is wrong."))
    ));
    
    private final Dynamic2CommandExceptionType ERROR_ONLINE_IGN_MISMATCH = new Dynamic2CommandExceptionType((ign, uuid) -> MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("A player with the UUID "))
            .append(Component.text(uuid.toString())
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.copyToClipboard(uuid.toString())))
            .append(Component.text(" is online and does not have the IGN "))
            .append(Component.text(ign.toString())
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.copyToClipboard(ign.toString())))
            .append(Component.text(", which means the preset IGN is wrong."))
    ));
    
    
    private final @NotNull PresetStorageUtil storageUtil;
    private final @NotNull EventService eventService;
    private final @NotNull GameStateService gameStateService;
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    
    public EventApplyPresetCommand(@NotNull GameManager gameManager, @NotNull Main plugin) {
        this.gameManager = gameManager;
        this.storageUtil = new PresetStorageUtil(plugin.getDataFolder());
        this.eventService = gameManager.getEventService();
        this.plugin = plugin;
        this.gameStateService = gameManager.getGameStateService();
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("applyPreset")
                .then(Permissioned.argument("eventId", new EventInfoArgumentType(eventService))
                        .then(Permissioned.argument(PresetCommand.PRESET_FILE_ARG,
                                        new FileArgumentType(new File(plugin.getDataFolder(), "presets"), ".json"))
                                .executes(BrigadierAdapters.wraps(ctx -> CommandResult.success(Component.empty()
                                        .append(Component.text("Are you sure you want to overwrite the players and teams associated with this event? This operation can't be undone. If you're sure, add \"confirm\" to the end of this command"))
                                        .color(NamedTextColor.YELLOW)
                                )))
                                .then(Permissioned.literal("confirm")
                                        .executes(BrigadierAdapters.wrapsFuture(this::executeApplyPreset))
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
    private @NotNull CompletableFuture<CommandResult> executeApplyPreset(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        EventInfoResolver eventInfoResolver = ctx.getArgument("eventId", EventInfoResolver.class);
        FileResolver fileResolver = ctx.getArgument(PresetCommand.PRESET_FILE_ARG, FileResolver.class);
        try {
            EventInfo eventInfo = eventInfoResolver.resolve();
            File presetFile = fileResolver.resolve();
            Preset preset = storageUtil.loadPreset(presetFile);
            return applyPreset(eventInfo, preset);
        } catch (SQLException e) {
            return CommandResult.sqlException("applying preset", e).asFuture();
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Could not load preset. %s", e.getMessage()), e);
            return CommandResult.failure(Component.empty()
                            .append(Component.text("Error occurred loading preset. See console for details: "))
                            .append(Component.text(e.getMessage())))
                    .asFuture();
        }
    }
    
    /**
     * Applies the given preset to the given event. This is a replacement operation, it removes all participants and
     * teams that were previously associated with the event and adds all those from the given preset
     * @param eventInfo the event to apply the preset to
     * @param preset the preset to apply
     * @return a result detailing the operation's success
     */
    private @NotNull CompletableFuture<CommandResult> applyPreset(@NotNull EventInfo eventInfo, @NotNull Preset preset) throws CommandSyntaxException {
        if (gameManager.getMode().equals(Mode.EVENT)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Can't apply a preset to an event while in event mode. Switch to maintenance mode or practice mode, or use the "))
                    .append(Component.text("/mct team preset apply...")
                            .decorate(TextDecoration.UNDERLINED))
                    .append(Component.text(" command to apply the preset to the current context."))
            ).asFuture();
        }
        // check preset against all online players (as source of truth)
        for (Preset.PresetTeam team : preset.getTeams()) {
            for (Preset.PresetParticipant participant : team.getMembers()) {
                String ign = participant.getIgn();
                UUID uuid = participant.getUuid();
                verifyOnlineIGN(uuid, ign);
                verifyOnlineUUID(uuid, ign);
            }
        }
        return CompletableFuture.supplyAsync(() -> {
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
    private List<EventParticipantEntity> getParticipants(EventInfo eventInfo, Preset preset) throws SQLException {
        List<EventParticipantEntity> newParticipants = new ArrayList<>();
        for (Preset.PresetTeam team : preset.getTeams()) {
            for (Preset.PresetParticipant participant : team.getMembers()) {
                String ign = participant.getIgn();
                UUID uuid = participant.getUuid();
                gameStateService.registerPlayer(uuid.toString(), ign);
                EventParticipantEntity newParticipant = EventParticipantEntity.builder()
                        .eventId(eventInfo.getEventId())
                        .participantUUID(uuid.toString())
                        .teamId(team.getTeamId())
                        .substitute(false)
                        .build();
                newParticipants.add(newParticipant);
            }
        }
        return newParticipants;
    }
    
    private void verifyOnlineIGN(UUID uuid, String ign) throws CommandSyntaxException {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            if (!player.getName().equals(ign)) {
                throw ERROR_ONLINE_IGN_MISMATCH.create(ign, uuid);
            }
        }
    }
    
    private void verifyOnlineUUID(UUID uuid, String ign) throws CommandSyntaxException {
        Player player = plugin.getServer().getPlayer(ign);
        if (player != null) {
            if (!player.getUniqueId().equals(uuid)) {
                throw ERROR_ONLINE_UUID_MISMATCH.create(ign, uuid);
            }
        }
    }
}

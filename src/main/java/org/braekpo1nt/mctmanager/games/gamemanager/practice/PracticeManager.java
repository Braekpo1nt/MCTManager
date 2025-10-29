package org.braekpo1nt.mctmanager.games.gamemanager.practice;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.MasonryPane;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.hub.config.HubConfig;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PracticeManager {
    
    private final Component NETHER_STAR_NAME = Component.text("Practice");
    
    private final GameManager gameManager;
    private final Map<UUID, PracticeParticipant> participants;
    private final Map<String, Team> teams;
    /**
     * a map of the currently active invites (in progress, not yet cancelled or
     * executed). The keys are the games the invites are related to, since
     * only one invite per game can be active at a time.
     */
    private final Map<GameInstanceId, Invite> activeInvites;
    @Setter
    private @NotNull HubConfig.PracticeConfig config;
    
    public <P extends Participant, T extends Team> PracticeManager(@NotNull GameManager gameManager, @NotNull HubConfig.PracticeConfig config, Collection<T> newTeams, Collection<P> newParticipants) {
        this.gameManager = gameManager;
        this.config = config;
        this.activeInvites = new HashMap<>();
        this.teams = new HashMap<>(newTeams.size());
        for (Team team : newTeams) {
            teams.put(team.getTeamId(), team);
        }
        participants = new HashMap<>(newParticipants.size());
        for (Participant newParticipant : newParticipants) {
            PracticeParticipant participant = new PracticeParticipant(newParticipant);
            participants.put(participant.getUniqueId(), participant);
            giveNetherStar(participant);
        }
    }
    
    public void cleanup() {
        for (PracticeParticipant participant : participants.values()) {
            participant.closeLastGui();
            removeNetherStar(participant);
        }
        participants.clear();
        teams.clear();
        activeInvites.clear();
    }
    
    private void giveNetherStar(PracticeParticipant participant) {
        removeNetherStar(participant);
        ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
        netherStar.editMeta(meta -> meta.displayName(NETHER_STAR_NAME));
        participant.getInventory().addItem(netherStar);
    }
    
    private void removeNetherStar(PracticeParticipant participant) {
        participant.getInventory().remove(Material.NETHER_STAR);
    }
    
    private @NotNull ChestGui createMainMenu(PracticeParticipant participant) {
        ChestGui gui = new ChestGui(3, "Main");
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        OutlinePane main = new OutlinePane(0, 0, 9, 3);
        GameInstanceId id = gameManager.getTeamActiveGame(participant.getTeamId());
        if (id == null) {
            // Game Select
            ItemStack gameSelect = new ItemStack(Material.AMETHYST_SHARD);
            gameSelect.editMeta(meta -> meta.displayName(Component.text("Game Select")));
            main.addItem(new GuiItem(gameSelect, event -> {
                participant.showGui(createGameMenu(participant));
            }));
        } else {
            // Join Game
            Team team = teams.get(participant.getTeamId());
            ItemStack joinGame = new ItemStack(team.getColorAttributes().getPowder());
            joinGame.editMeta(meta -> meta.displayName(Component.empty()
                    .append(Component.text("Join "))
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(" in "))
                    .append(Component.text(id.getTitle()))));
            main.addItem(new GuiItem(joinGame, event -> {
                CommandResult commandResult = gameManager.joinParticipantToGame(id.getGameType(), id.getConfigFile(), participant.getUniqueId());
                participant.sendMessage(commandResult.getMessageOrEmpty());
            }));
        }
        // Team Select
        ItemStack teamSelect = new ItemStack(Material.WHITE_WOOL);
        teamSelect.editMeta(meta -> meta.displayName(Component.text("Team Select")));
        main.addItem(new GuiItem(teamSelect, event -> {
            participant.showGui(createTeamMenu(participant));
        }));
        gui.addPane(main);
        return gui;
    }
    
    /**
     * A game can be played if it's not already being played, and it's in ths list
     * of allowed games in the config.
     * @param id the game type to check
     * @return true if the given game type can be played at this time
     */
    private boolean canPlayGame(@NotNull GameInstanceId id) {
        return config.getAllowedGameIds().contains(id) && !gameManager.gameIsActive(id);
    }
    
    /**
     * An invite can be created for a game when that game is not active and no
     * invites are already created for it, and the config allows that game type.
     * @param id the game type to check
     * @return true if the given game type can be played (see {@link #canPlayGame(GameInstanceId)}
     * and doesn't already have an active invite
     */
    private boolean canInviteGame(@NotNull GameInstanceId id) {
        return canPlayGame(id) && !activeInvites.containsKey(id);
    }
    
    private @NotNull ChestGui createGameMenu(PracticeParticipant participant) {
        ChestGui gui = new ChestGui(3, "Select Game");
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        MasonryPane gameMenu = new MasonryPane(0, 0, 9, 3);
        
        PaginatedPane gameSelectPages = new PaginatedPane(0, 0, 9, 2);
        List<GuiItem> gameGuiItems = new ArrayList<>(config.getAllowedGames().size());
        for (HubConfig.GameInfo gameInfo : config.getAllowedGames()) {
            if (canInviteGame(gameInfo.getId())) {
                ItemStack itemStack = new ItemStack(gameInfo.getItemType());
                itemStack.editMeta(meta -> {
                    meta.displayName(gameInfo.getItemName());
                    meta.lore(gameInfo.getItemLore());
                });
                gameGuiItems.add(new GuiItem(itemStack,
                        event -> initiateInvite(participant, gameInfo.getId())));
            }
        }
        gameSelectPages.populateWithGuiItems(gameGuiItems);
        gameMenu.addPane(gameSelectPages);
        
        OutlinePane background = new OutlinePane(0, 2, 9, 1);
        background.addItem(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
        background.setRepeat(true);
        background.setPriority(Pane.Priority.LOWEST);
        gui.addPane(background);
        
        StaticPane navigation = new StaticPane(0, 2, 9, 1);
        ItemStack back = createItem(Material.BARRIER, Component.text("Back"));
        navigation.addItem(new GuiItem(back, event -> {
            participant.showGui(createMainMenu(participant));
        }), 4, 0);
        ItemStack previous = createItem(Material.RED_DYE, Component.text("Previous"));
        navigation.addItem(new GuiItem(previous, event -> {
            if (gameSelectPages.getPage() > 0) {
                gameSelectPages.setPage(gameSelectPages.getPage() - 1);
                gui.update();
            }
        }), 0, 0);
        ItemStack next = createItem(Material.GREEN_DYE, Component.text("Next"));
        navigation.addItem(new GuiItem(next, event -> {
            if (gameSelectPages.getPage() < gameSelectPages.getPages() - 1) {
                gameSelectPages.setPage(gameSelectPages.getPage() + 1);
                gui.update();
            }
        }), 8, 0);
        
        gameMenu.addPane(navigation);
        gui.addPane(gameMenu);
        return gui;
    }
    
    private static ItemStack createItem(Material material, Component displayName) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.editMeta(meta -> meta.displayName(displayName));
        return itemStack;
    }
    
    private @NotNull ChestGui createInviteTeamMenu(PracticeParticipant participant, @NotNull Invite invite) {
        ChestGui gui = new ChestGui(3, "Invite Teams");
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        MasonryPane inviteTeamMenu = new MasonryPane(0, 0, 9, 3);
        OutlinePane teamSelect = new OutlinePane(0, 0, 9, 2);
        for (Team team : teams.values()) {
            if (!invite.isInitiator(team.getTeamId()) && canInviteTeam(team.getTeamId())) {
                GuiItem inviteTeamItem = createInviteTeamItem(gui, team, invite);
                teamSelect.addItem(inviteTeamItem);
            }
        }
        inviteTeamMenu.addPane(teamSelect);
        
        OutlinePane navigation = new OutlinePane(0, 2, 9, 1);
        ItemStack back = new ItemStack(Material.BARRIER);
        back.editMeta(meta -> meta.displayName(Component.text("Cancel")));
        navigation.addItem(new GuiItem(back, event -> {
            participant.showGui(createGameMenu(participant));
        }));
        ItemStack send = new ItemStack(Material.LIME_DYE);
        send.editMeta(meta -> meta.displayName(Component.text("Send")));
        navigation.addItem(new GuiItem(send, event -> {
            sendInvite(participant, invite);
        }));
        inviteTeamMenu.addPane(navigation);
        gui.addPane(inviteTeamMenu);
        return gui;
    }
    
    /**
     * @param teamId the team to check
     * @return true if the given team can be invited to a game, false if
     * the team is involved in an active invite, has no online players, or has
     * at least one player in a game
     */
    private boolean canInviteTeam(String teamId) {
        for (Invite invite : activeInvites.values()) {
            if (invite.isInvolved(teamId)) {
                return false;
            }
        }
        if (getParticipantsOnTeam(teamId).isEmpty()) {
            return false;
        }
        return !gameManager.teamIsInGame(teamId);
    }
    
    /**
     * Creates an Invite object and shows the player a gui for inviting teams
     * @param initiator the participant who is creating the invite
     * @param id the game instance id create an Invite for
     */
    private void initiateInvite(PracticeParticipant initiator, @NotNull GameInstanceId id) {
        if (!canInviteGame(id)) {
            initiator.sendMessage(Component.empty()
                    .append(Component.text("Can't start a "))
                    .append(Component.text(id.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" game at this time")));
            return;
        }
        Team team = teams.get(initiator.getTeamId());
        if (!canInviteTeam(initiator.getTeamId())) {
            initiator.sendMessage(Component.empty()
                    .append(Component.text("Can't start an invite because"))
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(" is busy")));
        }
        Invite invite = new Invite(id, initiator.getTeamId());
        initiator.showGui(createInviteTeamMenu(initiator, invite));
    }
    
    private void cancelInvite(@NotNull Invite invite) {
        activeInvites.remove(invite.getId());
        Component cancelMessage = Component.empty()
                .append(Component.text(invite.getId().getTitle()))
                .append(Component.text(" invite was cancelled"));
        for (PracticeParticipant p : participants.values()) {
            if (invite.isInvolved(p.getTeamId())) {
                p.setInvite(null);
                p.closeLastGui();
                p.sendMessage(cancelMessage);
            }
        }
    }
    
    private void sendInvite(PracticeParticipant sender, @NotNull Invite invite) {
        // re-evaluate the invite-ability of the teams (including initiator)
        if (!canInviteGame(invite.getId())) {
            sender.sendMessage(Component.empty()
                    .append(Component.text("Could not create invite because "))
                    .append(Component.text(invite.getId().getTitle()))
                    .append(Component.text(" already has an invite in-progress")));
            return;
        }
        if (!canInviteTeam(invite.getInitiator())) {
            Team team = teams.get(invite.getInitiator());
            sender.sendMessage(Component.empty()
                    .append(Component.text("Could not create invite because "))
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(" is not available")));
            return;
        }
        for (Team team : teams.values()) {
            if (invite.isGuest(team.getTeamId())) {
                if (!canInviteTeam(team.getTeamId())) {
                    sender.sendMessage(Component.empty()
                            .append(Component.text("Can't invite "))
                            .append(team.getFormattedDisplayName()));
                    invite.removeGuest(team.getTeamId());
                }
            }
        }
        
        activeInvites.put(invite.getId(), invite);
        Team initiator = teams.get(invite.getInitiator());
        for (PracticeParticipant participant : participants.values()) {
            Team team = teams.get(participant.getTeamId());
            if (invite.isGuest(participant.getTeamId())) {
                participant.setInvite(invite);
                participant.closeLastGui();
                participant.sendMessage(Component.empty()
                        .append(initiator.getFormattedDisplayName())
                        .append(Component.text(" invited "))
                        .append(team.getFormattedDisplayName())
                        .append(Component.text(" to play "))
                        .append(Component.text(invite.getId().getTitle()))
                        .append(Component.text(". Use the nether star to accept or decline.")));
            }
            if (invite.isInitiator(participant.getTeamId())) {
                participant.setInvite(invite);
                participant.sendMessage(Component.empty()
                        .append(initiator.getFormattedDisplayName())
                        .append(Component.text(" sent an invite to play "))
                        .append(Component.text(invite.getId().getTitle())));
                if (!participant.getUniqueId().equals(sender.getUniqueId())) {
                    participant.closeLastGui();
                }
            }
        }
        sender.showGui(createInviteStatusMenu(sender, invite));
    }
    
    private @NotNull GuiItem createInviteTeamItem(ChestGui gui, Team team, Invite invite) {
        ItemStack teamWool = new ItemStack(team.getColorAttributes().getWool());
        teamWool.editMeta(meta -> meta.displayName(Component.empty()
                .append(Component.text("Invite "))
                .append(team.getFormattedDisplayName())));
        ItemStack teamGlass = new ItemStack(team.getColorAttributes().getStainedGlassPane());
        teamGlass.editMeta(meta -> meta.displayName(Component.empty()
                .append(Component.text("Un-invite "))
                .append(team.getFormattedDisplayName())));
        GuiItem inviteTeamItem = new GuiItem(teamWool);
        inviteTeamItem.setAction(event -> {
            if (invite.isGuest(team.getTeamId())) {
                invite.removeGuest(team.getTeamId());
                inviteTeamItem.setItem(teamWool);
            } else {
                invite.addGuest(team.getTeamId());
                inviteTeamItem.setItem(teamGlass);
            }
            gui.update();
        });
        return inviteTeamItem;
    }
    
    private @NotNull ChestGui createInviteRSVPMenu(PracticeParticipant participant, @NotNull Invite invite) {
        Team team = teams.get(participant.getTeamId());
        ChestGui gui = new ChestGui(3, ComponentHolder.of(Component.empty()
                .append(Component.text("Invite to play "))
                .append(Component.text(invite.getId().getTitle()))));
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        OutlinePane navigator = new OutlinePane(0, 0, 9, 1);
        ItemStack decline = new ItemStack(Material.BARRIER);
        decline.editMeta(meta -> meta.displayName(Component.empty()
                .append(Component.text("Decline"))
                .color(NamedTextColor.RED)));
        ItemStack accept = new ItemStack(Material.LIME_DYE);
        accept.editMeta(meta -> meta.displayName(Component.empty()
                .append(Component.text("Accept"))
                .color(NamedTextColor.GREEN)));
        
        navigator.addItem(new GuiItem(decline, event -> {
            declineInvite(invite, team);
            gui.getInventory().close();
        }));
        navigator.addItem(new GuiItem(accept, event -> {
            acceptInvite(invite, team);
            gui.getInventory().close();
        }));
        gui.addPane(navigator);
        return gui;
    }
    
    private void acceptInvite(@NotNull Invite invite, Team team) {
        if (!activeInvites.containsKey(invite.getId())) {
            return;
        }
        Team initiator = teams.get(invite.getInitiator());
        GameInstanceId gameType = invite.getId();
        invite.rsvp(team.getTeamId(), true);
        updateInviteStatusInventory(invite);
        for (PracticeParticipant p : participants.values()) {
            if (p.getTeamId().equals(team.getTeamId())) {
                p.sendMessage(Component.empty()
                        .append(team.getFormattedDisplayName())
                        .append(Component.text(" accepted "))
                        .append(initiator.getFormattedDisplayName())
                        .append(Component.text("'s invite to play "))
                        .append(Component.text(gameType.getTitle()))
                        .color(NamedTextColor.GREEN));
            }
            if (invite.isInitiator(p.getTeamId())) {
                p.sendMessage(Component.empty()
                        .append(team.getFormattedDisplayName())
                        .append(Component.text(" accepted your invite to play "))
                        .append(Component.text(gameType.getTitle()))
                        .color(NamedTextColor.GREEN));
            }
        }
    }
    
    /**
     * Updates the "game Invite Status" inventory of all initiators
     * @param invite the invite to update for
     */
    private void updateInviteStatusInventory(@NotNull Invite invite) {
        for (PracticeParticipant participant : getParticipantsOnTeam(invite.getInitiator())) {
            InventoryView openInventory = participant.getOpenInventory();
            if (openInventory.title().equals(invite.getStatusMenuTitle())) {
                participant.showGui(createInviteStatusMenu(participant, invite));
            }
        }
    }
    
    /**
     * @param invite the invite which is being declined
     * @param decliningTeam the team which is declining the invite
     */
    private void declineInvite(@NotNull Invite invite, Team decliningTeam) {
        if (!activeInvites.containsKey(invite.getId())) {
            return;
        }
        Team initiator = teams.get(invite.getInitiator());
        GameInstanceId gameType = invite.getId();
        invite.rsvp(decliningTeam.getTeamId(), false);
        invite.removeGuest(decliningTeam.getTeamId());
        updateInviteStatusInventory(invite);
        Component message = Component.empty()
                .append(decliningTeam.getFormattedDisplayName())
                .append(Component.text(" declined "))
                .append(initiator.getFormattedDisplayName())
                .append(Component.text("'s invite to play "))
                .append(Component.text(gameType.getTitle()))
                .color(NamedTextColor.RED);
        Audience.audience(getParticipantsOnTeam(invite.getInitiator()))
                .sendMessage(message);
        for (PracticeParticipant participant : getParticipantsOnTeam(decliningTeam.getTeamId())) {
            participant.setInvite(null);
            participant.sendMessage(message);
        }
    }
    
    private @NotNull ChestGui createInviteStatusMenu(PracticeParticipant participant, @NotNull Invite invite) {
        ChestGui gui = new ChestGui(3, ComponentHolder.of(invite.getStatusMenuTitle()));
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        MasonryPane inviteStatus = new MasonryPane(0, 0, 9, 3);
        OutlinePane rsvps = new OutlinePane(0, 0, 9, 2);
        for (Team team : teams.values()) {
            if (invite.isGuest(team.getTeamId())) {
                GuiItem teamStatusItem = createTeamStatusItem(team, invite);
                rsvps.addItem(teamStatusItem);
            }
        }
        inviteStatus.addPane(rsvps);
        
        OutlinePane navigation = new OutlinePane(0, 2, 9, 1);
        ItemStack back = new ItemStack(Material.BARRIER);
        back.editMeta(meta -> meta.displayName(Component.text("Cancel")));
        navigation.addItem(new GuiItem(back, event -> {
            cancelInvite(invite);
            gui.getInventory().close();
        }));
        ItemStack play = new ItemStack(Material.LIME_DYE);
        play.editMeta(meta -> meta.displayName(Component.empty()
                .append(Component.text("Play "))
                .append(Component.text(invite.getId().getTitle()))));
        navigation.addItem(new GuiItem(play, event -> {
            participant.setInvite(null);
            activeInvites.remove(invite.getId());
            for (PracticeParticipant p : participants.values()) {
                if (invite.isGuest(p.getTeamId())) {
                    p.setInvite(null);
                }
            }
            executeInvite(invite);
            gui.getInventory().close();
        }));
        inviteStatus.addPane(navigation);
        gui.addPane(inviteStatus);
        return gui;
    }
    
    private GuiItem createTeamStatusItem(@NotNull Team team, @NotNull Invite invite) {
        if (invite.isAttending(team.getTeamId())) {
            ItemStack teamWool = new ItemStack(team.getColorAttributes().getWool());
            teamWool.editMeta(meta -> meta.displayName(Component.empty()
                    .append(Component.empty()
                            .append(Component.text("Accepted: "))
                            .color(NamedTextColor.GREEN))
                    .append(team.getFormattedDisplayName())));
            return new GuiItem(teamWool);
        } else {
            ItemStack teamGlass = new ItemStack(team.getColorAttributes().getStainedGlassPane());
            teamGlass.editMeta(meta -> meta.displayName(Component.empty()
                    .append(Component.empty()
                            .append(Component.text("No response: "))
                            .color(NamedTextColor.GRAY))
                    .append(team.getFormattedDisplayName())));
            return new GuiItem(teamGlass);
        }
    }
    
    private Collection<PracticeParticipant> getParticipantsOnTeam(@NotNull String teamId) {
        return participants.values().stream().filter(participant -> participant.getTeamId().equals(teamId)).toList();
    }
    
    private void executeInvite(@NotNull Invite invite) {
        Audience initiatorAudience = Audience.audience(getParticipantsOnTeam(invite.getInitiator()));
        if (!canPlayGame(invite.getId())) {
            initiatorAudience.sendMessage(Component.empty()
                    .append(Component.text("Can't start a "))
                    .append(Component.text(invite.getId().getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" game at this time")));
            return;
        }
        for (PracticeParticipant participant : participants.values()) {
            if (invite.hasNotResponded(participant.getTeamId())) {
                participant.sendMessage(Component.empty()
                        .append(Component.text(invite.getId().getTitle()))
                        .append(Component.text(" was started, invite cancelled")));
            }
        }
        Set<String> teamIds = invite.getConfirmedGuestIds();
        CommandResult commandResult = gameManager.startGame(
                teamIds, Collections.emptyList(),
                invite.getId().getGameType(), invite.getId().getConfigFile());
        initiatorAudience.sendMessage(commandResult.getMessageOrEmpty());
    }
    
    private @NotNull ChestGui createTeamMenu(PracticeParticipant participant) {
        ChestGui gui = new ChestGui(3, "Select Team");
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        MasonryPane teamMenu = new MasonryPane(0, 0, 9, 3);
        OutlinePane teamSelect = new OutlinePane(0, 0, 9, 2);
        for (Team team : teams.values()) {
            if (!team.getTeamId().equals(participant.getTeamId())) {
                ItemStack teamWool = new ItemStack(team.getColorAttributes().getWool());
                teamWool.editMeta(meta -> meta.displayName(team.getFormattedDisplayName()));
                teamSelect.addItem(new GuiItem(teamWool, event -> {
                    Component message = joinTeam(event, team.getTeamId()).getMessage();
                    if (message != null) {
                        event.getWhoClicked().sendMessage(message);
                    }
                }));
            }
        }
        teamMenu.addPane(teamSelect);
        
        OutlinePane navigation = new OutlinePane(0, 2, 9, 1);
        ItemStack back = new ItemStack(Material.BARRIER);
        back.editMeta(meta -> meta.displayName(Component.text("Back")));
        navigation.addItem(new GuiItem(back, event -> {
            participant.showGui(createMainMenu(participant));
        }));
        teamMenu.addPane(navigation);
        gui.addPane(teamMenu);
        return gui;
    }
    
    private CommandResult joinTeam(InventoryClickEvent event, String teamId) {
        PracticeParticipant participant = participants.get(event.getWhoClicked().getUniqueId());
        if (participant == null) {
            return CommandResult.failure("You are not a participant");
        }
        return gameManager.joinParticipantToTeam(participant.getPlayer(), participant.getName(), teamId);
    }
    
    public void onParticipantInteract(PlayerInteractEvent event) {
        PracticeParticipant participant = participants.get(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        ItemStack netherStar = event.getItem();
        if (netherStar == null ||
                !netherStar.getType().equals(Material.NETHER_STAR)) {
            return;
        }
        ItemMeta netherStarMeta = netherStar.getItemMeta();
        if (netherStarMeta == null || !netherStarMeta.hasDisplayName() || !Objects.equals(netherStarMeta.displayName(), NETHER_STAR_NAME)) {
            return;
        }
        event.setUseItemInHand(Event.Result.DENY);
        openMenu(participant);
    }
    
    public CommandResult openMenu(@NotNull UUID uuid) {
        PracticeParticipant participant = participants.get(uuid);
        if (participant == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Can't open the hub menu at this time")));
        }
        openMenu(participant);
        return CommandResult.success();
    }
    
    private void openMenu(PracticeParticipant participant) {
        Invite invite = participant.getInvite();
        if (invite == null) {
            participant.showGui(createMainMenu(participant));
            return;
        }
        if (invite.isInitiator(participant.getTeamId())) {
            participant.showGui(createInviteStatusMenu(participant, invite));
            return;
        }
        participant.showGui(createInviteRSVPMenu(participant, invite));
    }
    
    public void addParticipant(Participant newParticipant) {
        PracticeParticipant participant = new PracticeParticipant(newParticipant);
        participants.put(participant.getUniqueId(), participant);
        for (Invite invite : activeInvites.values()) {
            if (invite.isInvolved(participant.getTeamId())) {
                participant.setInvite(invite);
            }
        }
        giveNetherStar(participant);
    }
    
    public void removeParticipant(UUID uuid) {
        PracticeParticipant participant = participants.remove(uuid);
        if (participant == null) {
            return;
        }
        for (Invite invite : activeInvites.values()) {
            String teamId = participant.getTeamId();
            if (invite.isInvolved(teamId)) {
                if (getParticipantsOnTeam(teamId).isEmpty()
                        || gameManager.teamIsInGame(teamId)) {
                    if (invite.isGuest(teamId)) {
                        Team team = teams.get(teamId);
                        declineInvite(invite, team);
                    } else { // they are the initiator
                        cancelInvite(invite);
                    }
                }
            }
        }
        participant.setInvite(null);
        participant.closeLastGui();
        removeNetherStar(participant);
    }
    
    public void addTeam(Team team) {
        teams.put(team.getTeamId(), team);
    }
    
    public void removeTeam(String teamId) {
        teams.remove(teamId);
        for (Invite invite : activeInvites.values()) {
            if (invite.isInitiator(teamId)) {
                cancelInvite(invite);
            }
            if (invite.isGuest(teamId)) {
                Team team = teams.get(teamId);
                declineInvite(invite, team);
            }
        }
    }
}

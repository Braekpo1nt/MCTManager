package org.braekpo1nt.mctmanager.games.gamemanager.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfig;
import org.braekpo1nt.mctmanager.games.gamemanager.event.config.EventConfigController;
import org.braekpo1nt.mctmanager.games.gamemanager.MCTParticipant;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.ReadyUpState;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class PracticeState extends GameManagerState {
    
    private final Component NETHER_STAR_NAME = Component.text("Practice");
    
    public PracticeState(@NotNull GameManager context, @NotNull ContextReference contextReference) {
        super(context, contextReference);
        setupSidebar();
        for (MCTParticipant participant : onlineParticipants.values()) {
            if (!isParticipantInGame(participant)) {
                giveNetherStar(participant);
            }
        }
    }
    
    protected void giveNetherStar(MCTParticipant participant) {
        ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
        netherStar.editMeta(meta -> meta.displayName(NETHER_STAR_NAME));
        participant.getInventory().addItem(netherStar);
    }
    
    protected void removeNetherStar(MCTParticipant participant) {
        participant.getInventory().remove(Material.NETHER_STAR);
    }
    
    /**
     * Set up the sidebar for this state. Called once in the constructor.
     */
    protected void setupSidebar() {
        sidebar.deleteAllLines();
        this.sidebar.updateTitle(Component.empty()
                .append(Sidebar.DEFAULT_TITLE)
                .append(Component.text(" - "))
                .append(Component.text("Practice")));
        sidebar.addLines(
                new KeyLine("team0", Component.empty()),
                new KeyLine("team1", Component.empty()),
                new KeyLine("team2", Component.empty()),
                new KeyLine("team3", Component.empty()),
                new KeyLine("team4", Component.empty()),
                new KeyLine("team5", Component.empty()),
                new KeyLine("team6", Component.empty()),
                new KeyLine("team7", Component.empty()),
                new KeyLine("team8", Component.empty()),
                new KeyLine("team9", Component.empty()),
                new KeyLine("personalScore", Component.empty())
        );
        updateSidebarTeamScores();
        updateSidebarPersonalScores(onlineParticipants.values());
    }
    
    @Override
    public CommandResult switchMode(@NotNull String mode) {
        switch (mode) {
            case "maintenance" -> {
                for (MCTParticipant participant : onlineParticipants.values()) {
                    removeNetherStar(participant);
                }
                context.setState(new MaintenanceState(context, contextReference));
                return CommandResult.success(Component.text("Switched to maintenance mode"));
            }
            case "practice" -> {
                return CommandResult.success(Component.text("Already in practice mode"));
            }
            case "event" -> {
                for (MCTParticipant participant : onlineParticipants.values()) {
                    removeNetherStar(participant);
                }
                return startEvent(7, 0);
            }
            default -> {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(mode)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a valid mode")));
            }
        }
    }
    
    @Override
    public CommandResult startEvent(int maxGames, int currentGameNumber) {
        try {
            EventConfig eventConfig = new EventConfigController(plugin.getDataFolder()).getConfig();
            context.setState(new ReadyUpState(context, contextReference, eventConfig, maxGames, currentGameNumber));
            return CommandResult.success(Component.text("Switched to event mode"));
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, e.getMessage(), e);
            return CommandResult.failure(Component.text("Can't switch to event mode. Error loading config file. See console for details:\n")
                    .append(Component.text(e.getMessage())));
        }
    }
    
    // leave/join start
    
    @Override
    public void onParticipantJoin(@NotNull MCTParticipant participant) {
        super.onParticipantJoin(participant);
        participant.teleport(config.getSpawn());
        giveNetherStar(participant);
    }
    
    @Override
    public void onParticipantQuit(@NotNull MCTParticipant participant) {
        removeNetherStar(participant);
        super.onParticipantQuit(participant);
    }
    
    // leave/join stop
    
    
    @Override
    protected void onParticipantReturnToHub(@NotNull MCTParticipant participant, @NotNull Location spawn) {
        super.onParticipantReturnToHub(participant, spawn);
        giveNetherStar(participant);
    }
    
    @Override
    public void onParticipantDropItem(@NotNull PlayerDropItemEvent event, MCTParticipant participant) {
        if (isParticipantInGame(participant)) {
            return;
        }
        event.setCancelled(true);
    }
    
}

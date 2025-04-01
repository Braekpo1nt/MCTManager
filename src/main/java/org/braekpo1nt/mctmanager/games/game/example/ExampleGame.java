package org.braekpo1nt.mctmanager.games.game.example;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.experimental.PreventHungerLoss;
import org.braekpo1nt.mctmanager.games.experimental.GameBase;
import org.braekpo1nt.mctmanager.games.experimental.PreventItemDrop;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.example.config.ExampleConfig;
import org.braekpo1nt.mctmanager.games.game.example.states.DescriptionSate;
import org.braekpo1nt.mctmanager.games.game.example.states.ExampleState;
import org.braekpo1nt.mctmanager.games.game.example.states.InitialState;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.topbar.BasicTopbar;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ExampleGame extends GameBase<ExampleParticipant, ExampleTeam, ExampleParticipant.QuitData, ExampleTeam.QuitData, ExampleState> {
    
    private final ExampleConfig config;
    private final BasicTopbar topbar;
    
    /**
     * Initialize data and start the game
     * 
     * @param plugin          the plugin
     * @param gameManager     the GameManager
     * @param title           the game's initial title, displayed in the sidebar
     * @param newTeams        the teams participating in the game
     * @param newParticipants the participants of the game
     * @param newAdmins       the admins
     */
    public ExampleGame(@NotNull Main plugin, @NotNull GameManager gameManager, @NotNull Component title, @NotNull ExampleConfig config, @NotNull Collection<Team> newTeams, @NotNull Collection<Participant> newParticipants, @NotNull List<Player> newAdmins) {
        super(GameType.EXAMPLE, plugin, gameManager, title, new InitialState());
        this.config = config;
        this.topbar = addUIManager(new BasicTopbar());
        registerListener(new PreventHungerLoss<>(this));
        registerListener(new PreventItemDrop<>(this, true));
        start(newTeams, newParticipants, newAdmins);
    }
    
    @Override
    protected @NotNull ExampleState getStartState() {
        return new DescriptionSate(this);
    }
    
    @Override
    protected void cancelAllTasks() {
        // cancel BukkitTasks here
    }
    
    @Override
    protected void cleanup() {
        // custom stop code
    }
    
    @Override
    protected ExampleParticipant createParticipant(Participant participant, ExampleParticipant.QuitData quitData) {
        // use the quitData to create a new participant, including setup that happens regardless of state
        return new ExampleParticipant(participant, quitData.getScore());
    }
    
    @Override
    protected ExampleParticipant createParticipant(Participant fromParticipant) {
        // create a new participant, including setup that happens regardless of state
        return new ExampleParticipant(fromParticipant, 0);
    }
    
    @Override
    public ExampleParticipant.QuitData getQuitData(ExampleParticipant participant) {
        return participant.getQuitData();
    }
    
    @Override
    protected void initializeParticipant(ExampleParticipant participant, ExampleTeam team) {
        participant.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        participant.getInventory().addItem(new ItemStack(Material.STICK));
        participant.teleport(config.getStartingLocation());
    }
    
    @Override
    protected void initializeTeam(ExampleTeam team) {
        // custom team initialization
    }
    
    @Override
    public ExampleTeam createTeam(Team team, ExampleTeam.QuitData quitData) {
        return new ExampleTeam(team, quitData.getScore());
    }
    
    @Override
    public ExampleTeam createTeam(Team team) {
        return new ExampleTeam(team, 0);
    }
    
    @Override
    public ExampleTeam.QuitData getQuitData(ExampleTeam team) {
        return team.getQuitData();
    }
    
    @Override
    protected void resetParticipant(ExampleParticipant participant, ExampleTeam team) {
        // custom participant resetting
    }
    
    @Override
    protected void initializeAdmin(Player admin) {
        // custom admin initialization
    }
    
    @Override
    protected void initializeAdminSidebar() {
        adminSidebar.addLine("timer", Component.empty());
    }
    
    @Override
    protected void resetAdmin(Player admin) {
        // custom admin resetting
    }
    
    @Override
    protected @Nullable SpectatorBoundary getSpectatorBoundary() {
        return config.getSpectatorBoundary();
    }
    
    @Override
    protected boolean shouldPreventInteractions(@NotNull Material type) {
        return config.getPreventInteractions().contains(type);
    }
    
    @Override
    protected void initializeSidebar() {
        sidebar.addLine("timer", Component.empty());
    }
    
    @Override
    public ExampleParticipant getParticipant(UUID uuid) {
        return participants.get(uuid);
    }
}

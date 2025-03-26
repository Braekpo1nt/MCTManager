package org.braekpo1nt.mctmanager.games.game.example;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.experimental.GameBase;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.example.config.ExampleConfig;
import org.braekpo1nt.mctmanager.games.game.example.states.DescriptionSate;
import org.braekpo1nt.mctmanager.games.game.example.states.ExampleState;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.UIManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ExampleGame extends GameBase<ExampleParticipant, ExampleTeam, ExampleParticipant.QuitData, ExampleTeam.QuitData> {
    
    private final ExampleConfig config;
    
    /**
     * Initialize data and start the game
     *
     * @param type            the type associated with this game
     * @param plugin          the plugin
     * @param gameManager     the GameManager
     * @param title           the game's initial title, displayed in the sidebar
     * @param newTeams        the teams participating in the game
     * @param newParticipants the participants of the game
     * @param newAdmins       the admins
     */
    public ExampleGame(@NotNull GameType type, @NotNull Main plugin, @NotNull GameManager gameManager, @NotNull Component title, @NotNull ExampleConfig config, @NotNull Collection<Team> newTeams, @NotNull Collection<Participant> newParticipants, @NotNull List<Player> newAdmins) {
        super(type, plugin, gameManager, title);
        this.config = config;
        init(newTeams, newParticipants, newAdmins);
    }
    
    @Override
    protected List<UIManager> createUIManagers() {
        return Collections.emptyList();
    }
    
    @Override
    protected ExampleState getInitialState() {
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
        return new ExampleParticipant(participant, quitData.getScore());
    }
    
    @Override
    protected ExampleParticipant createParticipant(Participant participant) {
        return new ExampleParticipant(participant, 0);
    }
    
    @Override
    public ExampleParticipant.QuitData getQuitData(ExampleParticipant participant) {
        return participant.getQuitData();
    }
    
    @Override
    protected void initializeParticipant(ExampleParticipant participant, ExampleTeam team) {
        participant.teleport(config.getStartingLocation());
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
        
    }
    
    @Override
    protected void initializeAdmin(Player admin) {
        
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
    protected void initializeSidebar() {
        sidebar.addLine("timer", Component.empty());
    }
    
}

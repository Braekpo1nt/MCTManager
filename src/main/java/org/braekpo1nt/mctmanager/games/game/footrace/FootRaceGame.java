package org.braekpo1nt.mctmanager.games.game.footrace;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.experimental.GameBase;
import org.braekpo1nt.mctmanager.games.experimental.PreventHungerLoss;
import org.braekpo1nt.mctmanager.games.experimental.PreventItemDrop;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfig;
import org.braekpo1nt.mctmanager.games.game.footrace.states.DescriptionState;
import org.braekpo1nt.mctmanager.games.game.footrace.states.FootRaceState;
import org.braekpo1nt.mctmanager.games.game.footrace.states.InitialState;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class FootRaceGame extends GameBase<FootRaceParticipant, FootRaceTeam, FootRaceParticipant.QuitData, FootRaceTeam.QuitData, FootRaceState> {
    
    public static final long COOL_DOWN_TIME = 3000L;
    private final PotionEffect SPEED = new PotionEffect(PotionEffectType.SPEED, 10000, 8, true, false, false);
    private final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false, false);
    private final FootRaceConfig config;
    /**
     * what place every participant is in at any given moment in the race
     */
    private List<FootRaceParticipant> standings;
    /**
     * Keeps track of how many participants have finished the race, so we can know 
     * what place a player should be in when they finish
     * TODO: Participant would {@link #standings} be sufficient for this?
     */
    private int numOfFinishedParticipants = 0;
    private long raceStartTime;
    private int timerRefreshTaskId;
    private int statusEffectsTaskId;
    private int standingsDisplayTaskId;
    
    public FootRaceGame(
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull FootRaceConfig config,
            @NotNull Collection<Team> newTeams,
            @NotNull Collection<Participant> newParticipants,
            @NotNull List<Player> newAdmins) {
        super(GameType.FOOT_RACE, plugin, gameManager, title, new InitialState());
        this.title = title;
        this.config = config;
        standings = new ArrayList<>(newParticipants.size());
        startStatusEffectsTask();
        closeGlassBarrier();
        addListener(new PreventItemDrop<>(this, true));
        addListener(new PreventHungerLoss<>(this));
        start(newTeams, newParticipants, newAdmins);
        updateStandings();
        displayStandings();
        Main.logger().info("Starting Foot Race game");
    }
    
    @Override
    protected @NotNull FootRaceState getStartState() {
        return new DescriptionState(this);
    }
    
    @Override
    protected @NotNull World getWorld() {
        return config.getWorld();
    }
    
    public void updateStandings() {
        standings = standings.stream()
                .sorted((participant1, participant2) -> {
                    if (participant1.isFinished() || participant2.isFinished()) {
                        if (participant1.isFinished() && participant2.isFinished()) {
                            return participant1.getPlacement() - participant2.getPlacement();
                        } else {
                            if (participant1.isFinished()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    }
                    
                    if (participant1.getLap() != participant2.getLap()) {
                        return participant2.getLap() - participant1.getLap(); // Reverse order
                    }
                    
                    int nextCheckpoint1 = MathUtils.wrapIndex(participant1.getCurrentCheckpoint() + 1, config.getCheckpoints().size());
                    int nextCheckpoint2 = MathUtils.wrapIndex(participant2.getCurrentCheckpoint() + 1, config.getCheckpoints().size());
                    if (nextCheckpoint1 != nextCheckpoint2) {
                        return nextCheckpoint2 - nextCheckpoint1; // Reverse order
                    }
                    
                    BoundingBox checkpoint = config.getCheckpoints().get(nextCheckpoint1);
                    double distance1 = MathUtils.getMinimumDistance(checkpoint, participant1.getLocation().toVector());
                    double distance2 = MathUtils.getMinimumDistance(checkpoint, participant2.getLocation().toVector());
                    if (distance1 != distance2) {
                        return Double.compare(distance1, distance2);
                    }
                    
                    return participant1.getName().compareTo(participant2.getName());
                }).collect(Collectors.toCollection(ArrayList::new));
    }
    
    public void displayStandings() {
        for (int i = 0; i < standings.size(); i++) {
            FootRaceParticipant participant = standings.get(i);
            List<KeyLine> standingLines = createStandingLines(i);
            sidebar.updateLines(participant.getUniqueId(), standingLines);
        }
        adminSidebar.updateLines(createStandingLines(0));
    }
    
    private List<KeyLine> createStandingLines(int standing) {
        // there are 5 or fewer participants, or the standing is top 4
        if (standings.size() <= 5 || (0 <= standing && standing <= 3)) {
            return List.of(
                    new KeyLine("standing1", standingLine(0)),
                    new KeyLine("standing2", standingLine(1)),
                    new KeyLine("standing3", standingLine(2)),
                    new KeyLine("standing4", standingLine(3)),
                    new KeyLine("standing5", standingLine(4))
            );
        }
        // last place
        if (standing == standings.size() - 1) {
            return List.of(
                    new KeyLine("standing1", standingLine(0)),
                    new KeyLine("standing2", Component.text("...").color(NamedTextColor.GRAY)),
                    new KeyLine("standing3", standingLine(standing - 2)),
                    new KeyLine("standing4", standingLine(standing - 1)),
                    new KeyLine("standing5", standingLine(standing))
            );
        }
        // 5th place or lower (but not last)
        return List.of(
                new KeyLine("standing1", standingLine(0)),
                new KeyLine("standing2", Component.text("...").color(NamedTextColor.GRAY)),
                new KeyLine("standing3", standingLine(standing - 1)),
                new KeyLine("standing4", standingLine(standing)),
                new KeyLine("standing5", standingLine(standing + 1))
        );
    }
    
    private Component standingLine(int standing) {
        if (standing < 0 || standings.size() <= standing) {
            return Component.empty();
        }
        return Component.empty()
                .append(Component.text(standing + 1))
                .append(Component.text(". "))
                .append(standings.get(standing).displayName())
                ;
    }
    
    public void closeGlassBarrier() {
        BlockPlacementUtils.createCubeReplace(config.getWorld(), config.getGlassBarrier(), Material.AIR, Material.WHITE_STAINED_GLASS_PANE);
        BlockPlacementUtils.updateDirection(config.getWorld(), config.getGlassBarrier());
    }
    
    public void openGlassBarrier() {
        BlockPlacementUtils.createCubeReplace(config.getWorld(), config.getGlassBarrier(), Material.WHITE_STAINED_GLASS_PANE, Material.AIR);
    }
    
    private void startStatusEffectsTask() {
        this.statusEffectsTaskId = new BukkitRunnable(){
            @Override
            public void run() {
                for (Participant participant : participants.values()) {
                    participant.addPotionEffect(SPEED);
                    participant.addPotionEffect(INVISIBILITY);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L).getTaskId();
    }
    
    @Override
    protected void setupTeamOptions(org.bukkit.scoreboard.@NotNull Team scoreboardTeam, @NotNull FootRaceTeam team) {
        scoreboardTeam.setAllowFriendlyFire(false);
        scoreboardTeam.setCanSeeFriendlyInvisibles(true);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.DEATH_MESSAGE_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
    }
    
    @Override
    protected void initializeParticipant(FootRaceParticipant participant, FootRaceTeam team) {
        standings.add(participant);
        participant.teleport(config.getStartingLocation());
        participant.setRespawnLocation(config.getStartingLocation(), true);
        giveBoots(participant);
    }
    
    @Override
    protected void initializeTeam(FootRaceTeam team) {
        // do nothing
    }
    
    public void giveBoots(Participant participant) {
        Color teamColor = gameManager.getTeam(participant).getBukkitColor();
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(teamColor);
        boots.setItemMeta(meta);
        participant.getEquipment().setBoots(boots);
    }
    
    @Override
    protected void cleanup() {
        Bukkit.getScheduler().cancelTask(timerRefreshTaskId);
        Bukkit.getScheduler().cancelTask(statusEffectsTaskId);
        Bukkit.getScheduler().cancelTask(standingsDisplayTaskId);
        closeGlassBarrier();
        standings.clear();
    }
    
    @Override
    protected FootRaceParticipant createParticipant(Participant participant) {
        return new FootRaceParticipant(participant, 0, 0);
    }
    
    @Override
    protected FootRaceParticipant createParticipant(Participant participant, FootRaceParticipant.QuitData quitData) {
        return new FootRaceParticipant(participant, quitData);
    }
    
    @Override
    protected FootRaceParticipant.QuitData getQuitData(FootRaceParticipant participant) {
        return participant.getQuitData();
    }
    
    @Override
    protected void resetParticipant(FootRaceParticipant participant, FootRaceTeam team) {
        
    }
    
    @Override
    protected FootRaceTeam createTeam(Team team) {
        return new FootRaceTeam(team, 0);
    }
    
    @Override
    protected FootRaceTeam.QuitData getQuitData(FootRaceTeam team) {
        return team.getQuitData();
    }
    
    @Override
    protected FootRaceTeam createTeam(Team team, FootRaceTeam.QuitData quitData) {
        return new FootRaceTeam(team, quitData.getScore());
    }
    
    @Override
    protected void initializeAdminSidebar() {
        adminSidebar.addLines(
                new KeyLine("elapsedTime", "00:00:000"),
                new KeyLine("timer", Component.empty()),
                new KeyLine("standing1", Component.empty()),
                new KeyLine("standing2", Component.empty()),
                new KeyLine("standing3", Component.empty()),
                new KeyLine("standing4", Component.empty()),
                new KeyLine("standing5", Component.empty())
        );
    }
    
    @Override
    protected void resetAdmin(Player admin) {
        // do nothing
    }
    
    @Override
    protected void initializeAdmin(Player admin) {
        admin.teleport(config.getStartingLocation());
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        resetAdmin(admin);
        admins.remove(admin);
    }
    
    @Override
    protected void initializeSidebar() {
        sidebar.addLines(
                new KeyLine("elapsedTime", "00:00:000"),
                new KeyLine("lap", Component.empty()
                        .append(Component.text("Lap: 1/"))
                        .append(Component.text(config.getLaps()))),
                new KeyLine("timer", Component.empty()),
                new KeyLine("standing1", Component.empty()),
                new KeyLine("standing2", Component.empty()),
                new KeyLine("standing3", Component.empty()),
                new KeyLine("standing4", Component.empty()),
                new KeyLine("standing5", Component.empty())
        );
    }
    
    @Override
    protected @Nullable SpectatorBoundary getSpectatorBoundary() {
        return config.getSpectatorBoundary();
    }
    
    @Override
    protected boolean shouldPreventInteractions(@NotNull Material type) {
        return config.getPreventInteractions().contains(type);
    }
    
}

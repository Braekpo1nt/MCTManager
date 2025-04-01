package org.braekpo1nt.mctmanager.games.experimental;


import org.braekpo1nt.mctmanager.participant.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public interface GameStateBase<P extends ParticipantData, T extends ScoredTeamData<P>> {
    
    void cleanup();
    
    // join start
    /**
     * 
     * @param team the team that is rejoining
     */
    void onTeamRejoin(T team);
    
    /**
     * 
     * @param team the team that is joining for the first time
     */
    void onNewTeamJoin(T team);
    
    /**
     * Called when a participant rejoins the game after having quit
     * @param participant the participant who is rejoining
     * @param team the participant's team
     */
    void onParticipantRejoin(P participant, T team);
    
    /**
     * Called when a participant joins the game for the first time
     * @param participant the participant who is joining for the first time
     * @param team the participant's team
     */
    void onNewParticipantJoin(P participant, T team);
    // join end
    
    /**
     * <p>This is called before removing the participant from {@link GameBase#getParticipants()},
     * and before removing them from their {@link T} team.</p>
     * @param participant the participant who quit. 
     * @param team the participant's team.
     */
    void onParticipantQuit(P participant, T team);
    
    /**
     * <p>React to a team quitting (all its members have quit).</p>
     * @param team the team that has quit. Has no more members. 
     *             (Will not be found in {@link GameBase#getTeams()}.)
     */
    void onTeamQuit(T team);
    
    // Listeners
    
    /**
     * <p>State-specific behavior for {@link PlayerMoveEvent}. Called by {@link GameBase#onPlayerMove(PlayerMoveEvent)}.</p>
     * <p>This is called before spectator management.</p>
     * @param event the event
     * @param participant the participant who triggered the event.
     */
    void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull P participant);
    
    /**
     * <p>State-specific behavior for {@link PlayerTeleportEvent}. Called by {@link GameBase#onPlayerTeleport(PlayerTeleportEvent)}.</p>
     * <p>This is called before spectator management.</p>
     * @param event the event
     * @param participant the participant who triggered the event.
     */
    void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull P participant);
    
    /**
     * <p>State-specific behavior for {@link PlayerInteractEvent}. Called by {@link GameBase#onPlayerInteract(PlayerInteractEvent)}.</p>
     * <p>This is called after a check to see if the interacted block should be prevented.</p>
     * @param event the event
     * @param participant the participant who triggered the event.
     */
    void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull P participant);
    
    /**
     * <p>State-specific behavior for {@link EntityDamageEvent}. Called by {@link GameBase#onEntityDamage(EntityDamageEvent)} if the triggering entity is also a participant in this game.</p>
     * @param event the event
     * @param participant the participant who triggered the event.
     */
    void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull P participant);
}

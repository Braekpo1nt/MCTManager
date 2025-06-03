package org.braekpo1nt.mctmanager.games.gamemanager.practice;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Invite {
    /**
     * The game that the invite was regarding
     */
    @Getter
    private final GameInstanceId id;
    /**
     * The teamId of the team who sent the invite
     */
    @Getter
    private final String initiator;
    /**
     * The teams who have been invited
     */
    private final Set<String> guests;
    /**
     * Each guest's RSVP (true is accepted, false is declined, no entry is no response)
     */
    private final Map<String, Boolean> rsvps;
    
    public Invite(
            @NotNull GameInstanceId id,
            @NotNull String initiator) {
        this.id = id;
        this.initiator = initiator;
        this.guests = new HashSet<>();
        this.rsvps = new HashMap<>();
    }
    
    public void addGuest(String teamId) {
        guests.add(teamId);
    }
    
    public void removeGuest(String teamId) {
        guests.remove(teamId);
    }
    
    /**
     * @param teamId the teamId to check
     * @return true if the given team has been invited to this invite,
     * and did not decline
     */
    public boolean isGuest(String teamId) {
        return guests.contains(teamId);
    }
    
    /**
     * @param teamId the teamId to check
     * @return true if the given team is the initiator of this invite
     */
    public boolean isInitiator(String teamId) {
        return initiator.equals(teamId);
    }
    
    /**
     * @param teamId the teamId to check
     * @return true if the given team is a guest or the initiator of this
     * invite
     */
    public boolean isInvolved(String teamId) {
        return isGuest(teamId) || isInitiator(teamId);
    }
    
    public void rsvp(String teamId, boolean rsvp) {
        if (!guests.contains(teamId)) {
            return;
        }
        rsvps.put(teamId, rsvp);
    }
    
    /**
     * @return a set of the teamIds of the guests which RSVPed yes
     * (always includes the initiator's teamId)
     */
    public Set<String> getConfirmedGuestIds() {
        Set<String> confirmed = new HashSet<>();
        confirmed.add(initiator);
        for (String teamId : guests) {
            Boolean rsvp = rsvps.get(teamId);
            if (rsvp != null && rsvp) {
                confirmed.add(teamId);
            }
        }
        return confirmed;
    }
    
    /**
     * @param teamId the team to check
     * @return true if the team RSVPed yes, false if they RSVPed no or have not yet responded
     */
    public boolean isAttending(String teamId) {
        return rsvps.getOrDefault(teamId, false);
    }
    
    public Component getStatusMenuTitle() {
        return Component.empty()
                .append(Component.text(id.getTitle()))
                .append(Component.text(" Invite Status"));
    }
    
    /**
     * @param teamId the teamId to check
     * @return true if the teamId a guest in this invite, and has not
     * rsvped yes or no
     */
    public boolean hasNotResponded(String teamId) {
        return guests.contains(teamId) && !rsvps.containsKey(teamId);
    }
}

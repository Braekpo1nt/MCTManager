package org.braekpo1nt.mctmanager.games.gamemanager.practice;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
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
    private final GameType gameType;
    /**
     * The one who sent the invite
     */
    @Getter
    private final PracticeParticipant initiator;
    /**
     * The teams who have been invited
     */
    private final Set<String> guests;
    /**
     * Each guest's RSVP (true is accepted, false is declined, no entry is no response)
     */
    private final Map<String, Boolean> rsvps;
    /**
     * True if the invite was sent, false otherwise
     */
    @Getter
    public boolean sent = false;
    
    public Invite(
            @NotNull GameType gameType,
            @NotNull PracticeParticipant initiator) {
        this.gameType = gameType;
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
    
    public boolean isGuest(String teamId) {
        return guests.contains(teamId);
    }
    
    public void rsvp(String teamId, boolean rsvp) {
        if (!guests.contains(teamId)) {
            return;
        }
        rsvps.put(teamId, rsvp);
    }
    
    public void send() {
        sent = true;
    }
    
    /**
     * @return a set of the teamIds of the guests which RSVPed yes
     * (always includes the initiator's teamId)
     */
    public Set<String> getConfirmedGuestIds() {
        Set<String> confirmed = new HashSet<>();
        confirmed.add(initiator.getTeamId());
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
                .append(Component.text(gameType.getTitle()))
                .append(Component.text(" Invite Status"));
    }
}

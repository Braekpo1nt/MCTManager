package org.braekpo1nt.mctmanager.participant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.utils.AudienceDelegate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a participant without a {@link Player} object. 
 * <p>
 * Implementations of {@link OfflineParticipant} should not use {@link EqualsAndHashCode}
 * so that only the {@link #uniqueId} is used to check for equality. 
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class OfflineParticipant implements AudienceDelegate {
    
    /**
     * The UUID of the player this Participant represents
     */
    @EqualsAndHashCode.Include
    protected final @NotNull UUID uniqueId;
    /**
     * The teamId of the team this Participant belongs to
     */
    @Getter
    protected final @NotNull String teamId;
    /**
     * the IGN of the player this represents
     */
    protected final @NotNull String name;
    
    protected final @NotNull Component displayName;
    /**
     * The participant's score
     */
    @Getter
    private final int score;
    
    /**
     * Create a new OfflineParticipant 
     * @param uniqueId the UUID of the player this participant represents
     * @param name the IGN of the player this represents
     * @param displayName the display name (usually the color of their team) of this participant
     * @param teamId the teamId of the participant
     * @param score the participant's score
     */
    public OfflineParticipant(@NotNull UUID uniqueId, @NotNull String name, @NotNull Component displayName, @NotNull String teamId, int score) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.displayName = displayName;
        this.teamId = teamId;
        this.score = score;
    }
    
    /**
     * Create a new OfflineParticipant
     * @param player the player this participant represents. The UUID, the name, the display name
     * @param teamId the teamId of the participant
     * @param score the participant's score
     */
    public OfflineParticipant(@NotNull Player player, @NotNull String teamId, int score) {
        this(player.getUniqueId(), player.getName(), player.displayName(), teamId, score);
    }
    
    /**
     * Copy everything about the given offline participant, but use the new score
     * @param offlineParticipant the offline participant to copy everything but the score from
     * @param newScore the score to use
     */
    public OfflineParticipant(OfflineParticipant offlineParticipant, int newScore) {
        this(offlineParticipant.getUniqueId(), offlineParticipant.getName(), offlineParticipant.displayName(), offlineParticipant.getTeamId(), newScore);
    }
    
    /**
     * @param offlineParticipant the OfflineParticipant to copy the info from
     */
    public OfflineParticipant(@NotNull OfflineParticipant offlineParticipant) {
        this(offlineParticipant, offlineParticipant.getScore());
    }
    
    @Override
    public @NotNull Audience getAudience() {
        return Audience.empty();
    }
    
    /**
     * @return the UUID of the player this Participant represents
     */
    public @NotNull UUID getUniqueId() {
        return uniqueId;
    }
    
    /**
     * @return the Player associated with this participant if they are online, null if they are not
     */
    public @Nullable Player getPlayer() {
        return null;
    }
    
    /**
     * @return the IGN of the player this represents
     */
    public @NotNull String getName() {
        return name;
    }
    
    /**
     * @return this participant's displayName
     */
    public @NotNull Component displayName() {
        return displayName;
    }
    
}

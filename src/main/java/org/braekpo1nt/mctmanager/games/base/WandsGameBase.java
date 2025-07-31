package org.braekpo1nt.mctmanager.games.base;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.base.states.GameStateBase;
import org.braekpo1nt.mctmanager.games.editor.wand.Wand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
public abstract class WandsGameBase<P extends ParticipantData, T extends ScoredTeamData<P>, QP extends QuitDataBase, QT extends QuitDataBase, S extends GameStateBase<P, T>> extends GameBase<P, T, QP, QT, S> {
    
    
    protected final @NotNull Collection<Wand<P>> wands;
    private int wandTickTaskId;
    
    /**
     * Initialize data and start the game
     *
     * @param gameInstanceId the {@link GameInstanceId} associated with this game
     * @param plugin         the plugin
     * @param gameManager    the GameManager
     * @param title          the game's initial title, displayed in the sidebar
     * @param initialState   the initialization state, should not contain any game functionality.
     *                       The state must never be null, so this is what the state should be
     *                       as the game is being initialized to prevent null-pointer
     *                       exceptions.
     */
    public WandsGameBase(
            @NotNull GameInstanceId gameInstanceId,
            @NotNull Main plugin,
            @NotNull GameManager gameManager,
            @NotNull Component title,
            @NotNull S initialState) {
        super(gameInstanceId, plugin, gameManager, title, initialState);
        this.wands = new ArrayList<>();
    }
    
    protected @NotNull Wand<P> addWand(@NotNull Wand<P> wand) {
        wands.add(wand);
        return wand;
    }
    
    /**
     * Kicks off a task in which every {@link Wand} in {@link #wands}'s 
     * {@link Wand#onHoldTick(PlayerInventory, Audience)} method is called for every {@link A} in {@link #admins}
     */
    protected void startWandTick() {
        this.wandTickTaskId = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            participants.values().forEach(
                    participant -> wands.forEach(
                            wand -> wand.onHoldTick(
                                    participant.getPlayer().getInventory(), 
                                    participant
                            )
                    )
            );
        }, 0L, 10L).getTaskId();
    }
    
    @Override
    protected void start(@NotNull Collection<Team> newTeams, @NotNull Collection<Participant> newParticipants, @NotNull List<Player> newAdmins) {
        super.start(newTeams, newParticipants, newAdmins);
        startWandTick();
    }
    
    @Override
    public void stop() {
        plugin.getServer().getScheduler().cancelTask(wandTickTaskId);
        super.stop();
    }
    
    /**
     * Use this to give a participant all the wands you registered with
     * {@link #addWand(Wand)}.
     * @return an array of the items associated with the {@link #wands}
     */
    public ItemStack[] getWandItems() {
        return wands.stream()
                .map(Wand::getWandItem)
                .toArray(ItemStack[]::new);
    }
    
    @Override
    protected void onParticipantInteract(@NotNull PlayerInteractEvent event, P participant) {
        wands.forEach(wand -> wand.onPlayerInteract(event, participant));
        super.onParticipantInteract(event, participant);
    }
}

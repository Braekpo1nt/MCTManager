package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PreRoundState extends FinalStateBase {
    
    private @Nullable Timer timer;
    
    public PreRoundState(@NotNull FinalGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        // TODO: clean the items on the ground
        resetLava();
        context.getTabList().setParticipantGreys(context.getParticipants().values(), false);
        for (FinalParticipant participant : context.getParticipants().values()) {
            switch (participant.getAffiliation()) {
                case NORTH -> {
                    participant.teleport(context.getConfig().getNorthMap().getSpawn());
                    participant.setKitId(null);
                    ParticipantInitializer.clearStatusEffects(participant);
                    ParticipantInitializer.resetHealthAndHunger(participant);
                    participant.setGameMode(GameMode.ADVENTURE);
                }
                case SOUTH -> {
                    participant.teleport(context.getConfig().getSouthMap().getSpawn());
                    participant.setKitId(null);
                    ParticipantInitializer.clearStatusEffects(participant);
                    ParticipantInitializer.resetHealthAndHunger(participant);
                    participant.setGameMode(GameMode.ADVENTURE);
                }
            }
        }
        context.updateRoundSidebar();
        context.updateAliveStatus(Affiliation.NORTH);
        context.updateAliveStatus(Affiliation.SOUTH);
        this.timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundStartingDuration())
                .withTopbar(context.getTopbar())
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .topbarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> {
                    context.setState(new KitSelectionState(context));
                })
                .build());
    }
    
    private void resetLava() {
        BoundingBox lavaArea = context.getConfig().getLava().getLavaArea();
        BlockPlacementUtils.createCubeReplace(
                context.getConfig().getWorld(),
                // all but the bottom layer
                new BoundingBox(
                        lavaArea.getMinX(),
                        lavaArea.getMinY() + 1,
                        lavaArea.getMinZ(),
                        lavaArea.getMaxX(),
                        lavaArea.getMaxY(),
                        lavaArea.getMaxZ()
                ),
                Material.LAVA,
                Material.AIR
        );
        BlockPlacementUtils.createCubeReplace(
                context.getConfig().getWorld(),
                // the bottom layer
                new BoundingBox(
                        lavaArea.getMinX(),
                        lavaArea.getMinY(),
                        lavaArea.getMinZ(),
                        lavaArea.getMaxX(),
                        lavaArea.getMinY(), // bottom layer
                        lavaArea.getMaxZ()
                ),
                Material.AIR,
                Material.LAVA
        );
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
}

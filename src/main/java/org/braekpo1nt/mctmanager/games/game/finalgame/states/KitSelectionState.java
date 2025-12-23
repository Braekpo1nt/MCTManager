package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalTeam;
import org.braekpo1nt.mctmanager.games.game.finalgame.KitPicker;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitSelectionState extends FinalStateBase {
    
    private @Nullable Timer timer;
    private final KitPicker northKitPicker;
    private final KitPicker southKitPicker;
    
    public KitSelectionState(@NotNull FinalGame context) {
        super(context);
        this.northKitPicker = new KitPicker(
                context.getConfig().getKits(),
                context.getNorthTeam().getParticipants(),
                context.getNetherStar(),
                context.getNorthTeam().getBukkitColor()
        );
        this.southKitPicker = new KitPicker(
                context.getConfig().getKits(),
                context.getSouthTeam().getParticipants(),
                context.getNetherStar(),
                context.getSouthTeam().getBukkitColor()
        );
    }
    
    @Override
    public void enter() {
        northKitPicker.showGuis();
        southKitPicker.showGuis();
        this.timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withTopbar(context.getTopbar())
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Kit selection: "))
                .onCompletion(() -> {
                    for (FinalParticipant participant : context.getParticipants().values()) {
                        participant.getInventory().remove(context.getNetherStar());
                    }
                    northKitPicker.stop(true);
                    southKitPicker.stop(true);
                    context.setState(new RoundActiveState(context));
                })
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
        northKitPicker.stop(false);
        southKitPicker.stop(false);
    }
    
    @Override
    public void onOpenKitPicker(@NotNull FinalParticipant participant) {
        switch (participant.getAffiliation()) {
            case NORTH -> {
                northKitPicker.showGui(participant);
            }
            case SOUTH -> {
                southKitPicker.showGui(participant);
            }
        }
    }
    
    @Override
    public void onNewParticipantJoin(FinalParticipant participant, FinalTeam team) {
        super.onNewParticipantJoin(participant, team);
        switch (participant.getAffiliation()) {
            case NORTH -> {
                participant.getInventory().addItem(context.getNetherStar());
                northKitPicker.addParticipant(participant);
            }
            case SOUTH -> {
                participant.getInventory().addItem(context.getNetherStar());
                southKitPicker.addParticipant(participant);
            }
        }
    }
    
    @Override
    public void onParticipantRejoin(FinalParticipant participant, FinalTeam team) {
        super.onParticipantRejoin(participant, team);
        switch (participant.getAffiliation()) {
            case NORTH -> {
                participant.getInventory().addItem(context.getNetherStar());
                northKitPicker.addParticipant(participant);
            }
            case SOUTH -> {
                participant.getInventory().addItem(context.getNetherStar());
                southKitPicker.addParticipant(participant);
            }
        }
    }
    
    @Override
    public void onParticipantQuit(FinalParticipant participant, FinalTeam team) {
        super.onParticipantQuit(participant, team);
        switch (participant.getAffiliation()) {
            case NORTH -> {
                participant.getInventory().addItem(context.getNetherStar());
                northKitPicker.removeParticipant(participant.getUniqueId());
            }
            case SOUTH -> {
                participant.getInventory().addItem(context.getNetherStar());
                southKitPicker.removeParticipant(participant.getUniqueId());
            }
        }
    }
}

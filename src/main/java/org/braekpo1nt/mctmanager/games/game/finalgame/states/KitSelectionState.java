package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
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
        this.northKitPicker = new KitPicker(context.getConfig().getKits(), context.getNorthTeam().getParticipants());
        this.southKitPicker = new KitPicker(context.getConfig().getKits(), context.getSouthTeam().getParticipants());
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
                    context.setState(new PreRoundState(context));
                })
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
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
}

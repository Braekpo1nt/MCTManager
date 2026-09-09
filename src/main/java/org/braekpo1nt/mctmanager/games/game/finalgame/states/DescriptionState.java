package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalTeam;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalTeam;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DescriptionState extends FinalStateBase {
    
    private @Nullable Timer timer;
    
    public DescriptionState(@NotNull FinalGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        context.placeConcrete();
        context.messageAllParticipants(context.getConfig().getDescription());
        this.timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withTopbar(context.getTopbar())
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> {
                    context.setState(new PreRoundState(context));
                })
                .build());
        
        List<FinalParticipant> spectators = context.getParticipants()
                .values()
                .stream()
                .filter(
                        p -> p.getAffiliation().equals(Affiliation.SPECTATOR)
                ).toList();
        
        FinalTeam northTeam = context.getNorthTeam();
        FinalTeam southTeam = context.getSouthTeam();
        List<FinalTeam> teams = new ArrayList<>();
        teams.add(northTeam);
        teams.add(southTeam);
        for(FinalTeam team: teams) {
            String teamPointerKey = team.getColorAttributes().getFinger();
            ItemStack teamPointer = new ItemStack(Material.ARMADILLO_SCUTE, 2);
            ItemMeta teamMeta = teamPointer.getItemMeta();
            CustomModelDataComponent teamData = teamMeta.getCustomModelDataComponent();
            teamData.setStrings(List.of(teamPointerKey));
            teamMeta.setCustomModelDataComponent(teamData);
            teamMeta.setDisplayName(team.getDisplayName());
            teamPointer.setItemMeta(teamMeta);
            spectators.stream().iterator().forEachRemaining(p -> p.getInventory().addItem(teamPointer));
        }
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
    
    @Override
    public void onParticipantRejoin(FinalParticipant participant, FinalTeam team) {
        super.onParticipantRejoin(participant, team);
        participant.sendMessage(context.getConfig().getDescription());
    }
    
    @Override
    public void onNewParticipantJoin(FinalParticipant participant, FinalTeam team) {
        super.onNewParticipantJoin(participant, team);
        participant.sendMessage(context.getConfig().getDescription());
    }
}

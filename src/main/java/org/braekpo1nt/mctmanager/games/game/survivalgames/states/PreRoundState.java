package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.glow.GlowManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class PreRoundState extends OnPlatformsState {
    
    public PreRoundState(@NotNull SurvivalGamesGame context) {
        super(context);
    }
    
    protected void initializeRound() {
        for (SurvivalGamesTeam team : context.getTeams().values()) {
            context.updateAliveCount(team);
        }
        context.setBorderStageIndex(0);
        context.getAdminSidebar().updateLine("respawn", Component.empty());
        context.getSidebar().updateLine("respawn", Component.empty());
        initializeGlowManager();
        context.getGlideTaskIds().clear(); // small maintenance
        context.getConfig().getBorder().initializeWorldBorder(context.getWorldBorder());
        context.createPlatformsAndTeleportTeams();
        ItemStack[] starterLoadout = context.getConfig().getStarterLoadout();
        for (SurvivalGamesParticipant participant : context.getParticipants().values()) {
            ParticipantInitializer.clearInventory(participant);
            participant.setGameMode(GameMode.ADVENTURE);
            participant.setAlive(true);
            if (starterLoadout != null) {
                participant.getInventory().setContents(starterLoadout);
            }
        }
        context.updateRoundLine();
    }
    
    /**
     * Set up all the appropriate glowing effects for the start of the game
     */
    protected void initializeGlowManager() {
        Main.logf("Initializing glow manager for round %d", context.getCurrentRound());
        GlowManager glowManager = context.getGlowManager();
        for (Participant viewer : context.getParticipants().values()) {
            for (Participant target : context.getParticipants().values()) {
                if (!viewer.equals(target) && viewer.sameTeam(target)) {
                    glowManager.showGlowing(viewer, target);
                }
            }
        }
        for (Player viewer : context.getAdmins()) {
            for (SurvivalGamesParticipant target : context.getParticipants().values()) {
                glowManager.showGlowing(viewer, target);
            }
        }
    }
    
}

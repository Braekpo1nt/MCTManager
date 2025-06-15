package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.states.old;

import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.old.ParkourAdminOld;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.old.ParkourPathwayEditorOld;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class ParkourPathwayEditorStateBaseOld implements ParkourPathwayEditorStateOld {
    
    protected final @NotNull ParkourPathwayEditorOld context;
    
    public ParkourPathwayEditorStateBaseOld(@NotNull ParkourPathwayEditorOld context) {
        this.context = context;
    }
    
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onAdminJoin(ParkourAdminOld admin) {
        admin.getPlayer().teleport(context.getConfig().getStartingLocation());
    }
    
    @Override
    public void onAdminQuit(ParkourAdminOld admin) {
        
    }
    
    @Override
    public void validateConfig(@NotNull String configFile) throws ConfigException {
        context.getConfigController().validateConfig(context.getConfig(), configFile);
    }
    
    @Override
    public void saveConfig(@NotNull String configFile) throws ConfigException {
        context.getConfigController().saveConfig(context.getConfig(), configFile);
    }
    
    @Override
    public void loadConfig(@NotNull String configFile) throws ConfigException {
        ParkourPathwayConfig config = context.getConfigController().getConfig(configFile);
        context.setConfig(config);
    }
    
    @Override
    public void onAdminInteract(PlayerInteractEvent event, ParkourAdminOld admin) {
        
    }
}

package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.states;

import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.editor.Admin;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.ParkourAdmin;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.ParkourPathwayEditor;
import org.jetbrains.annotations.NotNull;

public class EditingState extends ParkourPathwayEditorStateBase {
    
    public EditingState(@NotNull ParkourPathwayEditor context) {
        super(context);
        for (ParkourAdmin admin : context.getAdmins().values()) {
            context.selectPuzzle(admin, 0, false);
        }
    }
    
    @Override
    public void loadConfig(@NotNull String configFile) throws ConfigException {
        super.loadConfig(configFile);
        context.setPuzzles(context.getConfig().getPuzzles());
        for (ParkourAdmin admin : context.getAdmins().values()) {
            context.selectPuzzle(
                    admin,
                    admin.getCurrentPuzzle(),
                    admin.getCurrentInBound(),
                    admin.getCurrentCheckPoint(),
                    false
            );
        }
    }
}

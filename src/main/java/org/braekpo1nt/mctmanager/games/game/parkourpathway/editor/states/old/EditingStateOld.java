package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.states.old;

import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.old.ParkourAdminOld;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.old.ParkourPathwayEditorOld;
import org.jetbrains.annotations.NotNull;

public class EditingStateOld extends ParkourPathwayEditorStateBaseOld {
    
    public EditingStateOld(@NotNull ParkourPathwayEditorOld context) {
        super(context);
        for (ParkourAdminOld admin : context.getAdmins().values()) {
            context.selectPuzzle(admin, 0, false);
        }
    }
    
    @Override
    public @NotNull CommandResult loadConfig(@NotNull String configFile) throws ConfigException {
        super.loadConfig(configFile);
        context.setPuzzles(context.getConfig().getPuzzles());
        for (ParkourAdminOld admin : context.getAdmins().values()) {
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

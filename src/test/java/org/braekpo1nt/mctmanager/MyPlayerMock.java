package org.braekpo1nt.mctmanager;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.papermc.paper.entity.LookAnchor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MyPlayerMock extends PlayerMock {
    
    public MyPlayerMock(@NotNull ServerMock server, @NotNull String name) {
        super(server, name);
    }
    
    public MyPlayerMock(@NotNull ServerMock server, @NotNull String name, @NotNull UUID uuid) {
        super(server, name, uuid);
    }
    
    @Override
    public void lookAt(double x, double y, double z, @NotNull LookAnchor playerAnchor) {}
}

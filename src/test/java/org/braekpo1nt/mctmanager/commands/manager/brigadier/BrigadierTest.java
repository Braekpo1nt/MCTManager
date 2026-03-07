package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

class BrigadierTest {
    
    PretendPluginManager pluginManager;
    
    @BeforeEach
    void beforeEach() {
        this.pluginManager = new PretendPluginManager();
    }
    
    @Test
    void singleNodeTest() {
        CommandNode<CommandSourceStack> command = PermissionedWrapper.literal("test")
                .build(pluginManager);
        Assertions.assertNotNull(command);
        Assertions.assertEquals(Set.of("test"), pluginManager.getPermissionNodes().keySet());
    }
    
    @Test
    void doubleNodeTest() {
        CommandNode<CommandSourceStack> command = PermissionedWrapper.literal("foo")
                .then(PermissionedWrapper.literal("bar")
                )
                .build(pluginManager);
        assertThat(command).isNotNull();
        assertThat(pluginManager.getPermissionNodes().keySet()).containsExactlyInAnyOrder("foo", "foo.bar");
        assertThat(command.getChildren()).hasSize(1);
    }
    
    @Test
    void doubleNodeBranchTest() {
        CommandNode<CommandSourceStack> command = PermissionedWrapper.literal("foo")
                .then(PermissionedWrapper.literal("bar")
                )
                .then(PermissionedWrapper.literal("ref"))
                .build(pluginManager);
        assertThat(command).isNotNull();
        assertThat(pluginManager.getPermissionNodes().keySet()).containsExactlyInAnyOrder("foo", "foo.bar", "foo.ref");
        assertThat(command.getChildren()).hasSize(2);
    }
    
}

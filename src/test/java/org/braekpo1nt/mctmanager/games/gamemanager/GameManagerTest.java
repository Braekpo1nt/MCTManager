package org.braekpo1nt.mctmanager.games.gamemanager;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.MyPlayerMock;
import org.braekpo1nt.mctmanager.database.Database;
import org.braekpo1nt.mctmanager.database.entities.AllPlayersEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.MaintenanceParticipantEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.exception.UnimplementedOperationException;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.braekpo1nt.mctmanager.TestUtils.futureIsNotFailure;
import static org.braekpo1nt.mctmanager.TestUtils.futureIsNotNull;

class GameManagerTest {
    
    ServerMock server;
    MockMain plugin;
    GameManager gameManager;
    Database database;
    String teamId;
    
    @BeforeEach
    void setUpServerAndPlugin() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        try {
            plugin = MockBukkit.load(MockMain.class);
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException while setting up " + this.getClass() + ". MockBukkit must not support the functionality/operation you are trying to test. Check the stack trace below for the exact method that threw the exception. Message from exception:" + ex.getMessage());
            Main.logger().log(Level.SEVERE, "UnimplementedOperationException from MockBukkit", ex);
            System.exit(1);
        }
        Main.logger().setLevel(Level.SEVERE);
        gameManager = plugin.getGameManager();
        database = plugin.getDatabase();
        
        teamId = "purple";
        futureIsNotNull(gameManager.addTeam(teamId, "Purple", "dark_purple"));
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        MockBukkit.unmock();
    }
    
    @Test
    void joinPlayer() throws SQLException {
        String ign = "Player1";
        UUID uuid = UUID.randomUUID();
        PlayerMock player = new MyPlayerMock(server, ign, uuid);
        
        // a player who is not in the game state joins
        server.addPlayer(player);
        // this line makes it so that the async schedules the sync task in onPlayerJoin
        server.getScheduler().performOneTick();
        
        AllPlayersEntity allPlayersEntity = database.getAllPlayersDao().queryForId(uuid.toString());
        assertThat(allPlayersEntity).isNotNull();
        assertThat(allPlayersEntity.getIgn()).isEqualTo(ign);
    }
    
    @Test
    void joinParticipant() throws SQLException {
        String ign = "Player1";
        UUID uuid = UUID.randomUUID();
        PlayerMock player = new MyPlayerMock(server, ign, uuid);
        
        futureIsNotFailure(gameManager.joinOfflineParticipant(uuid, ign, teamId));
        // a player who is in the game state joins
        server.addPlayer(player);
        // this line makes it so that the async schedules the sync task in onPlayerJoin
        server.getScheduler().performOneTick();
        
        AllPlayersEntity allPlayersEntity = database.getAllPlayersDao().queryForId(uuid.toString());
        assertThat(allPlayersEntity).isNotNull();
        assertThat(allPlayersEntity.getIgn()).isEqualTo(ign);
        MaintenanceParticipantEntity maintenanceParticipantEntity = database.getMaintenanceParticipantsDao().queryForId(uuid.toString());
        assertThat(maintenanceParticipantEntity).isNotNull();
    }
    
    @Test
    void joinLeaveJoinParticipant() throws SQLException {
        String ign = "Player1";
        UUID uuid = UUID.randomUUID();
        PlayerMock player = new MyPlayerMock(server, ign, uuid);
        
        futureIsNotFailure(gameManager.joinOfflineParticipant(uuid, ign, teamId));
        // a player who is in the game state joins
        server.addPlayer(player);
        // this line makes it so that the async schedules the sync task in onPlayerJoin
        server.getScheduler().performOneTick();
        
        player.disconnect();
        server.addPlayer(player);
        server.getScheduler().performOneTick();
        
        AllPlayersEntity allPlayersEntity = database.getAllPlayersDao().queryForId(uuid.toString());
        assertThat(allPlayersEntity).isNotNull();
        assertThat(allPlayersEntity.getIgn()).isEqualTo(ign);
        MaintenanceParticipantEntity maintenanceParticipantEntity = database.getMaintenanceParticipantsDao().queryForId(uuid.toString());
        assertThat(maintenanceParticipantEntity).isNotNull();
    }
    
    @Test
    void joinWrongIGNToTeam() throws SQLException {
        String rightIGN = "Player1";
        String wrongIGN = "WrongIGN";
        UUID rightUUID = UUID.fromString("f5bc554e-3496-4a7b-b2e2-b7ea444e1d6a");
        PlayerMock rightPlayer = new MyPlayerMock(server, rightIGN, rightUUID);
        
        // add the wrong uuid but the right ign to the game state
        futureIsNotFailure(gameManager.joinOfflineParticipant(rightUUID, wrongIGN, teamId));
        
        // a player with the right uuid and the right ign joins the server
        server.addPlayer(rightPlayer);
        // this line makes it so that the async schedules the sync task in onPlayerJoin
        server.getScheduler().performOneTick();
        
        List<AllPlayersEntity> playersWithIGN = database.getAllPlayersDao().queryForEq("ign", wrongIGN);
        assertThat(playersWithIGN).isEmpty();
        
        AllPlayersEntity rightAllPlayersEntity = database.getAllPlayersDao().queryForId(rightUUID.toString());
        assertThat(rightAllPlayersEntity).isNotNull();
        assertThat(rightAllPlayersEntity.getIgn()).isEqualTo(rightIGN);
    }
    
    @Test
    void joinWrongUUIDToTeam() throws SQLException {
        String rightIGN = "Player1";
        UUID wrongUUID = UUID.fromString("f44b348a-c927-4a51-9d11-60424368ff24");
        UUID rightUUID = UUID.fromString("f5bc554e-3496-4a7b-b2e2-b7ea444e1d6a");
        PlayerMock wrongPlayer = new MyPlayerMock(server, rightIGN, wrongUUID);
        PlayerMock rightPlayer = new MyPlayerMock(server, rightIGN, rightUUID);
        
        // add the wrong uuid but the right ign to the game state
        futureIsNotFailure(gameManager.joinOnlineParticipant(wrongPlayer, teamId));
        
        // a player with the right uuid and the right ign joins the server
        server.addPlayer(rightPlayer);
        server.getScheduler().performOneTick();
        
        AllPlayersEntity wrongAllPlayersEntity = database.getAllPlayersDao().queryForId(wrongUUID.toString());
        assertThat(wrongAllPlayersEntity).isNull();
        AllPlayersEntity rightAllPlayersEntity = database.getAllPlayersDao().queryForId(rightUUID.toString());
        assertThat(rightAllPlayersEntity).isNotNull();
        assertThat(rightAllPlayersEntity.getIgn()).isEqualTo(rightIGN);
    }
    
}

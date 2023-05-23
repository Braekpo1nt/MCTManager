package org.braekpo1nt.mctmanager.games.capturetheflag;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.MyPlayerMock;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.games.gamestate.MockGameStateStorageUtil;
import org.braekpo1nt.mctmanager.ui.MockFastBoardManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class CaptureTheFlagTest {
    
    private ServerMock server;
    private Main plugin;
    private CommandSender sender;
    private MockFastBoardManager mockFastBoardManager;
    private GameManager gameManager;
    
    
    @BeforeEach
    void setUpServerAndPlugin() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        try {
            plugin = MockBukkit.load(Main.class);
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException while setting up " + this.getClass() + ". MockBukkit must not support the functionality/operation you are trying to test. Check the stack trace below for the exact method that threw the exception. Message from exception:" + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
        gameManager = plugin.getGameManager();
        mockFastBoardManager = new MockFastBoardManager();
        gameManager.setFastBoardManager(mockFastBoardManager);
        MockGameStateStorageUtil mockGameStateStorageUtil = new MockGameStateStorageUtil(plugin);
        gameManager.setGameStateStorageUtil(mockGameStateStorageUtil);
        sender = server.getConsoleSender();
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        MockBukkit.unmock();
    }
    
    @Test
    @DisplayName("Starting capture the flag with two players has no errors up to the class selection period")
    void twoPlayersGetToMatchStart() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            createParticipant("Player1", "red", "Red");
            createParticipant("Player2", "blue", "Blue");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            speedThroughRoundCountdown();
            speedThroughClassSelection();
            
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException in twoPlayersGetToMatchStart()");
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    void speedThroughRoundCountdown() {
        server.getScheduler().performTicks((20 * 10) + 1); // speed through the startMatchesStartingCountDown()
    }
    
    void speedThroughHalfRoundCountdown() {
        server.getScheduler().performTicks((20 * 5) + 1); // speed through the startMatchesStartingCountDown()
    }
    
    void speedThroughClassSelection() {
        server.getScheduler().performTicks((20 * 20) + 1); // speed through the startClassSelectionPeriod()
    }
    
    void speedThroughHalfClassSelection() {
        server.getScheduler().performTicks((20 * 10) + 1); // speed through the startClassSelectionPeriod()
    }
    
    void speedThroughRound() {
        server.getScheduler().performTicks((20*60*3)+1); // speed through the round
    }
    
    void speedThroughHalfRound() {
        server.getScheduler().performTicks((20*30*3)+1); // speed through half the round
    }
    
    
    MyPlayerMock createParticipant(String name, String teamName, String expectedDisplayName) {
        MyPlayerMock player = new MyPlayerMock(server, name, UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        server.addPlayer(player);
        gameManager.joinPlayerToTeam(player, teamName);
        Assertions.assertTrue(gameManager.isParticipant(player.getUniqueId()));
        return player;
    }
    
    void addTeam(String teamName, String teamDisplayName, String teamColor) {
        gameManager.addTeam(teamName, teamDisplayName, teamColor);
        Assertions.assertTrue(gameManager.hasTeam(teamName));
    }
    
    @Test
    @DisplayName("With 3 teams, the third team gets notified they're on deck")
    void threePlayerOnDeckTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            
            Assertions.assertTrue(player1.receivedMessagePlaintext("Red is competing against Blue this round."));
            Assertions.assertTrue(player2.receivedMessagePlaintext("Blue is competing against Red this round."));
            Assertions.assertTrue(player3.receivedMessagePlaintext("Green is not competing in this round. Their next round is 1"));
            mockFastBoardManager.assertLine(player3.getUniqueId(), 1, "On Deck");
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    // Rounds made tests
    
    @Test
    @DisplayName("four teams make 3 valid rounds")
    void fourTeamsTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            addTeam("purple", "Purple", "dark_purple");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            MyPlayerMock player4 = createParticipant("Player4", "purple", "Purple");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            List<CaptureTheFlagRound> roundsBeforeJoin = ctf.getRounds();
            Assertions.assertEquals(3, roundsBeforeJoin.size());
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("5 teams make 7 valid rounds")
    void fiveTeamsTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            addTeam("purple", "Purple", "dark_purple");
            addTeam("black", "Black", "black");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            MyPlayerMock player4 = createParticipant("Player4", "purple", "Purple");
            MyPlayerMock player5 = createParticipant("Player5", "black", "Black");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            List<CaptureTheFlagRound> roundsBeforeJoin = ctf.getRounds();
            Assertions.assertEquals(7, roundsBeforeJoin.size());
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    // Quit tests
    
    @Test
    @DisplayName("if two participants are on a team, and one quits during round countdown, the show goes on")
    void playerQuitDuringRoundCountdownTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "blue", "Blue");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            speedThroughHalfRoundCountdown();
            player3.disconnect();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            List<Player> participants = ctf.getParticipants();
            Assertions.assertEquals(2, participants.size());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRound);
            List<CaptureTheFlagMatch> currentMatches = currentRound.getMatches();
            Assertions.assertEquals(1, currentMatches.size());
            
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if an entire team quits during round countdown, the round is cancelled")
    void teamQuitDuringRoundCountdownTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            speedThroughHalfRoundCountdown();
            player2.disconnect();
            speedThroughHalfRoundCountdown();
            speedThroughClassSelection();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(1, ctf.getParticipants().size());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRound);
            Assertions.assertEquals(1, currentRound.getParticipants().size());
            List<CaptureTheFlagMatch> matches = currentRound.getMatches();
            Assertions.assertEquals(1, matches.size());
            CaptureTheFlagMatch match = matches.get(0);
            Assertions.assertTrue(match.isAliveInMatch(player1));
            Assertions.assertFalse(match.isAliveInMatch(player2));
            Assertions.assertEquals(1, match.getNorthParticipants().size());
            Assertions.assertEquals(0, match.getSouthParticipants().size());
    
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if two participants are on a team, and one quits during class selection, the show goes on")
    void playerQuitDuringClassSelectionTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "blue", "Blue");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            speedThroughRoundCountdown();
            speedThroughHalfClassSelection();
            player3.disconnect();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            List<Player> participants = ctf.getParticipants();
            Assertions.assertEquals(2, participants.size());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRound);
            List<CaptureTheFlagMatch> currentMatches = currentRound.getMatches();
            Assertions.assertEquals(1, currentMatches.size());
            CaptureTheFlagMatch match = currentMatches.get(0);
            ClassPicker northClassPicker = match.getNorthClassPicker();
            Assertions.assertTrue(northClassPicker.isActive());
            Assertions.assertEquals(1, northClassPicker.getTeamMates().size());
            ClassPicker southClassPicker = match.getSouthClassPicker();
            Assertions.assertTrue(southClassPicker.isActive());
            Assertions.assertEquals(1, southClassPicker.getTeamMates().size());
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if an entire team quits during class selection, the team is considered dead at the start of the match")
    void teamQuitDuringClassSelectionTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            speedThroughRoundCountdown();
            speedThroughHalfClassSelection();
            player2.disconnect();
            speedThroughHalfClassSelection();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(1, ctf.getParticipants().size());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            List<Player> participants = currentRound.getParticipants();
            Assertions.assertNotNull(participants);
            Assertions.assertEquals(1, participants.size());
            List<CaptureTheFlagMatch> matches = currentRound.getMatches();
            Assertions.assertEquals(1, matches.size());
            CaptureTheFlagMatch match = matches.get(0);
            Assertions.assertFalse(match.isAliveInMatch(player2));
            Assertions.assertEquals(1, match.getNorthParticipants().size());
            Assertions.assertEquals(0, match.getSouthParticipants().size());
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if two participants are on a team, and one quits during the match, the show goes on")
    void playerQuitDuringMatchTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "blue", "Blue");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            speedThroughRoundCountdown();
            speedThroughClassSelection();
            player3.disconnect();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(2, ctf.getParticipants().size());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRound);
            Assertions.assertEquals(2, currentRound.getParticipants().size());
            List<CaptureTheFlagMatch> currentMatches = currentRound.getMatches();
            Assertions.assertEquals(1, currentMatches.size());
            CaptureTheFlagMatch match = currentMatches.get(0);
            Assertions.assertEquals(1, match.getNorthParticipants().size());
            Assertions.assertEquals(1, match.getSouthParticipants().size());
            
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if an entire team quits during the match, they are considered dead")
    void teamQuitDuringMatchTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            speedThroughRoundCountdown();
            speedThroughClassSelection();
            player2.disconnect();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(1, ctf.getParticipants().size());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRound);
            Assertions.assertEquals(1, currentRound.getParticipants().size());
            List<CaptureTheFlagMatch> currentMatches = currentRound.getMatches();
            Assertions.assertEquals(1, currentMatches.size());
            CaptureTheFlagMatch match = currentMatches.get(0);
            Assertions.assertEquals(1, match.getNorthParticipants().size());
            Assertions.assertEquals(0, match.getSouthParticipants().size());
            
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if an entire team quits while they still have a round, their next rounds are removed")
    void teamQuitBeforeAllTheirRoundsTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(3, ctf.getParticipants().size());
            Assertions.assertEquals(3, ctf.getRounds().size());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRound);
            Assertions.assertEquals(2, currentRound.getParticipants().size());
            List<Player> onDeckParticipants = currentRound.getOnDeckParticipants();
            Assertions.assertEquals(1, onDeckParticipants.size());
            Assertions.assertTrue(onDeckParticipants.contains(player3));
            mockFastBoardManager.assertLine(player1.getUniqueId(), 2, "Round 1/3");
            mockFastBoardManager.assertLine(player2.getUniqueId(), 2, "Round 1/3");
            mockFastBoardManager.assertLine(player3.getUniqueId(), 2, "Round 1/3");
            
            player3.disconnect();
    
            Assertions.assertEquals(2, ctf.getParticipants().size());
            Assertions.assertEquals(1, ctf.getRounds().size());
            CaptureTheFlagRound currentRoundAfterDisconnect = ctf.getCurrentRound();
            Assertions.assertSame(currentRound, currentRoundAfterDisconnect);
            Assertions.assertEquals(2, currentRoundAfterDisconnect.getParticipants().size());
            Assertions.assertEquals(0, currentRoundAfterDisconnect.getOnDeckParticipants().size());
            mockFastBoardManager.assertLine(player1.getUniqueId(), 2, "Round 1/1");
            mockFastBoardManager.assertLine(player2.getUniqueId(), 2, "Round 1/1");
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if an entire team quits while they still have a round, their next rounds are removed")
    void teamQuitRoundsRemovedTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            addTeam("purple", "Purple", "dark_purple");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            MyPlayerMock player4 = createParticipant("Player4", "purple", "Purple");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(4, ctf.getParticipants().size());
            List<CaptureTheFlagRound> roundsBeforeDisconnect = ctf.getRounds();
            Assertions.assertEquals(3, roundsBeforeDisconnect.size());
            Assertions.assertEquals(2, roundsBeforeDisconnect.get(0).getMatches().size());
            Assertions.assertEquals(2, roundsBeforeDisconnect.get(1).getMatches().size());
            Assertions.assertEquals(2, roundsBeforeDisconnect.get(2).getMatches().size());
            
            CaptureTheFlagRound firstRoundBeforeDisconnect = ctf.getCurrentRound();
            Assertions.assertNotNull(firstRoundBeforeDisconnect);
            List<Player> currentRoundParticipants = firstRoundBeforeDisconnect.getParticipants();
            Assertions.assertEquals(4, currentRoundParticipants.size());
            Assertions.assertTrue(currentRoundParticipants.contains(player1));
            Assertions.assertTrue(currentRoundParticipants.contains(player2));
            Assertions.assertTrue(currentRoundParticipants.contains(player3));
            Assertions.assertTrue(currentRoundParticipants.contains(player4));
            Assertions.assertEquals(0, firstRoundBeforeDisconnect.getOnDeckParticipants().size());
            
            player4.disconnect();
            
            Assertions.assertEquals(3, ctf.getParticipants().size());
            List<CaptureTheFlagRound> roundsAfterDisconnect = ctf.getRounds();
            Assertions.assertEquals(3, roundsAfterDisconnect.size());
            Assertions.assertEquals(2, roundsAfterDisconnect.get(0).getMatches().size());
            Assertions.assertEquals(1, roundsAfterDisconnect.get(1).getMatches().size());
            Assertions.assertEquals(1, roundsAfterDisconnect.get(2).getMatches().size());
    
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if an entire team quits while they still have a round, the current round is unchanged")
    void teamQuitCurrentRoundUnchangedTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            addTeam("purple", "Purple", "dark_purple");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            MyPlayerMock player4 = createParticipant("Player4", "purple", "Purple");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(4, ctf.getParticipants().size());
            List<CaptureTheFlagRound> roundsBeforeDisconnect = ctf.getRounds();
            Assertions.assertEquals(3, roundsBeforeDisconnect.size());
            Assertions.assertEquals(2, roundsBeforeDisconnect.get(0).getMatches().size());
            Assertions.assertEquals(2, roundsBeforeDisconnect.get(1).getMatches().size());
            Assertions.assertEquals(2, roundsBeforeDisconnect.get(2).getMatches().size());
            
            CaptureTheFlagRound currentRoundBeforeDisconnect = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRoundBeforeDisconnect);
            List<Player> currentRoundParticipants = currentRoundBeforeDisconnect.getParticipants();
            Assertions.assertEquals(4, currentRoundParticipants.size());
            Assertions.assertTrue(currentRoundParticipants.contains(player1));
            Assertions.assertTrue(currentRoundParticipants.contains(player2));
            Assertions.assertTrue(currentRoundParticipants.contains(player3));
            Assertions.assertTrue(currentRoundParticipants.contains(player4));
            Assertions.assertEquals(0, currentRoundBeforeDisconnect.getOnDeckParticipants().size());
            
            player4.disconnect();
    
            CaptureTheFlagRound currentRoundAfterDisconnect = ctf.getCurrentRound();
            Assertions.assertSame(currentRoundBeforeDisconnect, currentRoundAfterDisconnect);
            Assertions.assertEquals(3, currentRoundAfterDisconnect.getParticipants().size());
            Assertions.assertEquals(0, currentRoundAfterDisconnect.getOnDeckParticipants().size());
            List<CaptureTheFlagMatch> currentMatchesAfterDisconnect = currentRoundAfterDisconnect.getMatches();
            Assertions.assertEquals(2, currentMatchesAfterDisconnect.size());
    
            speedThroughRoundCountdown();
            speedThroughClassSelection();
            
            Assertions.assertTrue(currentMatchesAfterDisconnect.get(0).isAliveInMatch(player1));
            Assertions.assertTrue(currentMatchesAfterDisconnect.get(0).isAliveInMatch(player2));
            Assertions.assertTrue(currentMatchesAfterDisconnect.get(1).isAliveInMatch(player3));
            Assertions.assertFalse(currentMatchesAfterDisconnect.get(1).isAliveInMatch(player4));
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if an entire team quits while they still have a round, the new set of rounds still progress")
    void teamQuitRoundProgressionTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            addTeam("purple", "Purple", "dark_purple");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            MyPlayerMock player4 = createParticipant("Player4", "purple", "Purple");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            CaptureTheFlagRound firstRound = ctf.getCurrentRound();
            Assertions.assertNotNull(firstRound);
            
            player4.disconnect();
    
            speedThroughRoundCountdown();
            speedThroughClassSelection();
            speedThroughRound();
            speedThroughRoundCountdown();
            speedThroughClassSelection();
            
            Assertions.assertEquals(3, ctf.getRounds().size());
            CaptureTheFlagRound secondRound = ctf.getCurrentRound();
            Assertions.assertNotSame(firstRound, secondRound);
            Assertions.assertEquals(2, secondRound.getParticipants().size());
            Assertions.assertTrue(secondRound.getParticipants().contains(player1));
            Assertions.assertTrue(secondRound.getParticipants().contains(player3));
            Assertions.assertEquals(1, secondRound.getOnDeckParticipants().size());
            Assertions.assertTrue(secondRound.getOnDeckParticipants().contains(player2));
            
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    // Join tests
    
    @Test
    @DisplayName("if a player joins during round count down, they are in the current round")
    void playerJoinRoundCountDown() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "blue", "Blue");
            player3.disconnect();
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
    
            speedThroughHalfRoundCountdown();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(1, ctf.getRounds().size());
            CaptureTheFlagRound currentRoundBeforeJoin = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRoundBeforeJoin);
            Assertions.assertEquals(2, currentRoundBeforeJoin.getParticipants().size());
            
            player3.reconnect();
    
            Assertions.assertEquals(1, ctf.getRounds().size());
            CaptureTheFlagRound currentRoundAfterJoin = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRoundAfterJoin);
            List<Player> participantsAfterJoin = currentRoundAfterJoin.getParticipants();
            Assertions.assertEquals(3, participantsAfterJoin.size());
            Assertions.assertEquals(0, currentRoundAfterJoin.getOnDeckParticipants().size());
            Assertions.assertTrue(participantsAfterJoin.contains(player3));
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if a player joins during round their team isn't in, they are on-deck")
    void playerJoinTeamNotInRound() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("purple", "Purple", "dark_purple");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "purple", "Purple");
            MyPlayerMock player4 = createParticipant("Player4", "purple", "Purple");
            player4.disconnect();
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            
            speedThroughRoundCountdown();
            speedThroughHalfClassSelection();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRound);
            List<Player> onDeckParticipants = currentRound.getOnDeckParticipants();
            Assertions.assertEquals(1, onDeckParticipants.size());
            Assertions.assertTrue(onDeckParticipants.contains(player3));
            
            player4.reconnect();
            
            CaptureTheFlagRound currentRoundAfterRejoin = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRoundAfterRejoin);
            List<Player> onDeckParticipantsAfterRejoin = currentRoundAfterRejoin.getOnDeckParticipants();
            Assertions.assertEquals(2, onDeckParticipantsAfterRejoin.size());
            Assertions.assertTrue(onDeckParticipantsAfterRejoin.contains(player3));
            Assertions.assertTrue(onDeckParticipantsAfterRejoin.contains(player4));
    
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("If a player joins during a round their team is in, they are dead")
    void playerJoinTeamInRound() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "blue", "Blue");
            player3.disconnect();
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            
            speedThroughRoundCountdown();
            speedThroughClassSelection();
            speedThroughHalfRound();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            
            CaptureTheFlagRound currentRoundBeforeDisconnect = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRoundBeforeDisconnect);
            Assertions.assertFalse(currentRoundBeforeDisconnect.isAliveInMatch(player3));
            
            player3.reconnect();
            
            CaptureTheFlagRound currentRoundAfterReconnect = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRoundAfterReconnect);
            Assertions.assertFalse(currentRoundAfterReconnect.isAliveInMatch(player3));
            CaptureTheFlagMatch match = currentRoundAfterReconnect.getMatches().get(0);
            Assertions.assertEquals(3, match.getAllParticipants().size());
            Assertions.assertEquals(1, match.getNorthParticipants().size());
            List<Player> southParticipants = match.getSouthParticipants();
            Assertions.assertEquals(2, southParticipants.size());
            Assertions.assertTrue(southParticipants.contains(player3));
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if a player joins during class selection, they are in the game")
    void playerJoinClassSelection() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "blue", "Blue");
            player3.disconnect();
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            
            speedThroughRoundCountdown();
            speedThroughHalfClassSelection();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(1, ctf.getRounds().size());
            CaptureTheFlagRound currentRoundBeforeJoin = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRoundBeforeJoin);
            Assertions.assertEquals(2, currentRoundBeforeJoin.getParticipants().size());
            
            player3.reconnect();
            
            Assertions.assertEquals(1, ctf.getRounds().size());
            CaptureTheFlagRound currentRoundAfterJoin = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRoundAfterJoin);
            Assertions.assertEquals(3, currentRoundAfterJoin.getParticipants().size());
            Assertions.assertEquals(0, currentRoundAfterJoin.getOnDeckParticipants().size());
            Assertions.assertTrue( currentRoundAfterJoin.getParticipants().contains(player3));
            
            speedThroughHalfClassSelection();
            
            Assertions.assertTrue(currentRoundAfterJoin.isAliveInMatch(player3));
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if a team joins mid-game (after class selection), they are on-deck")
    void teamJoinOnDeck() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            player3.disconnect();
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            
            speedThroughRoundCountdown();
            speedThroughClassSelection();
            speedThroughHalfRound();
            
            player3.reconnect();
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            CaptureTheFlagRound currentRound = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRound);
            List<Player> onDeckParticipants = currentRound.getOnDeckParticipants();
            Assertions.assertEquals(1, onDeckParticipants.size());
            Assertions.assertTrue(onDeckParticipants.contains(player3));
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if a new team joins mid-game, their rounds are added")
    void teamJoinRoundsAdded() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            player3.disconnect();
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            Assertions.assertEquals(1, ctf.getRounds().size());
            
            player3.reconnect();
            
            Assertions.assertEquals(3, ctf.getRounds().size());
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if a new team joins mid-game, their rounds are added to the end")
    void teamJoinRoundsAddedEnd() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            player3.disconnect();
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
    
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            List<CaptureTheFlagRound> roundsBeforeJoin = ctf.getRounds();
            Assertions.assertEquals(1, roundsBeforeJoin.size());
            Assertions.assertFalse(roundsBeforeJoin.get(0).containsTeam("green"));
            
            player3.reconnect();
            
            List<CaptureTheFlagRound> roundsAfterJoin = ctf.getRounds();
            Assertions.assertEquals(3, roundsAfterJoin.size());
            Assertions.assertFalse(roundsAfterJoin.get(0).containsTeam("green"));
            Assertions.assertTrue(roundsAfterJoin.get(1).containsTeam("green"));
            Assertions.assertTrue(roundsAfterJoin.get(2).containsTeam("green"));
    
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if a team joins at the end of many rounds, their rounds are added to the end")
    void teamJoinManyRoundsAddedEnd() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            addTeam("purple", "Purple", "dark_purple");
            addTeam("black", "Black", "black");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            MyPlayerMock player4 = createParticipant("Player4", "purple", "Purple");
            MyPlayerMock player5 = createParticipant("Player5", "black", "Black");
            player5.disconnect();
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
            List<CaptureTheFlagRound> roundsBeforeJoin = ctf.getRounds();
            Assertions.assertEquals(3, roundsBeforeJoin.size());
            speedThroughRoundCountdown();
            speedThroughClassSelection();
            speedThroughRound();
            speedThroughRoundCountdown();
            speedThroughClassSelection();
            speedThroughRound();
            speedThroughRoundCountdown();
            speedThroughClassSelection();
            
            player5.reconnect();
            
            List<CaptureTheFlagRound> roundsAfterJoin = ctf.getRounds();
            Assertions.assertEquals(7, roundsAfterJoin.size());
            
            Assertions.assertFalse(roundsAfterJoin.get(0).containsTeam("black"));
            Assertions.assertFalse(roundsAfterJoin.get(1).containsTeam("black"));
            Assertions.assertFalse(roundsAfterJoin.get(2).containsTeam("black"));
            //all the end rounds contain black
            Assertions.assertTrue(roundsAfterJoin.get(3).containsTeam("black"));
            Assertions.assertTrue(roundsAfterJoin.get(4).containsTeam("black"));
            Assertions.assertTrue(roundsAfterJoin.get(5).containsTeam("black"));
            Assertions.assertTrue(roundsAfterJoin.get(6).containsTeam("black"));
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("if a player quits during the game, then rejoins, they are dead and in the spawn observatory")
    void playerQuitRejoinDead() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "blue", "Blue");
            gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
            
            speedThroughRoundCountdown();
            speedThroughClassSelection();
            speedThroughHalfRound();
    
            CaptureTheFlagGame ctf = ((CaptureTheFlagGame) gameManager.getActiveGame());
    
            CaptureTheFlagRound currentRoundBeforeDisconnect = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRoundBeforeDisconnect);
            Assertions.assertTrue(currentRoundBeforeDisconnect.isAliveInMatch(player3));
            
            player3.disconnect();
            player3.reconnect();
            
            CaptureTheFlagRound currentRoundAfterReconnect = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRoundAfterReconnect);
            Assertions.assertFalse(currentRoundAfterReconnect.isAliveInMatch(player3));
            CaptureTheFlagMatch match = currentRoundAfterReconnect.getMatches().get(0);
            Assertions.assertEquals(3, match.getAllParticipants().size());
            Assertions.assertEquals(1, match.getNorthParticipants().size());
            List<Player> southParticipants = match.getSouthParticipants();
            Assertions.assertEquals(2, southParticipants.size());
            Assertions.assertTrue(southParticipants.contains(player3));
            
        } catch (UnimplementedOperationException ex) {
            ex.printStackTrace();
            Assertions.fail(ex.getMessage());
        }
    }
    
    @Test
    @DisplayName("The timer counts down for observers")
    void timerCountDown() {
        addTeam("red", "Red", "red");
        addTeam("blue", "Blue", "blue");
        addTeam("green", "Green", "green");
        MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
        MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
        MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
        gameManager.startGame(MCTGames.CAPTURE_THE_FLAG, sender);
        
        speedThroughHalfRoundCountdown();
        mockFastBoardManager.assertLine(player1.getUniqueId(), 5, "0:05");
        mockFastBoardManager.assertLine(player2.getUniqueId(), 5, "0:05");
        mockFastBoardManager.assertLine(player3.getUniqueId(), 5, "0:05");
        speedThroughHalfRoundCountdown();
        
        speedThroughHalfClassSelection();
        mockFastBoardManager.assertLine(player1.getUniqueId(), 5, "0:10");
        mockFastBoardManager.assertLine(player2.getUniqueId(), 5, "0:10");
        mockFastBoardManager.assertLine(player3.getUniqueId(), 5, "0:10");
        speedThroughHalfClassSelection();
        
        speedThroughHalfRound();
        mockFastBoardManager.assertLine(player1.getUniqueId(), 5, "1:30");
        mockFastBoardManager.assertLine(player2.getUniqueId(), 5, "1:30");
        mockFastBoardManager.assertLine(player3.getUniqueId(), 5, "1:30");
    }
    
}

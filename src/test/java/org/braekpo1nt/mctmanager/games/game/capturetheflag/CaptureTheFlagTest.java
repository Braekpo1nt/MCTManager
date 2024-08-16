package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import org.braekpo1nt.mctmanager.*;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.config.CaptureTheFlagConfigController;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

class CaptureTheFlagTest {
    
    private ServerMock server;
    private Main plugin;
    private CommandSender sender;
    private GameManager gameManager;


    @BeforeEach
    void setUpServerAndPlugin() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        try {
            plugin = MockBukkit.load(MockMain.class);
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException while setting up " + this.getClass() + ". MockBukkit must not support the functionality/operation you are trying to test. Check the stack trace below for the exact method that threw the exception. Message from exception:" + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
        gameManager = plugin.getGameManager();
        sender = server.getConsoleSender();
        InputStream inputStream = CaptureTheFlagConfigController.class.getResourceAsStream("exampleCaptureTheFlagConfig.json");
        TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), "captureTheFlagConfig.json"));
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
            speedThroughMatchesStarting();
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
    
    void performSeconds(int seconds) {
        server.getScheduler().performTicks((20L * seconds) + 1);
    }
    
    void speedThroughMatchesStarting() {
        performSeconds(10); // speed through the startMatchesStartingCountDown()
    }
    
    void speedThroughHalfMatchesStarting() {
        performSeconds(5); // speed through the startMatchesStartingCountDown()
    }
    
    void speedThroughClassSelection() {
        performSeconds(20); // speed through the startClassSelectionPeriod()
    }
    
    void speedThroughHalfClassSelection() {
        performSeconds(10);; // speed through the startClassSelectionPeriod()
    }
    
    void speedThroughRoundTimer() {
        performSeconds(180); // speed through the round timer
    }
    
    void speedThroughHalfRoundTimer() {
        performSeconds(90);; // speed through half the round timer
    }


    MyPlayerMock createParticipant(String name, String teamName, String expectedDisplayName) {
        MyPlayerMock player = new MyPlayerMock(server, name, UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        server.addPlayer(player);
        gameManager.joinPlayerToTeam(server.getConsoleSender(), player, teamName);
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
            
            Assertions.assertTrue(player1.receivedMessagePlaintext("Red is competing against Blue this round."));
            Assertions.assertTrue(player2.receivedMessagePlaintext("Blue is competing against Red this round."));
            Assertions.assertTrue(player3.receivedMessagePlaintext("Green is on-deck this round."));
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
            
            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());
            Assertions.assertEquals(3, ctf.getMaxRounds());
            
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
            
            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());
            Assertions.assertEquals(6, ctf.getMaxRounds());
            
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
            speedThroughHalfMatchesStarting();
            player3.disconnect();
            
            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());
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
    @DisplayName("if two participants are on a team, and one quits during class selection, the show goes on")
    void playerQuitDuringClassSelectionTest() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "blue", "Blue");
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
            speedThroughMatchesStarting();
            speedThroughHalfClassSelection();
            player3.disconnect();

            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
            speedThroughMatchesStarting();
            speedThroughHalfClassSelection();
            player2.disconnect();
            speedThroughHalfClassSelection();

            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
            speedThroughMatchesStarting();
            speedThroughClassSelection();
            player3.disconnect();

            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
            speedThroughMatchesStarting();
            speedThroughClassSelection();
            player2.disconnect();

            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
            
            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());
            CaptureTheFlagRound firstRound = ctf.getCurrentRound();
            Assertions.assertNotNull(firstRound);
            
            player4.disconnect();
            
            speedThroughMatchesStarting();
            speedThroughClassSelection();
            speedThroughRoundTimer();
            speedThroughMatchesStarting();
            speedThroughClassSelection();
            
            Assertions.assertEquals(4, ctf.getMaxRounds());
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
    
    @Test
    @DisplayName("if a match begins with an offline team, the match is skipped")
    void teamQuitRoundsCancelled() {
        try {
            addTeam("red", "Red", "red");
            addTeam("blue", "Blue", "blue");
            addTeam("green", "Green", "green");
            MyPlayerMock player1 = createParticipant("Player1", "red", "Red");
            MyPlayerMock player2 = createParticipant("Player2", "blue", "Blue");
            MyPlayerMock player3 = createParticipant("Player3", "green", "Green");
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
            
            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());
            speedThroughHalfMatchesStarting();
            
            player2.disconnect();
            
            speedThroughHalfMatchesStarting();
            speedThroughClassSelection();
    
            Assertions.assertTrue(ctf.isGameActive());
            Assertions.assertTrue(ctf.getCurrentRound().containsTeam("red"));
            Assertions.assertTrue(ctf.getCurrentRound().containsTeam("green"));
    
            speedThroughRoundTimer();
            speedThroughMatchesStarting();
            speedThroughClassSelection();
            speedThroughRoundTimer();
    
            Assertions.assertFalse(ctf.isGameActive());
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
            
            speedThroughHalfMatchesStarting();
            
            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());
            Assertions.assertEquals(1, ctf.getMaxRounds());
            CaptureTheFlagRound currentRoundBeforeJoin = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRoundBeforeJoin);
            Assertions.assertEquals(2, currentRoundBeforeJoin.getParticipants().size());
            
            player3.reconnect();
            
            Assertions.assertEquals(1, ctf.getMaxRounds());
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);

            speedThroughMatchesStarting();
            speedThroughHalfClassSelection();

            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);

            speedThroughMatchesStarting();
            speedThroughClassSelection();
            speedThroughHalfRoundTimer();

            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());

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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
            
            speedThroughMatchesStarting();
            speedThroughHalfClassSelection();
            
            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());
            Assertions.assertEquals(1, ctf.getMaxRounds());
            CaptureTheFlagRound currentRoundBeforeJoin = ctf.getCurrentRound();
            Assertions.assertNotNull(currentRoundBeforeJoin);
            Assertions.assertEquals(2, currentRoundBeforeJoin.getParticipants().size());
            
            player3.reconnect();
            
            Assertions.assertEquals(1, ctf.getMaxRounds());
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);

            speedThroughMatchesStarting();
            speedThroughClassSelection();
            speedThroughHalfRoundTimer();

            player3.reconnect();

            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);
            
            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());
            Assertions.assertEquals(1, ctf.getMaxRounds());
            
            player3.reconnect();
            
            Assertions.assertEquals(3, ctf.getMaxRounds());
            
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
            gameManager.startGame(GameType.CAPTURE_THE_FLAG, sender);

            speedThroughMatchesStarting();
            speedThroughClassSelection();
            speedThroughHalfRoundTimer();

            CaptureTheFlagGameOld ctf = ((CaptureTheFlagGameOld) gameManager.getActiveGame());

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
    
}

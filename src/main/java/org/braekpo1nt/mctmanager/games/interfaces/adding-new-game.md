# Adding a new Game

There are a few things you need to do when adding a new game.

- Create a folder under `braekpo1nt/mctmanager/games/<newgame>` (all lower-case, no spaces)

- Create a new enum for the game in `MCTGames.java` (all upper-case, underscores; in this example, it's `MCTGames.NEW_GAME`) 
- add the game to the `StartSubCommand.java` constructor
  - `mctGames.put("new-game", MCTGames.NEW_GAME);`
  - This allows the `/mct game start new-game` command to start your new game
- add the game to `VoteSubCommand.java` constructor
  - `mctGames.put("new-game", MCTGames.NEW_GAME);`
  - This allows the vote sub command to include this game in its list of votable games
- 


# Adding a new Game

There are a few things you need to do when adding a new game.

- Create a new enum for the game in `GameType.java` (all upper-case, underscores; in this example, it's `GameType.NEW_GAME`) 
  - Create a new enum value `NEW_GAME`
    - Give it a `title` and an `id` (for this example, it will be `NEW_GAME("New Game", "new-game")`)
      - `title` is the display name
      - `id` must be lowercase with no spaces (only dashes) and only alphanumeric characters (it must match the `validIdRegex` in the `GameType` constructor, or an `IllegalArgumentException` will be thrown)
  - The Enum for the game is used in various places to identify which game is being played/selected. For example:
    - This allows the `/mct game start new-game` command to start your new game
    - This allows the vote sub command to include this game in its list of votable games
- Add the `GameType.NEW_GAME` to the `GameManager.java`
  - add the game to the voting pool in `startVote()`
    - `votingPool.add(GameType.NEW_GAME);`
  - Create a case in the `startGame()` method switch statement for the game
    - You will implement this after you create the game class in the next few steps, leave it empty for now 
    - Use the other cases for the other games as an indicator 
- Add `GameType.NEW_GAME` to the `VoteManager.java`
  - You need to specify an item to represent your new game in the voting pool. In `showVoteGui()`, add an `ItemStack` for your game. Use the others as an example. In this example, we'll use `Material.STONE` for the material, and call the `ItemStack` variable `newGameItem`.
    - Make sure you add your `newGameItem` to the `votingItems` Map, like so: `votingItems.put(GameType.NEW_GAME, newGame);`.
  - Add a case to the switch statement in `clickVoteInventory()` for `Material.STONE` (or whatever material you used). This is why your material must be unique from the other game materials. Use the other cases as an example. 
- If you haven't already, create a folder under `braekpo1nt/mctmanager/games/game/<newgame>`
- Create your game class, which must implement `org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame`. For this example, we'll call it `NewGame.java`
  - Make `getType()` return `GameType.NEW_GAME`
  - For more details on implementing the `MCTGame` class and writing the functionality for your game, see the other games as examples. There should also be a general guide written soon.
- Add your game to the `GameManager.java`
  - Create a field for your game `private final NewGame newGame;`
  - Initialize `newGame` in `GameManager`'s constructor. Use the other games as an example. 
  - Now is when you implement the case in the switch statement you made for `GameType.NEW_GAME` in `startGame()`. 
    - Use the others as an example. 
    - Note: Colossal Colosseum is a special case, don't use that as an example.
- Now test and make sure you can 
  - run your game with `/mct game start new-game` (which is made possible with the addition to the `VoteSubCommand.java` above)
  - vote for your game with the vote system using `/mct game vote new-game <...any other games you want in the voting pool...>` (made possible by adding it to the `VoteManager.java`)
  - use your game in an Event (`/mct event start <number of games>`)



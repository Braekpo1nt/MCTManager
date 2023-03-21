# Dependencies

This plugin depends on [Multiverse-Core v4.3.1](https://github.com/Multiverse/Multiverse-Core/releases/tag/v4.3.1)

# Running the event

## Participants
In order to host an event, you need to [add participants](#adding-a-new-participant) (the plugin has to know who is a participant and who is an admin).

### Starting a game
You can start a game with the following command:

- `/mct startgame <game>`
  - `<game>` the game to start. Must be a valid game name in the event. See [list of games](#games-list)

### Stopping a game
If a game is running, you can manually stop a game with the following command:

- `/mct stopgame`

This will stop the game, and return all players to the beginning. As of the time of writing this, the points for playing the game will be retained. 

### Adding a new team
Every participant must be on a team, so you must first have at least one team. To add a new team, use the following command:

- `/mct team add <team> <displayname> <color>`
    - `<team>` the internal name of the team (All lowercase)
    - `<displayname>` the display name of the team
    - `<color>` the color of the team



### Adding a new participant
Participants must be on a team, so you must first [add a new team](#adding-a-new-team)

To add a new participant, use the following command:

- `/mct team join <team> <member>`
  - `<team>` the internal name of the team. The team must already exist.
  - `<member>` the name of the player to add. Must be an online player.

## Games List
This is a list of the currently implemented games

- Foot Race
  - Start game with `/mct startgame foot-race`

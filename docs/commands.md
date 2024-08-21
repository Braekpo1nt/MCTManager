# Commands

A comprehensive command reference for the Minecraft Championships (MCT) plugin

# Command Guide

Most, if not all commands are sub-commands of the `/mct <sub-command>` command.

### List and summary of /mct sub commands

| Command                      | Description                              |
|------------------------------|------------------------------------------|
| [`/mct game`](#mct-game)     | Start, stop, and vote for games          |
| [`/mct event`](#mct-event)   | Start, stop, pause, and resume events    |
| [`/mct team`](#mct-team)     | Add/remove, join/leave, and modify teams |
| [`/mct load`](#mct-load)     | Load the game state from memory          |
| [`/mct save`](#mct-save)     | Save the game state to memory            |
| [`/mct option`](#mct-option) | Toggle options for debugging and testing |

## /mct game

Start, stop, and vote for games.

- `/mct game start <game>`
  - Starts the specified game
  - `<game>` the game to start. Must be a valid game name in the event.
- `/mct game stop [true|false]`
  - This will stop the currently running game (if there is one), and return all players to the beginning. As of the time of writing this, the points for playing the game will be retained.
  - `[true|false]` This is an optional argument (defaults to true). If you provide false, the teleport to the hub will be cancelled. This is a debugging feature, and players will often not be reset properly after a game ends.
- `/mct game vote [one or more games]`
  - Initiates a vote for all participants using the provided games as the voting pool.
  - for example: `/mct game vote spleef foot-race capture-the-flag` will show all players a voting gui with those three games, and no others.

## /mct event

Start, stop, pause and resume events.

Starting an event kicks off an autonomous flow of a set number of games (specified by the command sender) with a 5-minute break in the middle. The flow looks like this:

- Waiting period in the hub
- Voting phase where players vote for the next game (games during an event can't be voted for/played more than once).
- The voted for game is played through to completion
- Repeat

After all the games in the event have been played (or there are no more games to be played) the top two winners will be sent to the final round to determine the overall champion. 

You can pause the flow of the event during the waiting period in the hub. You can't pause the event during any other period.

You can stop games manually with the `/mct game stop` command. This will continue the flow of the event as normal. Note: `/mct game stop false` has unknown effects on the event flow, and may break the event. More robustness will be added in future updates. 

When an event is over (either by naturally ending, or stopping manually) the points of the teams and players will be saved in the `plugins/MCTManager/gameState.json` file. The games that were played during the event will also be saved to that. However, starting a new event will clear the list of saved already-played games. 

- `/mct event start [number of games]`
  - Starts the event
  - `[number of games]` Optional argument to specify the number of games to play in the event. If omitted, defaults to 6 games.
  - You can't start an event while another event is going on
- `/mct event stop [confirm]`
  - Stops the event. This will stop the flow, and no more games will be automatically started by the event. 
  - `[confirm]` you must provide this argument for it to stop the event. If you don't provide this argument, you will be asked to confirm the stop.
- `/mct event pause`
  - Pauses the event. The event can only be paused during the waiting period between the end of a game and the voting phase. 
- `/mct event resume`
  - Resumes a paused event.


## /mct team

- `/mct team add <team> <"displayname"> <color>`
  - `<team>` the internal name of the team. Must match requirements of Minecraft team names. Can only be made up of these characters: `-`, `+`, `.`, `_`, `A-Z`, `a-z`, and `0-9`.
  - `<"displayname">` the display name of the team. Must be a quoted string.
  - `<color>` the color of the team
- `/mct team remove <team>`
  - Removes the specified team
  - `<team>` the same as above. The internal name of the team, specified during the initial creation of the team.
- `/mct team join <team> <player>`
  - Add the specified player to the specified team
  - `<team>` the internal name of the team. The team must already exist.
  - `<player>` the name of the player to join to the team. Must be an online player.
- `/mct team remove <player>`
  - Remove the specified player from their team. This loses any points the player had.
  - `<player>` the name of the player to remove. Does not have to be an online player, but does have to already be joined to a team.
  - Note: This will delete a participants points, but as of right now doesn't remove those points from the team they used to be on
- `/mct team list [true|false]`
  - Displays the list of the teams, the team scores, the players on those teams, and the player scores.
  - `[true|false]` whether or not to display the teams to all participants. Defaults to false. If you specify true, it will display the team list to all participants.
- `/mct team score <add|subtract|set> <player|team> <playerName|teamId> <value>`
  - `<add|subtract|set>`
    - `add` add the `value`
    - `subtract` subtract the `value`
    - `set` set the score to the `value`
  - `<player|team>` specify player or team
  - `<playerName|teamId>` If you specified `player` above, enter the `playerName` of the player whose score you want to modify. If you specified `team` above, enter the `teamId` of the team whose score you want to modify.
  - `<value>` the value you want to `add`/`subtract`/`set`.
    - Must be an integer.
    - Can be negative for `add`/`subtract`
      - If you add a negative number it's subtracted, if you subtract a negative number it's added.
    - Must be positive for `set`.
    - You can't modify a score so that it's less than 0. If you try to subtract a number from a score that would cause it to go negative, the score is simply set to 0.

## /mct load

## /mct save

## /mct option

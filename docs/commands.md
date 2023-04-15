# Commands

A comprehensive command reference for the Minecraft Championships (MCT) plugin

# Command Guide

Most, if not all commands are sub-commands of the `/mct <sub-command>` command.

### List and summary of /mct sub commands

| Command                      | Description                              |
|------------------------------|------------------------------------------|
| [`/mct game`](#mct-game)     | Start, stop, and vote for games.         |
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
  - for example: `/mct game vote spleef foot-race mecha` will show all players a voting gui with those three games, and no others.

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
- `/mct team score <add|subtract|set> <player|team> <playerName|teamName> <value>`
  - `<add|subtract|set>`
    - `add` add the `value`
    - `subtract` subtract the `value`
    - `set` set the score to the `value`
  - `<player|team>` specify player or team
  - `<playerName|teamName>` If you specified `player` above, enter the `playerName` of the player whose score you want to modify. If you specified `team` above, enter the `teamName` of the team whose score you want to modify.
  - `<value>` the value you want to `add`/`subtract`/`set`.
    - Must be an integer.
    - Can be negative for `add`/`subtract`
      - If you add a negative number it's subtracted, if you subtract a negative number it's added.
    - Must be positive for `set`.
    - You can't modify a score so that it's less than 0. If you try to subtract a number from a score that would cause it to go negative, the score is simply set to 0.

## /mct load

## /mct save

## /mct option

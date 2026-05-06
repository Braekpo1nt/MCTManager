# SQl Setup

## Tested

- Uses MySQL or MariaDB

## Setting up readonly user

The live website reader needs a read-only user with access to specific tables. Below is a script to create that user.

This assumes your database name is `challenger_trials`. Make sure you replace `<strong-password>` with an actual
password.

```sql
CREATE USER 'readonly_user'@'%' IDENTIFIED BY '<strong-password>';
GRANT SELECT ON `challenger_trials`.`system_state` TO `readonly_user`@`%`;
GRANT SELECT ON `challenger_trials`.`event_teams` TO `readonly_user`@`%`;
GRANT SELECT ON `challenger_trials`.`in_game_teams` TO `readonly_user`@`%`;
GRANT SELECT ON `challenger_trials`.`game_sessions` TO `readonly_user`@`%`;
GRANT SELECT ON `challenger_trials`.`event_participants` TO `readonly_user`@`%`;
GRANT SELECT ON `challenger_trials`.`active_teams` TO `readonly_user`@`%`;
GRANT SELECT ON `challenger_trials`.`in_game_participants` TO `readonly_user`@`%`;
GRANT SELECT ON `challenger_trials`.`active_participants` TO `readonly_user`@`%`;
GRANT SELECT ON `challenger_trials`.`event_info` TO `readonly_user`@`%`;
```

```mermaid
---
title: Survival Games
---
stateDiagram-v2


    [*] --> Description
    Description --> PreRound
    PreRound --> GracePeriod
    GracePeriod --> BorderDelay

    state border_states_left <<choice>>
    BorderDelay --> BorderShrinking
    BorderShrinking --> border_states_left

    border_states_left --> SuddenDeath: no stages left
    border_states_left --> BorderDelay: at least one<br>stage left
    SuddenDeath --> RoundOver

    state rounds_left <<choice>>
    rounds_left --> GameOver: no rounds left
    GameOver --> [*]

    RoundOver --> rounds_left
    rounds_left --> PreRound: at least one<br>round left
```
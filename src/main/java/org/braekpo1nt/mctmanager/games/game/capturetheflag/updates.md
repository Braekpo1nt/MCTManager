
- add `durations.description` to the configs
    - add it to `ConfigDTO`
        ```java
        , int description
        ```
    - add it to `Config`
    - add it to `ConfigDTO.toConfig()` (and vice versa if applicable)
        ```java
        .descriptionDuration(this.durations.description)
        ```


- Add this to each game:

Fields:
```java
private int descriptionPeriodTaskId;
private boolean descriptionShowing = false;
```

Start method:
```java
public void start() {
    //...
    gameActive = true; // must come before this
    startDescriptionPeriod();
    //...
}
```

The method to run the actual task (replace `//actually start the game` with whatever belongs there):
```java
    private void startDescriptionPeriod() {
        descriptionShowing = true;
        this.descriptionPeriodTaskId = new BukkitRunnable() {
            private int count = config.getDescriptionDuration();
            @Override
            public void run() {
                if (count <= 0) {
                    sidebar.updateLine("timer", "");
                    adminSidebar.updateLine("timer", "");
                    descriptionShowing = false;
                    // actually start the game
                    this.cancel();
                    return;
                }
                String timeLeft = TimeStringUtils.getTimeString(count);
                String timerString = String.format("Starting soon: %s", timeLeft);
                sidebar.updateLine("timer", timerString);
                adminSidebar.updateLine("timer", timerString);
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }
```

Stop method:
```java
private void stop() {
    //...
    descriptionShowing = false;
    cancelAllTasks();
    //...
}
```

Make sure this is here:
```java
private void cancelAllTasks() {
    //...
    Bukkit.getScheduler().cancelTask(descriptionPeriodTaskId);
    //...
}
```


- make sure that all `@EventHandlers` and `onPlayerJoin/Quit()` handle this state appropriately
- make sure that the sidebars have a `"timer"` line

During testing:
- Participants can join during the description period and be added in as if they were there from the beginning
- Participants can leave during the description period and be removed as if they were never there
- Participants can leave then immediately join during the description period and still be added in as if they were there from the beginning


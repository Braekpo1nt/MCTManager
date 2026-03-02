package org.braekpo1nt.mctmanager.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeStringUtils {
    
    // TODO: move these to a different helper class
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private TimeStringUtils() {
        // do not instantiate
    }
    
    /**
     * Returns the given seconds as a string representing time in the format
     * MM:ss (or minutes:seconds)
     * @param timeSeconds The time in seconds
     * @return Time string MM:ss
     */
    public static String getTimeString(long timeSeconds) {
        Duration duration = Duration.ofSeconds(timeSeconds);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%d:%02d", minutes, seconds);
    }
    
    /**
     * Returns the given seconds as a Component representing time in the format
     * MM:ss (or minutes:seconds)
     * @param timeSeconds The time in seconds
     * @return Time Component MM:ss
     */
    public static Component getTimeComponent(long timeSeconds) {
        Duration duration = Duration.ofSeconds(timeSeconds);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return Component.empty()
                .append(Component.text(minutes))
                .append(Component.text(":"))
                .append(Component.text(String.format("%02d", seconds)));
    }
    
    /**
     * Returns the given milliseconds as a string representing time in the format
     * MM:ss:mmm (or minutes:seconds:milliseconds)
     * @param timeMillis The time in milliseconds
     * @return Time string MM:ss:mmm
     */
    public static Component getTimeComponentMillis(long timeMillis) {
        Duration duration = Duration.ofMillis(timeMillis);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        long millis = duration.minusMinutes(minutes).minusSeconds(seconds).toMillis();
        return Component.empty()
                .append(Component.text(minutes))
                .append(Component.text(":"))
                .append(Component.text(String.format("%02d", seconds)))
                .append(Component.text(":"))
                .append(Component.text(String.format("%03d", millis)));
    }
    
    public static TextColor getColorForTime(int seconds) {
        switch (seconds) {
            case 3 -> {
                return NamedTextColor.RED;
            }
            case 2 -> {
                return NamedTextColor.YELLOW;
            }
            case 1 -> {
                return NamedTextColor.GREEN;
            }
            default -> {
                return NamedTextColor.WHITE;
            }
        }
    }
    
    public static Date parseDate(String dateString) {
        LocalDate localDate = LocalDate.parse(dateString, DATE_FORMATTER);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}

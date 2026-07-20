package org.tasa.socs.moc.hdtrend.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TelemetryTimeParser {

    public static Instant parse(String time) {
        Instant instant = null;
        if (time.matches("\\d+\\.\\d+")) { //
            boolean done = false;
            if (time.length() == 17) {
                try {
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyDDDHHmmss.SSS");
                    LocalDateTime dateTime = LocalDateTime.parse(time, inputFormatter);
                    instant = dateTime.toInstant(ZoneOffset.UTC);
                    done = true;
                }
                catch(DateTimeParseException e) {
                    // ignore
                }
            }

            if (!done) {
                instant = Instant.ofEpochMilli(Math.round(Double.parseDouble(time)));
            }
        }
        else if (time.matches("\\d+")) {
            boolean done = false;
            if (time.length() == 7) {
                try {
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyDDD");
                    LocalDate date = LocalDate.parse(time, inputFormatter);
                    LocalDateTime dateTime = date.atTime(0, 0, 0);
                    instant = dateTime.toInstant(ZoneOffset.UTC);
                    done = true;
                }
                catch(DateTimeParseException e) {
                    // ignore
                }
            }
            else if (time.length() == 13) {
                try {
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyDDDHHmmss");
                    LocalDateTime dateTime = LocalDateTime.parse(time, inputFormatter);
                    instant = dateTime.toInstant(ZoneOffset.UTC);
                    done = true;
                }
                catch(DateTimeParseException e) {
                    // ignore
                }
            }

            if (!done) {
                instant = Instant.ofEpochMilli(Long.parseLong(time));
            }
        } else {
            instant = Instant.parse(time);
        }
        return instant;
    }

    public static Instant time(LocalDate date, int hour, int minute, int second, int nanoOfSecond) {
        LocalDateTime time = date.atTime(hour, minute, second, nanoOfSecond);
        return time.toInstant(ZoneOffset.UTC);
    }

    public static Instant time(LocalDate date, int hour, int minute, int second) {
        LocalDateTime time = date.atTime(hour, minute, second);
        return time.toInstant(ZoneOffset.UTC);
    }

    public static Instant time(int year, int month, int dayOfMonth) {
        return time(year, month, dayOfMonth, 0, 0);
    }

    public static Instant time(int year, int month, int dayOfMonth, int hour, int minute) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute).toInstant(ZoneOffset.UTC);
    }

    public static Instant time(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second).toInstant(ZoneOffset.UTC);
    }

}

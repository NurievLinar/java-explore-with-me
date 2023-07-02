package ru.practicum.ewm;

import java.time.format.DateTimeFormatter;

@lombok.experimental.UtilityClass
public class UtilityClass {
    public static final String pattern = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
}
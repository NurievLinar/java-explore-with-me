package ru.practicum.ewm.exception;

public class InvalidDateTimeException extends RuntimeException {
    public InvalidDateTimeException(final String message) {
        super(message);
    }
}

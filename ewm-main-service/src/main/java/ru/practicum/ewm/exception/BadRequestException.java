package ru.practicum.ewm.exception;

public class BadRequestException extends IllegalArgumentException {
    public BadRequestException(final String message) {
        super(message);
    }
}

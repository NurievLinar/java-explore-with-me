package ru.practicum.ewm.exception;

public class IncorrectStateException extends RuntimeException {
    public IncorrectStateException(final String message) {
        super(message);
    }
}

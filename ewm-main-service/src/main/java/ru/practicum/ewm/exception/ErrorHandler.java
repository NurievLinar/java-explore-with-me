package ru.practicum.ewm.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleIdNotFoundException(NotFoundException exception) {
        log.debug("Получен статус 404 Not found {}", exception.getMessage(), exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidDateTimeException(InvalidDateTimeException exception) {
        log.debug("Получен статус 400 CONFLICT {}", exception.getMessage(), exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleViolationException(DataIntegrityViolationException exception) {
        log.debug("Получен статус 409 CONFLICT {}", exception.getMessage(), exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIncorrectStateException(IncorrectStateException exception) {
        log.debug("Получен статус 409 CONFLICT {}", exception.getMessage(), exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(ConflictException exception) {
        log.debug("Получен статус 409 CONFLICT {}", exception.getMessage(), exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestException(BadRequestException exception) {
        log.debug("Получен статус 400 BAD_REQUEST {}", exception.getMessage(), exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnknownDataException(MethodArgumentNotValidException exception) {
        log.warn("400 {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRuntimeException(RuntimeException exception) {
        log.debug("Получен статус 500 INTERNAL_SERVER_ERROR {}", exception.getMessage(), exception);
        return new ErrorResponse(exception.getMessage());
    }
}

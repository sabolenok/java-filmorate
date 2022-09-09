package ru.yandex.practicum.filmorate.exception;

import javax.validation.ValidationException;

public class CustomValidationException extends ValidationException {
    public CustomValidationException(String message) {
        super(message);
    }
}

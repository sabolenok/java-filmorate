package ru.yandex.practicum.filmorate.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IncorrectParameterException extends RuntimeException {
    private final String parameter;

    public String getParameter() {
        return parameter;
    }
}

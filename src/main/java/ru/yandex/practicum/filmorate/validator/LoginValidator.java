package ru.yandex.practicum.filmorate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LoginValidator implements ConstraintValidator<ValidLogin, String> {
    public void initialize(ValidLogin constraint) {
    }

    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !value.contains(" ");
    }
}

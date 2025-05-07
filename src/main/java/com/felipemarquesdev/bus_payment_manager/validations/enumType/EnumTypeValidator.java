package com.felipemarquesdev.bus_payment_manager.validations.enumType;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumTypeValidator implements ConstraintValidator<EnumType, String> {

    private String enumName;
    private Enum<?>[] enumValues;

    @Override
    public void initialize(EnumType annotation) {
        enumName = annotation.enumClass().getSimpleName();
        enumValues = annotation.enumClass().getEnumConstants();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;

        for (Enum<?> e : enumValues) {
            if (e.name().equals(value)) {
                return true;
            }
        }

        String allowedValues = Arrays.stream(enumValues)
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                String.format(
                        "Unable to deserialize value '%s' into '%s' enum. The allowed values are: %s",
                        value,
                        enumName,
                        allowedValues
                )
        ).addConstraintViolation();

        return false;
    }
}

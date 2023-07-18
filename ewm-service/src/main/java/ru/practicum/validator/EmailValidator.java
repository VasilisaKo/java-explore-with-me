package ru.practicum.validator;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<EmailValidation, String> {
    @Override
    public boolean isValid(String email, ConstraintValidatorContext cxt) {
        if (email != null) {
            boolean checkDomain = false;
            String domain = StringUtils.substringAfter(email, "@");
            String[] parts = domain.split(Pattern.quote("."));
            for (String element : parts) {
                if (element.length() > 63) {
                    return false;
                } else {
                    checkDomain = true;
                }
            }

            String local = StringUtils.substringBefore(email, "@");

            return local.length() <= 64 && checkDomain && !email.isEmpty();
        } else {
            return false;
        }
    }
}

package clusteval;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.ArrayList;

public class NotBlankOrNullValidator implements ConstraintValidator<NotBlankOrNull, String> {
    public boolean isValid(String s, ConstraintValidatorContext context) {
        if (s == null) {
            return true;
        }
        return s.trim().length() > 0;
    }

    @Override
    public void initialize(NotBlankOrNull constraint) {

    }
}

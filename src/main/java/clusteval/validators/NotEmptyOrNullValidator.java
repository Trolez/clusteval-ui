package clusteval;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.ArrayList;

public class NotEmptyOrNullValidator implements ConstraintValidator<NotBlankOrNull, ArrayList<String>> {
    @Override
    public boolean isValid(ArrayList<String> s, ConstraintValidatorContext context) {
        if (s.size() > 0 && s.get(0).equals("DUMMY")) {
            return true;
        }

        return s.size() > 0;
    }

    @Override
    public void initialize(NotBlankOrNull constraint) {

    }
}

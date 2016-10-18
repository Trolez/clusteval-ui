package clusteval;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

public class HasOneFileValidator implements ConstraintValidator<HasOneFile, MultipartFile> {
    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
        //Ignore null values like the most other validators
        if (value == null) return true;

        return (value.getSize() > 0);
    }

    @Override
    public void initialize(HasOneFile constraint) {

    }
}

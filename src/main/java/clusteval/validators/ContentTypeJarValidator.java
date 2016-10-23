package clusteval;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.web.multipart.MultipartFile;

public class ContentTypeJarValidator implements ConstraintValidator<ContentTypeJar, MultipartFile> {
    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
        //Ignore null values like the most other validators
        if (value == null) return true;
        
        return (value.getContentType().equals("application/x-java-archive") ||
                value.getContentType().equals("application/java-archive"));
    }

    @Override
    public void initialize(ContentTypeJar constraint) {

    }
}

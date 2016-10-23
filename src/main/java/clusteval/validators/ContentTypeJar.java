package clusteval;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = { ContentTypeJarValidator.class })
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContentTypeJar {
    String message() default "{org.hibernate.contraints.ContentTypeJar.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}

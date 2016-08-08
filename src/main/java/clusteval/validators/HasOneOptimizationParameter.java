package clusteval;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = { HasOneOptimizationParameterValidator.class })
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasOneOptimizationParameter {
    String message() default "{org.hibernate.contraints.HasOneOptimizationParameter.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}

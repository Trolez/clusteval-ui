package clusteval;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.ArrayList;

public class HasOneOptimizationParameterValidator implements ConstraintValidator<HasOneOptimizationParameter, ArrayList<ProgramParameter>> {
    @Override
    public boolean isValid(ArrayList<ProgramParameter> parameters, ConstraintValidatorContext context) {
        if (parameters == null) {
            return true;
        }
        
        for (ProgramParameter programParameter : parameters) {
            if (programParameter.getOptimize()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void initialize(HasOneOptimizationParameter constraint) {

    }
}

package abstractexpressions.annotations;

import abstractexpressions.matrixexpression.classes.TypeMatrixOperator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SimplifyMatrixOperator {
    
    public TypeMatrixOperator type();
    
}

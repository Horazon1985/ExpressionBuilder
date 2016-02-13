package abstractexpressions.annotations;

import abstractexpressions.expression.classes.TypeOperator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SimplifyOperator {
    
    public TypeOperator type();
    
}

package integrationmethods;

import expressionbuilder.Expression;
import java.util.ArrayList;

public abstract class RischAlgorithmMethods {
    
    private boolean containsEquivalent(ArrayList<Expression> fieldExtensions, Expression f){
        for (Expression fieldExtension : fieldExtensions){
            if (f.equivalent(fieldExtension)){
                return true;
            }
        }
        return false;
    }
    
    private boolean isAlgebraicOverDifferentialField(Expression f, String var, ArrayList<Expression> fieldExtensions){
    
        
    
        
        
        
        
    
        return true;
    
    }
    
    
    
}

package exceptions;

public class DifferentialEquationNotAlgebraicallyIntegrableException extends MathToolException {
    
    private static final String DIFFEQUATION_NOT_SOLVABLE_MESSAGE = "Differential equation is not algebraically integrable.";
    
    public DifferentialEquationNotAlgebraicallyIntegrableException() {
        super(DIFFEQUATION_NOT_SOLVABLE_MESSAGE);
    }
    
    public DifferentialEquationNotAlgebraicallyIntegrableException(String s) {
        super(s);
    }
    
    
    
}

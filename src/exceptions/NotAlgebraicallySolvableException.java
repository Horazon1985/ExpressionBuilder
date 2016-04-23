package exceptions;

public class NotAlgebraicallySolvableException extends MathToolException {

    private static final String NOT_ALGEBRAICALLY_SOLVABLE_MESSAGE = "Equation is not algebraically solvable.";
    
    public NotAlgebraicallySolvableException() {
        super(NOT_ALGEBRAICALLY_SOLVABLE_MESSAGE);
    }
    
    public NotAlgebraicallySolvableException(String s) {
        super(s);
    }
    
}

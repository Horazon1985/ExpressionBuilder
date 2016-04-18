package exceptions;

public class NotAlgebraicallyIntegrableException extends MathToolException {

    private static final String NOT_ALGEBRAICALLY_INTEGRABLE_MESSAGE = "Function is not algebraically integrable.";
    
    public NotAlgebraicallyIntegrableException() {
        super(NOT_ALGEBRAICALLY_INTEGRABLE_MESSAGE);
    }
    
    public NotAlgebraicallyIntegrableException(String s) {
        super(s);
    }
    
}

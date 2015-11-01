package exceptions;

public class NotPreciseIntegrableException extends MathToolException {

    private static final String NOT_INTEGRABLE_MESSAGE = "Function is not algebraically integrable.";
    
    public NotPreciseIntegrableException() {
        super(NOT_INTEGRABLE_MESSAGE);
    }
    
    public NotPreciseIntegrableException(String s) {
        super(s);
    }
    
}

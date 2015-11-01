package exceptions;

public class NotSubstitutableException extends MathToolException {

    private static final String NOT_SUBSTITUTABLE_MESSAGE = "Expression is not algebraically substitutable.";

    public NotSubstitutableException() {
        super(NOT_SUBSTITUTABLE_MESSAGE);
    }

    public NotSubstitutableException(String s) {
        super(s);
    }

}

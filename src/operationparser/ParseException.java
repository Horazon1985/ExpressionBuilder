package operationparser;

public class ParseException extends RuntimeException {

    private static String parseExceptionMessage = "Pattern could not be parsed.";
    private static String parseExceptionMessageWithParameterIndex = "Pattern could not be parsed. Incorrect parameter: ";

    public ParseException() {
        super(parseExceptionMessage);
    }

    public ParseException(int i) {
        super(parseExceptionMessageWithParameterIndex + (i + 1));
    }

}

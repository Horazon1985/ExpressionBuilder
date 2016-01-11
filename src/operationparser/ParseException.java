package operationparser;

public class ParseException extends RuntimeException {
    
    private static String parseExceptionMessage = "Pattern could not be parsed.";
    
    public ParseException(){
        super(parseExceptionMessage);
    }
    
}

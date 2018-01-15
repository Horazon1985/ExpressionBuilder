package util;

import exceptions.ExpressionException;
import java.util.ArrayList;
import java.util.List;
import lang.translator.Translator;

public final class OperationParsingUtils {

    private static final String OU_EXPRESSION_EMPTY_OR_INCOMPLETE = "OU_EXPRESSION_EMPTY_OR_INCOMPLETE";
    private static final String OU_IS_NOT_VALID_COMMAND = "OU_IS_NOT_VALID_COMMAND";
    private static final String OU_MISSING_CLOSING_BRACKET = "OU_MISSING_CLOSING_BRACKET";
    private static final String OU_EMPTY_PARAMETER = "OU_EMPTY_PARAMETER";
    private static final String OU_WRONG_BRACKETS = "OU_WRONG_BRACKETS";
    
    private OperationParsingUtils() {
    }
    
    public static OperationDataTO getOperationData(String input) throws ExpressionException {
        String[] opNameAndParams = getOperationAndArguments(input);
        String[] arguments = getArguments(opNameAndParams[1]);
        return new OperationDataTO(opNameAndParams[0], arguments);
    }
    
    /**
     * Der jeweilige Befehl und die Parameter in der Befehlsklammer werden
     * ausgelesen und zurückgegeben.<br>
     * BEISPIEL: commandLine = f(x, y, z). Zurückgegeben wird ein array der
     * Länge zwei: im 0. Eintrag steht der String "f", im 1. der String "x, y,
     * z". Wichtige Bedingung ist, dass nur solche Operationen geparst werden,
     * die mindestens ein Argument enthalten.
     *
     * @throws ExpressionException
     */
    private static String[] getOperationAndArguments(String input) throws ExpressionException {
        // Leerzeichen beseitigen
        input = input.replaceAll(" ", "");
        String[] result = new String[2];
        int i = input.indexOf("(");
        if (i == -1) {
            // Um zu verhindern, dass es eine IndexOutOfBoundsException gibt.
            i = 0;
        }
        result[0] = input.substring(0, i);
        // Wenn der Befehl leer ist -> Fehler.
        if (result[0].length() == 0) {
            throw new ExpressionException(Translator.translateOutputMessage(OU_EXPRESSION_EMPTY_OR_INCOMPLETE));
        }
        // Wenn length(result[0]) > l - 2 -> Fehler (der Befehl besitzt NICHT die Form command(...)).
        if (result[0].length() > input.length() - 2) {
            throw new ExpressionException(input + Translator.translateOutputMessage(OU_IS_NOT_VALID_COMMAND));
        }
        // Wenn am Ende nicht ")" steht.
        if (!input.substring(input.length() - 1, input.length()).equals(")")) {
            throw new ExpressionException(Translator.translateOutputMessage(OU_MISSING_CLOSING_BRACKET, input));
        }
        result[1] = input.substring(result[0].length() + 1, input.length() - 1);
        return result;
    }

    /**
     * Gibt ein String-Array zurück, in dem die einzelnen Parameter, welche in
     * input enthalten und durch ein Komma getrennt sind, stehen. Nach einem
     * eingelesenen Komma, welches NICHT von runden Klammern umgeben ist, werden
     * die Parameter getrennt.<br>
     * VORAUSSETZUNG: im String input stehen NUR die Parameter, getrennt durch
     * ein Komma.<br>
     * BEISPIEL input = "x,y,f(w,z),u,v". Die Paremeter sind dann {x, y, f(w,
     * z), u, v}.
     *
     * @throws ExpressionException
     */
    private static String[] getArguments(String input) throws ExpressionException {
        //Leerzeichen beseitigen
        input = input.replaceAll(" ", "");
        //Falls Parameterstring leer ist -> Fertig
        if (input.isEmpty()) {
            return new String[0];
        }
        List<String> resultParameters = new ArrayList<>();
        int startPositionOfCurrentParameter = 0;
        /*
        Differenz zwischen der Anzahl der öffnenden und der der schließenden
        Klammern (bracketCounter == 0 am Ende -> alles ok).
         */
        int bracketCounter = 0;
        int squareBracketCounter = 0;
        String currentChar;
        //Jetzt werden die einzelnen Parameter ausgelesen
        for (int i = 0; i < input.length(); i++) {
            currentChar = input.substring(i, i + 1);
            if (currentChar.equals("(")) {
                bracketCounter++;
            }
            if (currentChar.equals(")")) {
                bracketCounter--;
            }
            if (currentChar.equals("[")) {
                squareBracketCounter++;
            }
            if (currentChar.equals("]")) {
                squareBracketCounter--;
            }
            if (bracketCounter == 0 && squareBracketCounter == 0 && currentChar.equals(",")) {
                if (input.substring(startPositionOfCurrentParameter, i).isEmpty()) {
                    throw new ExpressionException(Translator.translateOutputMessage(OU_EMPTY_PARAMETER));
                }
                resultParameters.add(input.substring(startPositionOfCurrentParameter, i));
                startPositionOfCurrentParameter = i + 1;
            }
            if (i == input.length() - 1) {
                if (startPositionOfCurrentParameter == input.length()) {
                    throw new ExpressionException(Translator.translateOutputMessage(OU_EMPTY_PARAMETER));
                }
                resultParameters.add(input.substring(startPositionOfCurrentParameter, input.length()));
            }
        }
        if (bracketCounter != 0 || squareBracketCounter != 0) {
            throw new ExpressionException(Translator.translateOutputMessage(OU_WRONG_BRACKETS));
        }
        return resultParameters.toArray(new String[resultParameters.size()]);
    }
        
}

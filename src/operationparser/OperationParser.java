package operationparser;

import exceptions.ExpressionException;
import expressionbuilder.Operator;
import java.util.ArrayList;

public abstract class OperationParser {

    /**
     * Der jeweilige Befehl und die Parameter in der Befehlsklammer werden
     * ausgelesen und zurückgegeben.<br>
     * BEISPIEL: commandLine = f(x, y, z). Zurückgegeben wird ein array der
     * Länge zwei: im 0. Eintrag steht der String "f", im 1. der String "x, y,
     * z".
     *
     * @throws ExpressionException
     */
    private static String[] getOperationAndArguments(String input) {

        // Leerzeichen beseitigen
        input = input.replaceAll(" ", "");

        String[] result = new String[2];
        // Das Math.max() dient dazu, um zu verhindern, dass eine IndexOutOfBoundsException geworfen wird.
        result[0] = input.substring(0, Math.max(0, input.indexOf("(")));

        //Wenn der Befehl leer ist -> Fehler.
        if (result[0].length() == 0) {
            throw new ParseException();
        }

        //Wenn length(result[0]) > l - 2 -> Fehler (der Befehl besitzt NICHT die Form command(...)).
        if (result[0].length() > input.length() - 2) {
            throw new ParseException();
        }

        //Wenn am Ende nicht ")" steht.
        if (!input.substring(input.length() - 1, input.length()).equals(")")) {
            throw new ParseException();
        }

        result[1] = input.substring(result[0].length() + 1, input.length() - 1);

        return result;

    }

    /**
     * Input: String input, in der NUR die Parameter (getrennt durch ein Komma)
     * stehen. Beispiel commandLine == "x,y,f(w,z),u,v" -> Paremeter sind dann
     * {x, y, f(w, z), u, v}. Nach einem eingelesenen Komma, welches NICHT von
     * runden Klammern umgeben ist, werden die Parameter getrennt.
     *
     * @throws ExpressionException
     */
    private static String[] getArguments(String input) {

        //Leerzeichen beseitigen
        input = input.replaceAll(" ", "");

        //Falls Parameterstring leer ist -> Fertig
        if (input.isEmpty()) {
            return new String[0];
        }

        ArrayList<String> resultParameters = new ArrayList<>();
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
                    throw new ParseException();
                }
                resultParameters.add(input.substring(startPositionOfCurrentParameter, i));
                startPositionOfCurrentParameter = i + 1;
            }
            if (i == input.length() - 1) {
                if (startPositionOfCurrentParameter == input.length()) {
                    throw new ParseException();
                }
                resultParameters.add(input.substring(startPositionOfCurrentParameter, input.length()));
            }

        }

        if (bracketCounter != 0 || squareBracketCounter != 0) {
            throw new ParseException();
        }

        String[] resultParametersAsArray = new String[resultParameters.size()];
        for (int i = 0; i < resultParameters.size(); i++) {
            resultParametersAsArray[i] = resultParameters.get(i);
        }

        return resultParametersAsArray;

    }

    public static ParseResultPattern getResultPattern(String pattern) {

        String[] opAndArgs = getOperationAndArguments(pattern);
        // Operationsname.
        String opName = opAndArgs[0];
        // Operationsparameter.
        String[] args = getArguments(opAndArgs[1]);

        ArrayList<ParameterPattern> paramPatterns = new ArrayList<>();
        ParameterPattern[] paramPattern = new ParameterPattern[args.length];

        String[] paramTypeAndRestrictions;
        String paramType;
        String[] restrictions;
        ArrayList<String> restrictionsAsList = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {

            restrictionsAsList.clear();

            if (args[i].contains(ParameterPattern.var)) {

                if (args[i].equals(ParameterPattern.var)) {
                    paramPattern[i] = new ParameterPattern(ParameterPattern.ParamType.var, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    if (!paramType.equals(ParameterPattern.var)) {
                        throw new ParseException();
                    }
                    if (restrictions.length == 0) {
                        throw new ParseException();
                    }

                    // Optionale Parameter einlesen (es muss mindestens einer sein).
                    for (String restriction : restrictions) {
                        int index = -1;
                        try {
                        } catch (NumberFormatException e) {
                            throw new ParseException();
                        }
                        if (restriction.indexOf(ParameterPattern.notin) == 0) {
                            try {
                                index = Integer.parseInt(restriction.substring(1));
                            } catch (NumberFormatException e) {
                                throw new ParseException();
                            }
                        } else {
                            try {
                                index = Integer.parseInt(restriction);
                            } catch (NumberFormatException e) {
                                throw new ParseException();
                            }
                        }
                        if (index < 0 || index > args.length) {
                            throw new ParseException();
                        }
                        /* 
                         Restriktionen auf enthalten / nicht enthalten in sollen nur 
                         Parameter betreffen, die auch Variablen enthalten können.
                         */
                        if (!args[index].contains(ParameterPattern.equation)
                                && !args[index].contains(ParameterPattern.expr)
                                && !args[index].contains(ParameterPattern.logexpr)
                                && !args[index].contains(ParameterPattern.matexpr)) {
                            throw new ParseException();
                        }
                        restrictionsAsList.add(restriction);
                    }
                    paramPattern[i] = new ParameterPattern(ParameterPattern.ParamType.var, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.equation)) {

                if (args[i].equals(ParameterPattern.equation)) {
                    paramPattern[i] = new ParameterPattern(ParameterPattern.ParamType.equation, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    if (!paramType.equals(ParameterPattern.equation)) {
                        throw new ParseException();
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions);
                    paramPattern[i] = new ParameterPattern(ParameterPattern.ParamType.logexpr, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.expr)) {

                if (args[i].equals(ParameterPattern.expr)) {
                    paramPattern[i] = new ParameterPattern(ParameterPattern.ParamType.expr, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    if (!paramType.equals(ParameterPattern.expr)) {
                        throw new ParseException();
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions);
                    paramPattern[i] = new ParameterPattern(ParameterPattern.ParamType.matexpr, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.integer)) {

                if (args[i].equals(ParameterPattern.integer)) {
                    paramPattern[i] = new ParameterPattern(ParameterPattern.ParamType.integer, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    if (!paramType.equals(ParameterPattern.integer)) {
                        throw new ParseException();
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions);
                    paramPattern[i] = new ParameterPattern(ParameterPattern.ParamType.matexpr, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.logexpr)) {

                if (args[i].equals(ParameterPattern.logexpr)) {
                    paramPattern[i] = new ParameterPattern(ParameterPattern.ParamType.logexpr, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    if (!paramType.equals(ParameterPattern.logexpr)) {
                        throw new ParseException();
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions);
                    paramPattern[i] = new ParameterPattern(ParameterPattern.ParamType.matexpr, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.matexpr)) {

                if (args[i].equals(ParameterPattern.matexpr)) {
                    paramPattern[i] = new ParameterPattern(ParameterPattern.ParamType.matexpr, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    if (!paramType.equals(ParameterPattern.matexpr)) {
                        throw new ParseException();
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions);
                    paramPattern[i] = new ParameterPattern(ParameterPattern.ParamType.matexpr, restrictionsAsList);
                }

            } else {
                throw new ParseException();
            }
            paramPatterns.add(paramPattern[i]);

        }

        return new ParseResultPattern(opName, paramPatterns);

    }

    private static ArrayList<String> getRestrictionList(String[] restrictions) {

        ArrayList<String> restrictionsAsList = new ArrayList<>();
        
        if (restrictions.length != 2) {
            throw new ParseException();
        }

        // Optionale Parameter einlesen (es müssen genau zwei sein).
        for (String restriction : restrictions) {
            if (restriction.equals(ParameterPattern.none)) {
                restrictionsAsList.add(ParameterPattern.none);
            } else {
                try {
                    Integer.parseInt(restriction);
                    restrictionsAsList.add(restriction);
                } catch (NumberFormatException e) {
                    throw new ParseException();
                }
            }
        }
        
        return restrictionsAsList;

    }

    /**
     * Parsen mathematischer Standardoperatoren.
     */
    public static Operator parseDefaultOperator(String operator, String pattern) {

        return null;

    }

}

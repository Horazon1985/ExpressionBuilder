package operationparser;

import exceptions.ExpressionException;
import expressionbuilder.Expression;
import expressionbuilder.Operator;
import static expressionbuilder.Operator.getTypeFromName;
import expressionbuilder.TypeOperator;
import java.util.ArrayList;
import java.util.HashSet;
import logicalexpressionbuilder.LogicalExpression;
import matrixexpressionbuilder.MatrixExpression;
import operationparser.ParameterPattern.Multiplicity;
import operationparser.ParameterPattern.ParamType;
import translator.Translator;

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
        Multiplicity m;

        for (int i = 0; i < args.length; i++) {

            restrictionsAsList.clear();

            // Multiplizität bestimmen.
            if (args[i].substring(args[i].length() - 1).equals(ParameterPattern.multPlus)) {
                // "+" herausschneiden.
                args[i] = args[i].substring(0, args[i].length() - 1);
                m = Multiplicity.plus;
            } else {
                m = Multiplicity.one;
            }

            if (args[i].contains(ParameterPattern.var)) {

                if (args[i].equals(ParameterPattern.var)) {
                    paramPattern[i] = new ParameterPattern(ParamType.var, m, restrictionsAsList);
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
                    paramPattern[i] = new ParameterPattern(ParamType.var, m, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.equation)) {

                if (args[i].equals(ParameterPattern.equation)) {
                    paramPattern[i] = new ParameterPattern(ParamType.equation, m, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    if (!paramType.equals(ParameterPattern.equation)) {
                        throw new ParseException();
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions);
                    paramPattern[i] = new ParameterPattern(ParamType.logexpr, m, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.expr)) {

                if (args[i].equals(ParameterPattern.expr)) {
                    paramPattern[i] = new ParameterPattern(ParamType.expr, m, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    if (!paramType.equals(ParameterPattern.expr)) {
                        throw new ParseException();
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions);
                    paramPattern[i] = new ParameterPattern(ParamType.matexpr, m, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.integer)) {

                if (args[i].equals(ParameterPattern.integer)) {
                    paramPattern[i] = new ParameterPattern(ParamType.integer, m, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    if (!paramType.equals(ParameterPattern.integer)) {
                        throw new ParseException();
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions);
                    paramPattern[i] = new ParameterPattern(ParamType.matexpr, m, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.logexpr)) {

                if (args[i].equals(ParameterPattern.logexpr)) {
                    paramPattern[i] = new ParameterPattern(ParamType.logexpr, m, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    if (!paramType.equals(ParameterPattern.logexpr)) {
                        throw new ParseException();
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions);
                    paramPattern[i] = new ParameterPattern(ParamType.matexpr, m, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.matexpr)) {

                if (args[i].equals(ParameterPattern.matexpr)) {
                    paramPattern[i] = new ParameterPattern(ParamType.matexpr, m, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    if (!paramType.equals(ParameterPattern.matexpr)) {
                        throw new ParseException();
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions);
                    paramPattern[i] = new ParameterPattern(ParamType.matexpr, m, restrictionsAsList);
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
    public static Operator parseDefaultOperator(String operator, HashSet<String> vars, String pattern) throws ExpressionException {

        String[] operatorAndArgumentss = getOperationAndArguments(operator);
        // Operationsname.
        String operatorName = operatorAndArgumentss[0];
        // Operationsparameter.
        String[] arguments = getArguments(operatorAndArgumentss[1]);
        // Operatortyp.
        TypeOperator type = getTypeFromName(operatorName);

        // Muster für das Parsen des Operators.
        ParseResultPattern resultPattern = getResultPattern(pattern);

        Object[] params = new Object[arguments.length];

        int indexInOperatorArguments = 0;
        ParameterPattern p;
        ArrayList<String> restrictions;
        ArrayList<Integer> indices = new ArrayList<>();

        // Zunächst nur reines Parsen, OHNE die Einschränkungen für die Variablen zu beachten.
        for (int i = 0; i < resultPattern.size(); i++) {

            // Indizes loggen!
            p = resultPattern.getParameterPattern(i);
            restrictions = p.getRestrictions();

            if (p.getMultiplicity().equals(Multiplicity.one)) {
                params[i] = getOperatorParameter(arguments[i], vars, p.getParamType(), restrictions, i);
                indexInOperatorArguments++;
            } else {
                
            }

        }

        // Jetzt müssen noch einmal die Einschränkungen für die Variablen kontrolliert werden.
        return new Operator(type, params);

    }

    private static Object getOperatorParameter(String parameter, HashSet<String> vars, ParamType type, ArrayList<String> restrictions, int index) throws ExpressionException {

        HashSet<String> containedVars = new HashSet<>();

        if (type.equals(ParamType.var)) {

            if (Expression.isValidVariable(parameter)) {
                return parameter;
            }

        } else if (type.equals(ParamType.equation)) {

            try {
                if (!parameter.contains("=")) {
                    throw new ExpressionException(Translator.translateExceptionMessage(""));
                }
                Expression exprLeft = Expression.build(parameter.substring(0, parameter.indexOf("=")), vars);
                Expression exprRight = Expression.build(parameter.substring(parameter.indexOf("=")), vars);
                exprLeft.addContainedVars(containedVars);
                exprRight.addContainedVars(containedVars);
                if (!restrictions.isEmpty()) {
                    exprLeft.addContainedVars(containedVars);
                    if (restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        return exprLeft;
                    } else if (restrictions.get(0).equals(ParameterPattern.none) && !restrictions.get(1).equals(ParameterPattern.none)) {
                        if (containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                            return exprLeft;
                        }
                    } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))) {
                            return exprLeft;
                        }
                    } else {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))
                                && containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                            return exprLeft;
                        }
                    }
                }
            } catch (ExpressionException e) {
            }

        } else if (type.equals(ParamType.expr)) {

            try {
                Expression expr = Expression.build(parameter, vars);
                if (!restrictions.isEmpty()) {
                    expr.addContainedVars(containedVars);
                    if (restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        return expr;
                    } else if (restrictions.get(0).equals(ParameterPattern.none) && !restrictions.get(1).equals(ParameterPattern.none)) {
                        if (containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                            return expr;
                        }
                    } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))) {
                            return expr;
                        }
                    } else {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))
                                && containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                            return expr;
                        }
                    }
                }
            } catch (ExpressionException e) {
            }

        } else if (type.equals(ParamType.integer)) {

            try {
                int n = Integer.parseInt(parameter);
                if (!restrictions.isEmpty()) {
                    if (restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        return n;
                    } else if (restrictions.get(0).equals(ParameterPattern.none) && !restrictions.get(1).equals(ParameterPattern.none)) {
                        if (n <= Integer.parseInt(restrictions.get(1))) {
                            return n;
                        }
                    } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        if (n >= Integer.parseInt(restrictions.get(0))) {
                            return n;
                        }
                    } else {
                        if (n >= Integer.parseInt(restrictions.get(0)) && n <= Integer.parseInt(restrictions.get(1))) {
                            return n;
                        }
                    }
                }
            } catch (NumberFormatException e) {
            }

        } else if (type.equals(ParamType.logexpr)) {

            try {
                LogicalExpression expr = LogicalExpression.build(parameter, vars);
                if (!restrictions.isEmpty()) {
                    expr.addContainedVars(containedVars);
                    if (restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        return expr;
                    } else if (restrictions.get(0).equals(ParameterPattern.none) && !restrictions.get(1).equals(ParameterPattern.none)) {
                        if (containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                            return expr;
                        }
                    } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))) {
                            return expr;
                        }
                    } else {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))
                                && containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                            return expr;
                        }
                    }
                }
            } catch (ExpressionException e) {
            }

        } else if (type.equals(ParamType.matexpr)) {

            try {
                MatrixExpression expr = MatrixExpression.build(parameter, vars);
                if (!restrictions.isEmpty()) {
                    expr.addContainedVars(containedVars);
                    if (restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        return expr;
                    } else if (restrictions.get(0).equals(ParameterPattern.none) && !restrictions.get(1).equals(ParameterPattern.none)) {
                        if (containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                            return expr;
                        }
                    } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))) {
                            return expr;
                        }
                    } else {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))
                                && containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                            return expr;
                        }
                    }
                }
            } catch (ExpressionException e) {
            }

        }

        throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR"));

    }

}

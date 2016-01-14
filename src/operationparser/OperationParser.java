package operationparser;

import exceptions.ExpressionException;
import expressionInterfaces.AbstractExpression;
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

            // Argument im Pattern darf nicht leer sein.
            if (args[i].length() == 0) {
                throw new ParseException(i);
            }

            restrictionsAsList.clear();

            if (args[i].contains(ParameterPattern.var)) {

                if (args[i].equals(ParameterPattern.var)) {
                    paramPattern[i] = new ParameterPattern(ParamType.var, Multiplicity.one, restrictionsAsList);
                } else if (args[i].substring(0, args[i].length() - 1).equals(ParameterPattern.var) && args[i].substring(args[i].length() - 1).equals(ParameterPattern.multPlus)) {
                    paramPattern[i] = new ParameterPattern(ParamType.var, Multiplicity.plus, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    // Multiplizität bestimmen.
                    if (paramType.substring(paramType.length() - 1).equals(ParameterPattern.multPlus)) {
                        // "+" herausschneiden.
                        paramType = paramType.substring(0, paramType.length() - 1);
                        m = Multiplicity.plus;
                    } else {
                        m = Multiplicity.one;
                    }

                    if (!paramType.equals(ParameterPattern.var)) {
                        throw new ParseException(i);
                    }
                    if (restrictions.length == 0) {
                        throw new ParseException(i);
                    }

                    // Optionale Parameter einlesen (es muss mindestens einer sein).
                    for (String restriction : restrictions) {
                        int index = -1;
                        try {
                        } catch (NumberFormatException e) {
                            throw new ParseException(i);
                        }
                        if (restriction.indexOf(ParameterPattern.notin) == 0) {
                            try {
                                index = Integer.parseInt(restriction.substring(1));
                            } catch (NumberFormatException e) {
                                throw new ParseException(i);
                            }
                        } else {
                            try {
                                index = Integer.parseInt(restriction);
                            } catch (NumberFormatException e) {
                                throw new ParseException(i);
                            }
                        }
                        if (index < 0 || index > args.length) {
                            throw new ParseException(i);
                        }
                        /* 
                         Restriktionen auf enthalten / nicht enthalten in sollen nur 
                         Parameter betreffen, die auch Variablen enthalten können.
                         */
                        if (!args[index].contains(ParameterPattern.equation)
                                && !args[index].contains(ParameterPattern.expr)
                                && !args[index].contains(ParameterPattern.logexpr)
                                && !args[index].contains(ParameterPattern.matexpr)) {
                            throw new ParseException(i);
                        }
                        restrictionsAsList.add(restriction);
                    }
                    paramPattern[i] = new ParameterPattern(ParamType.var, m, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.equation)) {

                if (args[i].equals(ParameterPattern.equation)) {
                    paramPattern[i] = new ParameterPattern(ParamType.equation, Multiplicity.one, restrictionsAsList);
                } else if (args[i].substring(0, args[i].length() - 1).equals(ParameterPattern.equation) && args[i].substring(args[i].length() - 1).equals(ParameterPattern.multPlus)) {
                    paramPattern[i] = new ParameterPattern(ParamType.equation, Multiplicity.plus, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    // Multiplizität bestimmen.
                    if (paramType.substring(paramType.length() - 1).equals(ParameterPattern.multPlus)) {
                        // "+" herausschneiden.
                        paramType = paramType.substring(0, paramType.length() - 1);
                        m = Multiplicity.plus;
                    } else {
                        m = Multiplicity.one;
                    }

                    if (!paramType.equals(ParameterPattern.equation)) {
                        throw new ParseException(i);
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions, i);
                    paramPattern[i] = new ParameterPattern(ParamType.equation, m, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.expr)) {

                if (args[i].equals(ParameterPattern.expr)) {
                    paramPattern[i] = new ParameterPattern(ParamType.expr, Multiplicity.one, restrictionsAsList);
                } else if (args[i].substring(0, args[i].length() - 1).equals(ParameterPattern.expr) && args[i].substring(args[i].length() - 1).equals(ParameterPattern.multPlus)) {
                    paramPattern[i] = new ParameterPattern(ParamType.expr, Multiplicity.plus, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    // Multiplizität bestimmen.
                    if (paramType.substring(paramType.length() - 1).equals(ParameterPattern.multPlus)) {
                        // "+" herausschneiden.
                        paramType = paramType.substring(0, paramType.length() - 1);
                        m = Multiplicity.plus;
                    } else {
                        m = Multiplicity.one;
                    }

                    if (!paramType.equals(ParameterPattern.expr)) {
                        throw new ParseException(i);
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions, i);
                    paramPattern[i] = new ParameterPattern(ParamType.expr, m, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.integer)) {

                if (args[i].equals(ParameterPattern.integer)) {
                    paramPattern[i] = new ParameterPattern(ParamType.integer, Multiplicity.one, restrictionsAsList);
                } else if (args[i].substring(0, args[i].length() - 1).equals(ParameterPattern.integer) && args[i].substring(args[i].length() - 1).equals(ParameterPattern.multPlus)) {
                    paramPattern[i] = new ParameterPattern(ParamType.integer, Multiplicity.plus, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    // Multiplizität bestimmen.
                    if (paramType.substring(paramType.length() - 1).equals(ParameterPattern.multPlus)) {
                        // "+" herausschneiden.
                        paramType = paramType.substring(0, paramType.length() - 1);
                        m = Multiplicity.plus;
                    } else {
                        m = Multiplicity.one;
                    }

                    if (!paramType.equals(ParameterPattern.integer)) {
                        throw new ParseException(i);
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions, i);
                    paramPattern[i] = new ParameterPattern(ParamType.integer, m, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.logexpr)) {

                if (args[i].equals(ParameterPattern.logexpr)) {
                    paramPattern[i] = new ParameterPattern(ParamType.logexpr, Multiplicity.one, restrictionsAsList);
                } else if (args[i].substring(0, args[i].length() - 1).equals(ParameterPattern.logexpr) && args[i].substring(args[i].length() - 1).equals(ParameterPattern.multPlus)) {
                    paramPattern[i] = new ParameterPattern(ParamType.logexpr, Multiplicity.plus, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    // Multiplizität bestimmen.
                    if (paramType.substring(paramType.length() - 1).equals(ParameterPattern.multPlus)) {
                        // "+" herausschneiden.
                        paramType = paramType.substring(0, paramType.length() - 1);
                        m = Multiplicity.plus;
                    } else {
                        m = Multiplicity.one;
                    }

                    if (!paramType.equals(ParameterPattern.logexpr)) {
                        throw new ParseException(i);
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions, i);
                    paramPattern[i] = new ParameterPattern(ParamType.logexpr, m, restrictionsAsList);
                }

            } else if (args[i].contains(ParameterPattern.matexpr)) {

                if (args[i].equals(ParameterPattern.matexpr)) {
                    paramPattern[i] = new ParameterPattern(ParamType.matexpr, Multiplicity.one, restrictionsAsList);
                } else if (args[i].substring(0, args[i].length() - 1).equals(ParameterPattern.matexpr) && args[i].substring(args[i].length() - 1).equals(ParameterPattern.multPlus)) {
                    paramPattern[i] = new ParameterPattern(ParamType.matexpr, Multiplicity.plus, restrictionsAsList);
                } else {
                    paramTypeAndRestrictions = getOperationAndArguments(args[i]);
                    paramType = paramTypeAndRestrictions[0];
                    restrictions = getArguments(paramTypeAndRestrictions[1]);

                    // Multiplizität bestimmen.
                    if (paramType.substring(paramType.length() - 1).equals(ParameterPattern.multPlus)) {
                        // "+" herausschneiden.
                        paramType = paramType.substring(0, paramType.length() - 1);
                        m = Multiplicity.plus;
                    } else {
                        m = Multiplicity.one;
                    }

                    if (!paramType.equals(ParameterPattern.matexpr)) {
                        throw new ParseException(i);
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions, i);
                    paramPattern[i] = new ParameterPattern(ParamType.matexpr, m, restrictionsAsList);
                }

            } else {
                throw new ParseException(i);
            }
            paramPatterns.add(paramPattern[i]);

        }

        return new ParseResultPattern(opName, paramPatterns);

    }

    private static ArrayList<String> getRestrictionList(String[] restrictions, int index) {

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
                    throw new ParseException(index);
                }
            }
        }

        return restrictionsAsList;

    }

    /**
     * Parsen mathematischer Standardoperatoren.
     */
    public static Operator parseDefaultOperator(String operator, HashSet<String> vars, String pattern) throws ExpressionException {

        String[] operatorAndArguments = getOperationAndArguments(operator);
        // Operationsname.
        String operatorName = operatorAndArguments[0];
        // Operationsparameter.
        String[] arguments = getArguments(operatorAndArguments[1]);
        // Operatortyp.
        TypeOperator type = getTypeFromName(operatorName);

        // Muster für das Parsen des Operators.
        ParseResultPattern resultPattern = getResultPattern(pattern);

        /* 
         Falls Namen nicht übereinstimmen -> ParseException (!) werfen.
         In der Klasse Operator sollte man immer zuerst den Namen auslesen und 
         DANN erst das Parsen anwenden.
         */
        if (!operatorName.equals(resultPattern.getOperationName())) {
            throw new ParseException();
        }

        Object[] params = new Object[arguments.length];

        int indexInOperatorArguments = 0;
        ParameterPattern p;
        ArrayList<String> restrictions;
        ArrayList<Integer> indices = new ArrayList<>();

        // Zunächst nur reines Parsen, OHNE die Einschränkungen für die Variablen zu beachten.
        for (int i = 0; i < resultPattern.size(); i++) {

            // Das Pattern besitzt mehr Argumente als der zu parsende Ausdruck.
            if (indexInOperatorArguments >= arguments.length) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_NOT_ENOUGH_PARAMETER_IN_OPERATOR_1")
                        + operatorName);
            }

            // Indizes loggen!
            indices.add(indexInOperatorArguments);

            p = resultPattern.getParameterPattern(i);
            restrictions = p.getRestrictions();

            if (p.getMultiplicity().equals(Multiplicity.one)) {
                params[indexInOperatorArguments] = getOperatorParameter(operatorName, arguments[indexInOperatorArguments], vars, p.getParamType(), restrictions, indexInOperatorArguments);
                indexInOperatorArguments++;
            } else {
                while (indexInOperatorArguments < arguments.length) {
                    try {
                        params[indexInOperatorArguments] = getOperatorParameter(operatorName, arguments[indexInOperatorArguments], vars, p.getParamType(), restrictions, indexInOperatorArguments);
                        indexInOperatorArguments++;
                    } catch (ExpressionException e) {
                        if (indexInOperatorArguments == i) {
                            /* 
                             Es muss mindestens ein Parameter geparst werden, damit KEIN Fehler geworfen wird.
                             In diesem Fall konnte kein einziger Parameter geparst werden.
                             */
                            throw new ExpressionException("EB_Operator_NOT_ENOUGH_PARAMETER_IN_OPERATOR_1");
                        }
                        break;
                    }
                }
            }

        }

        // Der zu parsende Ausdruck besitzt mehr Argumente als das Pattern.
        if (indexInOperatorArguments < arguments.length - 1) {
            throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_TOO_MANY_PARAMETER_IN_OPERATOR_1")
                    + operatorName);
        }

        /* 
         Jetzt müssen noch einmal die Einschränkungen für die Variablen kontrolliert werden.
         Diese Kontrolle muss stattfinden, NACHDEM alle Ausdrücke bereits (erfolgreich) geparst wurden.
         */
        int maxIndexForControl, indexOfExpressionToControlInPattern, maxIndexOfExpressionToControl;
        boolean occurrence;
        AbstractExpression expr;
        AbstractExpression[] exprs;
        String var;
        for (int i = 0; i < resultPattern.size(); i++) {

            p = resultPattern.getParameterPattern(i);
            if (!p.getParamType().equals(ParamType.var)) {
                continue;
            }
            restrictions = p.getRestrictions();

            maxIndexForControl = i < resultPattern.size() - 1 ? indices.get(i + 1) - 1 : resultPattern.size() - 1;
            for (int j = indices.get(i); j <= maxIndexForControl; j++) {

                // Jeweilige Variable für die Kontrolle.
                var = (String) params[j];

                for (String restriction : restrictions) {

                    occurrence = !(restriction.indexOf(ParameterPattern.notin) == 0);
                    indexOfExpressionToControlInPattern = occurrence ? Integer.valueOf(restriction) : Integer.valueOf(restriction.substring(1));

                    if (indexOfExpressionToControlInPattern < resultPattern.size() - 1) {
                        maxIndexOfExpressionToControl = indices.get(indexOfExpressionToControlInPattern + 1) - 1;
                    } else {
                        maxIndexOfExpressionToControl = params.length - 1;
                    }

                    // Eigentliche Kontrolle.
                    for (int q = indices.get(indexOfExpressionToControlInPattern); q <= maxIndexOfExpressionToControl; q++) {

                        if (params[q] instanceof AbstractExpression) {
                            expr = (AbstractExpression) params[q];
                            if (occurrence && !expr.contains(var)) {
                                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_OCCUR_IN_PARAMETER_1")
                                        + var
                                        + Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_OCCUR_IN_PARAMETER_2")
                                        + (q + 1)
                                        + Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_OCCUR_IN_PARAMETER_3")
                                        + operatorName
                                        + Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_OCCUR_IN_PARAMETER_4"));
                            } else if (!occurrence && expr.contains(var)) {
                                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_1")
                                        + var
                                        + Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_2")
                                        + (q + 1)
                                        + Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_3")
                                        + operatorName
                                        + Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_4"));
                            }
                        } else if (params[q] instanceof AbstractExpression[]) {
                            exprs = (AbstractExpression[]) params[q];
                            boolean varOccurrs = false;
                            for (AbstractExpression abstrExpr : exprs){
                                varOccurrs = varOccurrs || abstrExpr.contains(var);
                            }
                            if (occurrence && !varOccurrs) {
                                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_OCCUR_IN_PARAMETER_1")
                                        + var
                                        + Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_OCCUR_IN_PARAMETER_2")
                                        + (q + 1)
                                        + Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_OCCUR_IN_PARAMETER_3")
                                        + operatorName
                                        + Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_OCCUR_IN_PARAMETER_4"));
                            } else if (!occurrence && varOccurrs) {
                                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_1")
                                        + var
                                        + Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_2")
                                        + (q + 1)
                                        + Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_3")
                                        + operatorName
                                        + Translator.translateExceptionMessage("EB_Operator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_4"));
                            }
                        }

                    }

                }

            }

        }

        return new Operator(type, params);

    }

    private static Object getOperatorParameter(String opName, String parameter, HashSet<String> vars, ParamType type, ArrayList<String> restrictions, int index) throws ExpressionException {

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
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))) {
                            return exprLeft;
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    } else {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))
                                && containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                            return exprLeft;
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    }
                } else {
                    return new Expression[]{exprLeft, exprRight};
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
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))) {
                            return expr;
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    } else {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))
                                && containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                            return expr;
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    }
                } else {
                    return expr;
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
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        if (n >= Integer.parseInt(restrictions.get(0))) {
                            return n;
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    } else {
                        if (n >= Integer.parseInt(restrictions.get(0)) && n <= Integer.parseInt(restrictions.get(1))) {
                            return n;
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    }
                } else {
                    return n;
                }
            } catch (NumberFormatException e) {
            }

        } else if (type.equals(ParamType.logexpr)) {

            try {
                LogicalExpression logExpr = LogicalExpression.build(parameter, vars);
                if (!restrictions.isEmpty()) {
                    logExpr.addContainedVars(containedVars);
                    if (restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        return logExpr;
                    } else if (restrictions.get(0).equals(ParameterPattern.none) && !restrictions.get(1).equals(ParameterPattern.none)) {
                        if (containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                            return logExpr;
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))) {
                            return logExpr;
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    } else {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))
                                && containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                            return logExpr;
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    }
                } else {
                    return logExpr;
                }
            } catch (ExpressionException e) {
            }

        } else if (type.equals(ParamType.matexpr)) {

            try {
                MatrixExpression matExpr = MatrixExpression.build(parameter, vars);
                if (!restrictions.isEmpty()) {
                    matExpr.addContainedVars(containedVars);
                    if (restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        return matExpr;
                    } else if (restrictions.get(0).equals(ParameterPattern.none) && !restrictions.get(1).equals(ParameterPattern.none)) {
                        if (containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                            return matExpr;
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))) {
                            return matExpr;
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    } else {
                        if (containedVars.size() >= Integer.parseInt(restrictions.get(0))
                                && containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                            return matExpr;
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(""));
                        }
                    }
                } else {
                    return matExpr;
                }
            } catch (ExpressionException e) {
            }

        }

        String failureMessage = Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_1")
                + (index + 1) + Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_2")
                + opName + Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_3");
        switch (type) {
            case var:
                failureMessage += Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_VAR");
                break;
            case equation:
                failureMessage += Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_EQUATION");
                break;
            case expr:
                failureMessage += Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_EXPRESSION");
                break;
            case integer:
                failureMessage += Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_INTEGER");
                break;
            case logexpr:
                failureMessage += Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_LOGICAL_EXPRESSION");
                break;
            case matexpr:
                failureMessage += Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_MATRIX_EXPRESSION");
                break;
        }

        throw new ExpressionException(failureMessage);

    }

}

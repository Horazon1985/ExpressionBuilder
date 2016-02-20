package operationparser;

import exceptions.ParseException;
import command.Command;
import command.TypeCommand;
import exceptions.ExpressionException;
import abstractexpressions.interfaces.AbstractExpression;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeOperator;
import abstractexpressions.expression.classes.Variable;
import java.util.ArrayList;
import java.util.HashSet;
import abstractexpressions.logicalexpression.classes.LogicalExpression;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import abstractexpressions.matrixexpression.classes.MatrixOperator;
import abstractexpressions.matrixexpression.classes.TypeMatrixOperator;
import operationparser.ParameterPattern.Multiplicity;
import operationparser.ParameterPattern.ParamRole;
import operationparser.ParameterPattern.ParamType;
import lang.translator.Translator;

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
    public static String[] getOperationAndArguments(String input) throws ExpressionException {

        // Leerzeichen beseitigen
        input = input.replaceAll(" ", "");

        String[] result = new String[2];
        int i = input.indexOf("(");
        if (i == -1) {
            // Um zu verhindern, dass es eine IndexOutOfBoundsException gibt.
            i = 0;
        }
        result[0] = input.substring(0, i);

        //Wenn der Befehl leer ist -> Fehler.
        if (result[0].length() == 0) {
            throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_EXPRESSION_EMPTY_OR_INCOMPLETE"));
        }

        //Wenn length(result[0]) > l - 2 -> Fehler (der Befehl besitzt NICHT die Form command(...)).
        if (result[0].length() > input.length() - 2) {
            throw new ExpressionException(input + Translator.translateExceptionMessage("EB_Expression_IS_NOT_VALID_COMMAND"));
        }

        //Wenn am Ende nicht ")" steht.
        if (!input.substring(input.length() - 1, input.length()).equals(")")) {
            throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_MISSING_CLOSING_BRACKET_1")
                    + input
                    + Translator.translateExceptionMessage("EB_Expression_MISSING_CLOSING_BRACKET_2"));
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
    public static String[] getArguments(String input) throws ExpressionException {

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
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_EMPTY_PARAMETER"));
                }
                resultParameters.add(input.substring(startPositionOfCurrentParameter, i));
                startPositionOfCurrentParameter = i + 1;
            }
            if (i == input.length() - 1) {
                if (startPositionOfCurrentParameter == input.length()) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_EMPTY_PARAMETER"));
                }
                resultParameters.add(input.substring(startPositionOfCurrentParameter, input.length()));
            }

        }

        if (bracketCounter != 0 || squareBracketCounter != 0) {
            throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_WRONG_BRACKETS"));
        }

        String[] resultParametersAsArray = new String[resultParameters.size()];
        for (int i = 0; i < resultParameters.size(); i++) {
            resultParametersAsArray[i] = resultParameters.get(i);
        }

        return resultParametersAsArray;

    }

    public static ParseResultPattern getResultPattern(String pattern) throws ExpressionException {

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
            ParamRole role;

            for (ParamType type : ParamType.values()) {

                role = type.getRole();

                if (role.equals(ParamRole.VARIABLE)) {

                    if (args[i].equals(type.name())) {
                        paramPattern[i] = new ParameterPattern(type, Multiplicity.one, restrictionsAsList);
                        break;
                    } else if (args[i].substring(0, args[i].length() - 1).equals(type.name()) && args[i].substring(args[i].length() - 1).equals(ParameterPattern.multPlus)) {
                        paramPattern[i] = new ParameterPattern(type, Multiplicity.plus, restrictionsAsList);
                        break;
                    } else if (args[i].indexOf(type.name()) == 0) {
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

                        if (!paramType.equals(type.name())) {
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
                            boolean referenceOfIndexIsNotExpression = true;
                            for (ParamType typeReferred : ParamType.values()) {
                                if (typeReferred.getRole().equals(ParamRole.EXPRESSION)) {
                                    referenceOfIndexIsNotExpression = referenceOfIndexIsNotExpression && !args[index].contains(typeReferred.name());
                                }
                            }
                            if (referenceOfIndexIsNotExpression) {
                                throw new ParseException(i);
                            }
                            restrictionsAsList.add(restriction);
                        }

                        paramPattern[i] = new ParameterPattern(type, m, restrictionsAsList);
                        break;
                    }

                } else if (role.equals(ParamRole.EXPRESSION)) {

                    if (args[i].equals(type.name())) {
                        paramPattern[i] = new ParameterPattern(type, Multiplicity.one, restrictionsAsList);
                        break;
                    } else if (args[i].substring(0, args[i].length() - 1).equals(type.name()) && args[i].substring(args[i].length() - 1).equals(ParameterPattern.multPlus)) {
                        paramPattern[i] = new ParameterPattern(type, Multiplicity.plus, restrictionsAsList);
                        break;
                    } else if (args[i].indexOf(type.name()) == 0) {
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

                        if (!paramType.equals(type.name())) {
                            throw new ParseException(i);
                        }
                        // Optionale Parameter einlesen (es müssen genau zwei sein).
                        restrictionsAsList = getRestrictionList(restrictions, i);
                        paramPattern[i] = new ParameterPattern(type, m, restrictionsAsList);
                        break;
                    }

                } else if (args[i].equals(type.name())) {
                    paramPattern[i] = new ParameterPattern(type, Multiplicity.one, restrictionsAsList);
                    break;
                } else if (args[i].substring(0, args[i].length() - 1).equals(type.name()) && args[i].substring(args[i].length() - 1).equals(ParameterPattern.multPlus)) {
                    paramPattern[i] = new ParameterPattern(type, Multiplicity.plus, restrictionsAsList);
                    break;
                } else if (args[i].indexOf(type.name()) == 0) {
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

                    if (!paramType.equals(type.name())) {
                        throw new ParseException(i);
                    }
                    // Optionale Parameter einlesen (es müssen genau zwei sein).
                    restrictionsAsList = getRestrictionList(restrictions, i);
                    paramPattern[i] = new ParameterPattern(type, m, restrictionsAsList);
                    break;
                }

            }

            if (paramPattern[i] == null) {
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
    public static Operator parseDefaultOperator(String operatorName, String[] arguments, HashSet<String> vars, String pattern) throws ExpressionException {

        // Operatortyp.
        TypeOperator type = Operator.getTypeFromName(operatorName);

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
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_1")
                        + operatorName
                        + Translator.translateExceptionMessage("EB_Operator_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_2"));
            }

            // Indizes loggen!
            indices.add(indexInOperatorArguments);

            p = resultPattern.getParameterPattern(i);
            restrictions = p.getRestrictions();

            if (p.getMultiplicity().equals(Multiplicity.one)) {
                params[indexInOperatorArguments] = getOperationParameter(operatorName, arguments[indexInOperatorArguments], vars, p.getParamType(), restrictions, indexInOperatorArguments, Operator.class);
                indexInOperatorArguments++;
            } else {
                while (indexInOperatorArguments < arguments.length) {
                    try {
                        params[indexInOperatorArguments] = getOperationParameter(operatorName, arguments[indexInOperatorArguments], vars, p.getParamType(), restrictions, indexInOperatorArguments, Operator.class);
                        indexInOperatorArguments++;
                    } catch (ExpressionException e) {
                        if (indexInOperatorArguments == i || indexInOperatorArguments < arguments.length && i == resultPattern.size() - 1) {
                            /* 
                             Es muss mindestens ein Parameter geparst werden, damit KEIN Fehler geworfen wird.
                             In diesem Fall konnte kein einziger Parameter geparst werden.
                             */
                            throw new ExpressionException(e.getMessage());
                        }
                        break;
                    }
                }
            }

        }

        // Der zu parsende Ausdruck besitzt mehr Argumente als das Pattern.
        if (indexInOperatorArguments < arguments.length - 1 || arguments.length > 0 && resultPattern.size() == 0) {
            throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_TOO_MANY_PARAMETERS_IN_OPERATOR_1")
                    + operatorName
                    + Translator.translateExceptionMessage("EB_Operator_TOO_MANY_PARAMETERS_IN_OPERATOR_2"));
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
            if (!p.getParamType().getRole().equals(ParamRole.VARIABLE)) {
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
                            for (AbstractExpression abstrExpr : exprs) {
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

        /* 
         Schließlich muss noch überprüft werden, ob uniquevars nur einmal 
         vorkommen.
         */
        for (int i = 0; i < resultPattern.size(); i++) {

            p = resultPattern.getParameterPattern(i);
            if (!p.getParamType().equals(ParamType.uniquevar)) {
                continue;
            }

            maxIndexForControl = i < resultPattern.size() - 1 ? indices.get(i + 1) - 1 : resultPattern.size() - 1;
            for (int j = indices.get(i); j <= maxIndexForControl; j++) {
                // Jeweilige Unique-Variable für die Kontrolle.
                var = (String) params[j];
                for (int k = 0; k < params.length; k++) {
                    if (params[k] instanceof String && ((String) params[k]).equals(var) && k != j) {
                        throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_UNIQUE_VARIABLE_OCCUR_TWICE_1")
                                + var
                                + Translator.translateExceptionMessage("EB_Operator_UNIQUE_VARIABLE_OCCUR_TWICE_2")
                                + operatorName
                                + Translator.translateExceptionMessage("EB_Operator_UNIQUE_VARIABLE_OCCUR_TWICE_3"));
                    }
                }
            }

        }

        return new Operator(type, params);

    }

    /**
     * Parsen mathematischer Matrizenoperatoren.
     */
    public static MatrixOperator parseDefaultMatrixOperator(String operatorName, String[] arguments, HashSet<String> vars, String pattern) throws ExpressionException {

        // Operatortyp.
        TypeMatrixOperator type = MatrixOperator.getTypeFromName(operatorName);

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
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixOperator_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_1")
                        + operatorName
                        + Translator.translateExceptionMessage("MEB_MatrixOperator_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_2"));
            }

            // Indizes loggen!
            indices.add(indexInOperatorArguments);

            p = resultPattern.getParameterPattern(i);
            restrictions = p.getRestrictions();

            if (p.getMultiplicity().equals(Multiplicity.one)) {
                params[indexInOperatorArguments] = getOperationParameter(operatorName, arguments[indexInOperatorArguments], vars, p.getParamType(), restrictions, indexInOperatorArguments, MatrixOperator.class);
                indexInOperatorArguments++;
            } else {
                while (indexInOperatorArguments < arguments.length) {
                    try {
                        params[indexInOperatorArguments] = getOperationParameter(operatorName, arguments[indexInOperatorArguments], vars, p.getParamType(), restrictions, indexInOperatorArguments, MatrixOperator.class);
                        indexInOperatorArguments++;
                    } catch (ExpressionException e) {
                        if (indexInOperatorArguments == i || i == resultPattern.size() - 1) {
                            /* 
                             Es muss mindestens ein Parameter geparst werden, damit KEIN Fehler geworfen wird.
                             In diesem Fall konnte kein einziger Parameter geparst werden.
                             ODER: Es wurde ein Argument nicht geparst und das Pattern ist zuende.
                             */
                            throw new ExpressionException(e.getMessage());
                        }
                        break;
                    }
                }
            }

        }

        // Der zu parsende Ausdruck besitzt mehr Argumente als das Pattern.
        if (indexInOperatorArguments < arguments.length - 1 || arguments.length > 0 && resultPattern.size() == 0) {
            throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixOperator_TOO_MANY_PARAMETERS_IN_OPERATOR_1")
                    + operatorName
                    + Translator.translateExceptionMessage("MEB_MatrixOperator_TOO_MANY_PARAMETERS_IN_OPERATOR_2"));
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
            if (!p.getParamType().getRole().equals(ParamRole.VARIABLE)) {
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
                                throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_OCCUR_IN_PARAMETER_1")
                                        + var
                                        + Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_OCCUR_IN_PARAMETER_2")
                                        + (q + 1)
                                        + Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_OCCUR_IN_PARAMETER_3")
                                        + operatorName
                                        + Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_OCCUR_IN_PARAMETER_4"));
                            } else if (!occurrence && expr.contains(var)) {
                                throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_1")
                                        + var
                                        + Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_2")
                                        + (q + 1)
                                        + Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_3")
                                        + operatorName
                                        + Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_4"));
                            }
                        } else if (params[q] instanceof AbstractExpression[]) {
                            exprs = (AbstractExpression[]) params[q];
                            boolean varOccurrs = false;
                            for (AbstractExpression abstrExpr : exprs) {
                                varOccurrs = varOccurrs || abstrExpr.contains(var);
                            }
                            if (occurrence && !varOccurrs) {
                                throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_OCCUR_IN_PARAMETER_1")
                                        + var
                                        + Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_OCCUR_IN_PARAMETER_2")
                                        + (q + 1)
                                        + Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_OCCUR_IN_PARAMETER_3")
                                        + operatorName
                                        + Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_OCCUR_IN_PARAMETER_4"));
                            } else if (!occurrence && varOccurrs) {
                                throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_1")
                                        + var
                                        + Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_2")
                                        + (q + 1)
                                        + Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_3")
                                        + operatorName
                                        + Translator.translateExceptionMessage("MEB_MatrixOperator_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_4"));
                            }
                        }

                    }

                }

            }

        }

        /* 
         Schließlich muss noch überprüft werden, ob uniquevars nur einmal 
         vorkommen.
         */
        for (int i = 0; i < resultPattern.size(); i++) {

            p = resultPattern.getParameterPattern(i);
            if (!p.getParamType().equals(ParamType.uniquevar)) {
                continue;
            }

            maxIndexForControl = i < resultPattern.size() - 1 ? indices.get(i + 1) - 1 : resultPattern.size() - 1;
            for (int j = indices.get(i); j <= maxIndexForControl; j++) {
                // Jeweilige Unique-Variable für die Kontrolle.
                var = (String) params[j];
                for (int k = 0; k < params.length; k++) {
                    if (params[k] instanceof String && ((String) params[k]).equals(var) && k != j) {
                        throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixOperator_UNIQUE_VARIABLE_OCCUR_TWICE_1")
                                + var
                                + Translator.translateExceptionMessage("MEB_MatrixOperator_UNIQUE_VARIABLE_OCCUR_TWICE_2")
                                + operatorName
                                + Translator.translateExceptionMessage("MEB_MatrixOperator_UNIQUE_VARIABLE_OCCUR_TWICE_3"));
                    }
                }
            }

        }

        return new MatrixOperator(type, params);

    }

    /**
     * Parsen mathematischer Standardoperatoren.
     */
    public static Command parseDefaultCommand(String commandName, String[] parameter, String pattern) throws ExpressionException {

        // Operatortyp.
        TypeCommand type = Command.getTypeFromName(commandName);

        // Muster für das Parsen des Operators.
        ParseResultPattern resultPattern = getResultPattern(pattern);

        /* 
         Falls Namen nicht übereinstimmen -> ParseException (!) werfen.
         In der Klasse Operator sollte man immer zuerst den Namen auslesen und 
         DANN erst das Parsen anwenden.
         */
        if (!commandName.equals(resultPattern.getOperationName())) {
            throw new ParseException();
        }

        Object[] params = new Object[parameter.length];

        int indexInCommandParameters = 0;
        ParameterPattern p;
        ArrayList<String> restrictions;
        ArrayList<Integer> indices = new ArrayList<>();

        // Zunächst nur reines Parsen, OHNE die Einschränkungen für die Variablen zu beachten.
        for (int i = 0; i < resultPattern.size(); i++) {

            // Das Pattern besitzt mehr Argumente als der zu parsende Ausdruck.
            if (indexInCommandParameters >= parameter.length) {
                throw new ExpressionException(Translator.translateExceptionMessage("MCC_COMMAND_NOT_ENOUGH_PARAMETERS_IN_COMMAND_1")
                        + commandName
                        + Translator.translateExceptionMessage("MCC_COMMAND_NOT_ENOUGH_PARAMETERS_IN_COMMAND_2"));
            }

            // Indizes loggen!
            indices.add(indexInCommandParameters);

            p = resultPattern.getParameterPattern(i);
            restrictions = p.getRestrictions();

            if (p.getMultiplicity().equals(Multiplicity.one)) {
                params[indexInCommandParameters] = getOperationParameter(commandName, parameter[indexInCommandParameters], null, p.getParamType(), restrictions, indexInCommandParameters, Command.class);
                indexInCommandParameters++;
            } else {
                while (indexInCommandParameters < parameter.length) {
                    try {
                        params[indexInCommandParameters] = getOperationParameter(commandName, parameter[indexInCommandParameters], null, p.getParamType(), restrictions, indexInCommandParameters, Command.class);
                        indexInCommandParameters++;
                    } catch (ExpressionException e) {
                        if (indexInCommandParameters == i) {
                            /* 
                             Es muss mindestens ein Parameter geparst werden, damit KEIN Fehler geworfen wird.
                             In diesem Fall konnte kein einziger Parameter geparst werden.
                             */
                            throw new ExpressionException(e.getMessage());
                        }
                        break;
                    }
                }
            }

        }

        // Der zu parsende Ausdruck besitzt mehr Argumente als das Pattern.
        if (indexInCommandParameters < parameter.length - 1 || parameter.length > 0 && resultPattern.size() == 0) {
            throw new ExpressionException(Translator.translateExceptionMessage("MCC_COMMAND_TOO_MANY_PARAMETERS_IN_COMMAND_1")
                    + commandName
                    + Translator.translateExceptionMessage("MCC_COMMAND_TOO_MANY_PARAMETERS_IN_COMMAND_2"));
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
            if (!p.getParamType().getRole().equals(ParamRole.VARIABLE)) {
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
                                throw new ExpressionException(Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_OCCUR_IN_PARAMETER_1")
                                        + var
                                        + Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_OCCUR_IN_PARAMETER_2")
                                        + (q + 1)
                                        + Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_OCCUR_IN_PARAMETER_3")
                                        + commandName
                                        + Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_OCCUR_IN_PARAMETER_4"));
                            } else if (!occurrence && expr.contains(var)) {
                                throw new ExpressionException(Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_1")
                                        + var
                                        + Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_2")
                                        + (q + 1)
                                        + Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_3")
                                        + commandName
                                        + Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_4"));
                            }
                        } else if (params[q] instanceof AbstractExpression[]) {
                            exprs = (AbstractExpression[]) params[q];
                            boolean varOccurrs = false;
                            for (AbstractExpression abstrExpr : exprs) {
                                varOccurrs = varOccurrs || abstrExpr.contains(var);
                            }
                            if (occurrence && !varOccurrs) {
                                throw new ExpressionException(Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_OCCUR_IN_PARAMETER_1")
                                        + var
                                        + Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_OCCUR_IN_PARAMETER_2")
                                        + (q + 1)
                                        + Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_OCCUR_IN_PARAMETER_3")
                                        + commandName
                                        + Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_OCCUR_IN_PARAMETER_4"));
                            } else if (!occurrence && varOccurrs) {
                                throw new ExpressionException(Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_1")
                                        + var
                                        + Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_2")
                                        + (q + 1)
                                        + Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_3")
                                        + commandName
                                        + Translator.translateExceptionMessage("MCC_COMMAND_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_4"));
                            }
                        }

                    }

                }

            }

        }

        /* 
         Schließlich muss noch überprüft werden, ob uniquevars nur einmal 
         vorkommen.
         */
        for (int i = 0; i < resultPattern.size(); i++) {

            p = resultPattern.getParameterPattern(i);
            if (!p.getParamType().equals(ParamType.uniquevar)) {
                continue;
            }

            maxIndexForControl = i < resultPattern.size() - 1 ? indices.get(i + 1) - 1 : resultPattern.size() - 1;
            for (int j = indices.get(i); j <= maxIndexForControl; j++) {
                // Jeweilige Unique-Variable für die Kontrolle.
                var = (String) params[j];
                for (int k = 0; k < params.length; k++) {
                    if (params[k] instanceof String && ((String) params[k]).equals(var) && k != j) {
                        throw new ExpressionException(Translator.translateExceptionMessage("MCC_COMMAND_UNIQUE_VARIABLE_OCCUR_TWICE_1")
                                + var
                                + Translator.translateExceptionMessage("MCC_COMMAND_UNIQUE_VARIABLE_OCCUR_TWICE_2")
                                + commandName
                                + Translator.translateExceptionMessage("MCC_COMMAND_UNIQUE_VARIABLE_OCCUR_TWICE_3"));
                    }
                }
            }

        }

        return new Command(type, params);

    }

    private static Object getOperationParameter(String opName, String parameter, HashSet<String> vars, ParamType type, ArrayList<String> restrictions, int index, Class cls) throws ExpressionException {

        /* 
         Da diese Methode für Operator, MatrixOperator, Command gültig sein soll,
         muss der Präfix für die Fehlermeldungen angepasst werden. 
         */
        String errorMessagePrefix = "";
        if (cls.equals(Operator.class)) {
            errorMessagePrefix = "EB_Operator_";
        } else if (cls.equals(MatrixOperator.class)) {
            errorMessagePrefix = "MEB_MatrixOperator_";
        } else if (cls.equals(Command.class)) {
            errorMessagePrefix = "MCC_COMMAND_";
        }

        HashSet<String> containedVars = new HashSet<>();

        if (type.getRole().equals(ParamRole.VARIABLE)) {

            if (Expression.isValidDerivateOfVariable(parameter) && Variable.create(parameter).getPreciseExpression() == null) {
                return parameter;
            }

        } else if (type.equals(ParamType.equation)) {

            Expression exprLeft, exprRight;

            try {
                if (!parameter.contains("=")) {
                    throw new ExpressionException(Translator.translateExceptionMessage(errorMessagePrefix + "PARAMETER_MUST_CONTAIN_EQUALITY_SIGN_1")
                            + (index + 1)
                            + Translator.translateExceptionMessage(errorMessagePrefix + "PARAMETER_MUST_CONTAIN_EQUALITY_SIGN_2")
                            + opName
                            + Translator.translateExceptionMessage(errorMessagePrefix + "PARAMETER_MUST_CONTAIN_EQUALITY_SIGN_3"));
                }
                exprLeft = Expression.build(parameter.substring(0, parameter.indexOf("=")), vars);
                exprRight = Expression.build(parameter.substring(parameter.indexOf("=") + 1), vars);
            } catch (ExpressionException e) {
                String failureMessage = Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_1")
                        + (index + 1) + Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_2")
                        + opName
                        + Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_EQUATION");
                throw new ExpressionException(failureMessage);
            }

            exprLeft.addContainedIndeterminates(containedVars);
            exprRight.addContainedIndeterminates(containedVars);
            if (!restrictions.isEmpty()) {
                if (restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                    return new Expression[]{exprLeft, exprRight};
                } else if (restrictions.get(0).equals(ParameterPattern.none) && !restrictions.get(1).equals(ParameterPattern.none)) {
                    if (containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                        return new Expression[]{exprLeft, exprRight};
                    } else {
                        throw new ExpressionException(Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_1")
                                + (index + 1)
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_2")
                                + opName
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_5")
                                + Integer.parseInt(restrictions.get(1))
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_6")
                        );
                    }
                } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                    if (containedVars.size() >= Integer.parseInt(restrictions.get(0))) {
                        return new Expression[]{exprLeft, exprRight};
                    } else {
                        throw new ExpressionException(Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_1")
                                + (index + 1)
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_2")
                                + opName
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_3")
                                + Integer.parseInt(restrictions.get(0))
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_6")
                        );
                    }
                } else if (containedVars.size() >= Integer.parseInt(restrictions.get(0))
                        && containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                    return new Expression[]{exprLeft, exprRight};
                } else {
                    throw new ExpressionException(Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_1")
                            + (index + 1)
                            + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_2")
                            + opName
                            + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_3")
                            + Integer.parseInt(restrictions.get(0))
                            + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_4")
                            + Integer.parseInt(restrictions.get(1))
                            + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_6")
                    );
                }
            } else {
                return new Expression[]{exprLeft, exprRight};
            }

        } else if (type.getRole().equals(ParamRole.EXPRESSION)) {

            AbstractExpression abstrExpr;

            try {
                if (type.equals(ParamType.expr)) {
                    abstrExpr = Expression.build(parameter, vars);
                } else if (type.equals(ParamType.logexpr)) {
                    abstrExpr = LogicalExpression.build(parameter, vars);
                } else {
                    abstrExpr = MatrixExpression.build(parameter, vars);
                }
            } catch (ExpressionException e) {
                String failureMessage = Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_1")
                        + (index + 1) + Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_2")
                        + opName;
                switch (type) {
                    case expr:
                        failureMessage += Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_EXPRESSION");
                        break;
                    case logexpr:
                        failureMessage += Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_LOGICAL_EXPRESSION");
                        break;
                    case matexpr:
                        failureMessage += Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_MATRIX_EXPRESSION");
                        break;
                }
                throw new ExpressionException(failureMessage);
            }

            if (!restrictions.isEmpty()) {
                containedVars = abstrExpr.getContainedIndeterminates();
                if (restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                    return abstrExpr;
                } else if (restrictions.get(0).equals(ParameterPattern.none) && !restrictions.get(1).equals(ParameterPattern.none)) {
                    if (containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                        return abstrExpr;
                    } else {
                        throw new ExpressionException(Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_1")
                                + (index + 1)
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_2")
                                + opName
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_5")
                                + Integer.parseInt(restrictions.get(1))
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_6")
                        );
                    }
                } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                    if (containedVars.size() >= Integer.parseInt(restrictions.get(0))) {
                        return abstrExpr;
                    } else {
                        throw new ExpressionException(Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_1")
                                + (index + 1)
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_2")
                                + opName
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_3")
                                + Integer.parseInt(restrictions.get(0))
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_6")
                        );
                    }
                } else if (containedVars.size() >= Integer.parseInt(restrictions.get(0))
                        && containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                    return abstrExpr;
                } else {
                    throw new ExpressionException(Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_1")
                            + (index + 1)
                            + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_2")
                            + opName
                            + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_3")
                            + Integer.parseInt(restrictions.get(0))
                            + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_4")
                            + Integer.parseInt(restrictions.get(1))
                            + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_6")
                    );
                }
            } else {
                return abstrExpr;
            }

        } else if (type.getRole().equals(ParamRole.INTEGER)) {

            try {
                int n = Integer.parseInt(parameter);
                if (!restrictions.isEmpty()) {
                    if (restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        return n;
                    } else if (restrictions.get(0).equals(ParameterPattern.none) && !restrictions.get(1).equals(ParameterPattern.none)) {
                        if (n <= Integer.parseInt(restrictions.get(1))) {
                            return n;
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_PARAMETER_1")
                                    + (index + 1)
                                    + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_PARAMETER_2")
                                    + opName
                                    + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_PARAMETER_5")
                                    + Integer.parseInt(restrictions.get(1))
                                    + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_PARAMETER_6")
                            );
                        }
                    } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        if (n >= Integer.parseInt(restrictions.get(0))) {
                            return n;
                        } else {
                            throw new ExpressionException(Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_PARAMETER_1")
                                    + (index + 1)
                                    + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_PARAMETER_2")
                                    + opName
                                    + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_PARAMETER_3")
                                    + Integer.parseInt(restrictions.get(0))
                                    + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_PARAMETER_6")
                            );
                        }
                    } else if (n >= Integer.parseInt(restrictions.get(0)) && n <= Integer.parseInt(restrictions.get(1))) {
                        return n;
                    } else {
                        throw new ExpressionException(Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_PARAMETER_1")
                                + (index + 1)
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_PARAMETER_2")
                                + opName
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_PARAMETER_3")
                                + Integer.parseInt(restrictions.get(0))
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_PARAMETER_4")
                                + Integer.parseInt(restrictions.get(1))
                                + Translator.translateExceptionMessage(errorMessagePrefix + "BOUNDS_FOR_PARAMETER_6")
                        );
                    }
                } else {
                    return n;
                }
            } catch (NumberFormatException e) {
            }

        }

        String failureMessage = Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_1")
                + (index + 1) + Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_2")
                + opName;
        switch (type) {
            case uniquevar:
            case var:
                failureMessage += Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_VAR");
                break;
            case equation:
                failureMessage += Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_EQUATION");
                break;
            case expr:
                failureMessage += Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_EXPRESSION");
                break;
            case integer:
                failureMessage += Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_INTEGER");
                break;
            case logexpr:
                failureMessage += Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_LOGICAL_EXPRESSION");
                break;
            case matexpr:
                failureMessage += Translator.translateExceptionMessage(errorMessagePrefix + "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_MATRIX_EXPRESSION");
                break;
        }

        throw new ExpressionException(failureMessage);

    }

}

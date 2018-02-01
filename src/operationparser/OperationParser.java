package operationparser;

import exceptions.ParseException;
import command.Command;
import command.TypeCommand;
import exceptions.ExpressionException;
import abstractexpressions.interfaces.AbstractExpression;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.VALIDATOR;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeOperator;
import java.util.ArrayList;
import java.util.HashSet;
import abstractexpressions.logicalexpression.classes.LogicalExpression;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import abstractexpressions.matrixexpression.classes.MatrixOperator;
import abstractexpressions.matrixexpression.classes.TypeMatrixOperator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import operationparser.ParameterPattern.Multiplicity;
import operationparser.ParameterPattern.ParamRole;
import operationparser.ParameterPattern.ParamType;
import lang.translator.Translator;
import util.OperationDataTO;
import util.OperationParsingUtils;

public abstract class OperationParser {
    
    private static final String OP_OPERATOR_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_1 = "OP_OPERATOR_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_1";
    private static final String OP_OPERATOR_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_2 = "OP_OPERATOR_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_2";
    private static final String OP_OPERATOR_TOO_MANY_PARAMETERS_IN_OPERATOR_1 = "OP_OPERATOR_TOO_MANY_PARAMETERS_IN_OPERATOR_1";
    private static final String OP_OPERATOR_TOO_MANY_PARAMETERS_IN_OPERATOR_2 = "OP_OPERATOR_TOO_MANY_PARAMETERS_IN_OPERATOR_2";
    private static final String OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_1 = "OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_1";
    private static final String OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_2 = "OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_2";
    private static final String OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_3 = "OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_3";
    private static final String OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_4 = "OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_4";
    private static final String OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_1 = "OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_1";
    private static final String OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_2 = "OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_2";
    private static final String OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_3 = "OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_3";
    private static final String OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_4 = "OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_4";
    private static final String OP_OPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_1 = "OP_OPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_1";
    private static final String OP_OPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_2 = "OP_OPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_2";
    private static final String OP_OPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_3 = "OP_OPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_3";
    private static final String OP_MATRIXOPERATOR_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_1 = "OP_MATRIXOPERATOR_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_1";
    private static final String OP_MATRIXOPERATOR_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_2 = "OP_MATRIXOPERATOR_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_2";
    private static final String OP_MATRIXOPERATOR_TOO_MANY_PARAMETERS_IN_OPERATOR_1 = "OP_MATRIXOPERATOR_TOO_MANY_PARAMETERS_IN_OPERATOR_1";
    private static final String OP_MATRIXOPERATOR_TOO_MANY_PARAMETERS_IN_OPERATOR_2 = "OP_MATRIXOPERATOR_TOO_MANY_PARAMETERS_IN_OPERATOR_2";
    private static final String OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_1 = "OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_1";
    private static final String OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_2 = "OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_2";
    private static final String OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_3 = "OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_3";
    private static final String OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_4 = "OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_4";
    private static final String OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_1 = "OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_1";
    private static final String OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_2 = "OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_2";
    private static final String OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_3 = "OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_3";
    private static final String OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_4 = "OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_4";
    private static final String OP_MATRIXOPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_1 = "OP_MATRIXOPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_1";
    private static final String OP_MATRIXOPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_2 = "OP_MATRIXOPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_2";
    private static final String OP_MATRIXOPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_3 = "OP_MATRIXOPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_3";
    private static final String MCC_COMMAND_NOT_ENOUGH_PARAMETERS_IN_COMMAND = "MCC_COMMAND_NOT_ENOUGH_PARAMETERS_IN_COMMAND";
    private static final String MCC_COMMAND_TOO_MANY_PARAMETERS_IN_COMMAND = "MCC_COMMAND_TOO_MANY_PARAMETERS_IN_COMMAND";
    private static final String MCC_COMMAND_VARIABLE_MUST_OCCUR_IN_PARAMETER = "MCC_COMMAND_VARIABLE_MUST_OCCUR_IN_PARAMETER";
    private static final String MCC_COMMAND_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER = "MCC_COMMAND_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER";
    private static final String MCC_COMMAND_UNIQUE_VARIABLE_OCCUR_TWICE = "MCC_COMMAND_UNIQUE_VARIABLE_OCCUR_TWICE";
    private static final String WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_1 = "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_1";
    private static final String WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_2 = "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_2";
    private static final String WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_TYPE = "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_TYPE";
    private static final String PARAMETER_MUST_CONTAIN_EQUALITY_SIGN = "PARAMETER_MUST_CONTAIN_EQUALITY_SIGN";
    private static final String WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_EQUATION = "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_EQUATION";
    private static final String BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_1 = "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_1";
    private static final String BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_2 = "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_2";
    private static final String BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_3 = "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_3";
    private static final String BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_4 = "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_4";
    private static final String BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_5 = "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_5";
    private static final String BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_6 = "BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_6";
    private static final String WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_EXPRESSION = "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_EXPRESSION";
    private static final String WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_LOGICAL_EXPRESSION = "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_LOGICAL_EXPRESSION";
    private static final String WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_MATRIX_EXPRESSION = "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_MATRIX_EXPRESSION";
    private static final String BOUNDS_FOR_PARAMETER_1 = "BOUNDS_FOR_PARAMETER_1";
    private static final String BOUNDS_FOR_PARAMETER_2 = "BOUNDS_FOR_PARAMETER_2";
    private static final String BOUNDS_FOR_PARAMETER_3 = "BOUNDS_FOR_PARAMETER_3";
    private static final String BOUNDS_FOR_PARAMETER_4 = "BOUNDS_FOR_PARAMETER_4";
    private static final String BOUNDS_FOR_PARAMETER_5 = "BOUNDS_FOR_PARAMETER_5";
    private static final String BOUNDS_FOR_PARAMETER_6 = "BOUNDS_FOR_PARAMETER_6";
    private static final String WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_INDET = "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_INDET";
    private static final String WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_VAR = "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_VAR";
    private static final String WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_INTEGER = "WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_INTEGER";

    /**
     * Gibt für ein Pattern pattern das entsprechende Objekt der Klasse
     * ParseResultPattern zurück, wenn möglich. Ansonst wird eine
     * ExpressionException geworfen.
     *
     * @throws ExpressionException
     */
    public static ParseResultPattern getResultPattern(String pattern) throws ExpressionException {

        OperationDataTO opData = OperationParsingUtils.getOperationData(pattern);
        // Operationsname und Operationsparameter auslesen.
        String opName = opData.getOperationName();
        String[] args = opData.getOperationArguments();

        List<ParameterPattern> paramPatterns = new ArrayList<>();
        ParameterPattern[] paramPattern = new ParameterPattern[args.length];

        String paramType;
        String[] restrictions;
        List<String> restrictionsAsList = new ArrayList<>();
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
                        OperationDataTO paramData = OperationParsingUtils.getOperationData(args[i]);
                        paramType = paramData.getOperationName();
                        restrictions = paramData.getOperationArguments();

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
                        OperationDataTO paramData = OperationParsingUtils.getOperationData(args[i]);
                        paramType = paramData.getOperationName();
                        restrictions = paramData.getOperationArguments();

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

                } else if (role.equals(ParamRole.TYPE)) {

                    if (args[i].indexOf(type.name()) == 0) {
                        OperationDataTO paramData = OperationParsingUtils.getOperationData(args[i]);
                        paramType = paramData.getOperationName();
                        restrictions = paramData.getOperationArguments();

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
                        // Optionale Parameter einlesen (es muss mindestens einer sein).
                        restrictionsAsList.addAll(Arrays.asList(restrictions));

                        if (restrictionsAsList.isEmpty()) {
                            // Restriktionen MÜSSEN vorhanden sein.
                            throw new ParseException(i);
                        }
                        paramPattern[i] = new ParameterPattern(type, m, restrictionsAsList);
                        break;
                    }
                    // Restriktionen MÜSSEN vorhanden sein.
                    throw new ParseException(i);

                } else if (args[i].equals(type.name())) {
                    paramPattern[i] = new ParameterPattern(type, Multiplicity.one, restrictionsAsList);
                    break;
                } else if (args[i].substring(0, args[i].length() - 1).equals(type.name()) && args[i].substring(args[i].length() - 1).equals(ParameterPattern.multPlus)) {
                    paramPattern[i] = new ParameterPattern(type, Multiplicity.plus, restrictionsAsList);
                    break;
                } else if (args[i].indexOf(type.name()) == 0) {
                    OperationDataTO paramData = OperationParsingUtils.getOperationData(args[i]);
                    paramType = paramData.getOperationName();
                    restrictions = paramData.getOperationArguments();

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

    /**
     * Gibt eine List mit Einschränkungsparametern zurück, falls der
     * gegebene Parameter ein (abstrakter) Ausdruck ist. Das Array mit den
     * Einschränkungen muss genau die Länge zwei haben und entweder
     * Integer-Zahlen (als Strings) enthalten, oder den String "none". Der erste
     * Parameter gibt die Mindestanzahl an Variablen im Ausdruck vor, der zweite
     * die Höchstzahl. Die Einschränkung "none" bedeutet jeweils, dass keine
     * Einschränkung vorliegt.
     */
    private static List<String> getRestrictionList(String[] restrictions, int index) {

        List<String> restrictionsAsList = new ArrayList<>();

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
    public static Operator parseDefaultOperator(String operatorName, String[] arguments, Set<String> vars, String pattern) throws ExpressionException {

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
        List<String> restrictions;
        List<Integer> indices = new ArrayList<>();

        // Zunächst nur reines Parsen, OHNE die Einschränkungen für die Variablen zu beachten.
        for (int i = 0; i < resultPattern.size(); i++) {

            // Das Pattern besitzt mehr Argumente als der zu parsende Ausdruck.
            if (indexInOperatorArguments >= arguments.length) {
                throw new ExpressionException(Translator.translateOutputMessage(OP_OPERATOR_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_1)
                        + operatorName
                        + Translator.translateOutputMessage(OP_OPERATOR_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_2));
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
        if (indexInOperatorArguments < arguments.length || arguments.length > 0 && resultPattern.size() == 0) {
            throw new ExpressionException(Translator.translateOutputMessage(OP_OPERATOR_TOO_MANY_PARAMETERS_IN_OPERATOR_1)
                    + operatorName
                    + Translator.translateOutputMessage(OP_OPERATOR_TOO_MANY_PARAMETERS_IN_OPERATOR_2));
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
                                throw new ExpressionException(Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_1)
                                        + var
                                        + Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_2)
                                        + (q + 1)
                                        + Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_3)
                                        + operatorName
                                        + Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_4));
                            } else if (!occurrence && expr.contains(var)) {
                                throw new ExpressionException(Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_1)
                                        + var
                                        + Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_2)
                                        + (q + 1)
                                        + Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_3)
                                        + operatorName
                                        + Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_4));
                            }
                        } else if (params[q] instanceof AbstractExpression[]) {
                            exprs = (AbstractExpression[]) params[q];
                            boolean varOccurrs = false;
                            for (AbstractExpression abstrExpr : exprs) {
                                varOccurrs = varOccurrs || abstrExpr.contains(var);
                            }
                            if (occurrence && !varOccurrs) {
                                throw new ExpressionException(Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_1)
                                        + var
                                        + Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_2)
                                        + (q + 1)
                                        + Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_3)
                                        + operatorName
                                        + Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_4));
                            } else if (!occurrence && varOccurrs) {
                                throw new ExpressionException(Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_1)
                                        + var
                                        + Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_2)
                                        + (q + 1)
                                        + Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_3)
                                        + operatorName
                                        + Translator.translateOutputMessage(OP_OPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_4));
                            }
                        }

                    }

                }

            }

        }

        /* 
         Schließlich muss noch überprüft werden, ob uniqueindets und uniquevars nur einmal 
         vorkommen.
         */
        for (int i = 0; i < resultPattern.size(); i++) {

            p = resultPattern.getParameterPattern(i);
            if (!p.getParamType().equals(ParamType.uniqueindet) && !p.getParamType().equals(ParamType.uniquevar)) {
                continue;
            }

            maxIndexForControl = i < resultPattern.size() - 1 ? indices.get(i + 1) - 1 : params.length - 1;
            for (int j = indices.get(i); j <= maxIndexForControl; j++) {
                // Jeweilige Unique-Variable für die Kontrolle.
                var = (String) params[j];
                for (int k = 0; k < params.length; k++) {
                    if (params[k] instanceof String && ((String) params[k]).equals(var) && k != j) {
                        throw new ExpressionException(Translator.translateOutputMessage(OP_OPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_1)
                                + var
                                + Translator.translateOutputMessage(OP_OPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_2)
                                + operatorName
                                + Translator.translateOutputMessage(OP_OPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_3));
                    }
                }
            }

        }

        return new Operator(type, params);

    }

    /**
     * Parsen mathematischer Matrizenoperatoren.
     */
    public static MatrixOperator parseDefaultMatrixOperator(String operatorName, String[] arguments, Set<String> vars, String pattern) throws ExpressionException {

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
        List<String> restrictions;
        List<Integer> indices = new ArrayList<>();

        // Zunächst nur reines Parsen, OHNE die Einschränkungen für die Variablen zu beachten.
        for (int i = 0; i < resultPattern.size(); i++) {

            // Das Pattern besitzt mehr Argumente als der zu parsende Ausdruck.
            if (indexInOperatorArguments >= arguments.length) {
                throw new ExpressionException(Translator.translateOutputMessage(OP_MATRIXOPERATOR_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_1)
                        + operatorName
                        + Translator.translateOutputMessage(OP_MATRIXOPERATOR_NOT_ENOUGH_PARAMETERS_IN_OPERATOR_2));
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
        if (indexInOperatorArguments < arguments.length || arguments.length > 0 && resultPattern.size() == 0) {
            throw new ExpressionException(Translator.translateOutputMessage(OP_MATRIXOPERATOR_TOO_MANY_PARAMETERS_IN_OPERATOR_1)
                    + operatorName
                    + Translator.translateOutputMessage(OP_MATRIXOPERATOR_TOO_MANY_PARAMETERS_IN_OPERATOR_2));
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
                                throw new ExpressionException(Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_1)
                                        + var
                                        + Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_2)
                                        + (q + 1)
                                        + Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_3)
                                        + operatorName
                                        + Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_4));
                            } else if (!occurrence && expr.contains(var)) {
                                throw new ExpressionException(Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_1)
                                        + var
                                        + Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_2)
                                        + (q + 1)
                                        + Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_3)
                                        + operatorName
                                        + Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_4));
                            }
                        } else if (params[q] instanceof AbstractExpression[]) {
                            exprs = (AbstractExpression[]) params[q];
                            boolean varOccurrs = false;
                            for (AbstractExpression abstrExpr : exprs) {
                                varOccurrs = varOccurrs || abstrExpr.contains(var);
                            }
                            if (occurrence && !varOccurrs) {
                                throw new ExpressionException(Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_1)
                                        + var
                                        + Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_2)
                                        + (q + 1)
                                        + Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_3)
                                        + operatorName
                                        + Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_OCCUR_IN_PARAMETER_4));
                            } else if (!occurrence && varOccurrs) {
                                throw new ExpressionException(Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_1)
                                        + var
                                        + Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_2)
                                        + (q + 1)
                                        + Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_3)
                                        + operatorName
                                        + Translator.translateOutputMessage(OP_MATRIXOPERATOR_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER_4));
                            }
                        }

                    }

                }

            }

        }

        /* 
         Schließlich muss noch überprüft werden, ob uniquevars und uniqueindets nur einmal 
         vorkommen.
         */
        for (int i = 0; i < resultPattern.size(); i++) {

            p = resultPattern.getParameterPattern(i);
            if (!p.getParamType().equals(ParamType.uniqueindet) && !p.getParamType().equals(ParamType.uniquevar)) {
                continue;
            }

            maxIndexForControl = i < resultPattern.size() - 1 ? indices.get(i + 1) - 1 : params.length - 1;
            for (int j = indices.get(i); j <= maxIndexForControl; j++) {
                // Jeweilige Unique-Variable für die Kontrolle.
                var = (String) params[j];
                for (int k = 0; k < params.length; k++) {
                    if (params[k] instanceof String && ((String) params[k]).equals(var) && k != j) {
                        throw new ExpressionException(Translator.translateOutputMessage(OP_MATRIXOPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_1)
                                + var
                                + Translator.translateOutputMessage(OP_MATRIXOPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_2)
                                + operatorName
                                + Translator.translateOutputMessage(OP_MATRIXOPERATOR_UNIQUE_VARIABLE_OCCUR_TWICE_3));
                    }
                }
            }

        }

        return new MatrixOperator(type, params);

    }

    /**
     * Parsen von Standardbefehlen (also Befehlen, zu denen ein Pattern
     * existiert, welches eine Instanz der Klasse ParseResultPattern ist).
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
        List<String> restrictions;
        List<Integer> indices = new ArrayList<>();

        // Zunächst nur reines Parsen, OHNE die Einschränkungen für die Variablen zu beachten.
        for (int i = 0; i < resultPattern.size(); i++) {

            // Das Pattern besitzt mehr Argumente als der zu parsende Ausdruck.
            if (indexInCommandParameters >= parameter.length) {
                throw new ExpressionException(Translator.translateOutputMessage(MCC_COMMAND_NOT_ENOUGH_PARAMETERS_IN_COMMAND, commandName));
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
        if (indexInCommandParameters < parameter.length || parameter.length > 0 && resultPattern.size() == 0) {
            throw new ExpressionException(Translator.translateOutputMessage(MCC_COMMAND_TOO_MANY_PARAMETERS_IN_COMMAND, commandName));
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
                                throw new ExpressionException(Translator.translateOutputMessage(MCC_COMMAND_VARIABLE_MUST_OCCUR_IN_PARAMETER,
                                        var, q + 1, commandName));
                            } else if (!occurrence && expr.contains(var)) {
                                throw new ExpressionException(Translator.translateOutputMessage(MCC_COMMAND_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER,
                                        var, q + 1, commandName));
                            }
                        } else if (params[q] instanceof AbstractExpression[]) {
                            exprs = (AbstractExpression[]) params[q];
                            boolean varOccurrs = false;
                            for (AbstractExpression abstrExpr : exprs) {
                                varOccurrs = varOccurrs || abstrExpr.contains(var);
                            }
                            if (occurrence && !varOccurrs) {
                                throw new ExpressionException(Translator.translateOutputMessage(MCC_COMMAND_VARIABLE_MUST_OCCUR_IN_PARAMETER,
                                        var, q + 1, commandName));
                            } else if (!occurrence && varOccurrs) {
                                throw new ExpressionException(Translator.translateOutputMessage(MCC_COMMAND_VARIABLE_MUST_NOT_OCCUR_IN_PARAMETER,
                                        var, q + 1, commandName));
                            }
                        }

                    }

                }

            }

        }

        /* 
         Schließlich muss noch überprüft werden, ob uniquevars und uniqueindets nur einmal 
         vorkommen.
         */
        for (int i = 0; i < resultPattern.size(); i++) {

            p = resultPattern.getParameterPattern(i);
            if (!p.getParamType().equals(ParamType.uniqueindet) && !p.getParamType().equals(ParamType.uniquevar)) {
                continue;
            }

            maxIndexForControl = i < resultPattern.size() - 1 ? indices.get(i + 1) - 1 : params.length - 1;
            for (int j = indices.get(i); j <= maxIndexForControl; j++) {
                // Jeweilige Unique-Variable für die Kontrolle.
                var = (String) params[j];
                for (int k = 0; k < params.length; k++) {
                    if (params[k] instanceof String && ((String) params[k]).equals(var) && k != j) {
                        throw new ExpressionException(Translator.translateOutputMessage(MCC_COMMAND_UNIQUE_VARIABLE_OCCUR_TWICE, var, commandName));
                    }
                }
            }

        }

        return new Command(type, params);

    }

    /**
     * Gibt den zu den Eingabeparametern zugehörigen Operationsparameter zurück.
     *
     * @throws ExpressionException
     */
    private static Object getOperationParameter(String opName, String parameter, Set<String> vars, ParamType type, List<String> restrictions, int index, Class cls) throws ExpressionException {

        /* 
         Da diese Methode für Operator, MatrixOperator, Command gültig sein soll,
         muss der Präfix für die Fehlermeldungen angepasst werden. 
         */
        String errorMessagePrefix = "";
        if (cls.equals(Operator.class)) {
            errorMessagePrefix = "OP_OPERATOR_";
        } else if (cls.equals(MatrixOperator.class)) {
            errorMessagePrefix = "OP_MATRIXOPERATOR_";
        } else if (cls.equals(Command.class)) {
            errorMessagePrefix = "OP_COMMAND_";
        }

        Set<String> containedVars = new HashSet<>();

        if (type.getRole().equals(ParamRole.VARIABLE)) {

            if (type.equals(ParamType.uniquevar) || type.equals(ParamType.var)) {
                if (VALIDATOR.isValidIdentifier(parameter)) {
                    return parameter;
                }
            } else if (Expression.isValidDerivativeOfIndeterminate(parameter)) {
                return parameter;
            }

        } else if (type.getRole().equals(ParamRole.TYPE)) {

            if (type.equals(ParamType.type)) {
                if (!restrictions.contains(parameter)) {
                    String failureMessage = Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_1)
                            + (index + 1) + Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_2)
                            + opName
                            + Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_TYPE);
                    for (int i = 0; i < restrictions.size(); i++) {
                        failureMessage += restrictions.get(i);
                        if (i < restrictions.size() - 1) {
                            failureMessage += ", ";
                        }
                    }
                    throw new ExpressionException(failureMessage);
                }
            }

        } else if (type.equals(ParamType.equation)) {

            Expression exprLeft, exprRight;

            try {
                if (!parameter.contains("=")) {
                    throw new ExpressionException(Translator.translateOutputMessage(errorMessagePrefix + PARAMETER_MUST_CONTAIN_EQUALITY_SIGN, index + 1, opName));
                }
                exprLeft = Expression.build(parameter.substring(0, parameter.indexOf("=")), vars);
                exprRight = Expression.build(parameter.substring(parameter.indexOf("=") + 1), vars);
            } catch (ExpressionException e) {
                String failureMessage = Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_1)
                        + (index + 1) + Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_2)
                        + opName
                        + Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_EQUATION);
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
                        throw new ExpressionException(Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_1)
                                + (index + 1)
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_2)
                                + opName
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_5)
                                + Integer.parseInt(restrictions.get(1))
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_6)
                        );
                    }
                } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                    if (containedVars.size() >= Integer.parseInt(restrictions.get(0))) {
                        return new Expression[]{exprLeft, exprRight};
                    } else {
                        throw new ExpressionException(Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_1)
                                + (index + 1)
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_2)
                                + opName
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_3)
                                + Integer.parseInt(restrictions.get(0))
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_6)
                        );
                    }
                } else if (containedVars.size() >= Integer.parseInt(restrictions.get(0))
                        && containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                    return new Expression[]{exprLeft, exprRight};
                } else {
                    throw new ExpressionException(Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_1)
                            + (index + 1)
                            + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_2)
                            + opName
                            + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_3)
                            + Integer.parseInt(restrictions.get(0))
                            + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_4)
                            + Integer.parseInt(restrictions.get(1))
                            + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_6)
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
                String failureMessage = Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_1)
                        + (index + 1) + Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_2)
                        + opName;
                switch (type) {
                    case expr:
                        failureMessage += Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_EXPRESSION);
                        break;
                    case logexpr:
                        failureMessage += Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_LOGICAL_EXPRESSION);
                        break;
                    case matexpr:
                        failureMessage += Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_MATRIX_EXPRESSION);
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
                        throw new ExpressionException(Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_1)
                                + (index + 1)
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_2)
                                + opName
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_5)
                                + Integer.parseInt(restrictions.get(1))
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_6)
                        );
                    }
                } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                    if (containedVars.size() >= Integer.parseInt(restrictions.get(0))) {
                        return abstrExpr;
                    } else {
                        throw new ExpressionException(Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_1)
                                + (index + 1)
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_2)
                                + opName
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_3)
                                + Integer.parseInt(restrictions.get(0))
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_6)
                        );
                    }
                } else if (containedVars.size() >= Integer.parseInt(restrictions.get(0))
                        && containedVars.size() <= Integer.parseInt(restrictions.get(1))) {
                    return abstrExpr;
                } else {
                    throw new ExpressionException(Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_1)
                            + (index + 1)
                            + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_2)
                            + opName
                            + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_3)
                            + Integer.parseInt(restrictions.get(0))
                            + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_4)
                            + Integer.parseInt(restrictions.get(1))
                            + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_VAR_OCCURRENCE_IN_PARAMETER_6)
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
                            throw new ExpressionException(Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_PARAMETER_1)
                                    + (index + 1)
                                    + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_PARAMETER_2)
                                    + opName
                                    + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_PARAMETER_5)
                                    + Integer.parseInt(restrictions.get(1))
                                    + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_PARAMETER_6)
                            );
                        }
                    } else if (!restrictions.get(0).equals(ParameterPattern.none) && restrictions.get(1).equals(ParameterPattern.none)) {
                        if (n >= Integer.parseInt(restrictions.get(0))) {
                            return n;
                        } else {
                            throw new ExpressionException(Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_PARAMETER_1)
                                    + (index + 1)
                                    + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_PARAMETER_2)
                                    + opName
                                    + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_PARAMETER_3)
                                    + Integer.parseInt(restrictions.get(0))
                                    + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_PARAMETER_6)
                            );
                        }
                    } else if (n >= Integer.parseInt(restrictions.get(0)) && n <= Integer.parseInt(restrictions.get(1))) {
                        return n;
                    } else {
                        throw new ExpressionException(Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_PARAMETER_1)
                                + (index + 1)
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_PARAMETER_2)
                                + opName
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_PARAMETER_3)
                                + Integer.parseInt(restrictions.get(0))
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_PARAMETER_4)
                                + Integer.parseInt(restrictions.get(1))
                                + Translator.translateOutputMessage(errorMessagePrefix + BOUNDS_FOR_PARAMETER_6)
                        );
                    }
                } else {
                    return n;
                }
            } catch (NumberFormatException e) {
            }

        }

        String failureMessage = Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_1)
                + (index + 1) + Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_2)
                + opName;
        switch (type) {
            case type:
                failureMessage += Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_TYPE);
                break;
            case uniqueindet:
            case indet:
                failureMessage += Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_INDET);
                break;
            case uniquevar:
            case var:
                failureMessage += Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_VAR);
                break;
            case equation:
                failureMessage += Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_EQUATION);
                break;
            case expr:
                failureMessage += Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_EXPRESSION);
                break;
            case integer:
                failureMessage += Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_INTEGER);
                break;
            case logexpr:
                failureMessage += Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_LOGICAL_EXPRESSION);
                break;
            case matexpr:
                failureMessage += Translator.translateOutputMessage(errorMessagePrefix + WRONG_FORM_OF_GENERAL_PARAMETER_IN_OPERATOR_MATRIX_EXPRESSION);
                break;
        }

        throw new ExpressionException(failureMessage);

    }

}

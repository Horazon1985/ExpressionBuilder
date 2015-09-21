package expressionbuilder;

import computation.AnalysisMethods;
import computation.ArithmeticMethods;
import computation.NumericalMethods;
import computationbounds.ComputationBounds;
import expressionsimplifymethods.SimplifyOperatorMethods;
import integrationmethods.SimplifyIntegralMethods;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import translator.Translator;

public class Operator extends Expression {

    private TypeOperator type;
    private Object[] params;
    private boolean precise;

    public Operator() {
        this.type = TypeOperator.none;
        this.params = new Object[1];
        this.precise = true;
    }

    public Operator(TypeOperator type, Object[] params) {
        this.type = type;
        this.params = params;
        this.precise = true;
    }

    public Operator(TypeOperator type, Object[] params, boolean precise) {
        this.type = type;
        this.params = params;
        this.precise = precise;
    }

    public TypeOperator getType() {
        return this.type;
    }

    public Object[] getParams() {
        return this.params;
    }

    public boolean getPrecise() {
        return this.precise;
    }

    public void setType(TypeOperator typeOperator) {
        this.type = typeOperator;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public void setPrecise(boolean precise) {
        this.precise = precise;
    }

    /**
     * Gibt den entsprechenden Typ des Operators zurück, der zum String operator
     * (welcher in der Konsole eingegeben wird) gehört.
     */
    public static TypeOperator getTypeFromName(String operator) {
        if (operator.equals("int")) {
            return TypeOperator.integral;
        }
        return TypeOperator.valueOf(operator);
    }

    /**
     * Gibt den Namen des Operators zurück, der in der Konsole eingegeben werden
     * muss bzw. der bei der Ausgabe erscheint.
     */
    public static String getNameFromType(TypeOperator type) {
        if (type.equals(TypeOperator.integral)) {
            return "int";
        }
        return type.toString();
    }

    /**
     * Ermittelt den zugehörigen Operator und liefert eine Instanz der Klasse
     * Operator.
     *
     * @throws ExpressionException
     */
    public static Operator getOperator(String operator, String[] params, HashSet<String> vars) throws ExpressionException {

        TypeOperator type = getTypeFromName(operator);

        // Ergebnisoperator zunächst (beliebig) initialisieren.
        Operator resultOperator = new Operator();
        Object[] resultOperatorParams;

        // DIFFERENTIALOPERATOR
        if (type.equals(TypeOperator.diff)) {
            if (params.length < 2) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_NOT_ENOUGH_PARAMETERS_IN_DIFF"));
            }

            if (params.length == 3) {

                try {
                    Expression.build(params[0], vars);
                } catch (NumberFormatException e) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_1_PARAMETER_IN_DIFF_IS_WRONG") + e.getMessage());
                }

                if (!isValidVariable(params[1])) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_2_PARAMETER_IN_DIFF_IS_INVALID"));
                }

                boolean thirdArgumentIsValid = true;
                if (!isValidVariable(params[2])) {
                    try {
                        Integer.parseInt(params[2]);
                    } catch (NumberFormatException e) {
                        thirdArgumentIsValid = false;
                    }
                }

                if (!thirdArgumentIsValid) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_3_PARAMETER_IN_DIFF_IS_INVALID"));
                }

                if (!isValidVariable(params[2])) {
                    int n = Integer.parseInt(params[2]);
                    if (n < 0) {
                        throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_3_PARAMETER_IN_DIFF_IS_INVALID"));
                    }
                }

                if (!isValidVariable(params[2])) {
                    resultOperatorParams = new Object[3];
                    resultOperatorParams[0] = Expression.build(params[0], vars);
                    resultOperatorParams[1] = params[1];
                    resultOperatorParams[2] = Integer.parseInt(params[2]);
                    resultOperator.setType(type);
                    resultOperator.setParams(resultOperatorParams);
                    return resultOperator;
                } else {
                    resultOperatorParams = new Object[3];
                    resultOperatorParams[0] = Expression.build(params[0], vars);
                    resultOperatorParams[1] = params[1];
                    resultOperatorParams[2] = params[2];
                    resultOperator.setType(type);
                    resultOperator.setParams(resultOperatorParams);
                    return resultOperator;
                }

            } else {

                try {
                    Expression.build(params[0], vars);
                } catch (ExpressionException e) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_1_PARAMETER_IN_DIFF_IS_WRONG") + e.getMessage());
                }

                // Es wird zunächst geprüft, ob alle übrigen Parameter gültige Variablen sind.
                boolean allVariablesAreValid = true;
                int indexOfInvalidVariable = 0;
                for (int i = 1; i < params.length; i++) {
                    if (!isValidVariable(params[i])) {
                        allVariablesAreValid = false;
                        indexOfInvalidVariable = i;
                    }
                }

                if (!allVariablesAreValid) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_GENERAL_PARAMETER_IN_DIFF_IS_INVALID_1")
                            + String.valueOf(indexOfInvalidVariable + 1)
                            + Translator.translateExceptionMessage("EB_Operator_GENERAL_PARAMETER_IN_DIFF_IS_INVALID_2"));
                }

                if (allVariablesAreValid) {
                    resultOperatorParams = new Object[params.length];
                    resultOperatorParams[0] = Expression.build(params[0], vars);
                    System.arraycopy(params, 1, resultOperatorParams, 1, params.length - 1);
                    resultOperator.setType(type);
                    resultOperator.setParams(resultOperatorParams);
                    return resultOperator;
                }

            }
        }

        // DIVERGENZ
        if (type.equals(TypeOperator.div)) {
            if (params.length != 1) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_NUMBER_OF_PARAMETERS_IN_DIV"));
            }

            try {
                Expression.build(params[0], vars);
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_PARAMETER_IN_DIV_IS_INVALID") + e.getMessage());
            }

            resultOperatorParams = new Object[1];
            resultOperatorParams[0] = Expression.build(params[0], vars);
            resultOperator.setType(type);
            resultOperator.setParams(resultOperatorParams);
            return resultOperator;
        }

        // FAKULTÄT
        if (type.equals(TypeOperator.fac)) {
            if (params.length != 1) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_NUMBER_OF_PARAMETERS_IN_FAC"));
            }

            try {
                Expression.build(params[0], vars);
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_PARAMETER_IN_FAC_IS_INVALID") + e.getMessage());
            }

            resultOperatorParams = new Object[1];
            resultOperatorParams[0] = Expression.build(params[0], vars);
            resultOperator.setType(type);
            resultOperator.setParams(resultOperatorParams);
            return resultOperator;
        }

        // GCD
        if (type.equals(TypeOperator.gcd)) {
            if (params.length == 0) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_NOT_ENOUGH_PARAMETERS_IN_GCD"));
            }

            resultOperatorParams = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                try {
                    resultOperatorParams[i] = Expression.build(params[i], vars);
                } catch (ExpressionException e) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_GENERAL_PARAMETER_IN_GCD_IS_INVALID_1")
                            + (i + 1)
                            + Translator.translateExceptionMessage("EB_Operator_GENERAL_PARAMETER_IN_GCD_IS_INVALID_2")
                            + e.getMessage());
                }
            }

            resultOperator.setType(type);
            resultOperator.setParams(resultOperatorParams);
            return resultOperator;
        }

        // INTEGRAL
        if (type.equals(TypeOperator.integral)) {
            if (params.length != 2 && params.length != 4) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_NUMBER_OF_PARAMETERS_PARAMETER_IN_INT"));
            }

            HashSet<String> varsInIntegrand = new HashSet<>();
            Expression integrand;
            try {
                integrand = Expression.build(params[0], vars);
                /**
                 * Dies dient dazu, die Variablen im Integranden zu bestimmen
                 */
                integrand.getContainedVars(varsInIntegrand);
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_1_PARAMETER_IN_INT_IS_INVALID") + e.getMessage());
            }

            String intVar = params[1];
            if (!Expression.isValidDerivateOfVariable(intVar)) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_2_PARAMETER_IN_INT_IS_INVALID"));
            }

            if (params.length == 2) {
                resultOperatorParams = new Object[2];
                resultOperator.setType(type);
                resultOperatorParams[0] = integrand;
                resultOperatorParams[1] = params[1];
                resultOperator.setParams(resultOperatorParams);
                return resultOperator;
            }

            HashSet<String> varsInIntegrationLimit = new HashSet<>();
            Expression lowerLimit, upperLimit;
            try {
                lowerLimit = Expression.build(params[2], varsInIntegrationLimit);
                if (varsInIntegrationLimit.contains(intVar)) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_3_PARAMETER_IN_INT_IS_INVALID"));
                }
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_3_PARAMETER_IN_INT_IS_INVALID"));
            }

            try {
                upperLimit = Expression.build(params[3], varsInIntegrationLimit);
                if (varsInIntegrationLimit.contains(intVar)) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_4_PARAMETER_IN_INT_IS_INVALID"));
                }
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_4_PARAMETER_IN_INT_IS_INVALID"));
            }

            resultOperatorParams = new Object[4];
            resultOperator.setType(type);
            resultOperatorParams[0] = integrand;
            resultOperatorParams[1] = params[1];
            resultOperatorParams[2] = lowerLimit;
            resultOperatorParams[3] = upperLimit;
            resultOperator.setParams(resultOperatorParams);
            return resultOperator;
        }

        // LAPLACE-OPERATOR
        if (type.equals(TypeOperator.laplace)) {
            if (params.length != 1) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_NUMBER_OF_PARAMETERS_IN_LAPLACE"));
            }

            try {
                Expression.build(params[0], vars);
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_PARAMETER_IN_LAPLACE_IS_INVALID") + e.getMessage());
            }

            resultOperatorParams = new Object[1];
            resultOperatorParams[0] = Expression.build(params[0], vars);
            resultOperator.setType(type);
            resultOperator.setParams(resultOperatorParams);
            return resultOperator;
        }

        // LCM
        if (type.equals(TypeOperator.lcm)) {
            if (params.length == 0) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_NOT_ENOUGH_PARAMETERS_IN_LCM"));
            }

            resultOperatorParams = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                try {
                    resultOperatorParams[i] = Expression.build(params[i], vars);
                } catch (ExpressionException e) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_GENERAL_PARAMETER_IN_LCM_IS_INVALID_1")
                            + (i + 1)
                            + Translator.translateExceptionMessage("EB_Operator_GENERAL_PARAMETER_IN_LCM_IS_INVALID_2")
                            + e.getMessage());
                }
            }

            resultOperator.setType(type);
            resultOperator.setParams(resultOperatorParams);
            return resultOperator;
        }

        // MOD
        if (type.equals(TypeOperator.mod)) {
            if (params.length != 2) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_NUMBER_OF_PARAMETERS_IN_MOD"));
            }

            resultOperatorParams = new Object[2];
            for (int i = 0; i < 2; i++) {
                try {
                    resultOperatorParams[i] = Expression.build(params[i], vars);
                } catch (ExpressionException e) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_GENERAL_PARAMETER_IN_MOD_IS_INVALID_1")
                            + (i + 1)
                            + Translator.translateExceptionMessage("EB_Operator_GENERAL_PARAMETER_IN_MOD_IS_INVALID_2")
                            + e.getMessage());
                }
            }

            resultOperator.setType(type);
            resultOperator.setParams(resultOperatorParams);
            return resultOperator;
        }

        // PRODUKT
        if (type.equals(TypeOperator.prod)) {
            if (params.length != 4) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_NUMBER_OF_PARAMETERS_IN_PROD"));
            }

            Expression factor, lowerLimit, upperLimit;
            try {
                factor = Expression.build(params[0], vars);
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_1_PARAMETER_IN_PROD_IS_INVALID") + e.getMessage());
            }

            if (!isValidVariable(params[1])) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_2_PARAMETER_IN_PROD_IS_INVALID"));
            }

            try {
                lowerLimit = Expression.build(params[2], vars);
                if (!lowerLimit.isIntegerConstant() && lowerLimit.isConstant()) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_1")
                            + 3
                            + Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_2"));
                }
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_1")
                        + 3
                        + Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_2"));
            }

            try {
                upperLimit = Expression.build(params[3], vars);
                if (!upperLimit.isIntegerConstant() && upperLimit.isConstant()) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_1")
                            + 4
                            + Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_2"));
                }
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_1")
                        + 4
                        + Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_2"));
            }

            if (lowerLimit.contains(params[1]) || upperLimit.contains(params[1])) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_BOUNDS_IN_PROD_CANNOT_CONTAIN_INDEX_VARIABLE"));
            }

            resultOperatorParams = new Object[4];
            resultOperatorParams[0] = factor;
            resultOperatorParams[1] = params[1];
            resultOperatorParams[2] = lowerLimit;
            resultOperatorParams[3] = upperLimit;
            resultOperator.setType(type);
            resultOperator.setParams(resultOperatorParams);
            return resultOperator;
        }

        // SUMME
        if (type.equals(TypeOperator.sum)) {
            if (params.length != 4) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_NUMBER_OF_PARAMETERS_IN_SUM"));
            }

            Expression summand, lowerLimit, upperLimit;
            try {
                summand = Expression.build(params[0], vars);
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_1_PARAMETER_IN_SUM_IS_INVALID") + e.getMessage());
            }

            if (!isValidVariable(params[1])) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_2_PARAMETER_IN_SUM_IS_INVALID"));
            }

            try {
                lowerLimit = Expression.build(params[2], vars);
                if (!lowerLimit.isIntegerConstant() && lowerLimit.isConstant()) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_1")
                            + 3
                            + Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_2"));
                }
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_1")
                        + 3
                        + Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_2"));
            }

            try {
                upperLimit = Expression.build(params[3], vars);
                if (!upperLimit.isIntegerConstant() && upperLimit.isConstant()) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_1")
                            + 4
                            + Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_2"));
                }
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_1")
                        + 4
                        + Translator.translateExceptionMessage("EB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_2"));
            }

            if (lowerLimit.contains(params[1]) || upperLimit.contains(params[1])) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_BOUNDS_IN_SUM_CANNOT_CONTAIN_INDEX_VARIABLE"));
            }

            resultOperatorParams = new Object[4];
            resultOperatorParams[0] = summand;
            resultOperatorParams[1] = params[1];
            resultOperatorParams[2] = lowerLimit;
            resultOperatorParams[3] = upperLimit;
            resultOperator.setType(type);
            resultOperator.setParams(resultOperatorParams);
            return resultOperator;
        }

        // TAYLORPOLYNOM
        if (type.equals(TypeOperator.taylor)) {
            if (params.length != 4) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_WRONG_NUMBER_OF_PARAMETERS_IN_TAYLOR"));
            }

            try {
                Expression.build(params[0], vars);
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_1_PARAMETER_IN_TAYLOR_IS_INVALID") + e.getMessage());
            }

            if (!isValidVariable(params[1])) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_2_PARAMETER_IN_TAYLOR_IS_INVALID"));
            }

            HashSet<String> varsInCenterPoint = new HashSet<>();
            try {
                Expression.build(params[2], varsInCenterPoint);
                if (!varsInCenterPoint.isEmpty()) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_3_PARAMETER_IN_TAYLOR_IS_INVALID"));
                }
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_3_PARAMETER_IN_TAYLOR_IS_INVALID"));
            }

            try {
                int k = Integer.parseInt(params[3]);
                if (k < 0) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_4_PARAMETER_IN_TAYLOR_IS_INVALID"));
                }
            } catch (NumberFormatException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Operator_4_PARAMETER_IN_TAYLOR_IS_INVALID"));
            }

            resultOperatorParams = new Object[4];
            resultOperatorParams[0] = Expression.build(params[0], vars);
            resultOperatorParams[1] = params[1];
            resultOperatorParams[2] = Expression.build(params[2], vars);
            resultOperatorParams[3] = Integer.parseInt(params[3]);
            resultOperator.setType(type);
            resultOperator.setParams(resultOperatorParams);
            return resultOperator;
        }

        // Kommt nicht vor, da alle Fälle abgedeckt sind, aber trotzdem.        
        return resultOperator;

    }

    @Override
    public Expression copy() {
        return new Operator(this.type, this.params, this.precise);
    }

    @Override
    public double evaluate() throws EvaluationException {

        /*
         Bei der Auswertung von Operatoren wird zunächst versucht, den
         Operqator soweit wie möglich zu vereinfachen. Das Ergebnis kann nur
         dann evtl. nicht durch die üblichen Funktionen ausgedrückt werden,
         wenn dieser entweder das Integral oder die Gammafunktion (Fakultät)
         ist. Dann muss "mit Gewalt" ausgewertet werden.
         */
        if (this.getType().equals(TypeOperator.diff)) {
            return simplifyTrivialDiff().evaluate();
        }
        if (this.getType().equals(TypeOperator.div)) {
            return simplifyTrivialDiv().evaluate();
        }
        if (this.getType().equals(TypeOperator.fac)) {

            Expression expr = this.simplifyTrivialFac();
            if (expr instanceof Operator && ((Operator) expr).getType().equals(TypeOperator.fac)) {
                return AnalysisMethods.Gamma(((Expression) this.params[0]).evaluate() + 1);
            }
            return expr.evaluate();

        }
        if (this.getType().equals(TypeOperator.gcd)) {
            return simplifyTrivialGCD().evaluate();
        }
        if (this.getType().equals(TypeOperator.integral)) {

            Expression expr = this.simplifyTrivialInt();
            if (expr instanceof Operator && ((Operator) expr).getType().equals(TypeOperator.integral)) {

                if (((Operator) expr).getParams().length == 2) {
                    /*
                     In diesem Fall war eine unbestimmte Integration NICHT
                     erfolgreich -> als Evaluierung einfach 0 ausgeben.
                     */
                    return 0;
                } else {
                    /*
                     Hier handelt es sich um eine bestimmte Integration,
                     welche mittels numerischer Verfahren behandelt werden
                     kann.
                     */
                    double a = ((Expression) this.params[2]).evaluate();
                    double b = ((Expression) this.params[3]).evaluate();
                    double result = NumericalMethods.integrateBySimpson((Expression) ((Operator) expr).getParams()[0], (String) this.params[1], a, b, 1000);
                    double betterResult = NumericalMethods.integrateBySimpson((Expression) ((Operator) expr).getParams()[0], (String) this.params[1], a, b, 2000);
                    double almostPreciseResult = 16 / ((double) 15) * betterResult - 1 / ((double) 15) * result;
                    return almostPreciseResult;
                }

            }
            return expr.evaluate();

        }
        if (this.getType().equals(TypeOperator.laplace)) {
            return simplifyTrivialLaplace().evaluate();
        }
        if (this.getType().equals(TypeOperator.lcm)) {
            return simplifyTrivialLCM().evaluate();
        }
        if (this.getType().equals(TypeOperator.mod)) {
            return simplifyTrivialMod().evaluate();
        }
        if (this.getType().equals(TypeOperator.prod)) {
            return simplifyTrivialProd().evaluate();
        }
        if (this.getType().equals(TypeOperator.sum)) {
            return simplifyTrivialSum().evaluate();
        }
        if (this.getType().equals(TypeOperator.taylor)) {
            return simplifyTrivialTaylor().evaluate();
        }
        return 0;

    }

    @Override
    public Expression evaluate(HashSet<String> vars) throws EvaluationException {
        Object[] paramsEvaluated = new Object[this.params.length];

        /*
         Bei den Operator INT, PROD und SUM sollen die (lokalen) Variablen
         (Integrationsvariable/Index) NICHT ausgewertet werden; Daher die
         Strategie: Erst die lokale Variable aus vars entfernen (sofern
         vorhanden), dann evaluate() anwenden, dann wieder hinzufügen.
         */
        if (this.type.equals(TypeOperator.integral) || this.type.equals(TypeOperator.prod) || this.type.equals(TypeOperator.sum)) {

            String localVar = (String) this.params[1];
            if (vars.contains(localVar)) {
                vars.remove(localVar);
                for (int i = 0; i < this.params.length; i++) {
                    if (this.params[i] instanceof Expression) {
                        paramsEvaluated[i] = ((Expression) this.params[i]).evaluate(vars);
                    } else {
                        paramsEvaluated[i] = this.params[i];
                    }
                }
                vars.add(localVar);
                return new Operator(this.type, paramsEvaluated, this.precise);
            }

        }

        // Alle anderen Operatoren.
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                paramsEvaluated[i] = ((Expression) this.params[i]).evaluate(vars);
            } else {
                paramsEvaluated[i] = this.params[i];
            }
        }
        return new Operator(this.type, paramsEvaluated, this.precise);
    }

    @Override
    public void getContainedVars(HashSet<String> vars) {

        /*
         Bei bestimmter Integration/Summen/Produkten zählt die
         Integrationsvariable/der Index NICHT als vorkommende Variable.
         */
        if (this.type.equals(TypeOperator.integral) && this.params.length == 2) {
            ((Expression) this.params[0]).getContainedVars(vars);
            return;
        }
        if (this.type.equals(TypeOperator.integral) && this.params.length == 4) {
            String var = (String) this.params[1];
            ((Expression) this.params[0]).getContainedVars(vars);
            vars.remove(var);
            ((Expression) this.params[2]).getContainedVars(vars);
            ((Expression) this.params[3]).getContainedVars(vars);
            return;
        }
        if (this.type.equals(TypeOperator.prod) || this.type.equals(TypeOperator.sum)) {
            String index = (String) this.params[1];
            ((Expression) this.params[0]).getContainedVars(vars);
            vars.remove(index);
            ((Expression) this.params[2]).getContainedVars(vars);
            ((Expression) this.params[3]).getContainedVars(vars);
            return;
        }

        // Alle anderen möglichen Matrizenoperatoren
        for (Object param : this.params) {
            if (param instanceof Expression) {
                ((Expression) param).getContainedVars(vars);
            }
        }

    }

    @Override
    public boolean contains(String var) {

        if (this.type.equals(TypeOperator.integral) && this.params.length == 2) {
            return ((Expression) this.params[0]).contains(var);
        }

        // Im bestimmten Integral zählt die Integrationsvariable NICHT als vorkommende Veränderliche.
        if (this.type.equals(TypeOperator.integral) && this.params.length == 4) {
            return (((Expression) this.params[0]).contains(var) || ((Expression) this.params[2]).contains(var)
                    || ((Expression) this.params[3]).contains(var)) && !var.equals((String) this.params[1]);
        }

        // In der Summe oder im Produkt zählt der Index NICHT als vorkommende Veränderliche!
        if (this.type.equals(TypeOperator.prod) || this.type.equals(TypeOperator.sum)) {
            return (((Expression) this.params[0]).contains(var) || ((Expression) this.params[2]).contains(var)
                    || ((Expression) this.params[3]).contains(var)) && !((String) this.params[1]).equals(var);
        }

        boolean result = false;
        for (Object param : this.params) {
            if (param instanceof Expression) {
                result = result || ((Expression) param).contains(var);
            }
        }
        return result;

    }

    @Override
    public boolean containsApproximates() {
        boolean result = false;
        for (Object param : this.params) {
            if (param instanceof Expression) {
                result = result | ((Expression) param).containsApproximates();
            }
        }
        return result;
    }

    @Override
    public boolean containsFunction() {
        boolean result = false;
        for (Object param : this.params) {
            if (param instanceof Expression) {
                result = result || ((Expression) param).containsFunction();
            }
        }
        return result;
    }
    
    @Override
    public boolean containsExponentialFunction() {
        boolean result = false;
        for (Object param : this.params) {
            if (param instanceof Expression) {
                result = result || ((Expression) param).containsExponentialFunction();
            }
        }
        return result;
    }

    @Override
    public boolean containsTrigonometricalFunction() {
        boolean result = false;
        for (Object param : this.params) {
            if (param instanceof Expression) {
                result = result || ((Expression) param).containsTrigonometricalFunction();
            }
        }
        return result;
    }

    @Override
    public boolean containsIndefiniteIntegral() {
        return this.type.equals(TypeOperator.integral) && this.params.length == 2;
    }

    @Override
    public Expression turnToApproximate() {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).turnToApproximate();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, false);
    }

    @Override
    public Expression turnToPrecise() {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).turnToPrecise();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, true);
    }

    @Override
    public Expression replaceVariable(String var, Expression expr) {

        Object[] resultParams = new Object[this.params.length];

        /*
         Bei den Operator INT, PROD und SUM sollen die (lokalen) Variablen
         (Integrationsvariable/Index) NICHT ersetzt werden; überall sonst
         schon.
         */
        if (this.type.equals(TypeOperator.integral) || this.type.equals(TypeOperator.prod) || this.type.equals(TypeOperator.sum)) {
            if (!var.equals((String) this.params[1])) {

                for (int i = 0; i < this.params.length; i++) {
                    if (this.params[i] instanceof Expression) {
                        resultParams[i] = ((Expression) this.params[i]).replaceVariable(var, expr);
                    } else {
                        resultParams[i] = this.params[i];
                    }
                }
                return new Operator(this.type, resultParams, this.precise);

            } else {
                return this;
            }
        }

        // Alle anderen Operatoren.
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).replaceVariable(var, expr);
            } else {
                resultParams[i] = this.params[i];
            }
        }

        return new Operator(this.type, resultParams, this.precise);

    }

    @Override
    public Expression replaceSelfDefinedFunctionsByPredefinedFunctions() {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).replaceSelfDefinedFunctionsByPredefinedFunctions();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression diff(String var) throws EvaluationException {

        if (!this.contains(var)) {
            return Expression.ZERO;
        }

        if (this.type.equals(TypeOperator.integral)) {
            if (this.params.length == 2) {

                // Unbestimmte Integration.
                if (var.equals((String) this.params[1])) {
                    return (Expression) this.params[0];
                }

                Object[] resultParams = new Object[2];
                resultParams[0] = ((Expression) this.params[0]).diff(var);
                resultParams[1] = this.params[1];
                return new Operator(TypeOperator.integral, resultParams, this.precise);

            } else {

                /*
                 Bestimmte Integration. Hier Leibnis-Regel: (d/dt)
                 int_a(t)^b(t) f(x, t) dt = int_a(t)^b(t) (d/dt)f(x, t) dt +
                 f(b(t), t)*(d/dt)b(t) - f(a(t), t)*(d/dt)a(t).
                 */
                if (var.equals((String) this.params[1])) {
                    return Expression.ZERO;
                }

                Expression f = ((Expression) this.params[0]);
                Expression differentiatedIntegral;
                String integrationVar = (String) this.params[1];
                Expression lowerLimit = (Expression) this.params[2];
                Expression upperLimit = (Expression) this.params[3];

                Object[] resultParams = new Object[4];
                resultParams[0] = f.diff(var);
                resultParams[1] = this.params[1];
                resultParams[2] = this.params[2];
                resultParams[3] = this.params[3];
                differentiatedIntegral = new Operator(TypeOperator.integral, resultParams, this.precise);

                return differentiatedIntegral.add(f.replaceVariable(integrationVar, upperLimit).mult(upperLimit.diff(var))).sub(f.replaceVariable(integrationVar, lowerLimit).mult(lowerLimit.diff(var)));

            }
        }

        if (this.type.equals(TypeOperator.fac)) {
            if (!((Expression) this.params[0]).contains(var)) {
                return Expression.ZERO;
            }
        }

        if (this.type.equals(TypeOperator.prod)) {

            /*
             Zunächst wird die Summationsvariable für die äußere Summe
             ermittelt. Dies ist entweder k (falls k nicht schon vorher
             auftaucht), oder k_0, k_1, k_2, ...
             */
            String indexVarForSum = "k";
            int index = 0;
            while (((Expression) this.params[0]).contains(indexVarForSum + "_" + index)) {
                index++;
            }
            if (index == 0) {
                if (((Expression) this.params[0]).contains("k")) {
                    indexVarForSum = indexVarForSum + "_0";
                } else {
                    indexVarForSum = "k";
                }
            } else {
                indexVarForSum = indexVarForSum + "_" + index;
            }

            /*
             Nun: diff(prod(f(k, x), k, m, n), x) = sum(diff(f(var_for_sum,
             x), x)*prod(f(k, x)/f(var_for_sum, x), k, m, n), var_for_sum, m, n).
             */
            Expression f = (Expression) this.params[0];
            Object[] resultParams = new Object[4];
            resultParams[0] = f.replaceVariable((String) this.params[1], Variable.create(indexVarForSum)).diff(var).simplify().mult(
                    this).div(f.replaceVariable((String) this.params[1], Variable.create(indexVarForSum))).simplify();
            resultParams[1] = indexVarForSum;
            resultParams[2] = this.params[2];
            resultParams[3] = this.params[3];
            return new Operator(TypeOperator.sum, resultParams, this.precise);

        }

        if (this.type.equals(TypeOperator.sum)) {

            /*
             Nun: diff(sum(f(k, x), k, m, n), x) = sum(diff(f(k, x), x), k, m, n).
             */
            Object[] resultParams = new Object[4];
            resultParams[0] = ((Expression) this.params[0]).diff(var).simplify();
            resultParams[1] = this.params[1];
            resultParams[2] = this.params[2];
            resultParams[3] = this.params[3];
            return new Operator(TypeOperator.sum, resultParams, this.precise);

        }

        // Falls man die Ableitung nicht exakt angeben kann (etwa (x!)' etc.)
        throw new EvaluationException(Translator.translateExceptionMessage("EB_Operator_EXPRESSION_IS_NOT_DIFFERENTIABLE_1")
                + this.writeExpression()
                + Translator.translateExceptionMessage("EB_Operator_EXPRESSION_IS_NOT_DIFFERENTIABLE_2")
                + var
                + Translator.translateExceptionMessage("EB_Operator_EXPRESSION_IS_NOT_DIFFERENTIABLE_3"));

    }

    @Override
    public Expression diffDifferentialEquation(String var) throws EvaluationException {

        if (!this.contains(var)) {
            return Expression.ZERO;
        }

        if (this.type.equals(TypeOperator.integral)) {
            if (this.params.length == 2) {

                // Unbestimmte Integration.
                if (var.equals((String) this.params[1])) {
                    return (Expression) this.params[0];
                }

                Object[] resultParams = new Object[2];
                resultParams[0] = ((Expression) this.params[0]).diffDifferentialEquation(var);
                resultParams[1] = this.params[1];
                return new Operator(TypeOperator.integral, resultParams, this.precise);

            } else {

                /*
                 Bestimmte Integration. Hier Leibnis-Regel: (d/dt)
                 int_a(t)^b(t) f(x, t) dt = int_a(t)^b(t) (d/dt)f(x, t) dt +
                 f(b(t), t)*(d/dt)b(t) - f(a(t), t)*(d/dt)a(t).
                 */
                if (var.equals((String) this.params[1])) {
                    return Expression.ZERO;
                }

                Expression f = ((Expression) this.params[0]);
                Expression differentiatedIntegral;
                String integrationVar = (String) this.params[1];
                Expression lowerLimit = (Expression) this.params[2];
                Expression upperLimit = (Expression) this.params[3];

                Object[] resultParams = new Object[4];
                resultParams[0] = f.diffDifferentialEquation(var);
                resultParams[1] = this.params[1];
                resultParams[2] = this.params[2];
                resultParams[3] = this.params[3];
                differentiatedIntegral = new Operator(TypeOperator.integral, resultParams, this.precise);

                return differentiatedIntegral.add(f.replaceVariable(integrationVar, upperLimit).mult(upperLimit.diffDifferentialEquation(var))).sub(f.replaceVariable(integrationVar, lowerLimit).mult(lowerLimit.diffDifferentialEquation(var)));

            }
        }

        if (this.type.equals(TypeOperator.fac)) {
            if (!((Expression) this.params[0]).contains(var)) {
                return Expression.ZERO;
            }
        }

        if (this.type.equals(TypeOperator.prod)) {

            /*
             Zunächst wird die Summationsvariable für die äußere Summe
             ermittelt. Dies ist entweder k (falls k nicht schon vorher
             auftaucht), oder k_0, k_1, k_2, ...
             */
            String indexVarForSum = "k";
            int index = 0;
            while (((Expression) this.params[0]).contains(indexVarForSum + "_" + index)) {
                index++;
            }
            if (index == 0) {
                if (((Expression) this.params[0]).contains("k")) {
                    indexVarForSum = indexVarForSum + "_0";
                } else {
                    indexVarForSum = "k";
                }
            } else {
                indexVarForSum = indexVarForSum + "_" + index;
            }

            /*
             Nun: diff(prod(f(k, x), k, m, n), x) = sum(diff(f(var_for_sum,
             x), x)*prod(f(k, x)/f(var_for_sum, x), k, m, n), var_for_sum, m, n).
             */
            Expression f = (Expression) this.params[0];
            Object[] resultParams = new Object[4];
            resultParams[0] = f.replaceVariable((String) this.params[1], Variable.create(indexVarForSum)).diffDifferentialEquation(var).simplify().mult(
                    this).div(f.replaceVariable((String) this.params[1], Variable.create(indexVarForSum))).simplify();
            resultParams[1] = indexVarForSum;
            resultParams[2] = this.params[2];
            resultParams[3] = this.params[3];
            return new Operator(TypeOperator.sum, resultParams, this.precise);

        }

        if (this.type.equals(TypeOperator.sum)) {

            // Nun: diff(sum(f(k, x), k, m, n), x) = sum(diff(f(k, x), x), k, m, n).
            Object[] resultParams = new Object[4];
            resultParams[0] = ((Expression) this.params[0]).diffDifferentialEquation(var).simplify();
            resultParams[1] = this.params[1];
            resultParams[2] = this.params[2];
            resultParams[3] = this.params[3];
            return new Operator(TypeOperator.sum, resultParams, this.precise);

        }

        // Falls man die Ableitung nicht exakt angeben kann (etwa (x!)' etc.)
        throw new EvaluationException(Translator.translateExceptionMessage("EB_Operator_EXPRESSION_IS_NOT_DIFFERENTIABLE_1")
                + this.writeExpression()
                + Translator.translateExceptionMessage("EB_Operator_EXPRESSION_IS_NOT_DIFFERENTIABLE_2")
                + var
                + Translator.translateExceptionMessage("EB_Operator_EXPRESSION_IS_NOT_DIFFERENTIABLE_3"));

    }

    @Override
    public boolean isConstant() {

        if (this.type.equals(TypeOperator.integral)) {

            // Unbestimmte Integrale sind NIE konstant.
            if (this.params.length == 2) {
                return false;
            }
            // Bestimmte Integrale.
            HashSet<String> varsInParameters = new HashSet<>();
            ((Expression) this.params[0]).getContainedVars(varsInParameters);
            varsInParameters.remove((String) this.params[1]);
            ((Expression) this.params[2]).getContainedVars(varsInParameters);
            ((Expression) this.params[3]).getContainedVars(varsInParameters);

            return varsInParameters.isEmpty();

        }

        if (this.type.equals(TypeOperator.prod) || this.type.equals(TypeOperator.sum)) {

            HashSet<String> varsInParameters = new HashSet<>();
            ((Expression) this.params[0]).getContainedVars(varsInParameters);
            varsInParameters.remove((String) this.params[1]);
            ((Expression) this.params[2]).getContainedVars(varsInParameters);
            ((Expression) this.params[3]).getContainedVars(varsInParameters);
            return varsInParameters.isEmpty();

        }

        if (this.type.equals(TypeOperator.taylor)) {
            return ((Expression) this.params[0]).isConstant();
        }

        boolean result = true;
        for (Object param : this.params) {
            if (param instanceof Expression) {
                result = result && ((Expression) param).isConstant();
            }
        }

        return result;

    }

    @Override
    public boolean isNonNegative() {

        if (!this.isConstant()) {
            return false;
        }

        // Positivität für Fakultäten entscheiden.
        if (this.type.equals(TypeOperator.fac)) {
            return isFactorialOfExpressionPositive((Expression) this.params[0]);
        }

        // Falls der Operator konstant ist -> versuchen auszuwerten.
        try {
            return this.evaluate() >= 0;
        } catch (EvaluationException e) {
        }

        return false;

    }

    @Override
    public boolean isAlwaysNonNegative() {

        if (this.isNonNegative()) {
            return true;
        }
        if (this.type.equals(TypeOperator.gcd) || this.type.equals(TypeOperator.lcm)
                || this.type.equals(TypeOperator.mod)) {
            return true;
        }
        if (this.type.equals(TypeOperator.integral) && this.params.length == 4) {
            try {
                Expression difference = ((Expression) this.getParams()[3]).sub((Expression) this.getParams()[2]).simplify();
                return ((Expression) this.getParams()[0]).isAlwaysNonNegative() && difference.isAlwaysNonNegative();
            } catch (EvaluationException e) {
            }
        }
        return false;

    }

    @Override
    public boolean isAlwaysPositive() {

        if (this.isNonNegative() && !this.equals(ZERO)) {
            return true;
        }
        if (this.type.equals(TypeOperator.integral) && this.params.length == 4) {
            try {
                Expression difference = ((Expression) this.getParams()[3]).sub((Expression) this.getParams()[2]).simplify();
                return ((Expression) this.getParams()[0]).isAlwaysPositive() && difference.isAlwaysPositive();
            } catch (EvaluationException e) {
            }
        }
        return false;

    }

    /**
     * Hilfsmethode: Gibt zurück, ob die Fakultät von argument positiv ist,
     * sofern argument konstant ist. Ist argument nicht konstant, so wird false
     * zurückgegeben.
     */
    public static boolean isFactorialOfExpressionPositive(Expression argument) {

        if (!argument.isConstant()) {
            return false;
        }
        /*
         Es wird Folgendes getestet: fac(argument) >= 0 genau dann wenn x > -1
         bzw. -3 < x < -2 bzw -5 < x < -4 etc.
         */
        Expression argumentPlusOne;
        try {
            argumentPlusOne = argument.add(Expression.ONE).simplify();
        } catch (EvaluationException e) {
            return false;
        }

        if (argumentPlusOne.isNonNegative() && !argumentPlusOne.equals(Expression.ZERO)) {
            return true;
        } else {

            try {
                double value = argument.evaluate();
                int valueRoundedDown = (int) value;
                if (value == valueRoundedDown) {
                    /*
                     In diesem Fall ist die Fakultät sogar nicht definiert (da
                     das Argument eine negative ganze Zahl ist).
                     */
                    return false;
                }
                return (valueRoundedDown / 2) * 2 == valueRoundedDown;
            } catch (EvaluationException e) {
                /*
                 Hier wird zumindest Folgendes getestet: Ist argument eine
                 ganze Zahl oder ein Bruch, dann wie oben testen! Ansonsten
                 ungewiss -> false zurückgeben.
                 */
                if (argument.isIntegerConstantOrRationalConstant() && argumentPlusOne.isIntegerConstantOrRationalConstantNegative()) {
                    if (argument instanceof Constant) {
                        if (argument.isIntegerConstant()) {
                            /*
                             In diesem Fall ist die Fakultät sogar nicht
                             definiert (da das Argument eine negative ganze
                             Zahl ist).
                             */
                            return false;
                        }
                        BigInteger valueRoundedDown = ((Constant) argument).getValue().toBigInteger();
                        return valueRoundedDown.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0;
                    } else {
                        /*
                         Wichtig: Der Bruch ist bereits gekürzt (wegen
                         simplify()), also kann der Quotient nicht mehr
                         ganzzahlig sein.
                         */
                        BigDecimal enumerator = ((Constant) ((BinaryOperation) argument).getLeft()).getValue();
                        BigDecimal denominator = ((Constant) ((BinaryOperation) argument).getRight()).getValue();
                        BigInteger valueRoundedDown = enumerator.divide(denominator, BigDecimal.ROUND_HALF_UP).toBigInteger();
                        return valueRoundedDown.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0;
                    }
                }
            }

        }

        return false;

    }

    @Override
    public String writeExpression() {

        // Operator Fakultät wird mit als (...)! ausgeschrieben.
        if (this.type.equals(TypeOperator.fac)) {
            if ((Expression) this.params[0] instanceof Constant && ((Constant) this.params[0]).getValue().compareTo(BigDecimal.ZERO) >= 0) {
                return ((Expression) this.params[0]).writeExpression() + "!";
            }
            if ((Expression) this.params[0] instanceof Variable) {
                return ((Expression) this.params[0]).writeExpression() + "!";
            }
            return "(" + ((Expression) this.params[0]).writeExpression() + ")!";
        }

        String result = (String) getNameFromType(this.type) + "(";
        String parameter = "";

        for (Object param : this.params) {
            if (param instanceof Expression) {
                parameter = ((Expression) param).writeExpression();
            } else if (param instanceof String) {
                parameter = (String) param;
            } else if (param instanceof Double) {
                parameter = String.valueOf((double) param);
            } else if (param instanceof Integer) {
                parameter = String.valueOf((int) param);
            } else if (param instanceof BigDecimal) {
                parameter = String.valueOf((BigDecimal) param);
            }
            result = result + parameter + ",";
        }

        return result.substring(0, result.length() - 1) + ")";
    }

    @Override
    public String expressionToLatex() {

        if (this.type.equals(TypeOperator.diff)) {
            if (this.params.length == 3) {

                // Es wird zunächst geprüft, ob params[2] eine gültige Variable ist.
                if (this.params[2] instanceof String) {
                    String result = "\\left(" + ((Expression) this.params[0]).expressionToLatex() + "\\right)";
                    for (int i = 1; i < 3; i++) {
                        result = "\\frac{\\partial}{\\partial " + ((String) this.params[i]) + "}" + result;
                    }
                    return result;
                }
                // Andernfalls ist der Operator von der Form diff(EXPRESSION, VAR, ZAHL)
                return "\\frac{\\partial^{" + ((int) this.params[2]) + "}}{\\partial " + ((String) this.params[1]) + "^{" + ((int) this.params[2]) + "}}\\left("
                        + ((Expression) this.params[0]).expressionToLatex() + "\\right)";

            } else {
                String result = "\\left(" + ((Expression) this.params[0]).expressionToLatex() + "\\right)";
                for (int i = 1; i < this.params.length; i++) {
                    result = "\\frac{\\partial}{\\partial " + ((String) this.params[i]) + "}" + result;
                }
                return result;
            }
        } else if (this.type.equals(TypeOperator.div)) {
            return "\\div\\left(" + ((Expression) this.params[0]).expressionToLatex() + "\\right)";
        } else if (this.type.equals(TypeOperator.gcd)) {
            String result = "\\gcd(";
            for (int i = 0; i < this.params.length - 1; i++) {
                result = result + ((Expression) this.params[i]).expressionToLatex() + ",";
            }
            result = result + ((Expression) this.params[this.params.length - 1]).expressionToLatex() + ")";
            return result;
        } else if (this.type.equals(TypeOperator.integral)) {

            String var = (String) this.params[1];
            if (this.params.length == 2) {
                return "\\int " + ((Expression) this.params[0]).expressionToLatex() + "\\ d" + var;
            } else {
                return "\\int_{" + ((Expression) this.params[2]).expressionToLatex() + "}^{" + ((Expression) this.params[3]).expressionToLatex()
                        + "} " + ((Expression) this.params[0]).expressionToLatex() + "\\ d" + var;
            }

        } else if (this.type.equals(TypeOperator.laplace)) {
            return "\\Delta\\left(" + ((Expression) this.params[0]).expressionToLatex() + "\\right)";
        } else if (this.type.equals(TypeOperator.lcm)) {
            String result = "\\lcm(";
            for (int i = 0; i < this.params.length - 1; i++) {
                result = result + ((Expression) this.params[i]).expressionToLatex() + ",";
            }
            result = result + ((Expression) this.params[this.params.length - 1]).expressionToLatex() + ")";
            return result;
        } else if (this.type.equals(TypeOperator.prod)) {
            return "\\prod_{" + ((String) this.params[1]) + " = " + ((Expression) this.params[2]).expressionToLatex() + "}^{"
                    + ((Expression) this.params[3]).expressionToLatex() + "}\\left("
                    + ((Expression) this.params[0]).expressionToLatex() + "\\right)";
        } else if (this.type.equals(TypeOperator.sum)) {
            return "\\sum_{" + ((String) this.params[1]) + " = " + ((Expression) this.params[2]).expressionToLatex() + "}^{"
                    + ((Expression) this.params[3]).expressionToLatex() + "}\\left("
                    + ((Expression) this.params[0]).expressionToLatex() + "\\right)";
        } else {
            // In diesem Fall ist es das Taylorpolynom
            return "T_{" + ((String) this.params[1]) + "," + ((Expression) this.params[2]).expressionToLatex() + "," + ((int) this.params[3]) + "}\\left("
                    + ((Expression) this.params[0]).expressionToLatex() + "\\right)";
        }
    }

    @Override
    public boolean equals(Expression expr) {

        if (expr instanceof Operator) {

            Operator operator = (Operator) expr;
            boolean result = this.type.equals(operator.type) && (this.params.length == operator.params.length);
            if (!result) {
                return false;
            }

            for (int i = 0; i < this.params.length; i++) {
                if (this.params[i] instanceof Expression) {
                    result = result && (((Expression) this.params[i]).equals((Expression) operator.params[i]));
                } else {
                    result = result && (this.params[i].equals(operator.params[i]));
                }
            }

            return result;

        }
        return false;

    }

    @Override
    public boolean equivalent(Expression expr) {

        if (expr instanceof Operator) {

            Operator operator = (Operator) expr;
            boolean result = (this.type.equals(operator.type) & (this.params.length == operator.params.length));
            if (!result) {
                return false;
            }

            for (int i = 0; i < this.params.length; i++) {
                if (this.params[i] instanceof Expression) {
                    result = result && (((Expression) this.params[i]).equivalent((Expression) operator.params[i]));
                } else {
                    result = result && (this.params[i].equals(operator.params[i]));
                }
            }
            return result;

        }
        return false;

    }

    @Override
    public boolean hasPositiveSign() {
        return true;
    }

    @Override
    public Expression orderSumsAndProducts() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).orderSumsAndProducts();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression orderDifferenceAndDivision() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).orderDifferenceAndDivision();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression collectProducts() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).collectProducts();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression factorizeInSums() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).factorizeInSums();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression factorizeInDifferences() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).factorizeInDifferences();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression factorizeRationalsInSums() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).factorizeRationalsInSums();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression factorizeRationalsInDifferences() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).factorizeRationalsInDifferences();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression reduceQuotients() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).reduceQuotients();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression expandRationalFactors() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).expandRationalFactors();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression expand() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).expand();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression reduceLeadingsCoefficients() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).reduceLeadingsCoefficients();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyTrivial() throws EvaluationException {

        // Zunächst alle Parameter, welche gültige Ausdrücke darstellen, vereinfachen.
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyTrivial();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        Operator operator = new Operator(this.type, resultParams, this.precise);

        // Nun wird noch operatorspezifisch vereinfacht.
        if (this.type.equals(TypeOperator.diff)) {
            return operator.simplifyTrivialDiff();
        }
        if (this.type.equals(TypeOperator.div)) {
            return operator.simplifyTrivialDiv();
        }
        if (this.type.equals(TypeOperator.fac)) {
            return operator.simplifyTrivialFac();
        }
        if (this.type.equals(TypeOperator.gcd)) {
            return operator.simplifyTrivialGCD();
        }
        if (this.type.equals(TypeOperator.integral)) {
            return operator.simplifyTrivialInt();
        }
        if (this.type.equals(TypeOperator.laplace)) {
            return operator.simplifyTrivialLaplace();
        }
        if (this.type.equals(TypeOperator.lcm)) {
            return operator.simplifyTrivialLCM();
        }
        if (this.type.equals(TypeOperator.mod)) {
            return operator.simplifyTrivialMod();
        }
        if (this.type.equals(TypeOperator.prod)) {
            return operator.simplifyTrivialProd();
        }
        if (this.type.equals(TypeOperator.sum)) {
            return operator.simplifyTrivialSum();
        }
        if (this.type.equals(TypeOperator.taylor)) {
            return operator.simplifyTrivialTaylor();
        }

        // Kommt nicht vor, aber trotzdem;
        return operator;

    }

    /**
     * Vereinfacht den Ableitungsoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private Expression simplifyTrivialDiff() throws EvaluationException {

        if (!this.type.equals(TypeOperator.fac)) {
            Expression expr = (Expression) this.params[0];
            if (this.params.length == 2) {
                expr = expr.diff((String) this.params[1]);
                expr = expr.simplify();
                return expr;
            } else {
                /*
                 Es wird zunächst geprüft, ob alle übrigen Parameter gültige
                 Variablen sind, oder ob der dritte Parameter eine ganze Zahl
                 ist.
                 */
                if (this.params[2] instanceof String) {
                    for (int i = 1; i < this.params.length; i++) {
                        expr = expr.diff((String) this.params[i]);
                        expr = expr.simplify();
                    }
                } else {
                    int k = (int) this.params[2];
                    for (int i = 0; i < k; i++) {
                        expr = expr.diff((String) this.params[1]);
                        expr = expr.simplify();
                    }
                }
                return expr;
            }
        } else if (this.type.equals(TypeOperator.fac) && ((Expression) this.params[0]).isConstant()) {
            return Expression.ZERO;
        }
        return this;

    }

    /**
     * Vereinfacht den Divergenzoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private Expression simplifyTrivialDiv() throws EvaluationException {

        Expression result = Expression.ZERO;
        Expression expr = (Expression) this.params[0];
        HashSet<String> vars = new HashSet<>();
        expr.getContainedVars(vars);
        Iterator iter = vars.iterator();
        String var;

        for (int i = 0; i < vars.size(); i++) {
            var = (String) iter.next();
            if (result.equals(Expression.ZERO)) {
                result = expr.diff(var);
            } else {
                result = result.add(expr.diff(var));
            }
        }

        return result;

    }

    /**
     * Vereinfacht den Fakultätoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private Expression simplifyTrivialFac() throws EvaluationException {

        Expression argument = ((Expression) this.params[0]).simplify();

        if (argument.isRationalConstant() && ((BinaryOperation) argument).getRight().equals(Expression.TWO)) {
            /*
             (n+1/2)! lässt sich explizit angeben. Dieser Wert wird nur für n
             <= einer bestimmten Schranke angegeben.
             */
            BigDecimal argumentEnumerator = ((Constant) ((BinaryOperation) argument).getLeft()).getValue();
            if (argumentEnumerator.equals(argumentEnumerator.setScale(0, BigDecimal.ROUND_HALF_UP)) && argumentEnumerator.abs().compareTo(BigDecimal.valueOf(ComputationBounds.getBound("Bound_FACTORIAL_WITH_DENOMINATOR_TWO"))) <= 0) {
                BigInteger argumentEnumeratorAsBigInteger = argumentEnumerator.toBigInteger();
                if (argumentEnumeratorAsBigInteger.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ONE) == 0) {

                    BigDecimal resultEnumerator = BigDecimal.ONE;
                    BigDecimal resultDenominator = BigDecimal.ONE;
                    if (argumentEnumeratorAsBigInteger.intValue() >= -1) {
                        for (int i = 0; i < (argumentEnumeratorAsBigInteger.intValue() + 1) / 2; i++) {
                            resultEnumerator = resultEnumerator.multiply(BigDecimal.valueOf(2 * i + 1));
                            resultDenominator = resultDenominator.multiply(BigDecimal.valueOf(2));
                        }
                        return (new Constant(resultEnumerator)).mult(Expression.PI.pow(Expression.ONE.div(Expression.TWO))).div(new Constant(resultDenominator));
                    } else {
                        for (int i = 0; i < (-argumentEnumeratorAsBigInteger.intValue() - 1) / 2; i++) {
                            resultEnumerator = resultEnumerator.multiply(BigDecimal.valueOf(-2));
                            resultDenominator = resultDenominator.multiply(BigDecimal.valueOf(2 * i + 1));
                        }
                        return (new Constant(resultEnumerator)).mult(Expression.PI.pow(Expression.ONE.div(Expression.TWO))).div(new Constant(resultDenominator));
                    }

                }
            }
        }

        BigInteger argumentRoundedDown;

        if (argument.isIntegerConstant()) {
            argumentRoundedDown = ((Constant) argument).getValue().toBigInteger();
            // Nur Fakultäten mit Argument <= einer bestimmten Schranke werden explizit ausgeben.
            if (argumentRoundedDown.compareTo(BigInteger.ZERO) < 0) {
                throw new EvaluationException(Translator.translateExceptionMessage("EB_Operator_FACULTIES_OF_NEGATIVE_INTEGERS_UNDEFINED"));
            }
            if (argumentRoundedDown.compareTo(BigInteger.valueOf(ComputationBounds.getBound("Bound_INTEGER_FACTORIAL"))) <= 0) {
                Constant result = new Constant(ArithmeticMethods.factorial(argumentRoundedDown.intValue()));
                result.setPrecise(this.precise);
                return result;
            }
            return this;
        } else {
            if (this.precise) {
                return this;
            }
            return new Constant(AnalysisMethods.Gamma(((Expression) this.params[0]).evaluate() + 1));
        }

    }

    /**
     * Vereinfacht den gcd-Operator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private Expression simplifyTrivialGCD() throws EvaluationException {

        Expression[] arguments = new Expression[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            arguments[i] = ((Expression) this.params[i]).simplify();
        }

        BigInteger[] argumentsAsBigInteger = new BigInteger[this.params.length];
        for (int i = 0; i < argumentsAsBigInteger.length; i++) {
            if (arguments[i].isIntegerConstant()) {
                argumentsAsBigInteger[i] = ((Constant) arguments[i]).getValue().toBigInteger();
            } else {
                throw new EvaluationException(Translator.translateExceptionMessage("EB_Operator_GENERAL_PARAMETER_IN_GCD_IS_NOT_INTEGER_1")
                        + (i + 1)
                        + Translator.translateExceptionMessage("EB_Operator_GENERAL_PARAMETER_IN_GCD_IS_NOT_INTEGER_2"));
            }
        }

        Constant result = new Constant(ArithmeticMethods.gcd(argumentsAsBigInteger));
        result.setPrecise(this.precise);
        return result;

    }

    /**
     * Vereinfacht den Integraloperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private Expression simplifyTrivialInt() throws EvaluationException {

        if (this.params.length == 2) {
            Object result = SimplifyIntegralMethods.indefiniteIntegration(this, true);
            if (result instanceof Expression) {
                return (Expression) result;
            }
        }

        if (this.params.length == 4) {

            // Hier wird versucht, explizit zu integrieren.
            Object result = SimplifyIntegralMethods.definiteIntegration(this);
            if (result instanceof Expression && this.precise) {
                /*
                 Nur im exakten (nichtapproximativen) Fall das
                 Urpsrungsintegral wieder zurückgeben, falls es nicht
                 berechnet werden konnte.
                 */
                return (Expression) result;
            }
        }

        if (!this.precise && this.params.length == 4) {

            // Falls das Integral keine Parameter enthält, kann es direkt ausgerechnet werden.
            if (((Expression) this.params[2]).isConstant() && ((Expression) this.params[3]).isConstant()) {
                Expression expr = (Expression) this.params[0];
                HashSet<String> varsInIntegrand = new HashSet<>();
                expr.getContainedVars(varsInIntegrand);
                if (varsInIntegrand.isEmpty() || (varsInIntegrand.size() == 1 && varsInIntegrand.contains((String) params[1]))) {
                    // Falls keine Parameter im Integranden auftauchen -> Integral approximativ berechnen.
                    double lowerLimit = ((Expression) params[2]).evaluate();
                    double upperLimit = ((Expression) params[3]).evaluate();
                    double result = NumericalMethods.integrateBySimpson(expr, (String) params[1], lowerLimit, upperLimit, 1000);
                    double betterResult = NumericalMethods.integrateBySimpson(expr, (String) params[1], lowerLimit, upperLimit, 2000);
                    double almostPreciseResult = 16 / ((double) 15) * betterResult - 1 / ((double) 15) * result;
                    return new Constant(almostPreciseResult);
                }
            }

        }

        return this;

    }

    /**
     * Vereinfacht den Laplaceoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private Expression simplifyTrivialLaplace() throws EvaluationException {

        Expression result = Expression.ZERO;
        Expression expr = (Expression) this.params[0];
        HashSet<String> vars = new HashSet<>();
        expr.getContainedVars(vars);
        Iterator iter = vars.iterator();
        String var;

        for (int i = 0; i < vars.size(); i++) {
            var = (String) iter.next();
            if (result.equals(Expression.ZERO)) {
                result = expr.diff(var).simplify().diff(var).simplify();
            } else {
                result = result.add(expr.diff(var).simplify().diff(var).simplify());
            }
        }
        return result;

    }

    /**
     * Vereinfacht den lcm-Operator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private Expression simplifyTrivialLCM() throws EvaluationException {

        Expression[] arguments = new Expression[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            arguments[i] = ((Expression) this.params[i]).simplify();
        }

        BigInteger[] argumentsAsBigInteger = new BigInteger[this.params.length];
        for (int i = 0; i < argumentsAsBigInteger.length; i++) {
            if (arguments[i].isIntegerConstant()) {
                argumentsAsBigInteger[i] = ((Constant) arguments[i]).getValue().toBigInteger();
            } else {
                throw new EvaluationException(Translator.translateExceptionMessage("EB_Operator_GENERAL_PARAMETER_IN_LCM_IS_NOT_INTEGER_1")
                        + (i + 1)
                        + Translator.translateExceptionMessage("EB_Operator_GENERAL_PARAMETER_IN_LCM_IS_NOT_INTEGER_2"));
            }
        }

        Constant result = new Constant(ArithmeticMethods.lcm(argumentsAsBigInteger));
        result.setPrecise(this.precise);
        return result;

    }

    /**
     * Vereinfacht den mod-Operator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private Expression simplifyTrivialMod() throws EvaluationException {

        Expression[] arguments = new Expression[2];
        for (int i = 0; i < 2; i++) {
            arguments[i] = ((Expression) this.params[i]).simplify();
        }

        BigInteger[] argumentsAsBigInteger = new BigInteger[2];
        for (int i = 0; i < 2; i++) {
            if (arguments[i].isIntegerConstant()) {
                argumentsAsBigInteger[i] = ((Constant) arguments[i]).getValue().toBigInteger();
            } else {
                throw new EvaluationException(Translator.translateExceptionMessage("EB_Operator_GENERAL_PARAMETER_IN_MOD_IS_NOT_INTEGER_1")
                        + (i + 1)
                        + Translator.translateExceptionMessage("EB_Operator_GENERAL_PARAMETER_IN_MOD_IS_NOT_INTEGER_2"));
            }
        }

        Constant result = new Constant(ArithmeticMethods.mod(argumentsAsBigInteger[0], argumentsAsBigInteger[1]));
        result.setPrecise(this.precise);
        return result;

    }

    /**
     * Vereinfacht den Produktoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private Expression simplifyTrivialProd() throws EvaluationException {

        Expression factor = (Expression) this.params[0];

        if (((Expression) this.params[2]).isIntegerConstant() && ((Expression) this.params[3]).isIntegerConstant()) {
            BigInteger lowerLimit = ((Constant) ((Expression) this.params[2])).getValue().toBigInteger();
            BigInteger upperLimit = ((Constant) ((Expression) this.params[3])).getValue().toBigInteger();
            if (lowerLimit.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0 && lowerLimit.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0
                    && upperLimit.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0 && upperLimit.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0) {

                // Dann kann man die Summe explizit ausschreiben.
                return AnalysisMethods.prod(factor, (String) this.params[1], lowerLimit.intValue(), upperLimit.intValue());

            }
        }

        // Falls die Faktoren von der Indexvariable nicht abhängen.
        if (!factor.contains((String) this.params[1])) {
            return factor.pow(((Expression) this.params[3]).add(Expression.ONE).sub((Expression) this.params[2]));
        }

        // Produkte von Produkten oder Quotienten aufteilen.
        if (factor.isProduct() || factor.isQuotient()) {
            return SimplifyOperatorMethods.splitProductsOfProductsOrQuotients((BinaryOperation) factor, (String) this.params[1],
                    (Expression) this.params[2], (Expression) this.params[3]);
        }

        // Konstante (rationale) Potenzen herausziehen mit ungeradem Nenner.
        if (factor.isPower() && !((BinaryOperation) factor).getRight().contains((String) this.params[1])) {
            return SimplifyOperatorMethods.takeConstantExponentsOutOfProducts((BinaryOperation) factor, (String) this.params[1],
                    (Expression) this.params[2], (Expression) this.params[3]);
        }

        // Falls die Faktoren Potenzen sind, wo die Basis von der Indexvariablen nicht abhängen.
        if (factor.isPower() && !((BinaryOperation) factor).getLeft().contains((String) this.params[1])) {
            return SimplifyOperatorMethods.simplifyProductWithConstantBase((BinaryOperation) factor, (String) this.params[1],
                    (Expression) this.params[2], (Expression) this.params[3]);
        }

        // Sonstiger Fall.
        Object[] resultParams = new Object[4];
        resultParams[0] = factor.simplify();
        resultParams[1] = this.params[1];
        resultParams[2] = this.params[2];
        resultParams[3] = this.params[3];
        return new Operator(TypeOperator.prod, resultParams, this.precise);

    }

    /**
     * Vereinfacht den Summenoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private Expression simplifyTrivialSum() throws EvaluationException {

        Expression summand = (Expression) this.params[0];

        if (((Expression) this.params[2]).isIntegerConstant() && ((Expression) this.params[3]).isIntegerConstant()) {
            BigInteger lowerLimit = ((Constant) ((Expression) this.params[2])).getValue().toBigInteger();
            BigInteger upperLimit = ((Constant) ((Expression) this.params[3])).getValue().toBigInteger();
            if (lowerLimit.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0 && lowerLimit.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0
                    && upperLimit.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0 && upperLimit.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0) {

                // Dann kann man die Summe explizit ausschreiben.
                return AnalysisMethods.sum(summand, (String) this.params[1], lowerLimit.intValue(), upperLimit.intValue());

            }
        }

        // Falls die Summanden von der Indexvariable nicht abhängen.
        if (!summand.contains((String) this.params[1])) {
            return ((Expression) this.params[3]).add(Expression.ONE).sub((Expression) this.params[2]).mult(summand);
        }

        // Summen von Summen oder Differenzen aufteilen.
        if (summand.isSum() || summand.isDifference()) {
            return SimplifyOperatorMethods.splitSumOfSumsOrDifferences((BinaryOperation) summand, (String) this.params[1],
                    (Expression) this.params[2], (Expression) this.params[3]);
        }

        // Konstante Faktoren im Zähler und Nenner herausziehen.
        if (summand.isProduct() || summand.isQuotient()) {
            return SimplifyOperatorMethods.takeConstantsOutOfSums((BinaryOperation) summand, (String) this.params[1],
                    (Expression) this.params[2], (Expression) this.params[3]);
        }

        // Sonstiger Fall (wo man nichts machen kann).
        Object[] resultParams = new Object[4];
        resultParams[0] = summand.simplify();
        resultParams[1] = this.params[1];
        resultParams[2] = this.params[2];
        resultParams[3] = this.params[3];
        return new Operator(TypeOperator.sum, resultParams, this.precise);

    }

    /**
     * Vereinfacht den Tayloroperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private Expression simplifyTrivialTaylor() throws EvaluationException {

        Expression f = (Expression) this.params[0];
        Expression centerPoint = (Expression) this.params[2];
        int degree = (int) this.params[3];
        return AnalysisMethods.getTaylorPolynomial(f, (String) this.params[1], centerPoint, degree);

    }

    @Override
    public Expression simplifyFunctionalRelations() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyFunctionalRelations();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyPolynomials() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyPolynomials();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyCollectLogarithms() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyCollectLogarithms();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyExpandLogarithms() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyExpandLogarithms();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyPowers() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyPowers();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression multiplyPowers() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).multiplyPowers();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyReplaceExponentialFunctionsByDefinitions() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyReplaceExponentialFunctionsByDefinitions();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyReplaceTrigonometricalFunctionsByDefinitions() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyReplaceTrigonometricalFunctionsByDefinitions();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyAlgebraicExpressions() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyAlgebraicExpressions();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

}

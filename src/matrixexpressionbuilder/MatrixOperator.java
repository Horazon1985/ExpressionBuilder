package matrixexpressionbuilder;

import computation.AnalysisMethods;
import expressionbuilder.Constant;
import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.isValidVariable;
import expressionbuilder.ExpressionException;
import expressionbuilder.Operator;
import expressionbuilder.TypeOperator;
import expressionbuilder.TypeSimplify;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import matrixsimplifymethods.SimplifyMatrixOperatorMethods;
import translator.Translator;

public class MatrixOperator extends MatrixExpression {

    private TypeMatrixOperator type;
    private Object[] params;
    private boolean precise;

    public MatrixOperator(TypeMatrixOperator type, Object[] params) {
        this.type = type;
        this.params = params;
        this.precise = true;
    }

    public MatrixOperator(TypeMatrixOperator type, Object[] params, boolean precise) {
        this.type = type;
        this.params = params;
        this.precise = precise;
    }

    //Getter und Setter
    //Getter
    public TypeMatrixOperator getType() {
        return this.type;
    }

    public Object[] getParams() {
        return this.params;
    }

    public boolean getPrecise() {
        return this.precise;
    }

    //Setter
    public void setType(TypeMatrixOperator typeMatrixOperator) {
        this.type = typeMatrixOperator;
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
    public static TypeMatrixOperator getTypeFromName(String operator) {
        if (operator.equals("int")) {
            return TypeMatrixOperator.integral;
        }
        return TypeMatrixOperator.valueOf(operator);
    }

    /**
     * Gibt den Namen des Operators zurück, der in der Konsole eingegeben werden
     * muss bzw. der bei der Ausgabe erscheint.
     */
    public static String getNameFromType(TypeMatrixOperator type) {
        if (type.equals(TypeMatrixOperator.integral)) {
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
    public static MatrixOperator getOperator(String operator, String[] params, HashSet<String> vars) throws ExpressionException {

        TypeMatrixOperator type = getTypeFromName(operator);

        Object[] resultMatrixOperatorParams;

        // DIFFERENTIALOPERATOR
        if (type.equals(TypeMatrixOperator.diff)) {
            if (params.length < 2) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_NOT_ENOUGH_PARAMETERS_IN_DIFF"));
            }

            if (params.length == 3) {

                try {
                    MatrixExpression.build(params[0], vars);
                } catch (NumberFormatException e) {
                    throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_1_PARAMETER_IN_DIFF_IS_WRONG") + e.getMessage());
                }

                if (!isValidVariable(params[1])) {
                    throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_2_PARAMETER_IN_DIFF_IS_INVALID"));
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
                    throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_3_PARAMETER_IN_DIFF_IS_INVALID"));
                }

                if (!isValidVariable(params[2])) {
                    int n = Integer.parseInt(params[2]);
                    if (n < 0) {
                        throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_3_PARAMETER_IN_DIFF_IS_INVALID"));
                    }
                }

                if (!isValidVariable(params[2])) {
                    resultMatrixOperatorParams = new Object[3];
                    resultMatrixOperatorParams[0] = MatrixExpression.build(params[0], vars);
                    resultMatrixOperatorParams[1] = params[1];
                    resultMatrixOperatorParams[2] = Integer.parseInt(params[2]);
                    return new MatrixOperator(type, resultMatrixOperatorParams);
                } else {
                    resultMatrixOperatorParams = new Object[3];
                    resultMatrixOperatorParams[0] = MatrixExpression.build(params[0], vars);
                    resultMatrixOperatorParams[1] = params[1];
                    resultMatrixOperatorParams[2] = params[2];
                    return new MatrixOperator(type, resultMatrixOperatorParams);
                }

            } else {

                try {
                    MatrixExpression.build(params[0], vars);
                } catch (ExpressionException e) {
                    throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_1_PARAMETER_IN_DIFF_IS_WRONG") + e.getMessage());
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
                    throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_GENERAL_PARAMETER_IN_DIFF_IS_INVALID_1")
                            + String.valueOf(indexOfInvalidVariable + 1)
                            + Translator.translateExceptionMessage("MEB_Operator_GENERAL_PARAMETER_IN_DIFF_IS_INVALID_2"));
                }

                if (allVariablesAreValid) {
                    resultMatrixOperatorParams = new Object[params.length];
                    resultMatrixOperatorParams[0] = MatrixExpression.build(params[0], vars);
                    System.arraycopy(params, 1, resultMatrixOperatorParams, 1, params.length - 1);
                    return new MatrixOperator(type, resultMatrixOperatorParams);
                }

            }
        } else if (type.equals(TypeMatrixOperator.div)) {
            // DIVERGENZ
            if (params.length != 1) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_WRONG_NUMBER_OF_PARAMETERS_IN_DIV"));
            }

            try {
                MatrixExpression.build(params[0], vars);
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_PARAMETER_IN_DIV_IS_INVALID") + e.getMessage());
            }

            resultMatrixOperatorParams = new Object[1];
            resultMatrixOperatorParams[0] = MatrixExpression.build(params[0], vars);
            return new MatrixOperator(type, resultMatrixOperatorParams);
        } else if (type.equals(TypeMatrixOperator.grad)) {
            // GRADIENT
            if (params.length < 2) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_WRONG_NUMBER_OF_PARAMETERS_IN_GRAD"));
            }

            try {
                resultMatrixOperatorParams = new Object[params.length];
                resultMatrixOperatorParams[0] = MatrixExpression.build(params[0], vars);
                Dimension dim = ((MatrixExpression) resultMatrixOperatorParams[0]).getDimension();
                if (dim.height != 1 || dim.width != 1) {
                    throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_FIRST_PARAMETER_IN_GRAD_IS_INVALID_1")
                            + dim.height + Translator.translateExceptionMessage("MEB_Operator_FIRST_PARAMETER_IN_GRAD_IS_INVALID_2")
                            + dim.width + Translator.translateExceptionMessage("MEB_Operator_FIRST_PARAMETER_IN_GRAD_IS_INVALID_3"));
                }
            } catch (ExpressionException | EvaluationException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_FIRST_PARAMETER_IN_GRAD_IS_INVALID") + e.getMessage());
            }

            HashSet<String> varsInParams = new HashSet<>();
            for (int i = 1; i < params.length; i++) {
                if (!isValidVariable(params[i])) {
                    throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_GENERAL_PARAMETER_IN_GRAD_IS_INVALID_1")
                            + i + Translator.translateExceptionMessage("MEB_Operator_GENERAL_PARAMETER_IN_GRAD_IS_INVALID_2"));
                }
                if (varsInParams.contains(params[i])) {
                    throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_PARAMETER_IN_GRAD_REPEATED"));
                }
                varsInParams.add(params[i]);
                resultMatrixOperatorParams[i] = params[i];
            }
            return new MatrixOperator(type, resultMatrixOperatorParams);
        } else if (type.equals(TypeMatrixOperator.integral)) {
            // INTEGRAL
            if (params.length != 2 && params.length != 4) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_WRONG_NUMBER_OF_PARAMETERS_PARAMETER_IN_INT"));
            }

            HashSet<String> varsInIntegrand = new HashSet<>();
            MatrixExpression integrand;
            try {
                integrand = MatrixExpression.build(params[0], vars);
                // Dies dient dazu, die Variablen im Integranden zu bestimmen.
                integrand.getContainedVars(varsInIntegrand);
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_1_PARAMETER_IN_INT_IS_INVALID") + e.getMessage());
            }

            String intVar = params[1];
            if (!Expression.isValidDerivateOfVariable(intVar)) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_2_PARAMETER_IN_INT_IS_INVALID"));
            }

            if (params.length == 2) {
                resultMatrixOperatorParams = new Object[2];
                resultMatrixOperatorParams[0] = integrand;
                resultMatrixOperatorParams[1] = params[1];
                return new MatrixOperator(type, resultMatrixOperatorParams);
            }

            HashSet<String> varsInIntegrationLimit = new HashSet<>();
            Expression lowerLimit, upperLimit;
            try {
                lowerLimit = Expression.build(params[2], varsInIntegrationLimit);
                if (varsInIntegrationLimit.contains(intVar)) {
                    throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_3_PARAMETER_IN_INT_IS_INVALID"));
                }
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_3_PARAMETER_IN_INT_IS_INVALID"));
            }

            try {
                upperLimit = Expression.build(params[3], varsInIntegrationLimit);
                if (varsInIntegrationLimit.contains(intVar)) {
                    throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_4_PARAMETER_IN_INT_IS_INVALID"));
                }
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_4_PARAMETER_IN_INT_IS_INVALID"));
            }

            resultMatrixOperatorParams = new Object[4];
            resultMatrixOperatorParams[0] = integrand;
            resultMatrixOperatorParams[1] = params[1];
            resultMatrixOperatorParams[2] = lowerLimit;
            resultMatrixOperatorParams[3] = upperLimit;
            return new MatrixOperator(type, resultMatrixOperatorParams);
        } else if (type.equals(TypeMatrixOperator.laplace)) {
            // LAPLACE-OPERATOR
            if (params.length != 1) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_WRONG_NUMBER_OF_PARAMETERS_IN_LAPLACE"));
            }

            try {
                MatrixExpression.build(params[0], vars);
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_PARAMETER_IN_LAPLACE_IS_INVALID") + e.getMessage());
            }

            resultMatrixOperatorParams = new Object[1];
            resultMatrixOperatorParams[0] = MatrixExpression.build(params[0], vars);
            return new MatrixOperator(type, resultMatrixOperatorParams);
        } else if (type.equals(TypeMatrixOperator.prod)) {
            // PRODUKT
            if (params.length != 4) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_WRONG_NUMBER_OF_PARAMETERS_IN_PROD"));
            }

            MatrixExpression factor;
            Expression lowerLimit, upperLimit;
            try {
                factor = MatrixExpression.build(params[0], vars);
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_1_PARAMETER_IN_PROD_IS_INVALID") + e.getMessage());
            }

            if (!isValidVariable(params[1])) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_2_PARAMETER_IN_PROD_IS_INVALID"));
            }

            try {
                lowerLimit = Expression.build(params[2], vars);
                if (!lowerLimit.isIntegerConstant() && lowerLimit.isConstant()) {
                    throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_1")
                            + 3
                            + Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_2"));
                }
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_1")
                        + 3
                        + Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_2"));
            }

            try {
                upperLimit = Expression.build(params[3], vars);
                if (!upperLimit.isIntegerConstant() && upperLimit.isConstant()) {
                    throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_1")
                            + 4
                            + Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_2"));
                }
            } catch (ExpressionException e) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_1")
                        + 4
                        + Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_PROD_2"));
            }

            if (lowerLimit.contains(params[1]) || upperLimit.contains(params[1])) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_BOUNDS_IN_PROD_CANNOT_CONTAIN_INDEX_VARIABLE"));
            }

            resultMatrixOperatorParams = new Object[4];
            resultMatrixOperatorParams[0] = factor;
            resultMatrixOperatorParams[1] = params[1];
            resultMatrixOperatorParams[2] = lowerLimit;
            resultMatrixOperatorParams[3] = upperLimit;
            return new MatrixOperator(type, resultMatrixOperatorParams);
        }

        // SUMME
        if (params.length != 4) {
            throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_WRONG_NUMBER_OF_PARAMETERS_IN_SUM"));
        }

        MatrixExpression summand;
        Expression lowerLimit, upperLimit;
        try {
            summand = MatrixExpression.build(params[0], vars);
        } catch (ExpressionException e) {
            throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_1_PARAMETER_IN_SUM_IS_INVALID") + e.getMessage());
        }

        if (!isValidVariable(params[1])) {
            throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_2_PARAMETER_IN_SUM_IS_INVALID"));
        }

        try {
            lowerLimit = Expression.build(params[2], vars);
            if (!lowerLimit.isIntegerConstant() && lowerLimit.isConstant()) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_1")
                        + 3
                        + Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_2"));
            }
        } catch (ExpressionException e) {
            throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_1")
                    + 3
                    + Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_2"));
        }

        try {
            upperLimit = Expression.build(params[3], vars);
            if (!upperLimit.isIntegerConstant() && upperLimit.isConstant()) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_1")
                        + 4
                        + Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_2"));
            }
        } catch (ExpressionException e) {
            throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_1")
                    + 4
                    + Translator.translateExceptionMessage("MEB_Operator_WRONG_FORM_OF_LIMIT_PARAMETER_IN_SUM_2"));
        }

        if (lowerLimit.contains(params[1]) || upperLimit.contains(params[1])) {
            throw new ExpressionException(Translator.translateExceptionMessage("MEB_Operator_BOUNDS_IN_SUM_CANNOT_CONTAIN_INDEX_VARIABLE"));
        }

        resultMatrixOperatorParams = new Object[4];
        resultMatrixOperatorParams[0] = summand;
        resultMatrixOperatorParams[1] = params[1];
        resultMatrixOperatorParams[2] = lowerLimit;
        resultMatrixOperatorParams[3] = upperLimit;
        return new MatrixOperator(type, resultMatrixOperatorParams);

    }

    @Override
    public boolean isConstant() {

        if (this.type.equals(TypeMatrixOperator.integral)) {

            // Unbestimmte Integrale sind NIE konstant.
            if (this.params.length == 2) {
                return false;
            }
            // Bestimmte Integrale.
            HashSet<String> varsInParameters = new HashSet<>();
            ((MatrixExpression) this.params[0]).getContainedVars(varsInParameters);
            varsInParameters.remove((String) this.params[1]);
            ((Expression) this.params[2]).getContainedVars(varsInParameters);
            ((Expression) this.params[3]).getContainedVars(varsInParameters);

            return varsInParameters.isEmpty();

        }

        if (this.type.equals(TypeMatrixOperator.prod) || this.type.equals(TypeMatrixOperator.sum)) {

            HashSet<String> varsInParameters = new HashSet<>();
            ((Expression) this.params[0]).getContainedVars(varsInParameters);
            varsInParameters.remove((String) this.params[1]);
            ((Expression) this.params[2]).getContainedVars(varsInParameters);
            ((Expression) this.params[3]).getContainedVars(varsInParameters);
            return varsInParameters.isEmpty();

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
    public boolean contains(String var) {

        if (this.type.equals(TypeMatrixOperator.integral) && this.params.length == 2) {
            return ((MatrixExpression) this.params[0]).contains(var);
        }

        // Im bestimmten Integral zählt die Integrationsvariable NICHT als vorkommende Veränderliche!
        if (this.type.equals(TypeMatrixOperator.integral) && this.params.length == 4) {
            return (((MatrixExpression) this.params[0]).contains(var) || ((Expression) this.params[2]).contains(var)
                    || ((Expression) this.params[3]).contains(var)) && !var.equals((String) this.params[1]);
        }

        // In der Summe oder im Produkt zählt der Index NICHT als vorkommende Veränderliche!
        if (this.type.equals(TypeMatrixOperator.prod) || this.type.equals(TypeMatrixOperator.sum)) {
            return (((MatrixExpression) this.params[0]).contains(var) || ((Expression) this.params[2]).contains(var)
                    || ((Expression) this.params[3]).contains(var)) && !((String) this.params[1]).equals(var);
        }

        boolean result = false;
        for (Object param : this.params) {
            if (param instanceof Expression) {
                result = result || ((Expression) param).contains(var);
            } else if (param instanceof MatrixExpression) {
                result = result || ((MatrixExpression) param).contains(var);
            }
        }
        return result;

    }

    @Override
    public MatrixExpression copy() {
        return new MatrixOperator(this.type, this.params, this.precise);
    }

    @Override
    public MatrixExpression diff(String var) throws EvaluationException {

        if (!this.contains(var)) {
            return new Matrix(Expression.ZERO);
        }

        if (this.type.equals(TypeMatrixOperator.integral)) {
            if (this.params.length == 2) {

                // Unbestimmte Integration.
                if (var.equals((String) this.params[1])) {
                    return (MatrixExpression) this.params[0];
                }

                Object[] resultParams = new Object[2];
                resultParams[0] = ((MatrixExpression) this.params[0]).diff(var);
                resultParams[1] = this.params[1];
                return new MatrixOperator(TypeMatrixOperator.integral, resultParams, this.precise);

            } else {

                /*
                 Bestimmte Integration. Hier Leibnis-Regel: (d/dt)
                 int_a(t)^b(t) f(x, t) dt = int_a(t)^b(t) (d/dt)f(x, t) dt +
                 f(b(t), t)*(d/dt)b(t) - f(a(t), t)*(d/dt)a(t).
                 */
                if (var.equals((String) this.params[1])) {
                    return new Matrix(Expression.ZERO);
                }

                MatrixExpression f = ((MatrixExpression) this.params[0]);
                MatrixExpression differentiatedIntegral;
                String integrationVar = (String) this.params[1];
                Expression lowerLimit = (Expression) this.params[2];
                Expression upperLimit = (Expression) this.params[3];

                Object[] resultParams = new Object[4];
                resultParams[0] = f.diff(var);
                resultParams[1] = this.params[1];
                resultParams[2] = this.params[2];
                resultParams[3] = this.params[3];
                differentiatedIntegral = new MatrixOperator(TypeMatrixOperator.integral, resultParams, this.precise);

                return differentiatedIntegral.add(f.replaceVariable(integrationVar, upperLimit).mult(upperLimit.diff(var))).sub(f.replaceVariable(integrationVar, lowerLimit).mult(lowerLimit.diff(var)));

            }
        }

        if (this.type.equals(TypeMatrixOperator.sum)) {

            // Nun: diff(sum(f(k, x), k, m, n), x) = sum(diff(f(k, x), x), k, m, n).
            Object[] resultParams = new Object[4];
            resultParams[0] = ((MatrixExpression) this.params[0]).diff(var).simplify();
            resultParams[1] = this.params[1];
            resultParams[2] = this.params[2];
            resultParams[3] = this.params[3];
            return new MatrixOperator(TypeMatrixOperator.sum, resultParams, this.precise);

        }

        Object[] paramsOfDerivative = new Object[2];
        paramsOfDerivative[0] = this;
        paramsOfDerivative[1] = var;
        return new MatrixOperator(TypeMatrixOperator.diff, paramsOfDerivative);

    }

    @Override
    public MatrixExpression replaceVariable(String var, Expression expr) {

        Object[] resultParams = new Object[this.params.length];

        /*
         Bei den Operator INT, PROD und SUM sollen die (lokalen) Variablen
         (Integrationsvariable/Index) NICHT ersetzt werden; überall sonst
         schon.
         */
        if (this.type.equals(TypeMatrixOperator.integral) || this.type.equals(TypeMatrixOperator.prod) || this.type.equals(TypeMatrixOperator.sum)) {
            if (!var.equals((String) this.params[1])) {

                for (int i = 0; i < this.params.length; i++) {
                    if (this.params[i] instanceof Expression) {
                        resultParams[i] = ((Expression) this.params[i]).replaceVariable(var, expr);
                    } else if (this.params[i] instanceof MatrixExpression) {
                        resultParams[i] = ((MatrixExpression) this.params[i]).replaceVariable(var, expr);
                    } else {
                        resultParams[i] = this.params[i];
                    }
                }
                return new MatrixOperator(this.type, resultParams, this.precise);

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

        return new MatrixOperator(this.type, resultParams, this.precise);

    }

    @Override
    public void getContainedVars(HashSet<String> vars) {

        /*
         Bei bestimmter Integration/Summen/Produkten zählt die
         Integrationsvariable/der Index NICHT als vorkommende Variable.
         */
        if (this.type.equals(TypeMatrixOperator.integral) && this.params.length == 2) {
            ((MatrixExpression) this.params[0]).getContainedVars(vars);
            return;
        }
        if (this.type.equals(TypeMatrixOperator.integral) && this.params.length == 4) {
            String var = (String) this.params[1];
            ((MatrixExpression) this.params[0]).getContainedVars(vars);
            vars.remove(var);
            ((Expression) this.params[2]).getContainedVars(vars);
            ((Expression) this.params[3]).getContainedVars(vars);
            return;
        }
        if (this.type.equals(TypeMatrixOperator.prod) || this.type.equals(TypeMatrixOperator.sum)) {
            String index = (String) this.params[1];
            ((MatrixExpression) this.params[0]).getContainedVars(vars);
            vars.remove(index);
            ((Expression) this.params[2]).getContainedVars(vars);
            ((Expression) this.params[3]).getContainedVars(vars);
            return;
        }

        // Alle anderen möglichen Matrizenoperatoren
        for (Object param : this.params) {
            if (param instanceof Expression) {
                ((Expression) param).getContainedVars(vars);
            } else if (param instanceof MatrixExpression) {
                ((MatrixExpression) param).getContainedVars(vars);
            }
        }

    }

    @Override
    public String writeMatrixExpression() {

        String result = (String) getNameFromType(this.type) + "(";
        String parameter = "";

        for (Object param : this.params) {
            if (param instanceof Expression) {
                parameter = ((Expression) param).writeExpression();
            } else if (param instanceof MatrixExpression) {
                parameter = ((MatrixExpression) param).writeMatrixExpression();
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
    public boolean equals(MatrixExpression matExpr) {

        if (matExpr instanceof MatrixOperator) {

            MatrixOperator operator = (MatrixOperator) matExpr;
            boolean result = this.type.equals(operator.type) && (this.params.length == operator.params.length);
            if (!result) {
                return false;
            }

            for (int i = 0; i < this.params.length; i++) {
                if (this.params[i] instanceof Expression) {
                    result = result && (((Expression) this.params[i]).equals((Expression) operator.params[i]));
                } else if (this.params[i] instanceof MatrixExpression) {
                    result = result && (((MatrixExpression) this.params[i]).equals((MatrixExpression) operator.params[i]));
                } else {
                    result = result && (this.params[i].equals(operator.params[i]));
                }
            }

            return result;

        }
        return false;

    }

    @Override
    public boolean equivalent(MatrixExpression matExpr) {

        if (matExpr instanceof MatrixOperator) {

            MatrixOperator operator = (MatrixOperator) matExpr;
            boolean result = (this.type.equals(operator.type) & (this.params.length == operator.params.length));
            if (!result) {
                return false;
            }

            for (int i = 0; i < this.params.length; i++) {
                if (this.params[i] instanceof Expression) {
                    result = result && (((Expression) this.params[i]).equivalent((Expression) operator.params[i]));
                } else if (this.params[i] instanceof MatrixExpression) {
                    result = result && (((MatrixExpression) this.params[i]).equivalent((MatrixExpression) operator.params[i]));
                } else {
                    result = result && (this.params[i].equals(operator.params[i]));
                }
            }
            return result;

        }
        return false;

    }

    @Override
    public MatrixExpression simplifyMatrixEntries() throws EvaluationException {

        // Zunächst alle Parameter, welche gültige Ausdrücke darstellen, vereinfachen.
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyTrivial();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        MatrixOperator matrixOperator = new MatrixOperator(this.type, resultParams, this.precise);

        // Nun wird noch operatorspezifisch vereinfacht.
        if (this.type.equals(TypeMatrixOperator.diff)) {
            return matrixOperator.simplifyTrivialDiff();
        }
        if (this.type.equals(TypeMatrixOperator.div)) {
            return matrixOperator.simplifyTrivialDiv();
        }
        if (this.type.equals(TypeMatrixOperator.grad)) {
            return matrixOperator.simplifyTrivialGrad();
        }
        if (this.type.equals(TypeMatrixOperator.integral)) {
            return matrixOperator.simplifyTrivialInt();
        }
        if (this.type.equals(TypeMatrixOperator.laplace)) {
            return matrixOperator.simplifyTrivialLaplace();
        }
        if (this.type.equals(TypeMatrixOperator.prod)) {
            return matrixOperator.simplifyTrivialProd();
        }
        if (this.type.equals(TypeMatrixOperator.sum)) {
            return matrixOperator.simplifyTrivialSum();
        }

        // Kommt nicht vor, aber trotzdem;
        return matrixOperator;

    }

    /**
     * Vereinfacht den Ableitungsoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private MatrixExpression simplifyTrivialDiff() throws EvaluationException {

        MatrixExpression matExpr = (MatrixExpression) this.params[0];
        if (this.params.length == 2) {
            matExpr = matExpr.diff((String) this.params[1]);
            matExpr = matExpr.simplify();
            return matExpr;
        }
        /*
         Es wird zunächst geprüft, ob alle übrigen Parameter gültige
         Variablen sind, oder ob der dritte Parameter eine ganze Zahl ist.
         */
        if (this.params[2] instanceof String) {
            for (int i = 1; i < this.params.length; i++) {
                matExpr = matExpr.diff((String) this.params[i]);
                matExpr = matExpr.simplify();
            }
        } else {
            int k = (int) this.params[2];
            for (int i = 0; i < k; i++) {
                matExpr = matExpr.diff((String) this.params[1]);
                matExpr = matExpr.simplify();
            }
        }
        return matExpr;

    }

    /**
     * Vereinfacht den Divergenzoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private MatrixExpression simplifyTrivialDiv() throws EvaluationException {

        Dimension dim = ((MatrixExpression) this.params[0]).getDimension();
        MatrixExpression zeroMatrix = MatrixExpression.getZeroMatrix(dim.height, dim.width);
        MatrixExpression result = zeroMatrix;
        MatrixExpression matExpr = (MatrixExpression) this.params[0];
        HashSet<String> vars = new HashSet<>();
        matExpr.getContainedVars(vars);
        Iterator<String> iter = vars.iterator();
        String var;

        for (int i = 0; i < vars.size(); i++) {
            var = iter.next();
            if (result.equals(zeroMatrix)) {
                result = matExpr.diff(var);
            } else {
                result = result.add(matExpr.diff(var));
            }
        }

        return result;

    }

    /**
     * Vereinfacht den Gradientenoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private MatrixExpression simplifyTrivialGrad() throws EvaluationException {

        MatrixExpression expr = ((MatrixExpression) this.params[0]).simplify();
        Object exprConverted = expr.convertOneTimesOneMatrixToExpression();
        
        if (exprConverted instanceof Expression) {
            Expression[][] gradEntries = new Expression[this.params.length - 1][1];
            for (int i = 1; i < this.params.length; i++){
                gradEntries[i - 1][0] = ((Expression) exprConverted).diff((String) this.params[i]);
            }
            return new Matrix(gradEntries);
        }

        // Dann kann nichts vereinfacht werden.
        return new MatrixOperator(TypeMatrixOperator.grad, new Object[]{expr});

    }

    /**
     * Vereinfacht den Integraloperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private MatrixExpression simplifyTrivialInt() throws EvaluationException {

        if (this.params[0] instanceof Matrix) {

            if (this.params.length == 2) {

                Expression[][] resultEntry = new Expression[((Matrix) this.params[0]).getRowNumber()][((Matrix) this.params[0]).getColumnNumber()];
                Object[][][] entryParams = new Object[((Matrix) this.params[0]).getRowNumber()][((Matrix) this.params[0]).getColumnNumber()][2];

                for (int i = 0; i < ((Matrix) this.params[0]).getRowNumber(); i++) {
                    for (int j = 0; j < ((Matrix) this.params[0]).getColumnNumber(); j++) {
                        entryParams[i][j][0] = ((Matrix) this.params[0]).getEntry(i, j);
                        entryParams[i][j][1] = this.params[1];
                        resultEntry[i][j] = new Operator(TypeOperator.integral, entryParams[i][j], this.precise);
                    }
                }

                return new Matrix(resultEntry);

            }

            if (this.params.length == 4) {

                Expression[][] resultEntry = new Expression[((Matrix) this.params[0]).getRowNumber()][((Matrix) this.params[0]).getColumnNumber()];
                Object[][][] entryParams = new Object[((Matrix) this.params[0]).getRowNumber()][((Matrix) this.params[0]).getColumnNumber()][4];

                for (int i = 0; i < ((Matrix) this.params[0]).getRowNumber(); i++) {
                    for (int j = 0; j < ((Matrix) this.params[0]).getColumnNumber(); j++) {
                        entryParams[i][j][0] = ((Matrix) this.params[0]).getEntry(i, j);
                        entryParams[i][j][1] = this.params[1];
                        entryParams[i][j][2] = this.params[2];
                        entryParams[i][j][3] = this.params[3];
                        resultEntry[i][j] = new Operator(TypeOperator.integral, entryParams[i][j], this.precise);
                    }
                }

                return new Matrix(resultEntry);

            }

        }

        return this;

    }

    /**
     * Vereinfacht den Laplaceoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private MatrixExpression simplifyTrivialLaplace() throws EvaluationException {

        Dimension dim = ((MatrixExpression) this.params[0]).getDimension();
        MatrixExpression zeroMatrix = MatrixExpression.getZeroMatrix(dim.height, dim.width);
        MatrixExpression result = zeroMatrix;
        MatrixExpression matExpr = (MatrixExpression) this.params[0];
        HashSet<String> vars = new HashSet<>();
        matExpr.getContainedVars(vars);
        Iterator iter = vars.iterator();
        String var;

        for (int i = 0; i < vars.size(); i++) {
            var = (String) iter.next();
            if (result.equals(zeroMatrix)) {
                result = matExpr.diff(var);
            } else {
                result = result.add(matExpr.diff(var).simplify().diff(var).simplify());
            }
        }

        return result;

    }

    /**
     * Vereinfacht den Produktoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private MatrixExpression simplifyTrivialProd() throws EvaluationException {

        MatrixExpression factor = (MatrixExpression) this.params[0];

        if (((Expression) this.params[2]).isIntegerConstant() && ((Expression) this.params[3]).isIntegerConstant()) {
            BigInteger lowerLimit = ((Constant) ((Expression) this.params[2])).getValue().toBigInteger();
            BigInteger upperLimit = ((Constant) ((Expression) this.params[3])).getValue().toBigInteger();
            if (lowerLimit.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0 && lowerLimit.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0
                    && upperLimit.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0 && upperLimit.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0) {

                /**
                 * Dann kann man die Summe explizit ausschreiben.
                 */
                return AnalysisMethods.prod(factor, (String) this.params[1], lowerLimit.intValue(), upperLimit.intValue());

            }
        }

        // Falls die Faktoren von der Indexvariable nicht abhängen.
        if (!factor.contains((String) this.params[1])) {
            return factor.pow(((Expression) this.params[3]).add(Expression.ONE).sub((Expression) this.params[2]));
        }

        // Sonstiger Fall.
        Object[] resultParams = new Object[4];
        resultParams[0] = factor.simplify();
        resultParams[1] = this.params[1];
        resultParams[2] = this.params[2];
        resultParams[3] = this.params[3];
        return new MatrixOperator(TypeMatrixOperator.prod, resultParams, this.precise);

    }

    /**
     * Vereinfacht den Summenoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    private MatrixExpression simplifyTrivialSum() throws EvaluationException {

        MatrixExpression summand = (MatrixExpression) this.params[0];

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
            return new Matrix(((Expression) this.params[3]).add(Expression.ONE).sub((Expression) this.params[2])).mult(summand);
        }

        // Summen von Summen oder Differenzen aufteilen.
        if (summand.isSum() || summand.isDifference()) {
            return SimplifyMatrixOperatorMethods.splitSumOfSumsOrDifferences((MatrixBinaryOperation) summand, (String) this.params[1],
                    (Expression) this.params[2], (Expression) this.params[3]);
        }

        // Sonstiger Fall (wo man nichts machen kann).
        Object[] resultParams = new Object[4];
        resultParams[0] = summand.simplify();
        resultParams[1] = this.params[1];
        resultParams[2] = this.params[2];
        resultParams[3] = this.params[3];
        return new MatrixOperator(TypeMatrixOperator.sum, resultParams, this.precise);

    }

    @Override
    public Dimension getDimension() throws EvaluationException {
        return ((MatrixExpression) this.params[0]).getDimension();
    }

    @Override
    public MatrixExpression computeMatrixOperations() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof MatrixExpression) {
                resultParams[i] = ((MatrixExpression) this.params[i]).computeMatrixOperations();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new MatrixOperator(this.type, resultParams, this.precise);
    }

    @Override
    public MatrixExpression simplifyMatrixEntries(HashSet<TypeSimplify> simplifyTypes) throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof MatrixExpression) {
                resultParams[i] = ((MatrixExpression) this.params[i]).simplifyMatrixEntries(simplifyTypes);
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new MatrixOperator(this.type, resultParams, this.precise);
    }

    @Override
    public MatrixExpression collectProducts() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof MatrixExpression) {
                resultParams[i] = ((MatrixExpression) this.params[i]).collectProducts();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new MatrixOperator(this.type, resultParams, this.precise);
    }

    @Override
    public MatrixExpression simplifyMatrixFunctionalRelations() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof MatrixExpression) {
                resultParams[i] = ((MatrixExpression) this.params[i]).simplifyMatrixFunctionalRelations();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new MatrixOperator(this.type, resultParams, this.precise);
    }

}

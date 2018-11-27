package abstractexpressions.matrixexpression.classes;

import abstractexpressions.annotations.SimplifyMatrixOperator;
import abstractexpressions.expression.computation.AnalysisUtils;
import computationbounds.ComputationBounds;
import enums.TypeSimplify;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeOperator;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import abstractexpressions.matrixexpression.basic.SimplifyMatrixOperatorUtils;
import exceptions.CancellationException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import operationparser.OperationParser;
import lang.translator.Translator;

public class MatrixOperator extends MatrixExpression {

    private static final String MEB_MatrixOperator_3_PARAMETER_IN_DIFF_IS_INVALID = "MEB_MatrixOperator_3_PARAMETER_IN_DIFF_IS_INVALID";
    private static final String MEB_MatrixOperator_WRONG_FORM_OF_FIRST_PARAMETER_IN_OPERATOR_DIV = "MEB_MatrixOperator_WRONG_FORM_OF_FIRST_PARAMETER_IN_OPERATOR_DIV";
    private static final String MEB_MatrixOperator_INVALID_MATRIX_OPERATOR = "MEB_MatrixOperator_INVALID_MATRIX_OPERATOR";
    private static final String MEB_MatrixOperator_COV_PARAMETERS_ARE_NOT_POINTS = "MEB_MatrixOperator_COV_PARAMETERS_ARE_NOT_POINTS";
    private static final String MEB_MatrixOperator_WRONG_FORM_OF_PARAMETERS_IN_OPERATOR_CROSS = "MEB_MatrixOperator_WRONG_FORM_OF_PARAMETERS_IN_OPERATOR_CROSS";
    private static final String MEB_MatrixOperator_GRAD_ONLY_POSSIBLE_FOR_1_X_1_MATRICES = "MEB_MatrixOperator_GRAD_ONLY_POSSIBLE_FOR_1_X_1_MATRICES";
    private static final String MEB_MatrixOperator_FIRST_PARAMETER_IN_PROD_NOT_SQUARE_MATRIX = "MEB_MatrixOperator_FIRST_PARAMETER_IN_PROD_NOT_SQUARE_MATRIX";
    private static final String MEB_MatrixOperator_WRONG_FORM_OF_PARAMETER_IN_OPERATOR_ROT = "MEB_MatrixOperator_WRONG_FORM_OF_PARAMETER_IN_OPERATOR_ROT";
    
    private TypeMatrixOperator type;
    private Object[] params;
    private boolean precise;

    // Patterns für die einzelnen Matrizenoperatoren.
    public static final String PATTERN_COV = "cov(matexpr+)";
    public static final String PATTERN_CROSS = "cross(matexpr+)";
    public static final String PATTERN_DIFF = "diff(matexpr,indet+)";
    public static final String PATTERN_DIFF_WITHOUT_ORDER = "diff(matexpr,indet,integer(0,2147483647))";
    public static final String PATTERN_DIV = "div(matexpr,uniqueindet+)";
    public static final String PATTERN_GRAD = "grad(matexpr,uniqueindet+)";
    public static final String PATTERN_INT_INDEF = "int(matexpr,indet)";
    public static final String PATTERN_INT_DEF = "int(matexpr,indet(!2,!3),expr,expr)";
    public static final String PATTERN_LAPLACE = "laplace(matexpr,uniqueindet+)";
    public static final String PATTERN_PROD = "prod(matexpr,indet(!2,!3),expr,expr)";
    public static final String PATTERN_ROT = "rot(matexpr,uniqueindet,uniqueindet,uniqueindet)";
    public static final String PATTERN_SUM = "sum(matexpr,indet(!2,!3),expr,expr)";

    public MatrixOperator() {
    }

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
        if (operator.equals(TypeMatrixOperator.integral.getOperatorName())) {
            return TypeMatrixOperator.integral;
        }
        return TypeMatrixOperator.valueOf(operator);
    }

    /**
     * Ermittelt den zugehörigen Operator und liefert eine Instanz der Klasse
     * Operator.
     *
     * @throws ExpressionException
     */
    public static MatrixOperator getMatrixOperator(String operator, String[] params, Set<String> vars) throws ExpressionException {

        TypeMatrixOperator type = getTypeFromName(operator);

        // Sonderfälle: überladene Operatoren.
        switch (type) {
            case diff:
                if (params.length != 3) {
                    return OperationParser.parseDefaultMatrixOperator(operator, params, vars, PATTERN_DIFF);
                }
                try {
                    return OperationParser.parseDefaultMatrixOperator(operator, params, vars, PATTERN_DIFF);
                } catch (ExpressionException e) {
                    try {
                        return OperationParser.parseDefaultMatrixOperator(operator, params, vars, PATTERN_DIFF_WITHOUT_ORDER);
                    } catch (ExpressionException ex) {
                        throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixOperator_3_PARAMETER_IN_DIFF_IS_INVALID));
                    }
                }
            case div:
                MatrixOperator divOpr =  OperationParser.parseDefaultMatrixOperator(operator, params, vars, PATTERN_DIV);
                Object[] oprParams = divOpr.getParams();
                MatrixExpression argument = (MatrixExpression) oprParams[0];
                try {
                    Dimension dim = argument.getDimension();
                    if (dim.width != 1 || dim.height != oprParams.length - 1) {
                        throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixOperator_WRONG_FORM_OF_FIRST_PARAMETER_IN_OPERATOR_DIV));
                    }
                    return divOpr;
                } catch (EvaluationException e) {
                    throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixOperator_WRONG_FORM_OF_FIRST_PARAMETER_IN_OPERATOR_DIV));
                }
            case integral:
                if (params.length <= 2) {
                    return OperationParser.parseDefaultMatrixOperator(operator, params, vars, PATTERN_INT_INDEF);
                }
                return OperationParser.parseDefaultMatrixOperator(operator, params, vars, PATTERN_INT_DEF);
        }

        // Mittels Reflection das passende Pattern suchen.
        Field[] fields = MatrixOperator.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.getType().equals(String.class) && Modifier.isStatic(field.getModifiers()) && ((String) field.get(null)).startsWith(operator)) {
                    return OperationParser.parseDefaultMatrixOperator(operator, params, vars, (String) field.get(null));
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
            }
        }

        throw new ExpressionException(MEB_MatrixOperator_INVALID_MATRIX_OPERATOR);

    }

    @Override
    public boolean isConstant() {

        if (this.type.equals(TypeMatrixOperator.integral)) {

            // Unbestimmte Integrale sind NIE konstant.
            if (this.params.length == 2) {
                return false;
            }
            // Bestimmte Integrale.
            Set<String> varsInParameters = new HashSet<>();
            ((MatrixExpression) this.params[0]).addContainedVars(varsInParameters);
            varsInParameters.remove((String) this.params[1]);
            ((Expression) this.params[2]).addContainedVars(varsInParameters);
            ((Expression) this.params[3]).addContainedVars(varsInParameters);

            return varsInParameters.isEmpty();

        }

        if (this.type.equals(TypeMatrixOperator.prod) || this.type.equals(TypeMatrixOperator.sum)) {

            Set<String> varsInParameters = new HashSet<>();
            ((Expression) this.params[0]).addContainedVars(varsInParameters);
            varsInParameters.remove((String) this.params[1]);
            ((Expression) this.params[2]).addContainedVars(varsInParameters);
            ((Expression) this.params[3]).addContainedVars(varsInParameters);
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
    public MatrixExpression evaluate() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof MatrixExpression) {
                resultParams[i] = ((MatrixExpression) this.params[i]).evaluate();
            } else if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).evaluate();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new MatrixOperator(this.type, resultParams, false);
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
    public MatrixExpression replaceMatrixVariable(String var, MatrixExpression matExpr) {

        Object[] resultParams = new Object[this.params.length];

        /*
         Bei den Operator INT, PROD und SUM können die (lokalen) Variablen
         (Integrationsvariable/Index) KEINE Matrizenvariablen sein.
         */
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof MatrixExpression) {
                resultParams[i] = ((MatrixExpression) this.params[i]).replaceMatrixVariable(var, matExpr);
            } else {
                resultParams[i] = this.params[i];
            }
        }

        return new MatrixOperator(this.type, resultParams, this.precise);

    }

    @Override
    public void addContainedVars(Set<String> vars) {

        /*
         Bei bestimmter Integration/Summen/Produkten zählt die
         Integrationsvariable/der Index NICHT als vorkommende Variable.
         */
        if (this.type.equals(TypeMatrixOperator.integral) && this.params.length == 2) {
            ((MatrixExpression) this.params[0]).addContainedVars(vars);
            return;
        }
        if (this.type.equals(TypeMatrixOperator.integral) && this.params.length == 4) {
            String var = (String) this.params[1];
            ((MatrixExpression) this.params[0]).addContainedVars(vars);
            vars.remove(var);
            ((Expression) this.params[2]).addContainedVars(vars);
            ((Expression) this.params[3]).addContainedVars(vars);
            return;
        }
        if (this.type.equals(TypeMatrixOperator.prod) || this.type.equals(TypeMatrixOperator.sum)) {
            String index = (String) this.params[1];
            ((MatrixExpression) this.params[0]).addContainedVars(vars);
            vars.remove(index);
            ((Expression) this.params[2]).addContainedVars(vars);
            ((Expression) this.params[3]).addContainedVars(vars);
            return;
        }

        // Alle anderen möglichen Matrizenoperatoren
        for (Object param : this.params) {
            if (param instanceof Expression) {
                ((Expression) param).addContainedVars(vars);
            } else if (param instanceof MatrixExpression) {
                ((MatrixExpression) param).addContainedVars(vars);
            }
        }

    }

    @Override
    public void addContainedIndeterminates(Set<String> vars) {

        /*
         Bei bestimmter Integration/Summen/Produkten zählt die
         Integrationsvariable/der Index NICHT als vorkommende Variable.
         */
        if (this.type.equals(TypeMatrixOperator.integral) && this.params.length == 2) {
            ((MatrixExpression) this.params[0]).addContainedIndeterminates(vars);
            return;
        }
        if (this.type.equals(TypeMatrixOperator.integral) && this.params.length == 4) {
            String var = (String) this.params[1];
            ((MatrixExpression) this.params[0]).addContainedIndeterminates(vars);
            vars.remove(var);
            ((Expression) this.params[2]).addContainedIndeterminates(vars);
            ((Expression) this.params[3]).addContainedIndeterminates(vars);
            return;
        }
        if (this.type.equals(TypeMatrixOperator.prod) || this.type.equals(TypeMatrixOperator.sum)) {
            String index = (String) this.params[1];
            ((MatrixExpression) this.params[0]).addContainedIndeterminates(vars);
            vars.remove(index);
            ((Expression) this.params[2]).addContainedIndeterminates(vars);
            ((Expression) this.params[3]).addContainedIndeterminates(vars);
            return;
        }

        // Alle anderen möglichen Matrizenoperatoren
        for (Object param : this.params) {
            if (param instanceof Expression) {
                ((Expression) param).addContainedIndeterminates(vars);
            } else if (param instanceof MatrixExpression) {
                ((MatrixExpression) param).addContainedIndeterminates(vars);
            }
        }

    }

    @Override
    public MatrixExpression turnToApproximate() {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).turnToApproximate();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new MatrixOperator(this.type, resultParams, this.precise);
    }

    @Override
    public MatrixExpression turnToPrecise() {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).turnToPrecise();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new MatrixOperator(this.type, resultParams, this.precise);
    }

    @Override
    public boolean containsApproximates() {
        for (Object param : this.params) {
            if (param instanceof MatrixExpression && ((MatrixExpression) param).containsApproximates()) {
                return true;
            } else if (param instanceof Expression && ((Expression) param).containsApproximates()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {

        String result = this.type.getOperatorName() + "(";
        String parameter = "";

        for (Object param : this.params) {
            if (param instanceof Expression) {
                parameter = ((Expression) param).toString();
            } else if (param instanceof MatrixExpression) {
                parameter = ((MatrixExpression) param).toString();
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
    public MatrixExpression orderSumsAndProducts() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).orderSumsAndProducts();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new MatrixOperator(this.type, resultParams, this.precise);
    }

    @Override
    public MatrixExpression orderDifferences() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).orderDifferencesAndQuotients();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new MatrixOperator(this.type, resultParams, this.precise);
    }

    @Override
    public MatrixExpression simplifyBasic() throws EvaluationException {

        // Zunächst alle Parameter, welche gültige Ausdrücke darstellen, vereinfachen.
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyBasic();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        MatrixOperator operator = new MatrixOperator(this.type, resultParams, this.precise);

        // Mittels Reflection die passende Ausführmethode ermittln (durch Vergleich der Annotation).
        Method[] methods = MatrixOperator.class.getDeclaredMethods();
        SimplifyMatrixOperator annotation;
        for (Method method : methods) {
            annotation = method.getAnnotation(SimplifyMatrixOperator.class);
            if (annotation != null && annotation.type().equals(this.type)) {
                try {
                    return (MatrixExpression) method.invoke(operator);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    if (e.getCause() instanceof EvaluationException) {
                        throw (EvaluationException) e.getCause();
                    }
                    if (e.getCause() instanceof CancellationException) {
                        throw (CancellationException) e.getCause();
                    }
                    throw new EvaluationException(Translator.translateOutputMessage(MEB_MatrixOperator_INVALID_MATRIX_OPERATOR));
                }
            }
        }

        return operator;

    }

    @Override
    public MatrixExpression simplifyByInsertingDefinedVars() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof MatrixExpression) {
                resultParams[i] = ((MatrixExpression) this.params[i]).simplifyByInsertingDefinedVars();
            } else if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyByInsertingDefinedVars();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new MatrixOperator(this.type, resultParams, false);
    }

    @Override
    public MatrixExpression simplifyMatrixEntries() throws EvaluationException {

        // Zunächst alle Parameter, welche gültige Ausdrücke darstellen, vereinfachen.
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyBasic();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        MatrixOperator matrixOperator = new MatrixOperator(this.type, resultParams, this.precise);

        // Nun wird noch operatorspezifisch vereinfacht.
        switch (type) {
            case cov:
                return matrixOperator.simplifyBasicCov();
            case cross:
                return matrixOperator.simplifyBasicCross();
            case diff:
                return matrixOperator.simplifyBasicDiff();
            case div:
                return matrixOperator.simplifyBasicDiv();
            case grad:
                return matrixOperator.simplifyBasicGrad();
            case integral:
                return matrixOperator.simplifyBasicInt();
            case laplace:
                return matrixOperator.simplifyBasicLaplace();
            case prod:
                return matrixOperator.simplifyBasicProd();
            case rot:
                return matrixOperator.simplifyBasicRot();
            case sum:
                return matrixOperator.simplifyBasicSum();
        }

        // Kommt nicht vor, aber trotzdem;
        return matrixOperator;

    }

    /**
     * Vereinfacht den cov-Operator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyMatrixOperator(type = TypeMatrixOperator.cov)
    private MatrixExpression simplifyBasicCov() throws EvaluationException {

        MatrixExpression[] points = new MatrixExpression[params.length];
        Dimension dim;

        for (int i = 0; i < points.length; i++) {
            points[i] = ((MatrixExpression) params[i]).simplify();
            dim = points[i].getDimension();
            if (!points[i].isMatrix() || dim.width != 1 || dim.height != 2) {
                throw new EvaluationException(Translator.translateOutputMessage(MEB_MatrixOperator_COV_PARAMETERS_ARE_NOT_POINTS));
            }
        }

        // Koeffizienten für die Regressionsgerade berechnen.
        Matrix[] pts = new Matrix[points.length];
        for (int i = 0; i < points.length; i++) {
            pts[i] = (Matrix) points[i];
        }

        Expression[] valuesX = new Expression[params.length];
        Expression[] valuesY = new Expression[params.length];

        for (int i = 0; i < params.length; i++) {
            valuesX[i] = pts[i].getEntry(0, 0);
            valuesY[i] = pts[i].getEntry(1, 0);
        }

        Expression muX = new Operator(TypeOperator.mu, valuesX);
        Expression muY = new Operator(TypeOperator.mu, valuesX);

        Expression result = ZERO;
        for (int i = 0; i < params.length; i++) {
            result = result.add(valuesX[i].sub(muX).mult(valuesY[i].sub(muY)));
        }

        return new Matrix(result.div(params.length));

    }

    /**
     * Vereinfacht den cross-Operator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyMatrixOperator(type = TypeMatrixOperator.cross)
    private MatrixExpression simplifyBasicCross() throws EvaluationException {

        MatrixExpression[] vectors = new MatrixExpression[params.length];
        Dimension dim;

        for (int i = 0; i < vectors.length; i++) {
            vectors[i] = ((MatrixExpression) params[i]).simplify();
            dim = vectors[i].getDimension();
            if (!vectors[i].isMatrix() || dim.width != 1 || dim.height != params.length + 1) {
                throw new EvaluationException(Translator.translateOutputMessage(MEB_MatrixOperator_WRONG_FORM_OF_PARAMETERS_IN_OPERATOR_CROSS, i + 1, params.length + 1));
            }
        }

        // Nur explizit ausrechnen, wenn alle Einträge Vektoren sind.
        for (int i = 0; i < params.length; i++) {
            if (!((MatrixExpression) vectors[i]).isMatrix()) {
                return this;
            }
        }

        Matrix[] minors = new Matrix[params.length + 1];
        Expression[][] entries = new Expression[params.length][params.length];
        for (int i = 0; i < params.length + 1; i++) {

            for (int j = 0; j < params.length; j++) {
                for (int k = 0; k < params.length; k++) {
                    if (j < i) {
                        entries[j][k] = ((Matrix) vectors[k]).getEntry(j, 0);
                    } else {
                        entries[j][k] = ((Matrix) vectors[k]).getEntry(j + 1, 0);
                    }
                }
            }
            minors[i] = new Matrix(entries);

        }

        boolean sign = true;
        Matrix unitVector;
        MatrixExpression crossProduct = MatrixExpression.getZeroMatrix(params.length + 1, 1);
        for (int i = 0; i < params.length + 1; i++) {
            unitVector = MatrixExpression.getUnitVector(i, params.length + 1);
            if (sign) {
                crossProduct = crossProduct.add(unitVector.mult(minors[i].det()));
            } else {
                crossProduct = crossProduct.sub(unitVector.mult(minors[i].det()));
            }
            sign = !sign;
        }

        return crossProduct;

    }

    /**
     * Vereinfacht den Ableitungsoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyMatrixOperator(type = TypeMatrixOperator.diff)
    private MatrixExpression simplifyBasicDiff() throws EvaluationException {

        MatrixExpression matExpr = (MatrixExpression) this.params[0];
        if (this.params.length == 2) {
            matExpr = matExpr.diff((String) this.params[1]);
            return matExpr;
        }
        /*
         Es wird zunächst geprüft, ob alle übrigen Parameter gültige
         Variablen sind, oder ob der dritte Parameter eine ganze Zahl ist.
         */
        if (this.params[2] instanceof String) {
            for (int i = 1; i < this.params.length; i++) {
                matExpr = matExpr.diff((String) this.params[i]);
            }
        } else {
            int k = (int) this.params[2];
            for (int i = 0; i < k; i++) {
                matExpr = matExpr.diff((String) this.params[1]);
            }
        }
        return matExpr;

    }

    /**
     * Vereinfacht den Divergenzoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyMatrixOperator(type = TypeMatrixOperator.div)
    private MatrixExpression simplifyBasicDiv() throws EvaluationException {

        if (!((MatrixExpression) this.params[0]).isMatrix()) {
            return this;
        }
        
        Matrix m = (Matrix) this.params[0];
        Expression div = Expression.ZERO;
        
        for (int i = 1; i < this.params.length; i++) {
            div = div.add(m.getEntry(i - 1, 0).diff((String) this.params[i]));
        }

        return new Matrix(div);

    }

    /**
     * Vereinfacht den Gradientenoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyMatrixOperator(type = TypeMatrixOperator.grad)
    private MatrixExpression simplifyBasicGrad() throws EvaluationException {

        MatrixExpression expr = ((MatrixExpression) this.params[0]).simplify();
        Object exprConverted = expr.convertOneTimesOneMatrixToExpression();

        if (exprConverted instanceof Expression) {
            Expression[][] gradEntries = new Expression[this.params.length - 1][1];
            for (int i = 1; i < this.params.length; i++) {
                gradEntries[i - 1][0] = ((Expression) exprConverted).diff((String) this.params[i]);
            }
            return new Matrix(gradEntries);
        }

        // Dann kann nichts vereinfacht werden.
        throw new EvaluationException(Translator.translateOutputMessage(MEB_MatrixOperator_GRAD_ONLY_POSSIBLE_FOR_1_X_1_MATRICES));

    }

    /**
     * Vereinfacht den Integraloperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyMatrixOperator(type = TypeMatrixOperator.integral)
    private MatrixExpression simplifyBasicInt() throws EvaluationException {

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
    @SimplifyMatrixOperator(type = TypeMatrixOperator.laplace)
    private MatrixExpression simplifyBasicLaplace() throws EvaluationException {

        Dimension dim = ((MatrixExpression) this.params[0]).getDimension();
        MatrixExpression result = MatrixExpression.getZeroMatrix(dim.height, dim.width);
        MatrixExpression matExpr = (MatrixExpression) this.params[0];

        for (int i = 1; i < this.params.length; i++) {
            result = result.add(matExpr.diff((String) this.params[i]).simplify().diff((String) this.params[i]).simplify());
        }

        return result;

    }

    /**
     * Vereinfacht den Produktoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyMatrixOperator(type = TypeMatrixOperator.prod)
    private MatrixExpression simplifyBasicProd() throws EvaluationException {

        MatrixExpression factor = (MatrixExpression) this.params[0];

        Dimension dim = factor.getDimension();
        if (dim.width != dim.height) {
            throw new EvaluationException(Translator.translateOutputMessage(MEB_MatrixOperator_FIRST_PARAMETER_IN_PROD_NOT_SQUARE_MATRIX));
        }

        if (((Expression) this.params[2]).isIntegerConstant() && ((Expression) this.params[3]).isIntegerConstant()) {
            BigInteger lowerLimit = ((Constant) ((Expression) this.params[2])).getBigIntValue();
            BigInteger upperLimit = ((Constant) ((Expression) this.params[3])).getBigIntValue();
            if (upperLimit.subtract(lowerLimit).compareTo(
                    BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_NUMBER_OF_MEMBERS_IN_SUM_OR_PRODUCT)) <= 0) {

                // Dann kann man das Produkt explizit ausschreiben.
                return AnalysisUtils.prod(factor, (String) this.params[1], lowerLimit, upperLimit);

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
     * Vereinfacht den Rotationsoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyMatrixOperator(type = TypeMatrixOperator.rot)
    private MatrixExpression simplifyBasicRot() throws EvaluationException {

        MatrixExpression matExpr = ((MatrixExpression) this.params[0]).simplify();

        if (!matExpr.isMatrix()) {
            return this;
        }

        Dimension dim = ((Matrix) matExpr).getDimension();

        if (dim.height != 3 || dim.width != 1) {
            throw new EvaluationException(Translator.translateOutputMessage(MEB_MatrixOperator_WRONG_FORM_OF_PARAMETER_IN_OPERATOR_ROT));
        }

        Expression matExprX = ((Matrix) this.params[0]).getEntry(0, 0);
        Expression matExprY = ((Matrix) this.params[0]).getEntry(1, 0);
        Expression matExprZ = ((Matrix) this.params[0]).getEntry(2, 0);
        String varX = (String) this.params[1];
        String varY = (String) this.params[2];
        String varZ = (String) this.params[3];
        Expression[][] rotEntries = new Expression[3][1];

        rotEntries[0][0] = matExprZ.diff(varY).sub(matExprY.diff(varZ));
        rotEntries[1][0] = matExprX.diff(varZ).sub(matExprZ.diff(varX));
        rotEntries[2][0] = matExprY.diff(varX).sub(matExprX.diff(varY));

        return new Matrix(rotEntries);

    }

    /**
     * Vereinfacht den Summenoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyMatrixOperator(type = TypeMatrixOperator.sum)
    private MatrixExpression simplifyBasicSum() throws EvaluationException {

        MatrixExpression summand = (MatrixExpression) this.params[0];

        if (((Expression) this.params[2]).isIntegerConstant() && ((Expression) this.params[3]).isIntegerConstant()) {
            BigInteger lowerLimit = ((Constant) ((Expression) this.params[2])).getBigIntValue();
            BigInteger upperLimit = ((Constant) ((Expression) this.params[3])).getBigIntValue();
            if (upperLimit.subtract(lowerLimit).compareTo(
                    BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_NUMBER_OF_MEMBERS_IN_SUM_OR_PRODUCT)) <= 0) {

                // Dann kann man die Summe explizit ausschreiben.
                return AnalysisUtils.sum(summand, (String) this.params[1], lowerLimit, upperLimit);

            }
        }

        // Falls die Summanden von der Indexvariable nicht abhängen.
        if (!summand.contains((String) this.params[1])) {
            return new Matrix(((Expression) this.params[3]).add(Expression.ONE).sub((Expression) this.params[2])).mult(summand);
        }

        // Summen von Summen oder Differenzen aufteilen.
        if (summand.isSum() || summand.isDifference()) {
            return SimplifyMatrixOperatorUtils.splitSumOfSumsOrDifferences((MatrixBinaryOperation) summand, (String) this.params[1],
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
    public MatrixExpression simplifyComputeMatrixOperations() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof MatrixExpression) {
                resultParams[i] = ((MatrixExpression) this.params[i]).simplifyComputeMatrixOperations();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new MatrixOperator(this.type, resultParams, this.precise);
    }

    @Override
    public MatrixExpression simplifyMatrixEntries(Set<TypeSimplify> simplifyTypes) throws EvaluationException {
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
    public MatrixExpression simplifyCollectProducts() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof MatrixExpression) {
                resultParams[i] = ((MatrixExpression) this.params[i]).simplifyCollectProducts();
            } else if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyCollectProducts();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new MatrixOperator(this.type, resultParams, this.precise);
    }

    @Override
    public MatrixExpression simplifyFactorizeScalars() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof MatrixExpression) {
                resultParams[i] = ((MatrixExpression) this.params[i]).simplifyFactorizeScalars();
            } else if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyFactorize();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new MatrixOperator(this.type, resultParams, this.precise);
    }

    @Override
    public MatrixExpression simplifyFactorize() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof MatrixExpression) {
                resultParams[i] = ((MatrixExpression) this.params[i]).simplifyFactorize();
            } else if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyFactorize();
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
            } else if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyFunctionalRelations();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new MatrixOperator(this.type, resultParams, this.precise);
    }

}

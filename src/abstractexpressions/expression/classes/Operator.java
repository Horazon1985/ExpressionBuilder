package abstractexpressions.expression.classes;

import abstractexpressions.annotations.SimplifyOperator;
import abstractexpressions.expression.computation.AnalysisMethods;
import abstractexpressions.expression.computation.ArithmeticMethods;
import abstractexpressions.expression.computation.NumericalMethods;
import computationbounds.ComputationBounds;
import enums.TypeExpansion;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.utilities.SimplifyOperatorMethods;
import abstractexpressions.expression.integration.SimplifyIntegralMethods;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import operationparser.OperationParser;
import lang.translator.Translator;

public class Operator extends Expression {

    private TypeOperator type;
    private Object[] params;
    private boolean precise;

    // Patterns für die einzelnen Operatoren.
    public static final String PATTERN_DIFF = "diff(expr,var+)";
    public static final String PATTERN_DIFF_WITH_ORDER = "diff(expr,var,integer(0,2147483647))";
    public static final String PATTERN_DIV = "div(expr,uniquevar+)";
    public static final String PATTERN_FAC = "fac(expr)";
    public static final String PATTERN_FOURIER = "fourier(expr,var(!2,!3),expr,expr,integer(0,2147483647))";
    public static final String PATTERN_GCD = "gcd(expr+)";
    public static final String PATTERN_INT_INDEF = "int(expr,var)";
    public static final String PATTERN_INT_DEF = "int(expr,var(!2,!3),expr,expr)";
    public static final String PATTERN_LCM = "lcm(expr+)";
    public static final String PATTERN_LAPLACE = "laplace(expr,uniquevar+)";
    public static final String PATTERN_MAX = "max(expr,expr+)";
    public static final String PATTERN_MIN = "min(expr,expr+)";
    public static final String PATTERN_MOD = "mod(expr,expr)";
    public static final String PATTERN_MU = "mu(expr+)";
    public static final String PATTERN_PROD = "prod(expr,var(!2,!3),expr,expr)";
    public static final String PATTERN_SIGMA = "sigma(expr+)";
    public static final String PATTERN_SUM = "sum(expr,var(!2,!3),expr,expr)";
    public static final String PATTERN_TAYLOR = "taylor(expr,var(!2),expr,integer(0,2147483647))";
    public static final String PATTERN_VAR = "var(expr+)";

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

        // Sonderfälle: überladene Operatoren.
        switch (type) {
            case diff:
                if (params.length != 3) {
                    return OperationParser.parseDefaultOperator(operator, params, vars, PATTERN_DIFF);
                }
                try {
                    return OperationParser.parseDefaultOperator(operator, params, vars, PATTERN_DIFF);
                } catch (ExpressionException e) {
                    try {
                        return OperationParser.parseDefaultOperator(operator, params, vars, PATTERN_DIFF_WITH_ORDER);
                    } catch (ExpressionException ex) {
                        throw new ExpressionException(Translator.translateOutputMessage("EB_Operator_3_PARAMETER_IN_DIFF_IS_INVALID"));
                    }
                }
            case integral:
                if (params.length <= 2) {
                    return OperationParser.parseDefaultOperator(operator, params, vars, PATTERN_INT_INDEF);
                }
                return OperationParser.parseDefaultOperator(operator, params, vars, PATTERN_INT_DEF);
        }

        // Mittels Reflection das passende Pattern suchen.
        Field[] fields = Operator.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.getType().equals(String.class) && Modifier.isStatic(field.getModifiers()) && ((String) field.get(null)).startsWith(operator)) {
                    return OperationParser.parseDefaultOperator(operator, params, vars, (String) field.get(null));
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
            }
        }

        throw new ExpressionException("EB_Operator_INVALID_OPERATOR");

    }

    @Override
    public Expression copy() {
        return new Operator(this.type, this.params, this.precise);
    }

    @Override
    public double evaluate() throws EvaluationException {

        /*
         Bei der Auswertung von Operatoren wird zunächst versucht, den
         Operqator soweit wie möglich zu vereinfachen. Falls das Ergebnis noch
         Operatoren enthält, so wird ein Fehler geworfen.
         */
        Expression operatorSimplified = this.simplifyTrivial();
        if (operatorSimplified.containsOperator()) {
            // Falls immer noch Operatoren auftreten -> keine explizite Auswertung möglich.
            throw new EvaluationException(Translator.translateOutputMessage("EB_Operator_OPERATOR_CANNOT_BE_EVALUATED"));
        }

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
    public void addContainedVars(HashSet<String> vars) {

        /*
         Bei bestimmter Integration/Summen/Produkten zählt die
         Integrationsvariable/der Index NICHT als vorkommende Variable.
         */
        if (this.type.equals(TypeOperator.integral) && this.params.length == 2) {
            ((Expression) this.params[0]).addContainedVars(vars);
            return;
        }
        if (this.type.equals(TypeOperator.integral) && this.params.length == 4) {
            String var = (String) this.params[1];
            ((Expression) this.params[0]).addContainedVars(vars);
            vars.remove(var);
            ((Expression) this.params[2]).addContainedVars(vars);
            ((Expression) this.params[3]).addContainedVars(vars);
            return;
        }
        if (this.type.equals(TypeOperator.prod) || this.type.equals(TypeOperator.sum)) {
            String index = (String) this.params[1];
            ((Expression) this.params[0]).addContainedVars(vars);
            vars.remove(index);
            ((Expression) this.params[2]).addContainedVars(vars);
            ((Expression) this.params[3]).addContainedVars(vars);
            return;
        }

        // Alle anderen möglichen Operatoren
        for (Object param : this.params) {
            if (param instanceof Expression) {
                ((Expression) param).addContainedVars(vars);
            }
        }

    }

    @Override
    public void addContainedIndeterminates(HashSet<String> vars) {
        /*
         Bei bestimmter Integration/Summen/Produkten zählt die
         Integrationsvariable/der Index NICHT als vorkommende Variable.
         */
        if (this.type.equals(TypeOperator.integral) && this.params.length == 2) {
            ((Expression) this.params[0]).addContainedIndeterminates(vars);
            // Integrationsvariable wird mitgezählt!
            vars.add((String) this.params[1]);
            return;
        }
        if (this.type.equals(TypeOperator.integral) && this.params.length == 4) {
            String var = (String) this.params[1];
            ((Expression) this.params[0]).addContainedIndeterminates(vars);
            // Integrationsvariable wird nicht mitgezählt!
            vars.remove(var);
            ((Expression) this.params[2]).addContainedIndeterminates(vars);
            ((Expression) this.params[3]).addContainedIndeterminates(vars);
            return;
        }
        if (this.type.equals(TypeOperator.prod) || this.type.equals(TypeOperator.sum)) {
            String index = (String) this.params[1];
            ((Expression) this.params[0]).addContainedIndeterminates(vars);
            vars.remove(index);
            ((Expression) this.params[2]).addContainedIndeterminates(vars);
            ((Expression) this.params[3]).addContainedIndeterminates(vars);
            return;
        }

        // Alle anderen möglichen Operatoren
        for (Object param : this.params) {
            if (param instanceof Expression) {
                ((Expression) param).addContainedIndeterminates(vars);
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
    public boolean containsOperator() {
        return true;
    }

    @Override
    public boolean containsOperator(TypeOperator type) {
        return this.type.equals(type);
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

        if (this.type.equals(TypeOperator.fac) && !((Expression) this.params[0]).contains(var)) {
            return Expression.ZERO;
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
        throw new EvaluationException(Translator.translateOutputMessage("EB_Operator_EXPRESSION_IS_NOT_DIFFERENTIABLE", this, var));

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
        throw new EvaluationException(Translator.translateOutputMessage("EB_Operator_EXPRESSION_IS_NOT_DIFFERENTIABLE", this, var));

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
            ((Expression) this.params[0]).addContainedVars(varsInParameters);
            varsInParameters.remove((String) this.params[1]);
            ((Expression) this.params[2]).addContainedVars(varsInParameters);
            ((Expression) this.params[3]).addContainedVars(varsInParameters);

            return varsInParameters.isEmpty();

        }

        if (this.type.equals(TypeOperator.prod) || this.type.equals(TypeOperator.sum)) {

            HashSet<String> varsInParameters = new HashSet<>();
            ((Expression) this.params[0]).addContainedVars(varsInParameters);
            varsInParameters.remove((String) this.params[1]);
            ((Expression) this.params[2]).addContainedVars(varsInParameters);
            ((Expression) this.params[3]).addContainedVars(varsInParameters);
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
    public int length() {
        Object[] arguments = ((Operator) this).getParams();
        int length = 0;
        for (Object argument : arguments) {
            if (argument instanceof Expression) {
                length += ((Expression) argument).length();
            } else {
                length++;
            }
        }
        return length;
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
    public Expression orderDifferencesAndQuotients() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).orderDifferencesAndQuotients();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyCollectProducts() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyCollectProducts();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyFactorize() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyFactorize();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyFactorizeAllButRationalsInSums() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyFactorizeAllButRationalsInSums();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyFactorizeAllButRationalsInDifferences() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyFactorizeAllButRationalsInDifferences();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyFactorizeAllButRationals() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyFactorizeAllButRationals();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyReduceQuotients() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyReduceQuotients();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyExpandRationalFactors() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyExpandRationalFactors();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyExpand(TypeExpansion type) throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyExpand(type);
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyReduceLeadingsCoefficients() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyReduceLeadingsCoefficients();
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

        // Mittels Reflection die passende Ausführmethode ermittln (durch Vergleich der Annotation).
        Method[] methods = Operator.class.getDeclaredMethods();
        SimplifyOperator annotation;
        for (Method method : methods) {
            annotation = method.getAnnotation(SimplifyOperator.class);
            if (annotation != null && annotation.type().equals(this.type)) {
                try {
                    return (Expression) method.invoke(operator);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    if (e.getCause() instanceof EvaluationException) {
                        // Methoden können nur EvaluationExceptions werfen.
                        throw (EvaluationException) e.getCause();
                    }
                    throw new EvaluationException(Translator.translateOutputMessage("EB_Operator_INVALID_OPERATOR"));
                }
            }
        }

        return operator;

    }

    @Override
    public Expression simplifyByInsertingDefinedVars() throws EvaluationException {
        Object[] paramsEvaluated = new Object[this.params.length];

        /*
         Bei den Operator INT, PROD und SUM sollen die (lokalen) Variablen
         (Integrationsvariable/Index) NICHT ausgewertet werden; Daher die
         Strategie: Erst die lokale Variable aus vars entfernen (sofern
         vorhanden), dann evaluateByInsertingDefinedVars() anwenden, dann wieder hinzufügen.
         */
        if (this.type.equals(TypeOperator.integral) || this.type.equals(TypeOperator.prod) || this.type.equals(TypeOperator.sum)) {

            String localVar = (String) this.params[1];
            /*
             Falls die lokale variable (Indexvariable, Integrationsvariable) einen
             vordefinierten Wert besitzt, so soll dieser kurzzeitig vergessen werden.
             */
            Expression valueOfLocalVar = Variable.create(localVar).getPreciseExpression();
            Variable.setPreciseExpression(localVar, null);
            for (int i = 0; i < this.params.length; i++) {
                if (this.params[i] instanceof Expression) {
                    paramsEvaluated[i] = ((Expression) this.params[i]).simplifyByInsertingDefinedVars();
                } else {
                    paramsEvaluated[i] = this.params[i];
                }
            }
            // Den Wert der lokalen Variable wiederherstellen.
            Variable.setPreciseExpression(localVar, valueOfLocalVar);
            return new Operator(this.type, paramsEvaluated, this.precise);

        }

        // Alle anderen Operatoren.
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                paramsEvaluated[i] = ((Expression) this.params[i]).simplifyByInsertingDefinedVars();
            } else {
                paramsEvaluated[i] = this.params[i];
            }
        }
        return new Operator(this.type, paramsEvaluated, this.precise);
    }

    /**
     * Vereinfacht den Ableitungsoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyOperator(type = TypeOperator.diff)
    private Expression simplifyTrivialDiff() throws EvaluationException {

        Expression expr = (Expression) this.params[0];
        if (this.params.length == 3 && this.params[2] instanceof Integer) {
            int k = (int) this.params[2];
            for (int i = 0; i < k; i++) {
                expr = expr.diff((String) this.params[1]);
                expr = expr.simplify();
            }
        } else {
            for (int i = 1; i < this.params.length; i++) {
                expr = expr.diff((String) this.params[i]);
                expr = expr.simplify();
            }
        }
        return expr;

    }

    /**
     * Vereinfacht den Divergenzoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyOperator(type = TypeOperator.div)
    private Expression simplifyTrivialDiv() throws EvaluationException {

        Expression result = Expression.ZERO;
        Expression expr = (Expression) this.params[0];

        for (int i = 1; i < this.params.length; i++) {
            if (result.equals(Expression.ZERO)) {
                result = expr.diff((String) this.params[i]);
            } else {
                result = result.add(expr.diff((String) this.params[i]));
            }
        }

        return result;

    }

    /**
     * Vereinfacht den Fakultätoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyOperator(type = TypeOperator.fac)
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
                throw new EvaluationException(Translator.translateOutputMessage("EB_Operator_FACULTIES_OF_NEGATIVE_INTEGERS_UNDEFINED"));
            }
            if (argumentRoundedDown.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_MAX_INTEGER_FACTORIAL)) <= 0) {
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
     * Vereinfacht den Fourieroperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyOperator(type = TypeOperator.fourier)
    private Expression simplifyTrivialFourier() throws EvaluationException {

        Expression f = ((Expression) this.params[0]).simplify();
        String var = (String) this.params[1];
        Expression startPoint = ((Expression) this.params[2]).simplify();
        Expression endPoint = ((Expression) this.params[3]).simplify();
        int degree = (int) this.params[4];

        return AnalysisMethods.getFourierPolynomial(f, var, startPoint, endPoint, degree);

    }

    /**
     * Vereinfacht den gcd-Operator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyOperator(type = TypeOperator.gcd)
    private Expression simplifyTrivialGCD() throws EvaluationException {

        Expression[] arguments = new Expression[this.params.length];
        ArrayList<BigInteger> integerArguments = new ArrayList<>();
        for (int i = 0; i < this.params.length; i++) {
            arguments[i] = ((Expression) this.params[i]).simplify();
            if (arguments[i].isIntegerConstant()) {
                integerArguments.add(((Constant) arguments[i]).getValue().toBigInteger());
            } else if (arguments[i].isConstant()) {
                throw new EvaluationException(Translator.translateOutputMessage("EB_Operator_GENERAL_PARAMETER_IN_GCD_IS_NOT_INTEGER", i + 1));
            }
        }

        if (integerArguments.isEmpty()) {
            return new Operator(this.type, arguments);
        }

        Constant resultGCD = new Constant(ArithmeticMethods.gcd(integerArguments));
        if (this.params.length == integerArguments.size()) {
            return resultGCD;
        }

        ArrayList<Expression> resultParams = new ArrayList<>();
        resultParams.add(resultGCD);
        for (Expression argument : arguments) {
            if (!argument.isIntegerConstant()) {
                resultParams.add(argument);
            }
        }

        Object[] resultParamsAsArray = new Object[1];
        return new Operator(this.type, resultParams.toArray(resultParamsAsArray));

    }

    /**
     * Vereinfacht den Integraloperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyOperator(type = TypeOperator.integral)
    private Expression simplifyTrivialInt() throws EvaluationException {

        if (this.params.length == 2) {
            return SimplifyIntegralMethods.integrateIndefinite(this);
        }

        if (this.precise && this.params.length == 4) {
            return SimplifyIntegralMethods.integrateDefinite(this);
        }

        if (!this.precise && this.params.length == 4) {

            // Falls das Integral keine Parameter enthält, kann es direkt ausgerechnet werden.
            if (((Expression) this.params[2]).isConstant() && ((Expression) this.params[3]).isConstant()) {
                Expression expr = (Expression) this.params[0];
                HashSet<String> varsInIntegrand = new HashSet<>();
                expr.addContainedVars(varsInIntegrand);
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
    @SimplifyOperator(type = TypeOperator.laplace)
    private Expression simplifyTrivialLaplace() throws EvaluationException {

        Expression result = Expression.ZERO;
        Expression expr = (Expression) this.params[0];
        HashSet<String> vars = new HashSet<>();
        expr.addContainedVars(vars);
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
    @SimplifyOperator(type = TypeOperator.lcm)
    private Expression simplifyTrivialLCM() throws EvaluationException {

        Expression[] arguments = new Expression[this.params.length];
        ArrayList<BigInteger> integerArguments = new ArrayList<>();
        for (int i = 0; i < this.params.length; i++) {
            arguments[i] = ((Expression) this.params[i]).simplify();
            if (arguments[i].isIntegerConstant()) {
                integerArguments.add(((Constant) arguments[i]).getValue().toBigInteger());
            } else if (arguments[i].isConstant()) {
                throw new EvaluationException(Translator.translateOutputMessage("EB_Operator_GENERAL_PARAMETER_IN_LCM_IS_NOT_INTEGER", i + 1));
            }
        }

        if (integerArguments.isEmpty()) {
            return new Operator(this.type, arguments);
        }

        Constant resultLCM = new Constant(ArithmeticMethods.lcm(integerArguments));
        if (this.params.length == integerArguments.size()) {
            return resultLCM;
        }

        ArrayList<Expression> resultParams = new ArrayList<>();
        resultParams.add(resultLCM);
        for (Expression argument : arguments) {
            if (!argument.isIntegerConstant()) {
                resultParams.add(argument);
            }
        }

        Object[] resultParamsAsArray = new Object[1];
        return new Operator(this.type, resultParams.toArray(resultParamsAsArray));

    }

    /**
     * Vereinfacht den max-Operator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyOperator(type = TypeOperator.max)
    private Expression simplifyTrivialMax() throws EvaluationException {
        Expression[] arguments = new Expression[this.params.length];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = ((Expression) this.params[i]).simplify();
        }
        return getMaxExplicitly(arguments);
    }

    private Expression getMaxExplicitly(Expression[] exprs) {

        if (exprs.length == 2) {
            return exprs[0].add(exprs[1]).div(2).add(exprs[0].sub(exprs[1]).abs().div(2));
        }

        Expression[] exprsPrevious = new Expression[exprs.length - 1];
        for (int i = 0; i < exprs.length - 1; i++) {
            exprsPrevious[i] = exprs[i];
        }

        Expression maxPrevious = getMaxExplicitly(exprsPrevious);
        return maxPrevious.add(exprs[exprs.length - 1]).div(2).add(maxPrevious.sub(exprs[exprs.length - 1]).abs().div(2));

    }

    /**
     * Vereinfacht den min-Operator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyOperator(type = TypeOperator.min)
    private Expression simplifyTrivialMin() throws EvaluationException {
        Expression[] arguments = new Expression[this.params.length];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = ((Expression) this.params[i]).simplify();
        }
        return getMinExplicitly(arguments);
    }

    private Expression getMinExplicitly(Expression[] exprs) {

        if (exprs.length == 2) {
            return exprs[0].add(exprs[1]).div(2).sub(exprs[0].sub(exprs[1]).abs().div(2));
        }

        Expression[] exprsPrevious = new Expression[exprs.length - 1];
        for (int i = 0; i < exprs.length - 1; i++) {
            exprsPrevious[i] = exprs[i];
        }

        Expression maxPrevious = getMinExplicitly(exprsPrevious);
        return maxPrevious.add(exprs[exprs.length - 1]).div(2).sub(maxPrevious.sub(exprs[exprs.length - 1]).abs().div(2));

    }

    /**
     * Vereinfacht den mod-Operator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyOperator(type = TypeOperator.mod)
    private Expression simplifyTrivialMod() throws EvaluationException {

        Expression[] arguments = new Expression[this.params.length];
        ArrayList<BigInteger> integerArguments = new ArrayList<>();
        for (int i = 0; i < this.params.length; i++) {
            arguments[i] = ((Expression) this.params[i]).simplify();
            if (arguments[i].isIntegerConstant()) {
                integerArguments.add(((Constant) arguments[i]).getValue().toBigInteger());
            } else if (arguments[i].isConstant()) {
                throw new EvaluationException(Translator.translateOutputMessage("EB_Operator_GENERAL_PARAMETER_IN_MOD_IS_NOT_INTEGER", i + 1));
            }
        }

        if (integerArguments.size() == 2) {
            Constant resultMod = new Constant(ArithmeticMethods.mod(integerArguments.get(0), integerArguments.get(1)));
            return resultMod;
        }

        return new Operator(this.type, arguments);

    }

    /**
     * Vereinfacht den mu-Operator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyOperator(type = TypeOperator.mu)
    private Expression simplifyTrivialMu() throws EvaluationException {

        Expression[] arguments = new Expression[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            arguments[i] = ((Expression) this.params[i]).simplifyTrivial();
        }

        Expression result = ZERO;
        for (Expression argument : arguments) {
            result = result.add(argument);
        }

        return result.div(arguments.length);

    }

    /**
     * Vereinfacht den Produktoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyOperator(type = TypeOperator.prod)
    private Expression simplifyTrivialProd() throws EvaluationException {

        Expression factor = (Expression) this.params[0];

        if (((Expression) this.params[2]).isIntegerConstant() && ((Expression) this.params[3]).isIntegerConstant()) {
            BigInteger lowerLimit = ((Constant) ((Expression) this.params[2])).getValue().toBigInteger();
            BigInteger upperLimit = ((Constant) ((Expression) this.params[3])).getValue().toBigInteger();
            if (upperLimit.subtract(lowerLimit).compareTo(
                    BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_NUMBER_OF_MEMBERS_IN_SUM_OR_PRODUCT)) <= 0) {

                // Dann kann man die Summe explizit ausschreiben.
                return AnalysisMethods.prod(factor, (String) this.params[1], lowerLimit, upperLimit);

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

        // Falls die Faktoren Exponentialfunktionen sind.
        if (factor.isFunction(TypeFunction.exp)) {
            return SimplifyOperatorMethods.simplifyProductOfExponentialFunctions(factor, (String) this.params[1],
                    (Expression) this.params[2], (Expression) this.params[3]);
        }

        // Sonstiger Fall.
        Object[] resultParams = new Object[4];
        resultParams[0] = factor.simplifyTrivial();
        resultParams[1] = this.params[1];
        resultParams[2] = this.params[2];
        resultParams[3] = this.params[3];
        return new Operator(TypeOperator.prod, resultParams, this.precise);

    }

    /**
     * Vereinfacht den var-Operator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyOperator(type = TypeOperator.sigma)
    private Expression simplifyTrivialSigma() throws EvaluationException {

        Expression[] arguments = new Expression[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            arguments[i] = ((Expression) this.params[i]).simplifyTrivial();
        }

        Expression mu = new Operator(TypeOperator.mu, arguments);
        Expression result = ZERO;
        for (Expression argument : arguments) {
            result = result.add(argument.sub(mu).pow(2));
        }

        return result.div(arguments.length).pow(1, 2);

    }

    /**
     * Vereinfacht den Summenoperator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyOperator(type = TypeOperator.sum)
    private Expression simplifyTrivialSum() throws EvaluationException {

        Expression summand = (Expression) this.params[0];

        if (((Expression) this.params[2]).isIntegerConstant() && ((Expression) this.params[3]).isIntegerConstant()) {
            BigInteger lowerLimit = ((Constant) ((Expression) this.params[2])).getValue().toBigInteger();
            BigInteger upperLimit = ((Constant) ((Expression) this.params[3])).getValue().toBigInteger();
            if (upperLimit.subtract(lowerLimit).compareTo(
                    BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_NUMBER_OF_MEMBERS_IN_SUM_OR_PRODUCT)) <= 0) {

                // Dann kann man die Summe explizit ausschreiben.
                return AnalysisMethods.sum(summand, (String) this.params[1], lowerLimit, upperLimit);

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

        // Konstante Faktoren im Zähler und Nenner der Summanden herausziehen.
        if (summand.isProduct() || summand.isQuotient()) {
            return SimplifyOperatorMethods.takeConstantsOutOfSums((BinaryOperation) summand, (String) this.params[1],
                    (Expression) this.params[2], (Expression) this.params[3]);
        }

        // Falls die Summanden Logarithmusfunktionen sind.
        if (summand.isFunction(TypeFunction.lg)) {
            return SimplifyOperatorMethods.simplifySumOfLogarithmicFunctions(summand, (String) this.params[1],
                    (Expression) this.params[2], (Expression) this.params[3], TypeFunction.lg);
        }
        if (summand.isFunction(TypeFunction.ln)) {
            return SimplifyOperatorMethods.simplifySumOfLogarithmicFunctions(summand, (String) this.params[1],
                    (Expression) this.params[2], (Expression) this.params[3], TypeFunction.ln);
        }

        // Summen von Potenzen ganzer Zahlen explizit berechnen.
        Expression simplifiedOperator = SimplifyOperatorMethods.simplifySumOfPowersOfIntegers(summand, (String) this.params[1],
                (Expression) this.params[2], (Expression) this.params[3]);
        if (!simplifiedOperator.equals(this)) {
            return simplifiedOperator;
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
    @SimplifyOperator(type = TypeOperator.taylor)
    private Expression simplifyTrivialTaylor() throws EvaluationException {

        Expression f = (Expression) this.params[0];
        Expression centerPoint = (Expression) this.params[2];
        int degree = (int) this.params[3];
        return AnalysisMethods.getTaylorPolynomial(f, (String) this.params[1], centerPoint, degree);

    }

    /**
     * Vereinfacht den var-Operator, soweit es möglich ist.
     *
     * @throws EvaluationException
     */
    @SimplifyOperator(type = TypeOperator.var)
    private Expression simplifyTrivialVar() throws EvaluationException {

        Expression[] arguments = new Expression[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            arguments[i] = ((Expression) this.params[i]).simplifyTrivial();
        }

        Expression mu = new Operator(TypeOperator.mu, arguments);
        Expression result = ZERO;
        for (Expression argument : arguments) {
            result = result.add(argument.sub(mu).pow(2));
        }

        return result.div(arguments.length);

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
    public Expression simplifyExpandAndCollectEquivalentsIfShorter() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyExpandAndCollectEquivalentsIfShorter();
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
    public Expression simplifyPullApartPowers() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyPullApartPowers();
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyMultiplyExponents() throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyMultiplyExponents();
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
    public Expression simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(String var) throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(var);
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
    public Expression simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(String var) throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(var);
            } else {
                resultParams[i] = this.params[i];
            }
        }
        return new Operator(this.type, resultParams, this.precise);
    }

    @Override
    public Expression simplifyExpandProductsOfComplexExponentialFunctions(String var) throws EvaluationException {
        Object[] resultParams = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            if (this.params[i] instanceof Expression) {
                resultParams[i] = ((Expression) this.params[i]).simplifyExpandProductsOfComplexExponentialFunctions(var);
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

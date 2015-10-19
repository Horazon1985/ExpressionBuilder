package expressionbuilder;

import enumerations.TypeExpansion;
import exceptions.EvaluationException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import translator.Translator;

public class Variable extends Expression {

    private static HashMap<String, Variable> variables = new HashMap<>();
    private static HashMap<String, Variable> preciseExpressions = new HashMap<>();
    private final String name;
    private double value;
    private Expression preciseExpression;
    /*
     Sinn vom Attribut "precise": Die Variablen gelten zunächst alle als
     Präzise, d. h. sie können exakte Werte annehmen wie 3/7 oder pi. Im
     Approximationsmodus wird zunächst im gesamten Ausdruck in allen
     vorkommenden Variablen der precise-Wert auf false gesetzt (und nach
     Beenden des Approximationsmodus wieder auf true). Dies bewirkt, dass der
     exakte Wert (welcher in preciseExpression gespeichert ist) sofort
     (approximativ per evaluate()) ausgewertet und zurückgegeben wird.
     Benötigt wird dies eigentlich nur bei PI.
     */
    private boolean precise;

    public Variable(String name, double value) {
        this.name = name;
        this.value = value;
        this.preciseExpression = new Constant(new BigDecimal(value));
        this.precise = true;
    }

    public Variable(String name, Expression preciseExpression) {
        this.name = name;
        try {
            this.value = preciseExpression.evaluate();
        } catch (EvaluationException e) {
            this.value = Double.NaN;
        }
        this.preciseExpression = preciseExpression;
        this.precise = true;
    }

    public String getName() {
        return this.name;
    }

    public double getValue() {
        return this.value;
    }

    public Expression getPreciseExpression() {
        return this.preciseExpression;
    }

    public boolean getPrecise() {
        return this.precise;
    }

    /**
     * Methode create: ohne Wertzuweisung (d.h. die Variable wird automatisch
     * auf 0 gesetzt)
     */
    public static Variable create(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else {
            if (name.equals("pi")) {
                Variable result = new Variable("pi", Math.PI);
                variables.put(name, result);
                return result;
            } else {
                Variable result = new Variable(name, 0);
                variables.put(name, result);
                preciseExpressions.put(name, result);
                return result;
            }
        }
    }

    /**
     * Methode create: mit Wertzuweisung
     */
    public static Variable create(String name, double value) {
        if (variables.containsKey(name)) {
            variables.get(name).value = value;
            return variables.get(name);
        } else {
            if (name.equals("pi")) {
                Variable result = new Variable("pi", Math.PI);
                variables.put(name, result);
                return result;
            } else {
                Variable result = new Variable(name, value);
                variables.put(name, result);
                preciseExpressions.put(name, result);
                return result;
            }
        }
    }

    /**
     * Methode create: mit Wertzuweisung als exakten Ausdruck
     */
    public static Variable create(String name, Expression preciseExpression) {
        if (variables.containsKey(name)) {
            variables.get(name).preciseExpression = preciseExpression;
            return variables.get(name);
        } else {
            if (name.equals("pi")) {
                Variable result = new Variable("pi", Math.PI);
                variables.put(name, result);
                return result;
            } else {
                Variable result = new Variable(name, preciseExpression);
                variables.put(name, result);
                preciseExpressions.put(name, result);
                return result;
            }
        }
    }

    public static void setValue(String name, double value) throws EvaluationException {
        // PI darf nicht verändert werden.
        if (name.equals("pi")) {
            if (variables.containsKey("pi")) {
                variables.get("pi").value = Math.PI;
            } else {
                Variable.create("pi", Math.PI);
            }
        }
        if (variables.containsKey(name)) {
            variables.get(name).value = value;
            preciseExpressions.get(name).preciseExpression = new Constant(value);
        } else {
            Variable.create(name, value);
        }
    }

    public static void setPreciseExpression(String name, Expression preciseExpression) {
        // PI darf nicht verändert werden.
        if (name.equals("pi")) {
            if (variables.containsKey("pi")) {
                variables.get("pi").value = Math.PI;
            } else {
                Variable.create("pi", Math.PI);
            }
        } else if (preciseExpressions.containsKey(name)) {
            try {
                variables.get(name).value = preciseExpression.evaluate();
            } catch (EvaluationException e) {
                variables.get(name).value = Double.NaN;
            }
            preciseExpressions.get(name).preciseExpression = preciseExpression;
        } else {
            Variable.create(name, preciseExpression);
        }
    }

    public static void setPrecise(String name, boolean precise) {
        if (variables.containsKey(name)) {
            variables.get(name).precise = precise;
            if (!name.equals("pi")) {
                preciseExpressions.get(name).precise = precise;
            }
        } else {
            Variable.create(name);
            variables.get(name).precise = precise;
            if (!name.equals("pi")) {
                preciseExpressions.get(name).precise = precise;
            }
        }
    }

    public static void setAllPrecise(boolean precise) {
        for (String var : variables.keySet()) {
            variables.get(var).precise = precise;
        }
        for (String var : preciseExpressions.keySet()) {
            if (!var.equals("pi")) {
                preciseExpressions.get(var).precise = precise;
            }
        }
    }

    @Override
    public Expression copy() {
        return Variable.create(this.name, this.value);
    }

    @Override
    public double evaluate() throws EvaluationException {
        if (!Double.isNaN(this.value) && !Double.isInfinite(this.value)) {
            return this.value;
        }
        throw new EvaluationException(Translator.translateExceptionMessage("EB_Variable_VARIABLE_HAS_UNDEFINED_VALUE_1")
                + this.name
                + Translator.translateExceptionMessage("EB_Variable_VARIABLE_HAS_UNDEFINED_VALUE_2"));
    }

    @Override
    public Expression evaluate(HashSet<String> vars) throws EvaluationException {
        if (vars.contains(this.name)) {
            return this.preciseExpression;
        }
        return this;
    }

    @Override
    public void addContainedVars(HashSet<String> vars) {
        if (!this.name.equals("pi")) {
            vars.add(this.name);
        }
    }

    @Override
    public boolean contains(String var) {
        return this.name.equals(var);
    }

    @Override
    public boolean containsApproximates() {
        return false;
    }

    @Override
    public boolean containsFunction() {
        return false;
    }

    @Override
    public boolean containsExponentialFunction() {
        return false;
    }

    @Override
    public boolean containsTrigonometricalFunction() {
        return false;
    }

    @Override
    public boolean containsIndefiniteIntegral() {
        return false;
    }

    @Override
    public boolean containsOperator() {
        return false;
    }

    @Override
    public Expression turnToApproximate() {
        Variable.setPrecise(this.name, false);
        return this;
    }

    @Override
    public Expression turnToPrecise() {
        Variable.setPrecise(this.name, true);
        return this;
    }

    @Override
    public Expression replaceVariable(String var, Expression expr) {
        if (this.name.equals(var)) {
            return expr;
        }
        return this;
    }

    @Override
    public Expression replaceSelfDefinedFunctionsByPredefinedFunctions() {
        return this;
    }

    @Override
    public Expression diff(String var) {

        //Partielles Differenzieren nach der Variablen var
        if (this.name.equals(var)) {
            if (this.precise) {
                return Expression.ONE;
            }
            return Expression.ONE.turnToApproximate();
        } else {
            if (this.precise) {
                return Expression.ZERO;
            }
            return Expression.ZERO.turnToApproximate();
        }

    }

    @Override
    public Expression diffDifferentialEquation(String var) {

        /*
         Differenzieren einer Differentialgleichung nach Variable var. Alle
         anderen Variablen außer var werden als Funktionen von var aufgefasst.
         -> Ableitung einer Variablen y (!= var) wird dann zu y', die von y'
         zu y'' etc.
         */
        if (this.name.equals(var)) {
            if (this.precise) {
                return Expression.ONE;
            }
            return Expression.ONE.turnToApproximate();
        } else {
            String varName = this.getName();
            if (this.precise) {
                return Variable.create(varName + "'");
            }
            return Variable.create(varName + "'").turnToApproximate();
        }

    }

    @Override
    public String writeExpression() {
        return name;
    }

    @Override
    public String expressionToLatex() {
        if (!this.name.equals("pi")) {
            return this.name;
        }
        return "\\" + "pi";
    }

    @Override
    public boolean isConstant() {
        return this.name.equals("pi");
    }

    @Override
    public boolean isNonNegative() {
        return false;
    }

    @Override
    public boolean isAlwaysNonNegative() {
        return this.name.equals("pi");
    }

    @Override
    public boolean isAlwaysPositive() {
        return this.name.equals("pi");
    }

    @Override
    public boolean equals(Expression expr) {
        if (expr instanceof Variable) {
            return this.getName().equals(((Variable) expr).getName());
        }
        return false;
    }

    @Override
    public boolean equivalent(Expression expr) {
        if (expr instanceof Variable) {
            Variable v = (Variable) expr;
            return this.getName().equals(v.getName());
        }
        return false;
    }

    @Override
    public boolean hasPositiveSign() {
        return true;
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public Expression simplifyTrivial() throws EvaluationException {
        if (this.name.equals("pi") && !this.precise) {
            return new Constant(Math.PI);
        }
        return this;
    }

    @Override
    public Expression simplifyExpandRationalFactors() {
        return this;
    }

    @Override
    public Expression simplifyExpand(TypeExpansion type) {
        return this;
    }

    @Override
    public Expression simplifyReduceLeadingsCoefficients() {
        return this;
    }

    @Override
    public Expression orderSumsAndProducts() {
        return this;
    }

    @Override
    public Expression orderDifferencesAndQuotients() {
        return this;
    }

    @Override
    public Expression simplifyCollectProducts() {
        return this;
    }

    @Override
    public Expression simplifyFactorizeInSums() {
        return this;
    }

    @Override
    public Expression simplifyFactorizeInDifferences() {
        return this;
    }

    @Override
    public Expression simplifyFactorizeAllButRationalsInSums() {
        return this;
    }

    @Override
    public Expression simplifyFactorizeAllButRationalsInDifferences() {
        return this;
    }

    @Override
    public Expression simplifyReduceQuotients() {
        return this;
    }

    @Override
    public Expression simplifyPowers() {
        return this;
    }

    @Override
    public Expression simplifyMultiplyPowers() {
        return this;
    }

    @Override
    public Expression simplifyFunctionalRelations() {
        return this;
    }

    @Override
    public Expression simplifyExpandAndCollectEquivalentsIfShorter() {
        return this;
    }

    @Override
    public Expression simplifyCollectLogarithms() {
        return this;
    }

    @Override
    public Expression simplifyExpandLogarithms() {
        return this;
    }

    @Override
    public Expression simplifyReplaceExponentialFunctionsByDefinitions() {
        return this;
    }

    @Override
    public Expression simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(String var) {
        return this;
    }

    @Override
    public Expression simplifyReplaceTrigonometricalFunctionsByDefinitions() {
        return this;
    }

    @Override
    public Expression simplifyExpandProductsOfComplexExponentialFunctions(String var) {
        return this;
    }

    @Override
    public Expression simplifyAlgebraicExpressions() throws EvaluationException {
        return this;
    }

}

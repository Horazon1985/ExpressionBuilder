package expressionbuilder;

import enumerations.TypeExpansion;
import exceptions.EvaluationException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import translator.Translator;

public class Constant extends Expression {

    /*
     approxValue wird gebraucht, wenn es um approximative Berechnungen geht. Dann
     wird die Konstante als irrational angesehen. Ansonsten wird value
     verwendet und die Konstante gilt als rational.
     */
    private double approxValue;
    private BigDecimal value;
    private boolean precise;

    // Verschiedene Konstruktoren
    public Constant(double approxValue) throws EvaluationException {
        if (Double.isNaN(approxValue) || Double.isInfinite(approxValue)) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_Constant_CONSTANT_CANNOT_BE_EVALUATED"));
        }
        this.approxValue = approxValue;
        this.value = BigDecimal.valueOf(this.approxValue);
        this.precise = false;
    }

    public Constant(int value) {
        this.approxValue = (double) value;
        this.value = BigDecimal.valueOf(value);
        this.precise = true;
    }

    public Constant(BigDecimal value) {
        this.approxValue = value.doubleValue();
        this.value = value;
        this.precise = true;
    }

    public Constant(BigInteger value) {
        this.approxValue = new BigDecimal(value).doubleValue();
        this.value = new BigDecimal(value);
        this.precise = true;
    }

    public Constant(String value) {
        BigDecimal valueAsBigdecimal;
        try {
            valueAsBigdecimal = new BigDecimal(value);
        } catch (NumberFormatException e) {
            this.approxValue = 0;
            this.value = BigDecimal.ZERO;
            this.precise = true;
            return;
        }
        this.approxValue = valueAsBigdecimal.doubleValue();
        this.value = valueAsBigdecimal;
        this.precise = true;
    }

    public Constant(BigDecimal value, boolean precise) {
        this.approxValue = value.doubleValue();
        this.value = value;
        this.precise = precise;
    }

    public double getApproxValue() {
        return this.approxValue;
    }

    public BigDecimal getValue() {
        return this.value;
    }

    public boolean getPrecise() {
        return this.precise;
    }

    public void setValue(double value) {
        this.approxValue = value;
    }

    public void setPreciseValue(BigDecimal preciseValue) {
        this.value = preciseValue;
    }

    public void setPrecise(boolean precise) {
        this.precise = precise;
    }

    /**
     * Ermittelt den Zähler und Nenner vom Bruch gekürzten Bruch
     * enumerator/denominator.
     */
    public static BigInteger[] reduceFraction(BigDecimal enumerator, BigDecimal denominator) {

        if (denominator.compareTo(BigDecimal.ZERO) < 0) {
            enumerator = enumerator.negate();
            denominator = denominator.negate();
        }

        BigDecimal reducedEnumerator = enumerator;
        BigDecimal reducedDenominator = denominator;

        while (!(reducedEnumerator.compareTo(reducedEnumerator.setScale(0, BigDecimal.ROUND_HALF_UP)) == 0)
                || !(reducedDenominator.compareTo(reducedDenominator.setScale(0, BigDecimal.ROUND_HALF_UP)) == 0)) {
            reducedEnumerator = reducedEnumerator.multiply(BigDecimal.TEN);
            reducedDenominator = reducedDenominator.multiply(BigDecimal.TEN);
        }

        BigInteger[] result = new BigInteger[2];
        result[0] = reducedEnumerator.toBigInteger().divide(reducedEnumerator.toBigInteger().gcd(reducedDenominator.toBigInteger()));
        result[1] = reducedDenominator.toBigInteger().divide(reducedEnumerator.toBigInteger().gcd(reducedDenominator.toBigInteger()));
        return result;

    }

    /**
     * Macht auch enumerator/denominator einen (gekürzten) Bruch (als
     * Expression)
     */
    public static Expression constantToQuotient(BigDecimal enumerator, BigDecimal denominator) {
        BigInteger[] reducedFraction = reduceFraction(enumerator, denominator);
        if (reducedFraction[1].equals(BigInteger.ONE)) {
            return new Constant(reducedFraction[0]);
        }
        return new Constant(reducedFraction[0]).div(reducedFraction[1]);
    }

    /**
     * Macht aus einem Ausdruck der Form c1 / c2 + c3 / c4 den Ausdruck (c1 * c4
     * + c2 * c3) / (c2 * c4)
     */
    public static Expression addFractionToFraction(BigDecimal c1, BigDecimal c2, BigDecimal c3, BigDecimal c4) {
        return new Constant((c1.multiply(c4)).add(c2.multiply(c3))).div(c2.multiply(c4));
    }

    /**
     * Macht aus einem Ausdruck der Form c1 + c2 / c3 den Ausdruck (c1 * c3 +
     * c2) / c3
     */
    public static Expression addFractionToConstant(BigDecimal c1, BigDecimal c2, BigDecimal c3) {
        return new Constant(c2.add(c1.multiply(c3))).div(c3);
    }

    /**
     * Addiert zwei rationale Zahlen (beide Argumente können entweder Konstanten
     * oder Quotienten von Konstanten sein). Falls eines der Argumente keine
     * (rationale) Konstante ist, dann liefert die Funktion einfach c_1 + c_2
     */
    public static Expression addTwoRationals(Expression c_1, Expression c_2) {

        if (!c_1.isIntegerConstantOrRationalConstant() || !c_2.isIntegerConstantOrRationalConstant()) {
            return c_1.add(c_2);
        }

        // c_1 und c_2 sind hier entweder Konstanten oder Quotienten von Konstanten.
        if ((c_1 instanceof Constant) && (c_2 instanceof Constant)) {
            return new Constant(((Constant) c_1).getValue().add(((Constant) c_2).getValue()));
        } else if (!(c_1 instanceof Constant) && (c_2 instanceof Constant)) {
            BigDecimal k_1 = ((Constant) ((BinaryOperation) c_1).getLeft()).getValue();
            BigDecimal k_2 = ((Constant) ((BinaryOperation) c_1).getRight()).getValue();
            BigDecimal k_3 = ((Constant) c_2).getValue();
            return addFractionToConstant(k_3, k_1, k_2);
        } else if ((c_1 instanceof Constant) && !(c_2 instanceof Constant)) {
            BigDecimal k_1 = ((Constant) c_1).getValue();
            BigDecimal k_2 = ((Constant) ((BinaryOperation) c_2).getLeft()).getValue();
            BigDecimal k_3 = ((Constant) ((BinaryOperation) c_2).getRight()).getValue();
            return addFractionToConstant(k_1, k_2, k_3);
        } else {
            BigDecimal k_1 = ((Constant) ((BinaryOperation) c_1).getLeft()).getValue();
            BigDecimal k_2 = ((Constant) ((BinaryOperation) c_1).getRight()).getValue();
            BigDecimal k_3 = ((Constant) ((BinaryOperation) c_2).getLeft()).getValue();
            BigDecimal k_4 = ((Constant) ((BinaryOperation) c_2).getRight()).getValue();
            return addFractionToFraction(k_1, k_2, k_3, k_4);
        }

    }

    /**
     * Multipliziert zwei rationale Zahlen (beide Argumente können entweder
     * Konstanten oder Quotienten von Konstanten sein). Falls eines der
     * Argumente keine (rationale) Konstante ist, dann liefert die Funktion
     * einfach c_1 * c_2
     */
    public static Expression multiplyTwoRationals(Expression c_1, Expression c_2) {

        if (!c_1.isIntegerConstantOrRationalConstant() || !c_2.isIntegerConstantOrRationalConstant()) {
            return c_1.mult(c_2);
        }

        // c_1 und c_2 sind hier entweder Konstanten oder Quotienten von Konstanten.
        if ((c_1 instanceof Constant) && (c_2 instanceof Constant)) {
            return new Constant(((Constant) c_1).getValue().multiply(((Constant) c_2).getValue()));
        } else if (!(c_1 instanceof Constant) && (c_2 instanceof Constant)) {
            BigDecimal k_1 = ((Constant) ((BinaryOperation) c_1).getLeft()).getValue();
            BigDecimal k_2 = ((Constant) ((BinaryOperation) c_1).getRight()).getValue();
            BigDecimal k_3 = ((Constant) c_2).getValue();
            return new Constant(k_1.multiply(k_3)).div(k_2);
        } else if ((c_1 instanceof Constant) && !(c_2 instanceof Constant)) {
            BigDecimal k_1 = ((Constant) c_1).getValue();
            BigDecimal k_2 = ((Constant) ((BinaryOperation) c_2).getLeft()).getValue();
            BigDecimal k_3 = ((Constant) ((BinaryOperation) c_2).getRight()).getValue();
            return new Constant(k_1.multiply(k_2)).div(k_3);
        } else {
            BigDecimal k_1 = ((Constant) ((BinaryOperation) c_1).getLeft()).getValue();
            BigDecimal k_2 = ((Constant) ((BinaryOperation) c_1).getRight()).getValue();
            BigDecimal k_3 = ((Constant) ((BinaryOperation) c_2).getLeft()).getValue();
            BigDecimal k_4 = ((Constant) ((BinaryOperation) c_2).getRight()).getValue();
            return new Constant(k_1.multiply(k_3)).div(k_2.multiply(k_4));
        }

    }

    @Override
    public Expression copy() {
        return new Constant(this.value, this.precise);
    }

    @Override
    public double evaluate() throws EvaluationException {
        if (Double.isNaN(this.approxValue) || Double.isInfinite(this.approxValue)) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_Constant_CONSTANT_CANNOT_BE_EVALUATED"));
        }
        return this.approxValue;
    }

    @Override
    public Expression evaluate(HashSet<String> vars) {
        return this;
    }

    @Override
    public void addContainedVars(HashSet<String> vars) {
    }

    @Override
    public boolean contains(String var) {
        return false;
    }

    @Override
    public boolean containsApproximates() {
        return !this.precise;
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
        return new Constant(this.getValue(), false);
    }

    @Override
    public Expression turnToPrecise() {
        return new Constant(this.getValue(), true);
    }

    @Override
    public Expression replaceVariable(String var, Expression expr) {
        return this;
    }

    @Override
    public Expression replaceSelfDefinedFunctionsByPredefinedFunctions() {
        return this;
    }

    @Override
    public Expression diff(String var) {
        if (this.precise) {
            return Expression.ZERO;
        }
        return Expression.ZERO.turnToApproximate();
    }

    @Override
    public Expression diffDifferentialEquation(String var) {
        if (this.precise) {
            return Expression.ZERO;
        }
        return Expression.ZERO.turnToApproximate();
    }

    @Override
    public String writeExpression() {

        if (this.precise) {

            //Falls preciseValue eine ganze Zahl ist -> value ohne Nachkommastellen ausgeben!
            if (this.isIntegerConstant()) {
                return this.value.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
            }
            return this.value.toPlainString();

        }
        /*
         Falls approximiert wird und this.value eine ganze Zahl ist -> value
         ohne Nachkommastellen ausgeben!
         */
        if (this.approxValue == Math.round(this.approxValue)) {
            return String.valueOf((long) this.approxValue);
        }
        return String.valueOf(this.approxValue);

    }

    @Override
    public String expressionToLatex() {
        return this.writeExpression();
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public boolean isNonNegative() {
        return this.value.compareTo(BigDecimal.ZERO) >= 0;
    }

    @Override
    public boolean isAlwaysNonNegative() {
        return this.value.compareTo(BigDecimal.ZERO) >= 0;
    }

    @Override
    public boolean isAlwaysPositive() {
        return this.value.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public boolean equals(Expression expr) {
        return expr instanceof Constant && this.precise == ((Constant) expr).getPrecise()
                && this.value.equals(((Constant) expr).getValue());
    }

    @Override
    public boolean equivalent(Expression expr) {
        return expr instanceof Constant && this.precise == ((Constant) expr).getPrecise()
                && this.value.equals(((Constant) expr).getValue());
    }

    @Override
    public boolean hasPositiveSign() {
        return this.getValue().compareTo(BigDecimal.ZERO) >= 0;
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public Expression simplifyTrivial() throws EvaluationException {
        if (this.precise) {
            return constantToQuotient(this.value, BigDecimal.ONE);
        }
        if (Double.isNaN(this.approxValue) || Double.isInfinite(this.approxValue)) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_Constant_CONSTANT_CANNOT_BE_EVALUATED"));
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
    public Expression simplifyPullApartPowers() {
        return this;
    }

    @Override
    public Expression simplifyMultiplyExponents() {
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
    public Expression simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(String var){
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

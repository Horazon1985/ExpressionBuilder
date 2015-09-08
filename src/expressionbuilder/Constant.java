package expressionbuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import translator.Translator;

public class Constant extends Expression {

    /**
     * value wird gebraucht, wenn es um approximative Berechnungen geht. Dann
     * wird die Konstante als irrational angesehen. Ansonsten wird preciseValue
     * verwendet und die Konstante gilt als rational.
     */
    private double value;
    private BigDecimal preciseValue;
    private boolean precise;

    /**
     * Verschiedene Konstruktoren
     */
    public Constant(double value) throws EvaluationException {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_Constant_CONSTANT_CANNOT_BE_EVALUATED"));
        }
        this.value = value;
        this.preciseValue = new BigDecimal(String.valueOf(this.value));
        this.precise = false;
    }

    public Constant(int value) {
        this.value = (double) value;
        this.preciseValue = new BigDecimal(value);
        this.precise = true;
    }

    public Constant(BigDecimal value) {
        this.value = value.doubleValue();
        this.preciseValue = value;
        this.precise = true;
    }

    public Constant(BigInteger value) {
        this.value = new BigDecimal(value).doubleValue();
        this.preciseValue = new BigDecimal(value);
        this.precise = true;
    }

    public Constant(String value) {
        BigDecimal valueAsBigdecimal;
        try {
            valueAsBigdecimal = new BigDecimal(value);
        } catch (NumberFormatException e) {
            this.value = 0;
            this.preciseValue = new BigDecimal("0");
            this.precise = true;
            return;
        }
        this.value = valueAsBigdecimal.doubleValue();
        this.preciseValue = valueAsBigdecimal;
        this.precise = true;
    }

    public Constant(BigDecimal value, boolean precise) {
        this.value = value.doubleValue();
        this.preciseValue = value;
        this.precise = precise;
    }

    public double getValue() {
        return this.value;
    }

    public BigDecimal getPreciseValue() {
        return this.preciseValue;
    }

    public boolean getPrecise() {
        return this.precise;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setPreciseValue(BigDecimal preciseValue) {
        this.preciseValue = preciseValue;
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
    public static Expression constantToQuotient(BigDecimal enumerator, BigDecimal denominator) throws EvaluationException {
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
            return new Constant(((Constant) c_1).getPreciseValue().add(((Constant) c_2).getPreciseValue()));
        } else if (!(c_1 instanceof Constant) && (c_2 instanceof Constant)) {
            BigDecimal k_1 = ((Constant) ((BinaryOperation) c_1).getLeft()).getPreciseValue();
            BigDecimal k_2 = ((Constant) ((BinaryOperation) c_1).getRight()).getPreciseValue();
            BigDecimal k_3 = ((Constant) c_2).getPreciseValue();
            return addFractionToConstant(k_3, k_1, k_2);
        } else if ((c_1 instanceof Constant) && !(c_2 instanceof Constant)) {
            BigDecimal k_1 = ((Constant) c_1).getPreciseValue();
            BigDecimal k_2 = ((Constant) ((BinaryOperation) c_2).getLeft()).getPreciseValue();
            BigDecimal k_3 = ((Constant) ((BinaryOperation) c_2).getRight()).getPreciseValue();
            return addFractionToConstant(k_1, k_2, k_3);
        } else {
            BigDecimal k_1 = ((Constant) ((BinaryOperation) c_1).getLeft()).getPreciseValue();
            BigDecimal k_2 = ((Constant) ((BinaryOperation) c_1).getRight()).getPreciseValue();
            BigDecimal k_3 = ((Constant) ((BinaryOperation) c_2).getLeft()).getPreciseValue();
            BigDecimal k_4 = ((Constant) ((BinaryOperation) c_2).getRight()).getPreciseValue();
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
            return new Constant(((Constant) c_1).getPreciseValue().multiply(((Constant) c_2).getPreciseValue()));
        } else if (!(c_1 instanceof Constant) && (c_2 instanceof Constant)) {
            BigDecimal k_1 = ((Constant) ((BinaryOperation) c_1).getLeft()).getPreciseValue();
            BigDecimal k_2 = ((Constant) ((BinaryOperation) c_1).getRight()).getPreciseValue();
            BigDecimal k_3 = ((Constant) c_2).getPreciseValue();
            return new Constant(k_1.multiply(k_3)).div(k_2);
        } else if ((c_1 instanceof Constant) && !(c_2 instanceof Constant)) {
            BigDecimal k_1 = ((Constant) c_1).getPreciseValue();
            BigDecimal k_2 = ((Constant) ((BinaryOperation) c_2).getLeft()).getPreciseValue();
            BigDecimal k_3 = ((Constant) ((BinaryOperation) c_2).getRight()).getPreciseValue();
            return new Constant(k_1.multiply(k_2)).div(k_3);
        } else {
            BigDecimal k_1 = ((Constant) ((BinaryOperation) c_1).getLeft()).getPreciseValue();
            BigDecimal k_2 = ((Constant) ((BinaryOperation) c_1).getRight()).getPreciseValue();
            BigDecimal k_3 = ((Constant) ((BinaryOperation) c_2).getLeft()).getPreciseValue();
            BigDecimal k_4 = ((Constant) ((BinaryOperation) c_2).getRight()).getPreciseValue();
            return new Constant(k_1.multiply(k_3)).div(k_2.multiply(k_4));
        }

    }

    /**
     * Gibt (-1)*expr zurück.
     */
    public static Expression negateExpression(Expression expr) {
        return MINUS_ONE.mult(expr);
    }

    @Override
    public Expression copy() {
        return new Constant(this.preciseValue, this.precise);
    }

    @Override
    public double evaluate() throws EvaluationException {
        if (Double.isNaN(this.value) || Double.isInfinite(this.value)) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_Constant_CONSTANT_CANNOT_BE_EVALUATED"));
        }
        return this.value;
    }

    @Override
    public Expression evaluate(HashSet<String> vars) throws EvaluationException {
        return this;
    }

    @Override
    public void getContainedVars(HashSet<String> vars) {
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
    public boolean containsIndefiniteIntegral() {
        return false;
    }

    @Override
    public Expression turnToApproximate() {
        Constant c = new Constant(this.getPreciseValue());
        c.setPrecise(false);
        return c;
    }

    @Override
    public Expression turnToPrecise() {
        Constant c = new Constant(this.getPreciseValue());
        c.setPrecise(true);
        return c;
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
                return this.preciseValue.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
            }
            return this.preciseValue.toPlainString();

        }
        /*
         Falls approximiert wird und this.value eine ganze Zahl ist -> value
         ohne Nachkommastellen ausgeben!
         */
        if (this.value == Math.round(this.value)) {
            return String.valueOf((long) this.value);
        }
        return String.valueOf(this.value);

    }

    @Override
    public String expressionToLatex(boolean beginning) {
        return this.writeExpression();
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public boolean isNonNegative() {
        return this.preciseValue.compareTo(BigDecimal.ZERO) >= 0;
    }

    @Override
    public boolean isAlwaysNonNegative() {
        return this.preciseValue.compareTo(BigDecimal.ZERO) >= 0;
    }
    
    @Override
    public boolean isAlwaysPositive() {
        return this.preciseValue.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public boolean equals(Expression expr) {
        return expr instanceof Constant && this.precise == ((Constant) expr).getPrecise()
                && this.preciseValue.equals(((Constant) expr).getPreciseValue());
    }

    @Override
    public boolean equivalent(Expression expr) {
        return expr instanceof Constant && this.precise == ((Constant) expr).getPrecise()
                && this.preciseValue.equals(((Constant) expr).getPreciseValue());
    }

    @Override
    public boolean hasPositiveSign() {
        return this.getPreciseValue().compareTo(BigDecimal.ZERO) >= 0;
    }

    @Override
    public Expression simplifyTrivial() throws EvaluationException {
        if (this.precise) {
            return constantToQuotient(this.preciseValue, BigDecimal.ONE);
        }
        if (Double.isNaN(this.value) || Double.isInfinite(this.value)) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_Constant_CONSTANT_CANNOT_BE_EVALUATED"));
        }
        return this;
    }

    @Override
    public Expression expandRationalFactors() throws EvaluationException {
        return this;
    }

    @Override
    public Expression expand() throws EvaluationException {
        return this;
    }

    @Override
    public Expression reduceLeadingsCoefficients() throws EvaluationException {
        return this;
    }

    @Override
    public Expression orderSumsAndProducts() {
        return this;
    }

    @Override
    public Expression orderDifferenceAndDivision() throws EvaluationException {
        return this;
    }

    @Override
    public Expression collectProducts() throws EvaluationException {
        return this;
    }

    @Override
    public Expression factorizeInSums() throws EvaluationException {
        return this;
    }

    @Override
    public Expression factorizeInDifferences() throws EvaluationException {
        return this;
    }

    @Override
    public Expression factorizeRationalsInSums() throws EvaluationException {
        return this;
    }

    @Override
    public Expression factorizeRationalsInDifferences() throws EvaluationException {
        return this;
    }

    @Override
    public Expression reduceQuotients() throws EvaluationException {
        return this;
    }

    @Override
    public Expression simplifyPowers() throws EvaluationException {
        return this;
    }

    @Override
    public Expression multiplyPowers() throws EvaluationException {
        return this;
    }

    @Override
    public Expression simplifyFunctionalRelations() throws EvaluationException {
        return this;
    }

    @Override
    public Expression simplifyPolynomials() throws EvaluationException {
        return this;
    }

    @Override
    public Expression simplifyCollectLogarithms() throws EvaluationException {
        return this;
    }

    @Override
    public Expression simplifyExpandLogarithms() throws EvaluationException {
        return this;
    }

    @Override
    public Expression simplifyReplaceExponentialFunctionsByDefinitions() throws EvaluationException {
        return this;
    }

    @Override
    public Expression simplifyReplaceTrigonometricalFunctionsByDefinitions() throws EvaluationException {
        return this;
    }

    @Override
    public Expression simplifyAlgebraicExpressions() throws EvaluationException {
        return this;
    }

}

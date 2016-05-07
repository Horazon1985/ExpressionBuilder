package abstractexpressions.expression.classes;

import enums.TypeExpansion;
import exceptions.EvaluationException;
import abstractexpressions.expression.utilities.SimplifyBinaryOperationMethods;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import lang.translator.Translator;

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
            throw new EvaluationException(Translator.translateOutputMessage("EB_Constant_CONSTANT_CANNOT_BE_EVALUATED"));
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

    @Override
    public Expression copy() {
        return new Constant(this.value, this.precise);
    }

    @Override
    public double evaluate() throws EvaluationException {
        if (Double.isNaN(this.approxValue) || Double.isInfinite(this.approxValue)) {
            throw new EvaluationException(Translator.translateOutputMessage("EB_Constant_CONSTANT_CANNOT_BE_EVALUATED"));
        }
        return this.approxValue;
    }

    @Override
    public void addContainedVars(HashSet<String> vars) {
    }

    @Override
    public void addContainedIndeterminates(HashSet<String> vars) {
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
    public boolean containsOperator(TypeOperator type) {
        return false;
    }

    @Override
    public boolean containsAlgebraicOperation(){
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
            return SimplifyBinaryOperationMethods.constantToQuotient(this.value, BigDecimal.ONE);
        }
        if (Double.isNaN(this.approxValue) || Double.isInfinite(this.approxValue)) {
            throw new EvaluationException(Translator.translateOutputMessage("EB_Constant_CONSTANT_CANNOT_BE_EVALUATED"));
        }
        return this;
    }

    @Override
    public Expression simplifyByInsertingDefinedVars() {
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
    public Expression simplifyReduceDifferencesAndQuotients() {
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
    public Expression simplifyFactorize() {
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
    public Expression simplifyFactorizeAllButRationals() {
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

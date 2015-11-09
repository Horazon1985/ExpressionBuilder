package expressionsimplifymethods;

import computation.ArithmeticMethods;
import exceptions.EvaluationException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import expressionbuilder.Operator;
import expressionbuilder.TypeSimplify;
import expressionbuilder.Variable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import solveequationmethods.PolynomialRootsMethods;
import translator.Translator;

public abstract class SimplifyPolynomialMethods {

    /**
     * Gibt zurück, ob expr ein Polynom in derivative Variablen var ist.
     * Voraussetzung: expr ist vereinfacht, d.h. Operatoren etc. kommen NICHT
     * vor (außer evtl. Gamma(x), was kein Polynom ist).
     */
    public static boolean isPolynomial(Expression expr, String var) {
        if (!expr.contains(var)) {
            return true;
        }
        if (expr instanceof Variable) {
            return true;
        }
        if (expr.isSum() || expr.isDifference() || expr.isProduct()) {
            return isPolynomial(((BinaryOperation) expr).getLeft(), var) && isPolynomial(((BinaryOperation) expr).getRight(), var);
        }
        if (expr.isQuotient() && !((BinaryOperation) expr).getRight().contains(var)) {
            return isPolynomial(((BinaryOperation) expr).getLeft(), var);
        }
        if (expr.isPower() && ((BinaryOperation) expr).getRight().isIntegerConstant() && ((BinaryOperation) expr).getRight().isNonNegative()) {
            return isPolynomial(((BinaryOperation) expr).getLeft(), var);
        }
        return false;
    }

    /**
     * Liefert (eine OBERE SCHRANKE für) den Grad des Polynoms, welches von f
     * repräsentiert wird. Falls f kein Polynom ist in var ist, so wird -1 (als
     * BigInteger) zurückgegeben.
     */
    public static BigInteger degreeOfPolynomial(Expression f, String var) {
        if (!f.contains(var)) {
            return BigInteger.ZERO;
        }
        if (f instanceof Variable) {
            if (((Variable) f).getName().equals(var)) {
                return BigInteger.ONE;
            }
            return BigInteger.ZERO;
        }
        if (f instanceof BinaryOperation) {
            if (f.isSum() || f.isDifference()) {
                return degreeOfPolynomial(((BinaryOperation) f).getLeft(), var).max(degreeOfPolynomial(((BinaryOperation) f).getRight(), var));
            }
            if (f.isProduct()) {
                return degreeOfPolynomial(((BinaryOperation) f).getLeft(), var).add(degreeOfPolynomial(((BinaryOperation) f).getRight(), var));
            }
            if (f.isQuotient() && !((BinaryOperation) f).getRight().contains(var)) {
                return degreeOfPolynomial(((BinaryOperation) f).getLeft(), var);
            }
            if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant() && ((BinaryOperation) f).getRight().isNonNegative()) {
                BigInteger exp = ((Constant) ((BinaryOperation) f).getRight()).getValue().toBigInteger();
                return degreeOfPolynomial(((BinaryOperation) f).getLeft(), var).multiply(exp);
            }
        }
        if (f instanceof Operator) {
            if (f.isConstant()) {
                return BigInteger.ZERO;
            }
        }
        // Dann ist f kein Polynom
        return BigInteger.valueOf(-1);
    }

    /**
     * Liefert (eine UNTERE SCHRANKE für) die Ordnung des Polynoms, welches von
     * f repräsentiert wird. Falls f kein Polynom ist in var ist, so wird -1
     * (als BigInteger) zurückgegeben.
     */
    public static BigInteger orderOfPolynomial(Expression f, String var) {
        if (!f.contains(var)) {
            return BigInteger.ZERO;
        }
        if (f instanceof Variable) {
            if (((Variable) f).getName().equals(var)) {
                return BigInteger.ONE;
            }
            return BigInteger.ZERO;
        }
        if (f instanceof BinaryOperation) {
            if (f.isSum() || f.isDifference()) {
                return orderOfPolynomial(((BinaryOperation) f).getLeft(), var).min(orderOfPolynomial(((BinaryOperation) f).getRight(), var));
            }
            if (f.isProduct()) {
                if (!((BinaryOperation) f).getLeft().contains(var) && !((BinaryOperation) f).getRight().contains(var)) {
                    return BigInteger.ZERO;
                }
                if (((BinaryOperation) f).getLeft().contains(var) && !((BinaryOperation) f).getRight().contains(var)) {
                    return orderOfPolynomial(((BinaryOperation) f).getLeft(), var);
                }
                if (!((BinaryOperation) f).getLeft().contains(var) && ((BinaryOperation) f).getRight().contains(var)) {
                    return orderOfPolynomial(((BinaryOperation) f).getRight(), var);
                }
                return orderOfPolynomial(((BinaryOperation) f).getLeft(), var).add(orderOfPolynomial(((BinaryOperation) f).getRight(), var));
            }
            if (f.isQuotient() && !((BinaryOperation) f).getRight().contains(var)) {
                return orderOfPolynomial(((BinaryOperation) f).getLeft(), var);
            }
            if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant() && ((BinaryOperation) f).getRight().isNonNegative()) {
                BigInteger exp = ((Constant) ((BinaryOperation) f).getRight()).getValue().toBigInteger();
                return orderOfPolynomial(((BinaryOperation) f).getLeft(), var).multiply(exp);
            }
            // Dann ist f kein Polynom
            return BigInteger.valueOf(-1);
        }
        if (f instanceof Operator) {
            if (f.isConstant()) {
                return BigInteger.ZERO;
            }
        }
        // Dann ist f kein Polynom
        return BigInteger.valueOf(-1);
    }

    /**
     * Ermittelt die Koeffizienten, falls f ein Polynom in derivative Variablen
     * var ist. Ist f kein Polynom, so wird eine leere ExpressionCollection
     * zurückgegeben.
     */
    public static ExpressionCollection getPolynomialCoefficients(Expression f, String var) throws EvaluationException {
        ExpressionCollection coefficients = new ExpressionCollection();
        if (!SimplifyPolynomialMethods.isPolynomial(f, var)) {
            return coefficients;
        }
        BigInteger deg = SimplifyPolynomialMethods.degreeOfPolynomial(f, var);
        BigInteger ord = SimplifyPolynomialMethods.orderOfPolynomial(f, var);
        if (deg.compareTo(BigInteger.ZERO) < 0) {
            return coefficients;
        }
        if (deg.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) >= 0) {
            throw new EvaluationException(Translator.translateExceptionMessage("SEM_PolynomialRootMethods_TOO_HIGH_DEGREE"));
        }
        Expression derivative = f;
        BigDecimal factorial = BigDecimal.ONE;
        for (int i = 0; i < ord.intValue(); i++) {
            if (i > 0) {
                factorial = factorial.multiply(BigDecimal.valueOf(i));
            }
            derivative = derivative.diff(var).simplify();
            coefficients.put(i, Expression.ZERO);
        }
        Expression coefficient;
        for (int i = ord.intValue(); i <= deg.intValue(); i++) {
            if (i > 0) {
                factorial = factorial.multiply(BigDecimal.valueOf(i));
            }
            coefficient = derivative.copy();
            coefficient = coefficient.replaceVariable(var, Expression.ZERO).div(factorial).simplify();
            coefficients.put(i, coefficient);
            derivative = derivative.diff(var).simplify();
        }
        while (coefficients.getBound() > 0 && coefficients.get(coefficients.getBound() - 1).equals(Expression.ZERO)) {
            coefficients.remove(coefficients.getBound() - 1);
        }
        return coefficients;
    }

    public static Expression getPolynomialFromCoefficients(ExpressionCollection coefficients, String var) {
        Expression result = Expression.ZERO;
        for (int i = 0; i < coefficients.getBound(); i++) {
            result = result.add(coefficients.get(i).mult(Variable.create(var).pow(i)));
        }
        return result;
    }

    /**
     * Zerlegt ein Polynom in Linearteile, soweit es geht.<br>
     * BEISPIEL wird 5*x+6*x^3+x^5-(2+6*x^2+4*x^4) zu (x-1)^2*(x-2)*(x^2+1)
     * faktorisiert.
     */
    public static Expression decomposePolynomialInIrreducibleFactors(Expression f, String var) throws EvaluationException {
        if (!SimplifyPolynomialMethods.isPolynomial(f, var)) {
            return f;
        }
        if (f.isProduct()) {
            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, decomposePolynomialInIrreducibleFactors(factors.get(i), var));
            }
            HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
            simplifyTypes.add(TypeSimplify.order_difference_and_division);
            simplifyTypes.add(TypeSimplify.order_sums_and_products);
            simplifyTypes.add(TypeSimplify.simplify_trivial);
            simplifyTypes.add(TypeSimplify.simplify_powers);
            simplifyTypes.add(TypeSimplify.collect_products);
            simplifyTypes.add(TypeSimplify.factorize_all_but_rationals_in_sums);
            simplifyTypes.add(TypeSimplify.factorize_all_but_rationals_in_differences);
            simplifyTypes.add(TypeSimplify.reduce_quotients);
            simplifyTypes.add(TypeSimplify.reduce_leadings_coefficients);
            simplifyTypes.add(TypeSimplify.simplify_functional_relations);
            return SimplifyUtilities.produceProduct(factors).simplify(simplifyTypes);
        }
        if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant() && ((BinaryOperation) f).getRight().isNonNegative()) {
            Expression baseDecomposed = decomposePolynomialInIrreducibleFactors(((BinaryOperation) f).getLeft(), var);
            ExpressionCollection factors = SimplifyUtilities.getFactors(baseDecomposed);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).pow(((BinaryOperation) f).getRight()));
            }
            return SimplifyUtilities.produceProduct(factors);
        }
        ExpressionCollection a = SimplifyPolynomialMethods.getPolynomialCoefficients(f, var);
        if (a.getBound() == 3) {
            Expression diskr = a.get(1).pow(2).sub(Expression.FOUR.mult(a.get(0)).mult(a.get(2))).simplify();
            if (diskr.isAlwaysNonNegative()) {
                Expression zeroOne = Expression.MINUS_ONE.mult(a.get(1)).add(diskr.pow(1, 2)).div(Expression.TWO.mult(a.get(2)));
                Expression zeroTwo = Expression.MINUS_ONE.mult(a.get(1)).sub(diskr.pow(1, 2)).div(Expression.TWO.mult(a.get(2)));
                if (zeroOne.equivalent(zeroTwo)) {
                    return a.get(2).mult(Variable.create(var).sub(zeroOne).simplify().pow(2));
                } else {
                    return a.get(2).mult(Variable.create(var).sub(zeroOne).simplify().mult(Variable.create(var).sub(zeroTwo).simplify()));
                }
            }
        }
        for (int i = 0; i < a.getBound(); i++) {
            if (!a.get(i).isIntegerConstantOrRationalConstant()) {
                return f;
            }
        }
        ExpressionCollection zeros = new ExpressionCollection();
        ExpressionCollection restCoefficients = PolynomialRootsMethods.findAllRationalZerosOfPolynomial(a, zeros);
        if (zeros.isEmpty()) {
            return f;
        }
        Expression result = SimplifyPolynomialMethods.getPolynomialFromCoefficients(restCoefficients, var).simplify();
        int l = zeros.getBound();
        Expression currentZero = zeros.get(0);
        int currentMultiplicity = 1;
        while (!zeros.isEmpty()) {
            for (int i = 0; i < l; i++) {
                if (zeros.get(i) != null) {
                    currentZero = zeros.get(i);
                    currentMultiplicity = 1;
                    zeros.remove(i);
                    break;
                }
            }
            for (int i = 0; i < l; i++) {
                if (zeros.get(i) != null && zeros.get(i).equals(currentZero)) {
                    currentMultiplicity++;
                    zeros.remove(i);
                }
            }
            if (result.equals(Expression.ONE)) {
                if (currentMultiplicity == 1) {
                    result = Variable.create(var).sub(currentZero).simplify();
                } else {
                    result = Variable.create(var).sub(currentZero).simplify().pow(currentMultiplicity);
                }
            } else {
                if (currentMultiplicity == 1) {
                    result = result.mult(Variable.create(var).sub(currentZero).simplify());
                } else {
                    result = result.mult(Variable.create(var).sub(currentZero).simplify().pow(currentMultiplicity));
                }
            }
        }
        return result;
    }

    /**
     * Polynomdivision des Polynoms coefficientsEnumerator[n]*x^n + ... +
     * coeffcicientsEnumerator.get(1)*x + coeffcicientsEnumerator.get(0) durch
     * coefficientsDenominator[m]*x^m + ... + coefficientsDenominator[0].
     * Zurückgegeben wird ein Array aus zwei ExpressionCollections, im 0-ten
     * Arrayeintrag stehen die Koeffizienten des Quotienten, im 1-ten
     * Arrayeintrag die Koeffizienten des Divisionsrests.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection[] polynomialDivision(ExpressionCollection coefficientsEnumerator, ExpressionCollection coefficientsDenominator) throws EvaluationException {
        ExpressionCollection[] quotient = new ExpressionCollection[2];
        quotient[0] = new ExpressionCollection();
        quotient[1] = new ExpressionCollection();
        ExpressionCollection multipleOfDenominator = new ExpressionCollection();
        if (coefficientsEnumerator.getBound() < coefficientsDenominator.getBound()) {
            quotient[0].put(0, Expression.ZERO);
            for (int i = 0; i < coefficientsDenominator.getBound(); i++) {
                quotient[1].put(i, coefficientsDenominator.get(i));
            }
            return quotient;
        }
        int degreeDenominator = coefficientsDenominator.getBound() - 1;
        ExpressionCollection coeffcicientsEnumeratorCopy = ExpressionCollection.copy(coefficientsEnumerator);
        for (int i = coeffcicientsEnumeratorCopy.getBound() - 1; i >= degreeDenominator; i--) {
            quotient[0].put(i - degreeDenominator, coeffcicientsEnumeratorCopy.get(i).div(coefficientsDenominator.get(degreeDenominator)).simplify());
            for (int j = degreeDenominator; j >= 0; j--) {
                multipleOfDenominator.put(j, (coefficientsDenominator.get(j).mult(coeffcicientsEnumeratorCopy.get(i))).div(coefficientsDenominator.get(degreeDenominator)).simplify());
            }
            for (int j = degreeDenominator; j >= 0; j--) {
                coeffcicientsEnumeratorCopy.put(i + j - degreeDenominator, coeffcicientsEnumeratorCopy.get(i + j - degreeDenominator).sub(multipleOfDenominator.get(j)).simplify());
            }
        }
        int indexOfLeadingCoefficientInRest = degreeDenominator - 1;
        while (indexOfLeadingCoefficientInRest >= 0 && coeffcicientsEnumeratorCopy.get(indexOfLeadingCoefficientInRest).equals(Expression.ZERO)) {
            indexOfLeadingCoefficientInRest--;
        }
        for (int i = 0; i <= indexOfLeadingCoefficientInRest; i++) {
            quotient[1].put(i, coeffcicientsEnumeratorCopy.get(i));
        }
        return quotient;
    }

    /**
     * Liefert die kleinste Periode, unter welcher die Koeffizienten
     * coefficients periodisch sind.
     */
    public static int getPeriodOfCoefficients(ExpressionCollection coefficients) {
        int l = coefficients.getBound();
        HashMap<Integer, BigInteger> cycleLengths = ArithmeticMethods.getDivisors(BigInteger.valueOf(l));
        ExpressionCollection periodForCompare;
        ExpressionCollection currentPeriod;
        boolean periodFound;
        for (int i = 0; i < cycleLengths.size(); i++) {
            periodForCompare = ExpressionCollection.copy(coefficients, 0, cycleLengths.get(i).intValue());
            periodFound = true;
            for (int j = 1; j < coefficients.getBound() / cycleLengths.get(i).intValue(); j++) {
                currentPeriod = ExpressionCollection.copy(coefficients, j * cycleLengths.get(i).intValue(), (j + 1) * cycleLengths.get(i).intValue());
                if (!SimplifyUtilities.equivalent(periodForCompare, currentPeriod)) {
                    periodFound = false;
                    break;
                }
            }
            if (periodFound) {
                return cycleLengths.get(i).intValue();
            }
        }
        return coefficients.getBound();
    }

}

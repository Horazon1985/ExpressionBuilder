package expressionsimplifymethods;

import computation.ArithmeticMethods;
import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import exceptions.MathToolException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.MINUS_ONE;
import static expressionbuilder.Expression.ONE;
import static expressionbuilder.Expression.PI;
import static expressionbuilder.Expression.TWO;
import static expressionbuilder.Expression.ZERO;
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

    private static final HashSet<TypeSimplify> simplifyTypesDecomposePolynomial = getSimplifyTypesDecomposePolynomial();

    private static HashSet<TypeSimplify> getSimplifyTypesDecomposePolynomial() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals_in_sums);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals_in_differences);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        return simplifyTypes;
    }

    /**
     * Private Fehlerklasse für den Fall, dass die Partialbruchzerlegung nicht
     * ermittelt werden konnte.
     */
    private static class PolynomialNotDecomposableException extends MathToolException {

        private static final String NO_EXPLICIT_DECOMPOSITION_MESSAGE = "Polynomial admits no explicit decomposition.";

        public PolynomialNotDecomposableException() {
            super(NO_EXPLICIT_DECOMPOSITION_MESSAGE);
        }

        public PolynomialNotDecomposableException(String s) {
            super(s);
        }

    }

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
            return SimplifyUtilities.produceProduct(factors).simplify(simplifyTypesDecomposePolynomial);
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

        try {
            return decomposePeriodicPolynomial(a, var);
        } catch (PolynomialNotDecomposableException e) {
        }

        try {
            return decomposeAntiperiodicPolynomial(a, var);
        } catch (PolynomialNotDecomposableException e) {
        }

        if (isPolynomialCyclic(a)) {
            try {
                Expression b = MINUS_ONE.mult(a.get(0)).div(a.get(a.getBound() - 1)).simplify();
                return decomposeCyclicPolynomial(a.getBound() - 1, b, var);
            } catch (PolynomialNotDecomposableException e) {
            }
        }

        if (isPolynomialRational(a)) {
            try {
                return decomposeRationalPolynomial(a, var);
            } catch (PolynomialNotDecomposableException e) {
            }
        }

        try {
            return decomposePolynomialInMonomial(a, var);
        } catch (PolynomialNotDecomposableException e) {
        }

        if (a.getBound() == 3) {
            try {
                return decomposeQuadraticPolynomial(a, var);
            } catch (PolynomialNotDecomposableException e) {
            }
        }
        if (a.getBound() == 4) {
            try {
                return decomposeCubicPolynomial(a, var);
            } catch (PolynomialNotDecomposableException e) {
            }
        }

        // Dann konnte das Polynom nicht faktorisiert werden.
        return f;

    }

    /**
     * Prüfung, ob ein Polynom zyklisch ist.
     */
    private static boolean isPolynomialCyclic(ExpressionCollection a) {
        for (int i = 1; i < a.getBound() - 1; i++) {
            if (!a.get(i).equals(ZERO)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Prüfung, ob ein Polynom rational ist.
     */
    private static boolean isPolynomialRational(ExpressionCollection a) {
        for (int i = 0; i < a.getBound(); i++) {
            if (!a.get(i).isIntegerConstantOrRationalConstant()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Faktorisiert quadratische Polynome.
     */
    private static Expression decomposeQuadraticPolynomial(ExpressionCollection a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        Expression discriminant = a.get(1).pow(2).sub(Expression.FOUR.mult(a.get(0)).mult(a.get(2))).simplify();
        if (discriminant.isAlwaysNonNegative()) {
            Expression zeroOne = Expression.MINUS_ONE.mult(a.get(1)).add(discriminant.pow(1, 2)).div(Expression.TWO.mult(a.get(2))).simplify();
            Expression zeroTwo = Expression.MINUS_ONE.mult(a.get(1)).sub(discriminant.pow(1, 2)).div(Expression.TWO.mult(a.get(2))).simplify();
            if (zeroOne.equivalent(zeroTwo)) {
                return a.get(2).mult(Variable.create(var).sub(zeroOne).simplify().pow(2));
            } else {
                return a.get(2).mult(Variable.create(var).sub(zeroOne).simplify().mult(Variable.create(var).sub(zeroTwo).simplify()));
            }
        }

        throw new PolynomialNotDecomposableException();

    }

    /**
     * Faktorisiert kubische Polynome.
     */
    private static Expression decomposeCubicPolynomial(ExpressionCollection a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        Expression A = a.get(2).div(a.get(3)).simplify();
        Expression B = a.get(1).div(a.get(3)).simplify();
        Expression C = a.get(0).div(a.get(3)).simplify();

        // Gelöst wird nun die Gleichung x^3 + Ax^2 + Bx + C = 0
        /*
         Substitution x = z - A/3 (später muss zurücksubstituiert werden): p =
         B - A^2/3, q = 2A^3/27 - AB/3 + C. Gelöst wird nun die Gleichung z^3
         + pz + q = 0
         */
        Expression p = B.sub(A.pow(2).div(3)).simplify();
        Expression q = Expression.TWO.mult(A.pow(3).div(27)).sub(A.mult(B).div(3)).add(C).simplify();

        // Diskriminante discriminant = (p/3)^3 + (q/2)^2 = p^3/27 + q^2/4.
        Expression discriminant = p.pow(3).div(27).add(q.pow(2).div(4)).simplify();

        if (discriminant.equals(ZERO) || discriminant.isAlwaysPositive() || discriminant.isAlwaysNegative()) {
            ExpressionCollection zeros = PolynomialRootsMethods.solveCubicEquation(a);
            if (discriminant.isAlwaysPositive()) {
                // z_0 = einzige Nullstelle. Restfaktor = x^2 + (z_0+A)*x + (z_0^2+A*z_0+B).
                Expression irreducibleQuadraticFactor = Variable.create(var).pow(2).add(
                        zeros.get(0).add(A).mult(Variable.create(var))).add(
                                zeros.get(0).pow(2).add(A.mult(zeros.get(0))).add(B)).simplify();
                return a.get(3).mult(Variable.create(var).sub(zeros.get(0)).simplify()).mult(irreducibleQuadraticFactor);
            }
            if (discriminant.equals(ZERO)) {
                return a.get(3).mult(Variable.create(var).sub(zeros.get(0)).pow(2).simplify()).mult(Variable.create(var).sub(zeros.get(1)).simplify());
            }
            if (discriminant.isAlwaysNegative()) {
                return a.get(3).mult(Variable.create(var).sub(zeros.get(0)).simplify()).mult(
                        Variable.create(var).sub(zeros.get(1)).simplify()).mult(
                                Variable.create(var).sub(zeros.get(2)).simplify());
            }
        }

        throw new PolynomialNotDecomposableException();

    }

    /**
     * Faktorisiert Polynome vom Typ x^n - a.
     */
    private static Expression decomposeCyclicPolynomial(int n, Expression a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        if (n <= 2 || n > ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL_EQUATION
                || !a.isAlwaysPositive() && !a.isAlwaysNegative()) {
            throw new PolynomialNotDecomposableException();
        }

        Expression decomposedPolynomial = Expression.ONE;
        Expression quadraticFactor;

        // Im Folgenden dient simplify() dazu, die Werte a^(1/n)*cos(2*k*pi/n) zu vereinfachen, falls möglich.
        if (a.isAlwaysPositive()) {

            if (n % 2 == 0) {
                decomposedPolynomial = Variable.create(var).sub(a.pow(1, n)).simplify().mult(Variable.create(var).add(a.pow(1, n)).simplify());
                for (int i = 1; i < n / 2; i++) {
                    quadraticFactor = Variable.create(var).pow(2).sub(
                            TWO.mult(a.pow(1, n)).mult(TWO.mult(i).mult(PI).div(n).cos()).mult(Variable.create(var))).add(
                                    a.pow(2, n)).simplify();
                    decomposedPolynomial = decomposedPolynomial.mult(quadraticFactor);
                }
            } else {
                decomposedPolynomial = Variable.create(var).sub(a.pow(1, n)).simplify();
                for (int i = 0; i < n / 2; i++) {
                    quadraticFactor = Variable.create(var).pow(2).sub(
                            TWO.mult(a.pow(1, n)).mult(TWO.mult(i + 1).mult(PI).div(n).cos()).mult(Variable.create(var))).add(
                                    a.pow(2, n)).simplify();
                    decomposedPolynomial = decomposedPolynomial.mult(quadraticFactor);
                }
            }

        } else {

            if (n % 2 == 0) {
                for (int i = 0; i < n / 2; i++) {
                    quadraticFactor = Variable.create(var).pow(2).sub(
                            TWO.mult(a.pow(1, n)).mult(TWO.mult(2 * i + 1).mult(PI).div(n).cos()).mult(Variable.create(var))).add(
                                    a.pow(2, n)).simplify();
                    decomposedPolynomial = decomposedPolynomial.mult(quadraticFactor);
                }
            } else {
                decomposedPolynomial = Variable.create(var).sub(a.pow(1, n)).simplify();
                for (int i = 0; i < n / 2; i++) {
                    quadraticFactor = Variable.create(var).pow(2).sub(
                            TWO.mult(a.pow(1, n)).mult(TWO.mult(2 * i + 1).mult(PI).div(n).cos()).mult(Variable.create(var))).add(
                                    a.pow(2, n)).simplify();
                    decomposedPolynomial = decomposedPolynomial.mult(quadraticFactor);
                }
            }

        }

        return decomposedPolynomial;

    }

    /**
     * Faktorisiert Polynome mit periodischen Koeffizienten.
     */
    private static Expression decomposePeriodicPolynomial(ExpressionCollection a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        int m = getPeriodOfCoefficients(a);

        if (m > ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL_EQUATION || m == 1 || m == a.getBound()) {
            throw new PolynomialNotDecomposableException();
        }

        Expression polynomialOfPrimitivePeriod = SimplifyPolynomialMethods.getPolynomialFromCoefficients(ExpressionCollection.copy(a, 0, m), var);
        Expression secondFactor = ONE;

        for (int i = 1; i < a.getBound() / m; i++) {
            secondFactor = secondFactor.add(Variable.create(var).pow(i * m));
        }

        polynomialOfPrimitivePeriod = decomposePolynomialInIrreducibleFactors(polynomialOfPrimitivePeriod, var);
        secondFactor = decomposePolynomialInIrreducibleFactors(secondFactor, var);

        return polynomialOfPrimitivePeriod.mult(secondFactor);

    }

    /**
     * Faktorisiert Polynome mit antiperiodischen Koeffizienten.
     */
    private static Expression decomposeAntiperiodicPolynomial(ExpressionCollection a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        int m = getAntiperiodOfCoefficients(a);

        if (m > ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL_EQUATION || m == 1 || m == a.getBound()) {
            throw new PolynomialNotDecomposableException();
        }

        Expression polynomialOfPrimitivePeriod = SimplifyPolynomialMethods.getPolynomialFromCoefficients(ExpressionCollection.copy(a, 0, m), var);
        Expression secondFactor = ONE;

        for (int i = 1; i < a.getBound() / m; i++) {
            if (i % 2 == 1) {
                secondFactor = secondFactor.sub(Variable.create(var).pow(i * m));
            } else {
                secondFactor = secondFactor.add(Variable.create(var).pow(i * m));
            }
        }

        polynomialOfPrimitivePeriod = decomposePolynomialInIrreducibleFactors(polynomialOfPrimitivePeriod, var);
        secondFactor = decomposePolynomialInIrreducibleFactors(secondFactor, var);

        return polynomialOfPrimitivePeriod.mult(secondFactor);

    }

    /**
     * Faktorisiert Polynome, deren Monome Exponenten mit nichttrivialem ggT
     * besitzen.
     */
    private static Expression decomposePolynomialInMonomial(ExpressionCollection a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        int m = getGGTOfAllExponents(a);

        if (m > ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL_EQUATION || m <= 1) {
            throw new PolynomialNotDecomposableException();
        }

        ExpressionCollection coefficients = new ExpressionCollection();
        for (int i = 0; i < a.getBound(); i++) {
            if (i % m == 0) {
                coefficients.add(a.get(i));
            }
        }

        Expression polynomialToDecompose = SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficients, var);
        polynomialToDecompose = decomposePolynomialInIrreducibleFactors(polynomialToDecompose, var);

        if (polynomialToDecompose.isProduct()) {

            // In diesem Fall konnte das Polynom faktorisiert werden.
            Expression decomposedPolynomial = polynomialToDecompose.replaceVariable(var, Variable.create(var).pow(m)).simplify(simplifyTypesDecomposePolynomial);
            ExpressionCollection factors = SimplifyUtilities.getFactors(decomposedPolynomial);
            if (factors.getBound() > 1) {
                for (int i = 0; i < factors.getBound(); i++) {
                    factors.put(i, decomposePolynomialInIrreducibleFactors(factors.get(i), var));
                }
                return SimplifyUtilities.produceProduct(factors);
            }

        }

        throw new PolynomialNotDecomposableException();

    }

    /**
     * Faktorisiert rationale Polynome.
     */
    private static Expression decomposeRationalPolynomial(ExpressionCollection a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        ExpressionCollection zeros = new ExpressionCollection();
        ExpressionCollection restCoefficients = PolynomialRootsMethods.findAllRationalZerosOfPolynomial(a, zeros);
        if (zeros.isEmpty()) {
            throw new PolynomialNotDecomposableException();
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
            result = result.mult((Variable.create(var).sub(currentZero).simplify()).pow(currentMultiplicity));
        }

        return a.get(a.getBound() - 1).mult(result);

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

        // Falls deg(Zähler) < deg(Nenner) -> fertig.
        if (coefficientsEnumerator.getBound() < coefficientsDenominator.getBound()) {
            quotient[1] = new ExpressionCollection(coefficientsEnumerator);
            return quotient;
        }

        /* 
         Division durch ein Nullpolynom. Sollte bei vernünftigen Anwendungen nicht passieren 
         (und muss im Vorfeld geprüft werden).
         */
        if (coefficientsDenominator.isEmpty()) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_DIVISION_BY_ZERO"));
        }
        
        int degreeDenominator = coefficientsDenominator.getBound() - 1;
        ExpressionCollection multipleOfDenominator = new ExpressionCollection();
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
     * Gibt den ggT zweier Polynome f und g zurück, falls deren Koeffizienten
     * rational sind. Ist f oder g kein Polynom in var, oder hat f oder g einen
     * zu hohen Grad, so wird 1 zurückgegeben.
     */
    public static Expression getGGTOfPolynomials(Expression f, Expression g, String var) {

        try {

            BigInteger degF = degreeOfPolynomial(f, var);
            BigInteger degG = degreeOfPolynomial(g, var);

            if (degF.compareTo(BigInteger.ZERO) < 0 || degG.compareTo(BigInteger.ZERO) < 0
                    || degF.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL_EQUATION)) > 0
                    || degG.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL_EQUATION)) > 0) {
                return ONE;
            }

            ExpressionCollection coefficientsF = SimplifyPolynomialMethods.getPolynomialCoefficients(f, var);
            ExpressionCollection coefficientsG = SimplifyPolynomialMethods.getPolynomialCoefficients(g, var);

            if (!isPolynomialRational(coefficientsF) || !isPolynomialRational(coefficientsG)) {
                return ONE;
            }

            ExpressionCollection coefficientsGGT = getGGTOfPolynomials(coefficientsF, coefficientsG);
            return getPolynomialFromCoefficients(coefficientsGGT, var);

        } catch (EvaluationException e) {
            return ONE;
        }

    }

    private static ExpressionCollection getGGTOfPolynomials(ExpressionCollection a, ExpressionCollection b) throws EvaluationException {

        if (a.getBound() < b.getBound()) {
            return getGGTOfPolynomials(b, a);
        }

        ExpressionCollection r = polynomialDivision(a, b)[1];

        while (!r.isEmpty()) {
            a = ExpressionCollection.copy(b);
            b = ExpressionCollection.copy(r);
            r = polynomialDivision(a, b)[1];
        }

        // Anschließend normieren!
        if (b.getBound() > 0 && !ZERO.equals(b.get(b.getBound() - 1))) {
            b.divByExpression(b.get(b.getBound() - 1));
            b = b.simplify();
        }

        return b;

    }

    /**
     * Liefert die kleinste Periode, unter welcher die Koeffizienten
     * coefficients periodisch sind.<br>
     * BEISPIEL: Sind [1,3,-17,2,3,9,1,3,-17,2,3,9] die Koeffizienten von einem
     * Polynom f (in aufsteigender Reihenfolge), so wird 6 zurückgegeben.
     */
    public static int getPeriodOfCoefficients(ExpressionCollection coefficients) {

        if (coefficients.isEmpty()) {
            return 0;
        }

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

    /**
     * Liefert die kleinste Periode, unter welcher die Koeffizienten
     * coefficients antiperiodisch sind.<br>
     * BEISPIEL: Sind [1,3,-17,-1,-3,17,1,3,-17,-1,-3,17] die Koeffizienten von
     * einem Polynom f (in aufsteigender Reihenfolge), so wird 3 zurückgegeben.
     */
    public static int getAntiperiodOfCoefficients(ExpressionCollection coefficients) {

        if (coefficients.isEmpty()) {
            return 0;
        }

        int l = coefficients.getBound();
        HashMap<Integer, BigInteger> cycleLengths = ArithmeticMethods.getDivisors(BigInteger.valueOf(l));
        ExpressionCollection periodForCompare;
        ExpressionCollection currentPeriod;
        Expression sum;

        boolean periodFound;
        for (int i = 0; i < cycleLengths.size(); i++) {
            periodForCompare = ExpressionCollection.copy(coefficients, 0, cycleLengths.get(i).intValue());
            periodFound = true;
            for (int j = 1; j < coefficients.getBound() / cycleLengths.get(i).intValue(); j++) {
                currentPeriod = ExpressionCollection.copy(coefficients, j * cycleLengths.get(i).intValue(), (j + 1) * cycleLengths.get(i).intValue());
                if (j % 2 == 1) {
                    for (int k = 0; k < periodForCompare.getBound(); k++) {
                        try {
                            sum = periodForCompare.get(k).add(currentPeriod.get(k)).simplify();
                            if (!sum.equals(ZERO)) {
                                periodFound = false;
                                break;
                            }
                        } catch (EvaluationException e) {
                            periodFound = false;
                            break;
                        }
                    }
                } else {
                    if (!SimplifyUtilities.equivalent(periodForCompare, currentPeriod)) {
                        periodFound = false;
                        break;
                    }
                }
                if (!periodFound) {
                    break;
                }
            }
            if (periodFound) {
                return cycleLengths.get(i).intValue();
            }
        }
        return coefficients.getBound();

    }

    /**
     * Liefert den ggT aller Exponenten nichttrivialer Monome des Polynoms mit
     * Koeffizienten coefficients.<br>
     * BEISPIEL: Beim Polynom x^15 + 2*x^10 + 7*x^5 + 1 wird der Wert 5
     * zurückgegeben. -> Später Substitution x^5 = t möglich.
     */
    public static int getGGTOfAllExponents(ExpressionCollection coefficients) {
        int result = 0;
        for (int i = 0; i < coefficients.getBound(); i++) {
            if (i != 0 && !coefficients.get(i).equals(Expression.ZERO)) {
                if (result == 0) {
                    result = i;
                } else {
                    result = ArithmeticMethods.gcd(result, i);
                }
            }
        }
        return result;
    }

}

package solveequationmethods;

import computation.ArithmeticMethods;
import computationbounds.ComputationBounds;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import exceptions.EvaluationException;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.TypeBinary;
import expressionbuilder.TypeFunction;
import expressionbuilder.TypeSimplify;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyPolynomialMethods;
import expressionsimplifymethods.SimplifyUtilities;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import translator.Translator;

public abstract class PolynomialRootsMethods {

    /**
     * Gibt zurück, ob expr ein Polynom in derivative Variablen x ist, falls die
     * Variable var durch ein geeignetes var = x^period, period ganzzahlig,
     * substituiert wird. Voraussetzung: expr ist vereinfacht, d.h. Operatoren
     * etc. kommen NICHT vor (außer evtl. Gamma(x), was kein Polynom ist).
     */
    public static boolean isPolynomialAfterSubstitutionByRoots(Expression expr, String var) {

        if (!expr.contains(var)) {
            return true;
        }
        if (expr instanceof Variable) {
            return true;
        }
        if (expr.isSum() || expr.isDifference() || expr.isProduct()) {
            return isPolynomialAfterSubstitutionByRoots(((BinaryOperation) expr).getLeft(), var) && isPolynomialAfterSubstitutionByRoots(((BinaryOperation) expr).getRight(), var);
        }
        if (expr.isQuotient() && !((BinaryOperation) expr).getRight().contains(var)) {
            return isPolynomialAfterSubstitutionByRoots(((BinaryOperation) expr).getLeft(), var);
        }
        if (expr.isPower()) {
            if (((BinaryOperation) expr).getRight().isIntegerConstant()) {
                return isPolynomialAfterSubstitutionByRoots(((BinaryOperation) expr).getLeft(), var);
            } else if (((BinaryOperation) expr).getRight().isRationalConstant()) {
                return ((BinaryOperation) expr).getLeft() instanceof Variable;
            }
        }

        return false;

    }

    /**
     * Zerlegt ein Polynom in Linearteile, soweit es geht. Beispielsweise wird
     * 5*x+6*x^3+x^5-(2+6*x^2+4*x^4) zu (x-1)^2*(x-2)*(x^2+1) faktorisiert.
     */
    public static Expression decomposePolynomialInIrreducibleFactors(Expression f, String var)
            throws EvaluationException {

        // Sicherheitsabfrage: Falls f kein Polynom ist, dann f wieder ausgeben.
        if (!SimplifyPolynomialMethods.isPolynomial(f, var)) {
            return f;
        }

        if (f.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            // Jeden Faktor einzeln zerlegen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, decomposePolynomialInIrreducibleFactors(factors.get(i), var));
            }
            // Zum Schluss noch: Gleiche Faktoren zusammenfassen.
            HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
            simplifyTypes.add(TypeSimplify.sort_difference_and_division);
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

        if (f.isPower()
                && ((BinaryOperation) f).getRight().isIntegerConstant()
                && ((BinaryOperation) f).getRight().isNonNegative()) {

            Expression baseDecomposed = decomposePolynomialInIrreducibleFactors(((BinaryOperation) f).getLeft(), var);
            ExpressionCollection factors = SimplifyUtilities.getFactors(baseDecomposed);
            // Jeden Faktor der Basis einzeln potenzieren.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).pow(((BinaryOperation) f).getRight()));
            }
            return SimplifyUtilities.produceProduct(factors);

        }

        /*
         Ab hier werden einfach die Polynomkoeffizienten von f bestimmt und es
         wird versucht, f so weit es geht zu zerlegen.
         */
        ExpressionCollection a = PolynomialRootsMethods.getPolynomialCoefficients(f, var);

        /*
         Zunächst Sonderfall: f ist ein reduzibles quadratisches Polynom.
         Polynome vom Grad 3 und 4 werden ausgelassen (zu umständlich).
         */
        if (a.getBound() == 3) {
            Expression diskr = a.get(1).pow(2).sub(Expression.FOUR.mult(a.get(0)).mult(a.get(2))).simplify();
            if (diskr.isAlwaysNonNegative()) {

                Expression zero_1 = Expression.MINUS_ONE.mult(a.get(1)).add(diskr.pow(1, 2)).div(Expression.TWO.mult(a.get(2)));
                Expression zero_2 = Expression.MINUS_ONE.mult(a.get(1)).sub(diskr.pow(1, 2)).div(Expression.TWO.mult(a.get(2)));
                if (zero_1.equivalent(zero_2)) {
                    // Falls beide Nullstellen gleich sind, etwa == a, dann (x - a)^2 zurückgeben.
                    return Variable.create(var).sub(zero_1).simplify().pow(2);
                } else {
                    /*
                     Falls beide Nullstellen verschieden sind, etwa == a und
                     == b, dann (x - a)*(x - b) zurückgeben.
                     */
                    return Variable.create(var).sub(zero_1).simplify().mult(Variable.create(var).sub(zero_2).simplify());
                }

            }
        }

        /*
         Prüfen: ist eines der Koeffizienten nicht rational, dann wird nicht
         weiter zerlegt (weil zu kompliziert).
         */
        for (int i = 0; i < a.getBound(); i++) {
            if (!a.get(i).isIntegerConstantOrRationalConstant()) {
                return f;
            }
        }

        // Ab hier sind die Koeffizienten allesamt rationale Zahlen.
        ExpressionCollection zeros = new ExpressionCollection();
        ExpressionCollection restCoefficients = PolynomialRootsMethods.findAllRationalZerosOfPolynomial(a, zeros);

        if (zeros.isEmpty()) {
            return f;
        }

        // Ab hier wurden rationale Nullstellen gefunden.
        // Zunächst: Gleiche Nullstellen zu mehrfachen Nullstellen zusammenfassen.
        Expression result = PolynomialRootsMethods.getPolynomialFromCoefficients(restCoefficients, var).simplify();
        int l = zeros.getBound();
        Expression current_zero = zeros.get(0);
        int current_multiplicity = 1;

        while (!zeros.isEmpty()) {

            for (int i = 0; i < l; i++) {
                if (zeros.get(i) != null) {
                    current_zero = zeros.get(i);
                    current_multiplicity = 1;
                    zeros.remove(i);
                    break;
                }
            }

            for (int i = 0; i < l; i++) {
                if (zeros.get(i) != null && zeros.get(i).equals(current_zero)) {
                    current_multiplicity++;
                    zeros.remove(i);
                }
            }

            // Entsprechende Potenz des Linearfaktors an result dranmultiplizieren.
            if (result.equals(Expression.ONE)) {
                // simplify() dient dazu, dass z. B. x - (-2) zu x + 2 vereinfacht wird.
                if (current_multiplicity == 1) {
                    result = Variable.create(var).sub(current_zero).simplify();
                } else {
                    result = Variable.create(var).sub(current_zero).simplify().pow(current_multiplicity);
                }
            } else {
                if (current_multiplicity == 1) {
                    result = result.mult(Variable.create(var).sub(current_zero).simplify());
                } else {
                    result = result.mult(Variable.create(var).sub(current_zero).simplify().pow(current_multiplicity));
                }
            }

        }

        return result;

    }

    /**
     * Falls isPolynomialAfterSubstitutionByRoots() true zurückgibt, gibt es den
     * kleinsten Exponenten m für eine geeignete Substitution var = y^m aus, so
     * dass die resultierende Gleichung ein Polynom in y wird. VORAUSSETZUNG:
     * expr muss ein Polynom sein.
     */
    public static BigInteger findExponentForPolynomialSubstitutionByRoots(Expression expr, String var) {

        if (expr instanceof Constant) {
            return BigInteger.ONE;
        }
        if (expr instanceof Variable) {
            return BigInteger.ONE;
        }
        if (expr instanceof BinaryOperation) {

            if (((BinaryOperation) expr).getType().equals(TypeBinary.PLUS)
                    || ((BinaryOperation) expr).getType().equals(TypeBinary.MINUS)
                    || ((BinaryOperation) expr).getType().equals(TypeBinary.TIMES)) {
                BigInteger m = findExponentForPolynomialSubstitutionByRoots(((BinaryOperation) expr).getLeft(), var);
                BigInteger n = findExponentForPolynomialSubstitutionByRoots(((BinaryOperation) expr).getRight(), var);
                return m.multiply(n).divide(m.gcd(n));
            }
            if (((BinaryOperation) expr).getType().equals(TypeBinary.DIV) && ((BinaryOperation) expr).getRight().isConstant()) {
                return findExponentForPolynomialSubstitutionByRoots(((BinaryOperation) expr).getLeft(), var);
            }
            if (((BinaryOperation) expr).getType().equals(TypeBinary.POW)) {

                if (((BinaryOperation) expr).getRight().isIntegerConstant()) {
                    return findExponentForPolynomialSubstitutionByRoots(((BinaryOperation) expr).getLeft(), var);
                } else if (((BinaryOperation) expr).getRight().isRationalConstant()
                        && ((BinaryOperation) expr).getLeft() instanceof Variable
                        && ((Variable) ((BinaryOperation) expr).getLeft()).getName().equals(var)) {
                    return ((Constant) ((BinaryOperation) ((BinaryOperation) expr).getRight()).getRight()).getValue().toBigInteger();
                }

            }

        }

        return BigInteger.ONE;

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

        // Ab hier ist f ein Polynom.
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

        // Falls Leitkoeffizient = 0 -> Grad um 1 senken, bis Leitkoeffizient != 0.
        while (coefficients.getBound() > 0 && coefficients.get(coefficients.getBound() - 1).equals(Expression.ZERO)) {
            coefficients.remove(coefficients.getBound() - 1);
        }

        return coefficients;

    }

    public static Expression getPolynomialFromCoefficients(ExpressionCollection coefficients, String var) {

        Expression result = Expression.ZERO;
        for (int i = 0; i < coefficients.getBound(); i++) {
            if (i == 0) {
                result = coefficients.get(0);
            } else {
                result = result.add(coefficients.get(i).mult(Variable.create(var).pow(i)));
            }
        }
        return result;

    }

    /**
     * Hilfsfunktion: liefert f/x^exponent, wobei x = var.
     */
    public static Expression divideExpressionByPowerOfVar(Expression f, String var, BigInteger exponent) throws EvaluationException {
        if (f instanceof BinaryOperation && (((BinaryOperation) f).getType().equals(TypeBinary.PLUS)
                || ((BinaryOperation) f).getType().equals(TypeBinary.MINUS))) {
            return new BinaryOperation(divideExpressionByPowerOfVar(((BinaryOperation) f).getLeft(), var, exponent),
                    divideExpressionByPowerOfVar(((BinaryOperation) f).getRight(), var, exponent), ((BinaryOperation) f).getType()).simplify();
        }
        return f.div(Variable.create(var).pow(exponent)).simplify();
    }

    /*
     Liefert, den maximalen Grad d eines Monoms, so dass das durch die
     Koeffizienten coefficientsOfDivisionRest gegebene Polynom eine
     Substitution durch ein Monom des Grades d erlaubt. Beispiel: Beim Polynom
     x^15 + 2*x^10 + 7*x^5 + 1 wird der Wert 5 zurückgegeben. -> Später
     Substitution x^5 = t möglich.
     */
    public static int getMaximalMonomial(ExpressionCollection coefficients, String var) {

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

    /**
     * Mach aus einem Polynom derivative Form a_{km}*x^{km} + ... + a_m*x^m +
     * a_0 (welches durch die Koeffizienten coefficientsOfDivisionRest gegeben
     * ist) das Polynom a_{km}*y^period + ... + a_m*y + a_0.
     */
    public static ExpressionCollection substituteMonomialInPolynomial(ExpressionCollection coefficients, int m, String var) {

        ExpressionCollection resultCoefficient = new ExpressionCollection();
        for (int i = 0; i < coefficients.getBound(); i++) {
            if (i % m == 0) {
                resultCoefficient.put(resultCoefficient.getBound(), coefficients.get(i));
            }
        }
        return resultCoefficient;

    }

    /**
     * Hauptmethode zum Lösen von Polynomgleichungen.
     */
    public static ExpressionCollection solvePolynomialEquation(ExpressionCollection coefficients, String var) throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();

        int degree = coefficients.getBound() - 1;

        /*
         Fall: f is ein Polynom in nichttrivialen Monomen. -> Substitution x^m
         = y und dann zuerst nach y auflösen und dann nach x.
         */
        int m = getMaximalMonomial(coefficients, var);
        if (m > 1 && degree / m <= ComputationBounds.BOUND_DEGREE_OF_POLYNOMIAL_FOR_SOLVING_EQUATION) {
            ExpressionCollection coefficientsOfSubstitutedPolynomial = substituteMonomialInPolynomial(coefficients, m, var);
            ExpressionCollection zerosOfSubstitutedPolynomial = solvePolynomialEquation(coefficientsOfSubstitutedPolynomial, var);
            if (m % 2 == 0) {
                for (int i = 0; i < zerosOfSubstitutedPolynomial.getBound(); i++) {

                    // Es sollen nur konstante nichtnegative oder nichtkonstante Lösungen akzeptiert werden.
                    if (zerosOfSubstitutedPolynomial.get(i).isConstant() && zerosOfSubstitutedPolynomial.get(i).isNonNegative()) {
                        zeros.put(zeros.getBound(), zerosOfSubstitutedPolynomial.get(i).pow(1, m).simplify());
                        zeros.put(zeros.getBound(), Expression.MINUS_ONE.mult(zerosOfSubstitutedPolynomial.get(i).pow(1, m)).simplify());
                    } else if (!zerosOfSubstitutedPolynomial.get(i).isConstant()) {
                        zeros.put(zeros.getBound(), zerosOfSubstitutedPolynomial.get(i).pow(1, m).simplify());
                        zeros.put(zeros.getBound(), Expression.MINUS_ONE.mult(zerosOfSubstitutedPolynomial.get(i).pow(1, m)).simplify());
                    }

                }
            } else {
                for (int i = 0; i < zerosOfSubstitutedPolynomial.getBound(); i++) {
                    zeros.put(zeros.getBound(), zerosOfSubstitutedPolynomial.get(i).pow(1, m).simplify());
                }
            }
        }
        if (!zeros.isEmpty()) {
            return zeros;
        }

        // Fall: f is ein Polynom mit zyklischen Koeffizienten.
        m = PolynomialRootsMethods.getPeriodOfCoefficients(coefficients);
        if (m < coefficients.getBound()) {
            return PolynomialRootsMethods.solveCyclicPolynomialEquation(coefficients, var);
        }

        // (Nichttriviale) Polynome sollen nur dann exakt gelöst werden, wenn deg - ord <= 100 ist.
        if (degree <= ComputationBounds.BOUND_DEGREE_OF_POLYNOMIAL_FOR_SOLVING_EQUATION) {

            if (degree == 0) {
                return zeros;
            }

            ExpressionCollection rationalZerosWithMultiplicities = new ExpressionCollection();
            /*
             Polynomdivision durch alle Linearfaktoren, welche zu den bisher
             gefundenen rationalen Nullstellen gehören.
             */
            coefficients = findAllRationalZerosOfPolynomial(coefficients, rationalZerosWithMultiplicities);
            degree = degree - rationalZerosWithMultiplicities.getBound();

            // Mehrfache Lösungen beseitigen!
            ExpressionCollection rationalZeros = SimplifyUtilities.removeMultipleEntries(rationalZerosWithMultiplicities);

            if (!rationalZeros.isEmpty()) {
                zeros = SimplifyUtilities.union(zeros, rationalZeros);
            }

            // Nullstellen von Polynomen vom Grad 1 - 3 können direkt ermittelt werden.
            if (degree == 1) {
                return SimplifyUtilities.union(zeros, PolynomialRootsMethods.solveLinearEquation(coefficients));
            }
            if (degree == 2) {
                return SimplifyUtilities.union(zeros, PolynomialRootsMethods.solveQuadraticEquation(coefficients));
            }
            if (degree == 3) {
                return SimplifyUtilities.union(zeros, PolynomialRootsMethods.solveCubicEquation(coefficients));
            }

        }

        return zeros;

    }

    /**
     * Ermittelt eine rationale Nullstelle eines Polynoms, welches durch
     * coefficients gegeben ist, falls diese existiert. Die Koeffizienten
     * coefficients sind indiziert via 0, 1, ..., degree. Zurückgegeben wird
     * entweder ein array {a, b} mit zwei BigInteger-Instanzen, so dass die
     * Nullstelle durch a/b gegeben ist, oder ein leeres BigInteger-Array, falls
     * keine rationale Nullstelle gefunden wurde, oder falls eines der
     * coefficients KEINE ganzzahlige Konstante ist.
     */
    public static BigInteger[] findRationalZeroOfPolynomial(ExpressionCollection coefficients) {

        for (int i = 0; i < coefficients.getBound(); i++) {
            if (!coefficients.get(i).isIntegerConstant()) {
                BigInteger[] zero = new BigInteger[0];
                return zero;
            }
        }

        /*
         Folgende Notation: Das Polynom soll die Form q*x^n + ... + p besitzen
         (also p = coefficients.get(0), q =
         coefficients.get(coefficients.getBound() - 1)).
         */
        BigInteger polynomValue;
        HashMap<Integer, BigInteger> pDivisors = ArithmeticMethods.getDivisors(((Constant) coefficients.get(0)).getValue().toBigInteger());
        HashMap<Integer, BigInteger> qDivisors = ArithmeticMethods.getDivisors(((Constant) coefficients.get(coefficients.getBound() - 1)).getValue().toBigInteger());

        // WICHTIG: 0 muss zum testen ebenfalls aufgenommen werden.
        HashMap<Integer, BigInteger> pDivisorsWithZero = new HashMap();
        pDivisorsWithZero.put(0, BigInteger.ZERO);
        for (int i = 0; i < pDivisors.size(); i++) {
            pDivisorsWithZero.put(pDivisorsWithZero.size(), pDivisors.get(i));
        }
        pDivisors = pDivisorsWithZero;
        // Jetzt enthält pDivisors auch die 0.

        for (int i = 0; i < pDivisors.size(); i++) {
            for (int j = 0; j < qDivisors.size(); j++) {
                BigInteger pDivisorPower;
                BigInteger qDivisorPower;

                // Test, ob p/q eine Nullstelle des Polynoms ist.
                polynomValue = BigInteger.ZERO;
                for (int k = 0; k <= coefficients.getBound() - 1; k++) {
                    pDivisorPower = ((BigInteger) pDivisors.get(i)).pow(k);
                    qDivisorPower = ((BigInteger) qDivisors.get(j)).pow(coefficients.getBound() - 1 - k);
                    polynomValue = polynomValue.add(((Constant) coefficients.get(k)).getValue().toBigInteger().multiply(
                            pDivisorPower.multiply(qDivisorPower)));
                }
                if (polynomValue.compareTo(BigInteger.ZERO) == 0) {
                    BigInteger[] zero = new BigInteger[2];
                    zero[0] = pDivisors.get(i);
                    zero[1] = qDivisors.get(j);
                    return zero;
                }
                // Test, ob -p/q eine Nullstelle des Polynoms ist.
                polynomValue = BigInteger.ZERO;
                for (int k = 0; k <= coefficients.getBound() - 1; k++) {
                    pDivisorPower = ((BigInteger) pDivisors.get(i)).negate().pow(k);
                    qDivisorPower = ((BigInteger) qDivisors.get(j)).pow(coefficients.getBound() - 1 - k);
                    polynomValue = polynomValue.add(((Constant) coefficients.get(k)).getValue().toBigInteger().multiply(
                            pDivisorPower.multiply(qDivisorPower)));
                }
                if (polynomValue.compareTo(BigInteger.ZERO) == 0) {
                    BigInteger[] zero = new BigInteger[2];
                    zero[0] = pDivisors.get(i).negate();
                    zero[1] = qDivisors.get(j);
                    return zero;
                }

            }
        }

        return new BigInteger[0];

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
    public static ExpressionCollection[] polynomialDivision(ExpressionCollection coefficientsEnumerator, ExpressionCollection coefficientsDenominator)
            throws EvaluationException {

        // Ergebnisformat: 0 - Quotient, 1 - Divisionsrest.
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

        /*
         Polynomdivisionsalgorithmus anwenden. Am Ende ist der Divisionsrest
         in coeffcicientsEnumeratorCopy gespeichert. Dieser wird aber im
         Ergebnis verworfen.
         */
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

        // Divisionsrest in result[1] kopieren und ausgeben.
        for (int i = 0; i <= indexOfLeadingCoefficientInRest; i++) {
            quotient[1].put(i, coeffcicientsEnumeratorCopy.get(i));
        }

        return quotient;

    }

    /**
     * Falls die Koeffizienten coefficients[i] des Polynoms alle rational sind,
     * so liefert diese Methode alle rationalen Nullstellen des Polynoms mit
     * |Zähler|, |Nenner| <= 1000000. Diese werden dem ExpressionCollection
     * rationalZeros hinten hinzugefügt. Der Rückgabewert ist das Ergebnis der
     * Polynomdivision durch alle ermittelten Linearfaktoren.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection findAllRationalZerosOfPolynomial(ExpressionCollection coefficients, ExpressionCollection rationalZeros)
            throws EvaluationException {

        boolean allCoefficientsAreRational = true;

        for (int i = 0; i < coefficients.getBound(); i++) {
            if (!coefficients.get(i).isIntegerConstantOrRationalConstant()) {
                allCoefficientsAreRational = false;
                break;
            }
        }

        if (!allCoefficientsAreRational) {
            return coefficients;
        }

        for (int i = 0; i < coefficients.getBound(); i++) {
            coefficients.put(i, coefficients.get(i).simplify());
        }

        /*
         Alle Polynomkoeffizienten werden mit dem kleinsten gemeinsamen Nenner
         multipliziert, damit alle Koeffizienten ganzzahlig werden.
         */
        BigInteger commonDenominator = BigInteger.ONE;
        BigInteger[] pairForLCM = new BigInteger[2];
        for (int i = 0; i < coefficients.getBound(); i++) {
            if (coefficients.get(i) instanceof BinaryOperation) {
                pairForLCM[0] = commonDenominator;
                pairForLCM[1] = ((Constant) ((BinaryOperation) coefficients.get(i)).getRight()).getValue().toBigInteger();
                commonDenominator = ArithmeticMethods.lcm(pairForLCM);
            }
        }

        ExpressionCollection coefficientsOfDivisionQuotient = new ExpressionCollection();
        if (commonDenominator.compareTo(BigInteger.ONE) > 0) {
            for (int i = 0; i < coefficients.getBound(); i++) {
                coefficients.put(i, coefficients.get(i).mult(commonDenominator).simplify());
                coefficientsOfDivisionQuotient.put(i, coefficients.get(i));
            }
        } else {
            for (int i = 0; i < coefficients.getBound(); i++) {
                coefficientsOfDivisionQuotient.put(i, coefficients.get(i));
            }
        }

        // Eigentliche Polynomdivision.
        BigInteger[] zero;
        ExpressionCollection divisor = new ExpressionCollection();
        for (int i = 0; i < coefficients.getBound() - 1; i++) {
            zero = findRationalZeroOfPolynomial(coefficientsOfDivisionQuotient);
            if (zero.length > 0) {
                rationalZeros.put(rationalZeros.getBound(), new Constant(zero[0]).div(zero[1]).simplify());
                divisor.put(0, new Constant(zero[0].negate()));
                divisor.put(1, new Constant(zero[1]));
                coefficientsOfDivisionQuotient = polynomialDivision(coefficientsOfDivisionQuotient, divisor)[0];
            } else {
                break;
            }
        }

        coefficients = new ExpressionCollection();
        for (int i = 0; i < coefficientsOfDivisionQuotient.getBound(); i++) {
            coefficients.put(i, coefficientsOfDivisionQuotient.get(i));
        }

        return coefficients;

    }

    /**
     * Ermittelt die Lösungen von coefficients.get(1)*x + coefficients.get(0) =
     * 0; Voraussetzung: coefficients.get(1) != 0.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveLinearEquation(ExpressionCollection coefficients) throws EvaluationException {
        ExpressionCollection zeros = new ExpressionCollection();
        zeros.put(0, (Expression.MINUS_ONE).mult(coefficients.get(0)).div(coefficients.get(1)).simplify());
        return zeros;
    }

    /**
     * Ermittelt die Lösungen von coefficients.get(2)*x^2 +
     * coefficients.get(1)*x + coefficients.get(0) = 0; Voraussetzung:
     * coefficients.get(2) != 0.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveQuadraticEquation(ExpressionCollection coefficients) throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();
        Expression p = coefficients.get(1).div(coefficients.get(2)).simplify();
        Expression q = coefficients.get(0).div(coefficients.get(2)).simplify();

        // Diskriminante diskr = p^2/4 - q.
        Expression diskriminant = p.pow(2).div(4).sub(q).simplify();

        if (!diskriminant.isConstant()) {
            // Rein abstrakt nach p-q-Formel auflösen: x = (-p +- (p^2 - 4q)^(1/2))/2
            zeros.put(0, (Expression.MINUS_ONE).mult(p).div(2).sub(diskriminant.pow(1, 2)).simplify());
            zeros.put(1, (Expression.MINUS_ONE).mult(p).div(2).add(diskriminant.pow(1, 2)).simplify());
            return zeros;
        }

        if (diskriminant.isNonNegative() && !diskriminant.equals(Expression.ZERO)) {
            // Nach p-q-Formel auflösen: x = (-p +- (p^2 - 4q)^(1/2))/2
            zeros.put(0, (Expression.MINUS_ONE).mult(p).div(2).sub(diskriminant.pow(1, 2)).simplify());
            zeros.put(1, (Expression.MINUS_ONE).mult(p).div(2).add(diskriminant.pow(1, 2)).simplify());
            return zeros;
        } else if (diskriminant.equals(Expression.ZERO)) {
            zeros.put(0, p.div(-2).simplify());
            return zeros;
        }

        // Es konnten keine Lösungen ermittelt werden.
        return zeros;

    }

    /**
     * Ermittelt die Lösungen von coefficients.get(3)*x^3 +
     * coefficients.get(2)*x^2 + coefficients.get(1)*x + coefficients.get(0) =
     * 0; Voraussetzung: coefficients.get(3) != 0.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveCubicEquation(ExpressionCollection coefficients) throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();
        Expression A = coefficients.get(2).div(coefficients.get(3)).simplify();
        Expression B = coefficients.get(1).div(coefficients.get(3)).simplify();
        Expression C = coefficients.get(0).div(coefficients.get(3)).simplify();

        // Gelöst wird nun die Gleichung x^3 + Ax^2 + Bx + C = 0
        /*
         Substitution x = z - A/3 (später muss zurücksubstituiert werden): p =
         B - A^2/3, q = 2A^3/27 - AB/3 + C. Gelöst wird nun die Gleichung z^3
         + pz + q = 0
         */
        Expression p = B.sub(A.pow(2).div(3)).simplify();
        Expression q = Expression.TWO.mult(A.pow(3).div(27)).sub(A.mult(B).div(3)).add(C).simplify();

        // Diskriminante diskr = (p/3)^3 + (q/2)^2 = p^3/27 + q^2/4.
        Expression diskriminant = p.pow(3).div(27).add(q.pow(2).div(4)).simplify();

        if (!diskriminant.isConstant()) {

            Expression radikand = new Constant(-27).div(p.pow(3)).simplify();
            if (!radikand.isConstant() || radikand.isNonNegative()) {
                // Casus irreduzibilis.
                Expression arg = new Function(Expression.MINUS_ONE.mult(q.div(2)).mult(new Constant(-27).div(p.pow(3)).pow(1, 2)),
                        TypeFunction.arccos).div(3).simplify();
                Expression factor = (new Constant(-4).mult(p).div(3)).pow(1, 2).simplify();
                zeros.put(0, factor.mult(new Function(arg, TypeFunction.cos)).sub(A.div(3)).simplify());
                zeros.put(1, (Expression.MINUS_ONE).mult(factor).mult((new Function(arg.add(Expression.PI.div(3)), TypeFunction.cos))).sub(A.div(3)).simplify());
                zeros.put(2, (Expression.MINUS_ONE).mult(factor).mult((new Function(arg.sub(Expression.PI.div(3)), TypeFunction.cos))).sub(A.div(3)).simplify());
            } else {
                Expression u = (diskriminant.pow(Expression.ONE.div(2)).sub(q.div(2)).simplify()).pow(1, 3);
                Expression v = (Expression.MINUS_ONE).mult(diskriminant.pow(1, 2)).sub(q.div(2)).simplify().pow(1, 3);
                zeros.put(0, u.add(v).sub(A.div(3)).simplify());
            }
            return zeros;
        }

        if (diskriminant.isNonPositive() && !diskriminant.equals(Expression.ZERO)) {
            // Casus irreduzibilis.
            Expression arg = new Function(Expression.MINUS_ONE.mult(q.div(2)).mult(new Constant(-27).div(p.pow(3)).pow(1, 2)),
                    TypeFunction.arccos).div(3).simplify();
            Expression factor = (new Constant(-4).mult(p).div(3)).pow(1, 2);
            zeros.put(0, factor.mult(new Function(arg, TypeFunction.cos)).sub(A.div(3)).simplify());
            zeros.put(1, (Expression.MINUS_ONE).mult(factor).mult((new Function(arg.add(Expression.PI.div(3)), TypeFunction.cos))).sub(A.div(3)).simplify());
            zeros.put(2, (Expression.MINUS_ONE).mult(factor).mult((new Function(arg.sub(Expression.PI.div(3)), TypeFunction.cos))).sub(A.div(3)).simplify());
            return zeros;
        } else if (diskriminant.equals(Expression.ZERO)) {
            // Auflösung nach Cardano-Formel: x = 2*(-q/2)^(1/3).
            zeros.put(0, Expression.TWO.mult((q.div(-2)).pow(1, 3)).sub(A.div(3)).simplify());
            return zeros;
        }

        /*
         In diesem Fall ist diskriminant positiv. Auflösung nach
         Cardano-Formel: x = (-q/2 + diskriminant^(1/2))^(1/3) + (-q/2 -
         diskriminant^(1/2))^(1/3).
         */
        Expression u = (diskriminant.pow(1, 2).sub(q.div(2)).simplify()).pow(1, 3);
        Expression v = (Expression.MINUS_ONE).mult(diskriminant.pow(1, 2)).sub(q.div(2)).simplify().pow(1, 3);
        zeros.put(0, u.add(v).sub(A.div(3)).simplify());
        return zeros;

    }

    /**
     * Liefert die kleinste Periode, unter welcher die Koeffizienten
     * coefficients periodisch sind.
     */
    public static int getPeriodOfCoefficients(ExpressionCollection coefficients) {

        int l = coefficients.getBound();
        HashMap<Integer, BigInteger> cycleLengths = ArithmeticMethods.getDivisors(BigInteger.valueOf(l));

        ExpressionCollection periodForCompare, currentPeriod;
        boolean periodFound = true;

        for (int i = 0; i < cycleLengths.size(); i++) {

            periodForCompare = ExpressionCollection.copy(coefficients, 0, cycleLengths.get(i).intValue());

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
     * Ermittelt die Nullstellen des Polynoms mit den Koeffizienten
     * coefficients, falls coefficients nichttriviale Perioden aufweist.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveCyclicPolynomialEquation(ExpressionCollection coefficients, String var) throws EvaluationException {

        int period = getPeriodOfCoefficients(coefficients);
        int n = coefficients.getBound() / period;
        ExpressionCollection result = solvePolynomialEquation(ExpressionCollection.copy(coefficients, 0, period), var);

        if ((period / 2) * 2 != period && (n / 2) * 2 == n) {
            ExpressionCollection specialZero = new ExpressionCollection();
            specialZero.put(0, Expression.MINUS_ONE);
            return SimplifyUtilities.union(result, specialZero);
        }
        return result;

    }

    /**
     * Hilfsmethode. Falls f kein Polynom in gebrochenen Potenzen von var ist,
     * so wird f wieder zurückgegeben. Andernfalls ersetzt es die Variable var
     * in f durch var^n und (!) fasst Exponenten zusammen. U. a. wird auch die
     * (nicht immer korrekte) Regel (x^2)^(1/2) = x angewendet. Dies wird aber
     * dennoch aus technischen Gründen benötigt, um Lösungen gewisser
     * Polynomgleichungen zu ermitteln. Diese Lösungen werden aber anschließend
     * in einer anderen Prozedur auf ihre Korrektheit überprüft.
     */
    private static Expression substituteVariableByPowerOfVariable(Expression f, String var, BigInteger n) {

        if (!isPolynomialAfterSubstitutionByRoots(f, var)) {
            return f;
        }

        if (!f.contains(var)) {
            return f;
        }
        if (f instanceof Variable && ((Variable) f).getName().equals(var)) {
            return f.pow(n);
        }
        if (f instanceof BinaryOperation) {

            if (((BinaryOperation) f).getType().equals(TypeBinary.POW)) {

                // Der Exponent ist eine nichtnegative ganze Zahl, da f hier ein Polynom ist.
                Expression leftSubstituted = substituteVariableByPowerOfVariable(((BinaryOperation) f).getLeft(), var, n);
                if (leftSubstituted instanceof BinaryOperation
                        && ((BinaryOperation) leftSubstituted).getType().equals(TypeBinary.POW)) {
                    return ((BinaryOperation) leftSubstituted).getLeft().pow(((BinaryOperation) leftSubstituted).getRight().mult(((BinaryOperation) f).getRight()));
                }
                return leftSubstituted.pow(((BinaryOperation) f).getRight());

            }

            return new BinaryOperation(substituteVariableByPowerOfVariable(((BinaryOperation) f).getLeft(), var, n),
                    substituteVariableByPowerOfVariable(((BinaryOperation) f).getRight(), var, n), ((BinaryOperation) f).getType());

        }

        return f;

    }

    /**
     * Hauptmethode zum Lösen von Polynomgleichungen in gebrochenen Potenzen von
     * var.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solvePolynomialEquationWithFractionalExponents(Expression f, String var)
            throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();

        BigInteger m = findExponentForPolynomialSubstitutionByRoots(f, var);
        if (m.compareTo(BigInteger.ONE) > 0) {

            Expression fSubstituted = substituteVariableByPowerOfVariable(f, var, m).simplify();
            zeros = solvePolynomialEquation(getPolynomialCoefficients(fSubstituted, var), var);
            if (m.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {

                ExpressionCollection zerosAfterResubstitution = new ExpressionCollection();
                for (int i = 0; i < zeros.getBound(); i++) {
                    if (!zeros.get(i).isConstant() || zeros.get(i).isNonNegative()) {
                        zerosAfterResubstitution.put(zerosAfterResubstitution.getBound(), zeros.get(i).pow(m).simplify());
                    }
                }
                return zerosAfterResubstitution;

            } else {

                for (int i = 0; i < zeros.getBound(); i++) {
                    zeros.put(i, zeros.get(i).pow(m).simplify());
                }
                return zeros;

            }

        }

        return zeros;

    }

}

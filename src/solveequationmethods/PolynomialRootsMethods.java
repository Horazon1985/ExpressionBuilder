package solveequationmethods;

import computation.ArithmeticMethods;
import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.MINUS_ONE;
import static expressionbuilder.Expression.PI;
import static expressionbuilder.Expression.TWO;
import static expressionbuilder.Expression.ZERO;
import expressionbuilder.Function;
import expressionbuilder.TypeBinary;
import expressionbuilder.TypeFunction;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyPolynomialMethods;
import expressionsimplifymethods.SimplifyUtilities;
import java.math.BigInteger;
import java.util.HashMap;

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
     * Hilfsfunktion: liefert f/x^exponent, wobei x = var.
     */
    public static Expression divideExpressionByPowerOfVar(Expression f, String var, BigInteger exponent) throws EvaluationException {
        if (f.isSum() || f.isDifference()) {
            return new BinaryOperation(divideExpressionByPowerOfVar(((BinaryOperation) f).getLeft(), var, exponent),
                    divideExpressionByPowerOfVar(((BinaryOperation) f).getRight(), var, exponent), ((BinaryOperation) f).getType()).simplify();
        }
        return f.div(Variable.create(var).pow(exponent)).simplify();
    }

    /**
     * Macht aus einem Polynom der Form a_{km}*x^{km} + ... + a_m*x^m + a_0
     * (welches durch die Koeffizienten coefficients gegeben ist) das Polynom
     * a_{km}*y^k + ... + a_m*y + a_0.
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
        int m = SimplifyPolynomialMethods.getGGTOfAllExponents(coefficients);
        if (m > 1 && degree / m <= ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL_EQUATION) {
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
        m = SimplifyPolynomialMethods.getPeriodOfCoefficients(coefficients);
        if (m < coefficients.getBound()) {
            return PolynomialRootsMethods.solvePeriodicPolynomialEquation(coefficients, var);
        }

        // (Nichttriviale) Polynome sollen nur dann exakt gelöst werden, wenn deg - ord <= 100 ist.
        if (degree <= ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL_EQUATION) {

            if (degree == 0) {
                return zeros;
            }

            ExpressionCollection rationalZeros = new ExpressionCollection();
            /*
             Polynomdivision durch alle Linearfaktoren, welche zu den bisher
             gefundenen rationalen Nullstellen gehören.
             */
            coefficients = findAllRationalZerosOfRationalPolynomial(coefficients, rationalZeros);
            degree = degree - rationalZeros.getBound();

            // Mehrfache Lösungen beseitigen!
            rationalZeros.removeMultipleTerms();

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
     * Falls die Koeffizienten a[i] des Polynoms alle rational sind, so liefert
     * diese Methode alle rationalen Nullstellen des Polynoms mit |Zähler|,
     * |Nenner| <= eine gewisse Schranke. Diese werden der ExpressionCollection
     * rationalZeros hinzugefügt. Der Rückgabewert ist das normierte .Ergebnis
     * der Polynomdivision durch alle ermittelten Linearfaktoren. Sind die
     * Koeffizienten a nicht allesamt rational, so wird die ExpressionCollection
     * a zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection findAllRationalZerosOfRationalPolynomial(ExpressionCollection a, ExpressionCollection rationalZeros)
            throws EvaluationException {

        for (int i = 0; i < a.getBound(); i++) {
            if (!a.get(i).isIntegerConstantOrRationalConstant()) {
                return a;
            }
        }

        /*
         Alle Polynomkoeffizienten werden mit dem kleinsten gemeinsamen Nenner
         multipliziert, damit alle Koeffizienten ganzzahlig werden.
         */
        BigInteger commonDenominator = BigInteger.ONE;
        for (int i = 0; i < a.getBound(); i++) {
            if (a.get(i).isQuotient()) {
                commonDenominator = ArithmeticMethods.lcm(new BigInteger[]{commonDenominator, ((Constant) ((BinaryOperation) a.get(i)).getRight()).getValue().toBigInteger()});
            }
        }

        ExpressionCollection coefficientsOfDivisionQuotient;
        ExpressionCollection multipleOfCoefficients = ExpressionCollection.copy(a);

        multipleOfCoefficients.multExpression(new Constant(commonDenominator));
        multipleOfCoefficients = multipleOfCoefficients.simplify();
        coefficientsOfDivisionQuotient = ExpressionCollection.copy(multipleOfCoefficients);

        // Eigentliche Polynomdivision.
        BigInteger[] zero;
        ExpressionCollection divisor = new ExpressionCollection();
        // Es kann höchstens soviele (rationale) Nullstellen geben, wie der Grad des Polynoms (= a.getBound() - 1) beträgt.
        for (int i = 0; i < a.getBound() - 1; i++) {
            zero = findRationalZeroOfPolynomial(coefficientsOfDivisionQuotient);
            if (zero.length > 0) {
                rationalZeros.add(new Constant(zero[0]).div(zero[1]).simplify());
                divisor.put(0, new Constant(zero[0].negate()));
                divisor.put(1, new Constant(zero[1]));
                coefficientsOfDivisionQuotient = SimplifyPolynomialMethods.polynomialDivision(coefficientsOfDivisionQuotient, divisor)[0];
            } else {
                break;
            }
        }

        // Zum Schluss: Ergebnis der Polynomdivision normieren.
        coefficientsOfDivisionQuotient.divByExpression(coefficientsOfDivisionQuotient.get(coefficientsOfDivisionQuotient.getBound() - 1));
        coefficientsOfDivisionQuotient = coefficientsOfDivisionQuotient.simplify();

        return coefficientsOfDivisionQuotient;

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
        Expression discriminant = p.pow(2).div(4).sub(q).simplify();

        if (!discriminant.isAlwaysNonNegative() && !discriminant.isAlwaysNonPositive()) {
            // Rein abstrakt nach p-q-Formel auflösen: x = (-p +- (p^2 - 4q)^(1/2))/2
            zeros.put(0, Expression.MINUS_ONE.mult(p).div(2).sub(discriminant.pow(1, 2)).simplify());
            zeros.put(1, Expression.MINUS_ONE.mult(p).div(2).add(discriminant.pow(1, 2)).simplify());
            return zeros;
        }

        if (discriminant.isAlwaysNonNegative() && !discriminant.equals(Expression.ZERO)) {
            // Nach p-q-Formel auflösen: x = (-p +- (p^2 - 4q)^(1/2))/2
            zeros.put(0, Expression.MINUS_ONE.mult(p).div(2).sub(discriminant.pow(1, 2)).simplify());
            zeros.put(1, Expression.MINUS_ONE.mult(p).div(2).add(discriminant.pow(1, 2)).simplify());
            return zeros;
        } else if (discriminant.equals(Expression.ZERO)) {
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
        Expression q = TWO.mult(A.pow(3).div(27)).sub(A.mult(B).div(3)).add(C).simplify();

        // Diskriminante discriminant = (p/3)^3 + (q/2)^2 = p^3/27 + q^2/4.
        Expression discriminant = p.pow(3).div(27).add(q.pow(2).div(4)).simplify();

        if (!discriminant.isAlwaysNonNegative() && !discriminant.isAlwaysNonPositive()) {
            /* 
             Dann ist die Diskriminante nichtkonstant und hat kein eindeutiges Vorzeichen. 
             In diesem Fall drei abstrakte Lösungen (gemäß Casus irreduzibilis) zurückgeben.
             */
            Expression arg = MINUS_ONE.mult(q.div(2)).mult(new Constant(-27).div(p.pow(3)).pow(1, 2)).arccos().div(3).simplify();
            Expression factor = (new Constant(-4).mult(p).div(3)).pow(1, 2).simplify();
            zeros.put(0, factor.mult(arg.cos()).sub(A.div(3)).simplify());
            zeros.put(1, MINUS_ONE.mult(factor).mult(arg.add(PI.div(3)).cos()).sub(A.div(3)).simplify());
            zeros.put(2, MINUS_ONE.mult(factor).mult(arg.sub(PI.div(3)).cos()).sub(A.div(3)).simplify());
            return zeros;
        }

        if (discriminant.isAlwaysNegative()) {
            // Casus irreduzibilis.
            Expression arg = MINUS_ONE.mult(q.div(2)).mult(new Constant(-27).div(p.pow(3)).pow(1, 2)).arccos().div(3).simplify();
            Expression factor = (new Constant(-4).mult(p).div(3)).pow(1, 2);
            zeros.put(0, factor.mult(new Function(arg, TypeFunction.cos)).sub(A.div(3)).simplify());
            zeros.put(1, MINUS_ONE.mult(factor).mult(arg.add(Expression.PI.div(3)).cos()).sub(A.div(3)).simplify());
            zeros.put(2, MINUS_ONE.mult(factor).mult(arg.sub(Expression.PI.div(3)).cos()).sub(A.div(3)).simplify());
            return zeros;
        } else if (discriminant.equals(ZERO)) {
            // Auflösung nach Cardano-Formel: 
            // Doppellösung z = (q/2)^(1/3)
            zeros.put(0, (q.div(2).pow(1, 3)).sub(A.div(3)).simplify());
            // Einfache Lösung z = (-4q)^(1/3)
            zeros.put(1, new Constant(-4).mult(q).pow(1, 3).sub(A.div(3)).simplify());
            return zeros;
        }

        /*
         In diesem Fall ist discriminant positiv. Auflösung nach
         Cardano-Formel: z = (-q/2 + discriminant^(1/2))^(1/3) + (-q/2 -
         discriminant^(1/2))^(1/3).
         */
        Expression u = (discriminant.pow(1, 2).sub(q.div(2)).simplify()).pow(1, 3);
        Expression v = MINUS_ONE.mult(discriminant.pow(1, 2)).sub(q.div(2)).simplify().pow(1, 3);
        zeros.put(0, u.add(v).sub(A.div(3)).simplify());
        return zeros;

    }

    /**
     * Ermittelt die Nullstellen des Polynoms mit den Koeffizienten
     * coefficients, falls coefficients nichttriviale Perioden aufweist.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solvePeriodicPolynomialEquation(ExpressionCollection coefficients, String var) throws EvaluationException {

        int period = SimplifyPolynomialMethods.getPeriodOfCoefficients(coefficients);
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
            zeros = solvePolynomialEquation(SimplifyPolynomialMethods.getPolynomialCoefficients(fSubstituted, var), var);
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

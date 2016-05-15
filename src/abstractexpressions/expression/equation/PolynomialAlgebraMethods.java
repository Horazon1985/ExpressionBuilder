package abstractexpressions.expression.equation;

import abstractexpressions.expression.computation.ArithmeticMethods;
import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.FOUR;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.PI;
import static abstractexpressions.expression.classes.Expression.THREE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.TypeBinary;
import abstractexpressions.expression.classes.TypeFunction;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyPolynomialMethods;
import abstractexpressions.expression.utilities.SimplifyUtilities;
import exceptions.NotAlgebraicallySolvableException;
import java.math.BigInteger;
import java.util.ArrayList;

public abstract class PolynomialAlgebraMethods {

    public static BigInteger getGCDOfExponentsInPolynomial(Expression f, String var) {

        if (!f.contains(var)) {
            return BigInteger.ZERO;
        }
        if (f.equals(Variable.create(var))) {
            return BigInteger.ONE;
        }
        if (f.isSum() || f.isDifference() || f.isProduct()) {
            return getGCDOfExponentsInPolynomial(((BinaryOperation) f).getLeft(), var).gcd(getGCDOfExponentsInPolynomial(((BinaryOperation) f).getRight(), var));
        }
        if (f.isQuotient() && !((BinaryOperation) f).getRight().contains(var)) {
            return getGCDOfExponentsInPolynomial(((BinaryOperation) f).getLeft(), var);
        }
        if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant()) {
            if (((BinaryOperation) f).getLeft().equals(Variable.create(var))) {
                return ((Constant) ((BinaryOperation) f).getRight()).getValue().toBigInteger();
            }
            return getGCDOfExponentsInPolynomial(((BinaryOperation) f).getLeft(), var);
        }
        // Dann ist es kein Polynom.
        return BigInteger.ONE.negate();

    }

    /**
     * Gibt zurück, ob expr ein Polynom in derivative Variablen x ist, falls die
     * Variable var durch ein geeignetes var = x^m, m ganzzahlig, substituiert
     * wird. Voraussetzung: f ist vereinfacht, d.h. Operatoren etc. kommen NICHT
     * vor (außer evtl. Gamma(x), was kein Polynom ist).
     */
    public static boolean isPolynomialInRationalPowers(Expression f, String var) {

        if (!f.contains(var)) {
            return true;
        }
        if (f.equals(Variable.create(var))) {
            return true;
        }
        if (f.isSum() || f.isDifference() || f.isProduct()) {
            return isPolynomialInRationalPowers(((BinaryOperation) f).getLeft(), var) && isPolynomialInRationalPowers(((BinaryOperation) f).getRight(), var);
        }
        if (f.isQuotient() && !((BinaryOperation) f).getRight().contains(var)) {
            return isPolynomialInRationalPowers(((BinaryOperation) f).getLeft(), var);
        }
        if (f.isPower()) {
            if (((BinaryOperation) f).getRight().isIntegerConstant()) {
                return isPolynomialInRationalPowers(((BinaryOperation) f).getLeft(), var);
            } else if (((BinaryOperation) f).getRight().isRationalConstant()) {
                return ((BinaryOperation) f).getLeft().equals(Variable.create(var));
            }
        }

        return false;

    }

    /**
     * Falls isPolynomialInRationalPowers() true zurückgibt, gibt es den
     * kleinsten Exponenten m für eine geeignete Substitution var = y^m aus, so
     * dass die resultierende Gleichung ein Polynom in y wird. VORAUSSETZUNG:
     * expr muss ein Polynom sein.
     */
    private static BigInteger findExponentForPolynomialSubstitutionByRoots(Expression f, String var) {

        if (f instanceof Constant) {
            return BigInteger.ONE;
        }
        if (f instanceof Variable) {
            return BigInteger.ONE;
        }
        if (f instanceof BinaryOperation) {

            if (((BinaryOperation) f).getType().equals(TypeBinary.PLUS)
                    || ((BinaryOperation) f).getType().equals(TypeBinary.MINUS)
                    || ((BinaryOperation) f).getType().equals(TypeBinary.TIMES)) {
                BigInteger m = findExponentForPolynomialSubstitutionByRoots(((BinaryOperation) f).getLeft(), var);
                BigInteger n = findExponentForPolynomialSubstitutionByRoots(((BinaryOperation) f).getRight(), var);
                return m.multiply(n).divide(m.gcd(n));
            }
            if (((BinaryOperation) f).getType().equals(TypeBinary.DIV) && ((BinaryOperation) f).getRight().isConstant()) {
                return findExponentForPolynomialSubstitutionByRoots(((BinaryOperation) f).getLeft(), var);
            }
            if (((BinaryOperation) f).getType().equals(TypeBinary.POW)) {

                if (((BinaryOperation) f).getRight().isIntegerConstant()) {
                    return findExponentForPolynomialSubstitutionByRoots(((BinaryOperation) f).getLeft(), var);
                } else if (((BinaryOperation) f).getRight().isRationalConstant()
                        && ((BinaryOperation) f).getLeft() instanceof Variable
                        && ((Variable) ((BinaryOperation) f).getLeft()).getName().equals(var)) {
                    return ((Constant) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getValue().toBigInteger();
                }

            }

        }

        return BigInteger.ONE;

    }

    /**
     * Hauptmethode zum Lösen von Polynomgleichungen.
     */
    public static ExpressionCollection solvePolynomialEquation(ExpressionCollection coefficients, String var) throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();

        // Vorarbeit: wenn die Koeffizienten alle Vielfache desselben Ausdrucks sind, so kann dieser Ausdruck gekürzt werden.
        try {
            Expression coefficientForTesting = null;
            for (int i = 0; i < coefficients.getBound(); i++) {
                if (!coefficients.get(i).equals(ZERO) && !coefficients.get(i).isIntegerConstantOrRationalConstant()) {
                    coefficientForTesting = coefficients.get(i);
                    break;
                }
            }

            if (coefficientForTesting != null) {
                boolean allQuotientsAreRational = true;
                ExpressionCollection reducedCoefficients = new ExpressionCollection();
                Expression quotient;
                for (int i = 0; i < coefficients.getBound(); i++) {
                    quotient = coefficients.get(i).div(coefficientForTesting).simplify();
                    if (!quotient.isIntegerConstantOrRationalConstant()) {
                        allQuotientsAreRational = false;
                        break;
                    } else {
                        reducedCoefficients.add(quotient);
                    }
                }
                if (allQuotientsAreRational) {
                    coefficients = reducedCoefficients;
                }
            }

        } catch (EvaluationException e) {
        }

        int degree = coefficients.getBound() - 1;

        /*
         Fall: f is ein Polynom in nichttrivialen Monomen. -> Substitution x^m
         = y und dann zuerst nach y auflösen und dann nach x.
         */
        int m = SimplifyPolynomialMethods.getGGTOfAllExponents(coefficients);
        if (m > 1 && degree / m <= ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL) {
            ExpressionCollection coefficientsOfSubstitutedPolynomial = substituteMonomialInPolynomial(coefficients, m, var);
            ExpressionCollection zerosOfSubstitutedPolynomial = solvePolynomialEquation(coefficientsOfSubstitutedPolynomial, var);
            if (m % 2 == 0) {
                for (Expression zero : zerosOfSubstitutedPolynomial) {
                    try {
                        zeros.add(zero.pow(1, m).simplify());
                        zeros.add(MINUS_ONE.mult(zero.pow(1, m)).simplify());
                    } catch (EvaluationException e) {
                    }
                }
            } else {
                for (Expression zero : zerosOfSubstitutedPolynomial) {
                    zeros.add(zero.pow(1, m).simplify());
                }
            }
        }
        if (!zeros.isEmpty()) {
            return zeros;
        }

        // Fall: f is ein Polynom mit zyklischen Koeffizienten.
        m = SimplifyPolynomialMethods.getPeriodOfCoefficients(coefficients);
        if (m < coefficients.getBound()) {
            return PolynomialAlgebraMethods.solvePeriodicPolynomialEquation(coefficients, var);
        }

        // (Nichttriviale) Polynome sollen nur dann exakt gelöst werden, wenn deg - ord <= gewisse Schranke ist.
        if (degree <= ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL) {

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

            // Versuchen, rationale Polynome zu faktorisieren.
            if (SimplifyPolynomialMethods.isPolynomialRational(coefficients)) {
                try {
                    Expression factorizedPolynomial = SimplifyPolynomialMethods.decomposeRationalPolynomialIntoSquarefreeFactors(coefficients, var);
                    // Fall: Faktorisiertes Polynom ist ein nichttriviales Produkt.
                    if (factorizedPolynomial.isProduct()
                            && SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) factorizedPolynomial).getLeft(), var).compareTo(BigInteger.ZERO) > 0
                            && SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) factorizedPolynomial).getRight(), var).compareTo(BigInteger.ZERO) > 0) {
                        return SimplifyUtilities.union(SolveGeneralEquationMethods.solveZeroEquation(((BinaryOperation) factorizedPolynomial).getLeft(), var),
                                SolveGeneralEquationMethods.solveZeroEquation(((BinaryOperation) factorizedPolynomial).getRight(), var));
                    }
                    // Fall: Faktorisiertes Polynom ist eine nichttriviale Potenz.
                    if (factorizedPolynomial.isIntegerPower()
                            && ((Constant) ((BinaryOperation) factorizedPolynomial).getRight()).getValue().toBigInteger().compareTo(BigInteger.ONE) > 0) {
                        return SolveGeneralEquationMethods.solveZeroEquation(((BinaryOperation) factorizedPolynomial).getLeft(), var);
                    }
                } catch (SimplifyPolynomialMethods.PolynomialNotDecomposableException | EvaluationException e) {
                    // Nichts tun.
                }
            }

            // Nullstellen von Polynomen vom Grad 1 - 4 können direkt ermittelt werden.
            if (degree == 1) {
                return SimplifyUtilities.union(zeros, PolynomialAlgebraMethods.solveLinearEquation(coefficients));
            }
            if (degree == 2) {
                return SimplifyUtilities.union(zeros, PolynomialAlgebraMethods.solveQuadraticEquation(coefficients));
            }
            if (degree == 3) {
                return SimplifyUtilities.union(zeros, PolynomialAlgebraMethods.solveCubicEquation(coefficients));
            }
            if (degree == 4) {
                return SimplifyUtilities.union(zeros, PolynomialAlgebraMethods.solveQuarticEquation(coefficients, var));
            }

        }

        return zeros;

    }

    /**
     * Hilfsmethode. Macht aus einem Polynom der Form a_{km}*x^{km} + ... +
     * a_m*x^m + a_0 (welches durch die Koeffizienten coefficients gegeben ist)
     * das Polynom a_{km}*y^k + ... + a_m*y + a_0. Ist schneller als diverse
     * Substitutionsmethoden.
     */
    private static ExpressionCollection substituteMonomialInPolynomial(ExpressionCollection coefficients, int m, String var) {
        ExpressionCollection resultCoefficient = new ExpressionCollection();
        for (int i = 0; i < coefficients.getBound(); i++) {
            if (i % m == 0) {
                resultCoefficient.put(resultCoefficient.getBound(), coefficients.get(i));
            }
        }
        return resultCoefficient;
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
    private static BigInteger[] findRationalZeroOfPolynomial(ExpressionCollection coefficients) {

        for (Expression coefficient : coefficients) {
            if (!coefficient.isIntegerConstant()) {
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
        ArrayList<BigInteger> pDivisors = new ArrayList<>();
        // Wichtig: pDivisors muss auch die 0 enthalten.
        pDivisors.add(BigInteger.ZERO);
        pDivisors.addAll(ArithmeticMethods.getDivisors(((Constant) coefficients.get(0)).getValue().toBigInteger()));
        ArrayList<BigInteger> qDivisors = ArithmeticMethods.getDivisors(((Constant) coefficients.get(coefficients.getBound() - 1)).getValue().toBigInteger());

        for (BigInteger pDivisor : pDivisors) {
            for (BigInteger qDivisor : qDivisors) {
                BigInteger pDivisorPower;
                BigInteger qDivisorPower;
                // Test, ob p/q eine Nullstelle des Polynoms ist.
                polynomValue = BigInteger.ZERO;
                for (int k = 0; k <= coefficients.getBound() - 1; k++) {
                    pDivisorPower = pDivisor.pow(k);
                    qDivisorPower = qDivisor.pow(coefficients.getBound() - 1 - k);
                    polynomValue = polynomValue.add(((Constant) coefficients.get(k)).getValue().toBigInteger().multiply(
                            pDivisorPower.multiply(qDivisorPower)));
                }
                if (polynomValue.compareTo(BigInteger.ZERO) == 0) {
                    BigInteger[] zero = new BigInteger[2];
                    zero[0] = pDivisor;
                    zero[1] = qDivisor;
                    return zero;
                }
                // Test, ob -p/q eine Nullstelle des Polynoms ist.
                polynomValue = BigInteger.ZERO;
                for (int k = 0; k <= coefficients.getBound() - 1; k++) {
                    pDivisorPower = pDivisor.negate().pow(k);
                    qDivisorPower = qDivisor.pow(coefficients.getBound() - 1 - k);
                    polynomValue = polynomValue.add(((Constant) coefficients.get(k)).getValue().toBigInteger().multiply(
                            pDivisorPower.multiply(qDivisorPower)));
                }
                if (polynomValue.compareTo(BigInteger.ZERO) == 0) {
                    BigInteger[] zero = new BigInteger[2];
                    zero[0] = pDivisor.negate();
                    zero[1] = qDivisor;
                    return zero;
                }
            }
        }

        return new BigInteger[0];

    }

    /**
     * Falls die Koeffizienten a.get(i) des Polynoms alle rational sind, so
     * liefert diese Methode alle rationalen Nullstellen des Polynoms mit
     * |Zähler|, |Nenner| <= eine gewisse Schranke.
     */
    public static ExpressionCollection getRationalZerosOfRationalPolynomial(Expression f, String var) {

        ExpressionCollection coefficients;
        try {
            coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(f, var);
        } catch (EvaluationException e) {
            return new ExpressionCollection();
        }

        ExpressionCollection zeros = new ExpressionCollection();
        try {
            findAllRationalZerosOfRationalPolynomial(coefficients, zeros);
            return zeros;
        } catch (EvaluationException e) {
            return new ExpressionCollection();
        }

    }

    /**
     * Falls die Koeffizienten a.get(i) des Polynoms alle rational sind, so
     * liefert diese Methode alle rationalen Nullstellen des Polynoms mit
     * |Zähler|, |Nenner| <= eine gewisse Schranke.
     */
    public static ExpressionCollection getRationalZerosOfRationalPolynomial(ExpressionCollection coefficients) {
        ExpressionCollection zeros = new ExpressionCollection();
        try {
            findAllRationalZerosOfRationalPolynomial(coefficients, zeros);
            return zeros;
        } catch (EvaluationException e) {
            return new ExpressionCollection();
        }
    }

    /**
     * Falls die Koeffizienten a.get(i) des Polynoms alle rational sind, so
     * liefert diese Methode alle rationalen Nullstellen des Polynoms mit
     * |Zähler|, |Nenner| <= eine gewisse Schranke. Diese werden der
     * ExpressionCollection rationalZeros hinzugefügt. Der Rückgabewert ist das
     * normierte Ergebnis der Polynomdivision durch alle ermittelten
     * Linearfaktoren. Sind die Koeffizienten a nicht allesamt rational, so wird
     * die ExpressionCollection a zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection findAllRationalZerosOfRationalPolynomial(ExpressionCollection coefficients, ExpressionCollection rationalZeros)
            throws EvaluationException {

        for (Expression coefficient : coefficients) {
            if (!coefficient.isIntegerConstantOrRationalConstant()) {
                return coefficients;
            }
        }

        /*
         Alle Polynomkoeffizienten werden mit dem kleinsten gemeinsamen Nenner
         multipliziert, damit alle Koeffizienten ganzzahlig werden.
         */
        BigInteger commonDenominator = BigInteger.ONE;
        for (Expression coefficient : coefficients) {
            if (coefficient.isQuotient()) {
                commonDenominator = ArithmeticMethods.lcm(new BigInteger[]{commonDenominator, ((Constant) ((BinaryOperation) coefficient).getRight()).getValue().toBigInteger()});
            }
        }

        ExpressionCollection coefficientsOfDivisionQuotient;
        ExpressionCollection multipleOfCoefficients = ExpressionCollection.copy(coefficients);

        multipleOfCoefficients.multiplyWithExpression(new Constant(commonDenominator));
        multipleOfCoefficients = multipleOfCoefficients.simplify();

        // Alle Polynomkoeffizienten werden nun durch ihren ggT dividiert.
        BigInteger gcd = BigInteger.ZERO;
        for (int i = 0; i < coefficients.getBound(); i++) {
            if (gcd.equals(BigInteger.ZERO)) {
                gcd = ((Constant) multipleOfCoefficients.get(i)).getValue().toBigInteger();
            } else {
                gcd = ArithmeticMethods.gcd(new BigInteger[]{gcd, ((Constant) multipleOfCoefficients.get(i)).getValue().toBigInteger()});
            }
        }
        if (gcd.compareTo(BigInteger.ONE) > 0) {
            multipleOfCoefficients.divideByExpression(new Constant(gcd));
            multipleOfCoefficients = multipleOfCoefficients.simplify();
        }

        coefficientsOfDivisionQuotient = ExpressionCollection.copy(multipleOfCoefficients);

        // Eigentliche Polynomdivision.
        BigInteger[] zero;
        ExpressionCollection divisor = new ExpressionCollection();
        // Es kann höchstens soviele (rationale) Nullstellen geben, wie der Grad des Polynoms (= a.getBound() - 1) beträgt.
        for (int i = 0; i < coefficients.getBound() - 1; i++) {
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
        coefficientsOfDivisionQuotient.divideByExpression(coefficientsOfDivisionQuotient.get(coefficientsOfDivisionQuotient.getBound() - 1));
        coefficientsOfDivisionQuotient = coefficientsOfDivisionQuotient.simplify();

        return coefficientsOfDivisionQuotient;

    }

    /**
     * Ermittelt die Lösungen von coefficients.get(1)*x + coefficients.get(0) =
     * 0.<br>
     * VORAUSSETZUNG: coefficients.get(1) != 0.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveLinearEquation(ExpressionCollection coefficients) throws EvaluationException {
        ExpressionCollection zeros = new ExpressionCollection();
        zeros.put(0, MINUS_ONE.mult(coefficients.get(0)).div(coefficients.get(1)).simplify());
        return zeros;
    }

    /**
     * Ermittelt die Lösungen von coefficients.get(2)*x^2 +
     * coefficients.get(1)*x + coefficients.get(0) = 0.<br>
     * VORAUSSETZUNG: coefficients.get(2) != 0.
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
            zeros.put(0, MINUS_ONE.mult(p).div(2).sub(discriminant.pow(1, 2)).simplify());
            zeros.put(1, MINUS_ONE.mult(p).div(2).add(discriminant.pow(1, 2)).simplify());
            return zeros;
        }

        if (discriminant.isAlwaysNonNegative() && !discriminant.equals(ZERO)) {
            // Nach p-q-Formel auflösen: x = (-p +- (p^2 - 4q)^(1/2))/2
            zeros.put(0, MINUS_ONE.mult(p).div(2).sub(discriminant.pow(1, 2)).simplify());
            zeros.put(1, MINUS_ONE.mult(p).div(2).add(discriminant.pow(1, 2)).simplify());
            return zeros;
        } else if (discriminant.equals(ZERO)) {
            zeros.put(0, p.div(-2).simplify());
            return zeros;
        }

        // Es konnten keine Lösungen ermittelt werden.
        return zeros;

    }

    /**
     * Ermittelt die Lösungen von coefficients.get(3)*x^3 +
     * coefficients.get(2)*x^2 + coefficients.get(1)*x + coefficients.get(0) =
     * 0.<br>
     * VORAUSSETZUNG: coefficients.get(3) != 0.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveCubicEquation(ExpressionCollection coefficients) throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();
        Expression a = coefficients.get(2).div(coefficients.get(3)).simplify();
        Expression b = coefficients.get(1).div(coefficients.get(3)).simplify();
        Expression c = coefficients.get(0).div(coefficients.get(3)).simplify();

        // Gelöst wird nun die Gleichung x^3 + ax^2 + bx + c = 0
        /*
         Substitution: x = z - a/3 (später muss zurücksubstituiert werden): p =
         b - a^2/3, q = 2a^3/27 - ab/3 + c. Gelöst wird nun die Gleichung z^3
         + pz + q = 0
         */
        Expression p = b.sub(a.pow(2).div(3)).simplify();
        Expression q = TWO.mult(a.pow(3).div(27)).sub(a.mult(b).div(3)).add(c).simplify();

        // Diskriminante discriminant = (p/3)^3 + (q/2)^2 = p^3/27 + q^2/4.
        Expression discriminant = p.pow(3).div(27).add(q.pow(2).div(4)).simplify();

        if (!discriminant.isAlwaysNonNegative() && !discriminant.isAlwaysNonPositive()) {
            /* 
             Dann ist die Diskriminante nichtkonstant und hat kein eindeutiges Vorzeichen. 
             In diesem Fall drei abstrakte Lösungen (gemäß Casus irreduzibilis) zurückgeben.
             */
            Expression arg = MINUS_ONE.mult(q.div(2)).mult(new Constant(-27).div(p.pow(3)).pow(1, 2)).arccos().div(3).simplify();
            Expression factor = (new Constant(-4).mult(p).div(3)).pow(1, 2).simplify();
            zeros.put(0, factor.mult(arg.cos()).sub(a.div(3)).simplify());
            zeros.put(1, MINUS_ONE.mult(factor).mult(arg.add(PI.div(3)).cos()).sub(a.div(3)).simplify());
            zeros.put(2, MINUS_ONE.mult(factor).mult(arg.sub(PI.div(3)).cos()).sub(a.div(3)).simplify());
            return zeros;
        }

        if (discriminant.isAlwaysNegative()) {
            // Casus irreduzibilis.
            Expression arg = MINUS_ONE.mult(q.div(2)).mult(new Constant(-27).div(p.pow(3)).pow(1, 2)).arccos().div(3).simplify();
            Expression factor = (new Constant(-4).mult(p).div(3)).pow(1, 2);
            zeros.put(0, factor.mult(new Function(arg, TypeFunction.cos)).sub(a.div(3)).simplify());
            zeros.put(1, MINUS_ONE.mult(factor).mult(arg.add(PI.div(3)).cos()).sub(a.div(3)).simplify());
            zeros.put(2, MINUS_ONE.mult(factor).mult(arg.sub(PI.div(3)).cos()).sub(a.div(3)).simplify());
            return zeros;
        } else if (discriminant.equals(ZERO)) {
            // Auflösung nach Cardano-Formel: 
            // Doppellösung z = (q/2)^(1/3)
            zeros.put(0, (q.div(2).pow(1, 3)).sub(a.div(3)).simplify());
            // Einfache Lösung z = (-4q)^(1/3)
            zeros.put(1, new Constant(-4).mult(q).pow(1, 3).sub(a.div(3)).simplify());
            return zeros;
        }

        /*
         In diesem Fall ist discriminant positiv. Auflösung nach
         Cardano-Formel: z = (-q/2 + discriminant^(1/2))^(1/3) + (-q/2 -
         discriminant^(1/2))^(1/3).
         */
        Expression u = (discriminant.pow(1, 2).sub(q.div(2)).simplify()).pow(1, 3);
        Expression v = MINUS_ONE.mult(discriminant.pow(1, 2)).sub(q.div(2)).simplify().pow(1, 3);
        zeros.put(0, u.add(v).sub(a.div(3)).simplify());
        return zeros;

    }

    /**
     * Ermittelt die Lösungen von coefficients.get(4)*x^4 +
     * coefficients.get(3)*x^3 + coefficients.get(2)*x^2 + coefficients.get(1)*x
     * + coefficients.get(0) = 0.<br>
     * VORAUSSETZUNG: coefficients.get(4) != 0.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveQuarticEquation(ExpressionCollection coefficients, String var) throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();
        Expression a = coefficients.get(3).div(coefficients.get(4)).simplify();
        Expression b = coefficients.get(2).div(coefficients.get(4)).simplify();
        Expression c = coefficients.get(1).div(coefficients.get(4)).simplify();
        Expression d = coefficients.get(0).div(coefficients.get(4)).simplify();

        // Gelöst wird nun die Gleichung x^4 + ax^3 + bx^2 + cx + d = 0
        /*
         Substitution: x = y - a/4 (später muss zurücksubstituiert werden): p =
         b - 3a^2/8, q = a^3/8 + C - ab/2, r = a^2b/16 + d - (ac/4 + 3a^4/256). Gelöst wird nun die Gleichung z^3
         + pz + q = 0. Gelöst wird nun die Gleichung y^4 + py^2 + qy + r = 0.
         */
        Expression p = b.sub(THREE.mult(a.pow(2)).div(8)).simplify();
        Expression q = a.pow(3).div(8).add(c).sub(a.mult(b).div(2)).simplify();
        Expression r = a.pow(2).mult(b).div(16).add(d).sub(a.mult(c).div(4).add(THREE.mult(a.pow(4)).div(256))).simplify();

        // Koeffizienten für die Gleichung der Resultante 8z^3 + 20pz^2 + (16p^2-8r)z + (4p^3-4pr-q^2) = 0 werden gebildet:
        ExpressionCollection coefficientsResultant = new ExpressionCollection();
        try {
            coefficientsResultant.add(FOUR.mult(p.pow(3)).sub(FOUR.mult(p).mult(r).add(q.pow(2))).simplify());
            coefficientsResultant.add(new Constant(16).mult(p.pow(2)).sub(new Constant(8).mult(r)).simplify());
            coefficientsResultant.add(new Constant(20).mult(p).simplify());
            coefficientsResultant.add(new Constant(8));
        } catch (EvaluationException e) {
            return zeros;
        }

        ExpressionCollection zerosResultant = solvePolynomialEquation(coefficientsResultant, var);

        if (zerosResultant.isEmpty()) {
            // Sollte eigentlich NIE vorkommen. Trotzdem sicherheitshalber.
            return zeros;
        }

        /* 
         Jetzt wird eine Nullstelle z der Resultante gesucht, so dass p + 2z > 0 gilt.
         Falls dies trotzdem nicht der Fall sein sollte, einfach leere Menge als Lösung zurückgeben.
         */
        Expression specialZeroResultant = null;
        double difference;
        for (Expression zeroResultant : zerosResultant) {
            try {
                difference = p.add(TWO.mult(zeroResultant)).evaluate();
                if (difference > 0) {
                    specialZeroResultant = zeroResultant;
                    break;
                }
            } catch (EvaluationException e) {
            }
        }

        if (specialZeroResultant == null) {
            return zeros;
        }

        /* 
         Zwei quadratische Gleichungen werden nun gelöst:
         (1) y^2 - (p+2z)^(1/2)y + q/(2(p+2z)^(1/2)) + p + z = 0.
         (2) y^2 + (p+2z)^(1/2)y - q/(2(p+2z)^(1/2)) + p + z = 0.
         */
        ExpressionCollection coefficientsQuadraticEquationOne = new ExpressionCollection();
        ExpressionCollection coefficientsQuadraticEquationTwo = new ExpressionCollection();
        Expression sum = p.add(TWO.mult(specialZeroResultant));
        coefficientsQuadraticEquationOne.add(q.div(TWO.mult(sum.pow(1, 2))).add(p).add(specialZeroResultant));
        coefficientsQuadraticEquationOne.add(MINUS_ONE.mult(sum.pow(1, 2)));
        coefficientsQuadraticEquationOne.add(ONE);
        coefficientsQuadraticEquationTwo.add(p.add(specialZeroResultant).sub(q.div(TWO.mult(sum.pow(1, 2)))));
        coefficientsQuadraticEquationTwo.add(sum.pow(1, 2));
        coefficientsQuadraticEquationTwo.add(ONE);

        ExpressionCollection zerosForSubst = SimplifyUtilities.union(solveQuadraticEquation(coefficientsQuadraticEquationOne),
                solveQuadraticEquation(coefficientsQuadraticEquationTwo));
        // Mehrfachlösungen werden beseitigt.
        zerosForSubst.removeMultipleTerms();

        // Rücksubstitution und Zurückgeben der Nullstellen.
        for (Expression zeroForSubst : zerosForSubst) {
            try {
                zeros.add(zeroForSubst.sub(a.div(4)).simplify());
            } catch (EvaluationException e) {
                // Nullstellen mit diversen Fehlern werden ignoriert.
            }
        }
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
            specialZero.put(0, MINUS_ONE);
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

        if (!isPolynomialInRationalPowers(f, var)) {
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
            throws EvaluationException, NotAlgebraicallySolvableException {

        if (!SimplifyPolynomialMethods.isPolynomial(f, var) && PolynomialAlgebraMethods.isPolynomialInRationalPowers(f, var)) {
            throw new NotAlgebraicallySolvableException();
        }

        BigInteger m = findExponentForPolynomialSubstitutionByRoots(f, var);
        if (m.compareTo(BigInteger.ONE) > 0) {

            ExpressionCollection zeros = new ExpressionCollection();
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

        throw new NotAlgebraicallySolvableException();

    }

}

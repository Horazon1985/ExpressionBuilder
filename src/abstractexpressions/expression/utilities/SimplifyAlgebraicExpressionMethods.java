package abstractexpressions.expression.utilities;

import abstractexpressions.expression.computation.ArithmeticMethods;
import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.THREE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.TypeBinary;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.equation.PolynomialAlgebraMethods;
import exceptions.MathToolException;
import java.math.BigInteger;
import java.util.ArrayList;

public abstract class SimplifyAlgebraicExpressionMethods {

    /**
     * Private Fehlerklasse für den Fall, dass ein Ausdruck keine rationale
     * Quadratwurzel besitzt.
     */
    private static class RootNotRationalException extends MathToolException {

        private static final String ROOT_NOT_RATIONAL = "Root is not of degree 2 over the rationals.";

        public RootNotRationalException() {
            super(ROOT_NOT_RATIONAL);
        }

    }

    /**
     * Gibt zurück, ob f eine rationale Funktion in x^(1/n), x = var, für ein
     * geeignetes n ist.
     */
    public static boolean isRationalFunctionInRationalPowerOfVar(Expression f, String var) {
        if (!f.contains(var)) {
            return true;
        }
        if (f.equals(Variable.create(var))) {
            return true;
        }
        if (f instanceof BinaryOperation) {
            if (f.isNotPower()) {
                return isRationalFunctionInRationalPowerOfVar(((BinaryOperation) f).getLeft(), var) && isRationalFunctionInRationalPowerOfVar(((BinaryOperation) f).getRight(), var);
            } else if (f.isPower() && ((BinaryOperation) f).getRight().isRationalPower() && ((BinaryOperation) f).getLeft().equals(Variable.create(var))) {
                return true;
            }
        }
        /*
         Falls f eine Instanz von Operator oder SelfDefinedFunction ist, in
         welchem var vorkommt, dann false zurückgeben.
         */
        return false;
    }

    /**
     * Gibt zurück, ob expr eine ganze Zahl oder ein Bruch mit ungeradem
     * Exponenten ist. Wird in vielen Methoden benötigt, wo man Wurzeln ziehen
     * oder Potenzieren muss, ohne Rücksicht auf das Vorzeichen der Basis nehmen
     * zu müssen.
     */
    public static boolean isAdmissibleExponent(Expression expr) {
        return expr.isIntegerConstant() || (expr.isRationalConstant() && ((BinaryOperation) expr).getRight().isOddIntegerConstant());
    }

    /**
     * Vereinfacht beispielsweise (8)^(1/2) = 2*2^(1/2), (108)^(2/3) = 9*4^(2/3)
     * etc. Faktorisiert werden nur Faktoren &#8804; eine gewisse Schranke (da
     * hier SolveMethods.getDivisors() benötigt wird).
     */
    public static Expression factorizeIntegerFactorsFromIntegerRoots(BinaryOperation expr) {

        if (expr.isNotPower() || !expr.getLeft().isIntegerConstant()
                || !(expr.getRight().isRationalConstant()
                && ((BinaryOperation) expr.getRight()).getLeft().isIntegerConstant()
                && ((BinaryOperation) expr.getRight()).getRight().isIntegerConstant())) {
            return expr;
        }

        BigInteger m = ((Constant) ((BinaryOperation) expr.getRight()).getLeft()).getBigIntValue();
        BigInteger n = ((Constant) ((BinaryOperation) expr.getRight()).getRight()).getBigIntValue();
        if (m.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 || n.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            return expr;
        }

        BigInteger a = ((Constant) expr.getLeft()).getBigIntValue();

        if (a.compareTo(BigInteger.ZERO) <= 0 || m.intValue() < 0 || n.intValue() < 0) {
            return expr;
        }

        int p = m.intValue();
        int q = n.intValue();

        BigInteger divisor;
        BigInteger factorOutside = BigInteger.ONE;
        int sqrtOfBoundOfDivisorsOfIntegers = (int) (Math.sqrt(ComputationBounds.BOUND_ARITHMETIC_DIVISORS_OF_INTEGERS) + 1);

        if (a.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_DIVISORS_OF_INTEGERS)) <= 0) {

            ArrayList<BigInteger> divisorsOfA = ArithmeticMethods.getDivisors(a);

            for (int i = 0; i < divisorsOfA.size(); i++) {

                divisor = divisorsOfA.get(i);
                if (divisor.compareTo(BigInteger.ONE) > 0 && a.mod(divisor.pow(q)).compareTo(BigInteger.ZERO) == 0) {
                    factorOutside = factorOutside.multiply(divisor.pow(p));
                    a = a.divide(divisor.pow(q));
                    // Derselbe Teiler darf noch einmal probiert werden!
                    i--;
                }
                if (a.compareTo(BigInteger.ONE) == 0) {
                    break;
                }

            }

        } else {

            // Dann nur alle Teiler <= 1000 ausprobieren
            for (int i = 1; i <= sqrtOfBoundOfDivisorsOfIntegers; i++) {

                divisor = BigInteger.valueOf(i);
                if (divisor.compareTo(BigInteger.ONE) > 0 && a.mod(divisor.pow(q)).compareTo(BigInteger.ZERO) == 0) {
                    factorOutside = factorOutside.multiply(divisor.pow(p));
                    a = a.divide(divisor.pow(q));
                    // Derselbe Teiler darf noch einmal probiert werden!
                    i--;
                }
                if (a.compareTo(BigInteger.ONE) == 0) {
                    break;
                }

            }

        }

        if (factorOutside.compareTo(BigInteger.ONE) == 0) {
            return expr;
        }

        Expression result = new Constant(factorOutside);
        if (a.compareTo(BigInteger.ONE) > 0) {
            result = result.mult(new Constant(a).pow(expr.getRight()));
        }
        return result;

    }

    /**
     * Dasselbe wie factorizeIntegerFactorsFromIntegerRoots(), nur für rationale
     * Basen statt ganze.
     */
    public static Expression factorizeRationalFactorsFromRationalRoots(BinaryOperation expr) {

        // Alles außer Ausdrücke der Form (a/b)^(p/q) wird aussortiert.
        if (expr.isNotPower() || !expr.getLeft().isRationalConstant()
                || !(((BinaryOperation) expr.getLeft()).getLeft().isIntegerConstant()
                && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant())
                || !(expr.getRight().isRationalConstant()
                && ((BinaryOperation) expr.getRight()).getLeft().isIntegerConstant()
                && ((BinaryOperation) expr.getRight()).getRight().isIntegerConstant())) {
            return expr;
        }

        BigInteger a = ((Constant) ((BinaryOperation) expr.getLeft()).getLeft()).getBigIntValue();
        BigInteger b = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getBigIntValue();
        BigInteger m = ((Constant) ((BinaryOperation) expr.getRight()).getLeft()).getBigIntValue();
        BigInteger n = ((Constant) ((BinaryOperation) expr.getRight()).getRight()).getBigIntValue();
        if (m.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 || n.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            return expr;
        }

        int p = m.intValue();
        int q = n.intValue();

        BigInteger divisor;
        BigInteger factorOutsideInNumerator = BigInteger.ONE;
        BigInteger factorOutsideInDenominator = BigInteger.ONE;

        // Im Zähler faktorisieren.
        ArrayList<BigInteger> setOfDivisors;
        int sqrtOfBoundOfDivisorsOfIntegers = (int) (Math.sqrt(ComputationBounds.BOUND_ARITHMETIC_DIVISORS_OF_INTEGERS) + 1);

        if (a.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_DIVISORS_OF_INTEGERS)) <= 0) {

            setOfDivisors = ArithmeticMethods.getDivisors(a);

            for (int i = 0; i < setOfDivisors.size(); i++) {

                divisor = setOfDivisors.get(i);
                if (divisor.compareTo(BigInteger.ONE) > 0 && a.mod(divisor.pow(q)).compareTo(BigInteger.ZERO) == 0) {
                    factorOutsideInNumerator = factorOutsideInNumerator.multiply(divisor.pow(p));
                    a = a.divide(divisor.pow(q));
                    // Derselbe Teiler darf noch einmal probiert werden!
                    i--;
                }
                if (a.compareTo(BigInteger.ONE) == 0) {
                    break;
                }

            }

        } else {

            // Dann nur alle Teiler <= 1000 ausprobieren
            for (int i = 1; i <= sqrtOfBoundOfDivisorsOfIntegers; i++) {

                divisor = BigInteger.valueOf(i);
                if (divisor.compareTo(BigInteger.ONE) > 0 && a.mod(divisor.pow(q)).compareTo(BigInteger.ZERO) == 0) {
                    factorOutsideInNumerator = factorOutsideInNumerator.multiply(divisor.pow(p));
                    a = a.divide(divisor.pow(q));
                    // Derselbe Teiler darf noch einmal probiert werden!
                    i--;
                }
                if (a.compareTo(BigInteger.ONE) == 0) {
                    break;
                }

            }

        }

        // Im Nenner faktorisieren.
        if (b.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_DIVISORS_OF_INTEGERS)) <= 0) {

            setOfDivisors = ArithmeticMethods.getDivisors(b);

            for (int i = 0; i < setOfDivisors.size(); i++) {

                divisor = setOfDivisors.get(i);
                if (divisor.compareTo(BigInteger.ONE) > 0 && b.mod(divisor.pow(q)).compareTo(BigInteger.ZERO) == 0) {
                    factorOutsideInDenominator = factorOutsideInDenominator.multiply(divisor.pow(p));
                    b = b.divide(divisor.pow(q));
                    // Derselbe Teiler darf noch einmal probiert werden!
                    i--;
                }
                if (b.compareTo(BigInteger.ONE) == 0) {
                    break;
                }

            }

        } else {

            // Dann nur alle Teiler <= 1000 ausprobieren
            for (int i = 1; i <= sqrtOfBoundOfDivisorsOfIntegers; i++) {

                divisor = BigInteger.valueOf(i);
                if (divisor.compareTo(BigInteger.ONE) > 0 && b.mod(divisor.pow(q)).compareTo(BigInteger.ZERO) == 0) {
                    factorOutsideInDenominator = factorOutsideInDenominator.multiply(divisor.pow(p));
                    b = b.divide(divisor.pow(q));
                    // Derselbe Teiler darf noch einmal probiert werden!
                    i--;
                }
                if (b.compareTo(BigInteger.ONE) == 0) {
                    break;
                }

            }

        }

        if (factorOutsideInNumerator.compareTo(BigInteger.ONE) == 0 && factorOutsideInDenominator.compareTo(BigInteger.ONE) == 0) {
            return expr;
        } else if (factorOutsideInNumerator.compareTo(BigInteger.ONE) > 0 && factorOutsideInDenominator.compareTo(BigInteger.ONE) == 0) {
            return new Constant(factorOutsideInNumerator).mult((new Constant(a).div(b)).pow(expr.getRight()));
        } else if (factorOutsideInNumerator.compareTo(BigInteger.ONE) == 0 && factorOutsideInDenominator.compareTo(BigInteger.ONE) > 0) {
            return new Constant(a).div(b).pow(expr.getRight()).div(factorOutsideInDenominator);
        } else {
            return new Constant(factorOutsideInNumerator).mult((new Constant(a).div(b)).pow(expr.getRight())).div(factorOutsideInDenominator);
        }

    }

    /**
     * Hilfsmethode: "Counter" für Exponententupel für (a_1 + ... + a_n)^k
     */
    private static int[] counterBinomialExponents(int[] counter, int k) {

        // Sicherheitsabfrage (sollte nicht eintreten, denn sonst hätte man nur einen Summanden).
        if (counter.length == 1) {
            return counter;
        }

        int[] result = new int[counter.length];

        boolean countingEnds = counter[counter.length - 1] == k;
        for (int i = 0; i < counter.length - 1; i++) {
            countingEnds = countingEnds && counter[i] == 0;
            if (!countingEnds) {
                break;
            }
        }

        // Dann ist das nächste Tupe (0, ..., 0).
        if (countingEnds) {
            return result;
        }

        // Den nächsten Eintrag ermitteln.
        int indexOfNextDigitToBeIncreased;
        int sumOfAllOtherDigits;
        if (counter[0] > 0) {
            result[0] = counter[0] - 1;
            result[1] = counter[1] + 1;
            System.arraycopy(counter, 2, result, 2, counter.length - 2);
        } else {

            indexOfNextDigitToBeIncreased = 1;
            while (counter[indexOfNextDigitToBeIncreased] == 0 && indexOfNextDigitToBeIncreased < counter.length) {
                indexOfNextDigitToBeIncreased++;
            }

            System.arraycopy(counter, indexOfNextDigitToBeIncreased + 2, result, indexOfNextDigitToBeIncreased + 2,
                    counter.length - (indexOfNextDigitToBeIncreased + 2));
            result[indexOfNextDigitToBeIncreased + 1] = counter[indexOfNextDigitToBeIncreased + 1] + 1;

            for (int j = 1; j < indexOfNextDigitToBeIncreased + 1; j++) {
                result[j] = 0;
            }

            sumOfAllOtherDigits = 0;
            for (int j = 1; j < counter.length; j++) {
                sumOfAllOtherDigits += result[j];
            }
            result[0] = k - sumOfAllOtherDigits;

        }

        return result;

    }

    /**
     * Gibt die Binomialentwicklung von (a_1 + ... + a_n)^exponent zurück.
     */
    public static Expression binomialExpansion(BinaryOperation expr, int exponent) {

        if (!expr.getType().equals(TypeBinary.PLUS) && !expr.getType().equals(TypeBinary.MINUS)) {
            return expr;
        }

        Expression result = Expression.ZERO;

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr);

        int n = summandsLeft.getBound() + summandsRight.getBound();
        int n_1 = summandsLeft.getBound();

        int[] exponents = new int[n];
        /*
         Starttupel initialisieren und dann den Counter verwenden, um die
         nächsten tupel zu erzeugen.
         */
        exponents[0] = exponent;
        for (int i = 1; i < n; i++) {
            exponents[i] = 0;
        }

        /*
         Anzahl der Summanden in (a_1 + ... + a_n)^k mit k = exponent ist (n -
         1 + k)!/[(n - 1)! * k!]
         */
        BigInteger numberOfSummandsInResult = ArithmeticMethods.factorial(n - 1 + exponent).divide(ArithmeticMethods.factorial(n - 1).multiply(ArithmeticMethods.factorial(exponent)));
        if (numberOfSummandsInResult.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            // Wenn es zu viele Summanden werden -> nicht entwickeln.
            return expr;
        }

        Expression summand;
        BigInteger binomialCoefficient;
        for (int i = 0; i < numberOfSummandsInResult.intValue(); i++) {

            // Aktuellen Binomialkoeffizienten berechnen.
            binomialCoefficient = ArithmeticMethods.factorial(exponent);
            for (int j = 0; j < n; j++) {
                binomialCoefficient = binomialCoefficient.divide(ArithmeticMethods.factorial(exponents[j]));
            }

            summand = new Constant(binomialCoefficient);
            if (exponents[0] > 0) {
                summand = summand.mult(summandsLeft.get(0).pow(exponents[0]));
            }

            for (int j = 1; j < n_1; j++) {
                if (exponents[j] > 0) {
                    summand = summand.mult(summandsLeft.get(j).pow(exponents[j]));
                }
            }
            for (int j = n_1; j < n; j++) {
                if (exponents[j] > 0) {
                    summand = summand.mult(summandsRight.get(j - n_1).pow(exponents[j]));
                }
            }

            // Jetzt wird über das Vorzeichen des Summanden entschieden.
            int signum = 0;
            for (int j = n_1; j < n; j++) {
                signum = signum + exponents[j];
            }

            if ((signum / 2) * 2 == signum) {
                // Dann ist signum gerade, also ist das Vorzeichen +.
                if (i == 0) {
                    result = summand;
                } else {
                    result = result.add(summand);
                }
            } else {
                result = result.sub(summand);
            }

            // Counter für die Exponenten um 1 erhöhen.
            exponents = counterBinomialExponents(exponents, exponent);

        }

        return result;

    }

    /**
     * Hier wird entschieden, ob es sich lohnt, einen Ausdruck der Form (a +-
     * b)^n nach dem Binomischen Lehrsatz zu entwickeln (z.B. ja, falls (1 +
     * 2^(1/2))^10, nein, falls (3^(1/7) + sin(1))^5 etc.). maxExponent ist
     * dabei der maximal erlaubte Exponent, bei welchem noch der bin. Lehrsatz
     * angewendet wird.
     */
    public static Expression expandAlgebraicExpressionsByBinomial(BinaryOperation expr, int maxExponent) {

        if (!expr.isConstant() || !expr.getType().equals(TypeBinary.POW) || !expr.getRight().isIntegerConstant()) {
            return expr;
        }

        BigInteger n = ((Constant) expr.getRight()).getBigIntValue();
        if (n.compareTo(BigInteger.ZERO) < 0 || n.compareTo(BigInteger.valueOf(maxExponent)) > 0) {
            return expr;
        }

        if (!expr.getLeft().isSum() && !expr.getLeft().isDifference()) {
            return expr;
        }

        Expression summandLeft = ((BinaryOperation) expr.getLeft()).getLeft();
        Expression summandRight = ((BinaryOperation) expr.getLeft()).getRight();

        boolean leftSummandSuitable = false, rightSummandSuitable = false;
        BigInteger leftExponentDenominator = BigInteger.ZERO, rightExponentDenominator = BigInteger.ZERO;

        // Form des linken Summanden untersuchen.
        if (summandLeft.isIntegerConstantOrRationalConstant()) {
            // Falls summandLeft = a oder = a/b
            leftSummandSuitable = true;
        } else if (summandLeft.isPower()
                && ((BinaryOperation) summandLeft).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) summandLeft).getRight().isRationalConstant()) {
            // Falls summandLeft = a^(k/m) oder = (a/b)^(k/m)
            leftExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight()).getBigIntValue();
            leftSummandSuitable = true;
        } else if (summandLeft.isProduct()
                && ((BinaryOperation) summandLeft).getLeft().isIntegerConstant()
                && ((BinaryOperation) summandLeft).getRight().isPower()
                && ((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight().isRationalConstant()) {
            // Falls summandLeft = a*b^(k/m) oder = a*(b/c)^(k/m)
            leftExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight()).getRight()).getBigIntValue();
            leftSummandSuitable = true;
        } else if (summandLeft.isQuotient()
                && ((BinaryOperation) summandLeft).getRight().isIntegerConstant()
                && ((BinaryOperation) summandLeft).getLeft().isPower()
                && ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getRight().isRationalConstant()) {
            // Falls summandLeft = a^(k/m)/b oder = (a/b)^(k/m)/c
            leftExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getRight()).getRight()).getBigIntValue();
            leftSummandSuitable = true;
        } else if (summandLeft.isQuotient()
                && ((BinaryOperation) summandLeft).getRight().isIntegerConstant()
                && ((BinaryOperation) summandLeft).getLeft().isProduct()
                && ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getLeft().isIntegerConstant()
                && ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getRight().isPower()
                && ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getRight()).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getRight()).getRight().isRationalConstant()) {
            // Falls summandLeft = a*c^(k/m)/b oder = a*(b/c)^(k/m)/d
            leftExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getRight()).getRight()).getRight()).getBigIntValue();
            leftSummandSuitable = true;
        } else if (summandLeft.isQuotient()
                && ((BinaryOperation) summandLeft).getLeft().isIntegerConstant()
                && ((BinaryOperation) summandLeft).getRight().isPower()
                && (((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getLeft()).isIntegerConstantOrRationalConstant()
                && (((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight()).isRationalConstant()) {
            // Falls summandLeft = a/b^(k/m) oder = a/(b/c)^(k/m)
            leftExponentDenominator = ((Constant) (((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight()).getRight())).getBigIntValue();
            leftSummandSuitable = true;
        } else if (summandLeft.isQuotient()
                && ((BinaryOperation) summandLeft).getLeft().isIntegerConstant()
                && ((BinaryOperation) summandLeft).getRight().isProduct()
                && (((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getLeft()).isIntegerConstant()
                && (((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight()).isPower()
                && ((BinaryOperation) (((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight())).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) (((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight())).getRight().isRationalConstant()) {
            // Falls summandLeft = a/(b*c^(k/m)) oder = a/(b*(c/d)^(k/m))
            leftExponentDenominator = ((Constant) ((BinaryOperation) (((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight()).getRight())).getRight()).getBigIntValue();
            leftSummandSuitable = true;
        }

        // Form des rechten Summanden untersuchen.
        if (summandRight.isIntegerConstantOrRationalConstant()) {
            // Falls summandRight = a oder = a/b
            rightSummandSuitable = true;
        } else if (summandRight.isPower()
                && ((BinaryOperation) summandRight).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) summandRight).getRight().isRationalConstant()) {
            // Falls summandRight = a^(k/m) oder = (a/b)^(k/m)
            rightExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight()).getBigIntValue();
            rightSummandSuitable = true;
        } else if (summandRight.isProduct()
                && ((BinaryOperation) summandRight).getLeft().isIntegerConstant()
                && ((BinaryOperation) summandRight).getRight().isPower()
                && ((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight().isRationalConstant()) {
            // Falls summandRight = a*b^(k/m) oder a*(b/c)^(k/m)
            rightExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight()).getRight()).getBigIntValue();
            rightSummandSuitable = true;
        } else if (summandRight.isQuotient()
                && ((BinaryOperation) summandRight).getRight().isIntegerConstant()
                && ((BinaryOperation) summandRight).getLeft().isPower()
                && ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight().isRationalConstant()) {
            // Falls summandRight = a^(k/m)/b oder = (a/b)^(k/m)/c
            rightExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight()).getRight()).getBigIntValue();
            rightSummandSuitable = true;
        } else if (summandRight.isQuotient()
                && ((BinaryOperation) summandRight).getRight().isIntegerConstant()
                && ((BinaryOperation) summandRight).getLeft().isProduct()
                && ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getLeft().isIntegerConstant()
                && ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight().isPower()
                && ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight()).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight()).getRight().isRationalConstant()) {
            // Falls summandRight = a*b^(k/m)/c oder = a*(b/c)^(k/m)/d
            rightExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight()).getRight()).getRight()).getBigIntValue();
            rightSummandSuitable = true;
        } else if (summandRight.isQuotient()
                && ((BinaryOperation) summandRight).getLeft().isIntegerConstant()
                && ((BinaryOperation) summandRight).getRight().isPower()
                && (((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getLeft()).isIntegerConstantOrRationalConstant()
                && (((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight()).isRationalConstant()) {
            // Falls summandRight = a/b^(k/m) oder = a/(b/c)^(k/m)
            rightExponentDenominator = ((Constant) (((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight()).getRight())).getBigIntValue();
            rightSummandSuitable = true;
        } else if (summandRight.isQuotient()
                && ((BinaryOperation) summandRight).getLeft().isIntegerConstant()
                && ((BinaryOperation) summandRight).getRight().isProduct()
                && (((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getLeft()).isIntegerConstant()
                && (((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight()).isPower()
                && ((BinaryOperation) (((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight())).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) (((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight())).getRight().isRationalConstant()) {
            // Falls summandRight = a/(b*c^(k/m)) oder = a/(b*(c/d)^(k/m))
            rightExponentDenominator = ((Constant) ((BinaryOperation) (((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight()).getRight())).getRight()).getBigIntValue();
            rightSummandSuitable = true;
        }

        if (!leftSummandSuitable || !rightSummandSuitable) {
            return expr;
        }

        /*
         Anwendung der binomischen Formel wird nur dann für sinnvoll erachtet,
         wenn leftExponentDenominator == rightExponentDenominator und
         leftExponentDenominator == rightExponentDenominator <= max(5, n) und
         n <= 100 ist.
         */
        if (leftExponentDenominator.compareTo(BigInteger.ZERO) > 0 && rightExponentDenominator.compareTo(BigInteger.ZERO) > 0
                && leftExponentDenominator.compareTo(rightExponentDenominator) != 0 || leftExponentDenominator.compareTo(n) > 0
                || leftExponentDenominator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_ROOTDEGREE_OF_SUMMAND_WITHIN_BINOMIAL)) > 0
                || rightExponentDenominator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_ROOTDEGREE_OF_SUMMAND_WITHIN_BINOMIAL)) > 0
                || n.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_BINOMIAL)) > 0) {
            return expr;
        }

        return binomialExpansion((BinaryOperation) expr.getLeft(), n.intValue());

    }

    /**
     * Hilfsfunktion: Liefert, ob expr einen Quadratwurzelfaktor enthält.
     */
    private static boolean containsSqrtFactor(Expression expr) {

        ExpressionCollection factors = SimplifyUtilities.getFactors(expr);
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i).isPower()
                    && ((BinaryOperation) factors.get(i)).getRight().isRationalConstant()
                    && ((BinaryOperation) ((BinaryOperation) factors.get(i)).getRight()).getRight().equals(Expression.TWO)) {
                return true;
            }
        }
        return false;

    }

    /**
     * Hilfsfunktion: Liefert, ob expr ein passender Kandidat für die Anwendung
     * der 3. binomischen Formel ist. Beispielsweise ist a + 5*b^(3/2) ein
     * passender Kandidat (wenn später noch etwa a - 5*b^(3/2) oder 5*b^(3/2) -
     * a als Faktor gefunden wird), nicht aber einfach nur a + b oder a +
     * b^(1/3) etc. Präzises Kriterium: expr muss eine Summe oder eine Differenz
     * sein, und in mindestens einer der beiden Summanden muss ein
     * Quadratwurzelfaktor enthalten sein.
     */
    private static boolean isSuitableCandidateForThirdBinomial(BinaryOperation expr) {
        return (expr.isSum() || expr.isDifference())
                && (containsSqrtFactor(expr.getLeft()) || containsSqrtFactor(expr.getRight()));
    }

    /**
     * Hilfsfunktion: Liefert, ob expr von der Form b^(1/2) oder a*b^(1/2) mit
     * ganzem a ist. Für die Rationalisierung des Nenners müssen im Wesentlichen
     * nur solche Terme betrachtet werden, da allgemeinere Terme während des
     * simplify() auf diese Form gebracht werden.
     */
    public static boolean isProductOfIntegerAndSqrtOfExpression(Expression expr) {
        if (expr.isPower()
                && ((BinaryOperation) expr).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) expr).getRight().isRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) expr).getRight()).getLeft().isIntegerConstant()
                && ((BinaryOperation) ((BinaryOperation) expr).getRight()).getRight().equals(Expression.TWO)) {
            return true;
        }
        return expr.isProduct()
                && ((BinaryOperation) expr).getLeft().isIntegerConstant()
                && ((BinaryOperation) expr).getRight().isPower()
                && ((BinaryOperation) ((BinaryOperation) expr).getRight()).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) expr).getRight()).getRight().isRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) expr).getRight()).getRight()).getLeft().isIntegerConstant()
                && ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) expr).getRight()).getRight()).getRight().equals(Expression.TWO);
    }

    /**
     * Hilfsfunktion: Liefert, ob expr ein passender Kandidat für die
     * Rationalisierung des Nenners ist.
     */
    private static boolean isSuitableCandidateForMakingDenominatorRational(BinaryOperation expr) {
        return (expr.isSum() || expr.isDifference())
                && (isProductOfIntegerAndSqrtOfExpression(expr.getLeft()) && expr.getRight().isIntegerConstantOrRationalConstant()
                || expr.getLeft().isIntegerConstantOrRationalConstant() && isProductOfIntegerAndSqrtOfExpression(expr.getRight()));
    }

    /**
     * Sammelt passende Faktoren via 3. binomischer Formel zusammen (dabei
     * werden containsSqrtFactor() und isSuitableCandidateForThirdBinomial()
     * benutzt).
     */
    public static Expression collectFactorsByThirdBinomialFormula(BinaryOperation expr) throws EvaluationException {

        if (expr.isNotProduct()) {
            return expr;
        }

        ExpressionCollection factors = SimplifyUtilities.getFactors(expr);

        BinaryOperation candidate;
        Expression candidateLeft, candidateRight;

        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) != null && factors.get(i) instanceof BinaryOperation && isSuitableCandidateForThirdBinomial((BinaryOperation) factors.get(i))) {
                candidate = (BinaryOperation) factors.get(i);
                candidateLeft = ((BinaryOperation) factors.get(i)).getLeft();
                candidateRight = ((BinaryOperation) factors.get(i)).getRight();
            } else {
                continue;
            }

            for (int j = i + 1; j < factors.getBound(); j++) {
                if (factors.get(j) != null) {
                    if (candidate.isSum() && factors.get(j).isDifference()) {

                        if (candidateLeft.equivalent(((BinaryOperation) factors.get(j)).getLeft()) && candidateRight.equivalent(((BinaryOperation) factors.get(j)).getRight())) {
                            factors.remove(i);
                            factors.remove(j);
                            factors.put(i, candidateLeft.mult(candidateLeft).sub(candidateRight.mult(candidateRight)));
                        } else if (candidateLeft.equivalent(((BinaryOperation) factors.get(j)).getRight()) && candidateRight.equivalent(((BinaryOperation) factors.get(j)).getLeft())) {
                            factors.remove(i);
                            factors.remove(j);
                            factors.put(i, candidateRight.mult(candidateRight).sub(candidateLeft.mult(candidateLeft)));
                        }

                    } else if (candidate.isDifference() && factors.get(j).isSum()) {

                        if ((candidateLeft.equivalent(((BinaryOperation) factors.get(j)).getLeft()) && candidateRight.equivalent(((BinaryOperation) factors.get(j)).getRight()))
                                || (candidateLeft.equivalent(((BinaryOperation) factors.get(j)).getRight()) && candidateRight.equivalent(((BinaryOperation) factors.get(j)).getLeft()))) {
                            factors.remove(i);
                            factors.remove(j);
                            factors.put(i, candidateLeft.mult(candidateLeft).sub(candidateRight.mult(candidateRight)));
                        }

                    }
                }
            }

        }

        // Ergebnis bilden.
        return SimplifyUtilities.produceProduct(factors);

    }

    /**
     * Macht den Nenner in Ausdrücken rational, falls der Nenner Faktoren der
     * Form a + b*(c/d)^(k/2) mit ganzen a, b, c, d und ungeradem k enthält.
     */
    public static Expression makeFactorsInDenominatorRational(BinaryOperation expr) {

        if (expr.isNotQuotient()) {
            return expr;
        }

        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactors(expr.getRight());
        /*
         additionalFactorsInNumerator stellt ZUSÄTZLICHE Faktoren im Zähler
         dar, die durch Rationalisierung des Nenners hinzugekommen sind.
         */
        ExpressionCollection additionalFactorsInNumerator = new ExpressionCollection();

        Expression additionalFactorInDenominator;
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i) instanceof BinaryOperation && isSuitableCandidateForMakingDenominatorRational((BinaryOperation) factorsDenominator.get(i))) {

                if (factorsDenominator.get(i).isSum()) {
                    additionalFactorsInNumerator.add(((BinaryOperation) factorsDenominator.get(i)).getLeft().sub(((BinaryOperation) factorsDenominator.get(i)).getRight()));
                    additionalFactorInDenominator = ((BinaryOperation) factorsDenominator.get(i)).getLeft().pow(2).sub(((BinaryOperation) factorsDenominator.get(i)).getRight().pow(2));
                    try {
                        /* 
                        Falls Ausdrücke der Art (x^(1/2))^2 auftauchen, dann sollen diese zu x vereinfacht werden.
                        Dies erweitert den Definitionsbereich des gesamten Ausdrucks nicht, denn im Zähler
                        taucht x^(1/2) immer noch auf.
                         */
                        additionalFactorInDenominator = additionalFactorInDenominator.simplifyMultiplyExponents();
                        additionalFactorInDenominator = additionalFactorInDenominator.orderDifferencesAndQuotients().orderSumsAndProducts();
                    } catch (EvaluationException e) {
                    }
                    factorsDenominator.put(i, additionalFactorInDenominator);
                } else if (factorsDenominator.get(i).isDifference()) {
                    additionalFactorsInNumerator.add(((BinaryOperation) factorsDenominator.get(i)).getLeft().add(((BinaryOperation) factorsDenominator.get(i)).getRight()));
                    additionalFactorInDenominator = ((BinaryOperation) factorsDenominator.get(i)).getLeft().pow(2).sub(((BinaryOperation) factorsDenominator.get(i)).getRight().pow(2));
                    try {
                        /* 
                        Falls Ausdrücke der Art (x^(1/2))^2 auftauchen, dann sollen diese zu x vereinfacht werden.
                        Dies erweitert den Definitionsbereich des gesamten Ausdrucks nicht, denn im Zähler
                        taucht x^(1/2) immer noch auf.
                         */
                        additionalFactorInDenominator = additionalFactorInDenominator.simplifyMultiplyExponents();
                        additionalFactorInDenominator = additionalFactorInDenominator.orderDifferencesAndQuotients().orderSumsAndProducts();
                    } catch (EvaluationException e) {
                    }
                    factorsDenominator.put(i, additionalFactorInDenominator);
                }

            }
        }

        // Ergebnis bilden.
        if (additionalFactorsInNumerator.isEmpty()) {
            // Dann konnte im Nenner nichts rationalisiert werden.
            return expr;
        }
        /*
         Der gesamte Zähler muss als Faktor im neuen Zähler ebenfalls
         aufgenommen werden.
         */
        additionalFactorsInNumerator.add(expr.getLeft());

        return SimplifyUtilities.produceProduct(additionalFactorsInNumerator).div(SimplifyUtilities.produceProduct(factorsDenominator));

    }

    /**
     * Sammelt in einem Produkt verschiedene Wurzeln zu einer großen Wurzel.
     */
    public static Expression collectVariousRootsToOneCommonRootInProducts(BinaryOperation expr) {

        if (expr.isNotProduct()) {
            return expr;
        }

        ExpressionCollection factors = SimplifyUtilities.getFactors(expr);
        ExpressionCollection resultFactors = new ExpressionCollection();

        /*
         Die Bezeichnungen sind wie folgt: der Term (a/b)^(p/m)*(c/d)^(q/n)
         wird zu einer einzigen großen Wurzel (u/v)^(1/commonRootDegree)
         zusammengefasst.
         */
        BigInteger a, b, c, d;
        BigInteger m, n, p, q, commonRootDegree;

        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) != null && factors.get(i).isPower()
                    && ((BinaryOperation) factors.get(i)).getLeft().isIntegerConstantOrRationalConstant()
                    && ((BinaryOperation) factors.get(i)).getRight().isRationalConstant()) {

                p = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(i)).getRight()).getLeft()).getBigIntValue();
                m = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(i)).getRight()).getRight()).getBigIntValue();
                for (int j = i + 1; j < factors.getBound(); j++) {

                    if (factors.get(j) != null && factors.get(j).isPower()
                            && ((BinaryOperation) factors.get(j)).getLeft().isIntegerConstantOrRationalConstant()
                            && ((BinaryOperation) factors.get(j)).getRight().isRationalConstant()) {

                        q = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(j)).getRight()).getLeft()).getBigIntValue();
                        n = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(j)).getRight()).getRight()).getBigIntValue();
                        commonRootDegree = ArithmeticMethods.lcm(m, n);

                        // In diesem Fall werden die Potenzen von simplify() nicht vollständig ausgerechnet.
                        if (ArithmeticMethods.lcm(m, n).divide(m).multiply(p).add(ArithmeticMethods.lcm(m, n).divide(n).multiply(q)).compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_COMMON_ROOT)) > 0) {
                            continue;
                        }

                        if (((BinaryOperation) factors.get(i)).getLeft().isRationalConstant()) {
                            a = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(i)).getLeft()).getLeft()).getBigIntValue();
                            b = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(i)).getLeft()).getRight()).getBigIntValue();
                        } else {
                            a = ((Constant) ((BinaryOperation) factors.get(i)).getLeft()).getBigIntValue();
                            b = BigInteger.ONE;
                        }
                        if (((BinaryOperation) factors.get(j)).getLeft().isRationalConstant()) {
                            c = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(j)).getLeft()).getLeft()).getBigIntValue();
                            d = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(j)).getLeft()).getRight()).getBigIntValue();
                        } else {
                            c = ((Constant) ((BinaryOperation) factors.get(j)).getLeft()).getBigIntValue();
                            d = BigInteger.ONE;
                        }

                        resultFactors.add(new Constant(a.pow(ArithmeticMethods.lcm(m, n).divide(m).multiply(p).intValue()).multiply(
                                c.pow(ArithmeticMethods.lcm(m, n).divide(n).multiply(q).intValue()))).div(
                                new Constant(b.pow(ArithmeticMethods.lcm(m, n).divide(m).multiply(p).intValue()).multiply(
                                        d.pow(ArithmeticMethods.lcm(m, n).divide(n).multiply(q).intValue())))).pow(BigInteger.ONE, commonRootDegree));
                        factors.remove(i);
                        factors.remove(j);
                        break;

                    }
                }

            }
        }

        // Ergebnis bilden.
        if (resultFactors.isEmpty()) {
            return expr;
        }

        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) != null) {
                resultFactors.add(factors.get(i));
            }
        }

        return SimplifyUtilities.produceProduct(resultFactors);

    }

    /**
     * Sammelt in einem Produkt verschiedene Wurzeln zu einer großen Wurzel.
     */
    public static Expression collectVariousRootsToOneCommonRootInQuotients(BinaryOperation expr) {

        if (expr.isNotQuotient()) {
            return expr;
        }

        ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(expr);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(expr);
        ExpressionCollection resultFactorsNumerator = new ExpressionCollection();
        ExpressionCollection resultFactorsDenominator = new ExpressionCollection();

        /*
         Die Bezeichnungen sind wie folgt: der Term (a/b)^(p/m)/(c/d)^(q/n)
         wird zu einer einzigen großen Wurzel (u/v)^(1/commonRootDegree)
         zusammengefasst.
         */
        BigInteger a, b, c, d;
        BigInteger m, n, p, q, commonRootDegree;

        // Jetzt im Zähler UND Nenner sammeln.
        for (int i = 0; i < factorsNumerator.getBound(); i++) {

            if (factorsNumerator.get(i) != null && factorsNumerator.get(i).isPower()
                    && ((BinaryOperation) factorsNumerator.get(i)).getLeft().isIntegerConstantOrRationalConstant()
                    && ((BinaryOperation) factorsNumerator.get(i)).getRight().isRationalConstant()) {

                p = ((Constant) ((BinaryOperation) ((BinaryOperation) factorsNumerator.get(i)).getRight()).getLeft()).getBigIntValue();
                m = ((Constant) ((BinaryOperation) ((BinaryOperation) factorsNumerator.get(i)).getRight()).getRight()).getBigIntValue();

                for (int j = 0; j < factorsDenominator.getBound(); j++) {

                    if (factorsDenominator.get(j) != null && factorsDenominator.get(j).isPower()
                            && ((BinaryOperation) factorsDenominator.get(j)).getLeft().isIntegerConstantOrRationalConstant()
                            && ((BinaryOperation) factorsDenominator.get(j)).getRight().isRationalConstant()) {

                        q = ((Constant) ((BinaryOperation) ((BinaryOperation) factorsDenominator.get(j)).getRight()).getLeft()).getBigIntValue();
                        n = ((Constant) ((BinaryOperation) ((BinaryOperation) factorsDenominator.get(j)).getRight()).getRight()).getBigIntValue();
                        commonRootDegree = ArithmeticMethods.lcm(m, n);

                        // In diesem Fall werden die Potenzen von simplify() nicht vollständig ausgerechnet.
                        if (ArithmeticMethods.lcm(m, n).divide(m).multiply(p).add(ArithmeticMethods.lcm(m, n).divide(n).multiply(q)).compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_COMMON_ROOT)) > 0) {
                            continue;
                        }

                        if (((BinaryOperation) factorsNumerator.get(i)).getLeft().isRationalConstant()) {
                            a = ((Constant) ((BinaryOperation) ((BinaryOperation) factorsNumerator.get(i)).getLeft()).getLeft()).getBigIntValue();
                            b = ((Constant) ((BinaryOperation) ((BinaryOperation) factorsNumerator.get(i)).getLeft()).getRight()).getBigIntValue();
                        } else {
                            a = ((Constant) ((BinaryOperation) factorsNumerator.get(i)).getLeft()).getBigIntValue();
                            b = BigInteger.ONE;
                        }
                        if (((BinaryOperation) factorsDenominator.get(j)).getLeft().isRationalConstant()) {
                            c = ((Constant) ((BinaryOperation) ((BinaryOperation) factorsDenominator.get(j)).getLeft()).getLeft()).getBigIntValue();
                            d = ((Constant) ((BinaryOperation) ((BinaryOperation) factorsDenominator.get(j)).getLeft()).getRight()).getBigIntValue();
                        } else {
                            c = ((Constant) ((BinaryOperation) factorsDenominator.get(j)).getLeft()).getBigIntValue();
                            d = BigInteger.ONE;
                        }

                        resultFactorsNumerator.add(new Constant(a.pow(commonRootDegree.divide(m).multiply(p).intValue()).multiply(
                                d.pow(commonRootDegree.divide(n).multiply(q).intValue()))).div(
                                new Constant(b.pow(commonRootDegree.divide(m).multiply(p).intValue()).multiply(
                                        c.pow(commonRootDegree.divide(n).multiply(q).intValue())))).pow(BigInteger.ONE, commonRootDegree));
                        factorsNumerator.remove(i);
                        factorsDenominator.remove(j);
                        break;

                    }
                }

            }

        }

        // Ergebnis bilden.
        if (resultFactorsNumerator.isEmpty() && resultFactorsDenominator.isEmpty()) {
            return expr;
        }

        for (int i = 0; i < factorsNumerator.getBound(); i++) {
            if (factorsNumerator.get(i) != null) {
                resultFactorsNumerator.add(factorsNumerator.get(i));
            }
        }

        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i) != null) {
                resultFactorsNumerator.add(factorsDenominator.get(i));
            }
        }

        return SimplifyUtilities.produceQuotient(resultFactorsNumerator, resultFactorsDenominator);

    }

    /*
     Methoden zum Auffinden von Quadratwureln in Q[root(a)], a rational.
     */
    /**
     * Hilfsmethode. Gibt zurück, ob der Ausdruck expr eine Quadratwurzel aus
     * einer rationalen Zahl ist.
     */
    private static boolean isSqrtOfRational(Expression expr) {
        return expr.isPower() && ((BinaryOperation) expr).getRight().equals(ONE.div(TWO)) && ((BinaryOperation) expr).getLeft().isIntegerConstantOrRationalConstant();
    }

    /**
     * Vereinfacht einen Ausdruck der Form (a + b*c^(1/2))^(1/n) in die Form x +
     * y*c^(1/2), wenn möglich (a, b, c, x, y rationale Zahlen).
     */
    public static Expression computeRootFromDegreeTwoElementsOverRationals(Expression expr) {

        if (!expr.isConstant() || !expr.isPower() || !((BinaryOperation) expr).getRight().isRationalConstant()
                || !((BinaryOperation) ((BinaryOperation) expr).getRight()).getLeft().isPositiveIntegerConstant()
                || !((BinaryOperation) ((BinaryOperation) expr).getRight()).getRight().isPositiveIntegerConstant()
                || !((BinaryOperation) expr).getLeft().isSum() && !((BinaryOperation) expr).getLeft().isDifference()) {
            return expr;
        }

        BigInteger exponent = ((Constant) ((BinaryOperation) ((BinaryOperation) expr).getRight()).getLeft()).getBigIntValue();
        BigInteger rootdegree = ((Constant) ((BinaryOperation) ((BinaryOperation) expr).getRight()).getRight()).getBigIntValue();
        Expression a = ZERO, b = ZERO, c = ZERO;
        boolean rationalSummandFound = false, sqrtSummandFound = false;

        Expression summandLeft = ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getLeft(), summandRight = ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getRight();

        if (((BinaryOperation) expr).getLeft().isSum()) {

            // Fall: expr = a + b*c^(1/2)
            if (summandLeft.isIntegerConstantOrRationalConstant()) {
                rationalSummandFound = true;
                a = summandLeft;

                if (isSqrtOfRational(summandRight)) {
                    sqrtSummandFound = true;
                    b = ONE;
                    c = ((BinaryOperation) summandRight).getLeft();
                } else if (summandRight.isProduct() && ((BinaryOperation) summandRight).getLeft().isIntegerConstant() && isSqrtOfRational(((BinaryOperation) summandRight).getRight())) {
                    sqrtSummandFound = true;
                    b = ((BinaryOperation) summandRight).getLeft();
                    c = ((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getLeft();
                } else if (summandRight.isQuotient() && ((BinaryOperation) summandRight).getRight().isIntegerConstant()) {
                    if (isSqrtOfRational(((BinaryOperation) summandRight).getLeft())) {
                        sqrtSummandFound = true;
                        b = ONE.div(((BinaryOperation) summandRight).getRight());
                        c = ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getLeft();
                    } else if (((BinaryOperation) summandRight).getLeft().isProduct() && ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getLeft().isIntegerConstant()
                            && isSqrtOfRational(((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight())) {
                        sqrtSummandFound = true;
                        b = ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getLeft().div(((BinaryOperation) summandRight).getRight());
                        c = ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight()).getRight();
                    }
                }

            }

        } else if (summandLeft.isIntegerConstantOrRationalConstant()) {

            // Fall: expr = a - b*c^(1/2)
            rationalSummandFound = true;
            a = summandLeft;

            if (isSqrtOfRational(summandRight)) {
                sqrtSummandFound = true;
                b = MINUS_ONE;
                c = ((BinaryOperation) summandRight).getLeft();
            } else if (summandRight.isProduct() && ((BinaryOperation) summandRight).getLeft().isIntegerConstant() && isSqrtOfRational(((BinaryOperation) summandRight).getRight())) {
                sqrtSummandFound = true;
                b = MINUS_ONE.mult(((BinaryOperation) summandRight).getLeft());
                c = ((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getLeft();
            } else if (summandRight.isQuotient() && ((BinaryOperation) summandRight).getRight().isIntegerConstant()) {
                if (isSqrtOfRational(((BinaryOperation) summandRight).getLeft())) {
                    sqrtSummandFound = true;
                    b = MINUS_ONE.div(((BinaryOperation) summandRight).getRight());
                    c = ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getLeft();
                } else if (((BinaryOperation) summandRight).getLeft().isProduct() && ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getLeft().isIntegerConstant()
                        && isSqrtOfRational(((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight())) {
                    sqrtSummandFound = true;
                    b = MINUS_ONE.mult(((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getLeft()).div(((BinaryOperation) summandRight).getRight());
                    c = ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight()).getRight();
                }
            }

        } else if (summandRight.isIntegerConstantOrRationalConstant()) {

            // Fall: expr = b*c^(1/2) - a
            rationalSummandFound = true;
            a = MINUS_ONE.mult(summandRight);

            if (isSqrtOfRational(summandLeft)) {
                sqrtSummandFound = true;
                b = ONE;
                c = ((BinaryOperation) summandLeft).getLeft();
            } else if (summandLeft.isProduct() && ((BinaryOperation) summandLeft).getLeft().isIntegerConstant() && isSqrtOfRational(((BinaryOperation) summandLeft).getRight())) {
                sqrtSummandFound = true;
                b = ((BinaryOperation) summandLeft).getLeft();
                c = ((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getLeft();
            } else if (summandLeft.isQuotient() && ((BinaryOperation) summandLeft).getRight().isIntegerConstant()) {
                if (isSqrtOfRational(((BinaryOperation) summandLeft).getLeft())) {
                    sqrtSummandFound = true;
                    b = ONE.div(((BinaryOperation) summandLeft).getRight());
                    c = ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getLeft();
                } else if (((BinaryOperation) summandLeft).getLeft().isProduct() && ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getLeft().isIntegerConstant()
                        && isSqrtOfRational(((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getRight())) {
                    sqrtSummandFound = true;
                    b = ONE.mult(((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getLeft()).div(((BinaryOperation) summandLeft).getRight());
                    c = ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getRight()).getLeft();
                }
            }

        }

        if (rationalSummandFound && sqrtSummandFound) {

            Expression[] coefficients = new Expression[3];
            coefficients[0] = a;
            coefficients[1] = b;
            coefficients[2] = c;
            BigInteger divisor = BigInteger.valueOf(2);
            while (rootdegree.mod(divisor).compareTo(BigInteger.ZERO) == 0) {
                try {
                    coefficients = computeSqrtFromDegreeTwoElementsOverRationals(coefficients[0], coefficients[1], coefficients[2]);
                    rootdegree = rootdegree.divide(BigInteger.valueOf(2));
                } catch (RootNotRationalException e) {
                    break;
                }
            }
            divisor = BigInteger.valueOf(3);
            while (rootdegree.mod(divisor).compareTo(BigInteger.ZERO) == 0) {
                try {
                    coefficients = computeCubicRootFromDegreeTwoElementsOverRationals(coefficients[0], coefficients[1], coefficients[2]);
                    rootdegree = rootdegree.divide(BigInteger.valueOf(3));
                } catch (RootNotRationalException e) {
                    break;
                }
            }
            divisor = BigInteger.valueOf(5);
            while (rootdegree.mod(divisor).compareTo(BigInteger.ZERO) == 0) {
                try {
                    coefficients = computeFifthRootFromDegreeTwoElementsOverRationals(coefficients[0], coefficients[1], coefficients[2]);
                    rootdegree = rootdegree.divide(BigInteger.valueOf(5));
                } catch (RootNotRationalException e) {
                    break;
                }
            }

            if (!a.equals(coefficients[0]) || !b.equals(coefficients[1])) {
                return coefficients[0].add(coefficients[1].mult(c.pow(1, 2))).pow(exponent);
            }

        }

        return expr;

    }

    /**
     * Hilfsmethode. Direktes Quadratwurzelziehen, wenn der Radikand rational
     * ist.
     *
     * @throws RootNotRationalException
     */
    private static Expression root(Expression radicand, int n) throws RootNotRationalException {

        if (radicand.isIntegerConstant()) {
            BigInteger a = ((Constant) radicand).getBigIntValue();
            BigInteger sqrt;
            try {
                sqrt = ArithmeticMethods.root(a, n);
            } catch (EvaluationException e) {
                throw new RootNotRationalException();
            }
            if (sqrt.pow(2).compareTo(a) == 0) {
                return new Constant(sqrt);
            }
        } else if (radicand.isRationalConstant()) {
            BigInteger numerator = ((Constant) ((BinaryOperation) radicand).getLeft()).getBigIntValue();
            BigInteger denominator = ((Constant) ((BinaryOperation) radicand).getRight()).getBigIntValue();
            BigInteger sqrtOfNumerator, sqrtOfDenominator;
            try {
                sqrtOfNumerator = ArithmeticMethods.root(numerator, n);
                sqrtOfDenominator = ArithmeticMethods.root(denominator, n);
            } catch (EvaluationException e) {
                throw new RootNotRationalException();
            }
            if (sqrtOfNumerator.pow(2).compareTo(numerator) == 0 && sqrtOfDenominator.pow(2).compareTo(denominator) == 0) {
                return new Constant(sqrtOfNumerator).div(sqrtOfDenominator);
            }
        }

        throw new RootNotRationalException();

    }

    private static Expression[] computeSqrtFromDegreeTwoElementsOverRationals(Expression a, Expression b, Expression c) throws RootNotRationalException {

        if (a.isAlwaysNegative()) {
            // In diesem Fall kann die Wurzel nicht vereinfacht werden.
            throw new RootNotRationalException();
        }

        Expression rationalPart, rationalCoefficientOfSqrtPart;

        /*
         Falls (a+b*c^(1/2))^(1/2) = u+v*c^(1/2) mit rationalen u, v, so ist
         u = (2*a + 2*(a^2-c*b^2)^(1/2))^(1/2)/2 oder u = (2*a - 2*(a^2-c*b^2)^(1/2))^(1/2)/2.
         Hier werden beide Möglichkeiten ausprobiert.
         */
        Expression radical;
        try {
            radical = a.pow(2).sub(c.mult(b.pow(2))).simplify();
            radical = root(radical, 2);
        } catch (EvaluationException | RootNotRationalException e) {
            throw new RootNotRationalException();
        }
        try {

            rationalPart = TWO.mult(a).sub(TWO.mult(radical)).simplify();
            rationalPart = root(rationalPart, 2).div(2).simplify();
            rationalCoefficientOfSqrtPart = b.div(TWO.mult(rationalPart)).simplify();
            return new Expression[]{rationalPart, rationalCoefficientOfSqrtPart, c};

        } catch (EvaluationException e) {
            throw new RootNotRationalException();
        } catch (RootNotRationalException e) {

            try {
                rationalPart = TWO.mult(a).add(TWO.mult(radical)).simplify();
                rationalPart = root(rationalPart, 2).div(2).simplify();
                rationalCoefficientOfSqrtPart = b.div(TWO.mult(rationalPart)).simplify();
                return new Expression[]{rationalPart, rationalCoefficientOfSqrtPart, c};
            } catch (EvaluationException | RootNotRationalException ex) {
                throw new RootNotRationalException();
            }

        }

    }

    private static Expression[] computeCubicRootFromDegreeTwoElementsOverRationals(Expression a, Expression b, Expression c) throws RootNotRationalException {

        Expression rationalPart, rationalCoefficientOfSqrtPart;

        ExpressionCollection coefficients = new ExpressionCollection();
        try {
            coefficients.add(MINUS_ONE.mult(b.pow(3)).simplify());
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(new Constant(27).mult(a.pow(2)).sub(new Constant(15).mult(b.pow(2)).mult(c)).simplify());
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(new Constant(-48).mult(b).mult(c.pow(2)).simplify());
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(new Constant(64).mult(c.pow(3)).simplify());
        } catch (EvaluationException e) {
            throw new RootNotRationalException();
        }

        ExpressionCollection rationalZeros = PolynomialAlgebraMethods.getRationalZerosOfRationalPolynomial(coefficients);

        // Da die Lösung eindeutig ist, muss rationalZeros.size() <= 1 sein.
        if (rationalZeros.isEmpty()) {
            throw new RootNotRationalException();
        }

        rationalCoefficientOfSqrtPart = rationalZeros.get(0);
        try {
            rationalPart = THREE.mult(a).mult(rationalCoefficientOfSqrtPart).div(new Constant(8).mult(c).mult(rationalCoefficientOfSqrtPart.pow(3)).add(b)).simplify();
            return new Expression[]{rationalPart, rationalCoefficientOfSqrtPart, c};
        } catch (EvaluationException e) {
            throw new RootNotRationalException();
        }

    }

    private static Expression[] computeFifthRootFromDegreeTwoElementsOverRationals(Expression a, Expression b, Expression c) throws RootNotRationalException {

        Expression rationalPart, rationalCoefficientOfSqrtPart;

        ExpressionCollection coefficients = new ExpressionCollection();
        try {
            coefficients.add(MINUS_ONE.mult(b.pow(5)).simplify());
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(new Constant(3125).mult(a.pow(4)).add(new Constant(705).mult(b.pow(4)).mult(c.pow(2))).sub(new Constant(3750).mult(a.pow(2)).mult(b.pow(2)).mult(c)).simplify());
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(new Constant(120000).mult(a.pow(2)).mult(b).mult(c.pow(3)).sub(new Constant(122560).mult(b.pow(3)).mult(c.pow(4))).simplify());
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(new Constant(640000).mult(a.pow(2)).mult(c.pow(5)).sub(new Constant(599040).mult(b.pow(2)).mult(c.pow(6))).simplify());
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(new Constant(-327680).mult(b).mult(c.pow(8)).simplify());
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(ZERO);
            coefficients.add(new Constant(1048576).mult(c.pow(10)).simplify());
        } catch (EvaluationException e) {
            throw new RootNotRationalException();
        }

        ExpressionCollection rationalZeros = PolynomialAlgebraMethods.getRationalZerosOfRationalPolynomial(coefficients);

        // Da die Lösung eindeutig ist, muss rationalZeros.size() <= 1 sein.
        if (rationalZeros.isEmpty()) {
            throw new RootNotRationalException();
        }

        rationalCoefficientOfSqrtPart = rationalZeros.get(0);

        coefficients.clear();
        try {
            coefficients.add(MINUS_ONE.mult(a).simplify());
            coefficients.add(new Constant(5).mult(c.pow(2)).mult(rationalCoefficientOfSqrtPart.pow(4)).simplify());
            coefficients.add(ZERO);
            coefficients.add(new Constant(10).mult(c).mult(rationalCoefficientOfSqrtPart.pow(2)).simplify());
            coefficients.add(ZERO);
            coefficients.add(ONE);
        } catch (EvaluationException e) {
            throw new RootNotRationalException();
        }

        rationalZeros = PolynomialAlgebraMethods.getRationalZerosOfRationalPolynomial(coefficients);
        // Da die Lösung eindeutig ist, muss rationalZeros.size() <= 1 sein.
        if (rationalZeros.isEmpty()) {
            throw new RootNotRationalException();
        }

        rationalPart = rationalZeros.get(0);
        return new Expression[]{rationalPart, rationalCoefficientOfSqrtPart, c};

    }

}

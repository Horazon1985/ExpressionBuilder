package expressionsimplifymethods;

import computation.ArithmeticMethods;
import computationbounds.ComputationBounds;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import expressionbuilder.TypeBinary;
import java.math.BigInteger;
import java.util.HashMap;

public abstract class SimplifyAlgebraicExpressionMethods {

    /**
     * Gibt zurück, ob expr eine ganze Zahl oder ein Bruch mit ungeradem
     * Exponenten ist. Wird in vielen Methoden benötigt, wo man Wurzeln ziehen
     * oder Potenzieren muss, ohne Rücksicht auf das Vorzeichen der Basis nehmen
     * zu müssen.
     */
    public static boolean isAdmissibleExponent(Expression expr) {

        return expr.isIntegerConstant() || (expr.isRationalConstant()
                && ((BinaryOperation) expr).getLeft().isIntegerConstant() && ((BinaryOperation) expr).getRight().isOddConstant());

    }

    /**
     * Vereinfacht beispielsweise (8)^(1/2) = 2*2^(1/2), (108)^(2/3) = 9*4^(2/3)
     * etc. Faktorisiert werden nur Faktoren <= 1000000 (da hier
     * SolveMethods.getDivisors() benötigt wird).
     */
    public static Expression factorizeIntegerFactorsFromIntegerRoots(BinaryOperation expr) {

        if (!expr.getType().equals(TypeBinary.POW) || !expr.getLeft().isIntegerConstant()
                || !(expr.getRight().isRationalConstant()
                && ((BinaryOperation) expr.getRight()).getLeft().isIntegerConstant()
                && ((BinaryOperation) expr.getRight()).getRight().isIntegerConstant())) {
            return expr;
        }

        BigInteger m = ((Constant) ((BinaryOperation) expr.getRight()).getLeft()).getValue().toBigInteger();
        BigInteger n = ((Constant) ((BinaryOperation) expr.getRight()).getRight()).getValue().toBigInteger();
        if (m.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 || n.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            return expr;
        }

        BigInteger a = ((Constant) expr.getLeft()).getValue().toBigInteger();

        if (a.compareTo(BigInteger.ZERO) <= 0 || m.intValue() < 0 || n.intValue() < 0) {
            return expr;
        }

        int p = m.intValue();
        int q = n.intValue();
        
        BigInteger divisor;
        BigInteger factorOutside = BigInteger.ONE;
        int sqrtOfBoundOfDivisorsOfIntegers = (int) (Math.sqrt(ComputationBounds.BOUND_DIVISORS_OF_INTEGERS) + 1);
        
        if (a.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_DIVISORS_OF_INTEGERS)) <= 0) {

            HashMap<Integer, BigInteger> divisorsOfA = ArithmeticMethods.getDivisors(a);

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
    public static Expression factorizeIntegerFactorsFromRationalRoots(BinaryOperation expr) {

        // Alles außer Ausdrücke der Form (a/b)^(p/q) wird aussortiert.
        if (!expr.getType().equals(TypeBinary.POW) || !expr.getLeft().isRationalConstant()
                || !(((BinaryOperation) expr.getLeft()).getLeft().isIntegerConstant()
                && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant())
                || !(expr.getRight().isRationalConstant()
                && ((BinaryOperation) expr.getRight()).getLeft().isIntegerConstant()
                && ((BinaryOperation) expr.getRight()).getRight().isIntegerConstant())) {
            return expr;
        }

        BigInteger a = ((Constant) ((BinaryOperation) expr.getLeft()).getLeft()).getValue().toBigInteger();
        BigInteger b = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();
        BigInteger m = ((Constant) ((BinaryOperation) expr.getRight()).getLeft()).getValue().toBigInteger();
        BigInteger n = ((Constant) ((BinaryOperation) expr.getRight()).getRight()).getValue().toBigInteger();
        if (m.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 || n.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            return expr;
        }

        int p = m.intValue();
        int q = n.intValue();

        BigInteger divisor;
        BigInteger factorOutsideInEnumerator = BigInteger.ONE;
        BigInteger factorOutsideInDenominator = BigInteger.ONE;

        // Im Zähler faktorisieren.
        HashMap<Integer, BigInteger> setOfDivisors;
        int sqrtOfBoundOfDivisorsOfIntegers = (int) (Math.sqrt(ComputationBounds.BOUND_DIVISORS_OF_INTEGERS) + 1);
        
        if (a.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_DIVISORS_OF_INTEGERS)) <= 0) {

            setOfDivisors = ArithmeticMethods.getDivisors(a);

            for (int i = 0; i < setOfDivisors.size(); i++) {

                divisor = setOfDivisors.get(i);
                if (divisor.compareTo(BigInteger.ONE) > 0 && a.mod(divisor.pow(q)).compareTo(BigInteger.ZERO) == 0) {
                    factorOutsideInEnumerator = factorOutsideInEnumerator.multiply(divisor.pow(p));
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
                    factorOutsideInEnumerator = factorOutsideInEnumerator.multiply(divisor.pow(p));
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
        if (b.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_DIVISORS_OF_INTEGERS)) <= 0) {

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

        if (factorOutsideInEnumerator.compareTo(BigInteger.ONE) == 0 && factorOutsideInDenominator.compareTo(BigInteger.ONE) == 0) {
            return expr;
        } else if (factorOutsideInEnumerator.compareTo(BigInteger.ONE) > 0 && factorOutsideInDenominator.compareTo(BigInteger.ONE) == 0) {
            return new Constant(factorOutsideInEnumerator).mult((new Constant(a).div(b)).pow(expr.getRight()));
        } else if (factorOutsideInEnumerator.compareTo(BigInteger.ONE) == 0 && factorOutsideInDenominator.compareTo(BigInteger.ONE) > 0) {
            return new Constant(a).div(b).pow(expr.getRight()).div(factorOutsideInDenominator);
        } else {
            return new Constant(factorOutsideInEnumerator).mult((new Constant(a).div(b)).pow(expr.getRight())).div(factorOutsideInDenominator);
        }

    }

    /**
     * Hilfsmethode: "Counter" für Exponententupel für (a_1 + ... + a_n)^k
     */
    public static int[] counterBinomialExponents(int[] counter, int k) {

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
            while (counter[indexOfNextDigitToBeIncreased] == 0 && indexOfNextDigitToBeIncreased < k) {
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

        BigInteger n = ((Constant) expr.getRight()).getValue().toBigInteger();
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
            leftExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight()).getValue().toBigInteger();
            leftSummandSuitable = true;
        } else if (summandLeft.isProduct()
                && ((BinaryOperation) summandLeft).getLeft().isIntegerConstant()
                && ((BinaryOperation) summandLeft).getRight().isPower()
                && ((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight().isRationalConstant()) {
            // Falls summandLeft = a*b^(k/m) oder = a*(b/c)^(k/m)
            leftExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight()).getRight()).getValue().toBigInteger();
            leftSummandSuitable = true;
        } else if (summandLeft.isQuotient()
                && ((BinaryOperation) summandLeft).getRight().isIntegerConstant()
                && ((BinaryOperation) summandLeft).getLeft().isPower()
                && ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getRight().isRationalConstant()) {
            // Falls summandLeft = a^(k/m)/b oder = (a/b)^(k/m)/c
            leftExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getRight()).getRight()).getValue().toBigInteger();
            leftSummandSuitable = true;
        } else if (summandLeft.isQuotient()
                && ((BinaryOperation) summandLeft).getRight().isIntegerConstant()
                && ((BinaryOperation) summandLeft).getLeft().isProduct()
                && ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getLeft().isIntegerConstant()
                && ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getRight().isPower()
                && ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getRight()).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getRight()).getRight().isRationalConstant()) {
            // Falls summandLeft = a*c^(k/m)/b oder = a*(b/c)^(k/m)/d
            leftExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getLeft()).getRight()).getRight()).getRight()).getValue().toBigInteger();
            leftSummandSuitable = true;
        } else if (summandLeft.isQuotient()
                && ((BinaryOperation) summandLeft).getLeft().isIntegerConstant()
                && ((BinaryOperation) summandLeft).getRight().isPower()
                && (((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getLeft()).isIntegerConstantOrRationalConstant()
                && (((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight()).isRationalConstant()) {
            // Falls summandLeft = a/b^(k/m) oder = a/(b/c)^(k/m)
            leftExponentDenominator = ((Constant) (((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight()).getRight())).getValue().toBigInteger();
            leftSummandSuitable = true;
        } else if (summandLeft.isQuotient()
                && ((BinaryOperation) summandLeft).getLeft().isIntegerConstant()
                && ((BinaryOperation) summandLeft).getRight().isProduct()
                && (((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getLeft()).isIntegerConstant()
                && (((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight()).isPower()
                && ((BinaryOperation) (((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight())).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) (((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight())).getRight().isRationalConstant()) {
            // Falls summandLeft = a/(b*c^(k/m)) oder = a/(b*(c/d)^(k/m))
            leftExponentDenominator = ((Constant) ((BinaryOperation) (((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandLeft).getRight()).getRight()).getRight())).getRight()).getValue().toBigInteger();
            leftSummandSuitable = true;
        }

        // Form des rechten Summanden untersuchen.
        if (summandRight.isPower()
                && ((BinaryOperation) summandRight).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) summandRight).getRight().isRationalConstant()) {
            // Falls summandRight = a^(k/m) oder = (a/b)^(k/m)
            rightExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight()).getValue().toBigInteger();
            rightSummandSuitable = true;
        } else if (summandRight.isProduct()
                && ((BinaryOperation) summandRight).getLeft().isIntegerConstant()
                && ((BinaryOperation) summandRight).getRight().isPower()
                && ((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight().isRationalConstant()) {
            // Falls summandRight = a*b^(k/m) oder a*(b/c)^(k/m)
            rightExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight()).getRight()).getValue().toBigInteger();
            rightSummandSuitable = true;
        } else if (summandRight.isQuotient()
                && ((BinaryOperation) summandRight).getRight().isIntegerConstant()
                && ((BinaryOperation) summandRight).getLeft().isPower()
                && ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight().isRationalConstant()) {
            // Falls summandRight = a^(k/m)/b oder = (a/b)^(k/m)/c
            rightExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight()).getRight()).getValue().toBigInteger();
            rightSummandSuitable = true;
        } else if (summandRight.isQuotient()
                && ((BinaryOperation) summandRight).getRight().isIntegerConstant()
                && ((BinaryOperation) summandRight).getLeft().isProduct()
                && ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getLeft().isIntegerConstant()
                && ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight().isPower()
                && ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight()).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight()).getRight().isRationalConstant()) {
            // Falls summandRight = a*b^(k/m)/c oder = a*(b/c)^(k/m)/d
            rightExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getLeft()).getRight()).getRight()).getRight()).getValue().toBigInteger();
            rightSummandSuitable = true;
        } else if (summandRight.isQuotient()
                && ((BinaryOperation) summandRight).getLeft().isIntegerConstant()
                && ((BinaryOperation) summandRight).getRight().isPower()
                && (((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getLeft()).isIntegerConstantOrRationalConstant()
                && (((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight()).isRationalConstant()) {
            // Falls summandRight = a/b^(k/m) oder = a/(b/c)^(k/m)
            rightExponentDenominator = ((Constant) (((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight()).getRight())).getValue().toBigInteger();
            rightSummandSuitable = true;
        } else if (summandRight.isQuotient()
                && ((BinaryOperation) summandRight).getLeft().isIntegerConstant()
                && ((BinaryOperation) summandRight).getRight().isProduct()
                && (((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getLeft()).isIntegerConstant()
                && (((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight()).isPower()
                && ((BinaryOperation) (((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight())).getLeft().isIntegerConstantOrRationalConstant()
                && ((BinaryOperation) (((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight())).getRight().isRationalConstant()) {
            // Falls summandRight = a/(b*c^(k/m)) oder = a/(b*(c/d)^(k/m))
            rightExponentDenominator = ((Constant) ((BinaryOperation) (((BinaryOperation) ((BinaryOperation) ((BinaryOperation) summandRight).getRight()).getRight()).getRight())).getRight()).getValue().toBigInteger();
            rightSummandSuitable = true;
        }

        if (!leftSummandSuitable || !rightSummandSuitable) {
            return expr;
        }

        /**
         * Anwendung der binomischen Formel wird nur dann für sinnvoll erachtet,
         * wenn leftExponentDenominator == rightExponentDenominator und
         * leftExponentDenominator == rightExponentDenominator <= max(5, n) und
         * n <= 100 ist.
         */
        if (leftExponentDenominator.compareTo(BigInteger.ZERO) > 0 && rightExponentDenominator.compareTo(BigInteger.ZERO) > 0
                && leftExponentDenominator.compareTo(rightExponentDenominator) != 0 || leftExponentDenominator.compareTo(n) > 0
                || leftExponentDenominator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ROOTDEGREE_OF_SUMMAND_WITHIN_BINOMIAL)) > 0
                || rightExponentDenominator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ROOTDEGREE_OF_SUMMAND_WITHIN_BINOMIAL)) > 0
                || n.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_POWER_OF_BINOMIAL)) > 0) {
            return expr;
        }

        return binomialExpansion((BinaryOperation) expr.getLeft(), n.intValue());

    }

    /**
     * Hilfsfunktion: Liefert, ob expr einen Quadratwurzelfaktor enthält.
     */
    public static boolean containsSqrtFactor(Expression expr) {

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
    public static boolean isSuitableCandidateForThirdBinomial(BinaryOperation expr) {
        return (expr.getType().equals(TypeBinary.PLUS) || expr.getType().equals(TypeBinary.MINUS))
                && (containsSqrtFactor(expr.getLeft()) || containsSqrtFactor(expr.getRight()));
    }

    /**
     * Hilfsfunktion: Liefert, ob expr von der Form b^(1/2) oder a*b^(1/2) mit
     * ganzem a und rationalem b ist. Für die Rationalisierung des Nenners
     * müssen im Wesentlichen nur solche Terme betrachtet werden, da
     * allgemeinere Terme während des simplify() auf diese Form gebracht werden.
     */
    public static boolean isProductOfIntegerAndSqrtOfRational(Expression expr) {
        if (expr.isPower()
                && ((BinaryOperation) expr).getRight().isRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) expr).getRight()).getLeft().isIntegerConstant()
                && ((BinaryOperation) ((BinaryOperation) expr).getRight()).getRight().equals(Expression.TWO)) {
            return true;
        }
        return expr.isProduct()
                && ((BinaryOperation) expr).getLeft().isIntegerConstant()
                && ((BinaryOperation) expr).getRight().isPower()
                && ((BinaryOperation) ((BinaryOperation) expr).getRight()).getRight().isRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) expr).getRight()).getRight()).getLeft().isIntegerConstant()
                && ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) expr).getRight()).getRight()).getRight().equals(Expression.TWO);
    }

    /**
     * Hilfsfunktion: Liefert, ob expr ein passender Kandidat für die
     * Rationalisierung des Nenners ist.
     */
    public static boolean isSuitableCandidateForMakingDenominatorRational(BinaryOperation expr) {
        return (expr.isSum() || expr.isDifference())
                && (isProductOfIntegerAndSqrtOfRational(expr.getLeft()) || isProductOfIntegerAndSqrtOfRational(expr.getRight()));
    }

    /**
     * Sammelt passende Faktoren via 3. binomischer Formel zusammen (dabei
     * werden containsSqrtFactor() und isSuitableCandidateForThirdBinomial()
     * benutzt).
     */
    public static Expression collectFactorsByThirdBinomialFormula(BinaryOperation expr) {

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
         additionalFactorsInEnumerator stellt ZUSÄTZLICHE Faktoren im Zähler
         dar, die durch Rationalisierung des Nenners hinzugekommen sind.
         */
        ExpressionCollection additionalFactorsInEnumerator = new ExpressionCollection();

        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i) instanceof BinaryOperation && isSuitableCandidateForMakingDenominatorRational((BinaryOperation) factorsDenominator.get(i))) {

                if (factorsDenominator.get(i).isSum()) {
                    additionalFactorsInEnumerator.add(((BinaryOperation) factorsDenominator.get(i)).getLeft().sub(((BinaryOperation) factorsDenominator.get(i)).getRight()));
                    factorsDenominator.put(i, ((BinaryOperation) factorsDenominator.get(i)).getLeft().pow(2).sub(((BinaryOperation) factorsDenominator.get(i)).getRight().pow(2)));
                } else if (factorsDenominator.get(i).isDifference()) {
                    additionalFactorsInEnumerator.add(((BinaryOperation) factorsDenominator.get(i)).getLeft().add(((BinaryOperation) factorsDenominator.get(i)).getRight()));
                    factorsDenominator.put(i, ((BinaryOperation) factorsDenominator.get(i)).getLeft().pow(2).sub(((BinaryOperation) factorsDenominator.get(i)).getRight().pow(2)));
                }

            }
        }

        // Ergebnis bilden.
        if (additionalFactorsInEnumerator.isEmpty()) {
            // Dann konnte im Nenner nichts rationalisiert werden.
            return expr;
        }
        /*
         Der gesamte Zähler muss als Faktor im neuen Zähler ebenfalls
         aufgenommen werden.
         */
        additionalFactorsInEnumerator.add(expr.getLeft());

        return SimplifyUtilities.produceProduct(additionalFactorsInEnumerator).div(SimplifyUtilities.produceProduct(factorsDenominator));

    }

    /**
     * Sammelt in einem Produkt verschiedene Wurzeln zu einer großen Wurzel.
     */
    public static Expression collectVariousRootsToOneCommonRoot(BinaryOperation expr) {

        if (!expr.getType().equals(TypeBinary.TIMES)) {
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

                p = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(i)).getRight()).getLeft()).getValue().toBigInteger();
                m = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(i)).getRight()).getRight()).getValue().toBigInteger();
                for (int j = i + 1; j < factors.getBound(); j++) {

                    if (factors.get(j) != null && factors.get(j).isPower()
                            && ((BinaryOperation) factors.get(j)).getLeft().isIntegerConstantOrRationalConstant()
                            && ((BinaryOperation) factors.get(j)).getRight().isRationalConstant()) {

                        q = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(j)).getRight()).getLeft()).getValue().toBigInteger();
                        n = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(j)).getRight()).getRight()).getValue().toBigInteger();
                        commonRootDegree = ArithmeticMethods.lcm(m, n);

                        // In diesem Fall werden die Potenzen von simplify() nicht vollständig ausgerechnet.
                        if (ArithmeticMethods.lcm(m, n).divide(m).multiply(p).add(ArithmeticMethods.lcm(m, n).divide(n).multiply(q)).compareTo(BigInteger.valueOf(ComputationBounds.BOUND_DEGREE_OF_COMMON_ROOT)) > 0) {
                            continue;
                        }

                        if (((BinaryOperation) factors.get(i)).getLeft().isRationalConstant()) {
                            a = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(i)).getLeft()).getLeft()).getValue().toBigInteger();
                            b = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(i)).getLeft()).getRight()).getValue().toBigInteger();
                        } else {
                            a = ((Constant) ((BinaryOperation) factors.get(i)).getLeft()).getValue().toBigInteger();
                            b = BigInteger.ONE;
                        }
                        if (((BinaryOperation) factors.get(j)).getLeft().isRationalConstant()) {
                            c = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(j)).getLeft()).getLeft()).getValue().toBigInteger();
                            d = ((Constant) ((BinaryOperation) ((BinaryOperation) factors.get(j)).getLeft()).getRight()).getValue().toBigInteger();
                        } else {
                            c = ((Constant) ((BinaryOperation) factors.get(j)).getLeft()).getValue().toBigInteger();
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

}

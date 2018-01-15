package abstractexpressions.expression.computation;

import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeOperator;
import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lang.translator.Translator;

public abstract class ArithmeticUtils {

    public static class PrimeFactorWithMultiplicity {

        private final BigInteger p;
        private final int mulitplicity;

        public PrimeFactorWithMultiplicity(BigInteger p, int mulitplicity) {
            this.p = p;
            this.mulitplicity = mulitplicity;
        }

        public BigInteger getP() {
            return p;
        }

        public int getMulitplicity() {
            return mulitplicity;
        }

    }

    /**
     * Liefert die Primfaktorzerlegung von a, falls a &#8804; eine gewisse
     * Schranke nicht überschreitet. Ansonsten werden womöglich nicht alle
     * ermittelten Faktoren prim.<br>
     * VORAUSSETZUNG: a &#8805; 0.
     */
    public static List<PrimeFactorWithMultiplicity> getPrimeDecomposition(BigInteger a) {

        BigInteger bound;
        try {
            bound = ArithmeticUtils.root(a.abs(), 2);
        } catch (EvaluationException e) {
            bound = a.abs();
        }

        bound = bound.min(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_DIVISORS_OF_INTEGERS));

        List<PrimeFactorWithMultiplicity> primeDivisors = new ArrayList<>();

        if (a.compareTo(BigInteger.ONE) <= 0) {
            return primeDivisors;
        }

        BigInteger currentPrime;
        int currentMultiplicity;
        for (int i = 2; i <= bound.intValue(); i++) {
            if (a.compareTo(BigInteger.valueOf(i)) < 0) {
                break;
            }
            currentPrime = BigInteger.valueOf(i);
            currentMultiplicity = 0;
            while (a.mod(BigInteger.valueOf(i)).compareTo(BigInteger.ZERO) == 0) {
                currentMultiplicity++;
                a = a.divide(BigInteger.valueOf(i));
            }
            if (currentMultiplicity > 0) {
                primeDivisors.add(new PrimeFactorWithMultiplicity(currentPrime, currentMultiplicity));
            }
            if (a.equals(BigInteger.ONE)) {
                break;
            }
        }

        return primeDivisors;

    }

    /**
     * Liefert alle (positiven) Teiler von a, indiziert via 0, 1, 2, ..., falls
     * a &#8804; eine gewisse Schranke. Ansonsten werden nur alle Teiler, welche
     * kleiner oder gleich der Wurzel der Schranke sind, sowie ihre
     * Komplementärteiler ermittelt.
     */
    public static List<BigInteger> getDivisors(BigInteger a) {

        BigInteger bound;
        try {
            bound = ArithmeticUtils.root(a.abs(), 2);
        } catch (EvaluationException e) {
            bound = a.abs();
        }

        bound = bound.min(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_DIVISORS_OF_INTEGERS));

        List<BigInteger> divisors = new ArrayList<>();

        if (a.compareTo(BigInteger.ZERO) == 0) {
            divisors.add(BigInteger.ONE);
            return divisors;
        }

        for (int i = 1; i <= bound.intValue(); i++) {
            if (a.mod(BigInteger.valueOf(i)).compareTo(BigInteger.ZERO) == 0) {
                divisors.add(BigInteger.valueOf(i));
            }
        }

        int numberOfDivisorsBelowSqrt = divisors.size();

        if (bound.pow(2).compareTo(a) == 0) {
            for (int i = numberOfDivisorsBelowSqrt - 2; i >= 0; i--) {
                divisors.add(a.divide((BigInteger) divisors.get(i)));
            }
        } else {
            for (int i = numberOfDivisorsBelowSqrt - 1; i >= 0; i--) {
                divisors.add(a.divide((BigInteger) divisors.get(i)));
            }
        }

        return divisors;

    }

    /**
     * Liefert alle (positiven) Teiler von a, indiziert via 0, 1, 2, ..., falls
     * a &#8804; eine gewisse Schranke. Ansonsten werden nur alle Teiler, welche
     * kleiner oder gleich der Wurzel der Schranke sind, sowie ihre
     * Komplementärteiler ermittelt.
     */
    public static List<BigInteger> getDivisors2(BigInteger a) {
        BigInteger absA = a.abs();
        List<PrimeFactorWithMultiplicity> primefactors = getPrimeDecomposition(absA);
        Set<BigInteger> divisorsSet = getDivisorsFromPrimeFactors(primefactors);
        Set<BigInteger> allDivisorsSet = new HashSet<>(divisorsSet);
        for (BigInteger d : divisorsSet) {
            allDivisorsSet.add(absA.divide(d));
        }
        List<BigInteger> divisors = new ArrayList<>(allDivisorsSet);
        Collections.sort(divisors);
        return divisors;
    }

    private static Set<BigInteger> getDivisorsFromPrimeFactors(List<PrimeFactorWithMultiplicity> computedPrimefactors) {
        Set<BigInteger> divisors = new HashSet<>();
        if (computedPrimefactors.isEmpty()) {
            divisors.add(BigInteger.ONE);
        } else {
            BigInteger p = computedPrimefactors.get(0).getP();
            int n = computedPrimefactors.get(0).getMulitplicity();
            computedPrimefactors.remove(0);
            for (int i = 0; i <= n; i++) {
                divisors.add(p.pow(i));
            }
            if (!computedPrimefactors.isEmpty()) {
                Set<BigInteger> divisorsComputedByFirstFactor = new HashSet<>(divisors);
                Set<BigInteger> divisorsOfQuotient = getDivisorsFromPrimeFactors(computedPrimefactors);
                for (BigInteger a : divisorsComputedByFirstFactor) {
                    for (BigInteger b : divisorsOfQuotient) {
                        divisors.add(a.multiply(b));
                    }
                }
            }
        }
        return divisors;
    }

    /**
     * Gibt den ggT von a und b zurück.
     */
    public static int gcd(int a, int b) {

        if (a < 0) {
            a = -a;
        }
        if (b < 0) {
            b = -b;
        }
        if (a < b) {
            return gcd(b, a);
        }

        int r = a - (a / b) * b;
        while (r != 0) {
            a = b;
            b = r;
            r = a - (a / b) * b;
        }
        return b;

    }

    /**
     * Gibt den ggT von a[0], a[1], ..., a[a.length - 1] zurück.
     */
    public static BigInteger gcd(BigInteger[] a) {

        if (a.length == 1) {
            return a[0];
        } else {
            BigInteger[] aRest = new BigInteger[a.length - 1];
            for (int i = 1; i < a.length; i++) {
                aRest[i - 1] = a[i];
            }
            return a[0].gcd(gcd(aRest));
        }

    }

    public static BigInteger gcd(List<BigInteger> a) {
        BigInteger[] result = new BigInteger[a.size()];
        return gcd(a.toArray(result));
    }

    public static Expression gcdSimplifyTrivial(List<Expression> a) {
        for (Expression argument : a) {
            if (argument.isIntegerConstant() && ((Constant) argument).getBigIntValue().abs().equals(BigInteger.ONE)) {
                return ONE;
            }
        }
        return new Operator(TypeOperator.gcd, a.toArray(new Object[1]));
    }

    /**
     * Gibt den kgV von a und b zurück.
     */
    public static BigInteger lcm(BigInteger a, BigInteger b) {
        if (a.equals(BigInteger.ZERO) && b.equals(BigInteger.ZERO)) {
            return BigInteger.ZERO;
        }
        return a.multiply(b).divide(a.gcd(b)).abs();
    }

    public static BigInteger lcm(BigInteger[] a) {

        switch (a.length) {
            case 1:
                return a[0].abs();
            case 2:
                return lcm(a[0], a[1]);
            default:
                BigInteger[] aRest = new BigInteger[a.length - 1];
                for (int i = 1; i < a.length; i++) {
                    aRest[i - 1] = a[i];
                }
                BigInteger[] aReduced = new BigInteger[2];
                aReduced[0] = a[0];
                aReduced[1] = lcm(aRest);
                return lcm(aReduced);
        }

    }

    public static BigInteger lcm(ArrayList<BigInteger> a) {
        BigInteger[] result = new BigInteger[a.size()];
        return lcm(a.toArray(result));
    }

    public static Expression lcmSimplifyTrivial(ArrayList<Expression> a) {
        for (int i = 0; i < a.size(); i++) {
            if (a.size() > 1 && a.get(i).isIntegerConstant()
                    && ((Constant) a.get(i)).getBigIntValue().abs().equals(BigInteger.ONE)) {
                a.remove(i);
                i--;
            }
            if (a.get(i).equals(ZERO)) {
                return ZERO;
            }
        }
        return new Operator(TypeOperator.gcd, a.toArray(new Object[1]));
    }

    /**
     * Gibt den Divisionsrest von a bei ganzzahliger Division durch m zurück.
     *
     * @throws EvaluationException
     */
    public static BigInteger mod(BigInteger a, BigInteger m) throws EvaluationException {
        if (m.compareTo(BigInteger.ZERO) <= 0) {
            throw new EvaluationException(Translator.translateOutputMessage("CC_ArithmeticMethods_SECOND_PARAMETER_IN_MOD_IS_NON_POSITIVE"));
        }
        return a.mod(m);
    }

    public static BigInteger modpow(BigInteger a, BigInteger b, BigInteger m) throws EvaluationException {
        if (b.compareTo(BigInteger.ZERO) <= 0) {
            throw new EvaluationException(Translator.translateOutputMessage("CC_ArithmeticMethods_SECOND_PARAMETER_IN_MODPOW_IS_NON_POSITIVE"));
        }
        if (m.compareTo(BigInteger.ZERO) <= 0) {
            throw new EvaluationException(Translator.translateOutputMessage("CC_ArithmeticMethods_THIRD_PARAMETER_IN_MODPOW_IS_NON_POSITIVE"));
        }
        return a.modPow(b, m);
    }

    /**
     * Gibt a! zurück.
     */
    public static BigInteger factorial(int a) {

        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= a; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;

    }

    /**
     * Gibt Bin(n, k) := n!/(k!*(n - k)!), 0 &#8804; k &#8804; n, zurück.
     */
    public static BigInteger bin(int n, int k) {
        BigInteger result = BigInteger.ZERO;
        if (k > n / 2) {
            return bin(n, n - k);
        }
        if (k >= 0 && k <= n) {
            result = BigInteger.ONE;
            for (int i = 1; i <= k; i++) {
                result = result.multiply(BigInteger.valueOf(n + 1 - i)).divide(BigInteger.valueOf(i));
            }
        }
        return result;
    }

    /**
     * Liefert die größte Zahl b mit b^n &#8804; a. Falls die n-te Wurzel aus a
     * also ganzzahlig ist, gilt b^n = a.
     *
     * @throws EvaluationException
     */
    public static BigInteger root(BigInteger a, int n) throws EvaluationException {

        if (a.compareTo(BigInteger.ZERO) < 0 && n % 2 == 0) {
            throw new EvaluationException(Translator.translateOutputMessage("CC_ArithmeticMethods_ROOTS_OF_EVEN_ORDER_DO_NOT_EXIST", a));
        }

        if (a.compareTo(BigInteger.ZERO) == 0) {
            return BigInteger.ZERO;
        }

        int exp = 0;
        if (a.compareTo(BigInteger.ZERO) > 0) {
            while (BigInteger.TEN.pow(n * exp).compareTo(a) <= 0) {
                exp++;
            }
            exp--;
            BigInteger result = BigInteger.TEN.pow(exp);
            BigInteger current_digit;
            int current_exp = exp;
            for (int i = exp; i >= 0; i--) {
                current_digit = BigInteger.ZERO;
                while (result.add(current_digit.multiply(BigInteger.TEN.pow(current_exp))).pow(n).compareTo(a) <= 0) {
                    current_digit = current_digit.add(BigInteger.ONE);
                }
                current_digit = current_digit.subtract(BigInteger.ONE);
                result = result.add(current_digit.multiply(BigInteger.TEN.pow(current_exp)));
                current_exp--;
            }
            return result;
        } else {
            return BigInteger.ONE.negate().multiply(root(a.multiply(BigInteger.ONE.negate()), n));
        }

    }

}

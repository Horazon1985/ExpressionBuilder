package abstractexpressions.expression.computation;

import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import java.math.BigInteger;
import java.util.ArrayList;
import lang.translator.Translator;

public abstract class ArithmeticMethods {

    /**
     * Liefert die Primfaktorzerlegung von a, falls a <= 1000000. Sonst: alle
     * Primfaktoren (mit Vielfachheiten) von a, die <= 1000 sind.<br>
     * VORAUSSETZUNG: a >= 0.
     */
    public static ArrayList<BigInteger> getPrimeDecomposition(BigInteger a) {

        BigInteger bound;
        try {
            bound = ArithmeticMethods.root(a.abs(), 2);
        } catch (EvaluationException e) {
            bound = a.abs();
        }
        
        bound = bound.min(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_DIVISORS_OF_INTEGERS));

        ArrayList<BigInteger> primeDivisors = new ArrayList<>();

        if (a.compareTo(BigInteger.ONE) <= 0) {
            return primeDivisors;
        }

        for (int i = 1; i <= bound.intValue(); i++) {
            if (a.compareTo(BigInteger.valueOf(i)) < 0){
                break;
            }
            if (a.mod(BigInteger.valueOf(i)).compareTo(BigInteger.ZERO) == 0) {
                primeDivisors.add(BigInteger.valueOf(i));
                a = a.divide(BigInteger.valueOf(i));
                i--;
            }
        }

        return primeDivisors;

    }

    /**
     * Liefert alle (positiven) Teiler von a, indiziert via 0, 1, 2, ..., falls
     * a <= 1000000. Sonst: nur Teiler <= 1000 sowie ihre Komplementärteiler
     * ermitteln.
     */
    public static ArrayList<BigInteger> getDivisors(BigInteger a) {

        BigInteger bound;
        try {
            bound = ArithmeticMethods.root(a.abs(), 2);
        } catch (EvaluationException e) {
            bound = a.abs();
        }
        
        bound = bound.min(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_DIVISORS_OF_INTEGERS));

        ArrayList<BigInteger> divisors = new ArrayList<>();

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
            BigInteger[] a_rest = new BigInteger[a.length - 1];
            for (int i = 1; i < a.length; i++) {
                a_rest[i - 1] = a[i];
            }
            return a[0].gcd(gcd(a_rest));
        }

    }

    public static BigInteger gcd(ArrayList<BigInteger> a) {
        BigInteger[] result = new BigInteger[a.size()];
        return gcd(a.toArray(result));
    }

    /**
     * Gibt den kgV von a und b zurück.
     */
    public static BigInteger lcm(BigInteger a, BigInteger b) {
        return a.multiply(b).divide(a.gcd(b)).abs();
    }

    public static BigInteger lcm(BigInteger[] a) {

        if (a.length == 1) {
            return a[0].abs();
        } else if (a.length == 2) {
            return a[0].multiply(a[1]).divide(gcd(a)).abs();
        } else {
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
     * Gibt Bin(n, k) := n!/(k!*(n - k)!), 0 <= k <= n, zurück.
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
     * Liefert die größte Zahl b mit b^n <= a. Falls die n-te Wurzel aus a also
     * ganzzahlig ist, gilt b^n = a.
     *
     * @throws EvaluationException
     */
    public static BigInteger root(BigInteger a, int n) throws EvaluationException {

        if (a.compareTo(BigInteger.ZERO) < 0 && (n / 2) * 2 == n) {
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

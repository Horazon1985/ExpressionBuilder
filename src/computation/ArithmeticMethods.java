package computation;

import exceptions.EvaluationException;
import java.math.BigInteger;
import java.util.HashMap;
import translator.Translator;

public abstract class ArithmeticMethods {

    /**
     * Liefert alle (positiven) Teiler von a, indiziert via 0, 1, 2, ..., falls
     * a <= 1000000. Sonst: leere Menge.
     */
    public static HashMap<Integer, BigInteger> getDivisors(BigInteger a) {

        HashMap<Integer, BigInteger> result = new HashMap<>();
        if (a.abs().compareTo(BigInteger.valueOf(1000000)) > 0) {
            return result;
        }

        if (a.compareTo(BigInteger.ZERO) == 0) {
            result.put(0, BigInteger.ONE);
            return result;
        }

        BigInteger sqrt;
        try {
            sqrt = ArithmeticMethods.sqrt(a.abs(), 2);
        } catch (EvaluationException e) {
            sqrt = a.abs();
        }

        for (int i = 1; i <= sqrt.intValue(); i++) {
            if (a.mod(new BigInteger(String.valueOf(i))).compareTo(BigInteger.ZERO) == 0) {
                result.put(result.size(), new BigInteger(String.valueOf(i)));
            }
        }

        int numberOfDivisorsBelowSqrt = result.size();

        if (sqrt.pow(2).compareTo(a) == 0) {
            for (int i = numberOfDivisorsBelowSqrt - 2; i >= 0; i--) {
                result.put(result.size(), a.divide((BigInteger) result.get(i)));
            }
        } else {
            for (int i = numberOfDivisorsBelowSqrt - 1; i >= 0; i--) {
                result.put(result.size(), a.divide((BigInteger) result.get(i)));
            }
        }

        return result;

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
            BigInteger[] a_rest = new BigInteger[a.length - 1];
            for (int i = 1; i < a.length; i++) {
                a_rest[i - 1] = a[i];
            }
            BigInteger[] a_reduced = new BigInteger[2];
            a_reduced[0] = a[0];
            a_reduced[1] = lcm(a_rest);
            return lcm(a_reduced);
        }

    }

    /**
     * Gibt den Divisionsrest von a bei ganzzahliger Division durch m zurück.
     *
     * @throws EvaluationException
     */
    public static BigInteger mod(BigInteger a, BigInteger m) throws EvaluationException {
        if (m.compareTo(BigInteger.ZERO) <= 0) {
            throw new EvaluationException(Translator.translateExceptionMessage("CC_ArithmeticMethods_SECOND_PARAMETER_IN_MOD_IS_NON_POSITIVE"));
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
     * Liefert die größte Zahl b mit b^n <= a. Falls die n-te Wurzel aus a also
     * ganzzahlig ist, gilt b^n = a.
     * 
     * @throws EvaluationException
     */
    public static BigInteger sqrt(BigInteger a, int n) throws EvaluationException {

        if (a.compareTo(BigInteger.ZERO) < 0 && (n / 2) * 2 == n) {
            throw new EvaluationException(Translator.translateExceptionMessage("CC_ArithmeticMethods_ROOTS_OF_EVEN_ORDER_DO_NOT_EXIST_1")
                    + a.toString()
                    + Translator.translateExceptionMessage("CC_ArithmeticMethods_ROOTS_OF_EVEN_ORDER_DO_NOT_EXIST_2"));
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
            return BigInteger.ONE.negate().multiply(sqrt(a.multiply(BigInteger.ONE.negate()), n));
        }

    }

}

package expression.computationtests;

import abstractexpressions.expression.computation.ArithmeticUtils;
import abstractexpressions.expression.computation.ArithmeticUtils.PrimeFactorWithMultiplicity;
import basic.MathToolTestBase;
import java.math.BigInteger;
import java.util.List;
import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ArithmeticTests extends MathToolTestBase {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void defineExpressions() throws Exception {
    }

    @Test
    public void computePrimeDecompositionTest() {
        BigInteger a = BigInteger.valueOf(739800864);
        List<PrimeFactorWithMultiplicity> factors = ArithmeticUtils.getPrimeDecomposition(a);
        results = new Object[]{factors.size(), factors.get(0).getP(), factors.get(0).getMulitplicity(),
            factors.get(1).getP(), factors.get(1).getMulitplicity(),
            factors.get(2).getP(), factors.get(2).getMulitplicity(),
            factors.get(3).getP(), factors.get(3).getMulitplicity()};
        expectedResults = new Object[]{4,
            BigInteger.valueOf(2), 5,
            BigInteger.valueOf(3), 7,
            BigInteger.valueOf(11), 1,
            BigInteger.valueOf(31), 2};
        Assert.assertEquals(4, factors.size());
        Assert.assertEquals(expectedResults, results);
    }

    @Test
    public void computeDivisorsTest() {
        BigInteger a = BigInteger.valueOf(144);
        List<BigInteger> divisors = ArithmeticUtils.getDivisors(a);
        results = new Object[]{divisors.size(),
            divisors.get(0), divisors.get(1), divisors.get(2), divisors.get(3), divisors.get(4),
            divisors.get(5), divisors.get(6), divisors.get(7), divisors.get(8), divisors.get(9),
            divisors.get(10), divisors.get(11), divisors.get(12), divisors.get(13), divisors.get(14)};
        expectedResults = new Object[]{15,
            BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(3), BigInteger.valueOf(4), BigInteger.valueOf(6),
            BigInteger.valueOf(8), BigInteger.valueOf(9), BigInteger.valueOf(12), BigInteger.valueOf(16), BigInteger.valueOf(18),
            BigInteger.valueOf(24), BigInteger.valueOf(36), BigInteger.valueOf(48), BigInteger.valueOf(72), BigInteger.valueOf(144)};
        Assert.assertEquals(15, divisors.size());
        Assert.assertEquals(expectedResults, results);
    }

}

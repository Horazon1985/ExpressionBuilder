package simplifymethodstest.polynomialtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import expressionbuilder.Expression;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyPolynomialMethods;
import expressionsimplifymethods.SimplifyUtilities;
import java.math.BigInteger;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

public class PolynomialTests {

    Expression f, g, fFactorized, ggT;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void periodicCoefficientsTest1() {
        ExpressionCollection c = new ExpressionCollection(2, 1, -4, 2, 1, -4);
        Assert.assertTrue(SimplifyPolynomialMethods.getPeriodOfCoefficients(c) == 3);
    }

    @Test
    public void periodicCoefficientsTest2() {
        ExpressionCollection c = new ExpressionCollection(2, 1, -4, 2, 7, -4);
        Assert.assertTrue(SimplifyPolynomialMethods.getPeriodOfCoefficients(c) == 6);
    }

    @Test
    public void antiperiodicCoefficientsTest1() {
        ExpressionCollection c = new ExpressionCollection(2, 1, -4, -2, -1, 4, 2, 1, 4);
        Assert.assertTrue(SimplifyPolynomialMethods.getAntiperiodOfCoefficients(c) == 3);
    }

    // Tests für Polynomdivision.
    @Test
    public void polynomialDivisionTest1() {
        // f = ??? und g = ???.
        // Quotient = ??? und Rest = ???.
        try {
            f = Expression.build("6+5*x+x^2", null);
            g = Expression.build("3+13*x+7*x^2+x^3", null);
            ggT = Expression.build("3+x", null);
            Expression expectedResult = SimplifyPolynomialMethods.getGGTOfPolynomials(f, g, "x");
            Assert.assertTrue(expectedResult.equivalent(ggT));
        } catch (ExpressionException e) {
            fail("f konnte nicht vereinfacht werden.");
        }
    }

    // Tests für die Berechnung des ggT von Polynomen.
    @Test
    public void getGGTOfPolynomialsTest1() {
        // ggT von f = 6+5*x+x^2 ( = (3+x)*(2+x)) und g = 3+13*x+7*x^2+x^3 ( = (3+x)*(1+4*x+x^2)).
        // ggT = 3+x.
        try {
            f = Expression.build("6+5*x+x^2", null);
            g = Expression.build("3+13*x+7*x^2+x^3", null);
            ggT = Expression.build("3+x", null);
            Expression expectedResult = SimplifyPolynomialMethods.getGGTOfPolynomials(f, g, "x");
            Assert.assertTrue(expectedResult.equivalent(ggT));
        } catch (ExpressionException e) {
            fail("f konnte nicht vereinfacht werden.");
        }
    }

    // Tests für die Berechnung des ggT von Polynomen.
    @Test
    public void getGGTOfPolynomialsTest2() {
        /* 
         ggT von f = 64+244*x^2+41*x^4+352*x+136*x^3+x^6+9*x^5 ( = (x^2+5*x+1)*(x^2+2*x+8)^2) 
         und g = x^5+4*x^3-(256+48*x^2+64*x) ( = (x-4)*(x^2+2*x+8)^2).
         */
        // ggT = 64+20*x^2+x^4+32*x+4*x^3 ( = (x^2+2*x+8)^2).
        try {
            f = Expression.build("64+244*x^2+41*x^4+352*x+136*x^3+x^6+9*x^5", null);
            g = Expression.build("x^5+4*x^3-(256+48*x^2+64*x)", null);
            ggT = Expression.build("64+20*x^2+x^4+32*x+4*x^3", null);
            Expression expectedResult = SimplifyPolynomialMethods.getGGTOfPolynomials(f, g, "x");
            Assert.assertTrue(expectedResult.equivalent(ggT));
        } catch (ExpressionException e) {
            fail("f konnte nicht vereinfacht werden.");
        }
    }

    // Tests für Polynomfaktorisierung.
    @Test
    public void decomposePolynomialTest1() {
        // Zerlegung von x^5-7 in irreduzible Faktoren.
        try {
            f = Expression.build("x^5-7", null);
            fFactorized = Expression.build("(x-7^(1/5))*((x^2+7^(2/5))-2*7^(1/5)*(-1/4-5^(1/2)/4)*x)*((x^2+7^(2/5))-2*7^(1/5)*(5^(1/2)/4-1/4)*x)", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail("f konnte nicht vereinfacht werden.");
        }
    }

    @Test
    public void decomposePolynomialTest2() {
        // Zerlegung von 4*x^5-7*x^4+2*x^3+4*x^2-7*x+2 in irreduzible Faktoren.
        try {
            f = Expression.build("4*x^5+3*x^4+25*x^3+4*x^2+3*x+25", null);
            fFactorized = Expression.build("(4*x^2+3*x+25)*(1+x)*((1+x^2)-x)", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail("f konnte nicht vereinfacht werden.");
        }
    }

    @Test
    public void decomposePolynomialTest3() {
        // Zerlegung von 12+2*x+x^2-12*x^3-2*x^4-x^5+12*x^6+2*x^7+x^8 in irreduzible Faktoren.
        try {
            f = Expression.build("12+2*x+x^2-12*x^3-2*x^4-x^5+12*x^6+2*x^7+x^8", null);
            fFactorized = Expression.build("(12+2*x+x^2)*(1-x^3+x^6)", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail("f konnte nicht vereinfacht werden.");
        }
    }

    @Test
    public void decomposePolynomialTest4() {
        // Zerlegung von x^3+5*x+7 in irreduzible Faktoren.
        try {
            f = Expression.build("x^3+5*x+7", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            Assert.assertTrue(factors.getBound() == 2);
            Assert.assertTrue(SimplifyPolynomialMethods.degreeOfPolynomial(factors.get(0), "x").compareTo(BigInteger.ONE) == 0);
            Assert.assertTrue(SimplifyPolynomialMethods.degreeOfPolynomial(factors.get(1), "x").compareTo(BigInteger.valueOf(2)) == 0);
        } catch (ExpressionException | EvaluationException e) {
            fail("f konnte nicht vereinfacht werden.");
        }
    }

}

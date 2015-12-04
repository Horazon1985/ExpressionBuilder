package simplifymethodstest.polynomialtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.MINUS_ONE;
import static expressionbuilder.Expression.ONE;
import static expressionbuilder.Expression.TWO;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyPolynomialMethods;
import static expressionsimplifymethods.SimplifyTrigonometry.THREE;
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
    public void getPolynomialCoefficientsTest1() {
        try {
            f = Expression.build("(1+x)^2-(x-1)^2", null);
            ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(f, "x");
            ExpressionCollection coefficientsExpected = new ExpressionCollection(0, 4);
            Assert.assertTrue(coefficients.equals(coefficientsExpected));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getPolynomialCoefficientsTest2() {
        try {
            f = Expression.build("(1/2+x)^3-x^2", null);
            ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(f, "x");
            ExpressionCollection coefficientsExpected = new ExpressionCollection(ONE.div(8), THREE.div(4), ONE.div(TWO), ONE);
            Assert.assertTrue(coefficients.equals(coefficientsExpected));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getPolynomialCoefficientsTest3() {
        try {
            f = Expression.build("(2+x/3)^3+(1+x)^5-3*x^5", null);
            ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(f, "x");
            ExpressionCollection coefficientsExpected = new ExpressionCollection(9, 9, "32/3", "271/27", 5, -2);
            Assert.assertTrue(coefficients.equals(coefficientsExpected));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
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
    public void periodicCoefficientsEmptyTest() {
        ExpressionCollection c = new ExpressionCollection();
        Assert.assertTrue(SimplifyPolynomialMethods.getPeriodOfCoefficients(c) == 0);
    }

    @Test
    public void antiperiodicCoefficientsTest1() {
        ExpressionCollection c = new ExpressionCollection(2, 1, -4, -2, -1, 4, 2, 1, -4);
        Assert.assertTrue(SimplifyPolynomialMethods.getAntiperiodOfCoefficients(c) == 3);
    }

    @Test
    public void antiperiodicCoefficientsTest2() {
        ExpressionCollection c = new ExpressionCollection(5, 1, -5, -1, 5, 1, -5, -1);
        Assert.assertTrue(SimplifyPolynomialMethods.getAntiperiodOfCoefficients(c) == 2);
    }

    @Test
    public void antiperiodicCoefficientsTest3() {
        ExpressionCollection c = new ExpressionCollection(4, 3, -8, 2);
        Assert.assertTrue(SimplifyPolynomialMethods.getAntiperiodOfCoefficients(c) == 4);
    }

    @Test
    public void antiperiodicCoefficientsEmptyTest() {
        ExpressionCollection c = new ExpressionCollection();
        Assert.assertTrue(SimplifyPolynomialMethods.getAntiperiodOfCoefficients(c) == 0);
    }

    // Tests für Polynomdivision.
    @Test
    public void polynomialDivisionWithRestTest() {
        // f = 2*x^5+3*x^4+x^3/3-x^2+6*x+10 und g = 5*x^2+x-1/2.
        // Quotient = 2*x^3/5+13*x^2/25+x/375-557/3750 und Rest = 11531*x/1875+74443/7500.
        try {
            ExpressionCollection coefficientsF = new ExpressionCollection(10, 6, -1, ONE.div(THREE), 3, 2);
            ExpressionCollection coefficientsG = new ExpressionCollection(MINUS_ONE.div(TWO), 1, 5);
            ExpressionCollection[] divisionResult = SimplifyPolynomialMethods.polynomialDivision(coefficientsF, coefficientsG);
            ExpressionCollection expectedQuotient = new ExpressionCollection(new Constant(-557).div(3750), ONE.div(375), new Constant(13).div(25), TWO.div(5));
            ExpressionCollection expectedRest = new ExpressionCollection(new Constant(74443).div(7500), new Constant(11531).div(1875));
            Assert.assertTrue(divisionResult.length == 2);
            Assert.assertTrue(divisionResult[0].equals(expectedQuotient));
            Assert.assertTrue(divisionResult[1].equals(expectedRest));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void polynomialDivisionDegEnumeratorIsSmallerThanDegDenominatorTest() {
        // f = x^3+5*x^2+1 und g = x^5-4*x^3+2.
        // Quotient = 0 und Rest = x^3+5*x^2+1.
        try {
            ExpressionCollection coefficientsF = new ExpressionCollection(1, 0, 5, 3);
            ExpressionCollection coefficientsG = new ExpressionCollection(2, 0, 0, -4, 0, 1);
            ExpressionCollection[] divisionResult = SimplifyPolynomialMethods.polynomialDivision(coefficientsF, coefficientsG);
            ExpressionCollection expectedQuotient = new ExpressionCollection();
            ExpressionCollection expectedRest = new ExpressionCollection(1, 0, 5, 3);
            Assert.assertTrue(divisionResult.length == 2);
            Assert.assertTrue(divisionResult[0].equals(expectedQuotient));
            Assert.assertTrue(divisionResult[1].equals(expectedRest));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void polynomialDivisionWithoutRestTest() {
        // f = (7*x^2)/2+x^6+15*x^3+x^5-(7/2+(11*x)/2+13*x^4) und g = x^2+3*x-7.
        // Quotient = x^4-2*x^3+x+1/2 und Rest = 0.
        try {
            ExpressionCollection coefficientsF = new ExpressionCollection(new Constant(-7).div(2), new Constant(-11).div(2),
                    new Constant(7).div(2), 15, -13, 1, 1);
            ExpressionCollection coefficientsG = new ExpressionCollection(-7, 3, 1);
            ExpressionCollection[] divisionResult = SimplifyPolynomialMethods.polynomialDivision(coefficientsF, coefficientsG);
            ExpressionCollection expectedQuotient = new ExpressionCollection(ONE.div(2), 1, 0, -2, 1);
            ExpressionCollection expectedRest = new ExpressionCollection();
            Assert.assertTrue(divisionResult.length == 2);
            Assert.assertTrue(divisionResult[0].equals(expectedQuotient));
            Assert.assertTrue(divisionResult[1].equals(expectedRest));
        } catch (EvaluationException e) {
            fail(e.getMessage());
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
            fail(e.getMessage());
        }
    }

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
            fail(e.getMessage());
        }
    }

    @Test
    public void getGGTOfPolynomialsIsTrivialTest() {
        // ggT von f = x^3+5*x^2-7*x-4 und g = 2*x^2+4*x-18 ist = 1.
        try {
            f = Expression.build("x^3+5*x^2-7*x-4", null);
            g = Expression.build("2*x^2+4*x-18", null);
            ggT = ONE;
            Expression expectedResult = SimplifyPolynomialMethods.getGGTOfPolynomials(f, g, "x");
            Assert.assertTrue(expectedResult.equals(ggT));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    // Tests für Polynomfaktorisierung.
    @Test
    public void decomposeCyclicPolynomialTest1() {
        // Zerlegung von x^5-7 in irreduzible Faktoren.
        try {
            f = Expression.build("x^5-7", null);
            fFactorized = Expression.build("(x-7^(1/5))*((x^2+7^(2/5))+(7^(1/5)*(1+5^(1/2))*x)/2)*((x^2+7^(2/5))-(7^(1/5)*(5^(1/2)-1)*x)/2)", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposeCyclicPolynomialTest2() {
        // Zerlegung von x^4+1 in irreduzible Faktoren.
        try {
            f = Expression.build("x^4+1", null);
            fFactorized = Expression.build("(x^2+2^(1/2)*x+1)*((x^2+1)-2^(1/2)*x)", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposeCyclicPolynomialTest3() {
        // Zerlegung von x^3+1 in irreduzible Faktoren.
        try {
            f = Expression.build("x^3+1", null);
            fFactorized = Expression.build("(x+1)*((1+x^2)-x)", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposeGeometricPolynomialOfEvenDegreeTest() {
        // Zerlegung von 1+x+x^2+x^3+x^4 in irreduzible Faktoren.
        try {
            f = Expression.build("1+x+x^2+x^3+x^4", null);
            fFactorized = Expression.build("((1+x^2)+((1-5^(1/2))*x)/2)*(1+x^2+((5^(1/2)+1)*x)/2)", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void decomposeGeometricPolynomialOfOddDegreeTest() {
        // Zerlegung von 1+x+x^2+x^3+x^4+x^5 in irreduzible Faktoren.
        try {
            f = Expression.build("1+x+x^2+x^3+x^4+x^5", null);
            fFactorized = Expression.build("(1+x)*((1+x^2)-x)*(1+x+x^2)", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void decomposePolynomialTest2() {
        // Zerlegung von 4*x^5-7*x^4+2*x^3+4*x^2-7*x+2 in irreduzible Faktoren.
        try {
            f = Expression.build("4*x^5+3*x^4+25*x^3+4*x^2+3*x+25", null);
            fFactorized = Expression.build("4*(x^2+(3*x)/4+25/4)*(1+x)*((1+x^2)-x)", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest3() {
        // Zerlegung von 12+2*x+x^2-12*x^3-2*x^4-x^5+12*x^6+2*x^7+x^8 in irreduzible Faktoren.
        try {
            f = Expression.build("12+2*x+x^2-12*x^3-2*x^4-x^5+12*x^6+2*x^7+x^8", null);
            fFactorized = Expression.build("(12+2*x+x^2)*((1+x^6)-x^3)", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x").orderDifferencesAndQuotients();
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
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
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest5() {
        // Zerlegung von f = 144+33*x^2+x^4+72*x+6*x^3 = (x^2+3*x+12)^2 in irreduzible Faktoren.
        try {
            f = Expression.build("144+33*x^2+x^4+72*x+6*x^3", null);
            fFactorized = Expression.build("(x^2+3*x+12)^2", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest6() {
        /* 
         Zerlegung von f = 248832+259200*x^2+61020*x^4+5085*x^6+150*x^8+x^10
         +311040*x+142560*x^3+19683*x^5+990*x^7+15*x^9 = (x^2+3*x+12)^5 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("248832+259200*x^2+61020*x^4+5085*x^6+150*x^8+x^10+311040*x+142560*x^3+19683*x^5+990*x^7+15*x^9", null);
            fFactorized = Expression.build("(x^2+3*x+12)^5", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest7() {
        /* 
         Zerlegung von f = 216+585*x^2+275*x^4+35*x^6+459*x+469*x^3+113*x^5+x^8+7*x^7 = (x^2+x+8)*(x^2+2*x+3)^3 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("216+585*x^2+275*x^4+35*x^6+459*x+469*x^3+113*x^5+x^8+7*x^7", null);
            fFactorized = Expression.build("(x^2+x+8)*(x^2+2*x+3)^3", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest8() {
        /* 
         Zerlegung von f = 2+4*x^2+2*x^4 = 2*(x^2+1)^2 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("2+4*x^2+2*x^4", null);
            fFactorized = Expression.build("2*(1+x^2)^2", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest9() {
        /* 
         Zerlegung von f = 32+178*x^2+259*x^4+165*x^6+80*x+218*x^3+197*x^5+56*x^8+82*x^7+11*x^10+16*x^9+x^12+x^11 = (x^2+2)*(x^2-x+4)^2*(x^2+x+1)^3 in irreduzible Faktoren.
         */
        try {
            f = Expression.build("32+178*x^2+259*x^4+165*x^6+80*x+218*x^3+197*x^5+56*x^8+82*x^7+11*x^10+16*x^9+x^12+x^11", null);
            fFactorized = Expression.build("(x^2+2)*((4+x^2)-x)^2*(x^2+x+1)^3", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void decomposePolynomialTest10() {
        /* 
         Zerlegung von f = 14+93*x+125*x^2+51*x^3+5*x^4 = 5*(x+1)*(x+2)*(x+7)*(x+1/5) in irreduzible Faktoren.
         */
        try {
            f = Expression.build("14+93*x+125*x^2+51*x^3+5*x^4", null);
            fFactorized = Expression.build("5*(x+1)*(x+2)*(x+7)*(x+1/5)", null);
            f = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(f, "x");
            Assert.assertTrue(f.equivalent(fFactorized));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
}

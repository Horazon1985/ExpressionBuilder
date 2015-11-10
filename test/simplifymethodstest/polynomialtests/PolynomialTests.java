package simplifymethodstest.polynomialtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import expressionbuilder.Expression;
import expressionsimplifymethods.SimplifyPolynomialMethods;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

public class PolynomialTests {

    Expression f, fFactorized;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    // Tests für triviale Vereinfachung.
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
    
    
}

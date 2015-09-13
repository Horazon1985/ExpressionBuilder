package simplifymethodstest;

import expressionbuilder.Expression;
import java.util.HashSet;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import solveequationmethods.SpecialEquationMethods;

public class SpecialEquationMethodsTest {

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
    public void isRationalExponentialEquationTest() {
        try {
            // Test: e^(2*x)+e^(7*x+1)/(2-exp(4*x))^3 ist eine rationale Exponentialgleichung.
            Expression f = Expression.build("exp(2*x)+exp(7*x+1)/(2-exp(4*x))^3", new HashSet<String>());
            HashSet<Expression> factorsOfVar = new HashSet<>();
            assertTrue(SpecialEquationMethods.isRationalFunktionInExp(f, "x", factorsOfVar));
            System.out.println(factorsOfVar);
            // Test: 5^x/(25^(x+3)-14)-12*625^x ist eine rationale Exponentialgleichung.
            Expression g = Expression.build("5^x/(25^(x+3)-14)-12*625^x", new HashSet<String>());
            factorsOfVar.clear();
            assertTrue(SpecialEquationMethods.isRationalFunktionInExp(g, "x", factorsOfVar));
            System.out.println(factorsOfVar);
            // Test: 7^x+2*10^x ist keine rationale Exponentialgleichung.
            Expression h = Expression.build("7^x+2*10^x", new HashSet<String>());
            factorsOfVar.clear();
            Assert.assertFalse(SpecialEquationMethods.isRationalFunktionInExp(h, "x", factorsOfVar));
            System.out.println(factorsOfVar);
        } catch (expressionbuilder.ExpressionException e) {
            fail("f konnte nicht vereinfacht werden.");
        }
    }

}

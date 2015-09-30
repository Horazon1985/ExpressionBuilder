package simplifymethodstest.solvetests;

import expressionbuilder.Constant;
import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import expressionbuilder.ExpressionException;
import expressionsimplifymethods.ExpressionCollection;
import java.util.HashSet;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import solveequationmethods.SolveMethods;
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
            // Test: e^(2*x)+e^(7*x+1)/(2-e^(4*x))^3 ist eine rationale Exponentialgleichung.
            Expression f = Expression.build("exp(2*x)+exp(7*x+1)/(2-exp(4*x))^3", null);
            f = SpecialEquationMethods.separateConstantPartsInRationalExponentialEquations(f, "x");
            HashSet<Expression> argumentsOfExp = new HashSet<>();
            assertTrue(SpecialEquationMethods.isRationalFunktionInExp(f, "x", argumentsOfExp));
            System.out.println(f);
            System.out.println(argumentsOfExp);
            // Test: 5^x/(25^(x+3)-14)-12*625^x ist eine rationale Exponentialgleichung.
            Expression g = Expression.build("5^x/(25^(x+3)-14)-12*625^x", null);
            g = SpecialEquationMethods.separateConstantPartsInRationalExponentialEquations(g, "x");
            argumentsOfExp.clear();
            assertTrue(SpecialEquationMethods.isRationalFunktionInExp(g, "x", argumentsOfExp));
            System.out.println(g);
            System.out.println(argumentsOfExp);
            // Test: 7^x+2*10^x ist keine rationale Exponentialgleichung.
            Expression h = Expression.build("7^x+2*10^x", null);
            h = SpecialEquationMethods.separateConstantPartsInRationalExponentialEquations(h, "x");
            argumentsOfExp.clear();
            Assert.assertFalse(SpecialEquationMethods.isRationalFunktionInExp(h, "x", argumentsOfExp));
            System.out.println(h);
            System.out.println(argumentsOfExp);
        } catch (expressionbuilder.ExpressionException e) {
            fail("f konnte nicht vereinfacht werden.");
        }
    }

    @Test
    public void isGeneralRationalExponentialEquationTest() {
        try {
            // Test: a^x+a^(2*x)-30 ist eine rationale Exponentialgleichung.
            Expression f = Expression.build("a^x+a^(2*x)-30", null);
            f = SpecialEquationMethods.separateConstantPartsInRationalExponentialEquations(f, "x");
            HashSet<Expression> argumentsOfExp = new HashSet<>();
            assertTrue(SpecialEquationMethods.isRationalFunktionInExp(f, "x", argumentsOfExp));
            System.out.println(f);
            System.out.println(argumentsOfExp);
        } catch (expressionbuilder.ExpressionException e) {
            fail("f konnte nicht vereinfacht werden.");
        }
    }
    
    @Test
    public void solveExponentialEquationTest1() {
        try {
            // Test: f = e^(3*x) - 20*e^(2*x) + 101*e^(x) - 130 = 0. Lösungen sind ln(2), ln(5), ln(13).
            Expression f = Expression.build("exp(3*x)-20*exp(2*x)+101*exp(x)-130", null);
            SolveMethods.setSolveTries(100);
            ExpressionCollection zeros = SpecialEquationMethods.solveExponentialEquation(f, "x");
            assertTrue(zeros.getBound() == 3);
            assertTrue(zeros.contains(new Constant(2).ln()));
            assertTrue(zeros.contains(new Constant(5).ln()));
            assertTrue(zeros.contains(new Constant(13).ln()));
        } catch (ExpressionException e) {
            fail("f konnte nicht vereinfacht werden.");
        } catch (EvaluationException e) {
            fail("Die Gleichung f = 0 konnte nicht gelöst werden.");
        }
    }
    
    @Test
    public void solveExponentialEquationTest2() {
        try {
            // Test: f = exp(x) + exp(2^(1/2)*x) = 10. Keine algebraischen Lösungen.
            Expression f = Expression.build("exp(x) + exp(2^(1/2)*x) - 10", null);
            SolveMethods.setSolveTries(100);
            ExpressionCollection zeros = SpecialEquationMethods.solveExponentialEquation(f, "x");
            assertTrue(zeros.getBound() == 0);
        } catch (ExpressionException e) {
            fail("f konnte nicht vereinfacht werden.");
        } catch (EvaluationException e) {
            fail("Die Gleichung f = 0 konnte nicht gelöst werden.");
        }
    }
    
    @Test
    public void solveExponentialEquationTest3() {
        try {
            // Test: f = 5^x + 3*25^x - 8/25 = 0. Lösung ist x = -1.
            Expression f = Expression.build("5^x + 3*25^x - 8/25", null);
            SolveMethods.setSolveTries(100);
            ExpressionCollection zeros = SpecialEquationMethods.solveExponentialEquation(f, "x");
            assertTrue(zeros.getBound() == 1);
            assertTrue(zeros.contains(Expression.MINUS_ONE));
        } catch (ExpressionException e) {
            fail("f konnte nicht vereinfacht werden.");
        } catch (EvaluationException e) {
            fail("Die Gleichung f = 0 konnte nicht gelöst werden.");
        }
    }
    
}

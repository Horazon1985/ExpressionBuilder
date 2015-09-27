package simplifymethodstest.solvetests;

import expressionbuilder.Constant;
import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import expressionbuilder.ExpressionException;
import expressionsimplifymethods.ExpressionCollection;
import java.util.HashSet;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import solveequationmethods.SolveMethods;

public class GeneralEquationMethods {

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
    public void solveByEquivalenceTransformationTest() {
        try {
            // Test: f = 5*exp(x^4-7) = 2. Lösungen sind (7+ln(2/5))^(1/4), -(7+ln(2/5))^(1/4).
            Expression f = Expression.build("5*exp(x^4-7)", null);
            SolveMethods.setSolveTries(100);
            ExpressionCollection zeros = SolveMethods.solveGeneralEquation(f, Expression.TWO, "x");
            assertTrue(zeros.getBound() == 2);
            Expression zeroOne = Expression.build("(7+ln(2/5))^(1/4)", null);
            Expression zeroTwo = Expression.build("-(7+ln(2/5))^(1/4)", null);
            assertTrue(zeros.contains(zeroOne));
            assertTrue(zeros.contains(zeroTwo));
        } catch (ExpressionException | EvaluationException e) {
            fail("Die Gleichung konnte werden.");
        }
    }
    
    @Test
    public void solvePolynomialEquationTest1() {
        try {
            // Test: f = x^3+3*x^2-5*x+1 = 0. Lösungen sind 1, -2-5^(1/2), 5^(1/2)-2.
            Expression f = Expression.build("x^3+3*x^2-5*x+1", new HashSet<String>());
            SolveMethods.setSolveTries(100);
            ExpressionCollection zeros = SolveMethods.solveGeneralEquation(f, Expression.ZERO, "x");
            assertTrue(zeros.getBound() == 3);
            assertTrue(zeros.contains(Expression.ONE));
            assertTrue(zeros.contains(new Constant(-2).sub(new Constant(5).pow(1, 2))));
            assertTrue(zeros.contains(new Constant(5).pow(1, 2).sub(2)));
        } catch (ExpressionException | EvaluationException e) {
            fail("Die Gleichung konnte werden.");
        }
    }

    @Test
    public void solvePolynomialEquationTest2() {
        try {
            // Test: f = x^4+2 = 0. Keine Lösungen.
            Expression f = Expression.build("x^4+2", new HashSet<String>());
            SolveMethods.setSolveTries(100);
            ExpressionCollection zeros = SolveMethods.solveGeneralEquation(f, Expression.ZERO, "x");
            assertTrue(zeros.isEmpty());
        } catch (ExpressionException | EvaluationException e) {
            fail("Die Gleichung konnte werden.");
        }
    }

}

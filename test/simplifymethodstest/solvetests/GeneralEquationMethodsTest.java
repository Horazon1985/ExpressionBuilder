package simplifymethodstest.solvetests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.ONE;
import static expressionbuilder.Expression.TEN;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import solveequationmethods.SolveMethods;

public class GeneralEquationMethodsTest {

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
            fail(e.getMessage());
        }
    }

    @Test
    public void solveZeroQuotientTest() {
        try {
            // Test: f = (x^2+5*x-14)/(x-2) = 0. Die Lösung ist -7. Die Nullstelle 2 des Nenners wird aussortiert.
            Expression f = Expression.build("(x^2+5*x-14)/(x-2)", null);
            SolveMethods.setSolveTries(100);
            ExpressionCollection zeros = SolveMethods.solveGeneralEquation(f, Expression.ZERO, "x");
            assertTrue(zeros.getBound() == 1);
            Expression zeroOne = new Constant(-7);
            assertTrue(zeros.contains(zeroOne));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solvePolynomialEquationTest1() {
        try {
            // Test: f = x^3+3*x^2-5*x+1 = 0. Lösungen sind 1, -2-5^(1/2), 5^(1/2)-2.
            Expression f = Expression.build("x^3+3*x^2-5*x+1", null);
            SolveMethods.setSolveTries(100);
            ExpressionCollection zeros = SolveMethods.solveGeneralEquation(f, Expression.ZERO, "x");
            assertTrue(zeros.getBound() == 3);
            assertTrue(zeros.contains(Expression.ONE));
            assertTrue(zeros.contains(new Constant(-2).sub(new Constant(5).pow(1, 2))));
            assertTrue(zeros.contains(new Constant(5).pow(1, 2).sub(2)));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solvePolynomialEquationTest2() {
        try {
            // Test: f = x^4+2 = 0. Keine Lösungen.
            Expression f = Expression.build("x^4+2", null);
            SolveMethods.setSolveTries(100);
            ExpressionCollection zeros = SolveMethods.solveGeneralEquation(f, Expression.ZERO, "x");
            assertTrue(zeros.isEmpty());
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solvePolynomialEquationTest3() {
        try {
            // Test: f = 14+93*x+125*x^2+51*x^3+5*x^4 = 0. Die Lösungen sind -1, -2, -7, -1/5.
            Expression f = Expression.build("14+93*x+125*x^2+51*x^3+5*x^4", null);
            SolveMethods.setSolveTries(100);
            ExpressionCollection zeros = SolveMethods.solveGeneralEquation(f, Expression.ZERO, "x");
            assertTrue(zeros.getBound() == 4);
            assertTrue(zeros.contains(new Constant(-1)));
            assertTrue(zeros.contains(new Constant(-2)));
            assertTrue(zeros.contains(new Constant(-7)));
            assertTrue(zeros.contains(new Constant(-1).div(5)));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void solveFunctionalEquationTest1() {
        try {
            // Test: f = exp(x) = 3. Keine Lösungen.
            Expression f = Expression.build("exp(x)", null);
            SolveMethods.setSolveTries(100);
            ExpressionCollection zeros = SolveMethods.solveGeneralEquation(f, Expression.THREE, "x");
            assertTrue(zeros.getBound() == 1);
            assertTrue(zeros.contains(Expression.THREE.ln()));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveFunctionalEquationTest2() {
        try {
            // Test: f = sin(x) = 1/2. Lösungen sind pi/6 + 2*pi*K_1 und 5*pi/6 + 2*pi*K_1.
            Expression f = Expression.build("sin(x)", null);
            SolveMethods.setSolveTries(100);
            ExpressionCollection zeros = SolveMethods.solveGeneralEquation(f, Expression.ONE.div(2), "x");
            assertTrue(zeros.getBound() == 2);
            Expression zeroOne = Expression.PI.mult(ONE.div(6).add(Expression.TWO.mult(Variable.create("K_1"))));
            Expression zeroTwo = Expression.PI.mult(new Constant(5).div(6).add(Expression.TWO.mult(Variable.create("K_1"))));
            assertTrue(zeros.contains(zeroOne));
            assertTrue(zeros.contains(zeroTwo));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveFractionalEquationTest() {
        try {
            // Test: f = 1/x+2/(x+1) = 2*x/(2*x-1). Lösungen sind 1, 1/2-3^(1/2)/2 und 1/2+3^(1/2)/2.
            Expression f = Expression.build("1/x+2/(x+1)", null);
            Expression g = Expression.build("2*x/(2*x-1)", null);
            SolveMethods.setSolveTries(100);
            ExpressionCollection zeros = SolveMethods.solveGeneralEquation(f, g, "x");
            assertTrue(zeros.getBound() == 3);
            Expression zeroOne = Expression.ONE.div(2).add(Expression.THREE.pow(1, 2).div(2));
            Expression zeroTwo = Expression.ONE.div(2).sub(Expression.THREE.pow(1, 2).div(2));
            assertTrue(zeros.contains(Expression.ONE));
            assertTrue(zeros.contains(zeroOne));
            assertTrue(zeros.contains(zeroTwo));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveEquationBySubstitutionTest() {
        try {
            // Test: f = exp(x^2+5*x-1) = 10. Lösungen sind -5/2-(29/4+ln(10))^(1/2) und (29/4+ln(10))^(1/2)-5/2.
            Expression f = Expression.build("exp(x^2+5*x-1)", null);
            SolveMethods.setSolveTries(100);
            ExpressionCollection zeros = SolveMethods.solveGeneralEquation(f, new Constant(10), "x");
            assertTrue(zeros.getBound() == 2);
            Expression zeroOne = new Constant(-5).div(2).sub((new Constant(29).div(4).add(TEN.ln())).pow(1, 2));
            Expression zeroTwo = new Constant(29).div(4).add(TEN.ln()).pow(1, 2).sub(new Constant(5).div(2));
            assertTrue(zeros.contains(zeroOne));
            assertTrue(zeros.contains(zeroTwo));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
}

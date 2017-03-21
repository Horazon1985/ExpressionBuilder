package expression.computationtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import abstractexpressions.expression.equation.PolynomialAlgebraUtils;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.TEN;
import static abstractexpressions.expression.classes.Expression.THREE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.basic.ExpressionCollection;
import static abstractexpressions.expression.classes.Expression.PI;
import static abstractexpressions.expression.classes.Expression.TWO;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import abstractexpressions.expression.equation.SolveGeneralEquationUtils;
import basic.MathToolTestBase;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SolveGeneralEquationTests extends MathToolTestBase {

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
            Expression f = Expression.build("5*exp(x^4-7)");
            ExpressionCollection zeros = SolveGeneralEquationUtils.solveEquation(f, Expression.TWO, "x");
            Expression zeroOne = Expression.build("(7+ln(2/5))^(1/4)");
            Expression zeroTwo = Expression.build("-(7+ln(2/5))^(1/4)");
            expectedResults = new Object[]{2, zeroOne, zeroTwo};
            results = new Object[]{zeros.getBound(), zeros.get(0), zeros.get(1)};
            assertTrue(zeros.getBound() == 2);
            assertTrue(zeros.containsExpression(zeroOne));
            assertTrue(zeros.containsExpression(zeroTwo));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveZeroQuotientTest() {
        try {
            // Test: f = (x^2+5*x-14)/(x-2) = 0. Die Lösung ist -7. Die Nullstelle 2 des Nenners wird aussortiert.
            Expression f = Expression.build("(x^2+5*x-14)/(x-2)");
            ExpressionCollection zeros = SolveGeneralEquationUtils.solveEquation(f, ZERO, "x");
            Expression zeroOne = new Constant(-7);
            expectedResults = new Object[]{1, zeroOne};
            results = new Object[]{zeros.getBound(), zeros.get(0)};
            assertTrue(zeros.getBound() == 1);
            assertTrue(zeros.containsExpression(zeroOne));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solvePolynomialEquationTest() {
        try {
            // Test: f = x^3+3*x^2-5*x+1 = 0. Lösungen sind 1, -2-5^(1/2), 5^(1/2)-2.
            Expression f = Expression.build("x^3+3*x^2-5*x+1");
            ExpressionCollection zeros = SolveGeneralEquationUtils.solveEquation(f, ZERO, "x");
            Expression zeroOne = ONE;
            Expression zeroTwo = new Constant(-2).sub(new Constant(5).pow(1, 2));
            Expression zeroThree = new Constant(5).pow(1, 2).sub(2);
            expectedResults = new Object[]{3, zeroOne, zeroTwo, zeroThree};
            results = new Object[]{zeros.getBound(), zeros.get(0), zeros.get(1), zeros.get(2)};
            assertTrue(zeros.getBound() == 3);
            assertTrue(zeros.containsExpression(zeroOne));
            assertTrue(zeros.containsExpression(zeroTwo));
            assertTrue(zeros.containsExpression(zeroThree));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solvePolynomialEquationNoSolutionTest() {
        try {
            // Test: f = x^4+2 = 0. Keine Lösungen.
            Expression f = Expression.build("x^4+2");
            ExpressionCollection zeros = SolveGeneralEquationUtils.solveEquation(f, ZERO, "x");
            expectedResults = new Object[]{0};
            results = new Object[]{zeros.getBound()};
            assertTrue(zeros.isEmpty());
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solvePolynomialEquationWithRationalZerosTest() {
        try {
            // Test: f = 14+93*x+125*x^2+51*x^3+5*x^4 = 0. Die Lösungen sind -1, -2, -7, -1/5.
            Expression f = Expression.build("14+93*x+125*x^2+51*x^3+5*x^4");
            ExpressionCollection zeros = SolveGeneralEquationUtils.solveEquation(f, ZERO, "x");
            Expression zeroOne = MINUS_ONE;
            Expression zeroTwo = MINUS_ONE.div(5);
            Expression zeroThree = new Constant(-2);
            Expression zeroFour = new Constant(-7);
            expectedResults = new Object[]{4, zeroOne, zeroTwo, zeroThree, zeroFour};
            results = new Object[]{zeros.getBound(), zeros.get(0), zeros.get(1), zeros.get(2), zeros.get(3)};
            assertTrue(zeros.getBound() == 4);
            assertTrue(zeros.containsExpression(zeroOne));
            assertTrue(zeros.containsExpression(new Constant(-2)));
            assertTrue(zeros.containsExpression(new Constant(-7)));
            assertTrue(zeros.containsExpression(new Constant(-1).div(5)));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solvePolynomialEquationByFactorizingTest() {
        try {
            /* 
             Test: f = 2*x^9+x^11+126*x^7+294*x^3+147*x^5+2058*x-(686+6*x^10+42*x^6+21*x^8+882*x^4+343*x^2)
             = (x^3-7)^3*(x^2-6*x+2). Die Lösungen sind 3-7^(1/2), 3+7^(1/2), 7^(1/3).
             */
            Expression f = Expression.build("2*x^9+x^11+126*x^7+294*x^3+147*x^5+2058*x-(686+6*x^10+42*x^6+21*x^8+882*x^4+343*x^2)");
            ExpressionCollection zeros = SolveGeneralEquationUtils.solveEquation(f, ZERO, "x");
            Expression zeroOne = new Constant(3).sub(new Constant(7).pow(1, 2));
            Expression zeroTwo = new Constant(3).add(new Constant(7).pow(1, 2));
            Expression zeroThree = new Constant(7).pow(1, 3);
            expectedResults = new Object[]{3, zeroOne, zeroTwo, zeroThree};
            results = new Object[]{zeros.getBound(), zeros.get(0), zeros.get(1), zeros.get(2)};
            assertTrue(zeros.getBound() == 3);
            assertTrue(zeros.containsExpression(zeroOne));
            assertTrue(zeros.containsExpression(zeroTwo));
            assertTrue(zeros.containsExpression(zeroThree));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solvePolynomialEquationByReducingTest() {
        try {
            /* 
             Test: f = (1+exp(a))*x^2-5-5*exp(a) = 0. Die Lösungen sind -5^(1/2), 5^(1/2).
             */
            ExpressionCollection coefficients = new ExpressionCollection(Expression.build("-5-5*exp(a)"), ZERO, Expression.build("1+exp(a)"));
            ExpressionCollection zeros = PolynomialAlgebraUtils.solvePolynomialEquation(coefficients, "x");
            Expression zeroOne = new Constant(5).pow(1, 2);
            Expression zeroTwo = MINUS_ONE.mult(new Constant(5).pow(1, 2));
            expectedResults = new Object[]{2, zeroOne, zeroTwo};
            results = new Object[]{zeros.getBound(), zeros.get(0), zeros.get(1)};
            assertTrue(zeros.getBound() == 2);
            assertTrue(zeros.containsExpression(zeroOne));
            assertTrue(zeros.containsExpression(zeroTwo));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveFunctionalEquationTest1() {
        try {
            // Test: f = exp(x) = 3. Keine Lösungen.
            Expression f = Expression.build("exp(x)");
            ExpressionCollection zeros = SolveGeneralEquationUtils.solveEquation(f, THREE, "x");
            assertTrue(zeros.getBound() == 1);
            assertTrue(zeros.containsExpression(Expression.THREE.ln()));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveFunctionalEquationTest2() {
        try {
            // Test: f = sin(x) = 1/2. Lösungen sind pi/6 + 2*pi*K_1 und 5*pi/6 + 2*pi*K_1.
            Expression f = Expression.build("sin(x)");
            ExpressionCollection zeros = SolveGeneralEquationUtils.solveEquation(f, ONE.div(2), "x");
            Expression zeroOne = PI.mult(ONE.div(6).add(TWO.mult(Variable.create("K_1"))));
            Expression zeroTwo = PI.mult(new Constant(5).div(6).add(TWO.mult(Variable.create("K_1"))));
            expectedResults = new Object[]{2, zeroOne, zeroTwo};
            results = new Object[]{zeros.getBound(), zeros.get(0), zeros.get(1)};
            assertTrue(zeros.getBound() == 2);
            assertTrue(zeros.containsExpression(zeroOne));
            assertTrue(zeros.containsExpression(zeroTwo));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveFractionalEquationTest() {
        try {
            // Test: f = 1/x+2/(x+1) = 2*x/(2*x-1). Lösungen sind 1, 1/2-3^(1/2)/2 und 1/2+3^(1/2)/2.
            Expression f = Expression.build("1/x+2/(x+1)");
            Expression g = Expression.build("2*x/(2*x-1)");
            ExpressionCollection zeros = SolveGeneralEquationUtils.solveEquation(f, g, "x");
            Expression zeroOne = ONE;
            Expression zeroTwo = ONE.div(2).sub(THREE.pow(1, 2).div(2));
            Expression zeroThree = ONE.div(2).add(THREE.pow(1, 2).div(2));
            expectedResults = new Object[]{3, zeroOne, zeroTwo, zeroThree};
            results = new Object[]{zeros.getBound(), zeros.get(0), zeros.get(1), zeros.get(2)};
            assertTrue(zeros.getBound() == 3);
            assertTrue(zeros.containsExpression(zeroOne));
            assertTrue(zeros.containsExpression(zeroTwo));
            assertTrue(zeros.containsExpression(zeroThree));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveEquationBySubstitutionTest() {
        try {
            // Test: f = exp(x^2+5*x-1) = 10. Lösungen sind -5/2-(29/4+ln(10))^(1/2) und (29/4+ln(10))^(1/2)-5/2.
            Expression f = Expression.build("exp(x^2+5*x-1)");
            ExpressionCollection zeros = SolveGeneralEquationUtils.solveEquation(f, new Constant(10), "x");
            Expression zeroOne = new Constant(-5).div(2).sub((new Constant(29).div(4).add(TEN.ln())).pow(1, 2));
            Expression zeroTwo = new Constant(29).div(4).add(TEN.ln()).pow(1, 2).sub(new Constant(5).div(2));
            expectedResults = new Object[]{2, zeroOne, zeroTwo};
            results = new Object[]{zeros.getBound(), zeros.get(0), zeros.get(1)};
            assertTrue(zeros.getBound() == 2);
            assertTrue(zeros.containsExpression(zeroOne));
            assertTrue(zeros.containsExpression(zeroTwo));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

}

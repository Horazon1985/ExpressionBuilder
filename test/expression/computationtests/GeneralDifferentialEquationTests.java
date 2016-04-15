package expression.computationtests;

import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.FOUR;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.THREE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.differentialequation.SolveGeneralDifferentialEquationMethods;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyPolynomialMethods;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeneralDifferentialEquationTests {

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
    public void getOrderAndSubOrderOfDifferentialEquationTest1() {
        try {
            // DGL: f = x^2*y''''-sin(x)*y'''-x^5/7. Dann ist ord = 4, subOrd = 3. 
            Expression f = Expression.build("x^2*y''''-sin(x)*y'''-x^5/7", null);
            int ord = SolveGeneralDifferentialEquationMethods.getOrderOfDifferentialEquation(f, "y");
            int subOrd = SolveGeneralDifferentialEquationMethods.getSubOrderOfDifferentialEquation(f, "y");
            assertTrue(ord == 4);
            assertTrue(subOrd == 3);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getOrderAndSubOrderOfDifferentialEquationTest2() {
        try {
            // DGL: f = y_1'''''+x^2*y''''-sin(x)*y'''-x^5/7. Dann ist ord = 4, subOrd = 3. 
            Expression f = Expression.build("y_1'''''+x^2*y''''-sin(x)*y'''-x^5/7", null);
            int ord = SolveGeneralDifferentialEquationMethods.getOrderOfDifferentialEquation(f, "y");
            int subOrd = SolveGeneralDifferentialEquationMethods.getSubOrderOfDifferentialEquation(f, "y");
            assertTrue(ord == 4);
            assertTrue(subOrd == 3);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveDiffEqWithSeparableVariablesExplicitTest() {
        try {
            // DGL: y' = y^5*x^3. Lösungen sind: y_1 = 0, y_2 = (-1/(x^4+4*C_1))^(1/4) und y_3 = -(-1/(x^4+4*C_1))^(1/4).
            Expression rightSide = Expression.build("y^5*x^3", null);
            ExpressionCollection solutions = SolveGeneralDifferentialEquationMethods.solveDifferentialEquation(Variable.create("y'"), rightSide, "x", "y");
            assertTrue(solutions.getBound() == 3);
            assertTrue(solutions.contains(ZERO));
            Expression solutionOne = MINUS_ONE.div(Variable.create("x").pow(4).add(FOUR.mult(Variable.create("C_1")))).pow(1, 4);
            Expression solutionTwo = MINUS_ONE.mult(MINUS_ONE.div(Variable.create("x").pow(4).add(FOUR.mult(Variable.create("C_1")))).pow(1, 4));
            assertTrue(solutions.containsExquivalent(solutionOne));
            assertTrue(solutions.containsExquivalent(solutionTwo));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveDiffEqWithSeparableVariablesImplicitTest() {
        try {
            // DGL: y' = x^2/ln(y). Implizite Lösungen sind: y*(ln(y) - 1) - (x^3/3 + C_1) = 0.
            Expression rightSide = Expression.build("x^2/ln(y)", null);
            ExpressionCollection solutions = SolveGeneralDifferentialEquationMethods.solveDifferentialEquation(Variable.create("y'"), rightSide, "x", "y");
            assertTrue(solutions.getBound() == 1);
            Expression solution = Variable.create("y").mult(Variable.create("y").ln().sub(1)).sub(SimplifyPolynomialMethods.getPolynomialFromCoefficients("x", "C_1", null, null, ONE.div(3)));
            assertTrue(solutions.containsExquivalent(solution));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveDiffEqLinearAndHomogeneousWithConstantCoefficientsTest1() {
        try {
            // DGL: y''' - 3*y' - 2*y = 0. Lösungen sind: y = exp(-x)*(C_1+C_2*x)+C_3*exp(2*x).
            Expression leftSide = Expression.build("y''' - 3*y' - 2*y", null);
            ExpressionCollection solutions = SolveGeneralDifferentialEquationMethods.solveDifferentialEquation(leftSide, ZERO, "x", "y");
            assertTrue(solutions.getBound() == 1);
            Expression solution = MINUS_ONE.mult(Variable.create("x")).exp().mult(SimplifyPolynomialMethods.getPolynomialFromCoefficients("x", "C_1", "C_2")).add(
                    Variable.create("C_3").mult(TWO.mult(Variable.create("x")).exp()));
            assertTrue(solutions.containsExquivalent(solution));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveDiffEqLinearAndHomogeneousWithConstantCoefficientsTest2() {
        try {
            // DGL: y''-7*y'+12*y = 0. Lösungen sind: y = C_1*exp(3*x)+C_2*exp(4*x).
            Expression leftSide = Expression.build("y''-7*y'+12*y", null);
            ExpressionCollection solutions = SolveGeneralDifferentialEquationMethods.solveDifferentialEquation(leftSide, ZERO, "x", "y");
            assertTrue(solutions.getBound() == 1);
            Expression solution = Variable.create("C_1").mult(THREE.mult(Variable.create("x")).exp()).add(
                    Variable.create("C_2").mult(FOUR.mult(Variable.create("x")).exp()));
            assertTrue(solutions.containsExquivalent(solution));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveDiffEqLinearWithConstantCoefficientsTest() {
        try {
            // DGL: y'' - 2*y' + y = x^2*exp(x). Lösungen sind: exp(-x)*(C_1+C_2*x)+C_3*exp(2*x).
            Expression leftSide = Expression.build("y'' - 2*y' + y", null);
            Expression rightSide = Expression.build("x^2*exp(x)", null);
            ExpressionCollection solutions = SolveGeneralDifferentialEquationMethods.solveDifferentialEquation(leftSide, rightSide, "x", "y");
            assertTrue(solutions.getBound() == 1);
            Expression solution = Variable.create("x").exp().mult(SimplifyPolynomialMethods.getPolynomialFromCoefficients("x", "C_1", "C_2", null, null, ONE.div(12)));
            assertTrue(solutions.containsExquivalent(solution));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

}

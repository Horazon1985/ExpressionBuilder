package expression.computationtests;

import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.FOUR;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.diferentialequation.SolveGeneralDifferentialEquationMethods;
import abstractexpressions.expression.equation.SolveGeneralEquationMethods;
import abstractexpressions.expression.utilities.ExpressionCollection;
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
    public void solveDiffEqWithSeparableVariablesTest1() {
        try {
            // DGL: y' = y^5*x^3. Lösungen sind: 0, (-1/(x^4+4*C_1))^(1/4) und -(-1/(x^4+4*C_1))^(1/4).
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
    public void solveDiffEqWithSeparableVariablesTest2() {

    }

    @Test
    public void solveDiffEqLinearAndHomogeneousWithConstantCoefficientsTest1() {

    }

    @Test
    public void solveDiffEqLinearAndHomogeneousWithConstantCoefficientsTest2() {

    }

    @Test
    public void solveDiffEqLinearAndHomogeneousWithConstantCoefficientsTest3() {

    }

    @Test
    public void solveDiffEqLinearWithConstantCoefficientsTest() {

    }

}

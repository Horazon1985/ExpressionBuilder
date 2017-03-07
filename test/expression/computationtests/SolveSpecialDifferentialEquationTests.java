package expression.computationtests;

import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeOperator;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.differentialequation.SolveGeneralDifferentialEquationUtils;
import abstractexpressions.expression.basic.ExpressionCollection;
import basic.MathToolTestBase;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SolveSpecialDifferentialEquationTests extends MathToolTestBase {

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
    public void solveDiffEqWithOnlySecondDerivativeAndFunctionTest() {
        try {
            // DGL: y'' = y^2. Implizite Lösungen sind: int(1/((2*y^3)/3+C_1)^(1/2),y)+x+C_2 = 0, int(1/((2*y^3)/3+C_1)^(1/2),y)-(x+C_3) = 0.
            Expression leftSide = Expression.build("y''");
            Expression rightSide = Expression.build("y^2");
            ExpressionCollection solutions = SolveGeneralDifferentialEquationUtils.solveDifferentialEquation(leftSide, rightSide, "x", "y");
            assertTrue(solutions.getBound() == 2);
            Expression solutionOne = new Operator(TypeOperator.integral, new Object[]{ONE.div(TWO.mult(Variable.create("y").pow(3)).div(3).add(Variable.create("C_1")).pow(1, 2)), "y"}).add(Variable.create("x")).add(Variable.create("C_2"));
            Expression solutionTwo = new Operator(TypeOperator.integral, new Object[]{ONE.div(TWO.mult(Variable.create("y").pow(3)).div(3).add(Variable.create("C_1")).pow(1, 2)), "y"}).sub(Variable.create("x").add(Variable.create("C_3")));
            assertTrue(solutions.containsExquivalent(solutionOne));
            assertTrue(solutions.containsExquivalent(solutionTwo));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveDiffEqWithOnlythirdAndFirstDerivativeTest() {
        try {
            // DGL: y''' = y'^2. Keine algebraischen Lösungen.
            Expression leftSide = Expression.build("y'''");
            Expression rightSide = Expression.build("y'^2");
            ExpressionCollection solutions = SolveGeneralDifferentialEquationUtils.solveDifferentialEquation(leftSide, rightSide, "x", "y");
            assertTrue(solutions.isEmpty());
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void solveBernoulliDifferentialEquationTest() {
        try {
            // DGL: y' -4*x*y + 6*y^2 = 0. Lösungen sind exp(2*x^2)/(C_1+6*int(exp(2*x^2),x)).
            Expression leftSide = Expression.build("y' -4*x*y + 6*y^2");
            ExpressionCollection solutions = SolveGeneralDifferentialEquationUtils.solveDifferentialEquation(leftSide, ZERO, "x", "y");
            assertTrue(solutions.getBound() == 1);
            Expression solution = TWO.mult(Variable.create("x").pow(2)).exp().div(Variable.create("C_1").add(new Constant(6).mult(new Operator(TypeOperator.integral, new Object[]{TWO.mult(Variable.create("x").pow(2)).exp(), "x"}))));
            assertTrue(solutions.containsExquivalent(solution));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    
}

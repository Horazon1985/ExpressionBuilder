/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expression.computationtests;

import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeOperator;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.differentialequation.SolveGeneralDifferentialEquationMethods;
import abstractexpressions.expression.utilities.ExpressionCollection;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Sergei
 */
public class SolveSpecialDifferentialEquationTests {

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
            // DGL: y'' = y^2. Implizite Lösungen sind: int(1/((2*y^3)/3+C_1)^(1/2),y)+x+C_4 = 0, int(1/((2*y^3)/3+C_1)^(1/2),y)-(x+C_4) = 0.
            Expression leftSide = Expression.build("y''", null);
            Expression rightSide = Expression.build("y^2", null);
            ExpressionCollection solutions = SolveGeneralDifferentialEquationMethods.solveDifferentialEquation(leftSide, rightSide, "x", "y");
            assertTrue(solutions.getBound() == 2);
            Expression solutionOne = new Operator(TypeOperator.integral, new Object[]{ONE.div(TWO.mult(Variable.create("y").pow(3)).div(3).add(Variable.create("C_1")).pow(1, 2)), "y"}).add(Variable.create("x")).add(Variable.create("C_4"));
            Expression solutionTwo = new Operator(TypeOperator.integral, new Object[]{ONE.div(TWO.mult(Variable.create("y").pow(3)).div(3).add(Variable.create("C_1")).pow(1, 2)), "y"}).sub(Variable.create("x").add(Variable.create("C_5")));
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
            Expression leftSide = Expression.build("y'''", null);
            Expression rightSide = Expression.build("y'^2", null);
            ExpressionCollection solutions = SolveGeneralDifferentialEquationMethods.solveDifferentialEquation(leftSide, rightSide, "x", "y");
            assertTrue(solutions.isEmpty());
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void solveBernoulliDifferentialEquationTest() {
        try {
            // DGL: y' -4*x*y + 6*y^2 = 0. Lösungen sind exp(2*x^2)/(C_1+6*int(exp(2*x^2),x)).
            Expression leftSide = Expression.build("y' -4*x*y + 6*y^2", null);
            ExpressionCollection solutions = SolveGeneralDifferentialEquationMethods.solveDifferentialEquation(leftSide, ZERO, "x", "y");
            assertTrue(solutions.getBound() == 1);
            Expression solution = TWO.mult(Variable.create("x").pow(2)).exp().div(Variable.create("C_1").add(new Constant(6).mult(new Operator(TypeOperator.integral, new Object[]{TWO.mult(Variable.create("x").pow(2)).exp(), "x"}))));
            assertTrue(solutions.containsExquivalent(solution));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    
}

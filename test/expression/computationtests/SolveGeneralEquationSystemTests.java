package expression.computationtests;

import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.THREE;
import static abstractexpressions.expression.classes.Expression.TWO;
import abstractexpressions.expression.equation.SolveGeneralSystemOfEquationsUtils;
import abstractexpressions.expression.equation.SolveGeneralSystemOfEquationsUtils.SolutionType;
import basic.MathToolTestBase;
import java.util.ArrayList;
import exceptions.ExpressionException;
import exceptions.NotAlgebraicallySolvableException;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import testrunner.TestUtilities;

public class SolveGeneralEquationSystemTests extends MathToolTestBase {

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
    public void solvePolynomialSystemOfEquationsTest1() {
        try {
            Expression f = Expression.build("x^2+x*y-10");
            Expression g = Expression.build("y^2+5*x*y-39");
            List<String> vars = new ArrayList<>();
            vars.add("x");
            vars.add("y");
            List<Expression[]> solutions;
            try {
                solutions = SolveGeneralSystemOfEquationsUtils.solvePolynomialSystemOfEquations(new Expression[]{f, g}, vars, SolutionType.ALL);
                expectedResults = new Object[]{2, TWO, THREE, new Constant(-2), new Constant(-3)};
                results = new Object[]{solutions.size(), solutions.get(0)[0], solutions.get(0)[1], solutions.get(1)[0], solutions.get(1)[1]};
                assertTrue(solutions.size() == 2);
                assertTrue(solutions.get(0)[0].equals(TWO));
                assertTrue(solutions.get(0)[1].equals(THREE));
                assertTrue(solutions.get(1)[0].equals(new Constant(-2)));
                assertTrue(solutions.get(1)[1].equals(new Constant(-3)));
            } catch (NotAlgebraicallySolvableException e) {
                fail(e.getMessage());
            }
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveGeneralSystemOfEquationsTest1() {
        try {
            Expression f = Expression.build("x+y-10");
            Expression g = Expression.build("y^2+y-6");
            Expression h = Expression.build("3^y+9^y-90");
            List<String> vars = new ArrayList<>();
            vars.add("x");
            vars.add("y");
            List<Expression[]> solutions;
            try {
                solutions = SolveGeneralSystemOfEquationsUtils.solveGeneralSystemOfEquations(new Expression[]{f, g, h}, vars);
                expectedResults = new Object[]{1, new Constant(8), TWO};
                results = new Object[]{solutions.size(), solutions.get(0)[0], solutions.get(0)[1]};
                assertTrue(solutions.size() == 1);
                assertTrue(solutions.get(0)[0].equals(new Constant(8)));
                assertTrue(solutions.get(0)[1].equals(TWO));
            } catch (NotAlgebraicallySolvableException e) {
                fail(e.getMessage());
            }
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    //solvesystem(x^2-6*x*y+1=0,2*sin(y)-1/3=0,z^2=7,x,y,z)
    @Test
    public void solveGeneralSystemOfEquationsTest2() {
        try {
            Expression f = Expression.build("x^2-6*x*y+1");
            Expression g = Expression.build("2*sin(y)-1/3");
            Expression h = Expression.build("z^2-7");
            List<String> vars = new ArrayList<>();
            vars.add("x");
            vars.add("y");
            vars.add("z");
            List<Expression[]> solutions;
            try {
                solutions = SolveGeneralSystemOfEquationsUtils.solveGeneralSystemOfEquations(new Expression[]{f, g, h}, vars);
                // Lösungen sind zu komplex, hier wird nur die Anzahl geprüft.
                expectedResults = new Object[]{8};
                results = new Object[]{solutions.size()};
                assertTrue(solutions.size() == 8);
            } catch (NotAlgebraicallySolvableException e) {
                fail(e.getMessage());
            }
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

}

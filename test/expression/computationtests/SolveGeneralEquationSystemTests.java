package expression.computationtests;

import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.equation.SolveGeneralSystemOfEquationsMethods;
import java.util.ArrayList;
import exceptions.ExpressionException;
import exceptions.NotAlgebraicallySolvableException;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SolveGeneralEquationSystemTests {

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
            Expression f = Expression.build("x^2+x*y-10", null);
            Expression g = Expression.build("y^2+5*x*y-39", null);
            ArrayList<String> vars = new ArrayList<>();
            vars.add("x");
            vars.add("y");
            ArrayList<Expression[]> solutions;
            try {
                solutions = SolveGeneralSystemOfEquationsMethods.solveTriangularPolynomialSystemOfEquations(new Expression[]{f, g}, vars);
                assertTrue(solutions.size() == 2);
                assertTrue(solutions.get(0)[0].equals(new Constant(2)));
                assertTrue(solutions.get(0)[1].equals(new Constant(3)));
                assertTrue(solutions.get(1)[0].equals(new Constant(-2)));
                assertTrue(solutions.get(1)[1].equals(new Constant(-3)));
            } catch (NotAlgebraicallySolvableException e) {
                fail(e.getMessage());
            }
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

}

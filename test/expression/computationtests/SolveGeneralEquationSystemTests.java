package expression.computationtests;

import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.equation.SolveGeneralSystemOfEquationsMethods;
import abstractexpressions.expression.equation.SolveGeneralSystemOfEquationsMethods.SolutionType;
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
                solutions = SolveGeneralSystemOfEquationsMethods.solvePolynomialSystemOfEquations(new Expression[]{f, g}, vars, SolutionType.ALL);
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

    @Test
    public void solveGeneralSystemOfEquationsTest1() {
        try {
            Expression f = Expression.build("x+y-10", null);
            Expression g = Expression.build("y^2+y-6", null);
            Expression h = Expression.build("3^y+9^y-90", null);
            ArrayList<String> vars = new ArrayList<>();
            vars.add("x");
            vars.add("y");
            ArrayList<Expression[]> solutions;
            try {
                solutions = SolveGeneralSystemOfEquationsMethods.solveGeneralSystemOfEquations(new Expression[]{f, g, h}, vars);
                assertTrue(solutions.size() == 1);
                assertTrue(solutions.get(0)[0].equals(new Constant(8)));
                assertTrue(solutions.get(0)[1].equals(new Constant(2)));
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
            Expression f = Expression.build("x^2-6*x*y+1", null);
            Expression g = Expression.build("2*sin(y)-1/3", null);
            Expression h = Expression.build("z^2-7", null);
            ArrayList<String> vars = new ArrayList<>();
            vars.add("x");
            vars.add("y");
            vars.add("z");
            ArrayList<Expression[]> solutions;
            try {
                solutions = SolveGeneralSystemOfEquationsMethods.solveGeneralSystemOfEquations(new Expression[]{f, g, h}, vars);
                // Lösungen sind zu komplex, hier wird nur die Anzahl geprüft.
                assertTrue(solutions.size() == 8);
            } catch (NotAlgebraicallySolvableException e) {
                fail(e.getMessage());
            }
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }
    

}

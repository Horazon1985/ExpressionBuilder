package expression.generaltests;

import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyUtilities;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimplifyUtilitiesTest {

    Expression f, g, h;
    Expression exprWithMultipleVariables;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void defineExpressions() throws Exception {
        // f = 1*x*sin(x)
        f = new Constant(1).mult(Variable.create("x")).mult(Variable.create("x").sin());
        // g = x*1*sin(x)
        g = Variable.create("x").mult(ONE).mult(Variable.create("x").sin());
        // h = 1*1*1
        h = ONE.mult(ONE).mult(ONE);
        Variable x = Variable.create("x");
        Variable y = Variable.create("y");
        // expr = (1*x*sin(x)*1*y^2)/(5*y^3)
        exprWithMultipleVariables = ONE.mult(x).mult(x.sin()).mult(1).mult(y.pow(2)).div(new Constant(5).mult(y.pow(3)));
    }

    @Test
    public void getFactorsOfFTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactors(f);
        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getFactorsOfGTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactors(g);
        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getFactorsOfGIfApproximatingTest() {
        g = g.turnToApproximate();
        ExpressionCollection factors = SimplifyUtilities.getFactors(g);
        assertTrue(factors.getBound() == 2);
        // Notwendig für weitere Tests!
        g = g.turnToPrecise();
    }

    @Test
    public void getFactorsOfHTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactors(h);
        assertTrue(factors.getBound() == 1);
    }

    @Test
    public void getFactorsOfEnumeratorOfFTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactorsOfNumeratorInExpression(f);
        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getFactorsOfDenominatorOfFTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
        assertTrue(factors.isEmpty());
    }

    @Test
    public void getFactorsOfEnumeratorOfGTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactorsOfNumeratorInExpression(g);
        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getFactorsOfDenominatorOfGTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactorsOfDenominatorInExpression(g);
        assertTrue(factors.isEmpty());
    }

    @Test
    public void getNonConstantFactorsOfEnumeratorOfExprTest() {
        ExpressionCollection factors = SimplifyUtilities.getNonConstantFactorsOfNumeratorInExpression(exprWithMultipleVariables, "x");
        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getNonConstantFactorsOfDenominatorOfExprWithRespectToXTest() {
        ExpressionCollection factors = SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(exprWithMultipleVariables, "x");
        assertTrue(factors.isEmpty());
    }

    @Test
    public void getNonConstantFactorsOfDenominatorOfExprWithRespectToYTest() {
        ExpressionCollection factors = SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(exprWithMultipleVariables, "y");
        assertTrue(factors.getBound() == 1);
    }

}
